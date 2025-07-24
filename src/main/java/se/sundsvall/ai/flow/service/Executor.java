package se.sundsvall.ai.flow.service;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.integration.intric.IntricService;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInputRef;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;
import se.sundsvall.ai.flow.model.session.Input;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;
import se.sundsvall.ai.flow.model.support.StringMultipartFile;
import se.sundsvall.dept44.requestid.RequestId;

@Service
public class Executor {

	private static final Logger LOG = LoggerFactory.getLogger(Executor.class);

	private final IntricService intricService;

	public Executor(final IntricService intricService) {
		this.intricService = intricService;
	}

	@Async
	public void executeSession(final String municipalityId, final Session session, final String requestId) {
		try {
			RequestId.init(requestId);
			var flow = session.getFlow();

			// Upload all inputs (files) in the local session that haven't been uploaded before
			uploadMissingInputFilesInSessionToIntric(municipalityId, session);

			// Mark the session as running
			session.setState(Session.State.RUNNING);
			// Execute the steps in the order defined in the flow, running required steps if they exist
			flow.getSteps().stream()
				.map(step -> session.getStepExecution(step.getId()))
				.forEach(step -> executeStepInternal(municipalityId, step, null, true));
			// Mark the session as finished
			session.setState(Session.State.FINISHED);
		} finally {
			RequestId.reset();
		}
	}

	// @Async
	public void executeStep(final String municipalityId, final StepExecution stepExecution, final String input, final boolean runRequiredSteps) {
		var session = stepExecution.getSession();

		// The session must either be running or finished before allowing any individual steps to be executed individually
		if (session.getState() != Session.State.RUNNING && session.getState() != Session.State.FINISHED) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Unable to run step '%s' in flow '%s' for session %s since the session has never been run yet".formatted(stepExecution.getStep().getId(), session.getFlow().getName(), session.getId()));
		}

		// Make sure the step isn't already running
		if (stepExecution.isRunning()) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Unable to run already running step '%s' in flow '%s' for session %s".formatted(stepExecution.getStep().getId(), session.getFlow().getName(), session.getId()));
		}

		executeStepInternal(municipalityId, stepExecution, input, runRequiredSteps);
	}

	void executeStepInternal(final String municipalityId, final StepExecution stepExecution, final String input, final boolean runRequiredSteps) {
		var session = stepExecution.getSession();
		var flow = session.getFlow();
		var step = stepExecution.getStep();

		LOG.info("Executing step '{}' in flow '{}' for session {}", step.getName(), flow.getName(), session.getId());

		// Recursively execute required steps, if "enabled" and if there are any
		if (runRequiredSteps) {
			for (var requiredStepExecution : stepExecution.getRequiredStepExecutions()) {
				// Skip if the required step has already been executed
				if (requiredStepExecution.getIntricSessionId() == null) {
					LOG.info("Triggering step '{}' required by step '{}' in flow '{}' for session {}", requiredStepExecution.getStep().getName(), step.getName(), flow.getName(), session.getId());

					executeStepInternal(municipalityId, requiredStepExecution, input, runRequiredSteps);
				}
			}
		}

		// Mark the step execution as running
		stepExecution.setState(StepExecution.State.RUNNING);

		// Extract inputs that are "regular" flow input references
		var flowInputRefStepInputs = step.getInputs().stream()
			.filter(FlowInputRef.class::isInstance)
			.map(FlowInputRef.class::cast)
			// Skip inputs that are marked as passthrough
			.filter(not(flowInputRef -> flow.getFlowInput(flowInputRef.getInput()).isPassthrough()))
			.toList();

		// Extract inputs that are redirected output from other steps
		var redirectedOutputStepInputs = step.getInputs().stream()
			.filter(RedirectedOutput.class::isInstance)
			.map(RedirectedOutput.class::cast)
			.toList();

		// Add any redirected output inputs to the session
		redirectedOutputStepInputs.forEach(redirectedOutputInput -> {
			// Get the required step execution
			var requiredStepExecution = session.getStepExecutions().get(redirectedOutputInput.getStep());
			// Wrap the output of the required step execution in a StringMultipartFile
			var requiredStepOutputMultipartFile = new StringMultipartFile(session.getFlow().getInputPrefix(), redirectedOutputInput.getName(), requiredStepExecution.getOutput());
			// Add it as an input to the session
			session.addRedirectedOutputAsInput(redirectedOutputInput.getStep(), requiredStepOutputMultipartFile);
		});

		// At this point we may have inputs that haven't been uploaded to Intric yet - upload them
		uploadMissingInputFilesInSessionToIntric(municipalityId, session);

		// Join both input types to get all inputs actually in use for the current step execution
		var inputsInUse = Stream.concat(
			flowInputRefStepInputs.stream().map(FlowInputRef::getInput),
			redirectedOutputStepInputs.stream().map(RedirectedOutput::getStep)).toList();

		// Extract the input files (ie Intric file id:s) for the inputs actually in use for the current step execution
		var inputFilesInUse = session.getAllInput().entrySet().stream()
			.filter(entry -> inputsInUse.contains(entry.getKey()))
			.flatMap(entry -> entry.getValue().stream())
			.map(Input::getIntricFileId)
			.toList();

		// Create an additional instruction on what information lies within each input
		var inputsInUseInfo = session.getInputInfo().entrySet().stream()
			.filter(entry -> inputsInUse.contains(entry.getKey()))
			.map(Map.Entry::getValue)
			.collect(joining());

		// Get the Intric endpoint id
		var intricEndpointId = step.getIntricEndpoint().id();

		try {
			switch (step.getIntricEndpoint().type()) {
				case SERVICE -> {
					LOG.info("Running step {} using SERVICE {}", step.getName(), intricEndpointId);

					// Run the service
					var response = intricService.runService(municipalityId, intricEndpointId, inputFilesInUse, inputsInUseInfo, input);
					// Store the answer in the step execution
					stepExecution.setOutput(response.answer());
				}
				case ASSISTANT -> {
					// Are we asking the initial question or a follow-up?
					if (stepExecution.getIntricSessionId() == null) {
						LOG.info("Running step {} using ASSISTANT {}", step.getName(), intricEndpointId);

						// "Ask" the assistant
						var response = intricService.askAssistant(municipalityId, intricEndpointId, inputFilesInUse, inputsInUseInfo);
						// Store the Intric session id in the step execution to be able to ask follow-ups
						stepExecution.setIntricSessionId(response.sessionId());
						// Store the (current) answer in the step execution
						stepExecution.setOutput(response.answer());
					} else {
						LOG.info("Running FOLLOW-UP on step {} using ASSISTANT {}", step.getName(), intricEndpointId);

						// "Ask" the assistant a follow-up
						var response = intricService.askAssistantFollowup(municipalityId, intricEndpointId, stepExecution.getIntricSessionId(), inputFilesInUse, inputsInUseInfo, input);
						// Store the (current) answer in the step execution
						stepExecution.setOutput(response.answer());
					}
				}
			}
			stepExecution.setState(StepExecution.State.DONE);
		} catch (Exception e) {
			stepExecution.setState(StepExecution.State.ERROR);
			stepExecution.setErrorMessage(e.getMessage());
		}
	}

	void uploadMissingInputFilesInSessionToIntric(final String municipalityId, final Session session) {
		// Upload any missing regular inputs
		session.getInput().values().stream()
			.flatMap(Collection::stream)
			.filter(not(Input::isUploadedToIntric))
			.forEach(input -> {
				LOG.info("Uploading file for input {}", input.getFile().getName());

				// Upload the file to Intric
				var intricFileId = intricService.uploadFile(municipalityId, input.getFile());
				// Keep a reference to it for later
				input.setIntricFileId(intricFileId);
			});

		// Handle redirected output inputs by deleting old ones and uploading ones
		var inputsToRemoveFromSession = new HashMap<String, Input>();
		session.getRedirectedOutputInput().forEach((sourceStepId, inputs) -> {
			for (var input : inputs) {
				if (input.isUploadedToIntric()) {
					LOG.info("Deleting previous redirected output file from step {} with id {}", sourceStepId, input.getIntricFileId());

					// Delete the file from Intric
					intricService.deleteFile(municipalityId, input.getIntricFileId());
					// Mark the input for removal from the session
					inputsToRemoveFromSession.put(sourceStepId, input);
				} else {
					LOG.info("Uploading redirected output file from step {}", sourceStepId);

					// Upload the file to Intric
					var intricFileId = intricService.uploadFile(municipalityId, input.getFile());
					// Keep a reference to it for later
					input.setIntricFileId(intricFileId);

					LOG.info("Uploaded redirected output file for step {} with id {}", sourceStepId, intricFileId);
				}
			}
		});

		// Remove inputs from the session if needed
		inputsToRemoveFromSession.forEach((sourceStepId, input) -> session.getRedirectedOutputInput().get(sourceStepId).remove(input));
	}
}

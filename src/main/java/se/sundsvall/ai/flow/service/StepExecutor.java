package se.sundsvall.ai.flow.service;

import static java.util.Optional.ofNullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import se.sundsvall.ai.flow.integration.intric.IntricIntegration;
import se.sundsvall.ai.flow.model.flow.FlowInputRef;
import se.sundsvall.ai.flow.model.flow.RedirectedOutput;
import se.sundsvall.ai.flow.service.flow.ExecutionState;
import se.sundsvall.ai.flow.service.flow.StepExecution;
import se.sundsvall.ai.flow.service.flow.exception.FlowExecutionException;

@Service
public class StepExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(StepExecutor.class);

	private static final String INTRIC_INPUT_TEMPLATE = "%s%s:\n%s\n";

	private final SessionService sessionService;
	private final IntricIntegration intricIntegration;

	public StepExecutor(final SessionService sessionService, final IntricIntegration intricIntegration) {
		this.sessionService = sessionService;
		this.intricIntegration = intricIntegration;
	}

	@Async
	public void executeStep(final StepExecution stepExecution) {
		executeStepInternal(stepExecution);
	}

	void executeStepInternal(final StepExecution stepExecution) {
		var session = sessionService.getSession(stepExecution.getSessionId());
		var flow = session.getFlow();
		var step = stepExecution.getStep();

		LOG.info("Executing step '{}' in flow '{}' for session {}", step.getName(), flow.getName(), session.getId());

		// Recursively execute required steps
		for (var requiredStepExecution : stepExecution.getRequiredStepExecutions()) {
			LOG.info("Triggering step '{}' required by step '{}' in flow '{}' for session {}", requiredStepExecution.getStep().getName(), step.getName(), flow.getName(), session.getId());

			executeStepInternal(requiredStepExecution);
		}

		// Mark the step execution as running
		stepExecution.setState(ExecutionState.RUNNING);

		var intricInput = "";
		for (var input : step.getInputs()) {
			intricInput += switch (input) {
				case FlowInputRef flowInputRef -> {
					var flowInput = session.getFlow().getInputMap().get(flowInputRef.getInput());
					// Make sure the input actually exists
					if (flowInput == null) {
						throw new FlowExecutionException("No referenced flow input '%s' for step '%s' in flow '%s' for session %s".formatted(flowInputRef.getInput(), step.getName(), flow.getName(), session.getId()));
					}

					// Get the session input for the given flow input
					var sessionInput = ofNullable(session.getInput()).map(inputMap -> inputMap.get(flowInput.getId())).orElse(null);
					if (sessionInput == null) {
						throw new FlowExecutionException("Required input '%s' is missing for step '%s' in flow '%s' for session %s".formatted(flowInput.getId(), step.getName(), flow.getName(), session.getId()));
					}

					// Append to the Intric input
					yield INTRIC_INPUT_TEMPLATE.formatted(flow.getInputPrefix(), flowInput.getName(), String.join("\n", sessionInput));
				}
				case RedirectedOutput redirectedOutput -> {
					var dependentStepExecution = session.getStepExecutions().get(redirectedOutput.getStep());

					yield INTRIC_INPUT_TEMPLATE.formatted(flow.getInputPrefix(), redirectedOutput.getName(), dependentStepExecution.getOutput());
				}
				// This should never happen, but still...
				default -> throw new FlowExecutionException("Unknown input: " + input.getClass().getName());
			};
		}

		// Invoke the Intric service
		try {
			var output = intricIntegration.runService(step.getIntricServiceId(), intricInput);

			// Store the output
			stepExecution.setOutput(output);
			// Marke the step execution as done
			stepExecution.setState(ExecutionState.DONE);
		} catch (Exception e) {
			// Store the exception
			stepExecution.setErrorMessage(e.getMessage());
			// Marke the step execution as done
			stepExecution.setState(ExecutionState.ERROR);
		}
	}
}

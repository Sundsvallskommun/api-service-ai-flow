package se.sundsvall.ai.flow.service.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.integration.eneo.EneoService;
import se.sundsvall.ai.flow.model.flowdefinition.Step;

@Component
public class AssistantTargetExecutor extends TargetExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(AssistantTargetExecutor.class);

	private final EneoService eneoService;

	public AssistantTargetExecutor(final EneoService eneoService) {
		this.eneoService = eneoService;
	}

	@Override
	public boolean supports(final Step.Target.Type type) {
		return type == Step.Target.Type.ASSISTANT;
	}

	@Override
	public TargetResult execute(final StepRunContext stepRunContext) {
		final var stepExecution = stepRunContext.stepExecution();
		final var step = stepExecution.getStep();
		final var targetEndpointId = step.getTarget().id();

		if (stepExecution.getEneoSessionId() == null) {
			LOG.info("Running step {} using ASSISTANT {}", step.getName(), targetEndpointId);
			final var response = eneoService.askAssistant(stepRunContext.municipalityId(), targetEndpointId, stepRunContext.inputFileIdsInUse(), stepRunContext.inputsInUseInfo());
			final var output = response.answer();
			final var sessionId = response.sessionId();
			return new TargetResult(output, null, sessionId);
		} else {
			LOG.info("Running FOLLOW-UP on step {} using ASSISTANT {}", step.getName(), targetEndpointId);
			final var response = eneoService.askAssistantFollowup(stepRunContext.municipalityId(), targetEndpointId, stepExecution.getEneoSessionId(), stepRunContext.inputFileIdsInUse(), stepRunContext.inputsInUseInfo(), stepRunContext.userInput());
			final var output = response.answer();
			final var sessionId = stepExecution.getEneoSessionId();
			return new TargetResult(output, null, sessionId);
		}
	}
}

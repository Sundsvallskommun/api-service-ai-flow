package se.sundsvall.ai.flow.service.execution;

import java.util.UUID;
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
	public TargetResult execute(final StepRunContext ctx) {
		final var stepExecution = ctx.stepExecution();
		final var step = stepExecution.getStep();
		final UUID targetEndpointId = step.getTarget().id();

		if (stepExecution.getIntricSessionId() == null) {
			LOG.info("Running step {} using ASSISTANT {}", step.getName(), targetEndpointId);
			final var response = eneoService.askAssistant(ctx.municipalityId(), targetEndpointId, ctx.inputFileIdsInUse(), ctx.inputsInUseInfo());
			final var output = response.answer();
			final var sessionId = response.sessionId();
			return new TargetResult() {
				@Override
				public String output() {
					return output;
				}

				@Override
				public UUID runId() {
					return null;
				}

				@Override
				public UUID sessionId() {
					return sessionId;
				}
			};
		} else {
			LOG.info("Running FOLLOW-UP on step {} using ASSISTANT {}", step.getName(), targetEndpointId);
			final var response = eneoService.askAssistantFollowup(ctx.municipalityId(), targetEndpointId, stepExecution.getIntricSessionId(), ctx.inputFileIdsInUse(), ctx.inputsInUseInfo(), ctx.userInput());
			final var output = response.answer();
			final var sessionId = stepExecution.getIntricSessionId();
			return new TargetResult() {
				@Override
				public String output() {
					return output;
				}

				@Override
				public UUID runId() {
					return null;
				}

				@Override
				public UUID sessionId() {
					return sessionId;
				}
			};
		}
	}
}

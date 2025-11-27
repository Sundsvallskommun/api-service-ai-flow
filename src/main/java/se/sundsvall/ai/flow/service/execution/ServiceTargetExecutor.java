package se.sundsvall.ai.flow.service.execution;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.integration.eneo.EneoService;
import se.sundsvall.ai.flow.model.flowdefinition.Step;

@Component
public class ServiceTargetExecutor extends TargetExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceTargetExecutor.class);

	private final EneoService eneoService;

	public ServiceTargetExecutor(final EneoService eneoService) {
		this.eneoService = eneoService;
	}

	@Override
	public boolean supports(final Step.Target.Type type) {
		return type == Step.Target.Type.SERVICE;
	}

	@Override
	public TargetResult execute(final StepRunContext ctx) {
		final var step = ctx.stepExecution().getStep();
		final UUID targetEndpointId = step.getTarget().id();
		LOG.info("Running step {} using SERVICE {}", step.getName(), targetEndpointId);

		final var response = eneoService.runService(
			ctx.municipalityId(),
			targetEndpointId,
			ctx.inputFileIdsInUse(),
			ctx.inputsInUseInfo(),
			ctx.userInput());

		final var output = response.answer();
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
				return null;
			}
		};
	}
}

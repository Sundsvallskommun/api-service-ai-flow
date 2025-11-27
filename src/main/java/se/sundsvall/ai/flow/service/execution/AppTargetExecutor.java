package se.sundsvall.ai.flow.service.execution;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.integration.eneo.EneoService;
import se.sundsvall.ai.flow.model.flowdefinition.Step;

@Component
public class AppTargetExecutor extends TargetExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(AppTargetExecutor.class);

	private final EneoService eneoService;
	private final AppRunPoller appRunPoller;

	public AppTargetExecutor(final EneoService eneoService, final AppRunPoller appRunPoller) {
		this.eneoService = eneoService;
		this.appRunPoller = appRunPoller;
	}

	@Override
	public boolean supports(final Step.Target.Type type) {
		return type == Step.Target.Type.APP;
	}

	@Override
	public TargetResult execute(final StepRunContext ctx) throws InterruptedException {
		final var stepExecution = ctx.stepExecution();
		final var step = stepExecution.getStep();
		final UUID targetEndpointId = step.getTarget().id();

		if (stepExecution.getIntricRunId() == null) {
			LOG.info("Running step {} using APP {}", step.getName(), targetEndpointId);
			final var response = eneoService.runApp(ctx.municipalityId(), targetEndpointId, ctx.inputFileIdsInUse());
			final var runId = response.runId();
			return getTargetResult(ctx, step, runId);
		} else {
			LOG.info("Re-polling existing app run for step {} using APP {}", step.getName(), targetEndpointId);
			final var runId = stepExecution.getIntricRunId();
			return getTargetResult(ctx, step, runId);
		}
	}

	private TargetExecutor.TargetResult getTargetResult(final StepRunContext ctx, final Step step, final UUID runId) throws InterruptedException {
		final var output = appRunPoller.pollUntilComplete(ctx.municipalityId(), runId, step.getName());
		return new TargetResult() {
			@Override
			public String output() {
				return output;
			}

			@Override
			public UUID runId() {
				return runId;
			}

			@Override
			public UUID sessionId() {
				return null;
			}
		};
	}
}

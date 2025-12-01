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
	public TargetResult execute(final StepRunContext stepRunContext) throws InterruptedException {
		final var stepExecution = stepRunContext.stepExecution();
		final var step = stepExecution.getStep();
		final var targetEndpointId = step.getTarget().id();

		if (stepExecution.getEneoRunId() == null) {
			LOG.info("Running step {} using APP {}", step.getName(), targetEndpointId);
			final var response = eneoService.runApp(stepRunContext.municipalityId(), targetEndpointId, stepRunContext.inputFileIdsInUse());
			final var runId = response.runId();
			return getTargetResult(stepRunContext, step, runId);
		} else {
			LOG.info("Re-polling existing app run for step {} using APP {}", step.getName(), targetEndpointId);
			final var runId = stepExecution.getEneoRunId();
			return getTargetResult(stepRunContext, step, runId);
		}
	}

	private TargetExecutor.TargetResult getTargetResult(final StepRunContext stepRunContext, final Step step, final UUID runId) throws InterruptedException {
		final var output = appRunPoller.pollUntilComplete(stepRunContext.municipalityId(), runId, step.getName());
		return new TargetResult(output, runId, null);
	}
}

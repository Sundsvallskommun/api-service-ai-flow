package se.sundsvall.ai.flow.service.execution;

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
	public TargetResult execute(final StepRunContext stepRunContext) {
		final var step = stepRunContext.stepExecution().getStep();
		final var targetEndpointId = step.getTarget().id();
		LOG.info("Running step {} using SERVICE {}", step.getName(), targetEndpointId);

		final var response = eneoService.runService(
			stepRunContext.municipalityId(),
			targetEndpointId,
			stepRunContext.inputFileIdsInUse(),
			stepRunContext.inputsInUseInfo(),
			stepRunContext.userInput());

		final var output = response.answer();
		return new TargetResult(output, null, null);
	}
}

package se.sundsvall.ai.flow.service.execution;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.TextInputValue;

/** Adds redirected outputs from required steps as inputs to the session. */
@Component
public class RedirectedOutputResolver {
	private static final Logger LOG = LoggerFactory.getLogger(RedirectedOutputResolver.class);

	public void addRedirectedOutputsAsInputs(final Session session, final Step step) {
		requireNonNull(session);
		requireNonNull(step);

		step.getInputs().stream()
			.filter(RedirectedOutput.class::isInstance)
			.map(RedirectedOutput.class::cast)
			.forEach(redirected -> {
				final var requiredStepExecution = session.getStepExecutions().get(redirected.getStep());
				final var value = new TextInputValue(redirected.getUseAs(), requiredStepExecution.getOutput());
				LOG.debug("Adding redirected output from step {} as input '{}'", redirected.getStep(), redirected.getUseAs());
				session.addRedirectedOutputAsInput(redirected.getStep(), value);
			});
	}
}

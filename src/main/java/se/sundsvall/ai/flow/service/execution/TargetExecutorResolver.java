package se.sundsvall.ai.flow.service.execution;

import java.util.List;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.ai.flow.model.flowdefinition.Step;

@Component
public class TargetExecutorResolver {

	private final List<TargetExecutor> executors;

	public TargetExecutorResolver(final List<TargetExecutor> executors) {
		this.executors = executors;
	}

	public TargetExecutor resolve(final Step.Target.Type type) {
		return executors.stream()
			.filter(ex -> ex.supports(type))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(Status.NOT_IMPLEMENTED, "No TargetExecutor for type " + type));
	}
}

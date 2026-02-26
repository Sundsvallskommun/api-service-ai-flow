package se.sundsvall.ai.flow.service.execution;

import java.util.List;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;

@Component
public class TargetExecutorResolver {

	private final List<TargetExecutor> targetExecutors;

	public TargetExecutorResolver(final List<TargetExecutor> targetExecutors) {
		this.targetExecutors = targetExecutors;
	}

	public TargetExecutor resolve(final Step.Target.Type type) {
		return targetExecutors.stream()
			.filter(targetExecutor -> targetExecutor.supports(type))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_IMPLEMENTED, "No TargetExecutor for type " + type));
	}
}

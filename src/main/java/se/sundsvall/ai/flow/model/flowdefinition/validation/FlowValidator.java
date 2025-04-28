package se.sundsvall.ai.flow.model.flowdefinition.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;

public final class FlowValidator {

	private FlowValidator() {}

	/*
	 * Checks if there is any circular dependencies between the steps in the given flow.
	 */
	public static boolean hasStepDependencyCycle(final Flow flow) {
		var graph = new HashMap<String, List<String>>();

		for (var step : flow.getSteps()) {
			graph.putIfAbsent(step.getId(), new LinkedList<>());
			for (var stepInput : step.getInputs()) {
				if (stepInput instanceof RedirectedOutput redirectedOutput) {
					graph.get(step.getId()).add(redirectedOutput.getStep());
				}
			}
		}

		var visited = new HashSet<String>();

		return graph.keySet().stream()
			.anyMatch(step -> !visited.contains(step) && hasCycle(graph, step, visited, new HashSet<>()));
	}

	static boolean hasCycle(final Map<String, List<String>> graph, final String currentStep, final Set<String> visited, final Set<String> recursionStack) {
		var result = false;
		var finished = false;

		// If the recursion stack contains the step, we have a cycle
		if (recursionStack.contains(currentStep)) {
			result = true;
		} else if (!visited.contains(currentStep)) {
			visited.add(currentStep);
			recursionStack.add(currentStep);
			var dependentSteps = graph.getOrDefault(currentStep, new LinkedList<>());
			for (var dependentStep : dependentSteps) {
				if (hasCycle(graph, dependentStep, visited, recursionStack)) {
					result = true;
					finished = true;
					break;
				}
			}
			if (!finished) {
				recursionStack.remove(currentStep);
			}
		}

		return result;
	}
}

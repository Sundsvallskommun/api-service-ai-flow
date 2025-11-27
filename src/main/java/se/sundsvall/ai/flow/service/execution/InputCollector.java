package se.sundsvall.ai.flow.service.execution;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInputRef;
import se.sundsvall.ai.flow.model.flowdefinition.RedirectedOutput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.session.Input;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.service.InputDescriptor;

/** Resolves which inputs are in use for a given step and collects ids and a human-readable descriptor. */
@Component
public class InputCollector {
	private final InputDescriptor inputDescriptor;

	public InputCollector(final InputDescriptor inputDescriptor) {
		this.inputDescriptor = inputDescriptor;
	}

	public Inputs resolve(final Session session, final Step step) {
		final var flow = session.getFlow();

		final var flowInputRefStepInputs = step.getInputs().stream()
			.filter(FlowInputRef.class::isInstance)
			.map(FlowInputRef.class::cast)
			.filter(not(flowInputRef -> flow.getFlowInput(flowInputRef.getInput()).isPassthrough()))
			.toList();

		final var redirectedOutputStepInputs = step.getInputs().stream()
			.filter(RedirectedOutput.class::isInstance)
			.map(RedirectedOutput.class::cast)
			.toList();

		final var inputsInUse = java.util.stream.Stream.concat(
			flowInputRefStepInputs.stream().map(FlowInputRef::getInput),
			redirectedOutputStepInputs.stream().map(RedirectedOutput::getStep))
			.toList();

		final var fileIdsInUse = session.getAllInput().entrySet().stream()
			.filter(entry -> inputsInUse.contains(entry.getKey()))
			.flatMap(entry -> entry.getValue().stream())
			.map(Input::getEneoFileId)
			.toList();

		final var descriptorMap = inputDescriptor.describe(session);
		final var inputsInUseInfo = inputsInUse.stream()
			.map(descriptorMap::get)
			.collect(joining());

		return new Inputs(inputsInUse, inputsInUseInfo, fileIdsInUse);
	}

	public record Inputs(java.util.List<String> inputsInUse, String inputsInUseInfo, java.util.List<java.util.UUID> fileIdsInUse) {
	}
}

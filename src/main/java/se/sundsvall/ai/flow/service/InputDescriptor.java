package se.sundsvall.ai.flow.service;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import se.sundsvall.ai.flow.model.session.Input;
import se.sundsvall.ai.flow.model.session.Session;

@Service
public class InputDescriptor {

	private static final String FILE_INFO_TEMPLATE = "du hittar %s i filen/filerna %s. ";

	public Map<String, String> describe(final Session session) {
		final var flow = session.getFlow();

		// Regular inputs (skip optional if empty)
		final var regular = session.getInput().entrySet().stream()
			.map(entry -> {
				final var inputId = entry.getKey();
				final var flowInput = flow.getFlowInput(inputId);
				if (flowInput.isOptional() && entry.getValue().isEmpty()) {
					return null;
				}
				return createInfoEntry(inputId, flowInput.getName(), entry.getValue());
			})
			.filter(Objects::nonNull)
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		// Redirected output inputs
		final var redirected = session.getRedirectedOutputInput().entrySet().stream()
			.map(entry -> {
				final var stepId = entry.getKey();
				final var step = flow.getStep(stepId);
				return createInfoEntry(stepId, step.getName(), entry.getValue());
			})
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

		return Stream.concat(regular.entrySet().stream(), redirected.entrySet().stream())
			.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map.Entry<String, String> createInfoEntry(final String key, final String name, final java.util.List<Input> inputs) {
		final var fileIds = inputs.stream()
			.map(Input::getEneoFileId)
			.map(UUID::toString)
			.toList();
		final var info = String.format(FILE_INFO_TEMPLATE, name.toLowerCase(), String.join(",", fileIds));
		return Map.entry(key, info);
	}
}

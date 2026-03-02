package se.sundsvall.ai.flow.model.flowdefinition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serial;
import java.util.Map;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@JsonDeserialize(using = StepInput.Deserializer.class)
public abstract class StepInput {

	static final String FLOW_INPUT_REF = "use-flow-input";
	static final String STEP_OUTPUT_REF = "use-output-from-step";
	static final String NAME = "use-as";

	public enum Type {
		FLOW_INPUT,
		STEP_OUTPUT
	}

	@JsonIgnore
	private final Type type;

	protected StepInput(final Type type) {
		this.type = type;
	}

	public final Type getType() {
		return type;
	}

	static class Deserializer extends StdDeserializer<StepInput> {

		@Serial
		private static final long serialVersionUID = -703432197878787L;

		Deserializer() {
			super(StepInput.class);
		}

		@Override
		public StepInput deserialize(final JsonParser parser, final DeserializationContext context) {
			final var root = (JsonNode) parser.readValueAsTree();

			// Make sure we actually have an object node to work with
			if (!root.isObject()) {
				throw MismatchedInputException.from(parser, StepInput.class, "Object node expected");
			}

			// Re-map the child nodes as a map from node name to the actual node, for easier access
			final var children = root.properties().stream()
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

			// Attempt to extract the "use-flow-input" field
			final var flowInputRef = ofNullable(children.get(FLOW_INPUT_REF))
				.map(JsonNode::textValue)
				.orElse(null);
			// Attempt to extract the "step" and "name" fields
			final var step = ofNullable(children.get(STEP_OUTPUT_REF))
				.map(JsonNode::textValue)
				.orElse(null);
			final var name = ofNullable(children.get(NAME))
				.map(JsonNode::textValue)
				.orElse(null);

			if (isNotBlank(flowInputRef) && isBlank(step) && isBlank(name)) {
				return new FlowInputRef().withInput(flowInputRef);
			} else if (isBlank(flowInputRef) && isNotBlank(step) && isNotBlank(name)) {
				return new RedirectedOutput().withStep(step).withUseAs(name);
			}

			throw MismatchedInputException.from(parser, StepInput.class,
				"Unable to parse step input. Either \"%s\" OR (\"%s\" AND \"%s\") should be set".formatted(FLOW_INPUT_REF, STEP_OUTPUT_REF, NAME));
		}
	}
}

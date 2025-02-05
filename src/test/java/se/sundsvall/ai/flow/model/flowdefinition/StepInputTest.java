package se.sundsvall.ai.flow.model.flowdefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.model.flowdefinition.StepInput.FLOW_INPUT_REF;
import static se.sundsvall.ai.flow.model.flowdefinition.StepInput.NAME;
import static se.sundsvall.ai.flow.model.flowdefinition.StepInput.STEP_OUTPUT_REF;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StepInputTest {

	@Nested
	class DeserializerTest {

		@Mock(answer = Answers.RETURNS_DEEP_STUBS)
		private JsonParser mockJsonParser;

		@Mock
		private DeserializationContext mockDeserializationContext;

		@Mock
		private JsonNode mockRootNode;

		@Mock
		private JsonNode mockFlowInputRefNode;

		@Mock
		private JsonNode mockRedirectedOutputNode;

		@Mock
		private JsonNode mockNameNode;

		@InjectMocks
		private StepInput.Deserializer deserializer;

		@BeforeEach
		void setUp() {
			deserializer = new StepInput.Deserializer();
		}

		@Test
		void deserializeFlowInputRef() throws IOException {
			var inputId = "someInputId";

			var fields = Map.of(FLOW_INPUT_REF, mockFlowInputRefNode).entrySet();

			when(mockJsonParser.getCodec().readTree(mockJsonParser)).thenReturn(mockRootNode);
			when(mockRootNode.isObject()).thenReturn(true);
			when(mockRootNode.fields()).thenReturn(fields.iterator());
			when(mockFlowInputRefNode.textValue()).thenReturn(inputId);

			var result = deserializer.deserialize(mockJsonParser, mockDeserializationContext);

			assertThat(result).isInstanceOf(FlowInputRef.class).asInstanceOf(InstanceOfAssertFactories.type(FlowInputRef.class)).satisfies(flowInputRef -> {
				assertThat(flowInputRef.getInput()).isEqualTo(inputId);
			});
		}

		@Test
		void deserializeRedirectedOutput() throws IOException {
			var step = "someStep";
			var name = "someName";

			var fields = Map.of(
				STEP_OUTPUT_REF, mockRedirectedOutputNode,
				NAME, mockNameNode).entrySet();

			when(mockJsonParser.getCodec().readTree(mockJsonParser)).thenReturn(mockRootNode);
			when(mockRootNode.isObject()).thenReturn(true);
			when(mockRootNode.fields()).thenReturn(fields.iterator());
			when(mockRedirectedOutputNode.textValue()).thenReturn(step);
			when(mockNameNode.textValue()).thenReturn(name);

			var result = deserializer.deserialize(mockJsonParser, mockDeserializationContext);

			assertThat(result).isInstanceOf(RedirectedOutput.class).asInstanceOf(InstanceOfAssertFactories.type(RedirectedOutput.class)).satisfies(redirectedOutput -> {
				assertThat(redirectedOutput.getStep()).isEqualTo(step);
				assertThat(redirectedOutput.getName()).isEqualTo(name);
			});
		}

		@Test
		void deserializeWhenInputIsInvalid() throws IOException {
			var fields = Map.<String, JsonNode>of().entrySet();

			when(mockJsonParser.getCodec().readTree(mockJsonParser)).thenReturn(mockRootNode);
			when(mockRootNode.isObject()).thenReturn(true);
			when(mockRootNode.fields()).thenReturn(fields.iterator());

			assertThatExceptionOfType(IOException.class)
				.isThrownBy(() -> deserializer.deserialize(mockJsonParser, mockDeserializationContext));
		}

		@Test
		void deserializeWhenJsonIsInvalid() throws IOException {
			when(mockJsonParser.getCodec().readTree(mockJsonParser)).thenReturn(mockRootNode);
			when(mockRootNode.isObject()).thenReturn(false);

			assertThatExceptionOfType(IOException.class)
				.isThrownBy(() -> deserializer.deserialize(mockJsonParser, mockDeserializationContext));
		}
	}
}

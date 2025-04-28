package se.sundsvall.ai.flow.integration.templating;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import generated.se.sundsvall.templating.RenderRequest;
import java.util.Base64;
import java.util.HashMap;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInput;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.support.StringMultipartFile;

@Component
public class TemplatingIntegration {

	static final String CLIENT_ID = "templating";

	private static final String BASE64_VALUE_PREFIX = "BASE64:";

	private final TemplatingClient client;

	TemplatingIntegration(final TemplatingClient client) {
		this.client = client;
	}

	public String renderSession(final Session session, final String templateId, final String municipalityId) {
		var parameters = new HashMap<String, Object>();

		// Include any "passthrough" inputs. For now, only the first of the input values is passed
		// on. TODO: validate the flow, making sure that multiple input cardinality isn't allowed
		// in conjunction with passthrough set
		session.getFlow().getFlowInputs().stream()
			.filter(FlowInput::isPassthrough)
			.forEach(passthroughInput -> {
				var value = session.getInput().get(passthroughInput.getId());
				if (value != null && !value.isEmpty()) {
					var actualInput = value.getFirst();
					if (actualInput.getFile() instanceof StringMultipartFile multipartFile) {
						parameters.put(passthroughInput.getId(), multipartFile.getValue());
					}
				}
			});

		// "Re-write" the session step output to templating parameters, using the step id as the
		// key (parameter name) and the step execution output as the value (parameter value). Also,
		// BASE64-encode the parameter value
		session.getStepExecutions().forEach((stepId, stepExecution) -> {
			if (isNotBlank(stepExecution.getOutput())) {
				parameters.put(stepExecution.getStep().getId(), BASE64_VALUE_PREFIX + base64Encode(stepExecution.getOutput()));
			}
		});

		var request = new RenderRequest()
			.identifier(templateId)
			.parameters(parameters);

		var response = client.render(municipalityId, request);

		return response.getOutput();
	}

	String base64Encode(final String s) {
		return Base64.getEncoder().encodeToString(s.getBytes(UTF_8));
	}
}

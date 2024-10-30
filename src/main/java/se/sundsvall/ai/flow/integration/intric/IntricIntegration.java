package se.sundsvall.ai.flow.integration.intric;

import org.springframework.stereotype.Component;

import se.sundsvall.ai.flow.integration.intric.model.RunService;

@Component
public class IntricIntegration {

	static final String CLIENT_ID = "intric";

	private final IntricClient client;

	IntricIntegration(final IntricClient client) {
		this.client = client;
	}

	public String runService(final String serviceId, final String input) {
		var response = client.runService(serviceId, new RunService(input));

		return response.output();
	}
}

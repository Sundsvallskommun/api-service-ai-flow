package se.sundsvall.ai.flow.integration.templating;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.ai.flow.integration.templating.TemplatingIntegration.CLIENT_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import generated.se.sundsvall.templating.RenderRequest;
import generated.se.sundsvall.templating.RenderResponse;

@FeignClient(
	name = CLIENT_ID,
	configuration = TemplatingIntegrationConfiguration.class,
	url = "${integration.templating.base-url}")
interface TemplatingClient {

	@PostMapping(path = "/{municipalityId}/render", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	RenderResponse render(@PathVariable(name = "municipalityId") String municipalityId, @RequestBody RenderRequest renderRequest);
}

package se.sundsvall.ai.flow.integration.templating;

import generated.se.sundsvall.templating.RenderRequest;
import generated.se.sundsvall.templating.RenderResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.ai.flow.integration.templating.TemplatingIntegration.CLIENT_ID;

@FeignClient(
	name = CLIENT_ID,
	configuration = TemplatingConfiguration.class,
	url = "${integration.templating.base-url}")
@CircuitBreaker(name = CLIENT_ID)
interface TemplatingClient {

	@PostMapping(path = "/{municipalityId}/render", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	RenderResponse render(@PathVariable(name = "municipalityId") String municipalityId, @RequestBody RenderRequest renderRequest);
}

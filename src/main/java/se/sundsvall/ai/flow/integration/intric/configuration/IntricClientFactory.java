package se.sundsvall.ai.flow.integration.intric.configuration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import feign.Logger;
import feign.Request;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.integration.db.model.InstanceEntity;
import se.sundsvall.ai.flow.integration.intric.IntricClient;
import se.sundsvall.ai.flow.integration.intric.IntricTokenService;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Component
public class IntricClientFactory {

	private final ApplicationContext applicationContext;
	private final IntricTokenService tokenService;

	public IntricClientFactory(final ApplicationContext applicationContext, final IntricTokenService tokenService) {
		this.applicationContext = applicationContext;
		this.tokenService = tokenService;
	}

	public IntricClient createIntricClient(final InstanceEntity instanceEntity) {
		final var clientName = "intric-%s".formatted(instanceEntity.getMunicipalityId());

		return new FeignClientBuilder(applicationContext)
			.forType(IntricClient.class, clientName)
			.customize(builder -> builder
				.errorDecoder(new ProblemErrorDecoder(clientName))
				.requestInterceptor(template -> template.header(AUTHORIZATION, "Bearer " + tokenService.getToken(instanceEntity.getMunicipalityId())))
				.logLevel(Logger.Level.FULL)
				.dismiss404()
				.options(new Request.Options(instanceEntity.getConnectTimeout(), SECONDS, instanceEntity.getReadTimeout(), SECONDS, true)))
			.url(instanceEntity.getBaseUrl())
			.build();
	}
}

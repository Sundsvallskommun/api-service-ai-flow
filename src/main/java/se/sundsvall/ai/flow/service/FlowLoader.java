package se.sundsvall.ai.flow.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static se.sundsvall.ai.flow.model.flowdefinition.validation.FlowValidator.hasStepDependencyCycle;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.integration.db.FlowRepository;
import se.sundsvall.ai.flow.integration.db.model.FlowEntity;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.exception.FlowConfigurationException;

@Component
@EnableConfigurationProperties(FlowLoader.Configuration.class)
@ConditionalOnProperty(value = "flow-loader.enabled", havingValue = "true")
class FlowLoader implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(FlowLoader.class);

	private final Configuration configuration;
	private final ObjectMapper objectMapper;
	private final FlowRepository flowRepository;

	FlowLoader(final Configuration configuration, final ObjectMapper objectMapper, final FlowRepository flowRepository) {
		this.configuration = configuration;
		this.objectMapper = objectMapper;
		this.flowRepository = flowRepository;
	}

	@Override
	public void run(final String... args) throws Exception {
		for (final var flowResource : configuration.getFlowResources()) {
			final var flowResourceString = new String(flowResource.getContentAsByteArray(), UTF_8);
			final var flow = objectMapper.readValue(flowResourceString, Flow.class);

			final var flowId = flow.getId();
			final var flowVersion = flow.getVersion();
			final var flowName = flow.getName();
			final var flowDescription = flow.getDescription();

			// Validate the flow
			if (hasStepDependencyCycle(flow)) {
				throw new FlowConfigurationException("The flow %s (%s) has a dependency cycle between steps".formatted(flow.getId(), flow.getName()));
			}

			if (flowRepository.existsByIdAndVersion(flowId, flowVersion)) {
				LOG.info("NOT loading flow '{}' (id: '{}') from '{}' as version(s) exist(s)", flowName, flowId, flowResource.getFilename());

				continue;
			}

			flowRepository.save(new FlowEntity()
				.withId(flowId)
				.withVersion(flowVersion)
				.withName(flowName)
				.withDescription(flowDescription)
				.withContent(flowResourceString));

			LOG.info("Loaded flow '{}' (id: '{}', version: {}) from '{}'", flowName, flowId, flowVersion, flowResource.getFilename());
		}
	}

	@ConfigurationProperties(prefix = "flow-loader")
	record Configuration(@DefaultValue("false") boolean enabled, @DefaultValue("classpath:/flows/*.json") String locationPattern) {

		Resource[] getFlowResources() throws IOException {
			final var resolver = new PathMatchingResourcePatternResolver();

			return resolver.getResources(locationPattern);
		}
	}
}

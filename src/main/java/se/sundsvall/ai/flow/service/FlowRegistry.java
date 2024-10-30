package se.sundsvall.ai.flow.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.NOT_FOUND;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

import se.sundsvall.ai.flow.model.flow.Flow;

@Component
public class FlowRegistry {

	private static final Logger LOG = LoggerFactory.getLogger(FlowRegistry.class);

	private final Map<String, Flow> flows = new HashMap<>();

	public FlowRegistry(final ObjectMapper objectMapper) throws IOException {
		var resolver = new PathMatchingResourcePatternResolver();

		var flowResources = resolver.getResources("classpath:/flows/*.json");
		for (var flowResource : flowResources) {
			var flow = objectMapper.readValue(new String(flowResource.getContentAsByteArray(), UTF_8), Flow.class);

			flows.put(flow.getId(), flow);

			LOG.info("Loaded flow '{}' (id: '{}') from '{}'", flow.getName(), flow.getId(), flowResource.getFilename());
		}
	}

	public List<Flow> getAllFlows() {
		return List.copyOf(flows.values());
	}

	public Flow getFlow(final String flowId) {
		return ofNullable(flows.get(flowId))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No flow found with id " + flowId));
	}
}

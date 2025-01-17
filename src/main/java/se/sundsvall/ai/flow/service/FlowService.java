package se.sundsvall.ai.flow.service;

import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.ai.flow.service.FlowMapper.toFlowResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.ai.flow.api.model.FlowResponse;
import se.sundsvall.ai.flow.api.model.Flows;
import se.sundsvall.ai.flow.integration.db.FlowEntity;
import se.sundsvall.ai.flow.integration.db.FlowEntityId;
import se.sundsvall.ai.flow.integration.db.FlowEntityRepository;
import se.sundsvall.ai.flow.model.flow.Flow;

@Service
public class FlowService {

	private static final Logger LOG = LoggerFactory.getLogger(FlowService.class);

	private final FlowEntityRepository flowEntityRepository;
	private final ObjectMapper objectMapper;

	public FlowService(final FlowEntityRepository flowEntityRepository, final ObjectMapper objectMapper) {
		this.flowEntityRepository = flowEntityRepository;
		this.objectMapper = objectMapper;
	}

	public FlowResponse getFlow(final String flowName, final int version) {
		var flow = flowEntityRepository.findById(new FlowEntityId(flowName, version))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No flow found with name " + flowName + " and version " + version));

		return toFlowResponse(flow);
	}

	public Flows getFlows() {
		var flows = flowEntityRepository.findAll().stream()
			.map(FlowMapper::toFlowSummary)
			.toList();
		return new Flows(flows);
	}

	public void deleteFlow(final String flowName, final int version) {
		var exists = flowEntityRepository.existsById(new FlowEntityId(flowName, version));
		if (exists) {
			flowEntityRepository.deleteById(new FlowEntityId(flowName, version));
		} else {
			throw Problem.valueOf(NOT_FOUND, "No flow found with name " + flowName + " and version " + version);
		}
	}

	public URI createFlow(final Flow flow) {
		var flowEntity = new FlowEntity();
		var newVersion = flowEntityRepository.findMaxVersionByName(flow.getName())
			.map(oldVersion -> oldVersion + 1)
			.orElse(1);

		try {
			var content = objectMapper.writeValueAsString(flow);
			flowEntityRepository.save(flowEntity
				.withName(flow.getName())
				.withVersion(newVersion)
				.withContent(content));
		} catch (JsonProcessingException e) {
			LOG.error("Flow could not be written as string", e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Flow could not be written as string");
		}
		return fromPath("/{flowName}/{version}").build(flowEntity.getName(), flowEntity.getVersion());
	}
}

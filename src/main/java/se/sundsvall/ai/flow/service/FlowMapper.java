package se.sundsvall.ai.flow.service;

import java.util.Optional;
import se.sundsvall.ai.flow.api.model.FlowResponse;
import se.sundsvall.ai.flow.api.model.FlowSummary;
import se.sundsvall.ai.flow.integration.db.FlowEntity;

public final class FlowMapper {

	private FlowMapper() {}

	public static FlowResponse toFlowResponse(final FlowEntity flowEntity) {
		return Optional.ofNullable(flowEntity).map(entity -> new FlowResponse(
			entity.getName(),
			entity.getVersion(),
			entity.getContent()))
			.orElse(null);
	}

	public static FlowSummary toFlowSummary(final FlowEntity flowEntity) {
		return Optional.ofNullable(flowEntity).map(entity -> new FlowSummary(
			entity.getName(),
			entity.getVersion()))
			.orElse(null);
	}

}

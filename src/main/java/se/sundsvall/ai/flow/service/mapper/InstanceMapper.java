package se.sundsvall.ai.flow.service.mapper;

import java.util.Optional;
import se.sundsvall.ai.flow.api.model.instance.Instance;
import se.sundsvall.ai.flow.integration.db.model.InstanceEntity;

public final class InstanceMapper {

	private InstanceMapper() {}

	public static Instance toInstance(final InstanceEntity entity) {
		return Instance.create()
			.withId(entity.getId())
			.withBaseUrl(entity.getBaseUrl())
			.withUsername(entity.getUsername())
			.withConnectTimeout(entity.getConnectTimeout())
			.withReadTimeout(entity.getReadTimeout());
	}

	public static InstanceEntity fromInstance(final String municipalityId, final Instance instance, final String encryptedPassword) {
		return InstanceEntity.create()
			.withMunicipalityId(municipalityId)
			.withBaseUrl(instance.getBaseUrl())
			.withUsername(instance.getUsername())
			.withConnectTimeout(Optional.ofNullable(instance.getConnectTimeout()).orElse(5))
			.withReadTimeout(Optional.ofNullable(instance.getReadTimeout()).orElse(60))
			.withPassword(encryptedPassword);
	}

	public static InstanceEntity updateInstance(final InstanceEntity entity, final Instance instance, final String encryptedPassword) {
		Optional.ofNullable(instance.getBaseUrl()).ifPresent(entity::setBaseUrl);
		Optional.ofNullable(instance.getUsername()).ifPresent(entity::setUsername);
		Optional.ofNullable(instance.getConnectTimeout()).ifPresent(entity::setConnectTimeout);
		Optional.ofNullable(instance.getReadTimeout()).ifPresent(entity::setReadTimeout);
		Optional.ofNullable(encryptedPassword).ifPresent(entity::setPassword);

		return entity;
	}
}

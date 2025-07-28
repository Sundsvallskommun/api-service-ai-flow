package se.sundsvall.ai.flow.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import se.sundsvall.ai.flow.api.model.instance.Instance;
import se.sundsvall.ai.flow.integration.db.model.InstanceEntity;

class InstanceMapperTest {

	@Test
	void toInstance() {
		final var baseUrl = "url";
		final var username = "user";
		final var connectTimeout = 10;
		final var readTimeout = 20;

		final var entity = new InstanceEntity()
			.withBaseUrl(baseUrl)
			.withUsername(username)
			.withConnectTimeout(connectTimeout)
			.withReadTimeout(readTimeout);

		final var instance = InstanceMapper.toInstance(entity);

		assertThat(instance).isNotNull();
		assertThat(instance.getBaseUrl()).isEqualTo(baseUrl);
		assertThat(instance.getUsername()).isEqualTo(username);
		assertThat(instance.getConnectTimeout()).isEqualTo(connectTimeout);
		assertThat(instance.getReadTimeout()).isEqualTo(readTimeout);
	}

	@Test
	void fromInstance() {
		final var municipalityId = "municipalityId";
		final var baseUrl = "url";
		final var username = "user";
		final var connectTimeout = 10;
		final var readTimeout = 20;
		final var encryptedPassword = "encryptedPassword";

		final var instance = Instance.create()
			.withBaseUrl(baseUrl)
			.withUsername(username)
			.withConnectTimeout(connectTimeout)
			.withReadTimeout(readTimeout);

		final var entity = InstanceMapper.fromInstance(municipalityId, instance, encryptedPassword);

		assertThat(entity).isNotNull();
		assertThat(entity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(entity.getBaseUrl()).isEqualTo(baseUrl);
		assertThat(entity.getUsername()).isEqualTo(username);
		assertThat(entity.getConnectTimeout()).isEqualTo(connectTimeout);
		assertThat(entity.getReadTimeout()).isEqualTo(readTimeout);
		assertThat(entity.getPassword()).isEqualTo(encryptedPassword);
	}

	@Test
	void updateInstance() {
		final var oldBaseUrl = "oldUrl";
		final var oldUsername = "oldUser";
		final var oldConnectTimeout = 5;
		final var oldReadTimeout = 10;
		final var oldPassword = "oldPassword";

		final var newBaseUrl = "newUrl";
		final var newUsername = "newUser";
		final var newConnectTimeout = 15;
		final var newReadTimeout = 25;
		final var newPassword = "newPassword";

		final var entity = new InstanceEntity()
			.withBaseUrl(oldBaseUrl)
			.withUsername(oldUsername)
			.withConnectTimeout(oldConnectTimeout)
			.withReadTimeout(oldReadTimeout)
			.withPassword(oldPassword);

		final var instance = Instance.create()
			.withBaseUrl(newBaseUrl)
			.withUsername(newUsername)
			.withConnectTimeout(newConnectTimeout)
			.withReadTimeout(newReadTimeout);

		final var updatedEntity = InstanceMapper.updateInstance(entity, instance, newPassword);

		assertThat(updatedEntity).isNotNull();
		assertThat(updatedEntity.getBaseUrl()).isEqualTo(newBaseUrl);
		assertThat(updatedEntity.getUsername()).isEqualTo(newUsername);
		assertThat(updatedEntity.getConnectTimeout()).isEqualTo(newConnectTimeout);
		assertThat(updatedEntity.getReadTimeout()).isEqualTo(newReadTimeout);
		assertThat(updatedEntity.getPassword()).isEqualTo(newPassword);
	}
}

package se.sundsvall.ai.flow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import se.sundsvall.ai.flow.api.model.instance.Instance;
import se.sundsvall.ai.flow.integration.db.InstanceRepository;
import se.sundsvall.ai.flow.integration.db.model.InstanceEntity;
import se.sundsvall.ai.flow.integration.intric.configuration.IntricClientFactory;
import se.sundsvall.ai.flow.util.EncryptionUtility;

@ExtendWith(MockitoExtension.class)
class InstanceServiceTest {

	@Mock
	private InstanceRepository instanceRepositoryMock;

	@Mock
	private EncryptionUtility encryptionUtilityMock;

	@Mock
	private IntricClientFactory intricClientFactoryMock;

	@InjectMocks
	private InstanceService instanceService;

	@Captor
	private ArgumentCaptor<InstanceEntity> instanceEntityCaptor;

	@Test
	void createInstance() {

		// Arrange
		final var municipalityId = "2281";
		final var instanceId = "1234";
		final var instance = Instance.create().withPassword("someNewPassword");
		final var encryptedPassword = "encryptedPassword";
		final var instanceEntity = InstanceEntity.create().withId(instanceId).withMunicipalityId(municipalityId).withPassword(encryptedPassword);

		when(encryptionUtilityMock.encrypt(instance.getPassword().getBytes())).thenReturn(encryptedPassword);
		when(instanceRepositoryMock.save(any(InstanceEntity.class))).thenReturn(instanceEntity);

		// Act
		final var result = instanceService.createInstance(municipalityId, instance);

		// Assert
		assertThat(result).isEqualTo(instanceId);
		verify(instanceRepositoryMock).save(instanceEntityCaptor.capture());

		final var capturedInstanceEntity = instanceEntityCaptor.getValue();
		assertThat(capturedInstanceEntity).isNotNull();
		assertThat(capturedInstanceEntity.getPassword()).isEqualTo(encryptedPassword);
		assertThat(capturedInstanceEntity.getMunicipalityId()).isEqualTo(municipalityId);

		verify(encryptionUtilityMock).encrypt(instance.getPassword().getBytes());
		verify(intricClientFactoryMock).createIntricClient(instanceEntity);
		verifyNoMoreInteractions(instanceRepositoryMock, encryptionUtilityMock, intricClientFactoryMock);
	}

	@Test
	void deleteInstance() {
		final var municipalityId = "2281";
		final var instanceId = "1234";
		final var instanceEntity = InstanceEntity.create();

		when(instanceRepositoryMock.findByMunicipalityIdAndId(municipalityId, instanceId)).thenReturn(Optional.of(instanceEntity));

		instanceService.deleteInstance(municipalityId, instanceId);

		verify(instanceRepositoryMock).findByMunicipalityIdAndId(municipalityId, instanceId);
		verify(instanceRepositoryMock).delete(instanceEntity);
		verifyNoMoreInteractions(instanceRepositoryMock);
		verifyNoInteractions(encryptionUtilityMock, intricClientFactoryMock);
	}

	@Test
	void deleteInstanceThrowsException() {
		final var municipalityId = "2281";
		final var instanceId = "1234";

		when(instanceRepositoryMock.findByMunicipalityIdAndId(municipalityId, instanceId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> instanceService.deleteInstance(municipalityId, instanceId))
			.isInstanceOf(Problem.class)
			.hasFieldOrPropertyWithValue("title", "Not Found")
			.hasMessage(String.format("Not Found: An instance with id '%s' could not be found in municipality with id '%s'", instanceId, municipalityId));

		verify(instanceRepositoryMock).findByMunicipalityIdAndId(municipalityId, instanceId);
		verifyNoMoreInteractions(instanceRepositoryMock);
		verifyNoInteractions(encryptionUtilityMock, intricClientFactoryMock);
	}
}

package se.sundsvall.ai.flow.service;

import static org.zalando.problem.Status.NOT_FOUND;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.ai.flow.api.model.instance.Instance;
import se.sundsvall.ai.flow.integration.db.InstanceRepository;
import se.sundsvall.ai.flow.service.mapper.InstanceMapper;
import se.sundsvall.ai.flow.util.EncryptionUtility;

@Service
public class InstanceService {

	private final InstanceRepository instanceRepository;
	private final EncryptionUtility encryptionUtility;

	public InstanceService(final InstanceRepository instanceRepository, final EncryptionUtility encryptionUtility) {
		this.instanceRepository = instanceRepository;
		this.encryptionUtility = encryptionUtility;
	}

	@Transactional
	public String createInstance(final String municipalityId, final Instance instance) {
		final var encryptedPassword = encryptionUtility.encrypt(instance.getPassword().getBytes());
		final var result = instanceRepository.save(InstanceMapper.fromInstance(municipalityId, instance, encryptedPassword));
		//clientFactory.createClient(result);
		return result.getId();
	}

	@Transactional
	public void deleteInstance(final String municipalityId, final String instanceId) {
		final var entity = instanceRepository.findByMunicipalityIdAndId(municipalityId, instanceId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "An instance with id '%s' could not be found in municipality with id '%s'".formatted(instanceId, municipalityId)));
		instanceRepository.deleteById(instanceId);
	}
}

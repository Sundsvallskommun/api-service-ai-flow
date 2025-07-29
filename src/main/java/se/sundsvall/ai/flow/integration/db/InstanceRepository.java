package se.sundsvall.ai.flow.integration.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.ai.flow.integration.db.model.InstanceEntity;

@Repository
public interface InstanceRepository extends JpaRepository<InstanceEntity, String> {

	Optional<InstanceEntity> findByMunicipalityId(final String municipalityId);

	Optional<InstanceEntity> findByMunicipalityIdAndId(final String municipalityId, final String id);

}

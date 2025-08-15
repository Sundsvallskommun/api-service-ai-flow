package se.sundsvall.ai.flow.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.ai.flow.integration.db.model.InstanceEntity;

@Repository
@CircuitBreaker(name = "instanceRepository")
public interface InstanceRepository extends JpaRepository<InstanceEntity, String> {

	Optional<InstanceEntity> findByMunicipalityId(final String municipalityId);

}

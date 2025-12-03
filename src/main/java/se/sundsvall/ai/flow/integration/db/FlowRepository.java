package se.sundsvall.ai.flow.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.sundsvall.ai.flow.integration.db.model.FlowEntity;

@Repository
@CircuitBreaker(name = "flowRepository")
public interface FlowRepository extends JpaRepository<FlowEntity, FlowEntity.IdAndVersion> {

	boolean existsById(String id);

	boolean existsByIdAndVersion(String id, Integer version);

	void deleteById(String id);

	Optional<FlowEntity> findTopByIdOrderByVersionDesc(String id);

	@Query(value = "SELECT MAX(f.version) FROM flow f WHERE f.id = :id", nativeQuery = true)
	Optional<Integer> findMaxVersionById(@Param("id") final String id);
}

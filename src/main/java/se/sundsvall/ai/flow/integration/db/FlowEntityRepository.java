package se.sundsvall.ai.flow.integration.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowEntityRepository extends JpaRepository<FlowEntity, FlowEntityId> {

	@Query(value = "SELECT MAX(f.version) FROM flow f WHERE f.name = :name", nativeQuery = true)
	Optional<Integer> findMaxVersionByName(final String name);

}

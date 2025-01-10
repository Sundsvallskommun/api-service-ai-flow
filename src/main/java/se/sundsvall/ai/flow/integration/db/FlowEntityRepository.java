package se.sundsvall.ai.flow.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlowEntityRepository extends JpaRepository<FlowEntity, FlowEntityId> {

}

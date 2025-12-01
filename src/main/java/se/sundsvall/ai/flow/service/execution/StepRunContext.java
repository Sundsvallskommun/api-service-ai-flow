package se.sundsvall.ai.flow.service.execution;

import java.util.List;
import java.util.UUID;
import se.sundsvall.ai.flow.model.session.Session;
import se.sundsvall.ai.flow.model.session.StepExecution;

/** Immutable data required to execute a step. */
public record StepRunContext(
	String municipalityId,
	Session session,
	StepExecution stepExecution,
	List<String> inputsInUse,
	List<UUID> inputFileIdsInUse,
	String inputsInUseInfo,
	String userInput,
	boolean runRequiredSteps) {
}

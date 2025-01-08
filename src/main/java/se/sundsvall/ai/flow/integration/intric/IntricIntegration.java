package se.sundsvall.ai.flow.integration.intric;

import org.springframework.stereotype.Component;
import se.sundsvall.ai.flow.integration.intric.model.RunService;
import se.sundsvall.ai.flow.service.flow.ExecutionState;
import se.sundsvall.ai.flow.service.flow.StepExecution;

@Component
public class IntricIntegration {

	public static final String CLIENT_ID = "intric";

	private final IntricClient client;

	IntricIntegration(final IntricClient client) {
		this.client = client;
	}

	public void runService(final StepExecution stepExecution, final String input) {
		try {
			var response = client.runService(stepExecution.getStep().getIntricServiceId(), new RunService(input));

			// Store the output
			stepExecution.setOutput(response.output());
			// Mark the step execution as done
			stepExecution.setState(ExecutionState.DONE);
		} catch (Exception e) {
			// Store the exception
			stepExecution.setErrorMessage(e.getMessage());
			// Mark the step execution as done
			stepExecution.setState(ExecutionState.ERROR);
		}
	}
}

package se.sundsvall.ai.flow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.TestDataFactory.createFlowEntity;
import static se.sundsvall.ai.flow.service.FlowMapper.toFlowResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.ai.flow.TestDataFactory;
import se.sundsvall.ai.flow.integration.db.FlowEntityId;
import se.sundsvall.ai.flow.integration.db.FlowEntityRepository;

@ExtendWith(MockitoExtension.class)
class FlowServiceTest {

	@Mock
	private FlowEntityRepository flowEntityRepositoryMock;

	@Mock
	private ObjectMapper objectMapperMock;

	@InjectMocks
	private FlowService flowService;

	/**
	 * Test scenario where flow is found.
	 */
	@Test
	void getFlow_1() {
		var flowName = "flowName";
		var version = 1;
		var flowEntity = createFlowEntity();
		when(flowEntityRepositoryMock.findById(any(FlowEntityId.class))).thenReturn(Optional.of(flowEntity));

		var result = flowService.getFlow(flowName, version);

		assertThat(result).usingRecursiveComparison().isEqualTo(toFlowResponse(flowEntity));
		verify(flowEntityRepositoryMock).findById(new FlowEntityId(flowName, version));
		verifyNoMoreInteractions(flowEntityRepositoryMock);
	}

	/**
	 * Test scenario where flow is not found and problem is thrown.
	 */
	@Test
	void getFlow_2() {
		var flowName = "flowName";
		var version = 1;
		when(flowEntityRepositoryMock.findById(any(FlowEntityId.class))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> flowService.getFlow(flowName, version))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("title", "Not Found")
			.hasFieldOrPropertyWithValue("detail", "No flow found with name %s and version %s".formatted(flowName, version));

		verify(flowEntityRepositoryMock).findById(new FlowEntityId(flowName, version));
		verifyNoMoreInteractions(flowEntityRepositoryMock);
	}

	@Test
	void getFlows() {
		var flowEntity = createFlowEntity();
		when(flowEntityRepositoryMock.findAll()).thenReturn(List.of(flowEntity));

		var result = flowService.getFlows();

		assertThat(result.flows()).hasSize(1);
		assertThat(result.flows().getFirst()).usingRecursiveComparison().isEqualTo(FlowMapper.toFlowSummary(flowEntity));
		verify(flowEntityRepositoryMock).findAll();
		verifyNoMoreInteractions(flowEntityRepositoryMock);
	}

	/**
	 * Test scenario where flow exists and is deleted.
	 */
	@Test
	void deleteFlow_1() {
		var flowName = "flowName";
		var version = 1;
		when(flowEntityRepositoryMock.existsById(any(FlowEntityId.class))).thenReturn(true);

		flowService.deleteFlow(flowName, version);

		verify(flowEntityRepositoryMock).existsById(new FlowEntityId(flowName, version));
		verify(flowEntityRepositoryMock).deleteById(new FlowEntityId(flowName, version));
		verifyNoMoreInteractions(flowEntityRepositoryMock);
	}

	/**
	 * Test scenario where flow does not exist and problem is thrown.
	 */
	@Test
	void deleteFlow_2() {
		var flowName = "flowName";
		var version = 1;
		when(flowEntityRepositoryMock.existsById(any(FlowEntityId.class))).thenReturn(false);

		assertThatThrownBy(() -> flowService.deleteFlow(flowName, version))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("title", "Not Found")
			.hasFieldOrPropertyWithValue("detail", "No flow found with name %s and version %s".formatted(flowName, version));

		verify(flowEntityRepositoryMock).existsById(new FlowEntityId(flowName, version));
		verifyNoMoreInteractions(flowEntityRepositoryMock);
	}

	/**
	 * Test scenario where flow is created with version 2.
	 */
	@Test
	void createFlow_1() throws JsonProcessingException {
		var flow = TestDataFactory.createFlow();
		when(flowEntityRepositoryMock.findMaxVersionByName(flow.getName())).thenReturn(Optional.of(1));

		var result = flowService.createFlow(flow);

		assertThat(result).isNotNull();
		assertThat(result.toString()).isEqualTo("/Tj%C3%A4nsteskrivelse/2");
		verify(flowEntityRepositoryMock).findMaxVersionByName(flow.getName());
		verify(objectMapperMock).writeValueAsString(flow);
		verify(flowEntityRepositoryMock).save(any());
		verifyNoMoreInteractions(flowEntityRepositoryMock, objectMapperMock);
	}

	/**
	 * Test scenario where flow could not be written as string and problem is thrown.
	 */
	@Test
	void createFlow_2() throws JsonProcessingException {
		var flow = TestDataFactory.createFlow();
		when(flowEntityRepositoryMock.findMaxVersionByName(flow.getName())).thenReturn(Optional.of(1));
		when(objectMapperMock.writeValueAsString(flow)).thenThrow(JsonProcessingException.class);

		assertThatThrownBy(() -> flowService.createFlow(flow))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.INTERNAL_SERVER_ERROR)
			.hasFieldOrPropertyWithValue("title", "Internal Server Error")
			.hasFieldOrPropertyWithValue("detail", "Flow could not be written as string");

		verify(flowEntityRepositoryMock).findMaxVersionByName(flow.getName());
		verify(objectMapperMock).writeValueAsString(flow);
		verifyNoMoreInteractions(flowEntityRepositoryMock);
	}

}

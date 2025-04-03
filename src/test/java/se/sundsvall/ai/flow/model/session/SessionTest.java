package se.sundsvall.ai.flow.model.session;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.sundsvall.ai.flow.model.session.Session.FIlE_INFO_TEMPLATE;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.support.StringMultipartFile;
import se.sundsvall.ai.flow.model.support.UploadedMultipartFile;

@ExtendWith(MockitoExtension.class)
class SessionTest {

	@Test
	void getAndSetState() {
		var state = Session.State.FINISHED;
		var flow = new Flow();

		var session = new Session(flow);
		assertThat(session.getState()).isEqualTo(Session.State.CREATED);

		session.setState(state);

		assertThat(session.getState()).isEqualTo(state);
		assertThat(session.getFlow()).isEqualTo(flow);
	}

	@Test
	void getLastUpdatedAt() {
		var flow = new Flow()
			.withSteps(List.of(
				new Step().withId("step1"),
				new Step().withId("step2"),
				new Step().withId("step3")));

		var session = new Session(flow);
		ReflectionTestUtils.setField(session.getStepExecution("step1"), "lastUpdatedAt", LocalDateTime.now());
		ReflectionTestUtils.setField(session.getStepExecution("step2"), "lastUpdatedAt", LocalDateTime.now().plusHours(1));

		var result = session.getLastUpdatedAt();

		assertThat(result).isEqualTo(session.getStepExecution("step2").getLastUpdatedAt());
	}

	@Test
	void getLastUpdatedAtAlthoughNoStepsHaveBeenUpdated() {
		var flow = new Flow()
			.withSteps(List.of(
				new Step().withId("step1"),
				new Step().withId("step2"),
				new Step().withId("step3")));

		var session = new Session(flow);

		var result = session.getLastUpdatedAt();

		assertThat(result).isNotNull();
	}

	@Test
	void createInputInfo() {
		var key = "someKey";
		var name = "someName";
		var intricFileIds = List.of(UUID.randomUUID(), UUID.randomUUID());
		var info = String.format(FIlE_INFO_TEMPLATE, name.toLowerCase(), String.join(",", intricFileIds.stream().map(UUID::toString).toList()));
		var inputs = intricFileIds.stream().map(intricFileId -> new Input(null).withIntricFileId(intricFileId)).toList();

		var session = new Session(new Flow());
		var inputInfo = session.createInputInfo(key, name, inputs);

		assertThat(inputInfo.getKey()).isEqualTo(key);
		assertThat(inputInfo.getValue()).isEqualTo(info);
	}

	@Test
	void addSimpleInput() {
		var inputId = "input1";
		var inputName = "someName";
		var inputValue1 = "someValue";
		var inputValue2 = "someOtherValue";

		var flow = new Flow()
			.withFlowInputs(List.of(
				new FlowInput().withId(inputId).withName(inputName).withMultipleValued(true)));

		var session = new Session(flow);
		session.addSimpleInput(inputId, inputValue1);
		session.addSimpleInput(inputId, inputValue2);

		assertThat(session.getInput()).containsKey(inputId);
		assertThat(session.getInput().get(inputId)).hasSize(2).allSatisfy(input -> {
			assertThat(input.isUploadedToIntric()).isFalse();
			assertThat(input.getFile()).asInstanceOf(type(StringMultipartFile.class)).satisfies(stringMultipartFile -> {
				assertThat(stringMultipartFile.getName()).isEqualTo(inputName);
				assertThat(stringMultipartFile.getValue()).isIn(inputValue1, inputValue2);
			});
		});
	}

	@Test
	void addFileInput() throws IOException {
		var inputId = "input1";
		var inputName = "someName";
		var inputValue1 = "someValue";
		var inputValue2 = "someOtherValue";
		var inputFile1 = mock(MultipartFile.class);
		var inputFile2 = mock(MultipartFile.class);

		when(inputFile1.getBytes()).thenReturn(inputValue1.getBytes(UTF_8));
		when(inputFile2.getBytes()).thenReturn(inputValue2.getBytes(UTF_8));

		var flow = new Flow()
			.withFlowInputs(List.of(
				new FlowInput().withId(inputId).withName(inputName).withMultipleValued(true)));

		var session = new Session(flow);
		session.addFileInput(inputId, inputFile1);
		session.addFileInput(inputId, inputFile2);

		assertThat(session.getInput()).containsKey(inputId);
		assertThat(session.getInput().get(inputId)).hasSize(2).allSatisfy(input -> {
			assertThat(input.isUploadedToIntric()).isFalse();
			assertThat(input.getFile()).asInstanceOf(type(UploadedMultipartFile.class)).satisfies(uploadedMultipartFile -> {
				assertThat(uploadedMultipartFile.getName()).isEqualTo(inputName);
				assertThat(uploadedMultipartFile.getBytes()).isIn(inputValue1.getBytes(UTF_8), inputValue2.getBytes(UTF_8));
			});
		});
	}
}

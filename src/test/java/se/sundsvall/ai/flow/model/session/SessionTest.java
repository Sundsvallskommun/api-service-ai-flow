package se.sundsvall.ai.flow.model.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.sundsvall.ai.flow.model.flowdefinition.Flow;
import se.sundsvall.ai.flow.model.flowdefinition.FlowInput;
import se.sundsvall.ai.flow.model.flowdefinition.Step;
import se.sundsvall.ai.flow.model.support.ByteArrayMultipartFile;
import se.sundsvall.ai.flow.model.support.StringMultipartFile;

@ExtendWith(MockitoExtension.class)
class SessionTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Test
	void getAndSetState() {
		final var state = Session.State.FINISHED;
		final var flow = new Flow();

		final var session = new Session(MUNICIPALITY_ID, flow);
		assertThat(session.getState()).isEqualTo(Session.State.CREATED);

		session.setState(state);

		assertThat(session.getState()).isEqualTo(state);
		assertThat(session.getFlow()).isEqualTo(flow);
	}

	@Test
	void getLastUpdatedAt() {
		final var flow = new Flow()
			.withSteps(List.of(
				new Step().withId("step1"),
				new Step().withId("step2"),
				new Step().withId("step3")));

		final var session = new Session(MUNICIPALITY_ID, flow);
		ReflectionTestUtils.setField(session.getStepExecution("step1"), "lastUpdatedAt", LocalDateTime.now());
		ReflectionTestUtils.setField(session.getStepExecution("step2"), "lastUpdatedAt", LocalDateTime.now().plusHours(1));

		final var result = session.getLastUpdatedAt();

		assertThat(result).isEqualTo(session.getStepExecution("step2").getLastUpdatedAt());
	}

	@Test
	void getLastUpdatedAtAlthoughNoStepsHaveBeenUpdated() {
		final var flow = new Flow()
			.withSteps(List.of(
				new Step().withId("step1"),
				new Step().withId("step2"),
				new Step().withId("step3")));

		final var session = new Session(MUNICIPALITY_ID, flow);

		final var result = session.getLastUpdatedAt();

		assertThat(result).isNotNull();
	}

	@Test
	void viewsAreUnmodifiable() {
		final var inputId = "input1";
		final var flow = new Flow().withFlowInputs(List.of(new FlowInput().withId(inputId).withName("name").withMultipleValued(true)));
		final var session = new Session(MUNICIPALITY_ID, flow);
		session.addSimpleInput(inputId, "val");

		// getInput map should be unmodifiable
		final var inputsView = session.getInput();
		assertThat(inputsView).containsKey(inputId);
		assertThat(inputsView.entrySet()).isUnmodifiable();
		assertThat(inputsView.get(inputId)).isUnmodifiable();
	}

	@Test
	void addSimpleInput() {
		final var inputId = "input1";
		final var inputName = "someName";
		final var inputValue1 = "someValue";
		final var inputValue2 = "someOtherValue";

		final var flow = new Flow()
			.withFlowInputs(List.of(
				new FlowInput().withId(inputId).withName(inputName).withMultipleValued(true)));

		final var session = new Session(MUNICIPALITY_ID, flow);
		session.addSimpleInput(inputId, inputValue1);
		session.addSimpleInput(inputId, inputValue2);

		assertThat(session.getInput()).containsKey(inputId);
		assertThat(session.getInput().get(inputId)).hasSize(2).allSatisfy(input -> {
			assertThat(input.isUploadedToEneo()).isFalse();
			assertThat(input.getFile()).asInstanceOf(type(StringMultipartFile.class)).satisfies(stringMultipartFile -> {
				assertThat(stringMultipartFile.getName()).isEqualTo(inputName);
				assertThat(stringMultipartFile.getValue()).isIn(inputValue1, inputValue2);
			});
		});
	}

	@Test
	void addFileInputUsingInputValue() {
		final var inputId = "input1";
		final var inputName = "someName";
		final var inputValue1 = "someValue";
		final var inputValue2 = "someOtherValue";

		final var flow = new Flow()
			.withFlowInputs(List.of(new FlowInput().withId(inputId).withName(inputName).withMultipleValued(true)));

		final var session = new Session(MUNICIPALITY_ID, flow);
		session.addInput(inputId, new FileInputValue(inputName, inputValue1.getBytes(), "text/plain"));
		session.addInput(inputId, new FileInputValue(inputName, inputValue2.getBytes(), "text/plain"));

		assertThat(session.getInput()).containsKey(inputId);
		assertThat(session.getInput().get(inputId)).hasSize(2).allSatisfy(input -> {
			assertThat(input.isUploadedToEneo()).isFalse();
			assertThat(input.getFile()).asInstanceOf(type(ByteArrayMultipartFile.class)).satisfies(byteArrayMultipartFile -> {
				assertThat(byteArrayMultipartFile.getName()).isEqualTo(inputName);
			});
		});
	}
}

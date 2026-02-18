package se.sundsvall.ai.flow.model.session;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class InputTest {

	@Test
	void builder() {
		// Arrange
		final var eneoFileId = UUID.randomUUID();
		final var file = new MockMultipartFile("Some String", "some bytes".getBytes());
		// Act
		final var result = new Input(file)
			.withEneoFileId(eneoFileId);

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getEneoFileId()).isEqualTo(eneoFileId);
		assertThat(result.getFile()).isSameAs(file);

	}

	@Test
	void testEquals() {
		final var file = new MockMultipartFile("Some String", "some bytes".getBytes());
		final var input1 = new Input(file);
		final var input2 = new Input(file);

		assertThat(input1.equals(input2)).isTrue();

		input1.setEneoFileId(UUID.randomUUID());
		input2.setEneoFileId(UUID.randomUUID());

		assertThat(input1.equals(input2)).isFalse();
	}

	@Test
	void testHashCode() {
		final var file = new MockMultipartFile("Some String", "some bytes".getBytes());
		final var input1 = new Input(file);
		final var input2 = new Input(file);

		assertThat(input1).hasSameHashCodeAs(input2);

		final var eneoFileId = UUID.randomUUID();
		input1.setEneoFileId(eneoFileId);
		input2.setEneoFileId(eneoFileId);

		assertThat(input1).hasSameHashCodeAs(input2);

		input1.setEneoFileId(UUID.randomUUID());
		input2.setEneoFileId(UUID.randomUUID());

		assertThat(input1.hashCode()).isNotEqualTo(input2.hashCode());
	}
}

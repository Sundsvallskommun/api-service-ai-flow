package se.sundsvall.ai.flow.model.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.multipart.MultipartFile;

class InputTest {

	@Mock
	private MultipartFile mockMultipartFile;

	@Test
	void constructorAndGetters() {
		var input = new Input(mockMultipartFile);

		assertThat(input.getFile()).isEqualTo(mockMultipartFile);
		assertThat(input.getIntricFileId()).isNull();
		assertThat(input.isUploadedToIntric()).isFalse();
	}

	@Test
	void setIntricFileId() {
		var intricFileId = UUID.randomUUID();

		var input = new Input(mockMultipartFile);
		input.setIntricFileId(intricFileId);

		assertThat(input.getIntricFileId()).isEqualTo(intricFileId);
		assertThat(input.isUploadedToIntric()).isTrue();
	}

	@Test
	void testEquals() {
		var input1 = new Input(mockMultipartFile);
		var input2 = new Input(mockMultipartFile);

		assertThat(input1.equals(input2)).isTrue();

		input1.setIntricFileId(UUID.randomUUID());
		input2.setIntricFileId(UUID.randomUUID());

		assertThat(input1.equals(input2)).isFalse();
	}

	@Test
	void testHashCode() {
		var input1 = new Input(mockMultipartFile);
		var input2 = new Input(mockMultipartFile);

		assertThat(input1).hasSameHashCodeAs(input2);

		var intricFileId = UUID.randomUUID();
		input1.setIntricFileId(intricFileId);
		input2.setIntricFileId(intricFileId);

		assertThat(input1).hasSameHashCodeAs(input2);

		input1.setIntricFileId(UUID.randomUUID());
		input2.setIntricFileId(UUID.randomUUID());

		assertThat(input1.hashCode()).isNotEqualTo(input2.hashCode());
	}
}

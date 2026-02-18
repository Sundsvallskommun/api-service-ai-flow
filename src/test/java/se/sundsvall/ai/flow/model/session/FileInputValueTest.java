package se.sundsvall.ai.flow.model.session;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileInputValueTest {

	@Test
	void equalsAndHashCode_sameValuesDifferentArrayInstances_areEqual() {
		final var content = new byte[] {
			1, 2, 3
		};
		final var fileInputValue1 = new FileInputValue("file.txt", content, "text/plain");
		final var fileInputValue2 = new FileInputValue("file.txt", Arrays.copyOf(content, content.length), "text/plain");

		assertThat(fileInputValue1).isEqualTo(fileInputValue2);
		assertThat(fileInputValue1.hashCode()).hasSameHashCodeAs(fileInputValue2.hashCode());
	}

	@Test
	void equals_differentName_notEqual() {
		final var content = new byte[] {
			1, 2, 3
		};
		final var fileInputValue1 = new FileInputValue("fileA", content, "text/plain");
		final var fileInputValue2 = new FileInputValue("fileB", content, "text/plain");

		assertThat(fileInputValue1).isNotEqualTo(fileInputValue2);
	}

	@Test
	void equals_differentContent_notEqual() {
		final var fileInputValue1 = new FileInputValue("file", new byte[] {
			1, 2, 3
		}, "text/plain");
		final var fileInputValue2 = new FileInputValue("file", new byte[] {
			4, 5, 6
		}, "text/plain");

		assertThat(fileInputValue1).isNotEqualTo(fileInputValue2);
	}

	@Test
	void equals_differentContentType_notEqual() {
		final var content = new byte[] {
			1
		};
		final var fileInputValue1 = new FileInputValue("file", content, "text/plain");
		final var fileInputValue2 = new FileInputValue("file", content, "application/octet-stream");

		assertThat(fileInputValue1).isNotEqualTo(fileInputValue2);
	}

	@Test
	void accessorsAndToString_containsFieldsAndArrayRepresentation() {
		final var content = new byte[] {
			7, 8, 9
		};
		final var fileInputValue = new FileInputValue("myfile", content, "application/octet-stream");

		assertThat(fileInputValue.name()).isEqualTo("myfile");
		assertThat(fileInputValue.content()).containsExactly((byte) 7, (byte) 8, (byte) 9);
		assertThat(fileInputValue.contentType()).isEqualTo("application/octet-stream");

		final var toString = fileInputValue.toString();
		assertThat(toString).contains("FileInputValue{")
			.contains("name='myfile'")
			.contains(Arrays.toString(content))
			.contains("contentType='application/octet-stream'");
	}
}

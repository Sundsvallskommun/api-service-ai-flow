package se.sundsvall.ai.flow.model.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class FileInputValueTest {

	@Test
	void equalsAndHashCode_sameValuesDifferentArrayInstances_areEqual() {
		final byte[] content = new byte[] {
			1, 2, 3
		};
		final var a = new FileInputValue("file.txt", content, "text/plain");
		final var b = new FileInputValue("file.txt", Arrays.copyOf(content, content.length), "text/plain");

		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).hasSameHashCodeAs(b.hashCode());
	}

	@Test
	void equals_differentName_notEqual() {
		final byte[] content = new byte[] {
			1, 2, 3
		};
		final var a = new FileInputValue("fileA", content, "text/plain");
		final var b = new FileInputValue("fileB", content, "text/plain");

		assertThat(a).isNotEqualTo(b);
	}

	@Test
	void equals_differentContent_notEqual() {
		final var a = new FileInputValue("file", new byte[] {
			1, 2, 3
		}, "text/plain");
		final var b = new FileInputValue("file", new byte[] {
			4, 5, 6
		}, "text/plain");

		assertThat(a).isNotEqualTo(b);
	}

	@Test
	void equals_differentContentType_notEqual() {
		final byte[] content = new byte[] {
			1
		};
		final var a = new FileInputValue("file", content, "text/plain");
		final var b = new FileInputValue("file", content, "application/octet-stream");

		assertThat(a).isNotEqualTo(b);
	}

	@Test
	void accessorsAndToString_containsFieldsAndArrayRepresentation() {
		final byte[] content = new byte[] {
			7, 8, 9
		};
		final var v = new FileInputValue("myfile", content, "application/octet-stream");

		assertThat(v.name()).isEqualTo("myfile");
		assertThat(v.content()).containsExactly((byte) 7, (byte) 8, (byte) 9);
		assertThat(v.contentType()).isEqualTo("application/octet-stream");

		final String s = v.toString();
		assertThat(s).contains("FileInputValue{")
			.contains("name='myfile'")
			.contains(Arrays.toString(content))
			.contains("contentType='application/octet-stream'");
	}
}

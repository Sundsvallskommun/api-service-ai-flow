package se.sundsvall.ai.flow.model.support;

import java.io.File;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ByteArrayMultipartFileTest {

	@Test
	void coversAllAccessors_nonEmpty() throws Exception {
		final var bytes = "hello".getBytes();
		final var file = new ByteArrayMultipartFile("file.txt", bytes, "text/plain");

		assertThat(file.getName()).isEqualTo("file.txt");
		assertThat(file.getOriginalFilename()).isEqualTo("file.txt");
		assertThat(file.getContentType()).isEqualTo("text/plain");
		assertThat(file.isEmpty()).isFalse();
		assertThat(file.getSize()).isEqualTo(bytes.length);
		assertThat(file.getBytes()).isEqualTo(bytes);
		try (final var inputStream = file.getInputStream()) {
			assertThat(inputStream.readAllBytes()).isEqualTo(bytes);
		}

		final var destination = new File("/tmp/nowhere");
		assertThatThrownBy(() -> file.transferTo(destination))
			.isInstanceOf(UnsupportedOperationException.class)
			.hasMessageContaining("not supported");
	}

	@Test
	void emptyWhenNullBytes() {
		final var file = new ByteArrayMultipartFile("empty", null, null);
		assertThat(file.isEmpty()).isTrue();
		assertThat(file.getSize()).isZero();
		assertThat(file.getContentType()).isNull();
		assertThat(file.getBytes()).isEmpty();
	}

	@Nested
	class SerializerTest {

		@Test
		void serialize() {
			final var file = new ByteArrayMultipartFile("file.txt", "hello".getBytes(), "text/plain");
			final var serializer = new ByteArrayMultipartFile.Serializer();
			final var mockJsonGenerator = mock(JsonGenerator.class);

			serializer.serialize(file, mockJsonGenerator, null);

			verify(mockJsonGenerator).writeStartObject();
			verify(mockJsonGenerator).writeStringProperty("type", "file");
			verify(mockJsonGenerator).writeStringProperty("contentType", "text/plain");
			verify(mockJsonGenerator).writeNumberProperty("size", file.getSize());
			verify(mockJsonGenerator).writeEndObject();
			verifyNoMoreInteractions(mockJsonGenerator);
		}
	}
}

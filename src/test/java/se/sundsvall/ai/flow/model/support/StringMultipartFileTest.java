package se.sundsvall.ai.flow.model.support;

import java.io.ByteArrayInputStream;
import java.io.File;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonGenerator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

class StringMultipartFileTest {

	@Test
	void multipartFileContract() {
		final var name = "someName";
		final var value = "someValue";

		final var stringMultipartFile = new StringMultipartFile(name, value);

		assertThat(stringMultipartFile.getValue()).isEqualTo(value);
		assertThat(stringMultipartFile.getName()).isEqualTo(name);
		assertThat(stringMultipartFile.getOriginalFilename()).isEqualTo(name);
		assertThat(stringMultipartFile.getContentType()).isEqualTo(TEXT_PLAIN_VALUE);
		assertThat(stringMultipartFile.isEmpty()).isFalse();
		assertThat(stringMultipartFile.getSize()).isEqualTo(stringMultipartFile.getBytes().length);
		assertThat(stringMultipartFile.getBytes()).isEqualTo("%s:%s".formatted(name, value).getBytes(UTF_8));
		assertThat(stringMultipartFile.getInputStream()).isInstanceOf(ByteArrayInputStream.class);
		assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> stringMultipartFile.transferTo(mock(File.class)))
			.withMessage("NOT IMPLEMENTED");
	}

	@Nested
	class SerializerTest {

		@Test
		void serialize() {
			final var name = "someName";
			final var value = "someValue";

			final var stringMultipartFile = new StringMultipartFile(name, value);
			final var serializer = new StringMultipartFile.Serializer();
			final var mockJsonGenerator = mock(JsonGenerator.class);

			serializer.serialize(stringMultipartFile, mockJsonGenerator, null);

			verify(mockJsonGenerator).writeStartObject();
			verify(mockJsonGenerator).writeStringProperty("type", "simple");
			verify(mockJsonGenerator).writeStringProperty("contentType", stringMultipartFile.getContentType());
			verify(mockJsonGenerator).writeNumberProperty("size", stringMultipartFile.getSize());
			verify(mockJsonGenerator).writeEndObject();
			verifyNoMoreInteractions(mockJsonGenerator);
		}
	}
}

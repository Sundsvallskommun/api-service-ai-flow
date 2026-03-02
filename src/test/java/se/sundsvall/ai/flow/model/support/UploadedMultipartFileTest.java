package se.sundsvall.ai.flow.model.support;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JsonGenerator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@ExtendWith(MockitoExtension.class)
class UploadedMultipartFileTest {

	private static final String FILE_CONTENT = "someFileContent";

	@Mock
	private MultipartFile mockOriginalMultipartFile;

	@BeforeEach
	void setUp() throws IOException {
		when(mockOriginalMultipartFile.getBytes()).thenReturn(FILE_CONTENT.getBytes(UTF_8));
		when(mockOriginalMultipartFile.getContentType()).thenReturn(TEXT_PLAIN_VALUE);
	}

	@Test
	void multipartFileContract() throws IOException {
		final var name = "someName";

		final var uploadedMultipartFile = new UploadedMultipartFile(name, mockOriginalMultipartFile);

		assertThat(uploadedMultipartFile.getName()).isEqualTo(name);
		assertThat(uploadedMultipartFile.getOriginalFilename()).isEqualTo(name);
		assertThat(uploadedMultipartFile.getContentType()).isEqualTo(TEXT_PLAIN_VALUE);
		assertThat(uploadedMultipartFile.isEmpty()).isFalse();
		assertThat(uploadedMultipartFile.getSize()).isEqualTo(uploadedMultipartFile.getBytes().length);
		assertThat(uploadedMultipartFile.getBytes()).isEqualTo(mockOriginalMultipartFile.getBytes());
		assertThat(uploadedMultipartFile.getInputStream()).isInstanceOf(ByteArrayInputStream.class);
		assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> uploadedMultipartFile.transferTo(mock(File.class)))
			.withMessage("NOT IMPLEMENTED");
	}

	@Nested
	class SerializerTest {

		@Test
		void serialize() {
			final var name = "someName";

			final var uploadedMultipartFile = new UploadedMultipartFile(name, mockOriginalMultipartFile);
			final var serializer = new UploadedMultipartFile.Serializer();
			final var mockJsonGenerator = mock(JsonGenerator.class);

			serializer.serialize(uploadedMultipartFile, mockJsonGenerator, null);

			verify(mockJsonGenerator).writeStartObject();
			verify(mockJsonGenerator).writeStringProperty("type", "file");
			verify(mockJsonGenerator).writeStringProperty("contentType", uploadedMultipartFile.getContentType());
			verify(mockJsonGenerator).writeNumberProperty("size", uploadedMultipartFile.getSize());
			verify(mockJsonGenerator).writeEndObject();
			verifyNoMoreInteractions(mockJsonGenerator);
		}
	}
}

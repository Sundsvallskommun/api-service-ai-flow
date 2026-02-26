package se.sundsvall.ai.flow.model.support;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Serial;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.StdSerializer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@JsonSerialize(using = StringMultipartFile.Serializer.class)
public class StringMultipartFile implements MultipartFile {

	private final String name;
	private final String value;

	public StringMultipartFile(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getOriginalFilename() {
		return getName();
	}

	@Override
	public String getContentType() {
		return TEXT_PLAIN_VALUE;
	}

	@Override
	public boolean isEmpty() {
		return value.isBlank();
	}

	@Override
	public long getSize() {
		return getBytes().length;
	}

	@Override
	public byte[] getBytes() {
		return "%s:%s".formatted(name, value).getBytes(UTF_8);
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(getBytes());
	}

	@Override
	public void transferTo(final File dest) throws IllegalStateException {
		throw new UnsupportedOperationException("NOT IMPLEMENTED");
	}

	static class Serializer extends StdSerializer<StringMultipartFile> {

		@Serial
		private static final long serialVersionUID = -666632197878666L;

		protected Serializer() {
			super(StringMultipartFile.class);
		}

		@Override
		public void serialize(final StringMultipartFile stringMultipartFile, final JsonGenerator jsonGenerator, final SerializationContext serializationContext) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("type", "simple");
			jsonGenerator.writeStringProperty("contentType", stringMultipartFile.getContentType());
			jsonGenerator.writeNumberProperty("size", stringMultipartFile.getSize());
			jsonGenerator.writeEndObject();
		}
	}
}

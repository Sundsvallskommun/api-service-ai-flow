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

/** Simple in-memory MultipartFile backed by a byte array. */
@JsonSerialize(using = ByteArrayMultipartFile.Serializer.class)
public class ByteArrayMultipartFile implements MultipartFile {

	private final String name;
	private final byte[] content;
	private final String contentType;

	public ByteArrayMultipartFile(final String name, final byte[] content, final String contentType) {
		this.name = name;
		this.content = content != null ? content : new byte[0];
		this.contentType = contentType;
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
		return contentType;
	}

	@Override
	public boolean isEmpty() {
		return content.length == 0;
	}

	@Override
	public long getSize() {
		return content.length;
	}

	@Override
	public byte[] getBytes() {
		return content;
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(content);
	}

	@Override
	public void transferTo(final File dest) throws IllegalStateException {
		throw new UnsupportedOperationException("transferTo not supported for in-memory files");
	}

	static class Serializer extends StdSerializer<ByteArrayMultipartFile> {

		@Serial
		private static final long serialVersionUID = -444332197878444L;

		protected Serializer() {
			super(ByteArrayMultipartFile.class);
		}

		@Override
		public void serialize(final ByteArrayMultipartFile byteArrayMultipartFile, final JsonGenerator jsonGenerator, final SerializationContext serializationContext) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("type", "file");
			jsonGenerator.writeStringProperty("contentType", byteArrayMultipartFile.getContentType());
			jsonGenerator.writeNumberProperty("size", byteArrayMultipartFile.getSize());
			jsonGenerator.writeEndObject();
		}
	}
}

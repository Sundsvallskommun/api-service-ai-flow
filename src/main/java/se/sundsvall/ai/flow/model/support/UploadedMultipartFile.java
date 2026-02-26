package se.sundsvall.ai.flow.model.support;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.ai.flow.model.flowdefinition.exception.FlowException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = UploadedMultipartFile.Serializer.class)
public class UploadedMultipartFile implements MultipartFile {

	private final String name;
	private final byte[] value;
	private final String contentType;

	public UploadedMultipartFile(final String name, final MultipartFile original) {
		this.name = name;

		try {
			value = original.getBytes();
			contentType = original.getContentType();
		} catch (final IOException e) {
			throw new FlowException("Unable to handle uploaded file", e);
		}
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
		return value.length == 0;
	}

	@Override
	public long getSize() {
		return getBytes().length;
	}

	@Override
	public byte[] getBytes() {
		return value;
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(getBytes());
	}

	@Override
	public void transferTo(final File dest) throws IllegalStateException {
		throw new UnsupportedOperationException("NOT IMPLEMENTED");
	}

	static class Serializer extends StdSerializer<UploadedMultipartFile> {

		@Serial
		private static final long serialVersionUID = -555432197878555L;

		protected Serializer() {
			super(UploadedMultipartFile.class);
		}

		@Override
		public void serialize(final UploadedMultipartFile uploadedMultipartFile, final JsonGenerator jsonGenerator, final SerializationContext serializationContext) {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringProperty("type", "file");
			jsonGenerator.writeStringProperty("contentType", uploadedMultipartFile.getContentType());
			jsonGenerator.writeNumberProperty("size", uploadedMultipartFile.getSize());
			jsonGenerator.writeEndObject();
		}
	}
}

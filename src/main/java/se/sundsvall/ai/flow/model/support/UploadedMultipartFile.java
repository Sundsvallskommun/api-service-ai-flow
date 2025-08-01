package se.sundsvall.ai.flow.model.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.ai.flow.model.flowdefinition.exception.FlowException;

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
		} catch (IOException e) {
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

		protected Serializer() {
			super(UploadedMultipartFile.class);
		}

		@Override
		public void serialize(final UploadedMultipartFile uploadedMultipartFile, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
			jsonGenerator.writeStartObject();
			jsonGenerator.writeStringField("type", "file");
			jsonGenerator.writeStringField("contentType", uploadedMultipartFile.getContentType());
			jsonGenerator.writeNumberField("size", uploadedMultipartFile.getSize());
			jsonGenerator.writeEndObject();
		}
	}
}

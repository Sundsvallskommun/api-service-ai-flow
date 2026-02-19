package se.sundsvall.ai.flow.model.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

import static java.util.Objects.nonNull;

/*
 * "Mapping" between locally uploaded files and Intric file uploads
 */
public class Input {

	private final MultipartFile file;
	@JsonIgnore
	private UUID intricFileId;

	public Input(final MultipartFile file) {
		this.file = file;
	}

	public MultipartFile getFile() {
		return file;
	}

	public boolean isUploadedToIntric() {
		return nonNull(intricFileId);
	}

	public UUID getIntricFileId() {
		return intricFileId;
	}

	public Input withIntricFileId(final UUID intricFileId) {
		this.intricFileId = intricFileId;
		return this;
	}

	public void setIntricFileId(final UUID intricFileId) {
		this.intricFileId = intricFileId;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Input other)) {
			return false;
		}
		return Objects.equals(intricFileId, other.intricFileId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(intricFileId);
	}
}

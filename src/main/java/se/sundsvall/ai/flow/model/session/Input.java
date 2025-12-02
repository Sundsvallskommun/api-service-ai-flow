package se.sundsvall.ai.flow.model.session;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

/*
 * "Mapping" between locally uploaded files and Eneo file uploads
 */
public class Input {

	private final MultipartFile file;
	@JsonIgnore
	private UUID eneoFileId;

	public Input(final MultipartFile file) {
		this.file = file;
	}

	public MultipartFile getFile() {
		return file;
	}

	public boolean isUploadedToEneo() {
		return nonNull(eneoFileId);
	}

	// Current public API (kept)
	public UUID getEneoFileId() {
		return eneoFileId;
	}

	public void setEneoFileId(final UUID eneoFileId) {
		this.eneoFileId = eneoFileId;
	}

	public Input withEneoFileId(final UUID eneoFileId) {
		this.eneoFileId = eneoFileId;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof final Input other)) {
			return false;
		}
		return Objects.equals(eneoFileId, other.eneoFileId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(eneoFileId);
	}
}

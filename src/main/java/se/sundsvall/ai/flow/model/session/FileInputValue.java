package se.sundsvall.ai.flow.model.session;

import java.util.Arrays;
import java.util.Objects;

/** Domain file input value. */
public record FileInputValue(String name, byte[] content, String contentType)
	implements
	InputValue {

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		final FileInputValue that = (FileInputValue) o;
		return Objects.equals(name, that.name) && Objects.deepEquals(content, that.content) && Objects.equals(contentType, that.contentType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, Arrays.hashCode(content), contentType);
	}

	@Override
	public String toString() {
		return "FileInputValue{" +
			"name='" + name + '\'' +
			", content=" + Arrays.toString(content) +
			", contentType='" + contentType + '\'' +
			'}';
	}
}

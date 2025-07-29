package se.sundsvall.ai.flow.util;

public class EncryptionException extends RuntimeException {

	private static final long serialVersionUID = 1623955532137042015L;

	public EncryptionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public EncryptionException(final String message) {
		super(message);
	}
}

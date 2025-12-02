package se.sundsvall.ai.flow.integration.eneo.util;

import java.io.Serial;

public class EncryptionException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1623955532137042015L;

	public EncryptionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public EncryptionException(final String message) {
		super(message);
	}
}

package info.ata4.bspsrc.lib.exceptions;

/**
 * Thrown to indicate that a goldsrc bsp was selected for decompilation.
 */
public class GoldSrcFormatException extends BspException {

	public GoldSrcFormatException() {
	}

	public GoldSrcFormatException(String message) {
		super(message);
	}

	public GoldSrcFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public GoldSrcFormatException(Throwable cause) {
		super(cause);
	}

	public GoldSrcFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

package info.ata4.bspsrc.lib.exceptions;

/**
 * Thrown to indicate that a zip file was selected for decompilation.
 */
public class ZipFileBspException extends BspException {

	public ZipFileBspException() {
	}

	public ZipFileBspException(String message) {
		super(message);
	}

	public ZipFileBspException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZipFileBspException(Throwable cause) {
		super(cause);
	}

	public ZipFileBspException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

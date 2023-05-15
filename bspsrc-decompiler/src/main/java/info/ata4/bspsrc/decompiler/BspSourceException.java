package info.ata4.bspsrc.decompiler;

public class BspSourceException extends Exception {
	public BspSourceException() {
	}

	public BspSourceException(String message) {
		super(message);
	}

	public BspSourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public BspSourceException(Throwable cause) {
		super(cause);
	}

	public BspSourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

package info.ata4.bspsrc.lib.nmo;

public class NmoException extends Exception {

	public NmoException() {
	}

	public NmoException(String message) {
		super(message);
	}

	public NmoException(String message, Throwable cause) {
		super(message, cause);
	}

	public NmoException(Throwable cause) {
		super(cause);
	}

	public NmoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

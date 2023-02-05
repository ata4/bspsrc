package info.ata4.bspsrc.cli;

class BspSourceCliParseException extends Exception {
    public BspSourceCliParseException() {
    }

    public BspSourceCliParseException(String message) {
        super(message);
    }

    public BspSourceCliParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BspSourceCliParseException(Throwable cause) {
        super(cause);
    }

    public BspSourceCliParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

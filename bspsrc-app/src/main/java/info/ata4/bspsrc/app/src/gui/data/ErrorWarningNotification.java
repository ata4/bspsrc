package info.ata4.bspsrc.app.src.gui.data;

import static java.util.Objects.requireNonNull;

public record ErrorWarningNotification(
		Type type,
		String message,
		int taskIndex
) {
	public ErrorWarningNotification {
		requireNonNull(message);
	}

	public enum Type {
		ERROR,
		WARNING
	}
}

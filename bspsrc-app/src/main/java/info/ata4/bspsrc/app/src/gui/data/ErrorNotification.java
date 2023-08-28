package info.ata4.bspsrc.app.src.gui.data;

import static java.util.Objects.requireNonNull;

public record ErrorNotification(
		String message,
		int taskIndex
) {
	public ErrorNotification {
		requireNonNull(message);
	}
}

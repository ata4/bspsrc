package info.ata4.bspsrc.app.info.gui.data;

import static java.util.Objects.requireNonNull;

public record EmbeddedInfo(
		String name,
		long size
) {
	public EmbeddedInfo {
		requireNonNull(name);
	}
}

package info.ata4.bspsrc.app.info.gui.data;

import static java.util.Objects.requireNonNull;

public record LumpInfo(
		int id,
		String name,
		int size,
		int sizePercentage,
		int version
) {
	public LumpInfo {
		requireNonNull(name);
	}
}

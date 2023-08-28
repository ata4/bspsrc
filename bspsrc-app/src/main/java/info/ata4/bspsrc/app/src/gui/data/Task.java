package info.ata4.bspsrc.app.src.gui.data;

import java.nio.file.Path;

public record Task(
		State state,
		Path bspFile
) {
	public enum State {
		PENDING,
		RUNNING,
		FINISHED,
		FAILED
	}
}

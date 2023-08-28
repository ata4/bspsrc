package info.ata4.bspsrc.app.util;

import info.ata4.bspsrc.app.src.gui.data.ErrorWarningNotification;
import info.ata4.bspsrc.decompiler.BspSource;

import java.util.Set;
import java.util.stream.Collectors;

public class ErrorMessageUtil {

	public static String warningToMessage(BspSource.Warning warning) {
		return switch (warning) {
			case ExtractEmbedded -> "Failed to extract embedded files. See the decompilation log for more details.";
			case LoadNmo -> "Failed to load nmo file. See the decompilation log for more details.";
			case WriteNmos -> "Failed to write nmos file. See the decompilation log for more details.";
		};
	}

	public static String decompileExceptionToMessage(Throwable throwable) {
		// TODO: Refactor exception hierarchy to allow us to match here and provide customized error messages
		return "An unexpected exception occurred while decompiling. See the decompilation log for more details.";
	}

	public static Set<ErrorWarningNotification> signalToNotification(BspSource.Signal signal) {
		if (signal instanceof BspSource.Signal.TaskStarted) {
			return Set.of();
		} else if (signal instanceof BspSource.Signal.TaskFailed failed) {
			return Set.of(new ErrorWarningNotification(
					ErrorWarningNotification.Type.ERROR,
					decompileExceptionToMessage(failed.exception()),
					failed.index()
			));
		} else if (signal instanceof BspSource.Signal.TaskFinished finished) {
			return finished.warnings().stream()
					.map(warning -> new ErrorWarningNotification(
							ErrorWarningNotification.Type.WARNING,
							warningToMessage(warning),
							finished.index()
					))
					.collect(Collectors.toSet());
		}
		throw new RuntimeException("Not reachable");
	}
}

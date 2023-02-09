package info.ata4.bspsrc.common.util;

import java.nio.file.Path;
import java.util.Optional;

public class PathUtil {
	public static Optional<String> nameWithoutExtension(Path path) {
		Path fileName = path.getFileName();
		if (fileName == null)
			return Optional.empty();

		String fileNameString = fileName.toString();
		int dotIndex = fileNameString.lastIndexOf('.');
		return Optional.of(dotIndex == -1 ? fileNameString : fileNameString.substring(0, dotIndex));
	}

	public static Optional<String> extension(Path path) {
		Path fileName = path.getFileName();
		if (fileName == null)
			return Optional.empty();

		String fileNameString = fileName.toString();
		int dotIndex = fileNameString.lastIndexOf('.');
		return Optional.of(dotIndex == -1 ? fileNameString : fileNameString.substring(dotIndex + 1));
	}
}

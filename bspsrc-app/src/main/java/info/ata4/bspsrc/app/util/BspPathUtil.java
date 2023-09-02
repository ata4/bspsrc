package info.ata4.bspsrc.app.util;

import info.ata4.bspsrc.common.util.PathUtil;

import java.nio.file.Path;

public class BspPathUtil {

	public static Path defaultVmfPath(Path bspPath, Path vmfDirPath) {
		if (vmfDirPath == null)
			vmfDirPath = bspPath.getParent();

		String base = PathUtil.nameWithoutExtension(bspPath).orElse("");

		Path fileName = Path.of(base + "_d.vmf");
		return vmfDirPath == null ? fileName : vmfDirPath.resolve(fileName);
	}
}

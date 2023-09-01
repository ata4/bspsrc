package info.ata4.bspsrc.app.util;

import info.ata4.bspsrc.decompiler.BspSourceException;
import info.ata4.bspsrc.lib.exceptions.BspException;
import info.ata4.bspsrc.lib.exceptions.GoldSrcFormatException;
import info.ata4.bspsrc.lib.exceptions.ZipFileBspException;

public class ErrorMessageUtil {

	public static String decompileExceptionToMessage(Throwable throwable) {
		// this would be a great candidate for pattern matching in switch...
		if (throwable instanceof BspSourceException){
			return throwable.getMessage() + " See the decompilation log for more details.";
		}
		if (throwable instanceof GoldSrcFormatException) {
			return "The bsp is from a goldsrc-engine game."
					+ " BSPSource only supports game build on source-engine.";
		}
		if (throwable instanceof ZipFileBspException) {
			return "The selected file is a zip archive. Make sure to first extract any bsp "
					+ "file it might contain and then select these for decompilation.";
		}
		if (throwable instanceof BspException) {
			return "An error occured loading the bsp. This might be because the file is corrupted "
					+ "or not a valid bsp.";
		}

		return "An unexpected exception occurred while decompiling. See the decompilation log for more details.";
	}
}

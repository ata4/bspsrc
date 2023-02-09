package info.ata4.bspsrc.common.util;

public class StringUtil {
	public static long matches(String string, char c) {
		return string.chars()
				.filter(ch -> ch == c)
				.count();
	}
}

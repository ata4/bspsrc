package info.ata4.bspsrc.common.util;

public class StringUtil {
	public static long matches(String string, char c) {
		return string.chars()
				.filter(ch -> ch == c)
				.count();
	}

	public static boolean equalsIgnoreCase(String s0, String s1) {
		return s0 == s1 || (s0 != null && s0.equalsIgnoreCase(s1));
	}
}

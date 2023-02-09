package info.ata4.bspsrc.common.util;

import java.util.Map;

/**
 * Util helper class for methods that got introduced in jdk9+ but can't be
 * utilized because the project is compiled for jdk 8
 */
public class JavaUtil {

    /**
     * Same as {@link Map#getOrDefault(K, V)} but with relaxed type parameters
     */
    public static <K, V extends U, U> U mapGetOrDefault(Map<K, V> map, K key, U defaultValue) {
        V v;
        return (((v = map.get(key)) != null) || map.containsKey(key))
                ? v
                : defaultValue;
    }

    public static boolean equalsIgnoreCase(String s0, String s1) {
        return s0 == s1 || (s0 != null && s0.equalsIgnoreCase(s1));
    }
}

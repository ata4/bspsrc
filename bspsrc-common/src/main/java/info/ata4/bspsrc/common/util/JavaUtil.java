package info.ata4.bspsrc.common.util;

import java.util.*;
import java.util.stream.Stream;

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

    public static <T> Stream<T> streamOpt(Optional<T> optional) {
        return optional
                .map(Stream::of)
                .orElseGet(Stream::empty);
    }

    /**
     * @see List#copyOf(Collection)
     */
    public static <E> List<E> listCopyOf(Collection<? extends E> collection) {
        return Collections.unmodifiableList(new ArrayList<>(collection));
    }

    /**
     * @see Set#copyOf(Collection)
     */
    public static <E> Set<E> setCopyOf(Collection<? extends E> collection) {
        return Collections.unmodifiableSet(new HashSet<>(collection));
    }
}

package info.ata4.bspsrc.common.util;

import java.util.Iterator;
import java.util.Map;

/**
 * Util helper class for general Java related methods
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

    public static <A, B> Iterable<Map.Entry<A, B>> zip(Iterable<A> iterable0, Iterable<B> iterable1) {
        return () -> new Iterator<>() {
            private final Iterator<A> it0 = iterable0.iterator();
            private final Iterator<B> it1 = iterable1.iterator();

            @Override
            public boolean hasNext() {
                return it0.hasNext() && it1.hasNext();
            }

            @Override
            public Map.Entry<A, B> next() {
                return Map.entry(it0.next(), it1.next());
            }
        };
    }
}

package info.ata4.util;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Util helper class for methods that got introduced in jdk9+ but can't be
 * utilized because the project is compiled for jdk 8
 */
public class JavaUtil {
    public static <T> Stream<T> streamOpt(Optional<T> optional) {
        return optional
                .map(Stream::of)
                .orElseGet(Stream::empty);
    }
}

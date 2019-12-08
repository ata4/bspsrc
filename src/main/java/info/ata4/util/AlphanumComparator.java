package info.ata4.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comparator to sort strings by a more 'human logic'
 */
public class AlphanumComparator implements Comparator<String> {

    private static final Pattern CHUNK_PATTERN = Pattern.compile("\\D+|\\d+");
    private static final Pattern LEADING_ZERO_PATTERN = Pattern.compile("^0*");

    public static final Comparator<String> COMPARATOR = Comparator.nullsFirst(AlphanumComparator::compareInternal);

    @Override
    public int compare(String s1, String s2) {
        return COMPARATOR.compare(s1, s2);
    }

    private static int compareInternal(String s1, String s2) {
        ChunkIterator s1Iterator = new ChunkIterator(s1);
        ChunkIterator s2Iterator = new ChunkIterator(s2);

        while (s1Iterator.hasNext() && s2Iterator.hasNext()) {
            String s1Chunk = s1Iterator.next();
            String s2Chunk = s2Iterator.next();

            int result;
            if (Character.isDigit(s1Chunk.charAt(0)) && Character.isDigit(s2Chunk.charAt(0))) {
                result = compareNumber(s1Chunk, s2Chunk);
            } else {
                result = s1Chunk.compareTo(s2Chunk);
            }

            if (result != 0) {
                return result;
            }
        }

        return s1Iterator.hasNext() ? 1 : 0;
    }

    // Could be improved. Doesn't account for negative numbers for example
    private static int compareNumber(String s1, String s2) {
        Matcher m1 = LEADING_ZERO_PATTERN.matcher(s1);
        Matcher m2 = LEADING_ZERO_PATTERN.matcher(s2);

        String trimS1 = m1.replaceFirst("");
        String trimS2 = m2.replaceFirst("");

        int trimmedZeros1 = s1.length() - trimS1.length();
        int trimmedZeros2 = s2.length() - trimS2.length();

        int result = trimmedZeros2 - trimmedZeros1;
        if (result != 0) {
            return result;
        }

        return compareNumber(trimS1, trimS2, trimmedZeros1 + trimmedZeros2 > 0);
    }

    private static int compareNumber(String s1, String s2, boolean leadingZero) {
        if (!leadingZero) {
            int result = s1.length() - s2.length();
            if (result != 0) {
                return result;
            }
        }

        for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
            int result = Character.getNumericValue(s1.charAt(i)) - Character.getNumericValue(s2.charAt(i));
            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

    private static class ChunkIterator implements Iterator<String> {

        private final Matcher m;

        private String nextResult;

        public ChunkIterator(String s) {
            m = CHUNK_PATTERN.matcher(s);

            nextResult = newResult();
        }

        @Override
        public boolean hasNext() {
            return nextResult != null;
        }

        @Override
        public String next() {
            if (nextResult == null) {
                throw new NoSuchElementException();
            }

            String result = nextResult;
            nextResult = newResult();
            return result;
        }

        private String newResult() {
            return m.find() ? m.group() : null;
        }
    }
}

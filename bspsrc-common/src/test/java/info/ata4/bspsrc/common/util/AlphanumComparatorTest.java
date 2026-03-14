package info.ata4.bspsrc.common.util;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;

class AlphanumComparatorTest {

    private final AlphanumComparator comparator = new AlphanumComparator();

    // --- Basic Properties of a Comparator ---

    @Property
    void reflexivity(
            @ForAll("biasedAlphanum") String s
    ) {
        assertThat(comparator.compare(s, s)).isZero();
    }

    @Property
    void symmetry(
            @ForAll("biasedAlphanum") String s1,
            @ForAll("biasedAlphanum") String s2
    ) {
        int c1 = Integer.signum(comparator.compare(s1, s2));
        int c2 = Integer.signum(comparator.compare(s2, s1));
        assertThat(c1).isEqualTo(-c2);
    }

    @Property
    void transitivity(
            @ForAll("biasedAlphanum") String s1,
            @ForAll("biasedAlphanum") String s2,
            @ForAll("biasedAlphanum") String s3
    ) {
        int c12 = Integer.signum(comparator.compare(s1, s2));
        int c23 = Integer.signum(comparator.compare(s2, s3));

        if (c12 > 0 && c23 > 0) {
            assertThat(comparator.compare(s1, s3)).isPositive();
        } else if (c12 < 0 && c23 < 0) {
            assertThat(comparator.compare(s1, s3)).isNegative();
        } else if (c12 == 0 && c23 == 0) {
            assertThat(comparator.compare(s1, s3)).isZero();
        }
    }

    @Provide
    Arbitrary<String> biasedAlphanum() {
        return Arbitraries.frequencyOf(
                Tuple.of(3, Arbitraries.strings().ascii().numeric()),
                Tuple.of(1, Arbitraries.integers().between(0, 100).map(i -> "00" + i)),
                Tuple.of(2, Arbitraries.strings().all())
        );
    }

    // --- Natural Logic Specific Tests ---

    @Example
    void testNumericSequence() {
        // Traditional sort would do: "file1", "file10", "file2"
        // Human logic: "file1", "file2", "file10"
        assertThat(comparator.compare("file2", "file10")).isNegative();
        assertThat(comparator.compare("10", "2")).isPositive();
    }

    @Example
    void testLeadingZeros() {
        // more leading zeros come first for same numeric value
        assertThat(comparator.compare("001", "01")).isNegative();
        assertThat(comparator.compare("01", "1")).isNegative();
    }

    @Property
    void numericChunksAreComparedAsNumbers(
            @ForAll @IntRange(min = 0, max = 1000) int n1,
            @ForAll @IntRange(min = 0, max = 1000) int n2
    ) {
        String s1 = "item" + n1;
        String s2 = "item" + n2;

        int result = comparator.compare(s1, s2);
        assertThat(Integer.signum(result)).isEqualTo(Integer.signum(Integer.compare(n1, n2)));
    }
}
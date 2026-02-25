package info.ata4.bspsrc.lib.vector;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@Tag("vector")
@DisplayName("Vector2f Test")
class Vector2fTest extends VectorXfTest<Vector2f> {

    @Override
    protected int size() {
        return 2;
    }

    @Override
    protected Vector2f instantiate(float... elements) {
        verifySize(elements);
        return new Vector2f(elements[0], elements[1]);
    }

    @Override
    protected float[] getElements(Vector2f vec) {
        return new float[]{vec.x(), vec.y()};
    }

    @Nested
    class Constructors extends AbstractConstructors {

        @Nested
        @DisplayName("Constructor")
        class Constructor extends AbstractConstructorNormal {

            @Override
            protected Vector2f constructor(float[] elements) {
                verifySize(elements);
                return new Vector2f(elements[0], elements[1]);
            }
        }

        @Nested
        @DisplayName("Array constructor")
        class ConstructorArray extends AbstractConstructorArray {

            @Override
            protected Vector2f constructor(float[] elements) {
                return Vector2f.fromArray(elements);
            }
        }
    }

    @Nested
    class Properties extends AbstractProperties {

        @Nested
        class Length extends AbstractLength {

            @Override
            protected Stream<TestSample> testSamplesProvider() {
                return Stream.of(
                        new TestSample(new Vector2f(0, 0), 0, 0),
                        new TestSample(new Vector2f(1, 1), 1.4142f, 0.0001f),
                        new TestSample(new Vector2f(-1, -1), 1.4142f, 0.0001f),
                        new TestSample(new Vector2f(0.5f, 0.5f), 0.7071f, 0.0001f),
                        new TestSample(new Vector2f(2, 2), 2.8284f, 0.0001f),
                        new TestSample(new Vector2f(10, 0), 10, 0.0001f),
                        new TestSample(new Vector2f(0, 10), 10, 0.0001f),
                        new TestSample(new Vector2f(3.5f, 4.5f), 5.7008f, 0.0001f)
                );
            }
        }
    }

    @Nested
    class Operations extends AbstractOperations {

        @Nested
        class DotProduct extends AbstractDotProduct {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector2f(0, 0), new Vector2f(0, 0), 0, 0.0001f),
                        new TestSample(new Vector2f(0.5f, 0.5f), new Vector2f(0.5f, 0.5f), 0.5f, 0.0001f),
                        new TestSample(new Vector2f(-0.5f, -0.5f), new Vector2f(0.5f, 0.5f), -0.5f, 0.0001f),
                        new TestSample(new Vector2f(1, 1), new Vector2f(1, 1), 2, 0.0001f),
                        new TestSample(new Vector2f(5, 1), new Vector2f(5, 10), 35, 0.0001f)
                );
            }
        }

        @Nested
        class Normalize extends AbstractNormalize {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector2f(0, 0), new Vector2f(Float.NaN, Float.NaN), 0.0001f),
                        new TestSample(new Vector2f(1, 1), new Vector2f(0.7071f, 0.7071f), 0.0001f),
                        new TestSample(new Vector2f(-10, 10), new Vector2f(-0.7071f, 0.7071f), 0.0001f),
                        new TestSample(new Vector2f(5.5f, 0), new Vector2f(1, 0), 0.0001f),
                        new TestSample(new Vector2f(-5.5f, 0), new Vector2f(-1, 0), 0.0001f),
                        new TestSample(new Vector2f(0, 5.5f), new Vector2f(0, 1), 0.0001f),
                        new TestSample(new Vector2f(0, -5.5f), new Vector2f(0, -1), 0.0001f),
                        new TestSample(new Vector2f(-1, 2), new Vector2f(-0.4472f, 0.8944f), 0.0001f)
                );
            }
        }

        @Nested
        class Add extends AbstractAdd {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector2f(0, 0), new Vector2f(0, 0), new Vector2f(0, 0), 0.0001f),
                        new TestSample(new Vector2f(1, 1), new Vector2f(1, 1), new Vector2f(2, 2), 0.0001f),
                        new TestSample(new Vector2f(0.5f, 1.5f), new Vector2f(1.5f, 0.5f), new Vector2f(2, 2), 0.0001f),
                        new TestSample(new Vector2f(-0.5f, 1.5f), new Vector2f(1.5f, -0.5f), new Vector2f(1, 1),
                                0.0001f),
                        new TestSample(new Vector2f(0.5f, -1.5f), new Vector2f(-1.5f, 0.5f), new Vector2f(-1, -1),
                                0.0001f),
                        new TestSample(new Vector2f(-0.5f, -1.5f), new Vector2f(-1.5f, -0.5f), new Vector2f(-2, -2),
                                0.0001f)
                );
            }
        }

        @Nested
        class Subtract extends AbstractSubtract {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector2f(0, 0), new Vector2f(0, 0), new Vector2f(0, 0), 0.0001f),
                        new TestSample(new Vector2f(1, 1), new Vector2f(1, 1), new Vector2f(0, 0), 0.0001f),
                        new TestSample(new Vector2f(0.5f, 1.5f), new Vector2f(1.5f, 0.5f), new Vector2f(-1, 1),
                                0.0001f),
                        new TestSample(new Vector2f(-0.5f, 1.5f), new Vector2f(1.5f, -0.5f), new Vector2f(-2, 2),
                                0.0001f),
                        new TestSample(new Vector2f(0.5f, -1.5f), new Vector2f(-1.5f, 0.5f), new Vector2f(2, -2),
                                0.0001f),
                        new TestSample(new Vector2f(-0.5f, -1.5f), new Vector2f(-1.5f, -0.5f), new Vector2f(1, -1),
                                0.0001f)
                );
            }
        }

        @Nested
        class Scalar extends AbstractScalar {

            @Nested
            class Float extends AbstractFloat {

                @Override
                protected Stream<TestSample> testSampleProvider() {
                    return Stream.of(
                            new TestSample(new Vector2f(0, 0), 0, new Vector2f(0, 0), 0.0001f),
                            new TestSample(new Vector2f(0, 0), 1, new Vector2f(0, 0), 0.0001f),
                            new TestSample(new Vector2f(1, 1), 0, new Vector2f(0, 0), 0.0001f),
                            new TestSample(new Vector2f(1, 1), 1, new Vector2f(1, 1), 0.0001f),
                            new TestSample(new Vector2f(1, 2), 3.45f, new Vector2f(3.45f, 6.9f), 0.0001f),
                            new TestSample(new Vector2f(1, 2), -3.45f, new Vector2f(-3.45f, -6.9f), 0.0001f),
                            new TestSample(new Vector2f(0.5f, -0.75f), 3, new Vector2f(1.5f, -2.25f), 0.0001f)
                    );
                }
            }

            @Nested
            class Vector extends AbstractVector {

                @Override
                protected Stream<TestSample> testSampleProvider() {
                    return Stream.of(
                            new TestSample(new Vector2f(0, 0), new Vector2f(0, 0), new Vector2f(0, 0), 0.0001f),
                            new TestSample(new Vector2f(0, 0), new Vector2f(1, 1), new Vector2f(0, 0), 0.0001f),
                            new TestSample(new Vector2f(1, 1), new Vector2f(0, 0), new Vector2f(0, 0), 0.0001f),
                            new TestSample(new Vector2f(1, 1), new Vector2f(1, 1), new Vector2f(1, 1), 0.0001f),
                            new TestSample(new Vector2f(1, 2), new Vector2f(3.45f, 6.54f), new Vector2f(3.45f, 13.08f),
                                    0.0001f),
                            new TestSample(new Vector2f(1, -2), new Vector2f(-3.45f, 6.54f),
                                    new Vector2f(-3.45f, -13.08f), 0.0001f),
                            new TestSample(new Vector2f(0.5f, -0.75f), new Vector2f(4, 2), new Vector2f(2, -1.5f),
                                    0.0001f)
                    );
                }
            }
        }

        @Nested
        class Min extends AbstractMin {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector2f(0, 0), new Vector2f(0, 0), new Vector2f(0, 0), 0.0001f),
                        new TestSample(new Vector2f(1, 2), new Vector2f(2, 1), new Vector2f(1, 1), 0.0001f),
                        new TestSample(new Vector2f(1, -2), new Vector2f(-2, 1), new Vector2f(-2, -2), 0.0001f),
                        new TestSample(new Vector2f(0.5f, 1.4f), new Vector2f(0.4f, 1.5f), new Vector2f(0.4f, 1.4f),
                                0.0001f)
                );
            }
        }

        @Nested
        class Max extends AbstractMax {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector2f(0, 0), new Vector2f(0, 0), new Vector2f(0, 0), 0.0001f),
                        new TestSample(new Vector2f(1, 2), new Vector2f(2, 1), new Vector2f(2, 2), 0.0001f),
                        new TestSample(new Vector2f(1, -2), new Vector2f(-2, 1), new Vector2f(1, 1), 0.0001f),
                        new TestSample(new Vector2f(0.5f, 1.4f), new Vector2f(0.4f, 1.5f), new Vector2f(0.5f, 1.5f),
                                0.0001f)
                );
            }
        }

        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @Nested
        class Rotate {

            private Stream<Arguments> testSamplesProvider() {
                return Stream.of(
                        Arguments.of(new Vector2f(1, 2), 0, new Vector2f(1, 2), 0.0001f),
                        Arguments.of(new Vector2f(1, 2), 45, new Vector2f(-0.7071f, 2.1213f), 0.0001f),
                        Arguments.of(new Vector2f(1, 2), 90, new Vector2f(-2, 1), 0.0001f),
                        Arguments.of(new Vector2f(1, 2), 135, new Vector2f(-2.1213f, -0.7071f), 0.0001f),
                        Arguments.of(new Vector2f(1, 2), 180, new Vector2f(-1, -2), 0.0001f),
                        Arguments.of(new Vector2f(1, 2), 225, new Vector2f(0.7071f, -2.1213f), 0.0001f),
                        Arguments.of(new Vector2f(1, 2), 270, new Vector2f(2, -1), 0.0001f),
                        Arguments.of(new Vector2f(1, 2), 315, new Vector2f(2.1213f, 0.7071f), 0.0001f),
                        Arguments.of(new Vector2f(1, 2), 360, new Vector2f(1, 2), 0.0001f)
                );
            }

            @DisplayName("Test values")
            @ParameterizedTest(name = "[{index}] {0} rotated {1} degrees is {2} with epsilon {3}")
            @MethodSource("testSamplesProvider")
            void testValues(Vector2f vec, float rotation, Vector2f expectedVec, float epsilon) {
                assertArrayEquals(getElements(expectedVec), getElements(vec.rotate(rotation)), epsilon);
            }

            @DisplayName("Test immutability")
            @Test
            void testImmutability() {
                Vector2f vec = new Vector2f(1, 0);
                assertNotSame(vec.rotate(180), vec);
            }
        }
    }

    @Nested
    class IO extends AbstractIO {

        @Override
        protected Vector2f read(DataReader dataReader) throws IOException {
            return Vector2f.read(dataReader);
        }

        @Override
        protected void write(DataWriter dataWriter, Vector2f vec) throws IOException {
            Vector2f.write(dataWriter, vec);
        }
    }
}

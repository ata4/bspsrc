package info.ata4.bspsrc.lib.vector;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;

import java.io.IOException;
import java.util.stream.Stream;

@Tag("vector")
@DisplayName("Vector4f Test")
class Vector4fTest extends VectorXfTest<Vector4f> {

    @Override
    protected int size() {
        return 4;
    }

    @Override
    protected Vector4f instantiate(float[] elements) {
        verifySize(elements);
        return new Vector4f(elements[0], elements[1], elements[2], elements[3]);
    }

    @Override
    protected float[] getElements(Vector4f vec) {
        return new float[]{vec.x(), vec.y(), vec.z(), vec.w()};
    }

    @Nested
    class Constructors extends AbstractConstructors {

        @Nested
        @DisplayName("Constructor")
        class Constructor extends AbstractConstructorNormal {

            @Override
            protected Vector4f constructor(float[] elements) {
                verifySize(elements);
                return new Vector4f(elements[0], elements[1], elements[2], elements[3]);
            }
        }

        @Nested
        @DisplayName("Array constructor")
        class ConstructorArray extends AbstractConstructorArray {

            @Override
            protected Vector4f constructor(float[] elements) {
                return Vector4f.fromArray(elements);
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
                        new TestSample(new Vector4f(0, 0, 0, 0), 0, 0),
                        new TestSample(new Vector4f(1, 1, 1, 1), 2, 0.0001f),
                        new TestSample(new Vector4f(-1, -1, -1, -1), 2, 0.0001f),
                        new TestSample(new Vector4f(0.5f, 0.5f, 0.5f, 0.5f), 1, 0.0001f),
                        new TestSample(new Vector4f(2, 2, 2, 2), 4, 0.0001f),
                        new TestSample(new Vector4f(10, 0, 0, 0), 10, 0.0001f),
                        new TestSample(new Vector4f(0, 10, 0, 0), 10, 0.0001f),
                        new TestSample(new Vector4f(0, 0, 10, 0), 10, 0.0001f),
                        new TestSample(new Vector4f(3.5f, 4.5f, 5.5f, 6.5f), 10.2469f, 0.0001f)
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
                        new TestSample(new Vector4f(0, 0, 0, 0), new Vector4f(0, 0, 0, 0), 0, 0.0001f),
                        new TestSample(new Vector4f(0.5f, 0.5f, 0.5f, 0.5f), new Vector4f(0.5f, 0.5f, 0.5f, 0.5f),
                                1, 0.0001f),
                        new TestSample(new Vector4f(-0.5f, -0.5f, -0.5f, -0.5f), new Vector4f(0.5f, 0.5f, 0.5f, 0.5f),
                                -1, 0.0001f),
                        new TestSample(new Vector4f(1, 1, 1, 1), new Vector4f(1, 1, 1, 1), 4, 0.0001f),
                        new TestSample(new Vector4f(5, 1, 10, 20), new Vector4f(5, 10, 2, 1), 75, 0.0001f)
                );
            }
        }

        @Nested
        class Normalize extends AbstractNormalize {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector4f(0, 0, 0, 0),
                                new Vector4f(Float.NaN, Float.NaN, Float.NaN, Float.NaN), 0.0001f),
                        new TestSample(new Vector4f(1, 1, 1, 1), new Vector4f(0.5f, 0.5f, 0.5f, 0.5f), 0.0001f),
                        new TestSample(new Vector4f(-10, 10, -10, 10), new Vector4f(-0.5f, 0.5f, -0.5f, 0.5f), 0.0001f),
                        new TestSample(new Vector4f(5.5f, 0, 0, 0), new Vector4f(1, 0, 0, 0), 0.0001f),
                        new TestSample(new Vector4f(-5.5f, 0, 0, 0), new Vector4f(-1, 0, 0, 0), 0.0001f),
                        new TestSample(new Vector4f(0, 5.5f, 0, 0), new Vector4f(0, 1, 0, 0), 0.0001f),
                        new TestSample(new Vector4f(0, -5.5f, 0, 0), new Vector4f(0, -1, 0, 0), 0.0001f),
                        new TestSample(new Vector4f(0, 0, 5.5f, 0), new Vector4f(0, 0, 1, 0), 0.0001f),
                        new TestSample(new Vector4f(0, 0, -5.5f, 0), new Vector4f(0, 0, -1, 0), 0.0001f),
                        new TestSample(new Vector4f(0, 0, 0, 5.5f), new Vector4f(0, 0, 0, 1), 0.0001f),
                        new TestSample(new Vector4f(0, 0, 0, -5.5f), new Vector4f(0, 0, 0, -1), 0.0001f),
                        new TestSample(new Vector4f(-1, 2, -3, 4), new Vector4f(-0.1825f, 0.3651f, -0.5477f, 0.7302f),
                                0.0001f)
                );
            }
        }

        @Nested
        class Add extends AbstractAdd {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector4f(0, 0, 0, 0), new Vector4f(0, 0, 0, 0), new Vector4f(0, 0, 0, 0),
                                0.0001f),
                        new TestSample(new Vector4f(1, 1, 1, 1), new Vector4f(1, 1, 1, 1), new Vector4f(2, 2, 2, 2),
                                0.0001f),
                        new TestSample(new Vector4f(0.5f, 1.5f, 2.5f, 3.5f), new Vector4f(3.5f, 2.5f, 1.5f, 0.5f),
                                new Vector4f(4, 4, 4, 4), 0.0001f),
                        new TestSample(new Vector4f(-0.5f, 1.5f, -2.5f, 3.5f), new Vector4f(3.5f, -2.5f, 1.5f, -0.5f),
                                new Vector4f(3, -1, -1, 3), 0.0001f),
                        new TestSample(new Vector4f(-0.5f, -1.5f, -2.5f, -3.5f),
                                new Vector4f(-3.5f, -2.5f, -1.5f, -0.5f), new Vector4f(-4, -4, -4, -4), 0.0001f)
                );
            }
        }

        @Nested
        class Subtract extends AbstractSubtract {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector4f(0, 0, 0, 0), new Vector4f(0, 0, 0, 0), new Vector4f(0, 0, 0, 0),
                                0.0001f),
                        new TestSample(new Vector4f(1, 1, 1, 1), new Vector4f(1, 1, 1, 1), new Vector4f(0, 0, 0, 0),
                                0.0001f),
                        new TestSample(new Vector4f(0.5f, 1.5f, 2.5f, 3.5f), new Vector4f(3.5f, 2.5f, 1.5f, 0.5f),
                                new Vector4f(-3, -1, 1, 3), 0.0001f),
                        new TestSample(new Vector4f(-0.5f, 1.5f, -2.5f, 3.5f), new Vector4f(3.5f, -2.5f, 1.5f, -0.5f),
                                new Vector4f(-4, 4, -4, 4), 0.0001f),
                        new TestSample(new Vector4f(-0.5f, -1.5f, -2.5f, -3.5f),
                                new Vector4f(-3.5f, -2.5f, -1.5f, -0.5f), new Vector4f(3, 1, -1, -3), 0.0001f)
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
                            new TestSample(new Vector4f(0, 0, 0, 0), 0, new Vector4f(0, 0, 0, 0), 0.0001f),
                            new TestSample(new Vector4f(0, 0, 0, 0), 1, new Vector4f(0, 0, 0, 0), 0.0001f),
                            new TestSample(new Vector4f(1, 1, 1 ,1), 0, new Vector4f(0, 0, 0, 0), 0.0001f),
                            new TestSample(new Vector4f(1, 1, 1, 1), 1, new Vector4f(1, 1, 1, 1), 0.0001f),
                            new TestSample(new Vector4f(1, 2, 3, 4), 3.45f, new Vector4f(3.45f, 6.9f, 10.35f, 13.8f),
                                    0.0001f),
                            new TestSample(new Vector4f(1, 2, 3, 4), -3.45f,
                                    new Vector4f(-3.45f, -6.9f, -10.35f, -13.8f), 0.0001f),
                            new TestSample(new Vector4f(0.5f, -0.75f, 0.125f, 0.615f), 3,
                                    new Vector4f(1.5f, -2.25f, 0.375f, 1.845f), 0.0001f)
                    );
                }
            }

            @Nested
            class Vector extends AbstractVector {

                @Override
                protected Stream<TestSample> testSampleProvider() {
                    return Stream.of(
                            new TestSample(new Vector4f(0, 0, 0, 0), new Vector4f(0, 0, 0, 0), new Vector4f(0, 0, 0, 0),
                                    0.0001f),
                            new TestSample(new Vector4f(0, 0, 0, 0), new Vector4f(1, 1, 1, 1), new Vector4f(0, 0, 0, 0),
                                    0.0001f),
                            new TestSample(new Vector4f(1, 1, 1, 1), new Vector4f(0, 0, 0, 0), new Vector4f(0, 0, 0, 0),
                                    0.0001f),
                            new TestSample(new Vector4f(1, 1, 1, 1), new Vector4f(1, 1, 1, 1), new Vector4f(1, 1, 1, 1),
                                    0.0001f),
                            new TestSample(new Vector4f(1, 2, 3, 4), new Vector4f(3.45f, 6.54f, 0.2f, 3.67f),
                                    new Vector4f(3.45f, 13.08f, 0.6f, 14.68f), 0.0001f),
                            new TestSample(new Vector4f(1, -2, 3, -4), new Vector4f(-3.45f, 6.54f, -0.2f, 3.67f),
                                    new Vector4f(-3.45f, -13.08f, -0.6f, -14.68f), 0.0001f),
                            new TestSample(new Vector4f(0.5f, -0.75f, 0.2f, 0.34f), new Vector4f(4, 2, 6, 8),
                                    new Vector4f(2, -1.5f, 1.2f, 2.72f), 0.0001f)
                    );
                }
            }
        }

        @Nested
        class Min extends AbstractMin {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector4f(0, 0, 0, 0), new Vector4f(0, 0, 0, 0), new Vector4f(0, 0, 0, 0),
                                0.0001f),
                        new TestSample(new Vector4f(1, 2, 3, 4), new Vector4f(4, 3, 2, 1), new Vector4f(1, 2, 2, 1),
                                0.0001f),
                        new TestSample(new Vector4f(1, -2, 3, -4), new Vector4f(-4, 3, -2, 1),
                                new Vector4f(-4, -2, -2, -4), 0.0001f),
                        new TestSample(new Vector4f(0.5f, 1.4f, 0.6f, 0.3f), new Vector4f(0.4f, 1.5f, 0.5f, 0.7f),
                                new Vector4f(0.4f, 1.4f, 0.5f, 0.3f), 0.0001f)
                );
            }
        }

        @Nested
        class Max extends AbstractMax {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector4f(0, 0, 0, 0), new Vector4f(0, 0, 0, 0), new Vector4f(0, 0, 0, 0),
                                0.0001f),
                        new TestSample(new Vector4f(1, 2, 3, 4), new Vector4f(4, 3, 2, 1), new Vector4f(4, 3, 3, 4),
                                0.0001f),
                        new TestSample(new Vector4f(1, -2, 3, -4), new Vector4f(-4, 3, -2, 1),
                                new Vector4f(1, 3, 3, 1), 0.0001f),
                        new TestSample(new Vector4f(0.5f, 1.4f, 0.6f, 0.3f), new Vector4f(0.4f, 1.5f, 0.5f, 0.7f),
                                new Vector4f(0.5f, 1.5f, 0.6f, 0.7f), 0.0001f)
                );
            }
        }
    }

    @Nested
    class IO extends AbstractIO {

        @Override
        protected Vector4f read(DataReader dataReader) throws IOException {
            return Vector4f.read(dataReader);
        }

        @Override
        protected void write(DataWriter dataWriter, Vector4f vec) throws IOException {
            Vector4f.write(dataWriter, vec);
        }
    }
}

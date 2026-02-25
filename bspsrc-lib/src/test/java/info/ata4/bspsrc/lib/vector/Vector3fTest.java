package info.ata4.bspsrc.lib.vector;

import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("vector")
@DisplayName("Vector3f Test")
class Vector3fTest extends VectorXfTest<Vector3f> {

    @Override
    protected int size() {
        return 3;
    }

    @Override
    protected Vector3f instantiate(float[] elements) {
        verifySize(elements);
        return new Vector3f(elements[0], elements[1], elements[2]);
    }

    @Override
    protected float[] getElements(Vector3f vec) {
        return new float[]{vec.x(), vec.y(), vec.z()};
    }

    @Nested
    class Constructors extends AbstractConstructors {

        @Nested
        @DisplayName("Constructor")
        class Constructor extends AbstractConstructorNormal {

            @Override
            protected Vector3f constructor(float[] elements) {
                verifySize(elements);
                return new Vector3f(elements[0], elements[1], elements[2]);
            }
        }

        @Nested
        @DisplayName("Array constructor")
        class ConstructorArray extends AbstractConstructorArray {

            @Override
            protected Vector3f constructor(float[] elements) {
                return Vector3f.fromArray(elements);
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
                        new TestSample(new Vector3f(0, 0, 0), 0, 0),
                        new TestSample(new Vector3f(1, 1, 1), 1.7320f, 0.0001f),
                        new TestSample(new Vector3f(-1, -1, -1), 1.7320f, 0.0001f),
                        new TestSample(new Vector3f(0.5f, 0.5f, 0.5f), 0.8660f, 0.0001f),
                        new TestSample(new Vector3f(2, 2, 2), 3.4641f, 0.0001f),
                        new TestSample(new Vector3f(10, 0, 0), 10, 0.0001f),
                        new TestSample(new Vector3f(0, 10, 0), 10, 0.0001f),
                        new TestSample(new Vector3f(0, 0, 10), 10, 0.0001f),
                        new TestSample(new Vector3f(3.5f, 4.5f, 5.5f), 7.9214f, 0.0001f)
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
                        new TestSample(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0, 0.0001f),
                        new TestSample(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.5f, 0.5f, 0.5f), 0.75f, 0.0001f),
                        new TestSample(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(0.5f, 0.5f, 0.5f), -0.75f,
                                0.0001f),
                        new TestSample(new Vector3f(1, 1, 1), new Vector3f(1, 1, 1), 3, 0.0001f),
                        new TestSample(new Vector3f(5, 1, 10), new Vector3f(5, 10, 2), 55, 0.0001f)
                );
            }
        }

        @Nested
        class Normalize extends AbstractNormalize {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector3f(0, 0, 0), new Vector3f(Float.NaN, Float.NaN, Float.NaN), 0.0001f),
                        new TestSample(new Vector3f(1, 1, 1), new Vector3f(0.5773f, 0.5773f, 0.5773f), 0.0001f),
                        new TestSample(new Vector3f(-10, 10, -10), new Vector3f(-0.5773f, 0.5773f, -0.5773f), 0.0001f),
                        new TestSample(new Vector3f(5.5f, 0, 0), new Vector3f(1, 0, 0), 0.0001f),
                        new TestSample(new Vector3f(-5.5f, 0, 0), new Vector3f(-1, 0, 0), 0.0001f),
                        new TestSample(new Vector3f(0, 5.5f, 0), new Vector3f(0, 1, 0), 0.0001f),
                        new TestSample(new Vector3f(0, -5.5f, 0), new Vector3f(0, -1, 0), 0.0001f),
                        new TestSample(new Vector3f(0, 0, 5.5f), new Vector3f(0, 0, 1), 0.0001f),
                        new TestSample(new Vector3f(0, 0, -5.5f), new Vector3f(0, 0, -1), 0.0001f),
                        new TestSample(new Vector3f(-1, 2, -3), new Vector3f(-0.2672f, 0.5345f, -0.8017f), 0.0001f)
                );
            }
        }

        @Nested
        class Add extends AbstractAdd {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.0001f),
                        new TestSample(new Vector3f(1, 1, 1), new Vector3f(1, 1, 1), new Vector3f(2, 2, 2), 0.0001f),
                        new TestSample(new Vector3f(0.5f, 1.5f, 2.5f), new Vector3f(2.5f, 1.5f, 0.5f),
                                new Vector3f(3, 3, 3), 0.0001f),
                        new TestSample(new Vector3f(-0.5f, 1.5f, -2.5f), new Vector3f(2.5f, -1.5f, 0.5f),
                                new Vector3f(2, 0, -2), 0.0001f),
                        new TestSample(new Vector3f(-0.5f, -1.5f, -2.5f), new Vector3f(-2.5f, -1.5f, -0.5f),
                                new Vector3f(-3, -3, -3), 0.0001f)
                );
            }
        }

        @Nested
        class Subtract extends AbstractSubtract {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.0001f),
                        new TestSample(new Vector3f(1, 1, 1), new Vector3f(1, 1, 1), new Vector3f(0, 0, 0), 0.0001f),
                        new TestSample(new Vector3f(0.5f, 1.5f, 2.5f), new Vector3f(2.5f, 1.5f, 0.5f),
                                new Vector3f(-2, 0, 2), 0.0001f),
                        new TestSample(new Vector3f(-0.5f, 1.5f, -2.5f), new Vector3f(2.5f, -1.5f, 0.5f),
                                new Vector3f(-3, 3, -3), 0.0001f),
                        new TestSample(new Vector3f(-0.5f, -1.5f, -2.5f), new Vector3f(-2.5f, -1.5f, -0.5f),
                                new Vector3f(2, 0, -2), 0.0001f)
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
                            new TestSample(new Vector3f(0, 0, 0), 0, new Vector3f(0, 0, 0), 0.0001f),
                            new TestSample(new Vector3f(0, 0, 0), 1, new Vector3f(0, 0, 0), 0.0001f),
                            new TestSample(new Vector3f(1, 1, 1), 0, new Vector3f(0, 0, 0), 0.0001f),
                            new TestSample(new Vector3f(1, 1, 1), 1, new Vector3f(1, 1, 1), 0.0001f),
                            new TestSample(new Vector3f(1, 2, 3), 3.45f, new Vector3f(3.45f, 6.9f, 10.35f), 0.0001f),
                            new TestSample(new Vector3f(1, 2, 3), -3.45f, new Vector3f(-3.45f, -6.9f, -10.35f),
                                    0.0001f),
                            new TestSample(new Vector3f(0.5f, -0.75f, 0.125f), 3, new Vector3f(1.5f, -2.25f, 0.375f),
                                    0.0001f)
                    );
                }
            }

            @Nested
            class Vector extends AbstractVector {

                @Override
                protected Stream<TestSample> testSampleProvider() {
                    return Stream.of(
                            new TestSample(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0),
                                    0.0001f),
                            new TestSample(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), new Vector3f(0, 0, 0),
                                    0.0001f),
                            new TestSample(new Vector3f(1, 1, 1), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0),
                                    0.0001f),
                            new TestSample(new Vector3f(1, 1, 1), new Vector3f(1, 1, 1), new Vector3f(1, 1, 1),
                                    0.0001f),
                            new TestSample(new Vector3f(1, 2, 3), new Vector3f(3.45f, 6.54f, 0.2f),
                                    new Vector3f(3.45f, 13.08f, 0.6f), 0.0001f),
                            new TestSample(new Vector3f(1, -2, 3), new Vector3f(-3.45f, 6.54f, -0.2f),
                                    new Vector3f(-3.45f, -13.08f, -0.6f), 0.0001f),
                            new TestSample(new Vector3f(0.5f, -0.75f, 0.2f), new Vector3f(4, 2, 6),
                                    new Vector3f(2, -1.5f, 1.2f), 0.0001f)
                    );
                }
            }
        }

        @Nested
        class Min extends AbstractMin {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.0001f),
                        new TestSample(new Vector3f(1, 2, 3), new Vector3f(3, 2, 1), new Vector3f(1, 2, 1), 0.0001f),
                        new TestSample(new Vector3f(1, -2, 3), new Vector3f(-3, 2, -1), new Vector3f(-3, -2, -1),
                                0.0001f),
                        new TestSample(new Vector3f(0.5f, 1.4f, 0.6f), new Vector3f(0.4f, 1.5f, 0.5f),
                                new Vector3f(0.4f, 1.4f, 0.5f), 0.0001f)
                );
            }
        }

        @Nested
        class Max extends AbstractMax {

            @Override
            protected Stream<TestSample> testSampleProvider() {
                return Stream.of(
                        new TestSample(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.0001f),
                        new TestSample(new Vector3f(1, 2, 3), new Vector3f(3, 2, 1), new Vector3f(3, 2, 3), 0.0001f),
                        new TestSample(new Vector3f(1, -2, 3), new Vector3f(-3, 2, -1), new Vector3f(1, 2, 3),
                                0.0001f),
                        new TestSample(new Vector3f(0.5f, 1.4f, 0.6f), new Vector3f(0.4f, 1.5f, 0.5f),
                                new Vector3f(0.5f, 1.5f, 0.6f), 0.0001f)
                );
            }
        }

        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @Nested
        class Cross {

            private Stream<Arguments> testSamplesProvider() {
                return Stream.of(
                        Arguments.of(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), 0.0001f),
                        Arguments.of(new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1), 0.0001f),
                        Arguments.of(new Vector3f(1, 2, 3), new Vector3f(4, 5, 6), new Vector3f(-3, 6, -3), 0.0001f)
                );
            }

            @DisplayName("Test values")
            @ParameterizedTest(name = "[{index}] {0} rotated {1} is {2} with epsilon {3}")
            @MethodSource("testSamplesProvider")
            void testValues(Vector3f vec, Vector3f otherVec, Vector3f expectedCross, float epsilon) {
                assertArrayEquals(getElements(expectedCross), getElements(vec.cross(otherVec)), epsilon);
            }

            @DisplayName("Test immutability")
            @Test
            void testImmutability() {
                Vector3f vec = new Vector3f(1, 1, 1);
                assertNotSame(vec.cross(vec), vec);
            }

            @DisplayName("Fail on null argument")
            @Test
            void testFailOnNullArgument() {
                assertThrows(NullPointerException.class, () -> new Vector3f(0, 0, 0).cross(null));
            }
        }

        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @Nested
        class Rotate {

            private Stream<Arguments> testSamplesProvider() {
                return Stream.of(
                        Arguments.of(new Vector3f(1, 2, 3), new Vector3f(0, 0, 0), new Vector3f(1, 2, 3), 0.0001f),
                        Arguments.of(new Vector3f(1, 2, 3), new Vector3f(45, 0, 0), new Vector3f(1, -0.7071f, 3.5355f),
                                0.0001f),
                        Arguments.of(new Vector3f(1, 2, 3), new Vector3f(0, 45, 0), new Vector3f(2.8284f, 2, 1.4142f),
                                0.0001f),
                        Arguments.of(new Vector3f(1, 2, 3), new Vector3f(0, 0, 45), new Vector3f(-0.7071f, 2.1213f, 3),
                                0.0001f),
                        Arguments.of(new Vector3f(1, 2, 3), new Vector3f(45, 45, 45),
                                new Vector3f(2.7677f, 1.7677f, 1.7928f), 0.0001f)
                );
            }

            @DisplayName("Test values")
            @ParameterizedTest(name = "[{index}] {0} rotated {1} is {2} with epsilon {3}")
            @MethodSource("testSamplesProvider")
            void testValues(Vector3f vec, Vector3f angles, Vector3f expectedVec, float epsilon) {
                assertArrayEquals(getElements(expectedVec), getElements(vec.rotate(angles)), epsilon);
            }

            @DisplayName("Test immutability")
            @Test
            void testImmutability() {
                Vector3f vec = new Vector3f(1, 1, 1);
                assertNotSame(vec.rotate(new Vector3f(45, 45, 45)), vec);
            }
        }


        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @Nested
        class ProjectOnPlane {

            private Stream<Arguments> testSamplesProvider() {
                return Stream.of(
                        Arguments.of(new Vector3f(1, 2, 3), new Vector3f(1, 1, 10), new Vector3f(1, 0, 0),
                                new Vector3f(0, 1, 0), new Vector2f(0, 1), 0.0001f),
                        Arguments.of(new Vector3f(1, 2, 3), new Vector3f(1, 10, 1), new Vector3f(1, 0, 0),
                                new Vector3f(0, 0, 1), new Vector2f(0, 2), 0.0001f),
                        Arguments.of(new Vector3f(1, 2, 3), new Vector3f(10, 1, 1), new Vector3f(0, 1, 0),
                                new Vector3f(0, 0, 1), new Vector2f(1, 2), 0.0001f)
                );
            }

            @DisplayName("Test values")
            @ParameterizedTest(name = "[{index}] {0} projected on plane(origin:{1}, xAxis:{2}, yAxis:{3})" +
                    "is {4} with epsilon {5}")
            @MethodSource("testSamplesProvider")
            void testValues(Vector3f vec, Vector3f planeOrigin, Vector3f planeAxis1, Vector3f planeAxis2,
                    Vector2f expectedVec, float epsilon) {
                Vector2f projectedVec = vec.projectOnPlane(planeOrigin, planeAxis1, planeAxis2);
                assertArrayEquals(new float[]{expectedVec.x(), expectedVec.y()},
                        new float[]{projectedVec.x(), projectedVec.y()}, epsilon);
            }

            @DisplayName("Test immutability")
            @Test
            void testImmutability() {
                Vector3f vec = new Vector3f(1, 1, 1);
                assertNotSame(vec.rotate(new Vector3f(45, 45, 45)), vec);
            }

            @DisplayName("Fail on null argument")
            @Test
            void testFailOnNullArgument() {
                assertThrows(NullPointerException.class,
                        () -> Vector3f.NULL.projectOnPlane(null, Vector3f.NULL, Vector3f.NULL));
                assertThrows(NullPointerException.class,
                        () -> Vector3f.NULL.projectOnPlane(Vector3f.NULL, null, Vector3f.NULL));
                assertThrows(NullPointerException.class,
                        () -> Vector3f.NULL.projectOnPlane(Vector3f.NULL, Vector3f.NULL, null));
            }
        }
    }

    @Nested
    class IO extends AbstractIO {

        @Override
        protected Vector3f read(DataReader dataReader) throws IOException {
            return Vector3f.read(dataReader);
        }

        @Override
        protected void write(DataWriter dataWriter, Vector3f vec) throws IOException {
            Vector3f.write(dataWriter, vec);
        }
    }
}

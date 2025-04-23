package info.ata4.bspsrc.decompiler.util;

import info.ata4.bspsrc.lib.vector.Vector2f;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;

public class ConvexPolygon extends AbstractList<Vector2f> {

    public final Vector2f[] vertices;
    public final Line[] edges;

    public ConvexPolygon(List<Vector2f> list) {
        this(list.toArray(new Vector2f[0]));
    }

    public ConvexPolygon(Vector2f... vertices) {
        if (vertices.length < 3) {
            throw new IllegalArgumentException("Vertices array must have at least 3 vertices");
        }

        this.vertices = vertices.clone();
        this.edges = IntStream.range(0, vertices.length)
                .mapToObj(i -> new Line(vertices[i], vertices[(i + 1) % vertices.length]))
                .toArray(Line[]::new);

        assert verifyConvex() : "Provided vertices don't create convex polygon: " + Arrays.toString(vertices);
    }

    private boolean verifyConvex() {
        return Arrays.stream(edges)
                .flatMap(edge -> Arrays.stream(vertices)
                        .map(vertex -> edge.getDirectionVector().cross(vertex.sub(edge.start)))
                        .filter(cross -> cross != 0)
                        .map(cross -> cross > 0))
                .distinct()
                .limit(2)
                .count() <= 1;
    }

    /**
     * Calculates the intersecting convex polygon between this convex polygon and the specified one.
     *
     * @param polygon The other convex polygon
     * @return an {@code Optional<ConvexPolygon>} representing the intersection of the two
     *         polygons or an empty Optional if there is no intersection
     */
    public Optional<ConvexPolygon> getIntersectionPolygon(ConvexPolygon polygon) {
        Set<Vector2f> intersectingVertices = new HashSet<>();

        // Find all corners of w1 that are inside of w2
        intersectingVertices.addAll(this.stream()
                .filter(polygon::containsPosition)
                .collect(Collectors.toList()));

        // Find all corners of w2 that are inside of w1
        intersectingVertices.addAll(polygon.stream()
                .filter(this::containsPosition)
                .collect(Collectors.toList()));

        // Find all intersections of the 2 polygons
        intersectingVertices.addAll(this.getIntersectionVertices(polygon));

        if (intersectingVertices.size() < 3) {
            return Optional.empty();
        } else {
            // Order all vertices creating a valid convex polygon
            return Optional.of(fromUnorderedVertices(intersectingVertices));
        }
    }

    /**
     * Calculates the intersection points of the two convex polygons
     *
     * @param polygon The other convex polygon
     * @return A {@code Set<Vector2f>} of intersection points
     */
    public Set<Vector2f> getIntersectionVertices(ConvexPolygon polygon) {
        return Arrays.stream(edges)
                .flatMap(edge -> Arrays.stream(polygon.edges)
                        .flatMap(otherEdge -> edge.getIntersectionPoint(otherEdge).stream()))
                .collect(Collectors.toSet());
    }

    /**
     * Test if the specified position is inside this convex polygon.
     * <p>A position is also considered to be inside this polygon, if it's
     * directly on one of its edges
     *
     * @param position The specific position
     * @return true if the specified position is inside the convex polygon
     */
    public boolean containsPosition(Vector2f position) {
        return Arrays.stream(edges)
                .map(edge -> edge.getDirectionVector().cross(position.sub(edge.start)))
                .filter(cross -> cross != 0)
                .map(cross -> cross > 0)
                .distinct()
                .limit(2)
                .count() <= 1;
    }

    /**
     * Calculates the area of this convex polygon
     *
     * @return the area of this convex polygon
     */
    public float getArea() {
        return (float) Math.abs(Arrays.stream(edges)
                .mapToDouble(edge -> edge.start.cross(edge.end))
                .sum() / 2);
    }

    /**
     * Get the vertex at the specified index
     *
     * @param index The index of the vertex
     * @return the vertex at the specified index
     */
    @Override
    public Vector2f get(int index) {
        return vertices[index];
    }

    /**
     * Returns the amount of vertices this convex polygon has
     *
     * @return the amount of vertices
     */
    @Override
    public int size() {
        return vertices.length;
    }

    /**
     * Creates a convex polygon by sorting the provided vertices by their rotation around
     * the midpoint of all vertices
     *
     * @param vertices A collection of unordered vertices
     * @return A {@link ConvexPolygon} with the provided vertices
     */
    public static ConvexPolygon fromUnorderedVertices(Collection<Vector2f> vertices) {
        Vector2f midPoint = vertices.stream()
                .reduce((vertex1, vertex2) -> vertex1.add(vertex2).scalar(0.5f))
                .orElse(Vector2f.NULL);

        return new ConvexPolygon(vertices.stream()
                .sorted(Comparator.comparingDouble(vertex -> Math.atan2(vertex.x - midPoint.x, vertex.y - midPoint.y)))
                .toArray(Vector2f[]::new));
    }

    /**
     * An Edge represented by a starting point and end point
     */
    private record Line(
            Vector2f start,
            Vector2f end
    ) {
        private Line {
            requireNonNull(start);
            requireNonNull(end);
        }

        /**
         * @return the direction vector for this line
         */
        public Vector2f getDirectionVector() {
            return end.sub(start);
        }

        /**
         * Calculates the intersection point between the two lines.
         *
         * @param otherLine The other line for with a intersection should be calculated
         * @return an {@code Optional<Vector2f>} of the intersection point or an empty
         *         optional, if there is none
         *
         * @see <a href="https://stackoverflow.com/a/565282">https://stackoverflow.com/a/565282</a>
         */
        public Optional<Vector2f> getIntersectionPoint(Line otherLine) {
            Vector2f cmP = otherLine.start.sub(this.start);
            Vector2f r = this.getDirectionVector();
            Vector2f s = otherLine.getDirectionVector();

            float cmPxr = cmP.cross(r);
            float cmPxs = cmP.cross(s);
            float rxs = r.cross(s);

            if (rxs == 0f) {
                return Optional.empty(); // Lines are parallel.
            }

            float t = cmPxs / rxs;
            float u = cmPxr / rxs;

            if ((t >= 0f) && (t <= 1f) && (u >= 0f) && (u <= 1f)) {
                return Optional.of(start.add(r.scalar(t)));
            } else {
                return Optional.empty();
            }
        }
    }
}

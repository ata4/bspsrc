package info.ata4.bspsrc.decompiler.util;

import info.ata4.bspsrc.lib.vector.Vector3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class ConvexVolume<T> {
    
    private static final Logger L = LogManager.getLogger();
    
    private final List<Vector3d> positions;
    private final List<InternalFace<T>> faces;

    private ConvexVolume(
            List<Vector3d> positions,
            List<InternalFace<T>> faces
    ) {
        this.positions = List.copyOf(positions);
        this.faces = List.copyOf(faces);
    }

    public static <T> ConvexVolume<T> aabb(AABB aabb)
    {
        var min = aabb.getMin();
        var max = aabb.getMax();
        return aabb(min, max);
    }
    
    public static <T> ConvexVolume<T> aabb(
            Vector3d min,
            Vector3d max
    ) {
        var positions = List.of(
                new Vector3d(min.x(), min.y(), min.z()),
                new Vector3d(max.x(), min.y(), min.z()),
                new Vector3d(min.x(), max.y(), min.z()),
                new Vector3d(max.x(), max.y(), min.z()),
                new Vector3d(min.x(), min.y(), max.z()),
                new Vector3d(max.x(), min.y(), max.z()),
                new Vector3d(min.x(), max.y(), max.z()),
                new Vector3d(max.x(), max.y(), max.z())
        );
        var faces = List.<InternalFace<T>>of(
                new InternalFace<>(List.of(0, 2, 3, 1), null, true),
                new InternalFace<>(List.of(4, 5, 7, 6), null, true),
                new InternalFace<>(List.of(1, 3, 7, 5), null, true),
                new InternalFace<>(List.of(0, 4, 6, 2), null, true),
                new InternalFace<>(List.of(2, 6, 7, 3), null, true),
                new InternalFace<>(List.of(0, 1, 5, 4), null, true)
        );
        return new ConvexVolume<>(positions, faces);
    }

    public ConvexVolume<T> clip(Vector3d normal, double dist, double eps, boolean clipBack) {
        return clip(normal, dist, eps, clipBack, null);
    }

    public ConvexVolume<T> clip(Vector3d normal, double dist, double eps, boolean clipBack, T data) {
        double[] dists = new double[positions.size()];
        var sides = new int[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            var d = normal.dot(positions.get(i)) - dist;
            dists[i] = d;
            sides[i] = (d > eps ? 1 : (d < -eps ? -1 : 0)) * (clipBack ? -1 : 1);
        }

        var oldToNewMap = new int[positions.size()];
        Arrays.fill(oldToNewMap, -1);

        var newVertices = new ArrayList<Vector3d>();
        var capIndices = new ArrayList<Integer>();

        // Keep all valid "Inside" vertices
        for (int i = 0; i < positions.size(); i++) {
            if (sides[i] <= 0) { // Keep inside or on plane
                var newIndex = newVertices.size();
                oldToNewMap[i] = newIndex;
                newVertices.add(positions.get(i));

                if (sides[i] == 0) {
                    capIndices.add(newIndex);
                }
            }
        }
        var cutEdgeCache = new HashMap<String, Integer>();
        var newFaces = new ArrayList<InternalFace<T>>();

        for (var face : faces) {
            var faceIndices = face.vertexIndices;
            var nextFaceIndices = new ArrayList<Integer>();

            for (int i = 0; i < faceIndices.size(); i++) {
                int u = faceIndices.get(i);
                int v = faceIndices.get((i + 1) % faceIndices.size());

                double du = dists[u];
                double dv = dists[v];
                var su = sides[u];
                var sv = sides[v];

                // If u is inside, add it
                if (sides[u] <= 0) {
                    nextFaceIndices.add(oldToNewMap[u]);
                }

                // Check for intersection (crossing the plane)
                // One is strictly positive, the other is strictly negative (or vice versa)
                if ((su > 0 && sv < 0) || (su < 0 && sv > 0)) {
                    // Generate unique key for a given edge regardless of direction
                    var edgeKey = (u < v) ? u + ":" + v : v + ":" + u;
                    int intersectionIndex = cutEdgeCache.computeIfAbsent(
                            edgeKey, _ -> {
                                Vector3d pu = positions.get(u);
                                double t = du / (du - dv);
                                var intersection = pu.add(positions.get(v).sub(pu).scalar(t));

                                var index = newVertices.size();
                                newVertices.add(intersection);
                                capIndices.add(index);
                                return index;
                            }
                    );
                    nextFaceIndices.add(intersectionIndex);
                }
            }

            if (nextFaceIndices.size() >= 3) {
                newFaces.add(new InternalFace<>(nextFaceIndices, face.data, face.initial));
            }
        }

        if (capIndices.size() >= 3) {
            List<Integer> capFace = createCapFace(newVertices, capIndices, normal, clipBack);
            newFaces.add(new InternalFace<>(capFace, data, false));
        }

        return new ConvexVolume<>(newVertices, newFaces);
    }

    private List<Integer> createCapFace(
            List<Vector3d> positions,
            List<Integer> capIndices,
            Vector3d normal,
            boolean clipBack
    ) {
        var centroid = capIndices.stream()
                .map(positions::get)
                .reduce(Vector3d::add)
                .orElse(Vector3d.NULL)
                .scalar(1f / capIndices.size());

        var reference = Math.abs(normal.x()) > 0.9
                ? new Vector3d(0, 1, 0)  // avoid collinearity
                : new Vector3d(1, 0, 0);

        var u = normal.cross(reference).normalize();
        var v = normal.cross(u);

        return capIndices.stream()
                .sorted(Comparator.comparingDouble(i -> {
                    var r = positions.get(i).sub(centroid);
                    return Math.atan2(r.dot(v), r.dot(u)) * (clipBack ? -1 : 1);
                }))
                .toList();
    }

    public List<Vector3d> positions() {
        return positions;
    }

    public List<Face<T>> faces() {
        return faces.stream()
                .map(this::faceFromInternal)
                .toList();
    }

    public boolean isEmpty() {
        return positions.size() < 4;
    }


    private Face<T> faceFromInternal(InternalFace<T> tInternalFace) {
        return new Face<>(
                new Winding(tInternalFace.vertexIndices.stream()
                        .map(positions::get)
                        .toList()
                        .reversed()),
                tInternalFace.data
        );
    }

    private record InternalFace<T>(
            List<Integer> vertexIndices,
            T data,
            boolean initial
    ) {
        private InternalFace(List<Integer> vertexIndices, T data, boolean initial) {
            this.vertexIndices = List.copyOf(vertexIndices);
            this.data = data;
            this.initial = initial;
        }
    }
    public record Face<T>(
            Winding winding,
            T data
    ) {
        public Face(Winding winding, T data) {
            this.winding = requireNonNull(winding);
            this.data = data;
        }
    }
}
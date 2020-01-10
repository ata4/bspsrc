package info.ata4.bsplib.util;

import info.ata4.bsplib.struct.BspData;
import info.ata4.bsplib.struct.DBrush;
import info.ata4.bsplib.struct.DBrushSide;
import info.ata4.bsplib.struct.DOccluderPolyData;
import info.ata4.bsplib.vector.Vector2f;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bspsrc.util.AreaportalMapper;
import info.ata4.bspsrc.util.Winding;
import info.ata4.bspsrc.util.WindingFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VectorUtil {

	public static double matchingAreaPercentage(DOccluderPolyData occluderPolyData, DBrush brush, DBrushSide brushSide, BspData bsp) {
		if (occluderPolyData.planenum == brushSide.pnum) {
			return internalMatchingAreaPercentage(
					WindingFactory.fromOccluder(bsp, occluderPolyData).removeDegenerated(),
					WindingFactory.fromSide(bsp, brush, brushSide).removeDegenerated()
			);
		} else {
			return 0;
		}
	}

	public static double matchingAreaPercentage(AreaportalMapper.AreaportalHelper apHelper, DBrush brush, DBrushSide brushSide, BspData bsp) {
		Set<Integer> planeNums = apHelper.getPlaneIndices();

		if (planeNums.contains(brushSide.pnum)) {
			return internalMatchingAreaPercentage(
					apHelper.winding.removeDegenerated(),
					WindingFactory.fromSide(bsp, brush, brushSide).removeDegenerated()
			);
		} else {
			return 0;
		}
	}

	/**
	 * Returns the touching area of two Winding's in percent to w1 (0-1)
	 * <p><b>This assumes that the 2 windings already lie in the same plane!!!</b></p>
	 *
	 * @param w1 the areaportal this brush is compared to
	 * @param w2 a winding representing the brush side
	 * @return A probability in form of a double ranging from 0 to 1
	 */
	private static double internalMatchingAreaPercentage(Winding w1, Winding w2) {
		// In case the provided windings are invalid, return 0!
		if (w1.size() < 3 || w2.size() < 3 || w1.isHuge() || w2.isHuge())
			return 0;

		Vector3f[] plane = w1.buildPlane();
		Vector3f vec1 = plane[1].sub(plane[0]);
		Vector3f vec2 = plane[2].sub(plane[0]);
		Vector3f planeNormal = vec2.cross(vec1).normalize();

		Vector3f origin = w1.get(0);
		Vector3f axis1 = w1.get(1).sub(origin).normalize(); //Random vector orthogonal to planeNormal
		Vector3f axis2 = axis1.cross(planeNormal).normalize(); //Vector orthogonal to axis1 and planeNormal

		//Map 3d coordinates of windings to 2d (2d coordinates on the plane they lie on)
		List<Vector2f> w1Polygon = w1.stream()
				.map(vertex -> vertex.getAsPointOnPlane(origin, axis1, axis2))
				.collect(Collectors.toList());

		List<Vector2f> w2Polygon = w2.stream()
				.map(vertex -> vertex.getAsPointOnPlane(origin, axis1, axis2))
				.collect(Collectors.toList());

		Set<Vector2f> intersectingVertices = new HashSet<>();

		// Find all corners of w1 that are inside of w2
		intersectingVertices.addAll(w1Polygon.stream()
				.filter(vertex -> VectorUtil.isInsideConvexPolygon(vertex, w2Polygon))
				.collect(Collectors.toList()));

		// Find all corners of w2 that are inside of w1
		intersectingVertices.addAll(w2Polygon.stream()
				.filter(vertex -> VectorUtil.isInsideConvexPolygon(vertex, w1Polygon))
				.collect(Collectors.toList()));

		// Find all intersections of the 2 polygons
		intersectingVertices.addAll(VectorUtil.getPolygonIntersections(w1Polygon, w2Polygon));

		// Order all vertices creating a valid convex polygon
		List<Vector2f> intersectionPolygon = VectorUtil.orderVertices(intersectingVertices);

		double intersectionArea = VectorUtil.polygonArea(intersectionPolygon);
		double w1Area = VectorUtil.polygonArea(w1Polygon);

		// actually intersectionArea / w1Area should never be greater 1, but i don't know why I have written that, so im just gonna leave that here
		return intersectionArea / w1Area > 1 ? 0 : Math.abs(intersectionArea / w1Area);
	}

	//https://wrf.ecse.rpi.edu//Research/Short_Notes/pnpoly.html
	public static boolean isInsideConvexPolygon(Vector2f p, List<Vector2f> polygon) {
		return IntStream.range(0, polygon.size())
				.mapToObj(i -> new Vector2f[]{polygon.get(i), polygon.get((i + 1) % polygon.size())})
				.filter(edge -> (edge[0].y > p.y) != (edge[1].y > p.y) && (p.x < (edge[1].x - edge[0].x) * (p.y - edge[0].y) / (edge[1].y - edge[0].y) + edge[0].x))
				.limit(2)
				.count() % 2 == 1;
	}


	public static Set<Vector2f> getPolygonIntersections(List<Vector2f> polygon1, List<Vector2f> polygon2) {
		return IntStream.range(0, polygon1.size())
				.mapToObj(i -> new Vector2f[]{polygon1.get(i), polygon1.get((i + 1) % polygon1.size())})
				.flatMap(edge -> IntStream.range(0, polygon2.size())
						.mapToObj(i -> new Vector2f[]{polygon2.get(i), polygon2.get((i + 1) % polygon2.size())})
						.map(edge2 -> lineIntersection(edge[0], edge[1], edge2[0], edge2[1])))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	//https://stackoverflow.com/a/565282
	public static Optional<Vector2f> lineIntersection(Vector2f origin1, Vector2f end1, Vector2f origin2, Vector2f end2) {
		Vector2f cmP = origin2.sub(origin1);
		Vector2f r = end1.sub(origin1);
		Vector2f s = end2.sub(origin2);

		float CmPxr = cmP.x * r.y - cmP.y * r.x;
		float CmPxs = cmP.x * s.y - cmP.y * s.x;
		float rxs = r.x * s.y - r.y * s.x;

		if (rxs == 0f)
			return Optional.empty(); // Lines are parallel.

		float rxsr = 1f / rxs;
		float t = CmPxs * rxsr;
		float u = CmPxr * rxsr;

		if ((t >= 0f) && (t <= 1f) && (u >= 0f) && (u <= 1f))
			return Optional.of(origin1.add(end1.sub(origin1).scalar(t)));
		else
			return Optional.empty();
	}

	public static double polygonArea(List<Vector2f> polygon) {
		return IntStream.range(0, polygon.size())
				.mapToObj(i -> new Vector2f[]{polygon.get(i), polygon.get((i + 1) % polygon.size())})
				.mapToDouble(edge -> (edge[0].x + edge[1].x) * (edge[0].y - edge[1].y))
				.sum() / 2;
	}

	public static List<Vector2f> orderVertices(Collection<Vector2f> vertices) {
		Vector2f midPoint = vertices.stream()
				.reduce((vertex1, vertex2) -> vertex1.add(vertex2).scalar(0.5f))
				.orElse(new Vector2f(0, 0));

		return vertices.stream()
				.sorted(Comparator.comparingDouble(vertex -> Math.atan2(vertex.x - midPoint.x, vertex.y - midPoint.y)))
				.collect(Collectors.toList());
	}
}

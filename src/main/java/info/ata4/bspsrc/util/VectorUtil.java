package info.ata4.bspsrc.util;

import info.ata4.bsplib.struct.BspData;
import info.ata4.bsplib.struct.DBrush;
import info.ata4.bsplib.struct.DBrushSide;
import info.ata4.bsplib.struct.DOccluderPolyData;
import info.ata4.bsplib.vector.Vector3f;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class VectorUtil {

	public static double matchingAreaPercentage(DOccluderPolyData occluderPolyData, DBrush brush,
												DBrushSide brushSide, BspData bsp) {
		if (occluderPolyData.planenum == brushSide.pnum) {
			return internalMatchingAreaPercentage(
					WindingFactory.fromOccluder(bsp, occluderPolyData),
					WindingFactory.fromSide(bsp, brush, brushSide)
			);
		} else {
			return 0;
		}
	}

	public static double matchingAreaPercentage(AreaportalMapper.AreaportalHelper apHelper, DBrush brush,
												DBrushSide brushSide, BspData bsp) {
		Set<Integer> planeNums = apHelper.getPlaneIndices();

		if (planeNums.contains(brushSide.pnum)) {
			return internalMatchingAreaPercentage(
					apHelper.winding,
					WindingFactory.fromSide(bsp, brush, brushSide)
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
		w1 = w1.removeDegenerated();
		w2 = w2.removeDegenerated();

		// In case the provided windings are invalid, return 0!
		if (w1.size() < 3 || w2.size() < 3 || w1.isHuge() || w2.isHuge()) {
			return 0;
		}

		Vector3f origin = w1.get(0);
		Vector3f vec1 = w1.get(1).sub(origin);
		Vector3f vec2 = w1.get(2).sub(origin);
		Vector3f planeNormal = vec2.cross(vec1).normalize();

		Vector3f axis1 = w1.get(1).sub(origin).normalize(); //Random vector orthogonal to planeNormal
		Vector3f axis2 = axis1.cross(planeNormal).normalize(); //Vector orthogonal to axis1 and planeNormal

		//Map 3d coordinates of windings to 2d (2d coordinates on the plane they lie on)
		ConvexPolygon w1Polygon = w1.stream()
				.map(vertex -> vertex.getAsPointOnPlane(origin, axis1, axis2))
				.collect(Collectors.collectingAndThen(Collectors.toList(), ConvexPolygon::new));

		ConvexPolygon w2Polygon = w2.stream()
				.map(vertex -> vertex.getAsPointOnPlane(origin, axis1, axis2))
				.collect(Collectors.collectingAndThen(Collectors.toList(), ConvexPolygon::new));

		Optional<ConvexPolygon> intersectionPolygon = w1Polygon.getIntersectionPolygon(w2Polygon);
		if (intersectionPolygon.isPresent()) {
			float intersectionArea = intersectionPolygon.get().getArea();
			// error margin
			if (intersectionArea < 1) {
				intersectionArea = 0;
			}

			return Math.min(intersectionArea / w1Polygon.getArea(), 1);
		} else {
			return 0;
		}
	}
}

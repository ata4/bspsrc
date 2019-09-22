package info.ata4.bsplib.util;

import info.ata4.bsplib.vector.Vector2f;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VectorUtil {

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

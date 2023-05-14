package info.ata4.bspsrc.decompiler.modules.geom;

import info.ata4.bspsrc.decompiler.modules.ModuleRead;
import info.ata4.bspsrc.decompiler.util.Winding;
import info.ata4.bspsrc.decompiler.util.WindingFactory;
import info.ata4.bspsrc.lib.BspFileReader;
import info.ata4.bspsrc.lib.struct.DBrush;
import info.ata4.bspsrc.lib.struct.DBrushSide;
import info.ata4.bspsrc.lib.struct.DFace;
import info.ata4.bspsrc.lib.vector.Vector3f;
import info.ata4.log.LogUtils;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static info.ata4.bspsrc.common.util.JavaUtil.mapGetOrDefault;
import static java.util.Objects.requireNonNull;

public class BrushSideFaceMapper extends ModuleRead {

	private static final Logger L = LogUtils.getLogger();
	
	// epsilon for area comparison slop, in mu^2
	private static final float AREA_EPS = 1.0f;

	private final WindingFactory windingFactory;

	// This is modelled with the assumption that the relation between brushsides and original faces is always N to 1
	// So in other words any particular brushside can only ever have 0 or 1 original face
	public final Map<Integer, Integer> brushSideToOrigFace = new HashMap<>();
	public final Map<Integer, HashSet<Integer>> origFaceToBrushSide = new HashMap<>();

	public BrushSideFaceMapper(BspFileReader reader, WindingFactory windingFactory) {
		super(reader);

		this.windingFactory = requireNonNull(windingFactory);
	}

	public void load() {
		reader.loadOriginalFaces();
		reader.loadFaces();
		reader.loadBrushSides();

		identifyExactMatches();
		identifyMergedMatches();
	}

	/**
	 * Identify 1 to 1 matches between original faces and brushsides. A face and a brushside is
	 * considered to be identical if pnum, texinfo, (dispInfo) and windings match.
	 * <p>
	 * Currently, this also checks for dispInfo, even though in practice brushsides always seem
	 * to have dispInfo of 0
	 */
	private void identifyExactMatches() {

		// setup index for fast searching
		Map<FaceIndexKey, Set<Integer>> origFaceIndex = IntStream.range(0, bsp.origFaces.size())
				.boxed()
				.collect(Collectors.groupingBy(
						origFaceI -> FaceIndexKey.fromFace(bsp.origFaces.get(origFaceI)),
						Collectors.toCollection(HashSet::new)
				));

		// for every brushside find a matching original face
		for (DBrush brush : bsp.brushes) {
			for (int i = 0; i < brush.numside; i++) {
				int brushSideIndex = brush.fstside + i;
				DBrushSide brushSide = bsp.brushSides.get(brushSideIndex);
				Winding brushSideWinding = windingFactory.fromSide(bsp, brush, brushSide);

				Set<Integer> potentialFaces = origFaceIndex.getOrDefault(
						FaceIndexKey.fromBrushSide(brushSide),
						Collections.emptySet()
				);
				potentialFaces.stream()
						.filter(origFaceI -> windingFactory.fromFace(bsp, bsp.origFaces.get(origFaceI))
								.matches(brushSideWinding))
						.findAny()
						.ifPresent(origFaceI -> {
							potentialFaces.remove(origFaceI); // remove it so it's not considered twice
							brushSideToOrigFace.put(brushSideIndex, origFaceI);
							origFaceToBrushSide.computeIfAbsent(origFaceI, key -> new HashSet<>())
									.add(brushSideIndex);
						});
			}
		}

		Set<? extends DFace> remainingOrigFaces = origFaceIndex.values().stream()
				.flatMap(Collection::stream)
				.map(bsp.origFaces::get)
				.collect(Collectors.toSet());

		Map<Boolean, Long> partitionedRemainingOrigFacesCount = remainingOrigFaces.stream()
				.collect(Collectors.partitioningBy(dFace -> dFace.dispInfo >= 0, Collectors.counting()));
		L.info(String.format("%d (%.1f%%) exact brushside->origface matches",
				brushSideToOrigFace.size(), 100.0 * brushSideToOrigFace.size() / bsp.brushSides.size()));
		L.info(String.format("%d/%d (nondisp/disp) original faces left after exact brushside->origface matching",
				partitionedRemainingOrigFacesCount.get(false), partitionedRemainingOrigFacesCount.get(true)));
	}

	/**
	 * For some reason the matching process in {@link #identifyExactMatches()} doesn't identify
	 * all matches. Some brushsides are still left, even though visually they seem to belong to some
	 * original face.
	 * <p>
	 * From looking at the source code of vbsp, <a href="https://github.com/ValveSoftware/source-sdk-2013/blob/0d8dceea4310fde5706b3ce1c70609d72a38efdf/mp/src/utils/vbsp/faces.cpp#L1086">
	 * this piece of code</a> might partially explain this behaviour. From that, it seems like some
	 * faces are merged before being written to the bsp.
	 * <p>
	 * Nonetheless, this method seems to work good enough, even though it is probably bound to make
	 * some mistakes from time to time.
	 *
	 * @see <a href="https://github.com/ValveSoftware/source-sdk-2013/blob/0d8dceea4310fde5706b3ce1c70609d72a38efdf/mp/src/utils/vbsp/faces.cpp#L1086">
	 *     https://github.com/ValveSoftware/source-sdk-2013/blob/0d8dceea4310fde5706b3ce1c70609d72a38efdf/mp/src/utils/vbsp/faces.cpp#L1086</a>
	 */
	private void identifyMergedMatches() {

		// setup index for fast searching
		Map<FaceIndexKey, Set<Integer>> faceIndex = IntStream.range(0, bsp.faces.size())
				.boxed()
				.collect(Collectors.groupingBy(
						faceI -> FaceIndexKey.fromFace(bsp.faces.get(faceI)),
						Collectors.toCollection(HashSet::new)
				));

		int oldMappingCount = brushSideToOrigFace.size();

		for (DBrush brush : bsp.brushes) {
			for (int i = 0; i < brush.numside; i++) {
				int brushSideIndex = brush.fstside + i;

				if (brushSideToOrigFace.containsKey(brushSideIndex))
					continue;

				DBrushSide brushSide = bsp.brushSides.get(brushSideIndex);
				Winding brushSideWinding = windingFactory.fromSide(bsp, brush, brushSide);
				Vector3f normal = bsp.planes.get(brushSide.pnum).normal;

				Set<Integer> potentialFaces = faceIndex.getOrDefault(
						FaceIndexKey.fromBrushSide(brushSide),
						Collections.emptySet()
				);
				potentialFaces.stream()
						.map(bsp.faces::get)
						.filter(dFace -> dFace.origFace >= 0) // only use faces that have an original face
						.collect(Collectors.groupingBy(
								dFace -> dFace.origFace,
								Collectors.summingDouble(dFace ->  windingFactory.fromFace(bsp, dFace)
										.clipWinding(brushSideWinding, normal)
										.getArea())
						))
						.entrySet()
						.stream()
						.filter(entry -> entry.getValue() > AREA_EPS)
						.max(Map.Entry.comparingByKey())
						.map(Map.Entry::getKey)
						.ifPresent(origFaceI -> {
							brushSideToOrigFace.put(brushSideIndex, origFaceI);
							origFaceToBrushSide.computeIfAbsent(origFaceI, key -> new HashSet<>())
									.add(brushSideIndex);
						});
			}
		}

		int newMatchesCount = brushSideToOrigFace.size() - oldMappingCount;
		L.info(String.format("%d (%.1f%%) partial brushside->origface matches",
				newMatchesCount, 100.0 * newMatchesCount / bsp.brushSides.size()
		));
	}

	public Optional<Integer> getOrigFaceIndex(int brushSideI) {
		return Optional.ofNullable(brushSideToOrigFace.get(brushSideI));
	}

	public Set<Integer> getBrushSideIndices(int origFaceI) {
		return mapGetOrDefault(origFaceToBrushSide, origFaceI, Set.of());
	}

	private static class FaceIndexKey {
		public final int pnum;
		public final short texinfo;
		public final short dispInfo;

		private FaceIndexKey(int pnum, short texinfo, short dispInfo) {
			this.pnum = pnum;
			this.texinfo = texinfo;
			this.dispInfo = dispInfo;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			FaceIndexKey faceIndexKey = (FaceIndexKey) o;

			return pnum == faceIndexKey.pnum
					&& texinfo == faceIndexKey.texinfo
					&& dispInfo == faceIndexKey.dispInfo;

		}

		@Override
		public int hashCode() {
			int result = pnum;
			result = 31 * result + (int) texinfo;
			result = 31 * result + (int) dispInfo;
			return result;
		}

		public static FaceIndexKey fromFace(DFace face) {
			return new FaceIndexKey(face.pnum, face.texinfo, face.dispInfo);
		}
		public static FaceIndexKey fromBrushSide(DBrushSide side) {
			return new FaceIndexKey(side.pnum, side.texinfo, (short) (side.dispinfo - 1)); // -1 ?
		}
	}
}

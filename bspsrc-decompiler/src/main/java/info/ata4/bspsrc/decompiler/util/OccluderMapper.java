package info.ata4.bspsrc.decompiler.util;

import info.ata4.bspsrc.decompiler.BspSourceConfig;
import info.ata4.bspsrc.decompiler.VmfWriter;
import info.ata4.bspsrc.decompiler.modules.VmfMeta;
import info.ata4.bspsrc.decompiler.modules.geom.FaceSource;
import info.ata4.bspsrc.decompiler.modules.texture.ToolTexture;
import info.ata4.bspsrc.lib.struct.BspData;
import info.ata4.bspsrc.lib.struct.DOccluderData;
import info.ata4.bspsrc.lib.struct.DOccluderPolyData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static info.ata4.bspsrc.common.util.Collectors.mode;
import static info.ata4.bspsrc.decompiler.util.HungarianAlgorithm.hungarian;

/**
 * Class for mapping occluder entities to their original brushes
 */
public class OccluderMapper {

    private static final Logger L = LogManager.getLogger();

    /**
     * Creates and returns an occluder to brushes mapping
     * </p>
     * In contrast to the {@link AreaportalMapper}, this method will always manually map occluders to brushes.
     *
     * @return Occluder reallocation data
     */
    public static ReallocationData createReallocationData(
            BspData bsp,
            BspSourceConfig config,
            WindingFactory windingFactory
    ) {
        if (!config.writeOccluders)
            return new ReallocationData(Map.of(), Map.of());

        if (bsp.occluderDatas.isEmpty()) {
            L.info("No occluders to reallocate...");
            return new ReallocationData(Map.of(), Map.of());
        }

        L.info("Reallocating occluders...");
        return manualMapping(bsp, windingFactory);
    }

    /**
     * Maps all {@link DOccluderPolyData} to the likeliest brush which they originated from.
     * 
     * <p>This is done by calculating the amount of overlap between each occluder face with each brush side.
     * A mapping is then created by applying the Hungarian method to assign each occluder to a brush.
     */
    private static ReallocationData manualMapping(
            BspData bsp,
            WindingFactory windingFactory
    ) {
        // Occluder brushes are always non-world brushes.
        // We can't rely on texture data, and occluders don't have an equivalent flag like CONTENTS_AREAPORTAL
        var firstNonWorldIBrush = getFirstNonWorldBrushId(bsp);
        
        var occluderPolyIndices = bsp.occluderDatas.stream()
                .mapToInt(occluder -> occluder.polycount)
                .toArray();
        Arrays.parallelPrefix(occluderPolyIndices, Integer::sum);
        
        var brushSideIndices = bsp.brushes.stream()
                .skip(firstNonWorldIBrush)
                .mapToInt(brush -> brush.numside)
                .toArray();
        Arrays.parallelPrefix(brushSideIndices, Integer::sum);

        var scores = createScores(bsp, windingFactory, occluderPolyIndices, brushSideIndices, firstNonWorldIBrush);
        var mappingResult = hungarian((j, w) -> scores[j][w], scores.length, scores[0].length);
        var collectBrushes = collectBrushes(bsp, mappingResult, scores, occluderPolyIndices, brushSideIndices,
                firstNonWorldIBrush);
        
        var occluderToBrushes = new HashMap<Integer, Set<Integer>>();
        var brushToSideMapping = new HashMap<Integer, Map<Integer, Integer>>();

        collectBrushes.forEach((iBrush, sideMapping) -> {
            var dBrush = bsp.brushes.get(iBrush);
            
            var mostCommonIOccluder = sideMapping.values().stream()
                    .map(OccluderPoly::iOccluder)
                    .collect(mode())
                    .orElse(null);

            if (mostCommonIOccluder != null) {
                var newMapping = new HashMap<Integer, Integer>();
                for (int sideOfBrush = 0; sideOfBrush < dBrush.numside; sideOfBrush++) {
                    var occluderPoly = sideMapping.get(sideOfBrush);
                    if (occluderPoly == null)
                        continue;
                    if (occluderPoly.iOccluder() != mostCommonIOccluder) {
                        L.warn(
                                "Couldn't reallocate side {} of occluder {}, because reallocated brush belongs "
                                        + "to different occluder {}",
                                occluderPoly.side(),
                                occluderPoly.iOccluder(),
                                mostCommonIOccluder
                        );
                        continue;
                    }

                    newMapping.put(sideOfBrush, occluderPoly.side());
                }

                occluderToBrushes.computeIfAbsent(mostCommonIOccluder, _ -> new HashSet<>()).add(iBrush);
                brushToSideMapping.put(iBrush, newMapping);
            }
        });

        return new ReallocationData(
                occluderToBrushes,
                brushToSideMapping
        );
    }

    /**
     * Collects all brushsides which got mapped to an occluder face.
     * @return A map in the form of {@code Map<Brush Id, Map<BrushSide Id, OccluderPoly>>}
     */
    private static HashMap<Integer, Map<Integer, OccluderPoly>> collectBrushes(
            BspData bsp,
            HungarianAlgorithm.Result mappingResult,
            double[][] scores,
            int[] occluderPolyIndices,
            int[] brushSideIndices,
            int firstNonWorldIBrush
    ) {
        var brushes = new HashMap<Integer, Map<Integer, OccluderPoly>>();
        for (int iOccluder = 0; iOccluder < bsp.occluderDatas.size(); iOccluder++) {
            var occluder = bsp.occluderDatas.get(iOccluder);
            for (int sideOfOccluder = 0; sideOfOccluder < occluder.polycount; sideOfOccluder++) {
                var offset = iOccluder > 0 ? occluderPolyIndices[iOccluder - 1] : 0;
                var index = offset + sideOfOccluder;
                
                var flattenedBrushSide = mappingResult.jobToWorker()[index];
                if (flattenedBrushSide < 0 || scores[index][flattenedBrushSide] == 0) {
                    L.warn("Couldn't reallocate side {} of occluder {}.", sideOfOccluder, iOccluder);
                    continue;
                }
                
                var apBrush = Math.abs(Arrays.binarySearch(brushSideIndices, flattenedBrushSide) + 1);
                var sideOfBrush = flattenedBrushSide - (apBrush > 0 ? brushSideIndices[apBrush - 1] : 0);
                var iBrush = firstNonWorldIBrush + apBrush;
                
                brushes
                        .computeIfAbsent(iBrush, _ -> new HashMap<>())
                        .put(sideOfBrush, new OccluderPoly(iOccluder, sideOfOccluder));
            }
        }
        return brushes;
    }

    /**
     * Create the score matrix for the hungarian method. The score for a occluderface/brushside combination is defined as 
     * the amount of overlap between their surfaces. They can be retrieved using scores[occluderface][brushside].
     */
    private static double[][] createScores(
            BspData bsp,
            WindingFactory windingFactory,
            int[] occluderPolyIndices,
            int[] brushSideIndices,
            int firstNonWorldIBrush
    ) {
        int occluderSidesCount = occluderPolyIndices.length > 0 ? occluderPolyIndices[occluderPolyIndices.length - 1] : 0;
        int brushSideCount = brushSideIndices.length > 0 ? brushSideIndices[brushSideIndices.length - 1] : 0;

        var scores = new double[occluderSidesCount][brushSideCount];
        for (int iOccluder = 0; iOccluder < bsp.occluderDatas.size(); iOccluder++) {
            var occluder = bsp.occluderDatas.get(iOccluder);
            for (int sideOfOccluder = 0; sideOfOccluder < occluder.polycount; sideOfOccluder++) {
                var occluderPolyData = bsp.occluderPolyDatas.get(occluder.firstpoly + sideOfOccluder);
                
                for (int iBrush = firstNonWorldIBrush; iBrush < bsp.brushes.size(); iBrush++) {
                    var brush = bsp.brushes.get(iBrush);
                    for (int sideOfBrush = 0; sideOfBrush < brush.numside; sideOfBrush++) {
                        var brushSide = bsp.brushSides.get(brush.fstside + sideOfBrush);

                        int brushIndex = iBrush - firstNonWorldIBrush;
                        int flattenedOccluderPoly = (iOccluder > 0 ? occluderPolyIndices[iOccluder - 1] : 0) + sideOfOccluder;
                        int flattenedBrushSide = (brushIndex > 0 ? brushSideIndices[brushIndex - 1] : 0) + sideOfBrush;
                        double score = VectorUtil.matchingAreaPercentage(
                                occluderPolyData,
                                brush,
                                brushSide,
                                bsp,
                                windingFactory
                        );
                        if (!Double.isFinite(score)) {
                            assert false: "VectorUtil.matchingAreaPercentage returned NaN";
                            score = 0;
                        }
                        scores[flattenedOccluderPoly][flattenedBrushSide] = score;
                    }
                }
            }
        }
        return scores;
    }
    
    private static int getFirstNonWorldBrushId(BspData bsp) {
        var tree = new BspTreeStats(bsp);
        tree.walk(0);
        return tree.getMaxBrushLeaf() + 1;
    }
    
    private record OccluderPoly(
            int iOccluder,
            int side
    ) {}

    /**
     * @param occluderToBrushes A mapping where the keys represent an occluder ({@link DOccluderData}) by its id
     * and the values the set of assigned brushes by their ids.
     * @param brushToSideMapping A mapping where the keys reperesent a brush 
     * ({@link info.ata4.bspsrc.lib.struct.DBrush}) by its id and the values a mapping of each side of the brush
     * to a side of the occluder.
     */
    public record ReallocationData(
            Map<Integer, Set<Integer>> occluderToBrushes,
            Map<Integer, Map<Integer, Integer>> brushToSideMapping
    ) {
        public ReallocationData {
            occluderToBrushes = Map.copyOf(occluderToBrushes);
            brushToSideMapping = Map.copyOf(brushToSideMapping);
        }

        public boolean isOccluderBrush(int iBrush) {
            return brushToSideMapping.containsKey(iBrush);
        }

        public boolean isOccluderBrushSide(int iBrush, int side) {
            return brushToSideMapping.getOrDefault(iBrush, Map.of()).containsKey(side);
        }
    }

    /**
     * Writes debug entities, that represent the original occluders from the bsp
     */
    public static void writeDebugOccluders(
            BspData bsp,
            FaceSource faceSource,
            WindingFactory windingFactory,
            VmfWriter writer,
            VmfMeta vmfMeta
    ) {

        for (int iOccluder = 0; iOccluder < bsp.occluderDatas.size(); iOccluder++) {
            var occluder = bsp.occluderDatas.get(iOccluder);
            for (int sideOfOccluder = 0; sideOfOccluder < occluder.polycount; sideOfOccluder++) {
                var occluderPoly = bsp.occluderPolyDatas.get(occluder.firstpoly + sideOfOccluder);

                writer.start("entity");
                writer.put("id", vmfMeta.getUID());
                writer.put("classname", "func_detail");
                writer.put("iOccluder", iOccluder);
                writer.put("side", sideOfOccluder);
                writer.put("flags", occluder.flags);
                writer.put("iOccluderPoly", occluder.firstpoly + sideOfOccluder);
                writer.put("planenum", occluderPoly.planenum);

                faceSource.writePolygon(windingFactory.fromOccluder(bsp, occluderPoly), ToolTexture.SKIP, false);
                vmfMeta.writeMetaVisgroups(
                        List.of(vmfMeta.visgroups()
                                .getVisgroup("debug")
                                .getVisgroup("occluder-visualization")
                                .getVisgroup(String.valueOf(iOccluder)))
                );

                writer.end("entity");
            }
        }
    }
}

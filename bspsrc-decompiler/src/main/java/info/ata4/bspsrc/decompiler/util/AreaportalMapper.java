package info.ata4.bspsrc.decompiler.util;

import info.ata4.bspsrc.decompiler.BspSourceConfig;
import info.ata4.bspsrc.decompiler.VmfWriter;
import info.ata4.bspsrc.decompiler.modules.VmfMeta;
import info.ata4.bspsrc.decompiler.modules.geom.FaceSource;
import info.ata4.bspsrc.decompiler.modules.texture.ToolTexture;
import info.ata4.bspsrc.lib.struct.BspData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static info.ata4.bspsrc.common.util.JavaUtil.zip;
import static info.ata4.bspsrc.decompiler.util.HungarianAlgorithm.hungarian;

/**
 * Class for mapping areaportal entities to their original brushes
 * Accounts for vBsp optimization
 */
public class AreaportalMapper {

    private static final Logger L = LogManager.getLogger();

    /**
     * Creates and returns an areaportal to brush mapping.
     * <p>
     * If the number of portals is equal to the number of areaportal brushes, we just map the areaportal in the 
     * order we encounter them to the brushes.
     * This is possible because vBsp seems to compile the areaportals in sequential order.
     * If this is not the case we use {@link AreaportalMapper#manualMapping(BspData, List, WindingFactory)} to 
     * manually map the areaportal brushes to areaportal entities.
     */
    public static ReallocationData createReallocationData(
            BspData bsp,
            BspSourceConfig config,
            WindingFactory windingFactory
    ) {
        if (!config.writeAreaportals)
            return new ReallocationData(Map.of());

        if (checkAreaportals(bsp))
            L.warn("Invalid areaportals, map was probably compiled with errors! Errors should be expected");

        var areaportalIBrushes = getApBrushes(bsp);
       var portalKeys = getPortalKeys(bsp);

        if (portalKeys.isEmpty()) {
            L.info("No areaportals to reallocate...");
            return new ReallocationData(Map.of());
        }

        ApMappingMode mappingMode;
        if (config.apForceManualMapping) {
            L.info("Forced manual areaportal mapping method");
            mappingMode = ApMappingMode.MANUAL;
        } else {
           if (portalKeys.size() == areaportalIBrushes.size()) {
                L.info("Equal amount of areaporal entities and areaportal brushes. Using '{}' method", ApMappingMode.ORDERED);
                mappingMode = ApMappingMode.ORDERED;
            } else {
                L.info("Unequal amount of areaporal entities and areaportal brushes. Falling back to '{}' method", ApMappingMode.MANUAL);
                mappingMode = ApMappingMode.MANUAL;
            }
        }

        return switch (mappingMode) {
           case MANUAL -> manualMapping(bsp, areaportalIBrushes, windingFactory);
           case ORDERED -> orderedMapping(portalKeys, areaportalIBrushes);
        };
    }

    /**
     * Checks if there are any invalid areaportal entities, meaning the map was compiled with errors
     * @return {@code true} if any areaportal entity is invalid, else {@code false}
     */
    private static boolean checkAreaportals(BspData bsp) {
        return bsp.areaportals.stream()
                .filter(dAreaportal -> dAreaportal.portalKey != 0)
                .anyMatch(dAreaportal -> dAreaportal.clipPortalVerts == 0);
    }

    /**
     * @return A list of brush ids that could be potential areaportals
     */
    private static List<Integer> getApBrushes(BspData bsp) {
        return IntStream.range(0, bsp.brushes.size())
                .filter(i -> bsp.brushes.get(i).isAreaportal())
                .boxed()
                .toList();
    }

    /**
     * @return A list of all portal ids
     */
    private static List<Integer> getPortalKeys(BspData bsp) {
        return bsp.areaportals.stream()
                .mapToInt(ap -> ap.portalKey)
                .filter(key -> key > 0)
                .distinct()
                .boxed()
                .toList();
    }

    /**
     * Maps portals to the likeliest brush which they originated from.
     *
     * <p>This is done by calculating the amount of overlap between each areaportal with each brush.
     * A mapping is then created by applying the Hungarian method to assign each areaportal to a brush.
     */
    private static ReallocationData manualMapping(
            BspData bsp,
            List<Integer> areaportalIBrushes,
            WindingFactory windingFactory
    ) {
        var areaportalsByPortalKey = IntStream.range(0, bsp.areaportals.size())
                .filter(iAreaportal -> bsp.areaportals.get(iAreaportal).portalKey != 0)
                .boxed()
                .collect(Collectors.groupingBy(iAreaportal -> bsp.areaportals.get(iAreaportal).portalKey))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey()) // not needed, just makes debugging easier
                .toList();

        var scores = createScores(bsp, areaportalIBrushes, areaportalsByPortalKey, windingFactory);
        var mappingResult = hungarian((j, w) -> scores[j][w], scores.length, scores[0].length);
        var assignments = processMappingResult(mappingResult, scores, areaportalIBrushes, areaportalsByPortalKey);

        var mapping = new HashMap<Integer, Integer>();
        for (int portal = 0; portal < assignments.length; portal++) {
            var portalKey = areaportalsByPortalKey.get(portal).getKey();
            var apBrush = assignments[portal];
            if (apBrush < 0 || scores[portal][apBrush] == 0) {
                L.warn("Could not reallocate brush for portalKey {}.", portalKey);
                continue;
            }

            mapping.put(portalKey, areaportalIBrushes.get(apBrush));
        }

        return new ReallocationData(mapping);
    }

    /**
     * Create the score matrix for the hungarian method. The score for a portal/brush combination is defined as 
     * the amount of overlap between a portal surface and one of the brushes sides. They can be retrieved using 
     * scores[portal][brush].
     */
    private static double[][] createScores(
            BspData bsp,
            List<Integer> areaportalIBrushes,
            List<Map.Entry<Integer, List<Integer>>> areaportalsByPortalKey,
            WindingFactory windingFactory
    ) {
        var scores = new double[areaportalsByPortalKey.size()][areaportalIBrushes.size()];
        for (int portal = 0; portal < areaportalsByPortalKey.size(); portal++) {
            for (int apBrush = 0; apBrush < areaportalIBrushes.size(); apBrush++) {
                var areaportalIBrush = areaportalIBrushes.get(apBrush);
                var brush = bsp.brushes.get(areaportalIBrush);

                var bestScore = 0.0;
                for (var iAreaportal : areaportalsByPortalKey.get(portal).getValue()) {
                    var areaportal = bsp.areaportals.get(iAreaportal);
                    
                    for (var brushSide : bsp.brushSides.subList(brush.fstside, brush.fstside + brush.numside)) {
                        double newScore = VectorUtil.matchingAreaPercentage(
                                areaportal,
                                brush,
                                brushSide,
                                bsp,
                                windingFactory
                        );
                        bestScore = Math.max(bestScore, newScore);
                    }
                }
                if (!Double.isFinite(bestScore)) {
                    assert false: "VectorUtil.matchingAreaPercentage returned NaN";
                    bestScore = 0;
                }
                scores[portal][apBrush] = bestScore;
            }
        }
        
        return scores;
    }

    /**
     * Processes the result of the Hungarian algorithm used for matching portals to brushes.
     * 
     * <p>Sometimes vbsp merges multiple areaportals into one. This makes it impossible for our manual method 
     * to figure out which brush belonged to which areaportal, resulting in mappings where some brushes have 
     * the exact same scores for multiple areaportal entities. This method finds these groups of areaportals
     * and reorders their mapping such that the areaportal/brush indices are ascending. Because vbsp happens to
     * process them in that order, it makes for a good heuristic for which areaportal belong to which brush.
     */
    private static int[] processMappingResult(
            HungarianAlgorithm.Result mappingResult,
            double[][] scores,
            List<Integer> areaportalIBrushes,
            List<Map.Entry<Integer, List<Integer>>> areaportalsByPortalKey
    ) {
        var assignments = mappingResult.jobToWorker().clone();
        var groups = new HashSet<Set<Integer>>();
        
        outer:
        for (int job = 0; job < assignments.length; job++) {
            var portal = job; // because java sucks
            if (assignments[portal] <= 0 || scores[portal][assignments[portal]] == 0)
                continue;
            
            for (var group : groups) {
                var canAddToGroup = group.stream()
                        .allMatch(p -> scores[p][assignments[p]] == scores[portal][assignments[p]] 
                                && scores[p][assignments[portal]] == scores[portal][assignments[portal]]);
                
                if (canAddToGroup) {
                    group.add(portal);
                    continue outer;
                }
            }
            
            groups.add(new HashSet<>(Set.of(portal)));
        }
        
        for (var group : groups) {
            if (group.size() <= 1)
                continue;
            
            var sortedPortals = group.stream()
                    .sorted(Comparator.comparing(job -> areaportalsByPortalKey.get(job).getKey()))
                    .toList();
            var sortedApBrushes = sortedPortals.stream()
                    .map(job -> assignments[job])
                    .sorted(Comparator.comparing(areaportalIBrushes::get))
                    .toList();

            for (var mapping : zip(sortedPortals, sortedApBrushes)) {
                assignments[mapping.getKey()] = mapping.getValue();
            }
        }

        return assignments;
    }

    /**
     * Creates a mapping between portals and areaportal brushes based on their order in the BSP file.
     *
     * <p>Since VBsp processes areaportals and their corresponding brushes in the same sequential order that they are
     * written into the BSP we can exploit this behavior to assign areaportals to brushes by their position. 
     * The areaportals and brushes are simply mapped in order, as long as the number of portal keys and areaportal 
     * brushes match.
     */
    private static ReallocationData orderedMapping(
            List<Integer> portalKeys,
            List<Integer> areaportalIBrushes
    ) {
        var sortedPortalKeys = portalKeys.stream()
                .sorted()
                .toList();
        
        int maxMappingCount = Math.min(sortedPortalKeys.size(), areaportalIBrushes.size());
        Map<Integer, Integer> areaportalMapping = IntStream.range(0, maxMappingCount)
                .boxed()
                .collect(Collectors.toMap(sortedPortalKeys::get, areaportalIBrushes::get));

        return new ReallocationData(areaportalMapping);
    }

    /**
     * @param mapping A mapping where the keys represent portal ids and values the brush ids
     */
    public record ReallocationData(
            Map<Integer, Integer> mapping
    ) {
        public ReallocationData {
            mapping = Map.copyOf(mapping);
        }
    }

    /**
     * Writes debug entities, that represent the original areaportals from the bsp
     */
    public static void writeDebugPortals(
            BspData bsp,
            FaceSource faceSource,
            WindingFactory windingFactory,
            VmfWriter writer,
            VmfMeta vmfMeta
    ) {
        for (int iAreaportal = 0; iAreaportal < bsp.areaportals.size(); iAreaportal++) {
            var areaportal = bsp.areaportals.get(iAreaportal);

            writer.start("entity");
            writer.put("id", vmfMeta.getUID());
            writer.put("classname", "func_detail");
            writer.put("iAreaportal", iAreaportal);
            writer.put("portalKey", areaportal.portalKey);
            writer.put("otherPortal", areaportal.otherportal);

            faceSource.writePolygon(windingFactory.fromAreaportal(bsp, areaportal), ToolTexture.SKIP, false);
            vmfMeta.writeMetaVisgroups(
                    List.of(vmfMeta.visgroups()
                            .getVisgroup("debug")
                            .getVisgroup("areaportal-visualization")
                            .getVisgroup(String.valueOf(areaportal.portalKey)))
            );

            writer.end("entity");
        }
    }

    public enum ApMappingMode {
        MANUAL,
        ORDERED;

        @Override
        public String toString() {
            return super.toString().substring(0, 1).toUpperCase() + super.toString().substring(1).toLowerCase();
        }
    }
}

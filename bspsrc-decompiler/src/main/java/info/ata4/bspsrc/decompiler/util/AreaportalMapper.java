package info.ata4.bspsrc.decompiler.util;

import info.ata4.bspsrc.decompiler.BspSourceConfig;
import info.ata4.bspsrc.decompiler.VmfWriter;
import info.ata4.bspsrc.decompiler.modules.VmfMeta;
import info.ata4.bspsrc.decompiler.modules.geom.FaceSource;
import info.ata4.bspsrc.decompiler.modules.texture.ToolTexture;
import info.ata4.bspsrc.lib.entity.KeyValue;
import info.ata4.bspsrc.lib.struct.BspData;
import info.ata4.bspsrc.lib.struct.DAreaportal;
import info.ata4.bspsrc.lib.struct.DBrush;
import info.ata4.log.LogUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Class for mapping areaportal entities to their original brushes
 * Accounts for vBsp optimization
 */
public class AreaportalMapper {

    private static final Logger L = LogUtils.getLogger();

    private BspSourceConfig config;
    private BspData bsp;

    private ArrayList<AreaportalHelper> areaportalHelpers = new ArrayList<>();
    private ArrayList<DBrush> areaportalBrushes = new ArrayList<>();

    public AreaportalMapper(BspData bsp, BspSourceConfig config) {
        this.bsp = bsp;
        this.config = config;

        if (checkAreaportal())
            L.warning("Invalid areaportals, map was probably compiled with errors! Errors should be expected");

        prepareApHelpers();
        prepareApBrushes();
    }


    /**
     * Checks if there are any invalid areaportal entities, meaning the map was compiled with errors
     * @return {@code true} if any areaportal entity is invalid, else {@code false}
     */
    private boolean checkAreaportal() {
        return bsp.areaportals.stream()
                .filter(dAreaportal -> dAreaportal.portalKey != 0)
                .anyMatch(dAreaportal -> dAreaportal.clipPortalVerts == 0);
    }

    /**
     * Group similar areaportals into {@code areaportalHelpers}, as they represent the same portal
     */
    private void prepareApHelpers() {

        for (DAreaportal dAreaportal : bsp.areaportals) {
            // Ignore first areaportal because it doesn't seem important...      And i don't have any idea what it should even represent
            if (dAreaportal.portalKey == 0)
                continue;

            // Do we already have a 'AreaportalHelper' that represents this areaportal?
            Optional<AreaportalHelper> matchingApHelper = areaportalHelpers.stream()
                    .filter(apHelper -> apHelper.winding.matches(WindingFactory.fromAreaportal(bsp, dAreaportal)))
                    .findAny();

            // If there is no AreaportalHelper that represents this portal, we create one
            // If there is already an AreaportalHelper representing this portal, we just add the portalID to it
            if (matchingApHelper.isPresent()) {
                matchingApHelper.get().addPortalId(dAreaportal.portalKey);
            } else {
                AreaportalHelper apHelper = new AreaportalHelper(WindingFactory.fromAreaportal(bsp, dAreaportal));
                apHelper.addPortalId(dAreaportal.portalKey);
                areaportalHelpers.add(apHelper);
            }
        }
    }

    /**
     * Fills {@code areaportalBrushes} with Objects that represent every areaportal brush
     */
    private void prepareApBrushes() {
        areaportalBrushes.addAll(bsp.brushes.stream()
                .filter(DBrush::isAreaportal)
                .collect(Collectors.toList()));
    }

    public boolean hasValidGeometry(int portalId) {
        return areaportalHelpers.stream()
                .filter(apHelper -> !apHelper.winding.isEmpty())
                .anyMatch(apHelper -> apHelper.portalID.contains(portalId));
    }

    /**
     * Maps areaportal brushes to the likeliest areaportal entity it represents
     *
     * <p>This is done by comparing the brush to all available areaportals and determining how much they 'overlap'. This
     * means if a brush doesn't have a brush side that's on the same plane as the areaportal, the probability is
     * automatically 0. If there is a brush side that is on the same plane, the probability is computed by
     * 'sharedSurfaceArea / areaportalSurfaceArea'
     *
     * @return A {@code Map} where the keys represent areaportal ids and the values brush ids
     */
    private Map<Integer, Integer> manualMapping() {
        Set<BrushProbabilitiesMapping> brushProbMappings = areaportalBrushes.stream()
                .map(dBrush -> new BrushProbabilitiesMapping(dBrush, areaportalBrushProb(dBrush)))
                .filter(mapping -> !mapping.isEmpty())
                .collect(Collectors.toSet());

        // Only used if debug enabled
        Map<Integer, KeyValue> debugEntityInformation = new HashMap<>();

        // Final brush mapping that will be returned at the end
        // Key represent areaportal ids, values represent index in bsp.brushes
        HashMap<Integer, Integer> finalBrushMapping = new HashMap<>();

        while (!brushProbMappings.isEmpty()) {
            // Get a brush that has only ONE mapping. In a perfect world, we can be sure that they are 100% matches
            Optional<BrushMapping> opBrushMapping = brushProbMappings.stream()
                    .map(BrushProbabilitiesMapping::getOnlyMapping)
                    .flatMap(Optional::stream)
                    .max(Comparator.comparingDouble(brushMapping -> brushMapping.probability));

            // If we dont have any 'distinct' mappings, we just get the one with the highest probability
            if (!opBrushMapping.isPresent()) {
                opBrushMapping = brushProbMappings.stream()
                        .flatMap(BrushProbabilitiesMapping::stream)
                        .max(Comparator.comparingDouble(mapping -> mapping.probability));
            }

            // At this part we should have a mapping...
            if (!opBrushMapping.isPresent()) {
                // How did we end up here?? Shouldn't be possible
                L.warning("Internal error occurred reallocating areaportals");
                assert false;
                break;
            }


            BrushMapping brushMapping = opBrushMapping.get();

            // Get an id that isn't already used
            OptionalInt opApId = brushMapping.apHelper.getFirstNonOverlappingId(finalBrushMapping.keySet());
            if (!opApId.isPresent()) {
                // Again how did we end up here? Shouldn't be possible
                L.warning("Internal error occurred reallocating areaportals");
                assert false;
                break;
            }

            int areaportalId = opApId.getAsInt();
            int brushIndex = bsp.brushes.indexOf(brushMapping.brush);

            finalBrushMapping.put(areaportalId, brushIndex);
            L.log(Level.FINEST, String.format("Mapped brush %d to areaportal %d[%s] with a probability of %.2g",
                    brushIndex,
                    areaportalId,
                    brushMapping.apHelper.portalID.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", ")),
                    brushMapping.probability));

            debugEntityInformation.put(areaportalId, new KeyValue(
                    brushMapping.apHelper.portalID.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", ", "areaportal_prob[", "]")),
                    String.valueOf(brushMapping.probability)
            ));

            // Remove the BrushProbabilitiesMapping of the brush that got the areaportal assigned
            brushProbMappings.removeIf(mapping -> mapping.brush == brushMapping.brush);

            // Remove every brush -> areaportalHelper mapping if the areaportalHelper doesn't contain any unused
            // areaportal ids anymore
            brushProbMappings.forEach(mapping ->
                    mapping.probabilities.keySet().removeIf(areaportalHelper ->
                            !areaportalHelper.getFirstNonOverlappingId(finalBrushMapping.keySet()).isPresent()));

            // Remove every BrushProbabilitiesMapping if it doesn't contain any brush -> areaportalHelper
            // mapping anymore
            brushProbMappings.removeIf(BrushProbabilitiesMapping::isEmpty);
        }

        //In debug mode we write all probabilities to the entities for debugging
        if (config.isDebug()) {
            bsp.entities.stream()
                    .filter(entity -> entity.getClassName().startsWith("func_areaportal"))
                    .forEach(entity -> {
                        try {
                            int portalId = Integer.parseInt(entity.getValue("portalnumber"));
                            entity.setValue(debugEntityInformation.getOrDefault(
                                    portalId,
                                    new KeyValue("areaportal_prob[]", "0")
                            ));
                        } catch (NumberFormatException e) {
                            L.log(Level.FINE, "func_areaportal portalnumber property is missing or invalid", e);
                        }
                    });
        }

        return finalBrushMapping;
    }

    /**
     * Helper class representing every possible areaportal mapping for a specific brush
     */
    private static final class BrushProbabilitiesMapping {
        public final DBrush brush;
        public final Map<AreaportalHelper, Double> probabilities;

        private BrushProbabilitiesMapping(DBrush brush, Map<AreaportalHelper, Double> probabilities) {
            this.brush = Objects.requireNonNull(brush);
            this.probabilities = Objects.requireNonNull(probabilities);
        }

        private boolean isEmpty() {
            return probabilities.isEmpty();
        }

        private Optional<BrushMapping> getOnlyMapping() {
            if (probabilities.size() > 1)
                return Optional.empty();

            return probabilities.entrySet().stream()
                    .findAny()
                    .map(this::brushMappingFromEntry);
        }

        public Stream<BrushMapping> stream() {
            return probabilities.entrySet().stream()
                    .map(this::brushMappingFromEntry);
        }

        private BrushMapping brushMappingFromEntry(Map.Entry<AreaportalHelper, Double> entry) {
            return new BrushMapping(brush, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Helper class representing a specific mapping between a brush and an areaportal
     */
    private static final class BrushMapping {
        public final DBrush brush;
        public final AreaportalHelper apHelper;
        public final double probability;

        public BrushMapping(DBrush brush, AreaportalHelper apHelper, double probability) {
            this.brush = Objects.requireNonNull(brush);
            this.apHelper = Objects.requireNonNull(apHelper);
            this.probability = probability;
        }
    }

    /**
     * Returns all possible areaportals this brush could represent, each with a percentage as their likelihood
     *
     * @param dBrush An areaportal brush
     * @return a {@code Map} with {@link AreaportalHelper}'s as keys and and a percantage (0 < p <= 1) as likelihood
     */
    private Map<AreaportalHelper, Double> areaportalBrushProb(DBrush dBrush) {
        return bsp.brushSides.subList(dBrush.fstside, dBrush.fstside + dBrush.numside).stream()
                .flatMap(brushSide -> areaportalHelpers.stream()
                        .map(apHelper -> new AbstractMap.SimpleEntry<>(
                                apHelper,
                                VectorUtil.matchingAreaPercentage(apHelper, dBrush, brushSide, bsp)
                        ))
                        .filter(entry -> entry.getValue() > 0) // TODO: should use some epsilon
                )
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, Math::max));
    }

    private Map<Integer, Integer> orderedMapping() {
        int areaportalBrushCount = areaportalBrushes.size();
        List<Integer> areaportalIds = areaportalHelpers.stream()
                .flatMap(areaportalHelper -> areaportalHelper.portalID.stream())
                .sorted()
                .collect(Collectors.toList());

        return IntStream.range(0, Math.min(areaportalIds.size(), areaportalBrushCount))
                .boxed()
                .collect(Collectors.toMap(areaportalIds::get, i -> bsp.brushes.indexOf(areaportalBrushes.get(i))));
    }


    /**
     * Creates and returns an areaportal to brush mapping.
     * <p></p>
     * If the amount of portals is equal to the amount of areaportal brushes we just map the areaportal in order to the brushes.
     * This is possible because vBsp seems to compile the areaportals in order.
     * If this is not the case we use {@code manualMapping} to manually map the areaportal brushes to areaportal entities
     *
     * @return A {@code Map} where the keys represent portal ids and values the brush ids
     */
    public Map<Integer, Integer> getApBrushMapping() {
        if (!config.writeAreaportals)
            return Collections.emptyMap();

        if (areaportalHelpers.size() == 0) {
            L.info("No areaportals to reallocate...");
            return Collections.emptyMap();
        }

        if (config.apForceManualMapping) {
            L.info("Forced manual areaportal mapping method");
            return ApMappingMode.MANUAL.map(this);
        }

        if (areaportalHelpers.stream().mapToInt(value -> value.portalID.size()).sum() == areaportalBrushes.size()) {
            L.info("Equal amount of areaporal entities and areaportal brushes. Using '" + ApMappingMode.ORDERED + "' method");
            return ApMappingMode.ORDERED.map(this);
        } else {
            L.info("Unequal amount of areaporal entities and areaportal brushes. Falling back to '" + ApMappingMode.MANUAL + "' method");
            return ApMappingMode.MANUAL.map(this);
        }
    }

    /**
     * Writes debug entities, that represent the original areaportals from the bsp
     */
    public void writeDebugPortals(VmfWriter writer, VmfMeta vmfMeta, FaceSource faceSource) {
        for (AreaportalHelper areaportalHelper : areaportalHelpers) {
            writer.start("entity");
            writer.put("id", vmfMeta.getUID());
            writer.put("classname", "func_detail");
            writer.put("areaportalIDs", areaportalHelper.portalID.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", ")));

            faceSource.writePolygon(areaportalHelper.winding, ToolTexture.SKIP, true);
            vmfMeta.writeMetaVisgroups(
                    areaportalHelper.portalID.stream()
                            .map(id -> vmfMeta.visgroups()
                                    .getVisgroup("AreaportalID")
                                    .getVisgroup(String.valueOf(id)))
                            .collect(Collectors.toList())
            );

            writer.end("entity");
        }
    }

    public enum ApMappingMode {
        MANUAL(AreaportalMapper::manualMapping),
        ORDERED(AreaportalMapper::orderedMapping);

        private Function<AreaportalMapper, Map<Integer, Integer>> mapper;

        ApMappingMode(Function<AreaportalMapper, Map<Integer, Integer>> mapper) {
            this.mapper = mapper;
        }

        public Map<Integer, Integer> map(AreaportalMapper apMapper) {
            return mapper.apply(apMapper);
        }

        @Override
        public String toString() {
            return super.toString().substring(0, 1).toUpperCase() + super.toString().substring(1).toLowerCase();
        }
    }

    /**
     * A little areaportal helper class for simplicity
     */
    public class AreaportalHelper {

        //All areaportal entities assigned to this helper. All areaportals are sorted by their portalID!
        private final TreeSet<Integer> portalID = new TreeSet<>();
        public final Winding winding;

        private final HashSet<Integer> planeIndices = new HashSet<>();

        public AreaportalHelper(Winding winding) {
            Objects.requireNonNull(winding);
            this.winding = winding;
        }

        public void addPortalId(int portalId) {
            Set<Integer> planeNums = bsp.areaportals.stream()
                    .filter(dAreaportal -> dAreaportal.portalKey == portalId)
                    .map(dAreaportal -> dAreaportal.planenum)
                    .collect(Collectors.toSet());

            if (planeNums.isEmpty()) {
                throw new IllegalArgumentException("Specified portalkey doesn't exist");
            } else {
                portalID.add(portalId);
                planeIndices.addAll(planeNums);
            }
        }

        public Set<Integer> getPlaneIndices() {
            //Should never be more than 2
            assert planeIndices.size() <= 2;
            return Collections.unmodifiableSet(planeIndices);
        }

        /**
         *
         * @param collection A Collection of integer ids
         * @return the first id of this areaportal helper, that is not in the specified collection
         */
        public OptionalInt getFirstNonOverlappingId(Collection<Integer> collection) {
            return portalID.stream()
                    .filter(id -> !collection.contains(id))
                    .mapToInt(i -> i)
                    .findFirst();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AreaportalHelper that = (AreaportalHelper) o;

            if (!portalID.equals(that.portalID)) return false;
            return Objects.equals(winding, that.winding);
        }

        @Override
        public int hashCode() {
            int result = portalID.hashCode();
            result = 31 * result + winding.hashCode();
            return result;
        }
    }
}

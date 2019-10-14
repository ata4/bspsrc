package info.ata4.bspsrc.util;

import info.ata4.bsplib.struct.BspData;
import info.ata4.bsplib.struct.DAreaportal;
import info.ata4.bsplib.struct.DBrush;
import info.ata4.bsplib.util.VectorUtil;
import info.ata4.bspsrc.BspSourceConfig;
import info.ata4.log.LogUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                matchingApHelper.get().portalID.add((int) dAreaportal.portalKey);
            } else {
                AreaportalHelper areaportalHelper = new AreaportalHelper();
                areaportalHelper.winding = WindingFactory.fromAreaportal(bsp, dAreaportal);
                areaportalHelper.portalID.add((int) dAreaportal.portalKey);
                areaportalHelpers.add(areaportalHelper);
            }
        }

        // Sort the list of areaportal helpers by their first portal id. This is not necessary but could help if the manual mapping makes mistakes
        areaportalHelpers.sort(Comparator.comparingInt(apHelper -> apHelper.portalID.first()));
    }

    /**
     * Fills {@code areaportalBrushes} with Objects that represent every areaportal brush
     */
    private void prepareApBrushes() {
        areaportalBrushes.addAll(bsp.brushes.stream()
                .filter(DBrush::isAreaportal)
                .collect(Collectors.toList()));
    }

    /**
     * Maps areaportal brushes to the likeliest areaportal entity it represents
     * <p></p>
     * This is done by comparing the brush to all available areaportals and determining how much they 'overlap'.
     * This means if a brush doesn't have a brush side that's on the same plane as the areaportal, the probability is automatically 0.
     * If there is a brush side that is on the same plane, the probability is computed by 'sharedSurfaceArea / areaportalSurfaceArea'
     *
     * @return A {@code Map} where the keys represent areaportal ids and the values brush ids
     */
    private Map<Integer, Integer> manualMapping() {
        Map<DBrush, Map<AreaportalHelper, Double>> brushProbMapping = areaportalBrushes.stream()
                .collect(Collectors.toMap(dBrush -> dBrush, this::areaportalBrushProb));

        // Remove every brush entry if it doesn't have areaportals mapped to it. (Not sure if this could actually happen here)
        brushProbMapping.entrySet().removeIf(dBrushMapEntry -> dBrushMapEntry.getValue().isEmpty());

        // Final brush mapping. Key represent areaportal ids, values represent index in bsp.brushes
        HashMap<Integer, Integer> brushMapping = new HashMap<>();

        Map<DBrush, Map<AreaportalHelper, Double>> mappingQueue = new HashMap<>();
        Comparator<Entry<DBrush, Map<AreaportalHelper, Double>>> mappingQueueComparator = Comparator.comparingDouble(entry -> entry.getValue().entrySet().stream()
                .max(Comparator.comparingDouble(Entry::getValue))
                .map(Entry::getValue)
                .get());

        while (!brushProbMapping.isEmpty()) {
            // Clear our mapping queue before every iteration
            mappingQueue.clear();

            // Get all brush mappings that only have one available areaportal to map to. These can be safely mapped as they don't create conflicts with other areaportal brushes (At least they shouldn't theoretically)
            mappingQueue.putAll(brushProbMapping.entrySet().stream()
                    .filter(dBrushMapEntry -> dBrushMapEntry.getValue().size() == 1)
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

            if (mappingQueue.isEmpty()) {
                // We ended up with only brushes that can't be mapped distinctively so we guess one to map by using the mapping with the highest probability
                brushProbMapping.entrySet().stream()
                        .max(mappingQueueComparator)
                        .ifPresent(entry -> mappingQueue.put(entry.getKey(), entry.getValue()));
            }

            // Now mapped those brushes we selected earlier
            mappingQueue.entrySet().stream()
                    .sorted(mappingQueueComparator)
                    .forEachOrdered(entry -> {
                        DBrush dBrush = entry.getKey();
                        AreaportalHelper apHelper = entry.getValue().entrySet().stream()
                                .max(Comparator.comparingDouble(Entry::getValue))
                                .map(Entry::getKey)
                                .get();

                        if (!apHelper.portalID.isEmpty()) {
                            int portalID = apHelper.portalID.first();

                            // When we map a brush to the specific areaportal id, we need to make sure no other brush is mapped to it as well. -> Iterate over every brush mapping and remove the areaportal id if present
                            brushProbMapping.entrySet().stream()
                                    .flatMap(dBrushMapEntry -> dBrushMapEntry.getValue().keySet().stream())
                                    .forEach(areaportalHelper -> areaportalHelper.portalID.removeIf(integer -> integer == portalID));

                            // This could cause Areaportalhelpers to be empty (of portal ids), so we remove every entry with those
                            brushProbMapping.forEach((key, value) -> value.entrySet().removeIf(apEntry -> apEntry.getKey().portalID.isEmpty()));

                            // Finally put our new mapping in the map
                            brushMapping.put(portalID, bsp.brushes.indexOf(dBrush));
                        } else {
                            L.warning("Couldn't find valid Areaportal mapping for brush " + bsp.brushes.indexOf(dBrush));
                        }

                        brushProbMapping.remove(dBrush);
                    });

            // After each iteration we remove every entry that doesn't have a possible entry anymore. (This could for example happen if the algorithm makes mistake)
            brushProbMapping.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }

        return brushMapping;
    }

    /**
     * Returns all possible areaportals this brush could represent, each with a percentage as their likelihood
     *
     * @param dBrush An areaportal brush
     * @return A {@code Map} with <b>copys</b> of {@link AreaportalHelper}'s as keys and and a percantage(0 < p <= 1) as likelihood
     */
    private Map<AreaportalHelper, Double> areaportalBrushProb(DBrush dBrush) {
        return bsp.brushSides.subList(dBrush.fstside, dBrush.fstside + dBrush.numside).stream()
                .flatMap(brushSide -> areaportalHelpers.stream()
                        .map(apHelper -> new AbstractMap.SimpleEntry<>(new AreaportalHelper(apHelper), VectorUtil.matchingAreaPercentage(apHelper.getFirstDAreaportal(), dBrush, brushSide, bsp)))
                        .filter(entry -> entry.getValue() != 0)
                )
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private Map<Integer, Integer> orderedMapping() {
        int areaportalIDCount = areaportalHelpers.stream()
                .mapToInt(apHelper -> apHelper.portalID.size())
                .sum();

        int areaportalBrushCount = areaportalBrushes.size();

        return IntStream.range(0, Math.min(areaportalIDCount, areaportalBrushCount))
                .boxed()
                .collect(Collectors.toMap(i -> i + 1, i -> bsp.brushes.indexOf(areaportalBrushes.get(i))));
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

        if (config.apForceMapping) {
            L.info("Forced areaportal method: '" + config.apMappingMode + "'");
            return config.apMappingMode.map(this);
        }

        if (areaportalHelpers.stream().mapToInt(value -> value.portalID.size()).sum() == bsp.brushes.stream().filter(DBrush::isAreaportal).count()) {
            L.info("Equal amount of areaporal entities and areaportal brushes. Using '" + ApMappingMode.ORDERED + "' method");
            return ApMappingMode.ORDERED.map(this);
        } else {
            L.info("Unequal amount of areaporal entities and areaportal brushes. Falling back to '" + ApMappingMode.MANUAL + "' method");
            return ApMappingMode.MANUAL.map(this);
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
    private class AreaportalHelper {

        //All areaportal entities assigned to this helper. All areaportals are sorted by their portalID!
        public final TreeSet<Integer> portalID = new TreeSet<>();
        public Winding winding;

        public AreaportalHelper() {}

        public AreaportalHelper(AreaportalHelper apHelper) {
            portalID.addAll(apHelper.portalID);
            winding = apHelper.winding;
        }

        public DAreaportal getFirstDAreaportal() {
            return bsp.areaportals.stream()
                    .filter(areaportal -> areaportal.portalKey == portalID.first())
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("Areaportalhelper points to non existing dAreaportal " + portalID.first()));
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
            result = 31 * result + (winding != null ? winding.hashCode() : 0);
            return result;
        }
    }
}

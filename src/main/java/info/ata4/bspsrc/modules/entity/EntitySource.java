/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.modules.entity;

import info.ata4.bsplib.BspFileReader;
import info.ata4.bsplib.app.SourceAppID;
import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.entity.EntityIO;
import info.ata4.bsplib.entity.KeyValue;
import info.ata4.bsplib.nmo.NmoFile;
import info.ata4.bsplib.struct.*;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bspsrc.BspSourceConfig;
import info.ata4.bspsrc.VmfWriter;
import info.ata4.bspsrc.modules.BspProtection;
import info.ata4.bspsrc.modules.ModuleDecompile;
import info.ata4.bspsrc.modules.VmfMeta;
import info.ata4.bspsrc.modules.geom.BrushMode;
import info.ata4.bspsrc.modules.geom.BrushSource;
import info.ata4.bspsrc.modules.geom.BrushUtils;
import info.ata4.bspsrc.modules.geom.FaceSource;
import info.ata4.bspsrc.modules.texture.TextureSource;
import info.ata4.bspsrc.util.*;
import info.ata4.log.LogUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Decompiling module to write point and brush entities converted from various lumps.
 * 
 * Based on several entity building methods from Vmex
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntitySource extends ModuleDecompile {

    // logger
    private static final Logger L = LogUtils.getLogger();

    private static final Pattern INSTANCE_PREFIX = Pattern.compile("^([^-]+)-");

    // sub-modules
    private final BspSourceConfig config;
    private final BrushSource brushsrc;
    private final FaceSource facesrc;
    private final TextureSource texsrc;
    private final BspProtection bspprot;
    private final VmfMeta vmfmeta;

    // Areaportal to brush mapping
    Map<Integer, Integer> apBrushMap;

    // Occluder to brushes mapping;
    Map<Integer, List<Integer>> occBrushesMap;

    //'No More Room in Hell' Nmo data
    private NmoFile nmo;

    // overlay target names
    private final Map<Integer, String> overlayNames = new HashMap<>();

    public EntitySource(BspFileReader reader, VmfWriter writer, BspSourceConfig config,
            BrushSource brushsrc, FaceSource facesrc, TextureSource texsrc,
            BspProtection bspprot, VmfMeta vmfmeta) {
        super(reader, writer);
        this.config = config;
        this.brushsrc = brushsrc;
        this.facesrc = facesrc;
        this.texsrc = texsrc;
        this.bspprot = bspprot;
        this.vmfmeta = vmfmeta;

        processEntities();

        AreaportalMapper areaportalMapper = new AreaportalMapper(bsp, config);
        apBrushMap = areaportalMapper.getApBrushMapping();

        OccluderMapper occluderMapper = new OccluderMapper(bsp, config);
        occBrushesMap = occluderMapper.getOccBrushMapping();
    }

    /**
     * Writes all brush and point entities with exception of some internal
     * entities, including
     * - cubemaps
     * - overlays
     * - static props
     * - detail brushes
     * Those are written by separate methods because of their different data models.
     *
     * @see writeCubeMaps
     * @see writeOverlays
     * @see writeStaticProps
     * @see writeDetails
     */
    public void writeEntities() {
        L.info("Writing entities");

        // instances are currently supported in compilers for BSP v21+ only
        boolean instances = bspFile.getVersion() >= 21;

        // fix rotated instance brushes?
        // this option is unnecessary for BSP files without instances, it will
        // only cause errors
        boolean fixRot = config.fixEntityRot && instances;

        List<String> visgroups = new ArrayList<>();

        for (Entity ent : bsp.entities) {
            visgroups.clear();

            final String className = ent.getClassName();

            // don't write the worldspawn here
            if (className.equals("worldspawn")) {
                continue;
            }

            // workaround for a Hammer crashing bug
            if (className.equals("env_sprite")) {
                String model = ent.getValue("model");
                if (model != null && model.startsWith("model")) {
                    ent.removeValue("scale");
                }
            }

            // these two classes need special attention
            final boolean isAreaportal = className.startsWith("func_areaportal");
            final boolean isOccluder = className.equals("func_occluder");

            // areaportals and occluders don't have a "model" key, take that
            // into account
            final boolean hasBrush = ent.getModelNum() > 0 || isAreaportal || isOccluder;

            // skip point entities?
            if (!config.writePointEntities && !hasBrush) {
                continue;
            }

            // skip brush entities?
            if (!config.writeBrushEntities && hasBrush) {
                continue;
            }

            // skip areaportals?
            if (!config.writeAreaportals && isAreaportal) {
                continue;
            }

            // skip occluders?
            if (!config.writeOccluders && isOccluder) {
                continue;
            }

            // skip info_ladder entities, they are used by the engine only to get
            // the mins and maxs of a func_ladder to ease bot navigation
            if (className.equals("info_ladder")) {
                continue;
            }

            // check for non-internal info_overlay entities
            if (className.equals("info_overlay_accessor")) {
                String oidStr = ent.getValue("OverlayID");

                if (oidStr != null && !oidStr.isEmpty()) {
                    int oid = Integer.valueOf(oidStr);

                    // save the targetname for writeOverlays()
                    overlayNames.put(oid, ent.getTargetName());

                    // don't write this entity here
                    continue;
                }
            }

            // re-use hammerid if possible, otherwise generate a new UID
            int entID = getHammerID(ent);

            // if we have nmo data re-use extractions ids
            if (nmo != null) {
                entID = nmo.extractions.stream()
                        .filter(extraction -> extraction.name.equals(ent.getTargetName()))
                        .findAny()
                        .map(extraction -> extraction.id)
                        .orElse(entID);
            }

            if (entID == -1) {
                entID = vmfmeta.getUID();
            }

            writer.start("entity");
            writer.put("id", entID);

            // get areaportal numbers
            int portalNum = -1;
            if (isAreaportal) {
                String portalNumString = ent.getValue("portalnumber");

                // extract portal number
                if (portalNumString != null) {
                    try {
                        portalNum = Integer.valueOf(portalNumString);
                    } catch (NumberFormatException ex) {
                        portalNum = -1;
                    }

                    // keep the number when debugging
                    if (!config.isDebug()) {
                        ent.removeValue("portalnumber");
                    }
                }
            }

            // get occluder numbers
            int occluderNum = -1;
            if (isOccluder) {
                String occluderNumString = ent.getValue("occludernumber");

                // extract occluder number
                if (occluderNumString != null) {
                    try {
                        occluderNum = Integer.valueOf(occluderNumString);
                    } catch (NumberFormatException ex) {
                        occluderNum = -1;
                    }

                    // keep the number when debugging
                    if (!config.isDebug()) {
                        ent.removeValue("occludernumber");
                    }
                }
            }

            int modelNum = ent.getModelNum();

            for (Map.Entry<String, String> kv : ent.getEntrySet()) {
                String key = kv.getKey();
                String value = kv.getValue();

                // skip angles for models and world brushes when fixing rotation
                if (key.equals("angles") && modelNum >= 0 && fixRot) {
                    continue;
                }

                // skip origin for world brushes
                if (key.equals("origin") && modelNum == 0) {
                    continue;
                }

                // skip model for brush entities
                if (key.equals("model") && modelNum != -2) {
                    continue;
                }

                // skip hammerid
                if (key.equals("hammerid")) {
                    continue;
                }

                // don't write angles and origin for portals and occluders
                if ((isAreaportal || isOccluder) && (key.equals("angles") || key.equals("origin"))) {
                    continue;
                }

                writer.put(key, value);
            }

            writer.put("classname", className);

            // write entity I/O
            List<KeyValue> io = ent.getIO();
            if (!io.isEmpty()) {
                writer.start("connections");

                for (KeyValue kv : io) {
                    writer.put(kv);
                }

                writer.end("connections");
            }

            // use origin for brush entities
            Vector3f origin = ent.getOrigin();

            // brush entities with angles values existed in an instance
            // during compilation and need to be rotated manually so Hammer
            // displays their correct rotation
            Vector3f angles = fixRot ? ent.getAngles() : null;

            // write model brushes
            if (modelNum > 0) {
                if (config.brushMode == BrushMode.BRUSHPLANES) {
                    brushsrc.writeModel(modelNum, origin, angles);
                } else {
                    facesrc.writeModel(modelNum, origin, angles);
                }
            } else {
                // retrieve areaportal brush from map
                if (isAreaportal && portalNum != -1) {
                    if (config.brushMode == BrushMode.BRUSHPLANES && apBrushMap.containsKey(portalNum)) {
                        brushsrc.writeBrush(apBrushMap.get(portalNum));
                        visgroups.add("Reallocated" + VmfMeta.VISGROUP_SEPERATOR + "areaportals");
                    } else {
                        facesrc.writeAreaportal(portalNum);
                        visgroups.add("Rebuild" + VmfMeta.VISGROUP_SEPERATOR + "areaportals");
                    }
                }

                // retrieve occluder brushes from map
                if (isOccluder && occluderNum != -1) {
                    if (config.brushMode == BrushMode.BRUSHPLANES && occBrushesMap.containsKey(occluderNum)) {
                        for (int brushId: occBrushesMap.get(occluderNum)) {
                            brushsrc.writeBrush(brushId);
                        }
                        visgroups.add("Reallocated" + VmfMeta.VISGROUP_SEPERATOR + "occluders");
                    } else {
                        facesrc.writeOccluder(occluderNum);
                        visgroups.add("Rebuild" + VmfMeta.VISGROUP_SEPERATOR + "occluders");
                    }
                }
            }

            // find instance prefix and add it to a visgroup
            if (instances && ent.getTargetName() != null) {
                Matcher m = INSTANCE_PREFIX.matcher(ent.getTargetName());

                if (m.find()) {
                    visgroups.add(m.group(1));
                }
            }

            // add protection flags to visgroup
            if (bspprot.isProtectedEntity(ent)) {
                visgroups.add("VMEX flagged entities");
            }

            // when we have nmo data, add objectives visgroups
            if (nmo != null && ent.getTargetName() != null) {
                nmo.nodes.stream()
                        .filter(objective -> objective.entityName.equals(ent.getTargetName()))
                        .forEach(objective -> visgroups.add("Objectives" + VmfMeta.VISGROUP_SEPERATOR + objective.name));

                nmo.nodes.stream()
                        .filter(objective -> objective.entities.stream().anyMatch(entitiyName -> entitiyName.equals(ent.getTargetName())))
                        .forEach(objective -> visgroups.add("Objectives" + VmfMeta.VISGROUP_SEPERATOR + objective.name));

                nmo.antiNodes.stream()
                        .filter(anti -> anti.entities.stream().anyMatch(entitiyName -> entitiyName.equals(ent.getTargetName())))
                        .forEach(anti -> visgroups.add("Objectives" + VmfMeta.VISGROUP_SEPERATOR + "anti" + VmfMeta.VISGROUP_SEPERATOR + anti.name));
            }

            // write visgroup metadata if filled
            if (!visgroups.isEmpty()) {
                vmfmeta.writeMetaVisgroups(visgroups);
            }

            writer.end("entity");
        }
    }

    /**
     * Writes all func_detail entities
     */
    public void writeDetails() {
        L.info("Writing func_details");

        if (config.detailMerge) {
            Set<AABB> detailBounds = new HashSet<>();
            Map<AABB, Integer> detailIndices = new HashMap<>();

            // add all detail brushes to queue
            for (int i = 0; i < bsp.brushes.size(); i++) {
                DBrush brush = bsp.brushes.get(i);

                // skip non-detail/non-solid brushes
                if (!brush.isSolid() || !brush.isDetail()) {
                    continue;
                }

                // skip VMEX protector brushes
                if (bspprot.isProtectedBrush(brush)) {
                    continue;
                }

                // get bounding box of the detail brush
                AABB bounds = BrushUtils.getBounds(bsp, brush);

                // writeBrush() expects brush indices, so map it to the AABB
                detailBounds.add(bounds);
                detailIndices.put(bounds, i);
            }

            while (!detailBounds.isEmpty()) {
                // get next group of merged brush AABBs
                Set<AABB> detailBoundsGroup = mergeNearestNeighborAABB(
                        detailBounds, config.detailMergeThresh);

                // write brush group as func_detail to VMF
                writer.start("entity");
                writer.put("id", vmfmeta.getUID());
                writer.put("classname", "func_detail");

                for (AABB bounds : detailBoundsGroup) {
                    brushsrc.writeBrush(detailIndices.get(bounds));
                }

                writer.end("entity");
            }
        } else {
            for (int i = 0; i < bsp.brushes.size(); i++) {
                DBrush brush = bsp.brushes.get(i);

                // skip non-detail/non-solid brushes
                if (!brush.isSolid() || !brush.isDetail()) {
                    continue;
                }

                // skip VMEX protector brushes
                if (bspprot.isProtectedBrush(brush)) {
                    continue;
                }

                writer.start("entity");
                writer.put("id", vmfmeta.getUID());
                writer.put("classname", "func_detail");
                brushsrc.writeBrush(i);

                writer.end("entity");
            }
        }

        // write protector brushes separately
        List<DBrush> protBrushes = bspprot.getProtectedBrushes();
        if (!protBrushes.isEmpty()) {
            writer.start("entity");
            writer.put("id", vmfmeta.getUID());
            writer.put("classname", "func_detail");
            vmfmeta.writeMetaVisgroup("VMEX protector brushes");

            for (DBrush protBrush : protBrushes) {
                brushsrc.writeBrush(bsp.brushes.indexOf(protBrush));
            }

            writer.end("entity");
        }
    }

    /**
     * Transfers the next group of touching bounding volumes from a set of loose
     * bounding volumes.
     * 
     * @param src input bounding volumes
     * @param thresh touching threshold
     * @return set of bounding volumes that have been removed from src
     */
    private Set<AABB> mergeNearestNeighborAABB(Set<AABB> src, float thresh) {
        // pop next AABB from src
        Iterator<AABB> iter = src.iterator();
        List<AABB> first = Collections.singletonList(iter.next());
        iter.remove();

        Queue<AABB> pending = new ArrayDeque<>(first);
        Set<AABB> group = new HashSet<>(first);

        // do while there are pending AABBs
        while (!pending.isEmpty()) {
            // get next pending AABB
            AABB current = pending.remove();

            // expand AABB slightly so it can touch other AABBs more reliably
            AABB currentTest = current.expand(thresh);

            iter = src.iterator();
            while (iter.hasNext()) {
                // get next AABB
                AABB other = iter.next();

                // is it touching the target AABB?
                if (other.intersectsWith(currentTest)) {
                    // add it as pending...
                    pending.add(other);

                    // ...and transfer to group
                    iter.remove();
                    group.add(other);
                }
            }
        }

        return group;
    }

    /**
     * Writes all info_overlay entities
     */
    public void writeOverlays() {
        L.info("Writing info_overlays");

        for (int i = 0; i < bsp.overlays.size(); i++) {
            DOverlay o = bsp.overlays.get(i);

            // calculate u/v bases
            Vector3f ubasis = new Vector3f(o.uvpoints[0].z, o.uvpoints[1].z, o.uvpoints[2].z);

            boolean vflip = o.uvpoints[3].z == 1;

            for (int j = 0; j < 4; j++) {
                o.uvpoints[j] = o.uvpoints[j].set(2, 0);
            }

            Vector3f vbasis = o.basisNormal.cross(ubasis).normalize();

            if (vflip) {
                vbasis = vbasis.scalar(-1);
            }

            // write VMF
            writer.start("entity");
            writer.put("id", vmfmeta.getUID());
            writer.put("classname", "info_overlay");
            writer.put("material", texsrc.getTextureName(o.texinfo));
            writer.put("StartU", o.u[0]);
            writer.put("EndU", o.u[1]);
            writer.put("StartV", o.v[0]);
            writer.put("EndV", o.v[1]);
            writer.put("BasisOrigin", o.origin);
            writer.put("BasisU", ubasis);
            writer.put("BasisV", vbasis);
            writer.put("BasisNormal", o.basisNormal);
            writer.put("origin", o.origin);

            // write fade distances
            if (bsp.overlayFades != null && !bsp.overlayFades.isEmpty()) {
                DOverlayFade of = bsp.overlayFades.get(i);
                writer.put("fademindist", of.fadeDistMinSq);
                writer.put("fademaxdist", of.fadeDistMaxSq);
            }

            // write system levels
            if (bsp.overlaySysLevels != null && !bsp.overlaySysLevels.isEmpty()) {
                DOverlaySystemLevel osl = bsp.overlaySysLevels.get(i);
                writer.put("mincpulevel", osl.minCPULevel);
                writer.put("maxcpulevel", osl.maxCPULevel);
                writer.put("mingpulevel", osl.minGPULevel);
                writer.put("maxgpulevel", osl.maxGPULevel);
            }

            for (int j = 0; j < 4; j++) {
                writer.put("uv" + j, o.uvpoints[j]);
            }

            writer.put("RenderOrder", o.getRenderOrder());

            Set<Integer> sides = new HashSet<>();
            int faceCount = o.getFaceCount();

            if (config.brushMode == BrushMode.BRUSHPLANES) {
                Set<Integer> origFaces = new HashSet<>();

                // collect original faces for this overlay
                for (int j = 0; j < faceCount; j++) {
                    int iface = o.ofaces[j];
                    int ioface = bsp.faces.get(iface).origFace;
                    if (ioface > 0) {
                        origFaces.add(ioface);
                    }
                }

                // scan brush sides for the original faces
                for (Integer ioface : origFaces) {
                    findOverlayFaces(i, ioface, sides);
                }
            } else {
                for (int j = 0; j < faceCount; j++) {
                    int iface = o.ofaces[j];
                    int faceId = vmfmeta.getFaceUID(iface);

                    if (faceId != -1) {
                        sides.add(faceId);
                    }
                }
            }

            // write brush side list
            writer.put("sides", sides.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" ")));

            if (overlayNames.containsKey(o.id)) {
                writer.put("targetname", overlayNames.get(o.id));
            }

            writer.end("entity");
        }
    }

    /**
     * Writes all prop_static entities
     */
    public void writeStaticProps() {
        L.info("Writing prop_statics");

        Map<Vector3f, String> lightingOrigins = new LinkedHashMap<>();

        for (DStaticProp pst : bsp.staticProps) {
            DStaticPropV4 pst4 = (DStaticPropV4) pst;

            writer.start("entity");
            writer.put("id", vmfmeta.getUID());
            writer.put("classname", "prop_static");
            writer.put("origin", pst4.origin);
            writer.put("angles", pst4.angles);
            writer.put("skin", pst4.skin);
            writer.put("fademindist", pst4.fademin == 0 ? -1 : pst4.fademin);
            writer.put("fademaxdist", pst4.fademax);
            writer.put("solid", pst4.solid);
            writer.put("model", bsp.staticPropName.get(pst4.propType));
            writer.put("screenspacefade", pst4.hasScreenSpaceFadeInPixels());

            // store coordinates and targetname of the lighing origin for later
            if (pst4.usesLightingOrigin()) {
                String infoLightingName;

                if (lightingOrigins.containsKey(pst4.lightingOrigin)) {
                    infoLightingName = lightingOrigins.get(pst4.lightingOrigin);
                } else {
                    infoLightingName = "sprp_lighting_" + lightingOrigins.size();
                    lightingOrigins.put(pst4.lightingOrigin, infoLightingName);
                }

                writer.put("lightingorigin", infoLightingName);
            }

            writer.put("disableshadows", pst4.hasNoShadowing());

            if (pst instanceof DStaticPropV5) {
                DStaticPropV5 pst5 = (DStaticPropV5) pst;
                writer.put("fadescale", pst5.forcedFadeScale);
                writer.put("disableselfshadowing", pst5.hasNoSelfShadowing());
                writer.put("disablevertexlighting", pst5.hasNoPerVertexLighting());
            }

            if (pst instanceof DStaticPropV6) {
                DStaticPropV6 pst6 = (DStaticPropV6) pst;
                writer.put("maxdxlevel", pst6.maxDXLevel);
                writer.put("mindxlevel", pst6.minDXLevel);
                writer.put("ignorenormals", pst6.hasIgnoreNormals());
            }

            // write that later; both v7 and v8 have it, but v8 extends v5
            Color32 diffMod = null;

            if (pst instanceof DStaticPropV7L4D) {
                DStaticPropV7L4D pst7 = (DStaticPropV7L4D) pst;
                diffMod = pst7.diffuseModulation;
            }

            if (pst instanceof DStaticPropV8) {
                DStaticPropV8 pst8 = (DStaticPropV8) pst;
                diffMod = pst8.diffuseModulation;
                writer.put("maxcpulevel", pst8.maxCPULevel);
                writer.put("mincpulevel", pst8.minCPULevel);
                writer.put("maxgpulevel", pst8.maxGPULevel);
                writer.put("mingpulevel", pst8.minGPULevel);
            }

            if (diffMod != null) {
                writer.put("rendercolor", String.format("%d %d %d",
                        diffMod.r, diffMod.g, diffMod.b));
                writer.put("renderamt", diffMod.a);
            }

            if (pst instanceof DStaticPropV9) {
                DStaticPropV9 pst9 = (DStaticPropV9) pst;
                writer.put("disableX360", pst9.disableX360);
            }

            if (pst instanceof DStaticPropV5Ship) {
                writer.put("targetname", ((DStaticPropV5Ship) pst).targetname);
            }

            if (pst instanceof DStaticPropV10) {
                DStaticPropV10 pst10 = (DStaticPropV10) pst;

                boolean genLightmaps = !pst10.hasNoPerTexelLighting();
                writer.put("generatelightmaps", genLightmaps);
                if (genLightmaps) {
                    writer.put("lightmapresolutionx", pst10.lightmapResolutionX);
                    writer.put("lightmapresolutiony", pst10.lightmapResolutionY);
                }
            }

            if (pst instanceof DStaticPropV11lite) {
                DStaticPropV11lite pst11 = (DStaticPropV11lite) pst;
                // only write if it's set to anything other than default
                if (pst11.diffuseModulation.rgba != -1) {
                    diffMod = pst11.diffuseModulation;
                    writer.put("rendercolor", String.format("%d %d %d",
                            diffMod.r, diffMod.g, diffMod.b));
                    writer.put("renderamt", diffMod.a);
                }
            }

            if (pst instanceof DStaticPropV10CSGO) {
                writer.put("drawinfastreflection", ((DStaticPropV10CSGO) pst).hasRenderInFastReflection());
            }

            if (pst instanceof DStaticPropV11CSGO) {
                writer.put("uniformscale", ((DStaticPropV11CSGO) pst).uniformScale);
            }

            writer.end("entity");
        }

        // write lighting origins
        for (Vector3f origin : lightingOrigins.keySet()) {
            writer.start("entity");
            writer.put("id", vmfmeta.getUID());
            writer.put("classname", "info_lighting");
            writer.put("targetname", lightingOrigins.get(origin));
            writer.put("origin", origin);
            writer.end("entity");
        }
    }

    /**
     * Writes all env_cubemap entities
     */
    public void writeCubemaps() {
        L.info("Writing env_cubemaps");

        for (int i = 0; i < bsp.cubemaps.size(); i++) {
            DCubemapSample cm = bsp.cubemaps.get(i);

            writer.start("entity");
            writer.put("id", vmfmeta.getUID());
            writer.put("classname", "env_cubemap");
            writer.put("origin", new Vector3f(cm.origin[0], cm.origin[1], cm.origin[2]));
            writer.put("cubemapsize", cm.size);

            // FIXME: results are too bad, find a better way
            Set<Integer> sideList = texsrc.getBrushSidesForCubemap(i);

            if (sideList != null) {
                int cmSides = sideList.size();

                if (cmSides > config.maxCubemapSides) {
                    L.log(Level.FINER, "Cubemap {0} has too many sides: {1}",
                            new Object[]{i, sideList});
                }

                // write list of brush sides that use this cubemap
                if (cmSides > 0 && cmSides < config.maxCubemapSides) {
                    writer.put("sides", sideList.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(" ")));
                }
            }

            writer.end("entity");
        }
    }

    /**
     * Writes all func_ladder entities
     */
    public void writeLadders() {
        L.info("Writing func_ladders");

        for (int i = 0; i < bsp.brushes.size(); i++) {
            DBrush brush = bsp.brushes.get(i);

            // skip non-ladder brushes
            if (!brush.isLadder()) {
                continue;
            }

            // write brush as func_ladder
            writer.start("entity");
            writer.put("id", vmfmeta.getUID());
            writer.put("classname", "func_ladder");

            brushsrc.writeBrush(i);

            writer.end("entity");
        }
    }

    private void findOverlayFaces(int ioverlay, int ioface, Set<Integer> sides) {
        // no original face information available? then we're done here...
        if (bsp.origFaces.isEmpty()) {
            return;
        }

        // don't add more if we already hit the maximum
        if (sides.size() >= config.maxOverlaySides) {
            return;
        }

        int sidesPrev = sides.size();

        DFace origFace = bsp.origFaces.get(ioface);

        // use sideid of displacement, if existing
        if (origFace.dispInfo != -1) {
            int side = vmfmeta.getDispInfoUID(origFace.dispInfo);
            if (side != -1) {
                L.log(Level.FINER, "O: {0} D: {1} id: {2}",
                        new Object[]{ioverlay, origFace.dispInfo, side});
                sides.add(side);
            }

            return;
        }

        // create winding from original face
        Winding wof = WindingFactory.fromFace(bsp, origFace);

        for (int i = 0; i < bsp.brushes.size(); i++) {
            DBrush brush = bsp.brushes.get(i);

            for (int j = 0; j < brush.numside; j++) {
                int ibs = brush.fstside + j;
                int side = brushsrc.getBrushSideIDForIndex(ibs);

                // skip unmapped brush sides
                if (side == -1) {
                    continue;
                }

                DBrushSide bs = bsp.brushSides.get(ibs);

                // create winding from brush side
                Winding w = WindingFactory.fromSide(bsp, brush, j);

                // check for valid face: same plane, same texinfo, same geometry
                if (origFace.pnum != bs.pnum || origFace.texinfo != bs.texinfo
                        || !w.matches(wof)) {
                    continue;
                }

                L.log(Level.FINER, "O: {0} OF: {1} B: {2} BS: {3} id: {4}",
                        new Object[]{ioverlay, ioface, i, ibs, side});

                sides.add(side);

                // make sure we won't have too many brush sides for that overlay
                if (sides.size() >= config.maxOverlaySides) {
                    L.log(Level.WARNING, "Too many brush sides for overlay {0}", ioverlay);
                    break;
                }
            }
        }

        if (sides.size() == sidesPrev) {
            L.log(Level.FINER, "O: {0} OF: {1} no match", new Object[]{ioverlay, ioface});
        }
    }

    private void processEntities() {
        for (Entity ent : bsp.entities) {
            String className = ent.getClassName();

            // fix worldspawn
            if (className.equals("worldspawn")) {
                // remove values that are unknown to Hammer
                ent.removeValue("world_mins");
                ent.removeValue("world_maxs");
                ent.removeValue("hammerid");

                // rebuild mapversion
                if (!ent.hasKey("mapversion")) {
                    ent.setValue("mapversion", bspFile.getRevision());
                }
            }

            // convert VMF format if requested
            if (config.sourceFormat != SourceFormat.AUTO) {
                char srcSep;
                char dstSep;

                if (config.sourceFormat == SourceFormat.NEW) {
                    srcSep = EntityIO.SEP_CHR_OLD;
                    dstSep = EntityIO.SEP_CHR_NEW;
                } else {
                    srcSep = EntityIO.SEP_CHR_NEW;
                    dstSep = EntityIO.SEP_CHR_OLD;
                }

                for (KeyValue kv : ent.getIO()) {
                    String value = kv.getValue();
                    value = value.replace(srcSep, dstSep);
                    kv.setValue(value);
                }
            }

            // replace escaped quotes for VTMB so they can be loaded with the
            // inofficial SDK Hammer
            if (bspFile.getSourceApp().getAppID() == SourceAppID.VAMPIRE_BLOODLINES) {
                for (Map.Entry<String, String> kv : ent.getEntrySet()) {
                    String value = kv.getValue();
                    value = value.replace("\\\"", "");
                    kv.setValue(value);
                }

                for (KeyValue kv : ent.getIO()) {
                    String value = kv.getValue();
                    value = value.replace("\\\"", "");
                    kv.setValue(value);
                }
            }

            // func_simpleladder entities are used by the engine only and won't
            // work when re-compiling, so replace them with empty func_ladder's
            // instead.
            if (className.equals("func_simpleladder")) {
                int modelNum = ent.getModelNum();

                ent.clear();
                ent.setClassName("func_ladder");
                ent.setModelNum(modelNum);
            }

            // fix light entities (except for dynamic lights)
            if (className.startsWith("light")
                    && !className.equals("light_dynamic")) {
                fixLightEntity(ent);
            }

            // add cameras based on info_player_* positions
            if (className.startsWith("info_player_")) {
                createCamera(ent);
            }

            // add hammerid to UID blacklist to make sure they're not generated
            // for anything else
            int hammerid = getHammerID(ent);
            if (hammerid != -1) {
                vmfmeta.getUIDBlackList().add(hammerid);
            }
        }
    }

    private int getHammerID(Entity ent) {
        String keyName = "hammerid";

        if (!ent.hasKey(keyName)) {
            return -1;
        }

        String hammeridStr = ent.getValue(keyName);
        int hammerid = -1;

        try {
            hammerid = Integer.parseInt(ent.getValue("hammerid"));
        } catch (NumberFormatException ex) {
            L.log(Level.WARNING, "Invalid hammerid format {0}", hammeridStr);
        }

        return hammerid;
    }

    private void fixLightEntity(Entity ent) {
        String style = ent.getValue("style");
        String defaultStyle = ent.getValue("defaultstyle");

        if (style == null) {
            // no style
            return;
        }

        try {
            // values below 32 = default presets
            if (Integer.valueOf(style) < 32) {
                return;
            }
        } catch (NumberFormatException ex) {
            L.log(Level.WARNING, "Invalid light style number format: {0}", style);
        }

        // Use original preset style, if set. Empty the style otherwise.
        if (defaultStyle != null) {
            ent.setValue("style", defaultStyle);
            ent.removeValue("defaultstyle");
        } else {
            ent.removeValue("style");
        }
    }

    private void createCamera(Entity ent) {
        Vector3f origin = ent.getOrigin();
        Vector3f angles = ent.getAngles();

        if (origin == null) {
            return;
        }

        if (angles == null) {
            angles = Vector3f.NULL;
        }

        // calculate position and look vectors
        Vector3f pos, look;

        // move 64 units up
        pos = origin.add(new Vector3f(0, 0, 64));

        // look 256 units forwards to entity facing direction
        look = new Vector3f(192, 0, 0).rotate(angles).add(origin);

        // move 64 units backwards to facing direction
        pos = look.sub(pos).normalize().scalar(-64).add(pos);

        vmfmeta.getCameras().add(new Camera(pos, look));
    }

    /**
     * Sets nmo data. Causes referenced 'objectives/antiObjectives' entities to be written in visgroups and extraction entity to reuse nmo entity id
     * @param nmoData the nmo data
     */
    public void setNmo(NmoFile nmoData) {
        this.nmo = nmoData;

        nmoData.nodes.forEach(nmoObjective -> vmfmeta.reserveVisgroupId("Objectives" + VmfMeta.VISGROUP_SEPERATOR + nmoObjective.name, nmoObjective.id));
        nmoData.antiNodes.forEach(nmoAntiObjective -> vmfmeta.reserveVisgroupId("Objectives" + VmfMeta.VISGROUP_SEPERATOR + "anti" + VmfMeta.VISGROUP_SEPERATOR + nmoAntiObjective.name, nmoAntiObjective.id));
        nmoData.extractions.forEach(extraction -> vmfmeta.getUIDBlackList().add(extraction.id));
    }
}

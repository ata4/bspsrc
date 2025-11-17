/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.decompiler.modules.entity;

import info.ata4.bspsrc.decompiler.BspSourceConfig;
import info.ata4.bspsrc.decompiler.VmfWriter;
import info.ata4.bspsrc.decompiler.modules.BspProtection;
import info.ata4.bspsrc.decompiler.modules.ModuleDecompile;
import info.ata4.bspsrc.decompiler.modules.VmfMeta;
import info.ata4.bspsrc.decompiler.modules.geom.*;
import info.ata4.bspsrc.decompiler.modules.texture.TextureSource;
import info.ata4.bspsrc.decompiler.modules.texture.ToolTexture;
import info.ata4.bspsrc.decompiler.util.*;
import info.ata4.bspsrc.lib.BspFileReader;
import info.ata4.bspsrc.lib.app.SourceAppId;
import info.ata4.bspsrc.lib.entity.Entity;
import info.ata4.bspsrc.lib.entity.EntityIO;
import info.ata4.bspsrc.lib.entity.KeyValue;
import info.ata4.bspsrc.lib.nmo.NmoAntiObjective;
import info.ata4.bspsrc.lib.nmo.NmoFile;
import info.ata4.bspsrc.lib.nmo.NmoObjective;
import info.ata4.bspsrc.lib.struct.*;
import info.ata4.bspsrc.lib.vector.Vector3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Decompiling module to write point and brush entities converted from various lumps.
 *
 * Based on several entity building methods from Vmex
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntitySource extends ModuleDecompile {

    // logger
    private static final Logger L = LogManager.getLogger();

    private static final Pattern INSTANCE_PREFIX = Pattern.compile("^([^-]+)-");

    private final WindingFactory windingFactory;
    private final BrushBounds brushBounds;

    // sub-modules
    private final BspSourceConfig config;
    private final BrushSource brushsrc;
    private final FaceSource facesrc;
    private final TextureSource texsrc;
    private final BspProtection bspprot;
    private final VmfMeta vmfmeta;
    private final BrushSideFaceMapper brushSideFaceMapper;

    private final AreaportalMapper.ReallocationData areaportalReallocationData;
    private final OccluderMapper.ReallocationData occluderReallocationData;

    //'No More Room in Hell' Nmo data
    private NmoFile nmo;

    // overlay target names
    private final Map<Integer, String> overlayNames = new HashMap<>();

    public EntitySource(
            BspFileReader reader,
            VmfWriter writer,
            BspSourceConfig config,
            BrushSource brushsrc,
            FaceSource facesrc,
            TextureSource texsrc,
            BspProtection bspprot,
            VmfMeta vmfmeta,
            BrushSideFaceMapper brushSideFaceMapper,
            WindingFactory windingFactory,
            BrushBounds brushBounds,
            AreaportalMapper.ReallocationData areaportalReallocationData,
            OccluderMapper.ReallocationData occluderReallocationData
    ) {
        super(reader, writer);

        this.windingFactory = requireNonNull(windingFactory);
        this.brushBounds = requireNonNull(brushBounds);

        this.config = requireNonNull(config);
        this.brushsrc = requireNonNull(brushsrc);
        this.facesrc = requireNonNull(facesrc);
        this.texsrc = requireNonNull(texsrc);
        this.bspprot = requireNonNull(bspprot);
        this.vmfmeta = requireNonNull(vmfmeta);
        this.brushSideFaceMapper = requireNonNull(brushSideFaceMapper);

        this.areaportalReallocationData = requireNonNull(areaportalReallocationData);
        this.occluderReallocationData = requireNonNull(occluderReallocationData);

        processEntities();
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
     * @see #writeCubemaps
     * @see #writeOverlays
     * @see #writeStaticProps
     * @see #writeDetails
     */
    public void writeEntities() {
        L.info("Writing entities");

        // MOTIVATION BEHIND fixEntityRot:
        // Brush entities can have the 'angles' property, which rotates the model of the entitiy
        // Hammer itself, however, doesn't respect this property and doesn't these entities with their rotiation.
        // Because of this, normally brush entities don't have angles, as nobody would create brush entities
        // with rotation if it is not displayed in the editor anyway.
        // However, when a brush entity is part of a func_instance, the compilation process applies the rotation
        // of the instance by simply modifying the angles property of the brush entitiy, creating these cases
        // of brush entities with angles.
        // This (I think) is in it of itself not a problem, as decompiling and recompiling doesn't loose
        // the 'angles' property, meaning the rotation doesn't get lost.
        // However, because the hammer editor doesn't properly display this rotation we have the option
        // to directly apply the rotation of the entity when writing to the vmf, sort of 'baking' the rotation.
        // This is theoretically less 'correct' and futher away from the original map, but also these brushes
        // to be displayed in their correct rotation in hammer.
        // TODO: Maybe for some entities we don't want to bake the rotation. Perhaps consider whitelist of entities
        // we bake rotation for
        boolean fixEntityRot = config.fixEntityRot;

	    // Initialise visgroup colors
        VmfMeta.Visgroup reallocatedVg = vmfmeta
                .visgroups()
                .getVisgroup("Reallocated")
                .setColor(Color.GREEN);

        VmfMeta.Visgroup rebuildVg = vmfmeta
                .visgroups()
                .getVisgroup("Rebuild")
                .setColor(Color.RED);

        VmfMeta.Visgroup reallocatedAreaportalVg = reallocatedVg.getVisgroup("Areaportal")
                .setColor(Color.CYAN);
        VmfMeta.Visgroup rebuildAreaportalVg = rebuildVg.getVisgroup("Areaportal")
                .setColor(Color.CYAN.darker());

        VmfMeta.Visgroup reallocatedOccluderVg = reallocatedVg.getVisgroup("Occluder")
                .setColor(Color.MAGENTA);
        VmfMeta.Visgroup rebuildOccluderVg = rebuildVg.getVisgroup("Occluder")
                .setColor(Color.MAGENTA.darker());


        List<VmfMeta.Visgroup> visgroups = new ArrayList<>();

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

            // get areaportal numbers
            int portalNum = -1;
            if (isAreaportal) {
                String portalNumString = ent.getValue("portalnumber");

                if (portalNumString == null) {
                    L.warn(String.format(
                            "%s is missing 'portalnumber' attribute, skipping...",
                            className
                    ));
                    continue;
                }

                try {
                    portalNum = Integer.parseInt(portalNumString);
                } catch (NumberFormatException e) {
                    L.warn(String.format(
                            "Can't parse %s 'portalnumber' attribute: '%s', skipping...",
                            className,
                            portalNumString
                    ));
                    continue;
                }
                int finalPortalNum = portalNum;
                var areaportalsWithPortalKey = bsp.areaportals.stream()
                        .filter(areaportal -> areaportal.portalKey == finalPortalNum)
                        .collect(Collectors.toSet());
                if (areaportalsWithPortalKey.isEmpty()) {
                    L.warn("func_areaportal entity links to a non existing areaportal, skipping...");
                    continue;
                }
                
                var areaportalsHaveInvalidGeometry = areaportalsWithPortalKey.stream()
                        .anyMatch(areaportal -> areaportal.clipPortalVerts < 3);
                if (areaportalsHaveInvalidGeometry) {
                    L.warn("func_areaportal links areaportal with invalid geometry, skipping...");
                    continue;
                }

                // keep the number when debugging
                if (!config.debug) {
                    ent.removeValue("portalnumber");
                }
            }

            // get occluder numbers
            int occluderNum = -1;
            if (isOccluder) {
                String occluderNumString = ent.getValue("occludernumber");

                if (occluderNumString == null) {
                    L.warn(String.format(
                            "%s is missing 'occludernumber' attribute, skipping...",
                            className
                    ));
                    continue;
                }

                try {
                    occluderNum = Integer.parseInt(occluderNumString);
                } catch (NumberFormatException ex) {
                    L.warn(String.format(
                            "Can't parse %s 'occludernumber' attribute: '%s', skipping...",
                            className,
                            occluderNumString
                    ));
                    continue;
                }

                // keep the number when debugging
                if (!config.debug) {
                    ent.removeValue("occludernumber");
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

            int modelNum = ent.getModelNum();

            for (Map.Entry<String, String> kv : ent.getEntrySet()) {
                String key = kv.getKey();
                String value = kv.getValue();

                // skip angles for models and world brushes when fixing rotation
                if (key.equals("angles") && modelNum >= 0 && fixEntityRot) {
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
            Vector3f angles = fixEntityRot ? ent.getAngles() : null;

            // write model brushes
            if (modelNum > 0) {
                if (config.brushMode == BrushMode.BRUSHPLANES) {
                    brushsrc.writeModel(modelNum, origin, angles);
                } else {
                    facesrc.writeModel(modelNum, origin, angles);
                }
            } else {
                if (isAreaportal && portalNum >= 0) {
                    var reallocatedIBrush = areaportalReallocationData.mapping().get(portalNum);
                    if (config.brushMode == BrushMode.BRUSHPLANES && reallocatedIBrush != null) {
                        brushsrc.writeBrush(reallocatedIBrush);
                        visgroups.add(reallocatedAreaportalVg);
                    } else {
                        facesrc.writeAreaportal(portalNum);
                        visgroups.add(rebuildAreaportalVg);
                    }

                    if (config.debug) {
                        visgroups.add(vmfmeta.visgroups()
                                .getVisgroup("AreaportalID")
                                .getVisgroup(String.valueOf(portalNum)));
                    }
                }

                if (isOccluder && occluderNum >= 0) {
                    var reallocatedIBrushes = occluderReallocationData.occluderToBrushes().get(occluderNum);
                    if (config.brushMode == BrushMode.BRUSHPLANES && reallocatedIBrushes != null) {
                        for (int brushId: reallocatedIBrushes) {
                            brushsrc.writeBrush(brushId);
                        }
                        visgroups.add(reallocatedOccluderVg);
                    } else {
                        facesrc.writeOccluder(occluderNum);
                        visgroups.add(rebuildOccluderVg);
                    }
                }
            }

            // find instance prefix and add it to a visgroup
            // This is check is very weak and can create a lot of false positives
            // TODO: Perhaps design better one. Ref: (https://developer.valvesoftware.com/wiki/Func_instance)
            if (ent.getTargetName() != null) {
                Matcher m = INSTANCE_PREFIX.matcher(ent.getTargetName());

                if (m.find()) {
                    visgroups.add(vmfmeta.visgroups()
                            .getVisgroup("Instances")
                            .getVisgroup(m.group(1)));
                }
            }

            // add protection flags to visgroup
            if (bspprot.isProtectedEntity(ent)) {
                visgroups.add(vmfmeta.visgroups().getVisgroup("VMEX flagged entities"));
            }

            // when we have nmo data, add objectives visgroups
            if (nmo != null && ent.getTargetName() != null) {
                nmo.nodes.stream()
                        .filter(objective -> Stream.concat(objective.entities.stream(), Stream.of(objective.entityName))
                                .anyMatch(entitiyName -> entitiyName.equals(ent.getTargetName())))
                        .forEach(objective -> visgroups.add(vmfmeta.visgroups()
                                .getVisgroup("Objectives")
                                .getVisgroup(objective.name)));

                nmo.antiNodes.stream()
                        .filter(anti -> anti.entities.stream()
                                .anyMatch(entitiyName -> entitiyName.equals(ent.getTargetName())))
                        .forEach(anti -> visgroups.add(vmfmeta.visgroups()
                                .getVisgroup("Objectives")
                                .getVisgroup("anti")
                                .getVisgroup(anti.name)));
            }

            // write visgroup metadata if filled
            if (!visgroups.isEmpty()) {
                vmfmeta.writeMetaVisgroups(visgroups);
            }

            writer.end("entity");
        }

        //If we're in debug, we write some additional entities
        if (config.debug) {
            AreaportalMapper.writeDebugPortals(bsp, facesrc, windingFactory, writer, vmfmeta);
            OccluderMapper.writeDebugOccluders(bsp, facesrc, windingFactory, writer, vmfmeta);
        }
    }

    /**
     * Writes all func_detail entities
     */
    public void writeDetails() {
        L.info("Writing func_details");

        Set<DBrush> funcDetailBrushes = bsp.brushes.stream()
                .filter(brushsrc::isFuncDetail)
                .filter(dBrush -> !bspprot.isProtectedBrush(dBrush))
                .collect(Collectors.toSet());

        Set<Set<DBrush>> funcDetailBrushGroups;

        if (config.detailMerge) {
            int newGroupId = 0;
            Map<DBrush, Integer> brushGroups = new HashMap<>();
            for (DBrush funcDetailBrush : funcDetailBrushes) {
                AABB bounds = brushBounds.getBounds(bsp, funcDetailBrush);
                AABB extendedBounds = bounds.expand(config.detailMergeThresh);

                // get all ids of groups that touch this brush
                Set<Integer> intersectingGroupIds = new HashSet<>();
                brushGroups.forEach((dBrush, groupId) -> {
                    if (!intersectingGroupIds.contains(groupId)) {
                        AABB otherBrushBounds = brushBounds.getBounds(bsp, dBrush);
                        if (extendedBounds.intersectsWith(otherBrushBounds)) {
                            intersectingGroupIds.add(groupId);
                        }
                    }
                });

                if (!intersectingGroupIds.isEmpty()) {
                    int finalNewGroupId = newGroupId;
                    brushGroups.replaceAll((dBrush, groupId) -> {
                        return intersectingGroupIds.contains(groupId) ? finalNewGroupId : groupId;
                    });
                }
                brushGroups.put(funcDetailBrush, newGroupId++);
            }

            Map<Integer, Set<DBrush>> inverseBrushGroups = new HashMap<>();
            brushGroups.forEach((dBrush, groupId) -> inverseBrushGroups.computeIfAbsent(groupId, HashSet::new).add(dBrush));
            funcDetailBrushGroups = new HashSet<>(inverseBrushGroups.values());
        } else {
            funcDetailBrushGroups = funcDetailBrushes.stream()
                    .map(Collections::singleton)
                    .collect(Collectors.toSet());
        }

        for (Set<DBrush> funcDetailBrushGroup : funcDetailBrushGroups) {
            writer.start("entity");
            writer.put("id", vmfmeta.getUID());
            writer.put("classname", "func_detail");

            funcDetailBrushGroup.stream()
                    .map(bsp.brushes::indexOf)
                    .forEach(brushsrc::writeBrush);

            writer.end("entity");
        }

        // TODO: doesn't this cause all protected brushes to be written as func_detail
        //  (and therefore also causing some brushes to be written twice)?
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
    
    public void writeVisClusters() {
        L.info("Writing func_viscluster");
        
        var leafsByCluster = bsp.leaves.stream()
                .filter(dLeaf -> dLeaf.cluster >= 0)
                .filter(dLeaf -> {
                    var size = dLeaf.maxs.sub(dLeaf.mins);
                    return size.x > 0 && size.y > 0 && size.z > 0;
                })
                .collect(Collectors.groupingBy(dLeaf -> dLeaf.cluster));

        for (var leaves : leafsByCluster.values()) {
            if (leaves.size() < 2)
                continue;
            
            int cluster = leaves.getFirst().cluster;
            
            var bounds = leaves.stream()
                    .map(dLeaf -> new AABB(dLeaf.mins, dLeaf.maxs))
                    .reduce(AABB.ZERO, AABB::include);

            writer.start("entity");
            writer.put("id", vmfmeta.getUID());
            writer.put("classname", "func_viscluster");
            
            facesrc.writePolygon(
                    new Winding(List.of(
                            bounds.getMin(),
                            bounds.getMin().set(0, bounds.getMax().x),
                            bounds.getMin().set(0, bounds.getMax().x).set(1, bounds.getMax().y),
                            bounds.getMin().set(1, bounds.getMax().y)
                    )),
                    ToolTexture.TRIGGER,
                    true,
                    bounds.getMax().z - bounds.getMin().z
            );

            vmfmeta.writeMetaVisgroups(List.of(vmfmeta.visgroups()
                    .getVisgroup("Rebuild")
                    .getVisgroup("Vis Cluster")
                    .getVisgroup(String.valueOf(cluster))));

            writer.end("entity");
            
            if (config.debug) {
                for (var leaf : leaves) {
                    writer.start("entity");
                    writer.put("id", vmfmeta.getUID());
                    writer.put("classname", "func_detail");

                    facesrc.writePolygon(
                            new Winding(List.of(
                                    leaf.mins,
                                    leaf.mins.set(0, leaf.maxs.x),
                                    leaf.mins.set(0, leaf.maxs.x).set(1, leaf.maxs.y),
                                    leaf.mins.set(1, leaf.maxs.y)
                            )),
                            ToolTexture.SKIP,
                            true,
                            leaf.maxs.z - leaf.mins.z
                    );

                    vmfmeta.writeMetaVisgroups(List.of(vmfmeta.visgroups()
                            .getVisgroup("debug")
                            .getVisgroup("leafs-in-cluster")
                            .getVisgroup(String.valueOf(cluster))));

                    writer.end("entity");
                }
            }
        }
    }

    /**
     * Writes all info_overlay entities
     */
    public void writeOverlays() {
        L.info("Writing info_overlays");

        for (int overlayI = 0; overlayI < bsp.overlays.size(); overlayI++) {
            DOverlay o = bsp.overlays.get(overlayI);
            
            // Don't try writing overlays outside map extend. Make hammer hang for some reason...
            // See issue https://github.com/ata4/bspsrc/issues/143
            if (windingFactory.isHuge(o.origin)) {
                L.warn("Overlay {} has origin {} outside map extends, skipping...", overlayI, o.origin);
                continue;
            }
            var uvPointHuge = Arrays.stream(o.uvpoints)
                    .filter(windingFactory::isHuge)
                    .findFirst();
            if (uvPointHuge.isPresent()) {
                L.warn("Overlay {} has uvpoint {} outside map extends, skipping...",
                        overlayI, uvPointHuge.orElseThrow());
                continue;
            }

            // calculate u/v bases
            Vector3f ubasis = new Vector3f(o.uvpoints[0].z, o.uvpoints[1].z, o.uvpoints[2].z);

            boolean vflip = o.uvpoints[3].z == 1;

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
                DOverlayFade of = bsp.overlayFades.get(overlayI);
                writer.put("fademindist", of.fadeDistMinSq);
                writer.put("fademaxdist", of.fadeDistMaxSq);
            }

            // write system levels
            if (bsp.overlaySysLevels != null && !bsp.overlaySysLevels.isEmpty()) {
                DOverlaySystemLevel osl = bsp.overlaySysLevels.get(overlayI);
                writer.put("mincpulevel", osl.minCPULevel);
                writer.put("maxcpulevel", osl.maxCPULevel);
                writer.put("mingpulevel", osl.minGPULevel);
                writer.put("maxgpulevel", osl.maxGPULevel);
            }

            for (int j = 0; j < 4; j++) {
                writer.put("uv" + j, o.uvpoints[j].set(2, 0));
            }

            writer.put("RenderOrder", o.getRenderOrder());

            Set<Integer> sides = new HashSet<>();
            int faceCount = o.getFaceCount();

            if (config.brushMode == BrushMode.BRUSHPLANES) {
                for (int j = 0; j < faceCount; j++) {
                    DFace dFace = bsp.faces.get(o.ofaces[j]);

                    if (dFace.dispInfo >= 0) {
                        sides.add(vmfmeta.getDispInfoUID(dFace.dispInfo));
                    } else {
                        int origFaceI = dFace.origFace;
                        if (origFaceI < 0)
                            continue;

                        for (int brushSideI : brushSideFaceMapper.getBrushSideIndices(origFaceI)) {
                            int brushSideId = brushsrc.getBrushSideIDForIndex(brushSideI);
                            if (brushSideId >= 0) {
                                sides.add(brushSideId);
                            } else {
                                L.warn("Face {} used by overlay {} is mapped to brushside {}, which was never written.", overlayI, origFaceI, brushSideI);
                            }
                        }
                    }
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
            writer.put("ignorenormals", pst4.hasIgnoreNormals());
            writer.put("disableselfshadowing", pst4.hasNoSelfShadowing());
            writer.put("disablevertexlighting", pst4.hasNoPerVertexLighting());

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

            if (pst instanceof DStaticPropV5 pst5) {
                writer.put("fadescale", pst5.forcedFadeScale);
            }

            if (pst instanceof DStaticPropV6 pst6) {
                writer.put("maxdxlevel", pst6.maxDXLevel);
                writer.put("mindxlevel", pst6.minDXLevel);
            }

            if (pst instanceof DStaticPropVinScaling) {
                writer.put("scale", ((DStaticPropVinScaling) pst).getScaling());
            }

            // write that later; both v7 and v8 have it, but v8 extends v5
            Color32 diffMod = null;

            if (pst instanceof DStaticPropV7L4D pst7) {
                diffMod = pst7.diffuseModulation;
            }

            if (pst instanceof DStaticPropV8 pst8) {
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

            if (pst instanceof DStaticPropV9 pst9) {
                writer.put("disableX360", pst9.disableX360);
            }

            if (pst instanceof DStaticPropV5Ship) {
                writer.put("targetname", ((DStaticPropV5Ship) pst).targetname);
            }

            if (pst instanceof DStaticPropV10 pst10) {

                boolean genLightmaps = !pst10.hasNoPerTexelLighting();
                writer.put("generatelightmaps", genLightmaps);
                if (genLightmaps) {
                    writer.put("lightmapresolutionx", pst10.lightmapResolutionX);
                    writer.put("lightmapresolutiony", pst10.lightmapResolutionY);
                }
            }

            if (pst instanceof DStaticPropV11lite pst11) {
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
                writer.put("disableshadowdepth", ((DStaticPropV10CSGO) pst).hasDisableShadowDepth());
                writer.put("enablelightbounce", ((DStaticPropV10CSGO) pst).hasEnableLightBounce());
            }

            if (pst instanceof DStaticPropV11CSGO) {
                writer.put("uniformscale", ((DStaticPropV11CSGO) pst).uniformScale);
            }
            
            if (pst instanceof DStaticPropV13) {
                writer.put("scale", ((DStaticPropV13) pst).scale);
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
                    L.trace("Cubemap {} has too many sides: {}", i, sideList);
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

        for (int i = 0; i < brushsrc.getWorldbrushes(); i++) {
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
            if (bspFile.getAppId() == SourceAppId.VAMPIRE_BLOODLINES) {
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

            // func_simpleladder entities are created by left 4 dead engine branch
            // we use these to reconstruct func_ladder entities
            if (className.equals("func_simpleladder")) {
                int modelNum = ent.getModelNum();
                String hammerId = ent.getValue("hammerid");

                ent.clear();
                ent.setClassName("func_ladder");
                ent.setModelNum(modelNum);

                if (hammerId != null)
                    ent.setValue("hammerid", hammerId);
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
            L.warn("Invalid hammerid format {}", hammeridStr);
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
            L.warn("Invalid light style number format: {}", style);
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
     * Sets nmo data. Causes referenced 'objectives/antiObjectives' entities to be written in visgroups
     * and extraction entity to reuse nmo entity id
     * @param nmoData the nmo data
     */
    public void setNmo(NmoFile nmoData) {
        this.nmo = nmoData;

        for (NmoObjective nmoObjective : nmoData.nodes) {
            try {
                vmfmeta.reserveVisgroupId(nmoObjective.id, "Objectives", nmoObjective.name);
            } catch (VmfMeta.VisgroupException e) {
                L.error("Error reserving visgroup ids for Nmrih Objectives", e);
            }
        }
        for (NmoAntiObjective nmoAntiObjective : nmoData.antiNodes) {
            try {
                vmfmeta.reserveVisgroupId(nmoAntiObjective.id, "Objectives", "anti", nmoAntiObjective.name);
            } catch (VmfMeta.VisgroupException e) {
                L.error("Error reserving visgroup ids for Nmrih Objectives", e);
            }
        }
        nmoData.extractions.forEach(extraction -> vmfmeta.getUIDBlackList().add(extraction.id));
    }
}

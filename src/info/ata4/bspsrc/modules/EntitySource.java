/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.modules;

import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.entity.KeyValue;
import info.ata4.bsplib.struct.Color32;
import info.ata4.bsplib.struct.DAreaportal;
import info.ata4.bsplib.struct.DBrush;
import info.ata4.bsplib.struct.DBrushSide;
import info.ata4.bsplib.struct.DCubemapSample;
import info.ata4.bsplib.struct.DFace;
import info.ata4.bsplib.struct.DOverlay;
import info.ata4.bsplib.struct.DOverlayFade;
import info.ata4.bsplib.struct.DOverlaySystemLevel;
import info.ata4.bsplib.struct.DStaticProp;
import info.ata4.bsplib.struct.DStaticPropShip;
import info.ata4.bsplib.struct.DStaticPropV5;
import info.ata4.bsplib.struct.DStaticPropV6;
import info.ata4.bsplib.struct.DStaticPropV7;
import info.ata4.bsplib.struct.DStaticPropV8;
import info.ata4.bsplib.struct.DStaticPropV9;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bspsrc.Camera;
import info.ata4.bspsrc.util.Winding;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decompiling module to write point and brush entities converted from various lumps.
 * 
 * Based on several entity building methods from Vmex
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntitySource extends BspSourceVmfModule {

    // logger
    private static final Logger L = Logger.getLogger(EntitySource.class.getName());

    private static final Pattern INSTANCE_PREFIX = Pattern.compile("^([^-]+)-");

    // parent module
    private final BspDecompiler parent;
    
    // sub-modules
    private final BrushSource brushsrc;
    private final FaceSource facesrc;
    private final TextureSource texsrc;

    // list of areaportal brush ids
    private Set<Integer> apBrushes = new HashSet<Integer>();
    
    // cached brush side windings
    private Winding[] bsw;

    // overlay target names
    private Map<Integer, String> overlayNames = new HashMap<Integer, String>();

    // settings
    private int maxCubemapSides = 8;
    private int maxOverlaySides = 64;

    public EntitySource(BspDecompiler parent) {
        super(parent);

        this.parent = parent;
        this.brushsrc = parent.getBrushSource();
        this.facesrc = parent.getFaceSource();
        this.texsrc = parent.getTextureSource();
        
        bsw = new Winding[bsp.brushSides.size()];
        
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
        boolean fixRot = config.isFixEntityRotation() && instances;
        
        boolean brushMode = config.isBrushMode();

        for (Entity ent : bsp.entities) {
            final String className = ent.getClassName();
            
            // don't write the worldspawn here
            if (className.equals("worldspawn")) {
                continue;
            }
            
            // these two classes need special attention
            final boolean isAreaportal = className.startsWith("func_areaportal");
            final boolean isOccluder = className.equals("func_occluder");
            
            // areaportals and occluders don't have a "model" key, take that
            // into account
            final boolean hasBrush = ent.getModelNum() > 0 || isAreaportal || isOccluder;
            
            // skip point entities?
            if (!config.isWritePointEntities() && !hasBrush) {
                continue;
            }
            
            // skip brush entities?
            if (!config.isWriteBrushEntities() && hasBrush) {
                continue;
            }

            // skip areaportals?
            if (!config.isWriteAreaportals() && isAreaportal) {
                continue;
            }

            // skip occluders?
            if (!config.isWriteOccluders() && isOccluder) {
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

            writer.start("entity");
            writer.put("id", parent.nextBrushID());

            // get areaportal numbers
            int portalNum = -1;
            if (isAreaportal) {
                String portalNumString = ent.getValue("portalnumber");

                // extract portal number
                if (portalNumString != null) {
                    try {
                        portalNum = Integer.valueOf(portalNumString);
                    } catch(NumberFormatException ex) {
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
                if (brushMode) {
                    brushsrc.writeModel(modelNum, origin, angles);
                } else {
                    facesrc.writeModel(modelNum, origin, angles);
                }
            } else {
                // try to find the areaportal brush
                if (isAreaportal && portalNum != -1) {
                    int portalBrushNum = -1;

                    // find brushes in brush mode only
                    if (brushMode) {
                        portalBrushNum = findAreaportalBrush(portalNum);
                    }

                    if (portalBrushNum == -1) {
                        // no brush found, write areaportal polygon directly
                        facesrc.writeAreaportal(portalNum);
                    } else {
                        // don't rotate or move areaportal brushes, they're always
                        // positioned correctly
                        brushsrc.writeBrush(portalBrushNum);
                    }
                }

                // always write occluder polygons directly
                if (isOccluder && occluderNum != -1) {
                    facesrc.writeOccluder(occluderNum);
                }
            }
            
            // find instance prefix and add it to a visgroup
            if (instances && ent.getTargetName() != null) {
                Matcher m = INSTANCE_PREFIX.matcher(ent.getTargetName());

                if (m.find()) {
                    parent.writeVisgroup(m.group(1));
                }
            }

            writer.end("entity");
        }
    }

    /**
     * Writes all func_detail entities
     */
    public void writeDetails() {
        L.info("Writing func_details");

        for (int i = 0; i < bsp.brushes.size(); i++) {
            DBrush brush = bsp.brushes.get(i);

            // is a detail brush?
            if (!brush.isSolid() || !brush.isDetail()) {
                continue;
            }

            writer.start("entity");
            writer.put("id", parent.nextBrushID());
            writer.put("classname", "func_detail");
            brushsrc.writeBrush(i);
            writer.end("entity");
        }
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
                o.uvpoints[j] = o.uvpoints[j].setAxis(2, 0);
            }

            Vector3f vbasis = o.basisNormal.cross(ubasis).normalize();

            if (vflip) {
                vbasis = vbasis.scalar(-1);
            }

            // write VMF
            writer.start("entity");
            writer.put("id", parent.nextBrushID());
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
            
            Set<Integer> sides = new HashSet<Integer>();
            int faceCount = o.getFaceCount();
            
            if (config.isBrushMode()) {
                Set<Integer> origFaces = new HashSet<Integer>();
                 
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
                    
                    if (facesrc.faceToID.containsKey(iface)) {
                        // try origface
                        int ioface = bsp.faces.get(iface).origFace;
                        if (facesrc.origFaceToID.containsKey(ioface)) {
                            sides.add(facesrc.faceToID.get(ioface));
                        }
                    } else {
                        sides.add(facesrc.faceToID.get(iface));
                    }
                }
            }
            
            // write brush side list
            StringBuilder sb = new StringBuilder();

            for (Integer side : sides) {
                sb.append(side);
                sb.append(" ");
            }

            writer.put("sides", sb.toString());

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

        Map<Vector3f, String> lightingOrigins = new LinkedHashMap<Vector3f, String>();
        
        for (DStaticProp pst : bsp.staticProps) {
 
            writer.start("entity");
            writer.put("id", parent.nextBrushID());
            writer.put("classname", "prop_static");
            writer.put("origin", pst.origin);
            writer.put("angles", pst.angles);
            writer.put("skin", pst.skin);
            writer.put("fademindist", pst.fademin == 0 ? -1 : pst.fademin);
            writer.put("fademaxdist", pst.fademax);
            writer.put("solid", pst.solid);
            writer.put("model", bsp.staticPropName.get(pst.propType));
            
            // store coordinates and targetname of the lighing origin for later
            if (pst.usesLightingOrigin()) {
                String infoLightingName;

                if (lightingOrigins.containsKey(pst.lightingOrigin)) {
                    infoLightingName = lightingOrigins.get(pst.lightingOrigin);
                } else {
                    infoLightingName = "sprp_lighting_" + lightingOrigins.size();
                    lightingOrigins.put(pst.lightingOrigin, infoLightingName);
                }

                writer.put("lightingorigin", infoLightingName);
            }
            
            writer.put("disableshadows", pst.hasNoShadowing());
            
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
            
            if (pst instanceof DStaticPropV7) {
                DStaticPropV7 pst7 = (DStaticPropV7) pst;
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
            
            if (pst instanceof DStaticPropShip) {
                writer.put("targetname", ((DStaticPropShip) pst).targetname);
            }
            
            writer.end("entity");
        }

        // write lighting origins
        for (Vector3f origin : lightingOrigins.keySet()) {
            writer.start("entity");
            writer.put("id", parent.nextBrushID());
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
            writer.put("id", parent.nextBrushID());
            writer.put("classname", "env_cubemap");
            writer.put("origin", new Vector3f(cm.origin[0], cm.origin[1], cm.origin[2]));
            writer.put("cubemapsize", cm.size);
            
            // FIXME: results are too bad, find a better way
            Set<Integer> sideList = texsrc.getBrushSidesForCubemap(i);

            if (sideList != null) {
                int cmSides = sideList.size();

                if (cmSides > maxCubemapSides) {
                    L.log(Level.FINER, "Cubemap {0} has too many sides: {1}",
                            new Object[]{i, sideList});
                }

                // write list of brush sides that use this cubemap
                if (cmSides > 0 && cmSides < maxCubemapSides) {
                    StringBuilder sb = new StringBuilder();

                    for (int sideId : sideList) {
                        sb.append(sideId);
                        sb.append(" ");
                    }

                    // delete last space
                    sb.deleteCharAt(sb.length() - 1);

                    writer.put("sides", sb.toString());
                }
            }

            writer.end("entity");
        }
    }

    private int findAreaportalBrush(int portalnum) {
        // do we have areaportals at all?
        if (bsp.areaportals.isEmpty()) {
            return -1;
        }

        DAreaportal ap = null;

        // each portal key has two dareaportal_t's, but their geometry always
        // seems to be identical, so just pick the first one we get
        for (DAreaportal areaportal : bsp.areaportals) {
            if (areaportal.portalKey == portalnum) {
                ap = areaportal;
                break;
            }
        }

        // have we found something for that key?
        if (ap == null) {
            L.log(Level.FINER, "No portal geometry for portal key {0}", portalnum);
            return -1;
        }

        // create areaportal winding
        Winding wp = Winding.windFromAreaportal(bsp, ap);

        for (int i = 0; i < bsp.brushes.size(); i++) {
            DBrush brush = bsp.brushes.get(i);

            // considered brushes must be flagged as areaportal
            if (!brush.isAreaportal()) {
                continue;
            }

            // skip already assigned brushes
            if (apBrushes.contains(i)) {
                continue;
            }

            // compare each brush side with areaportal face
            for (int j = 0; j < brush.numside; j++) {
                int ibs = brush.fstside + j;
                
                // create brush side winding
                if (bsw[ibs] == null) {
                    bsw[ibs] = Winding.windFromSide(bsp, brush, j);
                }

                // compare windings
                if (bsw[ibs].matches(wp)) {
                    L.log(Level.FINER, "Brush {0} for portal key {1}",
                            new Object[]{i, portalnum});

                    // add as assigned brush
                    apBrushes.add(i);

                    return i;
                }
            }
        }

        // nothing found :(
        L.log(Level.FINER, "No brush for portal key {0}", portalnum);

        return -1;
    }

    private void findOverlayFaces(int ioverlay, int ioface, Set<Integer> sides) {
        // no original face information available? then we're done here...
        if (bsp.origFaces.isEmpty()) {
            return;
        }
        
        // don't add more if we already hit the maximum
        if (sides.size() >= maxOverlaySides) {
            return;
        }
        
        int sidesPrev = sides.size();
        
        DFace origFace = bsp.origFaces.get(ioface);
        
        // use sideid of displacement, if existing
        if (origFace.dispInfo != -1) {
            if (facesrc.dispinfoToID.containsKey(origFace.dispInfo)) {
                Integer side = facesrc.dispinfoToID.get(origFace.dispInfo);
                L.log(Level.FINER, "O: {0} D: {1} id: {2}",
                        new Object[]{ioverlay, origFace.dispInfo, side});
                sides.add(side);
            }
            
            return;
        }
        
        // create winding from original face
        Winding wof = Winding.windFromFace(bsp, origFace);

        for (int i = 0; i < bsp.brushes.size(); i++) {
            DBrush brush = bsp.brushes.get(i);

            for (int j = 0; j < brush.numside; j++) {
                int ibs = brush.fstside + j;
                Integer side = brushsrc.brushSideToID.get(ibs);
                DBrushSide bs = bsp.brushSides.get(ibs);

                // create winding from brush side
                if (bsw[ibs] == null) {
                    bsw[ibs] = Winding.windFromSide(bsp, brush, j);
                }

                // check for valid face: same plane, same texinfo, same geometry
                if (side == null || origFace.pnum != bs.pnum || origFace.texinfo != bs.texinfo
                        || !bsw[ibs].matches(wof)) {
                    continue;
                }

                L.log(Level.FINER, "O: {0} OF: {1} B: {2} BS: {3} id: {4}",
                        new Object[]{ioverlay, ioface, i, ibs, side});

                // make sure we won't have too many brush sides for that overlay
                if (sides.size() >= maxOverlaySides) {
                    L.log(Level.WARNING, "Too many brush sides for overlay {0}", ioverlay);
                    return;
                }

                sides.add(side);
            }
        }

        if (sides.size() == sidesPrev) {
            L.log(Level.FINER, "O: {0} OF: {1} no match", new Object[]{ioverlay, ioface});
        }
    }

    private void processEntities() {
        for (Entity ent : bsp.entities) {
            // fix worldspawn
            if (ent.getClassName().equals("worldspawn")) {
                // remove values that are unknown to Hammer
                ent.removeValue("world_mins");
                ent.removeValue("world_maxs");

                // rebuild mapversion
                if (!ent.hasKey("mapversion")) {
                    ent.setValue("mapversion", bspFile.getMapRev());
                }
            }
            
            // func_simpleladder entities are used by the engine only and won't
            // work when re-compiling, so replace them with empty func_ladder's
            // instead.
            if (ent.getClassName().equals("func_simpleladder")) {
                int modelNum = ent.getModelNum();

                ent.clear();
                ent.setClassName("func_ladder");
                ent.setModelNum(modelNum);
            }

            // fix light entities (except for dynamic lights)
            if (ent.getClassName().startsWith("light")
                    && !ent.getClassName().equals("light_dynamic")) {
                fixLightEntity(ent);
            }
            
            // add cameras based on info_player_* positions
            if (ent.getClassName().startsWith("info_player_")) {
                createCamera(ent);
            }
            
            // remove hammerid, unless debug is enabled
            if (!config.isDebug()) {
                ent.removeValue("hammerid");
            }
        }
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

        parent.getCameras().add(new Camera(pos, look));
    }
}

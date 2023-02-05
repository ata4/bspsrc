/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.modules.geom;

import info.ata4.bsplib.BspFileReader;
import info.ata4.bsplib.struct.*;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bspsrc.BspSourceConfig;
import info.ata4.bspsrc.VmfWriter;
import info.ata4.bspsrc.modules.ModuleDecompile;
import info.ata4.bspsrc.modules.VmfMeta;
import info.ata4.bspsrc.modules.texture.*;
import info.ata4.bspsrc.util.Winding;
import info.ata4.bspsrc.util.WindingFactory;
import info.ata4.log.LogUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Decompiling module to write brushes from the LUMP_FACES lump.
 * 
 * Based on several face building methods from Vmex
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FaceSource extends ModuleDecompile {

    // logger
    private static final Logger L = LogUtils.getLogger();

    //BSP=VMF 6=9  2=1  0=0
    private static final byte[] TRICONV = {0, 0, 1, 0, 0, 0, 9};

    // epsilon for area comparison slop, in mu^2
    private static final float AREA_EPS = 1.0f;

    // sub-modules
    private final BspSourceConfig config;
    private final TextureSource texsrc;
    private final VmfMeta vmfmeta;

    // mapped original faces
    public Map<Integer, Set<Integer>> origFaceToSplitFace = new HashMap<>();

    // set of face indices that are undersized
    private Set<Integer> undersizedFaces = new HashSet<>();

    // current offset in multiblend lump
    private int multiblendOffset;

    public FaceSource(BspFileReader reader, VmfWriter writer, BspSourceConfig config,
            TextureSource texsrc, VmfMeta vmfmeta) {
        super(reader, writer);

        this.config = config;
        this.texsrc = texsrc;
        this.vmfmeta = vmfmeta;

        if (bsp.origFaces.isEmpty()) {
            // fix invalid origFace indices when no original faces are available
            for (DFace face : bsp.faces) {
                face.origFace = -1;
            }
        } else {
            // sync texinfo index, this seems to be required since BSP v20
            for (DFace face : bsp.faces) {
                if (face.origFace != -1) {
                    bsp.origFaces.get(face.origFace).texinfo = face.texinfo;
                }
            }
        }
    }

    /**
     * Writes all split faces
     */
    public void writeFaces() {
        L.info("Writing split faces");

        DModel model = bsp.models.get(0); // Model 0 = world brushes

        // loop through all faces in M0
        for (int i = 0; i < model.numface; i++) {
            writeFace(model.fstface + i, false);
        }
    }

    /**
     * Writes all original faces. When an original face doesn't exist, it will
     * leave gaps in the brush structure.
     */
    public void writeOrigFaces() {
        L.info("Writing original faces");

        // set of face indices that are already written
        Set<Integer> writtenFaces = new HashSet<>();

        DModel model = bsp.models.get(0); // Model 0 = world brushes

        // loop through all faces in M0
        for (int i = 0; i < model.numface; i++) {
            int iface = model.fstface + i;
            DFace face = bsp.faces.get(iface);

            // skip if the original face is missing
            if (face.origFace < 0) {
                continue;
            }

            // the original face corresponding to this
            int iorigface = face.origFace;

            // don't write a face more than once
            if (writtenFaces.contains(iorigface)) {
                continue;
            }

            writeFace(iorigface, true);
            writtenFaces.add(iorigface);
        }
    }

    /**
     * Writes all original faces, unless one is undersized, in which case write
     * its split faces.
     */
    public void writeOrigFacesPlus() {
        createFaceMapping();

        // set of face indices that are already written
        Set<Integer> writtenFaces = new HashSet<>();

        L.info("Writing original faces where possible");

        DModel model = bsp.models.get(0); // Model 0 = world brushes
        int sfaces = 0; // number of written split faces

        for (int i = 0; i < model.numface; i++) {
            int iface = model.fstface + i;
            DFace face = bsp.faces.get(iface);

            if (face.origFace >= 0) {
                int iorigface = face.origFace;

                // don't write a face more than once
                if (writtenFaces.contains(iorigface)) {
                    continue;
                }

                if (undersizedFaces.contains(iorigface)) {
                    // oface is undersized! write it as split faces
                    sfaces++;

                    Set<Integer> faces = origFaceToSplitFace.get(face.origFace);

                    // iterate through the corresponding faces
                    for (Integer iface2 : faces) {
                        writeFace(iface2, false);
                    }

                    if (L.isLoggable(Level.FINEST)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("OF ").append(face.origFace).append(": ");

                        for (Integer findex : faces) {
                            sb.append(findex).append(' ');
                        }

                        L.finest(sb.toString());
                    }
                } else {
                    // write this oface as flat
                    writeFace(face.origFace, true);
                }

                writtenFaces.add(iorigface);
            } else {
                // write the face directly
                writeFace(iface, false);
            }
        }

        L.log(Level.INFO, "{0} original faces were written as split faces", sfaces);
    }

    /**
     * Writes faces with displacement data only
     */
    public void writeDispFaces() {
        L.info("Writing displacements");

        if (bsp.dispinfos == null || bsp.dispinfos.isEmpty()) {
            // no displacements, don't bother searching for matching faces
            return;
        }

        for (int i = 0; i < bsp.faces.size(); i++) {
            if (bsp.faces.get(i).dispInfo != -1) {
                writeFace(i, false);
            }
        }
    }

    public void writeModel(int imodel, Vector3f origin, Vector3f angles) {
        DModel model;

        try {
            model = bsp.models.get(imodel);
        } catch (IndexOutOfBoundsException ex) {
            L.log(Level.WARNING, "Invalid model index {0}", imodel);
            return;
        }

        for (int i = 0; i < model.numface; i++) {
            writeFace(model.fstface + i, false, origin, angles);
        }
    }

    public void writeModel(int imodel) {
        writeModel(imodel, null, null);
    }

    /**
     * Writes a flat face as a brush.
     */
    public void writeFace(int iface, boolean orig, Vector3f origin, Vector3f angles) {
        DFace face = orig ? bsp.origFaces.get(iface) : bsp.faces.get(iface);

        if (face.numedge < 2) {
            // 0 or 1 edges? Something must be wrong
            return;
        }

        Winding wind = WindingFactory.fromFace(bsp, face);

        // translate to origin
        if (origin != null) {
            wind = wind.translate(origin);
        }

        // rotate
        if (angles != null) {
            wind = wind.rotate(angles);
        }

        // calculate plane vectors
        Vector3f[] plane = wind.buildPlane();

        Vector3f e1 = plane[0];
        Vector3f e2 = plane[1];
        Vector3f e3 = plane[2];

        if (!e1.isValid() || !e2.isValid() || !e3.isValid()) {
            L.log(Level.WARNING, "Face with wind {0} is invalid", wind);
            return;
        }

        // calculate plane normal
        Vector3f ev12 = e2.sub(e1);
        Vector3f ev13 = e3.sub(e1);
        Vector3f normal = ev12.cross(ev13).normalize();

        if (normal.isNaN() || normal.isInfinite()) {
            // TODO: is there a way to fix/avoid this?
            L.log(Level.FINE, "Bad normal: {0} x {1}", new Object[]{ev12, ev13});
            return;
        }

        writer.start("solid");
        writer.put("id", vmfmeta.getUID());

        // write metadata for debugging
        if (config.isDebug()) {
            writer.start("bspsrc_debug");
            writer.put("face_index", iface);
            writer.put("normal", normal);
            writer.put("winding", wind.toString());

            if (face.texinfo != -1) {
                writer.put("texinfo_index", face.texinfo);
                writer.put("texinfo_flags", bsp.texinfos.get(face.texinfo).flags.toString());
            }
            writer.end("bspsrc_debug");
        }

        int sideID = vmfmeta.getUID();

        // map face index to brush side ID
        if (orig) {
            vmfmeta.setOrigFaceUID(iface, sideID);
        } else {
            vmfmeta.setFaceUID(iface, sideID);
        }

        // build texture
        TextureBuilder tb = texsrc.getTextureBuilder();

        tb.setOrigin(origin);
        tb.setAngles(angles);
        tb.setNormal(normal);

        tb.setTexinfoIndex(face.texinfo);

        Texture texture = tb.build();

        // set face texture string
        if (!config.faceTexture.isEmpty()) {
            texture.setOverrideTexture(config.faceTexture);
        }

        // add side id to cubemap side list
        if (texture.getData() != null) {
            texsrc.addBrushSideID(texture.getData().texname, sideID);
        }

        writer.start("side");
        writer.put("id", sideID);
        writer.put("plane", e1, e2, e3);
        writer.put("smoothing_groups", face.smoothingGroups);
        writer.put(texture);

        boolean disp = face.dispInfo != -1;

        // write displacement?
        if (disp && config.writeDisp) {
            // map face index to brush side ID
            vmfmeta.setDispInfoUID(face.dispInfo, sideID);
            // write dispinfo section
            writeDisplacement(face.dispInfo);
        }

        writer.end("side");

        // set back face texture string
        if (!config.backfaceTexture.isEmpty()) {
            texture.setOverrideTexture(config.backfaceTexture);
        }

        // write prismatic back faces for displacements, pyramidal otherwise
        if (disp) {
            writePrismBack(wind, texture);
        } else {
            writePyramBack(wind, texture);
        }

        writer.end("solid");
    }

    public void writeFace(int iface, boolean orig) {
        writeFace(iface, orig, null, null);
    }

    /**
     * Writes prismatic back brush sides for a face
     */
    private void writePrismBack(Winding wind, Texture texture, float depth) {
        Vector3f[] plane = wind.buildPlane();

        Vector3f e1 = plane[0];
        Vector3f e2 = plane[1];
        Vector3f e3 = plane[2];

        // calculate plane normal
        Vector3f ev12 = e2.sub(e1);
        Vector3f ev13 = e3.sub(e1);
        Vector3f normal = ev12.cross(ev13).normalize();

        // displace vertices from face in normal direction by depth
        Vector3f bedge = normal.scalar(depth);

        e1 = e1.add(bedge);
        e2 = e2.add(bedge);
        e3 = e3.add(bedge);

        // flip UAxis for back side
        texture.setUAxis(new TextureAxis(texture.getUAxis().axis.scalar(-1)));

        writeBackSide(texture, e1, e2, e3);

        Vector3f tv2 = bedge.normalize();

        // write surrounding sides
        int size = wind.size();

        for (int i = 0; i < size; i++) {
            e1 = wind.get(i);
            e2 = wind.get((i + 1) % size);
            e3 = e1.add(bedge);

            // e2.sub(e1) results in mirrored texture
            Vector3f tv1 = e1.sub(e2).normalize();

            // use null vector if the result is invalid
            if (!tv1.isValid()) {
                tv1 = Vector3f.NULL;
            }

            texture.setUAxis(new TextureAxis(tv1));
            texture.setVAxis(new TextureAxis(tv2));

            writeBackSide(texture, e1, e2, e3);
        }
    }

    private void writePrismBack(Winding wind, Texture texture) {
        writePrismBack(wind, texture, config.backfaceDepth);
    }

    /**
     * Writes pyramidal back brush sides for a face
     */
    private void writePyramBack(Winding wind, Texture texture, float depth) {
        Vector3f[] plane = wind.buildPlane();

        Vector3f e1 = plane[0];
        Vector3f e2 = plane[1];
        Vector3f e3 = plane[2];

        // calculate plane normal
        Vector3f ev12 = e2.sub(e1);
        Vector3f ev13 = e3.sub(e1);
        Vector3f normal = ev12.cross(ev13).normalize();

        // the coords of the barycenter
        e3 = wind.getCenter();

        // displace barycenter in normal direction by depth
        // results in the apex point for the pyramid
        e3 = e3.add(normal.scalar(depth));

        // write pyramid sides
        int size = wind.size();

        for (int i = 0; i < size; i++) {
            e1 = wind.get(i);
            e2 = wind.get((i + 1) % size);

            writeBackSide(texture, e1, e2, e3);
        }
    }

    private void writePyramBack(Winding wind, Texture texture) {
        writePyramBack(wind, texture, config.backfaceDepth);
    }

    public void writeAreaportal(int portalKey) {
        for (DAreaportal ap : bsp.areaportals) {
            if (ap.portalKey == portalKey) {
                writeAreaportal(ap);
                // write only once, even though there are two DAreaportal's with
                // that key, their geometries are identical
                return;
            }
        }
        L.warning("Tried to write non existing areaportal with portalkey " + portalKey);
    }

    public void writeAreaportal(DAreaportal ap) {
        Winding wind = WindingFactory.fromAreaportal(bsp, ap);
        // TODO: extrude polygon in the correct direction, currently it seems to be random?
        writePolygon(wind, ToolTexture.AREAPORTAL, true);
    }

    public void writeOccluder(int occluderKey) {
        try {
            writeOccluder(bsp.occluderDatas.get(occluderKey));
        } catch (IndexOutOfBoundsException ex) {
            L.log(Level.WARNING, "Invalid occluder key {0}", occluderKey);
        }
    }

    public void writeOccluder(DOccluderData od) {
        for (int i = 0; i < od.polycount; i++) {
            DOccluderPolyData opd = bsp.occluderPolyDatas.get(od.firstpoly + i);
            Winding wind = WindingFactory.fromOccluder(bsp, opd);
            // extrude by 8 units instead of one, the skip sides are ignored anyway | Seems to be no longer the case as creating occluders in csgo doesn't even work with skip anymore - changed back to nodraw and 1 unit extrude
            writePolygon(wind, ToolTexture.OCCLUDER, ToolTexture.NODRAW, true, 1);
        }
    }

    /**
     * Writes a brush from raw polygon data.
     * 
     * @param wind winding of the polygon
     * @param frontMaterial texture string to use on front brush side
     * @param backMaterial texture string to use on back brush sides
     * @param prism use prismatic back sides if true, pyramidal otherwise
     * @param depth extrude polygon by this depth
     */
    public void writePolygon(Winding wind, String frontMaterial, String backMaterial, boolean prism, float depth) {
        if (wind.isEmpty() || wind.size() < 3) {
            return;
        }

        Vector3f[] plane = wind.buildPlane();

        Vector3f e1 = plane[0];
        Vector3f e2 = plane[1];
        Vector3f e3 = plane[2];

        if (!e1.isValid() || !e2.isValid() || !e3.isValid()) {
            L.log(Level.WARNING, "Areaportal with wind {0} is invalid", wind);
            return;
        }

        // calculate plane normal
        Vector3f ev12 = e2.sub(e1);
        Vector3f ev13 = e3.sub(e1);
        Vector3f normal = ev12.cross(ev13).normalize();

        if (normal.isNaN() || normal.isInfinite()) {
            // TODO: is there a way to fix/avoid this?
            L.log(Level.FINE, "Bad normal: {0} x {1}", new Object[]{ev12, ev13});
            return;
        }

        writer.start("solid");
        writer.put("id", vmfmeta.getUID());

        int sideID = vmfmeta.getUID();

        // build texture
        TextureBuilder tb = texsrc.getTextureBuilder();
        tb.setNormal(normal);

        Texture texture = tb.build();
        texture.setOriginalTexture(frontMaterial);

        writer.start("side");
        writer.put("id", sideID);
        writer.put("plane", e1, e2, e3);
        writer.put(texture);
        writer.end("side");

        texture.setOriginalTexture(backMaterial);

        if (prism) {
            writePrismBack(wind, texture, depth);
        } else {
            writePyramBack(wind, texture, depth);
        }

        writer.end("solid");
    }

    public void writePolygon(Winding wind, String frontMaterial, String backMaterial, boolean prism) {
        writePolygon(wind, frontMaterial, backMaterial, prism, config.backfaceDepth);
    }

    public void writePolygon(Winding wind, String material, boolean prism, float depth) {
        writePolygon(wind, material, material, prism, depth);
    }

    public void writePolygon(Winding wind, String material, boolean prism) {
        writePolygon(wind, material, material, prism);
    }

    /**
     * Write a brush side with the given texture and plane
     */
    private void writeBackSide(Texture texture, Vector3f e1, Vector3f e2, Vector3f e3) {
        writer.start("side");
        writer.put("id", vmfmeta.getUID());
        writer.put("plane", e1, e3, e2);
        writer.put("smoothing_groups", 0);
        writer.put(texture);
        writer.end("side");
    }

    /**
     * Writes dispinfo data for a brush side
     *
     * @param idispinfo dispinfo index
     */
    public void writeDisplacement(int idispinfo) {
        DDispInfo di = bsp.dispinfos.get(idispinfo);

        Map<String, String> normalMap = new LinkedHashMap<>();
        Map<String, String> distanceMap = new LinkedHashMap<>();
        Map<String, String> alphaMap = new LinkedHashMap<>();
        Map<String, String> triangleTagMap = new LinkedHashMap<>();

        Map<String, String> multiBlendMap = new LinkedHashMap<>();
        Map<String, String> alphaBlendMap = new LinkedHashMap<>();

        List<Map<String, String>> multiBlendColorMaps = new ArrayList<>(DDispMultiBlend.MAX_MULTIBLEND_CHANNELS);
        for (int i = 0; i < DDispMultiBlend.MAX_MULTIBLEND_CHANNELS; i++) {
            multiBlendColorMaps.add(new LinkedHashMap<String, String>());
        }

        StringBuilder normalSb = new StringBuilder();
        StringBuilder distanceSb = new StringBuilder();
        StringBuilder alphaSb = new StringBuilder();
        StringBuilder multiblendSb = new StringBuilder();
        StringBuilder alphablendSb = new StringBuilder();
        List<StringBuilder> multiblendColorSbs = new ArrayList<>(DDispMultiBlend.MAX_MULTIBLEND_CHANNELS);
        for (int i = 0; i < DDispMultiBlend.MAX_MULTIBLEND_CHANNELS; i++) {
            multiblendColorSbs.add(new StringBuilder());
        }
        StringBuilder triangleTagSb = new StringBuilder();
        StringBuilder allowedVertSb = new StringBuilder();

        final int vertcount = di.getVertexCount();
        final int psize = di.getPowerSize();

        final boolean hasMultiBlend = !bsp.dispmultiblend.isEmpty() && di.hasMultiBlend();

        // build vertex related strings
        for (int i = 0; i < vertcount; i++) {
            DDispVert dv = bsp.dispverts.get(di.dispVertStart + i);
            DDispMultiBlend dmb = null;
            if (hasMultiBlend) {
                dmb = bsp.dispmultiblend.get(multiblendOffset + i);
            }

            // normals
            normalSb.append(dv.vector.x);
            normalSb.append(" ");
            normalSb.append(dv.vector.y);
            normalSb.append(" ");
            normalSb.append(dv.vector.z);

            // distance
            distanceSb.append(dv.dist);

            // alpha
            alphaSb.append(dv.alpha);

            if (hasMultiBlend) {
                // multiblend
                multiblendSb.append(dmb.multiblend.x);
                multiblendSb.append(" ");
                multiblendSb.append(dmb.multiblend.y);
                multiblendSb.append(" ");
                multiblendSb.append(dmb.multiblend.z);
                multiblendSb.append(" ");
                multiblendSb.append(dmb.multiblend.w);

                alphablendSb.append(dmb.alphablend.x);
                alphablendSb.append(" ");
                alphablendSb.append(dmb.alphablend.y);
                alphablendSb.append(" ");
                alphablendSb.append(dmb.alphablend.z);
                alphablendSb.append(" ");
                alphablendSb.append(dmb.alphablend.w);

                for (int j = 0; j < dmb.multiblendcolors.length; j++) {
                    StringBuilder mbcsb = multiblendColorSbs.get(j);
                    mbcsb.append(dmb.multiblendcolors[j].x);
                    mbcsb.append(" ");
                    mbcsb.append(dmb.multiblendcolors[j].y);
                    mbcsb.append(" ");
                    mbcsb.append(dmb.multiblendcolors[j].z);
                }
            }

            // check for new row
            if (i % (psize + 1) == psize) {
                normalMap.put("row" + normalMap.size(), normalSb.toString());
                distanceMap.put("row" + distanceMap.size(), distanceSb.toString());
                alphaMap.put("row" + alphaMap.size(), alphaSb.toString());

                normalSb.setLength(0);
                distanceSb.setLength(0);
                alphaSb.setLength(0);

                // multiblend
                if (hasMultiBlend) {
                    multiBlendMap.put("row" + multiBlendMap.size(), multiblendSb.toString());
                    alphaBlendMap.put("row" + alphaBlendMap.size(), alphablendSb.toString());
                    for (int j = 0; j < dmb.multiblendcolors.length; j++) {
                        Map<String, String> multiBlendColorMap = multiBlendColorMaps.get(j);
                        multiBlendColorMap.put("row" + multiBlendColorMap.size(),
                                multiblendColorSbs.get(j).toString());
                    }

                    multiblendSb.setLength(0);
                    alphablendSb.setLength(0);
                    for (int j = 0; j < dmb.multiblendcolors.length; j++) {
                        multiblendColorSbs.get(j).setLength(0);
                    }
                }
            } else {
                normalSb.append(" ");
                distanceSb.append(" ");
                alphaSb.append(" ");

                if (hasMultiBlend) {
                    multiblendSb.append(" ");
                    alphablendSb.append(" ");

                    for (int j = 0; j < dmb.multiblendcolors.length; j++) {
                        multiblendColorSbs.get(j).append(" ");
                    }
                }
            }
        }

        // count up multiblend index
        if (hasMultiBlend) {
            multiblendOffset += vertcount;
        }

        // build triangle tags
        int tcount = di.getTriangleTagCount();

        for (int i = 0; i < tcount; i++) {
            int dt = bsp.disptris.get(di.dispTriStart + i).tags;

            if (dt < 0 || dt > 6) {
                dt = 0;
            }

            triangleTagSb.append(TRICONV[dt]);

            // check for new row
            if (i % 2 * psize == 2 * psize - 1) {
                triangleTagMap.put("row" + triangleTagMap.size(), triangleTagSb.toString());
                triangleTagSb.setLength(0);
            } else {
                triangleTagSb.append(" ");
            }
        }


        // build allowed vertices
        for (int i = 0; i < di.allowedVerts.length; i++) {
            allowedVertSb.append(di.allowedVerts[i]);

            if (i < di.allowedVerts.length - 1) {
                allowedVertSb.append(" ");
            }
        }

        // write VMF data
        writer.start("dispinfo");

        if (config.isDebug()) {
            writer.put("bspsrc_dispinfo_index", idispinfo);
        }

        writer.put("power", di.power);
        writer.put("startposition", di.startPos, 2);
        writer.put("flags", di.getSurfaceFlags());
        writer.put("elevation", 0);
        writer.put("subdiv", 0);

        writer.start("normals");
        writer.put(normalMap);
        writer.end("normals");

        writer.start("distances");
        writer.put(distanceMap);
        writer.end("distances");

        writer.start("alphas");
        writer.put(alphaMap);
        writer.end("alphas");

        writer.start("triangle_tags");
        writer.put(triangleTagMap);
        writer.end("triangle_tags");

        writer.start("allowed_verts");
        writer.put("10", allowedVertSb.toString());
        writer.end("allowed_verts");

        // Multiblend
        if (hasMultiBlend) {
            writer.start("multiblend");
            writer.put(multiBlendMap);
            writer.end("multiblend");

            writer.start("alphablend");
            writer.put(alphaBlendMap);
            writer.end("alphablend");

            for (int j = 0; j < DDispMultiBlend.MAX_MULTIBLEND_CHANNELS; j++) {
                writer.start("multiblend_color_" + j);
                writer.put(multiBlendColorMaps.get(j));
                writer.end("multiblend_color_" + j);
            }
        }

        writer.end("dispinfo");
    }

    /**
     * Builds a HashSet array of all faces corresponding to i'th orig face.
     * Also calculates the area of ofaces.
     */
    private void createFaceMapping() {        
        L.info("Building split face to original face maps");

        // look at every face
        for (int i = 0; i < bsp.faces.size(); i++) {
            int o = bsp.faces.get(i).origFace;

            // must check for no face correspondence
            if (o == -1) {
                continue;
            }

            Set<Integer> faceSet;

            if (origFaceToSplitFace.containsKey(o)) {
                faceSet = origFaceToSplitFace.get(o);
            } else {
                faceSet = new HashSet<>();
                origFaceToSplitFace.put(o, faceSet);
            }

            //  add this face to the set
            faceSet.add(i);
        }

        L.info("Building original face areas");

        // look at every oface
        for (int i = 0; i < bsp.origFaces.size(); i++) {
            DFace origFace = bsp.origFaces.get(i);

            // recalculate face area when required
            if (origFace.area == 0) {
                Winding wind = WindingFactory.fromFace(bsp, origFace);
                origFace.area = wind.getArea();
            }

            if (L.isLoggable(Level.FINEST)) {
                L.log(Level.FINEST, "OF {0}: area {1}", new Object[]{i, origFace.area});
            }

            // area of face components
            float carea = 0;

            Set<Integer> faceSet = origFaceToSplitFace.get(i);

            // iterate through the corresponding split faces
            for (Integer face : faceSet) {
                // add up the areas of all split faces
                carea += bsp.faces.get(face).area;
            }

            // components are bigger, within slop
            if (carea > origFace.area + AREA_EPS) {
                undersizedFaces.add(i); // mark the oface

                if (L.isLoggable(Level.FINEST)) {
                    L.log(Level.FINEST, "OF {0} is undersized: {1}>{2}",
                            new Object[]{i, carea, origFace.area});
                }
            }
        }
    }
}

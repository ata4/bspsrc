/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppDB;
import info.ata4.bsplib.app.SourceAppID;
import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.io.EntityInputStream;
import info.ata4.bsplib.lump.*;
import info.ata4.bsplib.struct.*;
import info.ata4.util.EnumConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 * All-purpose BSP file and lump reader.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspFileReader {
    private static final Logger L = Logger.getLogger(BspFileReader.class.getName());

    // BSP headers and data
    private BspFile bspFile;
    private BspData bsp = new BspData();
    private int appID;

    // statistical stuff
    private Set<String> entityClasses = new TreeSet<String>();

    public BspFileReader(BspFile bspFile) throws IOException {
        this.bspFile = bspFile;
        this.appID = bspFile.getSourceApp().getAppID();
        
        if (bspFile.getFile() == null) {
            // "Gah! Hear me, man? Gah!"
            throw new BspException("BSP file is unloaded");
        }
        
        // uncompress all lumps first
        if (bspFile.isCompressed()) {
            bspFile.uncompress();
        }
    }

    /**
     * Loads all supported lumps
     */
    public void loadAll() {
        loadEntities();
        loadVertices();
        loadEdges();
        loadFaces();
        loadOriginalFaces();
        loadModels();
        loadSurfaceEdges();
        loadOccluders();
        loadTexInfo();
        loadTexData();
        loadStaticProps();
        loadCubemaps();
        loadPlanes();
        loadBrushes();
        loadBrushSides();
        loadAreaportals();
        loadClipPortalVertices();
        loadDispInfos();
        loadDispVertices();
        loadDispTriangleTags();
        loadDispMultiBlend();
        loadNodes();
        loadLeaves();
        loadLeafFaces();
        loadLeafBrushes();
        loadOverlays();
        loadFlags();
    }

    public void loadPlanes() {
        if (bsp.planes != null) {
            return;
        }
        
        bsp.planes = loadLump(LumpType.LUMP_PLANES, DPlane.class);
    }

    public void loadBrushes() {
        if (bsp.brushes != null) {
            return;
        }
        
        bsp.brushes = loadLump(LumpType.LUMP_BRUSHES, DBrush.class);
    }

    public void loadBrushSides() {
        if (bsp.brushSides != null) {
            return;
        }
        
        Class struct = DBrushSide.class;

        if (appID == SourceAppID.VINDICTUS) {
            struct = DBrushSideVin.class;
        } else if (bspFile.getVersion() >= 21 && appID != SourceAppID.LEFT_4_DEAD_2) {
            // newer BSP files have a slightly different struct that is still reported
            // as version 0
            struct = DBrushSideV2.class;
        }

        bsp.brushSides = loadLump(LumpType.LUMP_BRUSHSIDES, struct);
    }

    public void loadVertices() {
        if (bsp.verts != null) {
            return;
        }
        
        bsp.verts = loadLump(LumpType.LUMP_VERTEXES, DVertex.class);
    }

    public void loadClipPortalVertices() {
        if (bsp.clipPortalVerts != null) {
            return;
        }
        
        bsp.clipPortalVerts = loadLump(LumpType.LUMP_CLIPPORTALVERTS, DVertex.class);
    }

    public void loadEdges() {
        if (bsp.edges != null) {
            return;
        }

        Class struct = DEdge.class;

        if (appID == SourceAppID.VINDICTUS) {
            struct = DEdgeVin.class;
        }

        bsp.edges = loadLump(LumpType.LUMP_EDGES, struct);
    }

    private void loadFaces(boolean orig) {
        if ((orig && bsp.origFaces != null) || (!orig && bsp.faces != null)) {
            return;
        }
        
        Class struct = DFace.class;
        
        if (appID == SourceAppID.VAMPIRE_BLOODLINES) {
            struct = DFaceVTMB.class;
        } else if (appID == SourceAppID.VINDICTUS) {
            struct = DFaceVin.class;
        } else {
            switch (bspFile.getVersion()) {
                case 17:
                    struct = DFaceBSP17.class;
                    break;

                case 18:
                    struct = DFaceBSP18.class;
                    break;
            }
        }
        
        if (orig) {
            bsp.origFaces = loadLump(LumpType.LUMP_ORIGINALFACES, struct);
        } else {
            // use LUMP_FACES_HDR if LUMP_FACES is empty
            if (getLump(LumpType.LUMP_FACES).getLength() == 0) {
                bsp.faces = loadLump(LumpType.LUMP_FACES_HDR, struct);
            } else {
                bsp.faces = loadLump(LumpType.LUMP_FACES, struct);
            }
        }
    }
    
    public void loadFaces() {
        loadFaces(false);
    }

    public void loadOriginalFaces() {
        loadFaces(true);
    }

    public void loadModels() {
        if (bsp.models != null) {
            return;
        }
        
        Class struct = DModel.class;

        if (appID == SourceAppID.DARK_MESSIAH) {
            struct = DModelDM.class;
        }

        bsp.models = loadLump(LumpType.LUMP_MODELS, struct);
    }

    public void loadSurfaceEdges() {
        if (bsp.surfEdges != null) {
            return;
        }
        
        bsp.surfEdges = loadIntegerLump(LumpType.LUMP_SURFEDGES);
    }

    public void loadStaticProps() {
        if (bsp.staticProps != null && bsp.staticPropName != null) {
            return;
        }

        L.fine("Loading static props");

        GameLump sprpLump = bspFile.getGameLump("sprp");

        if (sprpLump == null) {
            // static prop lump not available
            bsp.staticProps = new ArrayList<DStaticProp>();
            return;
        }

        LumpDataInput lr = sprpLump.getDataInput();

        try {
            final int padsize = 128;
            final int psnames = lr.readInt();
            
            L.log(Level.FINE, "Static prop names: {0}", psnames);
            
            bsp.staticPropName = new ArrayList<String>(psnames);

            for (int i = 0; i < psnames; i++) {
                bsp.staticPropName.add(lr.readString(padsize));
            }

            // model path strings in Zeno Clash
            if (appID == SourceAppID.ZENO_CLASH) {
                int psextra = lr.readInt();
                lr.skipBytes(psextra * padsize);
            }

            // StaticPropLeafLump_t
            int propleaves = lr.readInt();
            
            L.log(Level.FINE, "Static prop leaves: {0}", propleaves);
            
            bsp.staticPropLeaf = new ArrayList<Integer>(propleaves);
            
            for (int i = 0; i < propleaves; i++) {
                bsp.staticPropLeaf.add(lr.readUnsignedShort());
            }

            // StaticPropLump_t
            final int propstatics = lr.readInt();
            
            int sprpver = sprpLump.getVersion();
            Class<? extends DStaticProp> structClass = null;
            
            // special cases where the lump version doesn't specify the correct
            // data structure
            switch (appID) {
                case SourceAppID.THE_SHIP:
                    structClass = DStaticPropShip.class;
                    break;
                    
                case SourceAppID.BLOODY_GOOD_TIME:
                    structClass = DStaticPropBGT.class;
                    break;
                    
                case SourceAppID.ZENO_CLASH:
                    structClass = DStaticPropZC.class;
                    break;
                    
                case SourceAppID.DARK_MESSIAH:
                    structClass = DStaticPropDM.class;
                    break;
                    
                case SourceAppID.DEAR_ESTHER:
                    structClass = DStaticPropDE.class;
                    break;
            }

            // get structure class for the static prop lump version if it's not
            // a special case
            if (structClass == null) {
                try {
                    String className = DStaticProp.class.getName();
                    structClass = (Class<? extends DStaticProp>) Class.forName(className + "V" + sprpver);
                } catch (ClassNotFoundException ex) {
                    structClass = DStaticPropV4.class;
                    L.log(Level.WARNING, "Couldn''t find static prop struct for lump version {0}, using v4 instead", sprpver);
                }
            }
            
            bsp.staticProps = new ArrayList<DStaticProp>(propstatics);
            
            for (int i = 0; i < propstatics; i++) {
                DStaticProp sp = structClass.newInstance();
                
                int pos = lr.position();
                sp.read(lr);
                int size = sp.getSize();
                if (lr.position() - pos != size) {
                    throw new IOException("Bytes read: " + pos + "; expected: " + size);
                }
                
                bsp.staticProps.add(sp);
            }

            L.log(Level.FINE, "Static props: {0}", propstatics);

            lr.checkRemaining();
        } catch (IOException ex) {
            lumpError(sprpLump, ex);
        } catch (InstantiationException ex) {
            L.log(Level.SEVERE, "Lump struct class error", ex);
        } catch (IllegalAccessException ex) {
            L.log(Level.SEVERE, "Lump struct class error", ex);
        }
    }

    public void loadCubemaps() {
        if (bsp.cubemaps != null) {
            return;
        }
        
        bsp.cubemaps = loadLump(LumpType.LUMP_CUBEMAPS, DCubemapSample.class);
    }

    public void loadDispInfos() {
        if (bsp.dispinfos != null) {
            return;
        }
        
        Class struct = DDispInfo.class;
        
        if (appID == SourceAppID.VINDICTUS) {
            struct = DDispInfoVin.class;
        } else if (bspFile.getVersion() == 17 && appID == SourceAppID.HALF_LIFE_2) {
            struct = DDispInfoBSP17.class;
        } else if (bspFile.getVersion() == 22) {
            struct = DDispInfoBSP22.class;
        } else if (bspFile.getVersion() >= 23) {
            struct = DDispInfoBSP23.class;
        }
        
        bsp.dispinfos = loadLump(LumpType.LUMP_DISPINFO, struct);
    }

    public void loadDispVertices() {
        if (bsp.dispverts != null) {
            return;
        }
        
        bsp.dispverts = loadLump(LumpType.LUMP_DISP_VERTS, DDispVert.class);
    }

    public void loadDispTriangleTags() {
        if (bsp.disptris != null) {
            return;
        }
        
        bsp.disptris = loadLump(LumpType.LUMP_DISP_TRIS, DDispTri.class);
    }
    
    public void loadDispMultiBlend() {
        if (bsp.dispmultiblend != null) {
            return;
        }
    	
        bsp.dispmultiblend = loadLump(LumpType.LUMP_DISP_MULTIBLEND, DDispMultiBlend.class);
    }

    public void loadTexInfo() {
        if (bsp.texinfos != null) {
            return;
        }
        
        Class struct = DTexInfo.class;

        if (appID == SourceAppID.DARK_MESSIAH) {
            struct = DTexInfoDM.class;
        }

        bsp.texinfos = loadLump(LumpType.LUMP_TEXINFO, struct);
    }

    public void loadTexData() {
        if (bsp.texdatas != null) {
            return;
        }
        
        bsp.texdatas = loadLump(LumpType.LUMP_TEXDATA, DTexData.class);
        loadTexDataStrings();  // load associated texdata strings
    }

    private void loadTexDataStrings() {
        L.log(Level.FINE, "Loading {0}", LumpType.LUMP_TEXDATA_STRING_DATA);
        
        byte[] stringData;

        Lump lump = getLump(LumpType.LUMP_TEXDATA_STRING_DATA);
        LumpDataInput li = lump.getDataInput();

        try {
            final int tdsds = lump.getLength();
            stringData = new byte[tdsds];
            li.readFully(stringData);
            li.checkRemaining();
        } catch (IOException ex) {
            lumpError(lump, ex);
            return;
        }
        
        L.log(Level.FINE, "Loading {0}", LumpType.LUMP_TEXDATA_STRING_TABLE);

        lump = getLump(LumpType.LUMP_TEXDATA_STRING_TABLE);
        li = lump.getDataInput();

        try {
            final int size = 4;
            final int tdsts = lump.getLength() / size;

            bsp.texnames = new ArrayList<String>(tdsts);

            tdst:
            for (int i = 0; i < tdsts; i++) {
                int ofs = li.readInt();
                int ofsNull;
                
                // find null byte offset
                for (ofsNull = ofs; ofsNull < stringData.length; ofsNull++) {
                    if (stringData[ofsNull] == 0) {
                        // build string from string data array
                        bsp.texnames.add(new String(stringData, ofs, ofsNull - ofs));
                        continue tdst;
                    }
                }
            }

            L.log(Level.FINE, "Texture data strings: {0}", tdsts);

            li.checkRemaining();
        } catch (IOException ex) {
            lumpError(lump, ex);
        }
    }
    
    public void loadEntities() {
        if (bsp.entities != null) {
            return;
        }
        
        L.log(Level.FINE, "Loading {0}", LumpType.LUMP_ENTITIES);
        
        Lump lump = getLump(LumpType.LUMP_ENTITIES);
        EntityInputStream entReader = null;
        
        try {            
            entReader = new EntityInputStream(lump.getInputStream());
            // allow escaped quotes for VTBM
            entReader.setAllowEscSeq(bspFile.getVersion() == 17);            
            bsp.entities = new ArrayList<Entity>();
            
            entityClasses.clear();
            Entity ent;
            while ((ent = entReader.readEntity()) != null) {
                bsp.entities.add(ent);
                entityClasses.add(ent.getClassName());
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Couldn''t read entity lump", ex);
        } finally {
            IOUtils.closeQuietly(entReader);
        }
        
        // detect appID with heuristics to handle special BSP formats if it's
        // still unknown or undefined at this point
        if (entReader != null && appID == SourceAppID.UNKNOWN) {
            SourceAppDB appDB = SourceAppDB.getInstance();
            SourceApp app = appDB.find(bspFile.getName(), bspFile.getVersion(), entityClasses);
            bspFile.setSourceApp(app);
            appID = app.getAppID();
        }

        L.log(Level.FINE, "Entities: {0}", bsp.entities.size());
    }

    public void loadNodes() {
        if (bsp.nodes != null) {
            return;
        }
        
        Class struct = DNode.class;
        
        if (appID == SourceAppID.VINDICTUS) {
            // use special struct for Vindictus
            struct = DNodeVin.class;
        }
        
        bsp.nodes = loadLump(LumpType.LUMP_NODES, struct);
    }

    public void loadLeaves() {
        if (bsp.leaves != null) {
            return;
        }
        
        Class struct = DLeafV1.class;
        
        if (appID == SourceAppID.VINDICTUS) {
            // use special struct for Vindictus
            struct = DLeafVin.class;
        } else if (getLump(LumpType.LUMP_LEAFS).getVersion() == 0
                && bspFile.getVersion() == 19) {
            // read AmbientLighting, it was used in initial Half-Life 2 maps 
            // only and doesn't exist in newer or older versions
            struct = DLeafV0.class;
        }

        bsp.leaves = loadLump(LumpType.LUMP_LEAFS, struct);
    }

    public void loadLeafFaces() {
        if (bsp.leafFaces != null) {
            return;
        }
        
        bsp.leafFaces = loadIntegerLump(LumpType.LUMP_LEAFFACES, appID != SourceAppID.VINDICTUS);
    }

    public void loadLeafBrushes() {
        if (bsp.leafBrushes != null) {
            return;
        }
        
        bsp.leafBrushes = loadIntegerLump(LumpType.LUMP_LEAFBRUSHES, appID != SourceAppID.VINDICTUS);
    }

    public void loadOverlays() {
        if (bsp.overlays != null) {
            return;
        }
        
        Class struct = DOverlay.class;
        
        if (appID == SourceAppID.VINDICTUS) {
            struct = DOverlayVin.class;
        }
        
        bsp.overlays = loadLump(LumpType.LUMP_OVERLAYS, struct);
        
        // read fade distances
        if (bsp.overlayFades == null) {
            bsp.overlayFades = loadLump(LumpType.LUMP_OVERLAY_FADES, DOverlayFade.class);
        }

        // read CPU/GPU levels
        if (bsp.overlaySysLevels == null) {
            bsp.overlaySysLevels = loadLump(LumpType.LUMP_OVERLAY_SYSTEM_LEVELS, DOverlaySystemLevel.class);
        }
    }

    public void loadAreaportals() {
        if (bsp.areaportals != null) {
            return;
        }
        
        Class struct = DAreaportal.class;
        
        if (appID == SourceAppID.VINDICTUS) {
            struct = DAreaportalVin.class;
        }
        
        bsp.areaportals = loadLump(LumpType.LUMP_AREAPORTALS, struct);
    }

    public void loadOccluders() {
        if (bsp.occluderDatas != null) {
            return;
        }

        L.log(Level.FINE, "Loading {0}", LumpType.LUMP_OCCLUSION);

        Lump lump = getLump(LumpType.LUMP_OCCLUSION);
        LumpDataInput li = lump.getDataInput();

        try {
            // load occluder data
            final int occluders = lump.getLength() == 0 ? 0 : li.readInt();
            bsp.occluderDatas = new ArrayList<DOccluderData>(occluders);

            for (int i = 0; i < occluders; i++) {
                DOccluderData od;
                
                if (lump.getVersion() < 2) {
                    od = new DOccluderData();
                } else {
                    od = new DOccluderDataV1();
                }

                od.read(li);
                bsp.occluderDatas.add(od);
            }

            L.log(Level.FINE, "Occluders: {0}", occluders);

            // load occluder polys
            final int occluderPolys = lump.getLength() == 0 ? 0 : li.readInt();
            bsp.occluderPolyDatas = new ArrayList<DOccluderPolyData>(occluderPolys);

            for (int i = 0; i < occluderPolys; i++) {
                DOccluderPolyData opd = new DOccluderPolyData();
                opd.read(li);
                bsp.occluderPolyDatas.add(opd);
            }

            L.log(Level.FINE, "Occluder polygons: {0}", occluderPolys);

            // load occluder vertices
            final int occluderVertices = lump.getLength() == 0 ? 0 : li.readInt();
            bsp.occluderVerts = new ArrayList<Integer>(occluderVertices);

            for (int i = 0; i < occluderVertices; i++) {
                bsp.occluderVerts.add(li.readInt());
            }

            L.log(Level.FINE, "Occluder vertices: {0}", occluderVertices);

            li.checkRemaining();
        } catch (IOException ex) {
            lumpError(lump, ex);
        }
    }

    public void loadFlags() {
        if (bsp.mapFlags != null) {
            return;
        }

        L.log(Level.FINE, "Loading {0}", LumpType.LUMP_MAP_FLAGS);

        Lump lump = getLump(LumpType.LUMP_MAP_FLAGS);

        if (lump.getLength() == 0) {
            return;
        }

        LumpDataInput li = lump.getDataInput();

        try {
            bsp.mapFlags = EnumConverter.fromInteger(LevelFlag.class, li.readInt());

            L.log(Level.FINE, "Map flags: {0}", bsp.mapFlags);

            li.checkRemaining();
        } catch (IOException ex) {
            lumpError(lump, ex);
        }
    }
    
    public void loadPrimitives() {
        if (bsp.prims != null) {
            return;
        }
        
        bsp.prims = loadLump(LumpType.LUMP_PRIMITIVES, DPrimitive.class);
    }
    
    public void loadPrimIndices() {
        if (bsp.primIndices != null) {
            return;
        }
        
        bsp.primIndices = loadIntegerLump(LumpType.LUMP_PRIMINDICES, true);
    }
    
    public void loadPrimVerts() {
        if (bsp.primVerts != null) {
            return;
        }
        
        bsp.primVerts = loadLump(LumpType.LUMP_PRIMVERTS, DVertex.class);
    }

    private <E extends DStruct> List<E> loadLump(LumpType lumpType, Class<E> struct) {
        // don't try to read lumps that aren't supported
        if (!bspFile.canReadLump(lumpType)) {
            return new ArrayList<E>(0);
        }

        Lump lump = getLump(lumpType);
        
        // don't try to read empty lumps
        if (lump.getLength() == 0) {
            return new ArrayList<E>(0);
        }
        
        L.log(Level.FINE, "Loading {0}", lumpType);
        
        LumpDataInput li = lump.getDataInput();
        
        try {
            final int structSize = struct.newInstance().getSize();            
            final int packetCount = lump.getLength() / structSize;
            
            List<E> packets = new ArrayList<E>(packetCount);

            for (int i = 0; i < packetCount; i++) {
                E packet = struct.newInstance();
                
                int pos = li.position();
                packet.read(li);
                if (li.position() - pos != packet.getSize()) {
                    throw new IOException("Bytes read: " + pos + "; expected: " + packet.getSize());
                }
                
                packets.add(packet);
            }
            
            li.checkRemaining();
            
            L.log(Level.FINE, "{0} {1} objects", new Object[]{packets.size(), struct.getSimpleName()});
            
            return packets;
        } catch (IOException ex) {
            lumpError(lump, ex);
        } catch (IllegalAccessException ex) {
            L.log(Level.SEVERE, "Lump struct class error", ex);
        } catch (InstantiationException ex) {
            L.log(Level.SEVERE, "Lump struct class error", ex);
        }
        
        return null;
    }
    
    private List<Integer> loadIntegerLump(LumpType lumpType, boolean unsignedShort) {
        L.log(Level.FINE, "Loading {0}", lumpType);

        Lump lump = getLump(lumpType);
        LumpDataInput li = lump.getDataInput();

        try {
            final int size = unsignedShort ? 2 : 4;
            final int arraySize = lump.getLength() / size;

            List<Integer> list = new ArrayList<Integer>(arraySize);

            for (int i = 0; i < arraySize; i++) {
                if (unsignedShort) {
                    list.add(li.readUnsignedShort());
                } else {
                    list.add(li.readInt());
                }
            }

            L.log(Level.FINE, "{0} Integer objects", arraySize);

            li.checkRemaining();
            
            return list;
        } catch (IOException ex) {
            lumpError(lump, ex);
        }
        
        return null;
    }
    
    private List<Integer> loadIntegerLump(LumpType lumpType) {
        return loadIntegerLump(lumpType, false);
    }

    private void lumpError(AbstractLump lump, IOException ex) {
        L.log(Level.SEVERE, "Lump reading error in " + lump, ex);
    }
    
    /**
     * Returns the lump for the given lump type
     *
     * @param type
     * @throws IllegalArgumentException if the current BSP doesn't support this lump type
     * @return
     */
    private Lump getLump(LumpType type) {
        return bspFile.getLump(type);
    }

    /**
     * Returns the set of unique entity classes that was generated by {@link #loadEntities}
     *
     * @return set of entity class names, null if {@link #loadEntities} hasn't been called yet
     */
    public Set<String> getEntityClassSet() {
        return entityClasses;
    }

    public BspFile getBspFile() {
        return bspFile;
    }

    public BspData getData() {
        return bsp;
    }
}

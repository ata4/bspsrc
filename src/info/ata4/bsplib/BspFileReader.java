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
import info.ata4.io.DataInputReader;
import info.ata4.util.EnumConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private Set<String> entityClasses = new TreeSet<>();

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
            LumpType lt = orig ? LumpType.LUMP_ORIGINALFACES : LumpType.LUMP_FACES;
            int facesver = getLump(lt).getVersion();
            if (facesver == 2) {
                struct = DFaceVinV2.class;
            } else {
                struct = DFaceVinV1.class;
            }
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
            bsp.staticProps = new ArrayList<>();
            return;
        }

        DataInputReader in = DataInputReader.newReader(sprpLump.getBuffer());
        int sprpver = sprpLump.getVersion();

        try {
            final int padsize = 128;
            final int psnames = in.readInt();
            
            L.log(Level.FINE, "Static prop names: {0}", psnames);
            
            bsp.staticPropName = new ArrayList<>(psnames);

            for (int i = 0; i < psnames; i++) {
                bsp.staticPropName.add(in.readStringPadded(padsize));
            }

            // model path strings in Zeno Clash
            if (appID == SourceAppID.ZENO_CLASH) {
                int psextra = in.readInt();
                in.skipBytes(psextra * padsize);
            }

            // StaticPropLeafLump_t
            int propleaves = in.readInt();
            
            L.log(Level.FINE, "Static prop leaves: {0}", propleaves);
            
            bsp.staticPropLeaf = new ArrayList<>(propleaves);
            
            for (int i = 0; i < propleaves; i++) {
                bsp.staticPropLeaf.add(in.readUnsignedShort());
            }
            
            // extra data for Vindictus
            if (appID == SourceAppID.VINDICTUS && sprpver == 6) {
                int psextra = in.readInt();
                in.skipBytes(psextra * 16);
            }
            
            // StaticPropLump_t
            final int propstatics = in.readInt();
            
            Class<? extends DStaticProp> structClass = null;
            
            // special cases where derivative lump structures are used
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
                    
                case SourceAppID.VINDICTUS:
                    structClass = DStaticPropV5.class;
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
            
            bsp.staticProps = new ArrayList<>(propstatics);
            
            for (int i = 0; i < propstatics; i++) {
                DStaticProp sp = structClass.newInstance();
                
                long pos = in.position();
                sp.read(in);
                int size = sp.getSize();
                if (in.position() - pos != size) {
                    throw new IOException("Bytes read: " + pos + "; expected: " + size);
                }
                
                bsp.staticProps.add(sp);
            }

            L.log(Level.FINE, "Static props: {0}", propstatics);

            checkRemaining(in);
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
        int bspv = bspFile.getVersion();
        
        // the lump version is useless most of the time, use the AppID instead
        switch (appID) {
            case SourceAppID.VINDICTUS:
                struct = DDispInfoVin.class;
                break;
                
            case SourceAppID.HALF_LIFE_2:
                if (bspv == 17) {
                    struct = DDispInfoBSP17.class;
                }
                break;
                
            case SourceAppID.DOTA_2_BETA:
                if (bspv == 22) {
                    struct = DDispInfoBSP22.class;
                } else if (bspv >= 23) {
                    struct = DDispInfoBSP23.class;
                }
                break;
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
        DataInputReader in = DataInputReader.newReader(lump.getBuffer());

        try {
            final int tdsds = lump.getLength();
            stringData = new byte[tdsds];
            in.readFully(stringData);
            checkRemaining(in);
        } catch (IOException ex) {
            lumpError(lump, ex);
            return;
        }
        
        L.log(Level.FINE, "Loading {0}", LumpType.LUMP_TEXDATA_STRING_TABLE);

        lump = getLump(LumpType.LUMP_TEXDATA_STRING_TABLE);
        in = DataInputReader.newReader(lump.getBuffer());

        try {
            final int size = 4;
            final int tdsts = lump.getLength() / size;

            bsp.texnames = new ArrayList<>(tdsts);

            tdst:
            for (int i = 0; i < tdsts; i++) {
                int ofs = in.readInt();
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

            checkRemaining(in);
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

        try (EntityInputStream entReader = new EntityInputStream(lump.getInputStream())) {
            // allow escaped quotes for VTBM
            entReader.setAllowEscSeq(bspFile.getVersion() == 17);            
            bsp.entities = new ArrayList<>();
            
            entityClasses.clear();
            Entity ent;
            while ((ent = entReader.readEntity()) != null) {
                bsp.entities.add(ent);
                entityClasses.add(ent.getClassName());
            }

            // detect appID with heuristics to handle special BSP formats if it's
            // still unknown or undefined at this point
            if (appID == SourceAppID.UNKNOWN) {
                SourceAppDB appDB = SourceAppDB.getInstance();
                SourceApp app = appDB.find(bspFile.getName(), bspFile.getVersion(), entityClasses);
                bspFile.setSourceApp(app);
                appID = app.getAppID();
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Couldn''t read entity lump", ex);
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
        } else if (appID == SourceAppID.DOTA_2_BETA) {
            struct = DOverlayDota2.class;
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
        DataInputReader in = DataInputReader.newReader(lump.getBuffer());

        try {
            // load occluder data
            final int occluders = lump.getLength() == 0 ? 0 : in.readInt();
            bsp.occluderDatas = new ArrayList<>(occluders);

            for (int i = 0; i < occluders; i++) {
                DOccluderData od;
                
                if (lump.getVersion() < 2) {
                    od = new DOccluderData();
                } else {
                    od = new DOccluderDataV1();
                }

                od.read(in);
                bsp.occluderDatas.add(od);
            }

            L.log(Level.FINE, "Occluders: {0}", occluders);

            // load occluder polys
            final int occluderPolys = lump.getLength() == 0 ? 0 : in.readInt();
            bsp.occluderPolyDatas = new ArrayList<>(occluderPolys);

            for (int i = 0; i < occluderPolys; i++) {
                DOccluderPolyData opd = new DOccluderPolyData();
                opd.read(in);
                bsp.occluderPolyDatas.add(opd);
            }

            L.log(Level.FINE, "Occluder polygons: {0}", occluderPolys);

            // load occluder vertices
            final int occluderVertices = lump.getLength() == 0 ? 0 : in.readInt();
            bsp.occluderVerts = new ArrayList<>(occluderVertices);

            for (int i = 0; i < occluderVertices; i++) {
                bsp.occluderVerts.add(in.readInt());
            }

            L.log(Level.FINE, "Occluder vertices: {0}", occluderVertices);

            checkRemaining(in);
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

        DataInputReader in = DataInputReader.newReader(lump.getBuffer());

        try {
            bsp.mapFlags = EnumConverter.fromInteger(LevelFlag.class, in.readInt());

            L.log(Level.FINE, "Map flags: {0}", bsp.mapFlags);

            checkRemaining(in);
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
            return Collections.EMPTY_LIST;
        }

        Lump lump = getLump(lumpType);
        
        // don't try to read empty lumps
        if (lump.getLength() == 0) {
            return Collections.EMPTY_LIST;
        }
        
        L.log(Level.FINE, "Loading {0}", lumpType);
        
        DataInputReader in = DataInputReader.newReader(lump.getBuffer());
        
        try {
            final int structSize = struct.newInstance().getSize();            
            final int packetCount = lump.getLength() / structSize;
            
            List<E> packets = new ArrayList<>(packetCount);

            for (int i = 0; i < packetCount; i++) {
                E packet = struct.newInstance();
                
                long pos = in.position();
                packet.read(in);
                if (in.position() - pos != packet.getSize()) {
                    throw new IOException("Bytes read: " + pos + "; expected: " + packet.getSize());
                }
                
                packets.add(packet);
            }
            
            checkRemaining(in);
            
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
        DataInputReader in = DataInputReader.newReader(lump.getBuffer());

        try {
            final int size = unsignedShort ? 2 : 4;
            final int arraySize = lump.getLength() / size;

            List<Integer> list = new ArrayList<>(arraySize);

            for (int i = 0; i < arraySize; i++) {
                if (unsignedShort) {
                    list.add(in.readUnsignedShort());
                } else {
                    list.add(in.readInt());
                }
            }

            L.log(Level.FINE, "{0} Integer objects", arraySize);

            checkRemaining(in);
            
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
     * Checks the byte buffer for remaining bytes. Should always be called when
     * no remaining bytes are expected.
     *
     * @throws IOException if remaining bytes are found
     */
    private void checkRemaining(DataInputReader in) throws IOException {
        if (in.hasRemaining()) {          
            throw new IOException(in.remaining()
                    + " bytes remaining");
        }
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
        return Collections.unmodifiableSet(entityClasses);
    }

    public BspFile getBspFile() {
        return bspFile;
    }

    public BspData getData() {
        return bsp;
    }
}

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
import static info.ata4.bsplib.app.SourceAppID.*;
import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.io.EntityInputStream;
import info.ata4.bsplib.lump.*;
import info.ata4.bsplib.struct.*;
import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import static info.ata4.io.Seekable.Origin.CURRENT;
import info.ata4.log.LogUtils;
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
    
    private static final Logger L = LogUtils.getLogger();

    // BSP headers and data
    private final BspFile bspFile;
    private final BspData bspData;
    private int appID;

    // statistical stuff
    private Set<String> entityClasses = new TreeSet<>();

    public BspFileReader(BspFile bspFile, BspData bspData) throws IOException {
        this.bspFile = bspFile;
        this.bspData = bspData;
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
    
    public BspFileReader(BspFile bspFile) throws IOException {
        this(bspFile, new BspData());
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
        if (bspData.planes != null) {
            return;
        }
        
        bspData.planes = loadLump(LumpType.LUMP_PLANES, DPlane.class);
    }

    public void loadBrushes() {
        if (bspData.brushes != null) {
            return;
        }
        
        bspData.brushes = loadLump(LumpType.LUMP_BRUSHES, DBrush.class);
    }

    public void loadBrushSides() {
        if (bspData.brushSides != null) {
            return;
        }
        
        Class struct = DBrushSide.class;

        if (appID == VINDICTUS) {
            struct = DBrushSideVin.class;
        } else if (bspFile.getVersion() >= 21 && appID != LEFT_4_DEAD_2) {
            // newer BSP files have a slightly different struct that is still reported
            // as version 0
            struct = DBrushSideV2.class;
        }

        bspData.brushSides = loadLump(LumpType.LUMP_BRUSHSIDES, struct);
    }

    public void loadVertices() {
        if (bspData.verts != null) {
            return;
        }
        
        bspData.verts = loadLump(LumpType.LUMP_VERTEXES, DVertex.class);
    }

    public void loadClipPortalVertices() {
        if (bspData.clipPortalVerts != null) {
            return;
        }
        
        bspData.clipPortalVerts = loadLump(LumpType.LUMP_CLIPPORTALVERTS, DVertex.class);
    }

    public void loadEdges() {
        if (bspData.edges != null) {
            return;
        }

        Class struct = DEdge.class;

        if (appID == VINDICTUS) {
            struct = DEdgeVin.class;
        }

        bspData.edges = loadLump(LumpType.LUMP_EDGES, struct);
    }

    private void loadFaces(boolean orig) {
        if ((orig && bspData.origFaces != null) || (!orig && bspData.faces != null)) {
            return;
        }
        
        Class struct = DFace.class;
        
        if (appID == VAMPIRE_BLOODLINES) {
            struct = DFaceVTMB.class;
        } else if (appID == VINDICTUS) {
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
            bspData.origFaces = loadLump(LumpType.LUMP_ORIGINALFACES, struct);
        } else {
            // use LUMP_FACES_HDR if LUMP_FACES is empty
            if (getLump(LumpType.LUMP_FACES).getLength() == 0) {
                bspData.faces = loadLump(LumpType.LUMP_FACES_HDR, struct);
            } else {
                bspData.faces = loadLump(LumpType.LUMP_FACES, struct);
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
        if (bspData.models != null) {
            return;
        }
        
        Class struct = DModel.class;

        if (appID == DARK_MESSIAH) {
            struct = DModelDM.class;
        }

        bspData.models = loadLump(LumpType.LUMP_MODELS, struct);
    }

    public void loadSurfaceEdges() {
        if (bspData.surfEdges != null) {
            return;
        }
        
        bspData.surfEdges = loadIntegerLump(LumpType.LUMP_SURFEDGES);
    }

    public void loadStaticProps() {
        if (bspData.staticProps != null && bspData.staticPropName != null) {
            return;
        }

        L.fine("Loading static props");

        GameLump sprpLump = bspFile.getGameLump("sprp");

        if (sprpLump == null) {
            // static prop lump not available
            bspData.staticProps = new ArrayList<>();
            return;
        }

        DataReader in = DataReaders.forByteBuffer(sprpLump.getBuffer());
        int sprpver = sprpLump.getVersion();

        try {
            final int padsize = 128;
            final int psnames = in.readInt();
            
            L.log(Level.FINE, "Static prop names: {0}", psnames);
            
            bspData.staticPropName = new ArrayList<>(psnames);

            for (int i = 0; i < psnames; i++) {
                bspData.staticPropName.add(in.readStringFixed(padsize));
            }

            // model path strings in Zeno Clash
            if (appID == ZENO_CLASH) {
                int psextra = in.readInt();
                in.seek(psextra * padsize, CURRENT);
            }

            // StaticPropLeafLump_t
            final int propleaves = in.readInt();
            
            L.log(Level.FINE, "Static prop leaves: {0}", propleaves);
            
            bspData.staticPropLeaf = new ArrayList<>(propleaves);
            
            for (int i = 0; i < propleaves; i++) {
                bspData.staticPropLeaf.add(in.readUnsignedShort());
            }
            
            // extra data for Vindictus
            if (appID == VINDICTUS && sprpver == 6) {
                int psextra = in.readInt();
                in.seek(psextra * 16, CURRENT);
            }
            
            // StaticPropLump_t
            final int propStaticCount = in.readInt();
            
            // don't try to read static props if there are none
            if (propStaticCount == 0) {
                bspData.staticProps = Collections.emptyList();
                return;
            }
            
            // calculate static prop struct size
            final int propStaticSize = (int) in.remaining() / propStaticCount;
            
            Class<? extends DStaticProp> structClass = null;
            
            // special cases where derivative lump structures are used
            switch (appID) {
                case THE_SHIP:
                    if (propStaticSize == 188) {
                        structClass = DStaticPropV5Ship.class;
                    }
                    break;
                    
                case BLOODY_GOOD_TIME:
                    if (propStaticSize == 192) {
                        structClass = DStaticPropV6BGT.class;
                    }
                    break;
                    
                case ZENO_CLASH:
                    if (propStaticSize == 68) {
                        structClass = DStaticPropV7ZC.class;
                    }
                    break;
                    
                case DARK_MESSIAH:
                    if (propStaticSize == 136) {
                        structClass = DStaticPropV6DM.class;
                    }
                    break;
                    
                case DEAR_ESTHER:
                    if (propStaticSize == 76) {
                        structClass = DStaticPropV9DE.class;
                    }
                    break;
                    
                case VINDICTUS:
                    // newer maps report v6 even though the size is still 60, so
                    // force v5 in all cases
                    if (propStaticSize == 60) {
                        structClass = DStaticPropV5.class;
                    }
                    break;
                    
                case LEFT_4_DEAD:
                    // old L4D maps use v7 that is incompatible to the newer
                    // Source 2013 v7
                    if (sprpver == 7 && propStaticSize == 70) {
                        structClass = DStaticPropV7L4D.class;
                    }
                    break;
                    
                case TEAM_FORTRESS_2:
                    // there's been a short period where TF2 used v7, which later
                    // became v10 in all Source 2013 game
                    if (sprpver == 7 && propStaticSize == 72) {
                        structClass = DStaticPropV10.class;
                    }
                    break;
                    
                case COUNTER_STRIKE_GO:
                    // custom v10 for CS:GO, not compatible with Source 2013 v10
                    if (sprpver == 10) {
                        structClass = DStaticPropV10CSGO.class;
                    }
                    break;
            }

            // get structure class for the static prop lump version if it's not
            // a special case
            if (structClass == null) {
                try {
                    String className = DStaticProp.class.getName();
                    structClass = (Class<? extends DStaticProp>) Class.forName(className + "V" + sprpver);
                } catch (ClassNotFoundException ex) {
                    L.log(Level.WARNING, "Couldn''t find static prop struct for version {0}", sprpver);
                    structClass = null;
                }
            }
            
            // check if the size is correct
            if (structClass != null) {
                int propStaticSizeActual = structClass.newInstance().getSize();
                if (propStaticSizeActual != propStaticSize) {
                    L.log(Level.WARNING, "Static prop struct size mismatch: expected {0}, got {1} (using {2})",
                            new Object[]{propStaticSize, propStaticSizeActual, structClass.getSimpleName()});
                    structClass = null;
                }
            }
            
            // if the correct class is still unknown at this point, fall back to
            // a very basic version that should hopefully work in all situations
            int numFillBytes = 0;
            if (structClass == null) {
                L.log(Level.WARNING, "Falling back to static prop v4");
                
                structClass = DStaticPropV4.class;
                numFillBytes = propStaticSize - 56;
            }
            
            bspData.staticProps = new ArrayList<>(propStaticCount);
            
            for (int i = 0; i < propStaticCount; i++) {
                DStaticProp sp = structClass.newInstance();
                sp.read(in);
                
                if (numFillBytes > 0) {
                    in.seek(numFillBytes, CURRENT);
                }
                
                bspData.staticProps.add(sp);
            }

            L.log(Level.FINE, "Static props: {0}", propStaticCount);

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
        if (bspData.cubemaps != null) {
            return;
        }
        
        bspData.cubemaps = loadLump(LumpType.LUMP_CUBEMAPS, DCubemapSample.class);
    }

    public void loadDispInfos() {
        if (bspData.dispinfos != null) {
            return;
        }
        
        Class struct = DDispInfo.class;
        int bspv = bspFile.getVersion();
        
        // the lump version is useless most of the time, use the AppID instead
        switch (appID) {
            case VINDICTUS:
                struct = DDispInfoVin.class;
                break;
                
            case HALF_LIFE_2:
                if (bspv == 17) {
                    struct = DDispInfoBSP17.class;
                }
                break;
                
            case DOTA_2_BETA:
                if (bspv == 22) {
                    struct = DDispInfoBSP22.class;
                } else if (bspv >= 23) {
                    struct = DDispInfoBSP23.class;
                }
                break;
        }
        
        bspData.dispinfos = loadLump(LumpType.LUMP_DISPINFO, struct);
    }

    public void loadDispVertices() {
        if (bspData.dispverts != null) {
            return;
        }
        
        bspData.dispverts = loadLump(LumpType.LUMP_DISP_VERTS, DDispVert.class);
    }

    public void loadDispTriangleTags() {
        if (bspData.disptris != null) {
            return;
        }
        
        bspData.disptris = loadLump(LumpType.LUMP_DISP_TRIS, DDispTri.class);
    }
    
    public void loadDispMultiBlend() {
        if (bspData.dispmultiblend != null) {
            return;
        }
    	
        bspData.dispmultiblend = loadLump(LumpType.LUMP_DISP_MULTIBLEND, DDispMultiBlend.class);
    }

    public void loadTexInfo() {
        if (bspData.texinfos != null) {
            return;
        }
        
        Class struct = DTexInfo.class;

        if (appID == DARK_MESSIAH) {
            struct = DTexInfoDM.class;
        }

        bspData.texinfos = loadLump(LumpType.LUMP_TEXINFO, struct);
    }

    public void loadTexData() {
        if (bspData.texdatas != null) {
            return;
        }
        
        bspData.texdatas = loadLump(LumpType.LUMP_TEXDATA, DTexData.class);
        loadTexDataStrings();  // load associated texdata strings
    }

    private void loadTexDataStrings() {
        L.log(Level.FINE, "Loading {0}", LumpType.LUMP_TEXDATA_STRING_DATA);
        
        byte[] stringData;

        Lump lump = getLump(LumpType.LUMP_TEXDATA_STRING_DATA);
        DataReader in = DataReaders.forByteBuffer(lump.getBuffer());

        try {
            final int tdsds = lump.getLength();
            stringData = new byte[tdsds];
            in.readBytes(stringData);
            checkRemaining(in);
        } catch (IOException ex) {
            lumpError(lump, ex);
            return;
        }
        
        L.log(Level.FINE, "Loading {0}", LumpType.LUMP_TEXDATA_STRING_TABLE);

        lump = getLump(LumpType.LUMP_TEXDATA_STRING_TABLE);
        in = DataReaders.forByteBuffer(lump.getBuffer());

        try {
            final int size = 4;
            final int tdsts = lump.getLength() / size;

            bspData.texnames = new ArrayList<>(tdsts);

            tdst:
            for (int i = 0; i < tdsts; i++) {
                int ofs = in.readInt();
                int ofsNull;
                
                // find null byte offset
                for (ofsNull = ofs; ofsNull < stringData.length; ofsNull++) {
                    if (stringData[ofsNull] == 0) {
                        // build string from string data array
                        bspData.texnames.add(new String(stringData, ofs, ofsNull - ofs));
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
        if (bspData.entities != null) {
            return;
        }
        
        L.log(Level.FINE, "Loading {0}", LumpType.LUMP_ENTITIES);
        
        Lump lump = getLump(LumpType.LUMP_ENTITIES);

        try (EntityInputStream entReader = new EntityInputStream(lump.getInputStream())) {
            // allow escaped quotes for VTBM
            entReader.setAllowEscSeq(bspFile.getVersion() == 17);            
            bspData.entities = new ArrayList<>();
            
            entityClasses.clear();
            Entity ent;
            while ((ent = entReader.readEntity()) != null) {
                bspData.entities.add(ent);
                entityClasses.add(ent.getClassName());
            }

            // detect appID with heuristics to handle special BSP formats if it's
            // still unknown or undefined at this point
            if (appID == UNKNOWN) {
                SourceAppDB appDB = SourceAppDB.getInstance();
                SourceApp app = appDB.find(bspFile.getName(), bspFile.getVersion(), entityClasses);
                bspFile.setSourceApp(app);
                appID = app.getAppID();
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Couldn''t read entity lump", ex);
        }
        
        L.log(Level.FINE, "Entities: {0}", bspData.entities.size());
    }

    public void loadNodes() {
        if (bspData.nodes != null) {
            return;
        }
        
        Class struct = DNode.class;
        
        if (appID == VINDICTUS) {
            // use special struct for Vindictus
            struct = DNodeVin.class;
        }
        
        bspData.nodes = loadLump(LumpType.LUMP_NODES, struct);
    }

    public void loadLeaves() {
        if (bspData.leaves != null) {
            return;
        }
        
        Class struct = DLeafV1.class;
        
        if (appID == VINDICTUS) {
            // use special struct for Vindictus
            struct = DLeafVin.class;
        } else if (getLump(LumpType.LUMP_LEAFS).getVersion() == 0
                && bspFile.getVersion() == 19) {
            // read AmbientLighting, it was used in initial Half-Life 2 maps 
            // only and doesn't exist in newer or older versions
            struct = DLeafV0.class;
        }

        bspData.leaves = loadLump(LumpType.LUMP_LEAFS, struct);
    }

    public void loadLeafFaces() {
        if (bspData.leafFaces != null) {
            return;
        }
        
        bspData.leafFaces = loadIntegerLump(LumpType.LUMP_LEAFFACES, appID != VINDICTUS);
    }

    public void loadLeafBrushes() {
        if (bspData.leafBrushes != null) {
            return;
        }
        
        bspData.leafBrushes = loadIntegerLump(LumpType.LUMP_LEAFBRUSHES, appID != VINDICTUS);
    }

    public void loadOverlays() {
        if (bspData.overlays != null) {
            return;
        }
        
        Class struct = DOverlay.class;
        
        if (appID == VINDICTUS) {
            struct = DOverlayVin.class;
        } else if (appID == DOTA_2_BETA) {
            struct = DOverlayDota2.class;
        }
        
        bspData.overlays = loadLump(LumpType.LUMP_OVERLAYS, struct);
        
        // read fade distances
        if (bspData.overlayFades == null) {
            bspData.overlayFades = loadLump(LumpType.LUMP_OVERLAY_FADES, DOverlayFade.class);
        }

        // read CPU/GPU levels
        if (bspData.overlaySysLevels == null) {
            bspData.overlaySysLevels = loadLump(LumpType.LUMP_OVERLAY_SYSTEM_LEVELS, DOverlaySystemLevel.class);
        }
    }

    public void loadAreaportals() {
        if (bspData.areaportals != null) {
            return;
        }
        
        Class struct = DAreaportal.class;
        
        if (appID == VINDICTUS) {
            struct = DAreaportalVin.class;
        }
        
        bspData.areaportals = loadLump(LumpType.LUMP_AREAPORTALS, struct);
    }

    public void loadOccluders() {
        if (bspData.occluderDatas != null) {
            return;
        }

        L.log(Level.FINE, "Loading {0}", LumpType.LUMP_OCCLUSION);

        Lump lump = getLump(LumpType.LUMP_OCCLUSION);
        DataReader in = DataReaders.forByteBuffer(lump.getBuffer());

        try {
            // load occluder data
            final int occluders = lump.getLength() == 0 ? 0 : in.readInt();
            bspData.occluderDatas = new ArrayList<>(occluders);

            for (int i = 0; i < occluders; i++) {
                DOccluderData od;
                
                int lumpVersion = lump.getVersion();
                
                // Contagion maps report lump version 0, but they're actually
                // using 1
                if (bspFile.getSourceApp().getAppID() == SourceAppID.CONTAGION) {
                    lumpVersion = 1;
                }
                
                if (lumpVersion < 2) {
                    od = new DOccluderData();
                } else {
                    od = new DOccluderDataV1();
                }

                od.read(in);
                bspData.occluderDatas.add(od);
            }

            L.log(Level.FINE, "Occluders: {0}", occluders);

            // load occluder polys
            final int occluderPolys = lump.getLength() == 0 ? 0 : in.readInt();
            bspData.occluderPolyDatas = new ArrayList<>(occluderPolys);

            for (int i = 0; i < occluderPolys; i++) {
                DOccluderPolyData opd = new DOccluderPolyData();
                opd.read(in);
                bspData.occluderPolyDatas.add(opd);
            }

            L.log(Level.FINE, "Occluder polygons: {0}", occluderPolys);

            // load occluder vertices
            final int occluderVertices = lump.getLength() == 0 ? 0 : in.readInt();
            bspData.occluderVerts = new ArrayList<>(occluderVertices);

            for (int i = 0; i < occluderVertices; i++) {
                bspData.occluderVerts.add(in.readInt());
            }

            L.log(Level.FINE, "Occluder vertices: {0}", occluderVertices);

            checkRemaining(in);
        } catch (IOException ex) {
            lumpError(lump, ex);
        }
    }

    public void loadFlags() {
        if (bspData.mapFlags != null) {
            return;
        }

        L.log(Level.FINE, "Loading {0}", LumpType.LUMP_MAP_FLAGS);

        Lump lump = getLump(LumpType.LUMP_MAP_FLAGS);

        if (lump.getLength() == 0) {
            return;
        }

        DataReader in = DataReaders.forByteBuffer(lump.getBuffer());

        try {
            bspData.mapFlags = EnumConverter.fromInteger(LevelFlag.class, in.readInt());

            L.log(Level.FINE, "Map flags: {0}", bspData.mapFlags);

            checkRemaining(in);
        } catch (IOException ex) {
            lumpError(lump, ex);
        }
    }
    
    public void loadPrimitives() {
        if (bspData.prims != null) {
            return;
        }
        
        bspData.prims = loadLump(LumpType.LUMP_PRIMITIVES, DPrimitive.class);
    }
    
    public void loadPrimIndices() {
        if (bspData.primIndices != null) {
            return;
        }
        
        bspData.primIndices = loadIntegerLump(LumpType.LUMP_PRIMINDICES, true);
    }
    
    public void loadPrimVerts() {
        if (bspData.primVerts != null) {
            return;
        }
        
        bspData.primVerts = loadLump(LumpType.LUMP_PRIMVERTS, DVertex.class);
    }

    private <E extends DStruct> List<E> loadLump(LumpType lumpType, Class<E> struct) {
        // don't try to read lumps that aren't supported
        if (!bspFile.canReadLump(lumpType)) {
            return Collections.emptyList();
        }

        Lump lump = getLump(lumpType);
        
        // don't try to read empty lumps
        if (lump.getLength() == 0) {
            return Collections.emptyList();
        }
        
        L.log(Level.FINE, "Loading {0}", lumpType);
        
        DataReader in = DataReaders.forByteBuffer(lump.getBuffer());
        
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
        DataReader in = DataReaders.forByteBuffer(lump.getBuffer());

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
    private void checkRemaining(DataReader in) throws IOException {
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
        return bspData;
    }
}

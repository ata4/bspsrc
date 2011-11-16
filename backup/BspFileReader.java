package bsplib;

import bsplib.appid.AppID;
import bsplib.lump.EntityRaw;
import bsplib.lump.Areaportal;
import bsplib.lump.Brush;
import bsplib.lump.BrushSide;
import bsplib.lump.Cubemap;
import bsplib.lump.DispInfo;
import bsplib.lump.DispVertex;
import bsplib.lump.Entity;
import bsplib.lump.Face;
import bsplib.lump.GameLump;
import bsplib.lump.GenericLump;
import bsplib.lump.Leaf;
import bsplib.lump.Lump;
import bsplib.lump.Model;
import bsplib.lump.Node;
import bsplib.lump.OccluderData;
import bsplib.lump.OccluderPolyData;
import bsplib.lump.Overlay;
import bsplib.lump.Plane;
import bsplib.lump.StaticProp;
import bsplib.lump.Texdata;
import bsplib.lump.Texinfo;
import bsplib.appid.AppIDFinder;
import bsplib.lump.LumpType;
import bsplib.util.ByteBufferInputStream;
import bsplib.util.StringTools;
import bsplib.vector.Vector3f;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.zip.ZipInputStream;

/**
 * BSP file and lump reader
 */

public class BspFileReader extends FileReader {
    // little-endian "VBSP"
    public static final int VBSPHEADER = (('P'<<24)+('S'<<16)+('B'<<8)+'V');

    // version limits
    public static final int VERSION_MIN = 17;
    public static final int VERSION_MAX = 21;

    // lump limits
    public static final int HEADER_LUMPS = 64;

    // map flag strings
    private static String[] MAP_FLAGS = {
        "LVLFLAGS_BAKED_STATIC_PROP_LIGHTING_NONHDR",
        "LVLFLAGS_BAKED_STATIC_PROP_LIGHTING_HDR"
    };

    // instanced BSP data in object form
    private BspData bsp;

    // lump data
    public Lump[] lump;
    public GameLump[] gameLump;

    // statistic stuff
    private TreeSet<String> entityClasses;

    // external lumps
    private HashMap<Integer, Lump> extLumps;

    // settings
    private final File file;
    private final boolean loadLumpFiles;
    
    // default AppID
    private static AppID appidDefault;

    /**
     * Opens a BSP file and loads its headers
     *
     * @param file BSP file to open
     * @param loadLumpFiles load lump files associated to the BSP file, if true
     * @throws IOException if the file can't be opened or read
     */
    public BspFileReader(File file, boolean loadLumpFiles) throws IOException {
        this.file = file;
        this.loadLumpFiles = loadLumpFiles;

        if (debug) {
            System.out.println("Loading headers for " + file.getName());
        }

        // open random access file
        RandomAccessFile raf = new RandomAccessFile(file, "r");

        // create mapped bytebuffer
        ByteBuffer bb = createMappedByteBuffer(raf);

        // we need the header for all further loading operations
        loadHeader(bb);
        loadGameLumpHeader(bb);

        // close random access file
        raf.close();
    }

    public BspFileReader(File file) throws IOException {
        this(file, true);
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
        loadTexinfos();
        loadTexdatas();
        loadTexdataStringData();
        loadTexdataStringTable();
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
        loadNodes();
        loadLeaves();
        loadLeafFaces();
        loadLeafBrushes();
        loadOverlays();
        loadFlags();
    }

    private void loadHeader(ByteBuffer bb) throws BspException {
        bsp = new BspData();
        
        // override AppID detection?
        if (appidDefault != null) {
            bsp.appid = appidDefault;
        }

        // make sure we have enough room for reading
        if (bb.capacity() < 1036) {
            throw new BspException("Invalid or missing BSP header");
        }

        // go to start of the BSP
        bb.position(0);

        // read ident and version
        int ident = bb.getInt();
        bsp.version = bb.getInt();
        
        // no GoldSrc, please!
        if (ident == 0x1E) {
            throw new BspException("The GoldSrc format is not supported");
        }

        // check ident...
        if (ident != VBSPHEADER) {
            throw new BspException("Unknown file ident: " + ident);
        }

        // ...and version
        if (bsp.version > VERSION_MAX || bsp.version < VERSION_MIN){
            throw new BspException("Unsupported map version: " + bsp.version);
        }
        
        if (bsp.appid == AppID.DARK_MESSIAH) {
            bsp.version &= 0xff; // should result in 20
        } else if (bsp.version == 262164) {
            // hack for Dark Messiah of M&M BSPs
            if (debug) {
                System.out.println("Found Dark Messiah header!");
            }

            bsp.version &= 0xff; // should result in 20

            // no heuristic necessary, the game must be Dark Messiah
            if (bsp.appid != null) {
                bsp.appid = AppID.DARK_MESSIAH;
            }
        }


        // hack for Alien Swarm BSPs
        // TODO: will this be necessary for Portal 2, too? It could be L4D2 specific!
        boolean fixv21 = false;

        if (bsp.version == 21) {
            if (bb.getInt() != 0) {
                if (debug) {
                    System.out.println("Found v20 header!");
                }
                fixv21 = true;
            }
            bb.position(8);
        }

        if (debug) {
            System.out.println("Ident: " + ident);
            System.out.println("BSP version: " + bsp.version);
        }

        // find and load external lump files
        if (loadLumpFiles) {
            File parentFile = file.getParentFile() == null ? new File("./") : file.getParentFile();
            File[] lumpFiles = parentFile.listFiles(new LumpFileFilter(file));
            extLumps = new HashMap<Integer, Lump>(lumpFiles.length);

            // load all found lump files
            for (File lumpFile : lumpFiles) {
                loadExternalLump(lumpFile);
            }
        }

        lump = new Lump[HEADER_LUMPS];

        for (int i = 0; i < lump.length; i++) {
            int vers, ofs, len;
            byte[] fourCC = new byte[4];

            if (bsp.version >= 21 && !fixv21) {
                vers = bb.getInt();
                ofs = bb.getInt();
                len = bb.getInt();
            } else {
                ofs = bb.getInt();
                len = bb.getInt();
                vers = bb.getInt();
            }

            bb.get(fourCC);

            // override internal lump, if an external one exist
            if (loadLumpFiles && extLumps.containsKey(i)) {
                lump[i] = extLumps.get(i);
                lump[i].setBspVersion(bsp.version);

                if (debug) {
                    System.out.println("External lump: " + lump[i].getName());
                }

                continue;
            }

            lump[i] = new Lump(i, bsp.version, bb, ofs, len);
            lump[i].setParentFile(file);
            lump[i].setFourCC(fourCC);
            lump[i].setVersion(vers);
            lump[i].setBspVersion(bsp.version);
        }

        bsp.mapRev = bb.getInt();

        if (debug) {
            System.out.println("Map revision: " + bsp.mapRev);
        }
    }

    private void loadGameLumpHeader(ByteBuffer bb) {
        Lump l = lump[35];
        LumpDataInput lr = l.getReader();

        try {
            int glumps = lr.readInt();

            gameLump = new GameLump[glumps];

            for (int i = 0; i < glumps; i++) {
                int ofs, len, flags, vers;
                byte[] fourCC = new byte[4];

                lr.readFully(fourCC);

                if (bsp.appid == AppID.VINDICTUS) {
                    flags = lr.readInt();
                    vers = lr.readInt();
                } else {
                    flags = lr.readUnsignedShort();
                    vers = lr.readUnsignedShort();
                }

                ofs = lr.readInt();
                len = lr.readInt();

                gameLump[i] = new GameLump(bb, ofs, len);
                gameLump[i].setFourCC(fourCC);
                gameLump[i].setFlags(flags);
                gameLump[i].setVersion(vers);
                gameLump[i].setBspVersion(bsp.version);
            }

            if (debug) {
                System.out.println("Game lumps: " + glumps);
            }
        } catch(IOException ex) {
            lumpError(l, ex);
        }
    }
    
    private void loadExternalLump(File lumpFile) {
        try {
            // load lump from lump file
            LumpFileReader lumpReader = new LumpFileReader(lumpFile, bsp.version);

            // store lump in list
            extLumps.put(lumpReader.lumpIndex, lumpReader.getLump());
        } catch (IOException ex) {
            System.err.println("Unable to load lump file " + lumpFile.getName()
                    + ": " + ex.getMessage());
            if (debug) {
                ex.printStackTrace();
            }
        }
    }

    public void loadPlanes() {
        Lump l = lump[1];
        LumpDataInput lr = l.getReader();

        try {
            int planes = l.getLength() / 20;

            bsp.plane = new Plane[planes];

            for (int i = 0; i < planes; i++) {
                bsp.plane[i] = new Plane();

                bsp.plane[i].normal = lr.readVector3f();
                bsp.plane[i].dist = lr.readFloat();
                bsp.plane[i].type = lr.readInt();
            }

            if (debug) {
                System.out.println("Planes: " + planes);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadBrushes() {
        Lump l = lump[18];
        LumpDataInput lr = l.getReader();

        try {
            int brushes = l.getLength() / 12;

            bsp.brush = new Brush[brushes];

            for (int i = 0; i < brushes; i++) {
                bsp.brush[i] = new Brush();

                bsp.brush[i].fstside = lr.readInt();
                bsp.brush[i].numside = lr.readInt();
                bsp.brush[i].contents = lr.readInt();
            }

            if (debug) {
                System.out.println("Brushes: " + brushes);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadBrushSides() {
        Lump l = lump[19];
        LumpDataInput lr = l.getReader();

        try {
            int brushsides = l.getLength() / 8;

            bsp.brushSide = new BrushSide[brushsides];

            for (int i = 0; i < brushsides; i++) {
                bsp.brushSide[i] = new BrushSide();

                bsp.brushSide[i].pnum = lr.readUnsignedShort();
                bsp.brushSide[i].texinfo = lr.readShort();
                bsp.brushSide[i].dispinfo = lr.readShort();

                if (bsp.appid == AppID.ALIEN_SWARM) {
                    bsp.brushSide[i].bevel = lr.readByte();
                    bsp.brushSide[i].thin = lr.readByte();
                } else {
                    bsp.brushSide[i].bevel = lr.readShort();
                }
            }

            if (debug) {
                System.out.println("Brush sides: " + brushsides);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadVertices() {
        Lump l = lump[3];
        LumpDataInput lr = l.getReader();
        try {

            int verts = l.getLength() / 12;

            bsp.vertex = new Vector3f[verts];

            for (int i = 0; i < verts; i++) {
                bsp.vertex[i] = lr.readVector3f();
            }

            if (debug) {
                System.out.println("Vertices: " + bsp.vertex.length);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadClipPortalVertices() {
        Lump l = lump[41];
        LumpDataInput lr = l.getReader();

        try {
            int pverts = l.getLength() / 12;

            bsp.portalVertex = new Vector3f[pverts];

            for (int i = 0; i < pverts; i++) {
                bsp.portalVertex[i] = lr.readVector3f();
            }

            if (debug) {
                System.out.println("Portal vertices: " + bsp.portalVertex.length);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadEdges() {
        Lump l = lump[12];
        LumpDataInput lr = l.getReader();

        try {
            int edges = l.getLength() / 4;

            bsp.vi1 = new int[edges];
            bsp.vi2 = new int[edges];

            int vmax = 0;

            for (int i = 0; i < edges; i++) {
                bsp.vi1[i] = lr.readUnsignedShort();
                bsp.vi2[i] = lr.readUnsignedShort();

                vmax = Math.max(bsp.vi1[i], vmax);
                vmax = Math.max(bsp.vi2[i], vmax);
            }

            // check for out of range indices
            if (vmax > bsp.vertex.length) {
                System.err.println("Vertex index too high: " + vmax + " > " + bsp.vertex);
            }

            if (debug) {
                System.out.println("Edges: " + edges);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    private Face loadFace(LumpDataInput lr) throws IOException {
        Face nface = new Face();
        int j;

        if (bsp.version == 17) {
            //m_AvgLightColor
            lr.skipBytes(32);
        }

        nface.pnum = lr.readUnsignedShort();
        nface.side = lr.readByte();
        nface.onnode = lr.readByte();
        nface.fstedge = lr.readInt();
        nface.numedge = lr.readShort();
        nface.texinfo = lr.readShort();
        nface.dispinfo = lr.readShort();

        lr.readShort(); //surfaceFogVolumeID

        // styles
        lr.readByte();
        lr.readByte();
        lr.readByte();
        lr.readByte();

        if (bsp.version == 17) {
            // day / night
            for (j = 0; j < 20; j++) {
                lr.readByte();
            }
        }

        lr.readInt(); // lightofs
        nface.area = lr.readFloat();
        lr.readInt(); // lightmapTextureMinsInLuxels[0]
        lr.readInt(); // lightmapTextureMinsInLuxels[1]
        lr.readInt(); // lightmapTextureSizeInLuxels[0]
        lr.readInt(); // lightmapTextureSizeInLuxels[1]
        nface.orig = lr.readInt();

        if (bsp.version != 17) {
            lr.readShort(); // numPrims
            lr.readShort(); // firstPrimID
        }

        nface.smooth = lr.readInt();

        return nface;
    }

    public void loadFaces() {
        Lump l = lump[7];
        LumpDataInput lr = l.getReader();

        try {
            int faces = l.getLength() / (bsp.version == 17 ? 104 : 56);

            bsp.face = new Face[faces];

            for (int i = 0; i < faces; i++) {
                bsp.face[i] = loadFace(lr);
            }

            if (debug) {
                System.out.println("Faces: " + faces);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadOriginalFaces() {
        Lump l = lump[27];
        LumpDataInput lr = l.getReader();

        try {
            int ofaces = l.getLength() / (bsp.version == 17 ? 104 : 56);

            bsp.origFace = new Face[ofaces];

            for (int i = 0; i < ofaces; i++) {
                bsp.origFace[i] = loadFace(lr);
            }

            if (debug) {
                System.out.println("Original faces: " + ofaces);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadModels() {
        Lump l = lump[14];
        LumpDataInput lr = l.getReader();

        try {
            int models = l.getLength() / 48;

            bsp.model = new Model[models];

            for (int i = 0; i < models; i++) {
                bsp.model[i] = new Model();

                // mins
                lr.readFloat();
                lr.readFloat();
                lr.readFloat();

                // maxs
                lr.readFloat();
                lr.readFloat();
                lr.readFloat();

                // origin
                lr.readFloat();
                lr.readFloat();
                lr.readFloat();

                bsp.model[i].headnode = lr.readInt();
                bsp.model[i].fstface = lr.readInt();
                bsp.model[i].numface = lr.readInt();
            }

            if (debug) {
                System.out.println("Models: " + models);
            }

            lr.checkBuffer();

            assignModels();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadSurfaceEdges() {
        Lump l = lump[13];
        LumpDataInput lr = l.getReader();

        try {
            int surfedges = l.getLength() / 4;

            bsp.surfedge = new int[surfedges];

            for (int i = 0; i < surfedges; i++) {
                bsp.surfedge[i] = lr.readInt();
            }

            if (debug) {
                System.out.println("Surface edges: " + surfedges);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadStaticProps() {
        GameLump sprp = getGameLump("sprp");

        if (sprp == null) {
            return;
        }

        LumpDataInput lr = sprp.getReader();

//        try {
//            lr.readToFile(new File("sprp.bin"), sprp.getLength());
//        } catch (IOException ex) {
//        }

        try {
            int psnames = lr.readInt();
            bsp.staticPropName = new String[psnames];

            for (int i = 0; i < psnames; i++) {
                bsp.staticPropName[i] = lr.readPaddedString(128);
            }

            if (debug) {
                System.out.println("Static prop names: " + psnames);
            }

            // model path strings in Zeno Clash
            if (bsp.appid == AppID.ZENO_CLASH) {
                int psextra = lr.readInt();
                lr.skipBytes(psextra * 128);
            }

            // StaticPropLeafLump_t
            int propleaves = lr.readInt();
            lr.skipBytes(propleaves * 2);

            // StaticPropLump_t
            int propstatics = lr.readInt();
            bsp.staticProp = new StaticProp[propstatics];

            for (int i = 0; i < propstatics; i++) {
                bsp.staticProp[i] = new StaticProp();

                // v4
                bsp.staticProp[i].origin = lr.readVector3f();
                bsp.staticProp[i].angles = lr.readVector3f();
                bsp.staticProp[i].type = lr.readUnsignedShort();
                lr.readShort(); // m_FirstLeaf
                lr.readShort(); // m_LeafCount
                bsp.staticProp[i].solid = lr.readUnsignedByte();
                bsp.staticProp[i].flags = lr.readUnsignedByte();
                bsp.staticProp[i].skin = lr.readInt();
                bsp.staticProp[i].fademin = lr.readFloat();
                bsp.staticProp[i].fademax = lr.readFloat();
                bsp.staticProp[i].lorigin = lr.readVector3f();

                int sprpver = sprp.getVersion();

                // ZC reports version 7, but the struct differs from other games
                // that use 7+, so use 6 instead.
                if (bsp.appid == AppID.ZENO_CLASH) {
                    sprpver = 6;
                    sprp.setVersion(sprpver);
                }

                // v5
                if (sprpver >= 5) {
                    bsp.staticProp[i].forcefadescale = lr.readFloat();
                }

                // v6/v7
                if (sprpver == 6 || sprpver == 7) {
                    bsp.staticProp[i].maxdxlevel = lr.readUnsignedShort();
                    bsp.staticProp[i].mindxlevel = lr.readUnsignedShort();
                }

                // v8
                if (sprpver >= 8) {
                    bsp.staticProp[i].mincpulevel = lr.readUnsignedByte();
                    bsp.staticProp[i].maxcpulevel = lr.readUnsignedByte();
                    bsp.staticProp[i].mingpulevel = lr.readUnsignedByte();
                    bsp.staticProp[i].maxgpulevel = lr.readUnsignedByte();
                }

                // v7
                if (sprpver >= 7) {
                    bsp.staticProp[i].diffmod = lr.readColor32();
                }

                // v9
                if (sprpver >= 9) {
                    bsp.staticProp[i].disablex360 = lr.readInt() == 1;
                }

                // target names for The Ship
                if (bsp.appid == AppID.THE_SHIP) {
                    bsp.staticProp[i].targetname = lr.readPaddedString(128);
                }

                // extra stuff for Dark Messiah of Might and Magic
                if (bsp.appid == AppID.DARK_MESSIAH) {
                    // 72 byte array, purpose unknown
                    lr.skipBytes(72);

                    // these values don't seem to make much sense for DM, reset them for now
                    bsp.staticProp[i].maxdxlevel = 0;
                    bsp.staticProp[i].mindxlevel = 0;
                }

                if (bsp.appid == AppID.ZENO_CLASH) {
                    // unknown
                    lr.skipBytes(4);
                }
            }

            if (debug) {
                System.out.println("Static props: " + propstatics);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(sprp, ex);
        }
    }

    public void loadCubemaps() {
        Lump l = lump[42];
        LumpDataInput lr = l.getReader();

        try {
            int cubemaps = l.getLength() / 16;

            bsp.cubemap = new Cubemap[cubemaps];

            for (int i = 0; i < cubemaps; i++) {
                bsp.cubemap[i] = new Cubemap();

                bsp.cubemap[i].x = lr.readInt();
                bsp.cubemap[i].y = lr.readInt();
                bsp.cubemap[i].z = lr.readInt();

                bsp.cubemap[i].size = (byte) lr.readInt();
            }

            if (debug) {
                System.out.println("Cubemaps: " + cubemaps);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadDispInfos() {
        Lump l = lump[26];
        LumpDataInput lr = l.getReader();

        try {
            int dispinfos = l.getLength() / 176;

            bsp.dispinfo = new DispInfo[dispinfos];

            for (int i = 0; i < dispinfos; i++) {
                DispInfo di = new DispInfo();

                di.startpos = lr.readVector3f();
                di.ivert = lr.readInt();
                di.itri = lr.readInt();
                di.power = lr.readInt();
                di.smoothangle = lr.readFloat();
                di.contents = lr.readInt();
                di.face = lr.readUnsignedShort();
                lr.readShort();
                lr.readInt(); // LightmapAlphaStart
                lr.readInt(); // LightmapSamplePositionStart

                // EdgeNeighbors, CornerNeighbors
                for (int j = 0; j < 92; j++) {
                    lr.readByte();
                }

                for (int j = 0; j < 10; j++) {
                    di.allow[j] = lr.readInt();
                }

                bsp.dispinfo[i] = di;
            }

            if (debug) {
                System.out.println("Displacement info: " + dispinfos);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadDispVertices() {
        Lump l = lump[33];
        LumpDataInput lr = l.getReader();

        try {
            int dispverts = l.getLength() / 20;

            bsp.dispvert = new DispVertex[dispverts];
            for (int i = 0; i < dispverts; i++) {
                bsp.dispvert[i] = new DispVertex();

                bsp.dispvert[i].x = lr.readFloat();
                bsp.dispvert[i].y = lr.readFloat();
                bsp.dispvert[i].z = lr.readFloat();

                bsp.dispvert[i].dist = lr.readFloat();
                bsp.dispvert[i].alpha = lr.readFloat();
            }

            if (debug) {
                System.out.println("Displacement vertices: " + dispverts);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadDispTriangleTags() {
        Lump l = lump[48];
        LumpDataInput lr = l.getReader();

        try {
            int disptris = l.getLength() / 2;

            bsp.disptri = new int[disptris];

            for (int i = 0; i < disptris; i++) {
                bsp.disptri[i] = lr.readUnsignedShort();
            }

            if (debug) {
                System.out.println("Displacement triangle tags: " + disptris);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadTexinfos() {
        Lump l = lump[6];
        LumpDataInput lr = l.getReader();

        try {
            int texinfos = l.getLength() / 72;

            bsp.texinfo = new Texinfo[texinfos];

            for (int i = 0; i < texinfos; i++) {
                int k;
                bsp.texinfo[i] = new Texinfo();

                for (int j = 0; j < 2; j++) {
                    for (k = 0; k < 4; k++) {
                        bsp.texinfo[i].tv[j][k] = lr.readFloat();
                    }
                }
                for (int j = 0; j < 2; j++) {
                    for (k = 0; k < 4; k++) {
                        bsp.texinfo[i].lv[j][k] = lr.readFloat();
                    }

                }

                bsp.texinfo[i].flags = lr.readInt();
                bsp.texinfo[i].texdata = lr.readInt();
            }

            if (debug) {
                System.out.println("Texture info: " + texinfos);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadTexdatas() {
        Lump l = lump[2];
        LumpDataInput lr = l.getReader();
        try {
            int texdatas = l.getLength() / 32;

            bsp.texdata = new Texdata[texdatas];

            for (int i = 0; i < texdatas; i++) {
                bsp.texdata[i] = new Texdata();

                // reflectivity
                lr.readFloat();
                lr.readFloat();
                lr.readFloat();

                bsp.texdata[i].nameid = lr.readInt();

                lr.readInt(); // width
                lr.readInt(); // height
                lr.readInt(); // view_width
                lr.readInt(); // view_height
            }

            if (debug) {
                System.out.println("Texture data: " + texdatas);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadTexdataStringData() {
        Lump l = lump[43];
        LumpDataInput lr = l.getReader();
        try {
            int tdsds = l.getLength();

            bsp.texdataStringData = new byte[tdsds];
            lr.readFully(bsp.texdataStringData);

            if (debug) {
                System.out.println("Texture string data: " + tdsds);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadTexdataStringTable() {
        Lump l = lump[44];
        LumpDataInput lr = l.getReader();

        try {
            int tdsts = l.getLength() / 4;

            bsp.texdataStringTable = new int[tdsts];
            bsp.texname = new String[tdsts];

            for (int i = 0; i < tdsts; i++) {
                int ix = bsp.texdataStringTable[i] = lr.readInt();

                for (int j = ix; j < bsp.texdataStringData.length; j++) {
                    if (bsp.texdataStringData[j] != 0) {
                        continue;
                    }
                    bsp.texname[i] = new String(bsp.texdataStringData, ix, j - ix);
                    break;
                }
            }

            if (debug) {
                System.out.println("Texture string table: " + tdsts);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    private ArrayList<EntityRaw> loadEntitiesRaw() {
        EntityRaw entity = new EntityRaw();
        ArrayList<EntityRaw> entityList = new ArrayList<EntityRaw>();
        boolean readEntity = false;

        Lump l = lump[0];
        LumpDataInput lr = l.getReader();

        try {
            while (lr.hasRemaining()) {
                String line = lr.readLine();

                if (line.equals("{")) {
                    readEntity = true;
                    continue;
                }

                if (line.equals("}")) {
                    readEntity = false;
                    entityList.add(entity);
                    entity = new EntityRaw();
                }

                if (!readEntity) {
                    continue;
                }

                String[] keyval = StringTools.fastSplit(line, '"');

                if (keyval.length != 5) {
                    continue;
                }

                entity.add(new KeyValue(keyval[1], keyval[3]));
            }

            // it should never complain here, but you never know...
            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }

        return entityList;
    }

    public void loadEntities() {
        // load the entities in raw mode first
        ArrayList<EntityRaw> entRawList = loadEntitiesRaw();

        // now post-process the result
        entityClasses = new TreeSet<String>();
        bsp.entity = new Entity[entRawList.size()];

        for (int i = 0; i < bsp.entity.length; i++) {
            bsp.entity[i] = new Entity(entRawList.get(i), bsp.version >= 21);
            entityClasses.add(bsp.entity[i].classname);
        }

        // we don't need this list anymore, clear it
        entRawList.clear();

        if (debug) {
            System.out.println("Entities: " + bsp.entity.length);
        }

        // heuristic game detection to handle uncommon BSP formats
        if (bsp.appid == AppID.UNKNOWN) {
            bsp.appid = AppIDFinder.getInstance().find(entityClasses, bsp.version);
        }
    }

    public void loadNodes() {
        Lump l = lump[5];
        LumpDataInput lr = l.getReader();

        try {
            int nodes = l.getLength() / 32;

            bsp.node = new Node[nodes];

            for (int i = 0; i < nodes; i++) {
                bsp.node[i] = new Node();

                bsp.node[i].pnum = lr.readInt();
                bsp.node[i].child[0] = lr.readInt();
                bsp.node[i].child[1] = lr.readInt();
                bsp.node[i].mins[0] = lr.readShort();
                bsp.node[i].mins[1] = lr.readShort();
                bsp.node[i].mins[2] = lr.readShort();
                bsp.node[i].maxs[0] = lr.readShort();
                bsp.node[i].maxs[1] = lr.readShort();
                bsp.node[i].maxs[2] = lr.readShort();
                bsp.node[i].fstface = lr.readUnsignedShort();
                bsp.node[i].numface = lr.readUnsignedShort();
                bsp.node[i].area = lr.readShort();

                lr.readShort(); // paddding
            }

            if (debug) {
                System.out.println("Nodes: " + nodes);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadLeaves() {
        Lump l = lump[10];
        LumpDataInput lr = l.getReader();

        try {
            // choose if we need to read AmbientLighting or not
            // it was used in the initial final Half-Life 2 Source engine only and
            // doesn't exist in newer or older versions
            boolean readAmbLighting = l.getVersion() == 0 && bsp.version == 19;

            int leaves = l.getLength() / (readAmbLighting ? 56 : 32);

            bsp.leaf = new Leaf[leaves];

            for (int i = 0; i < leaves; i++) {
                bsp.leaf[i] = new Leaf();

                bsp.leaf[i].contents = lr.readInt();
                bsp.leaf[i].cluster = lr.readShort();
                bsp.leaf[i].areaFlags = lr.readShort();
                bsp.leaf[i].mins[0] = lr.readShort();
                bsp.leaf[i].mins[1] = lr.readShort();
                bsp.leaf[i].mins[2] = lr.readShort();
                bsp.leaf[i].maxs[0] = lr.readShort();
                bsp.leaf[i].maxs[1] = lr.readShort();
                bsp.leaf[i].maxs[2] = lr.readShort();
                bsp.leaf[i].fstleafface = lr.readUnsignedShort();
                bsp.leaf[i].numleafface = lr.readUnsignedShort();
                bsp.leaf[i].fstleafbrush = lr.readUnsignedShort();
                bsp.leaf[i].numleafbrush = lr.readUnsignedShort();

                lr.readShort(); // leafWaterDataID

                // AmbientLighting
                if (readAmbLighting) {
                    for (int j = 0; j < 24; j++) {
                        lr.readByte();
                    }
                }

                lr.readShort(); // paddding
            }

            if (debug) {
                System.out.println("Leaves: " + leaves);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadLeafFaces() {
        Lump l = lump[16];
        LumpDataInput lr = l.getReader();

        try {
            int lfaces = l.getLength() / 2;

            bsp.leafFace = new int[lfaces];

            for (int i = 0; i < lfaces; i++) {
                bsp.leafFace[i] = lr.readUnsignedShort();
            }

            if (debug) {
                System.out.println("Leaf faces: " + lfaces);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadLeafBrushes() {
        Lump l = lump[17];
        LumpDataInput lr = l.getReader();

        try {
            int lbrushes = l.getLength() / 2;

            bsp.leafBrush = new int[lbrushes];

            for (int i = 0; i < lbrushes; i++) {
                bsp.leafBrush[i] = lr.readUnsignedShort();
            }

            if (debug) {
                System.out.println("Leaf brushes: " + lbrushes);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadOverlays() {
        Lump l = lump[45];
        LumpDataInput lr = l.getReader();

        try {
            int overlays = l.getLength() / 352;

            bsp.overlay = new Overlay[overlays];

            for (int i = 0; i < overlays; i++) {
                bsp.overlay[i] = new Overlay();

                bsp.overlay[i].id = lr.readInt();
                bsp.overlay[i].texinfo = lr.readShort();
                bsp.overlay[i].faces = lr.readUnsignedShort();

                for (int j = 0; j < 64; j++) {
                    bsp.overlay[i].face[j] = lr.readInt();
                }

                bsp.overlay[i].u0 = lr.readFloat();
                bsp.overlay[i].u1 = lr.readFloat();
                bsp.overlay[i].v0 = lr.readFloat();
                bsp.overlay[i].v1 = lr.readFloat();

                for (int j = 0; j < 4; j++) {
                    bsp.overlay[i].uv[j] = lr.readVector3f();
                }
                bsp.overlay[i].origin = lr.readVector3f();
                bsp.overlay[i].normal = lr.readVector3f();

                bsp.overlay[i].ubasis = new Vector3f(bsp.overlay[i].uv[0].z,
                        bsp.overlay[i].uv[1].z, bsp.overlay[i].uv[2].z);

                boolean vflip = bsp.overlay[i].uv[3].z == 1.0F;

                bsp.overlay[i].uv[0].z = 0.0F;
                bsp.overlay[i].uv[1].z = 0.0F;
                bsp.overlay[i].uv[2].z = 0.0F;
                bsp.overlay[i].uv[3].z = 0.0F;

                bsp.overlay[i].vbasis = bsp.overlay[i].normal.cross(
                        bsp.overlay[i].ubasis).normalize();

                if (!vflip) {
                    continue;
                }

                bsp.overlay[i].vbasis = bsp.overlay[i].vbasis.scalar(-1.0F);
            }

            if (debug) {
                System.out.println("Overlays: " + overlays);
            }

            lr.checkBuffer();

            // read fade distances
            loadOverlayFadeDistances();

            // read CPU/GPU levels
            loadOverlaySystemLevels();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    private void loadOverlayFadeDistances() {
        Lump l = lump[60];

        if (l.getLength() == 0) {
            return;
        }

        LumpDataInput lr = l.getReader();

        try {
            int overlayFadeDists = l.getLength() / 8;

            // both arrays must have equal sizes
            if (overlayFadeDists != bsp.overlay.length) {
                System.err.println("Overlay fade distances doesn't match bsp.overlay array size!");
                return;
            }

            for (int i = 0; i < overlayFadeDists; i++) {
                bsp.overlay[i].fadedists = true;
                bsp.overlay[i].fademin = lr.readFloat();
                bsp.overlay[i].fademax = lr.readFloat();

                // values are squared, convert them back
                if (bsp.overlay[i].fademin > 0) {
                    bsp.overlay[i].fademin = (float) Math.sqrt(bsp.overlay[i].fademin);
                }
                if (bsp.overlay[i].fademax > 0) {
                    bsp.overlay[i].fademax = (float) Math.sqrt(bsp.overlay[i].fademax);
                }
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    private void loadOverlaySystemLevels() {
        Lump l = lump[61];

        if (l.getLength() == 0) {
            return;
        }

        LumpDataInput lr = l.getReader();

        try {
            int overlaySysLevels = l.getLength() / 4;

            // both arrays must have equal sizes
            if (overlaySysLevels != bsp.overlay.length) {
                System.err.println("Overlay system levels dont't match bsp.overlay array size!");
                return;
            }

            for (int i = 0; i < overlaySysLevels; i++) {
                bsp.overlay[i].syslevels = true;
                bsp.overlay[i].mincpulevel = lr.readByte();
                bsp.overlay[i].maxcpulevel = lr.readByte();
                bsp.overlay[i].mingpulevel = lr.readByte();
                bsp.overlay[i].maxgpulevel = lr.readByte();
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadAreaportals() {
        Lump l = lump[21];
        LumpDataInput lr = l.getReader();

        try {
            int areaportals = l.getLength() / 12;

            bsp.areaportal = new Areaportal[areaportals];

            for (int i = 0; i < areaportals; i++) {
                bsp.areaportal[i] = new Areaportal();

                bsp.areaportal[i].portalKey = lr.readShort();
                bsp.areaportal[i].otherportal = lr.readShort();
                bsp.areaportal[i].firstClipPortalVert = lr.readShort();
                bsp.areaportal[i].clipPortalVerts = lr.readShort();
                bsp.areaportal[i].planenum = lr.readInt();
            }

            if (debug) {
                System.out.println("Areaportals: " + areaportals);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadOccluders() {
        Lump l = lump[9];
        LumpDataInput lr = l.getReader();

        try {
            // load occluder data
            int occluders = l.getLength() == 0 ? 0 : lr.readInt();
            bsp.occluderData = new OccluderData[occluders];

            for (int i = 0; i < occluders; i++) {
                bsp.occluderData[i] = new OccluderData();

                bsp.occluderData[i].flags = lr.readInt();
                bsp.occluderData[i].firstpoly = lr.readInt();
                bsp.occluderData[i].polycount = lr.readInt();
                bsp.occluderData[i].mins = lr.readVector3f();
                bsp.occluderData[i].maxs = lr.readVector3f();

                if (l.getVersion() >= 2) {
                    bsp.occluderData[i].area = lr.readInt();
                }
            }

            if (debug) {
                System.out.println("Occluders: " + occluders);
            }

            // load occluder polys
            int occluderPolys = l.getLength() == 0 ? 0 : lr.readInt();
            bsp.occluderPolyData = new OccluderPolyData[occluderPolys];

            for (int i = 0; i < occluderPolys; i++) {
                bsp.occluderPolyData[i] = new OccluderPolyData();

                bsp.occluderPolyData[i].firstvertexindex = lr.readInt();
                bsp.occluderPolyData[i].vertexcount = lr.readInt();
                bsp.occluderPolyData[i].planenum = lr.readInt();
            }

            if (debug) {
                System.out.println("Occluder polygons: " + occluderPolys);
            }

            // load occluder vertices
            int occluderVertices = l.getLength() == 0 ? 0 : lr.readInt();
            bsp.occluderVertex = new int[occluderVertices];

            for (int i = 0; i < occluderVertices; i++) {
                bsp.occluderVertex[i] = lr.readInt();
            }

            if (debug) {
                System.out.println("Occluder vertices: " + occluderVertices);
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    public void loadFlags() {
        Lump l = lump[59];

        if (l.getLength() == 0) {
            return;
        }

        LumpDataInput lr = l.getReader();

        try {
            bsp.mapFlags = lr.readInt();

            if (debug) {
                System.out.println("Map flags: " + getFlagString());
            }

            lr.checkBuffer();
        } catch (IOException ex) {
            lumpError(l, ex);
        }
    }

    private String getFlagString() {
        StringBuffer s = new StringBuffer();

        int v = bsp.mapFlags;

        for (int i = 0; i < MAP_FLAGS.length; i++) {
            if ((v & 0x1) == 1) {
                s.append(MAP_FLAGS[i] + " ");
            }
            v >>>= 1;
        }

        return s.toString();
    }

    private void assignModels() {
        for (int i = 1; i < bsp.entity.length; i++) {
            int imodel = bsp.entity[i].model;
            if (imodel <= 0) {
                continue;
            }
            bsp.model[imodel].entity = i;
        }
    }

    private void lumpError(GenericLump lump, IOException ex) {
        System.err.println("Lump reading error in " + lump + ": " + ex.getMessage());

        if (debug) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns the lump for the given lump type
     *
     * @param type
     * @throws IllegalArgumentException if the current BSP doesn't support this lump type
     * @return
     */
    public Lump getLump(LumpType type) {
        // lets make sure that type of lump is available in this BSP
        if (type.getBspVersion() != -1 && type.getBspVersion() < bsp.version) {
            throw new IllegalArgumentException("Lump type " + type
                    + " is not available in version " + bsp.version);
        }
        
        return lump[type.getIndex()];
    }

    /**
     * Returns the game lump for the matching fourCC
     *
     * @param sid game lump fourCC
     * @return game lump, if found. otherwise null
     */
    public GameLump getGameLump(String sid) {
        for (int i = 0; i < gameLump.length; i++) {
            if (new String(gameLump[i].getFourCC()).equals(sid)) {
                return gameLump[i];
            }
        }

        return null;
    }

    /**
     * Returns the set of unique entity classes that was generated by {@link #loadEntities}
     *
     * @return set of entity class names, null if {@link #loadEntities} hasn't been called yet
     */
    public TreeSet<String> getEntityClassSet() {
        return entityClasses;
    }

    /**
     * Creates a ZipInputStream instance for the uncompressed pakfile.
     *
     * @return the pakfile's ZipInputStream
     * @throws IOException
     */
    public ZipInputStream getPakfileStream() throws IOException {
        // duplicate buffer to read as big endian
        ByteBuffer pakBuffer = getLump(LumpType.PAKFILE).getBuffer().duplicate();

        return new ZipInputStream(new ByteBufferInputStream(pakBuffer));
    }

    public File getNextLumpFile() {
        LumpFileFilter filter = new LumpFileFilter(file);
        File parentFile = file.getAbsoluteFile().getParentFile();

        if (parentFile == null) {
            parentFile = file;
        }

        parentFile.listFiles(filter);
        int lumpIndex = filter.getHighestLumpIndex() + 1;
        String lumpFile = file.getName().replace(".bsp", "_l_" + lumpIndex + ".lmp");

        return new File(parentFile, lumpFile);
    }

    public File getFile() {
        return file;
    }

    public BspData getData() {
        return bsp;
    }

    public static void setDefaultAppID(AppID id) {
        appidDefault = id;
    }

    public static AppID getDefaultAppID() {
        return appidDefault;
    }
}

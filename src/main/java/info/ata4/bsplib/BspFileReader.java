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
import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.io.lumpreader.*;
import info.ata4.bsplib.lump.AbstractLump;
import info.ata4.bsplib.lump.GameLump;
import info.ata4.bsplib.lump.Lump;
import info.ata4.bsplib.lump.LumpType;
import info.ata4.bsplib.struct.*;
import info.ata4.log.LogUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static info.ata4.bsplib.app.SourceAppId.*;

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

    public BspFileReader(BspFile bspFile, BspData bspData) throws IOException {
        this.bspFile = bspFile;
        this.bspData = bspData;

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

    private int appId() {
        return bspFile.getSourceApp().getAppId();
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

        bspData.planes = readDStructChunksLump(LumpType.LUMP_PLANES, DPlane::new);
        L.fine(String.format("%d planes", bspData.planes.size()));
    }

    public void loadBrushes() {
        if (bspData.brushes != null) {
            return;
        }

        bspData.brushes = readDStructChunksLump(LumpType.LUMP_BRUSHES, DBrush::new);
        L.fine(String.format("%d brushes", bspData.brushes.size()));
    }

    public void loadBrushSides() {
        if (bspData.brushSides != null) {
            return;
        }

        Supplier<? extends DBrushSide> dStructSupplier;

        if (appId() == VINDICTUS) {
            dStructSupplier = DBrushSideVin::new;
        } else if (bspFile.getVersion() >= 21 && appId() != LEFT_4_DEAD_2) {
            // newer BSP files have a slightly different struct that is still reported
            // as version 0
	        dStructSupplier = DBrushSideV2::new;
        } else {
	        dStructSupplier = DBrushSide::new;
        }

        bspData.brushSides = readDStructChunksLump(LumpType.LUMP_BRUSHSIDES, dStructSupplier);
        L.fine(String.format("%d brush sides", bspData.brushSides.size()));
    }

    public void loadVertices() {
        if (bspData.verts != null) {
            return;
        }

        bspData.verts = readDStructChunksLump(LumpType.LUMP_VERTEXES, DVertex::new);
        L.fine(String.format("%d vertices", bspData.verts.size()));
    }

    public void loadClipPortalVertices() {
        if (bspData.clipPortalVerts != null) {
            return;
        }

        bspData.clipPortalVerts = readDStructChunksLump(LumpType.LUMP_CLIPPORTALVERTS, DVertex::new);
        L.fine(String.format("%d areaportal vertices", bspData.clipPortalVerts.size()));
    }

    public void loadEdges() {
        if (bspData.edges != null) {
            return;
        }

        Supplier<? extends DEdge> struct;

	    if (appId() == VINDICTUS) {
		    struct = DEdgeVin::new;
	    } else {
		    struct = DEdge::new;
	    }

        bspData.edges = readDStructChunksLump(LumpType.LUMP_EDGES, struct);
        L.fine(String.format("%d edges", bspData.edges.size()));
    }

    private Supplier<? extends DFace> faceDStructSupplier(int lumpVersion) {
        switch (appId()) {
            case VAMPIRE_BLOODLINES:
                return DFaceVTMB::new;
            case VINDICTUS:
                if (lumpVersion == 2)
                    return DFaceVinV2::new;
                else
                    return DFaceVinV1::new;
            default:
                switch (bspFile.getVersion()) {
                    case 17:
                        return DFaceBSP17::new;
                    case 18:
                        return DFaceBSP18::new;
                    default:
                        return DFace::new;
                }
        }
    }

    public void loadFaces() {
        if (bspData.faces != null) {
            return;
        }

        // prioritize LUMP_FACES_HDR over LUMP_FACES
        boolean useHdrLump = bspFile.canReadLump(LumpType.LUMP_FACES_HDR)
                && bspFile.getLump(LumpType.LUMP_FACES_HDR).getLength() != 0;
        LumpType faceLumpType = useHdrLump ? LumpType.LUMP_FACES_HDR : LumpType.LUMP_FACES;

        bspData.faces = readDStructChunksLump(faceLumpType, this::faceDStructSupplier);
        L.fine(String.format("%d faces", bspData.faces.size()));
    }

    public void loadOriginalFaces() {
        if (bspData.origFaces != null) {
            return;
        }

        bspData.origFaces = readDStructChunksLump(LumpType.LUMP_ORIGINALFACES, this::faceDStructSupplier);
        L.fine(String.format("%d original faces", bspData.origFaces.size()));
    }

    public void loadModels() {
        if (bspData.models != null) {
            return;
        }

        Supplier<? extends DModel> dStructSupplier;

	    if (appId() == DARK_MESSIAH) {
		    dStructSupplier = DModelDM::new;
	    } else {
		    dStructSupplier = DModel::new;
	    }

        bspData.models = readDStructChunksLump(LumpType.LUMP_MODELS, dStructSupplier);
        L.fine(String.format("%d models", bspData.models.size()));
    }

    public void loadSurfaceEdges() {
        if (bspData.surfEdges != null) {
            return;
        }

        bspData.surfEdges = readLump(LumpType.LUMP_SURFEDGES, new IntegerChunksLumpReader());
        L.fine(String.format("%d surface edges", bspData.surfEdges.size()));
    }

    public void loadStaticProps() {
        if (bspData.staticProps != null && bspData.staticPropName != null && bspData.staticPropLeaf != null) {
            return;
        }

        StaticPropLumpReader.StaticPropData staticPropData = readGameLump(
                "sprp",
                lumpVersion -> new StaticPropLumpReader(lumpVersion, appId()),
                StaticPropLumpReader.StaticPropData::new
        );

        bspData.staticPropName = staticPropData.names;
        bspData.staticProps = staticPropData.props;
        bspData.staticPropLeaf = staticPropData.leafs;

        L.fine(String.format("%d static prop names", staticPropData.names.size()));
        L.fine(String.format("%d static props", staticPropData.props.size()));
        L.fine(String.format("%d static prop leafs", staticPropData.leafs.size()));
    }

    public void loadCubemaps() {
        if (bspData.cubemaps != null) {
            return;
        }

        bspData.cubemaps = readDStructChunksLump(LumpType.LUMP_CUBEMAPS, DCubemapSample::new);
        L.fine(String.format("%d cubemaps", bspData.cubemaps.size()));
    }

    public void loadDispInfos() {
        if (bspData.dispinfos != null) {
            return;
        }

        Supplier<? extends DDispInfo> dStructSupplier = DDispInfo::new;
        int bspv = bspFile.getVersion();

        // the lump version is useless most of the time, use the AppID instead
        switch (appId()) {
            case VINDICTUS:
                dStructSupplier = DDispInfoVin::new;
                break;

            case HALF_LIFE_2:
                if (bspv == 17) {
                    dStructSupplier = DDispInfoBSP17::new;
                }
                break;

            case DOTA_2_BETA:
                if (bspv == 22) {
                    dStructSupplier = DDispInfoBSP22::new;
                } else if (bspv >= 23) {
                    dStructSupplier = DDispInfoBSP23::new;
                }
                break;
        }

        bspData.dispinfos = readDStructChunksLump(LumpType.LUMP_DISPINFO, dStructSupplier);
        L.fine(String.format("%d displacement infos", bspData.dispinfos.size()));
    }

    public void loadDispVertices() {
        if (bspData.dispverts != null) {
            return;
        }

        bspData.dispverts = readDStructChunksLump(LumpType.LUMP_DISP_VERTS, DDispVert::new);
        L.fine(String.format("%d displacement vertices", bspData.dispverts.size()));
    }

    public void loadDispTriangleTags() {
        if (bspData.disptris != null) {
            return;
        }

        bspData.disptris = readDStructChunksLump(LumpType.LUMP_DISP_TRIS, DDispTri::new);
        L.fine(String.format("%d displacement triangles", bspData.disptris.size()));
    }

    public void loadDispMultiBlend() {
        if (bspData.dispmultiblend != null) {
            return;
        }

        // black mesa uses the LUMP_OVERLAY_SYSTEM_LEVELS lump to store multiblend information.
        // the original purpose of that lump is no longer used
        LumpType lumpType;
        if (appId() == BLACK_MESA)
            lumpType = LumpType.LUMP_OVERLAY_SYSTEM_LEVELS;
        else
            lumpType = LumpType.LUMP_DISP_MULTIBLEND;

        bspData.dispmultiblend = readDStructChunksLump(lumpType, DDispMultiBlend::new);
        L.fine(String.format("%d displacement multiblend", bspData.dispmultiblend.size()));
    }

    public void loadTexInfo() {
        if (bspData.texinfos != null) {
            return;
        }

        Supplier<? extends DTexInfo> dStructSupplier;

        if (appId() == DARK_MESSIAH) {
            dStructSupplier = DTexInfoDM::new;
        } else {
            dStructSupplier = DTexInfo::new;
        }

        bspData.texinfos = readDStructChunksLump(LumpType.LUMP_TEXINFO, dStructSupplier);
        L.fine(String.format("%d texture infos", bspData.texinfos.size()));
    }

    public void loadTexData() {
        if (bspData.texdatas != null) {
            return;
        }

        bspData.texdatas = readDStructChunksLump(LumpType.LUMP_TEXDATA, DTexData::new);
        L.fine(String.format("%d texture data", bspData.texdatas.size()));

        loadTexDataStrings();  // load associated texdata strings
    }

    private void loadTexDataStrings() {
        if (bspData.texnames != null) {
            return;
        }

        List<Integer> stringTableData = readLump(LumpType.LUMP_TEXDATA_STRING_TABLE, new IntegerChunksLumpReader());
        bspData.texnames = readLump(LumpType.LUMP_TEXDATA_STRING_DATA, new TexdataStringLumpReader(stringTableData));
        L.fine(String.format("%d texture names", bspData.texnames.size()));
    }

    public void loadEntities() {
        if (bspData.entities != null) {
            return;
        }

        boolean allowEscSeq = bspFile.getVersion() == 17;
        bspData.entities = readLump(LumpType.LUMP_ENTITIES, new EntityLumpReader(allowEscSeq));
        L.fine(String.format("%d entities", bspData.entities.size()));

        Set<String> entityClasses = bspData.entities.stream()
                .map(Entity::getClassName)
                .collect(Collectors.toSet());

        // detect appID with heuristics to handle special BSP formats if it's
        // still unknown or undefined at this point
        if (appId() == UNKNOWN) {
            SourceAppDB appDB = SourceAppDB.getInstance();
            SourceApp app = appDB.find(bspFile.getName(), bspFile.getVersion(), entityClasses);
            bspFile.setSourceApp(app);
        }
    }

    public void loadNodes() {
        if (bspData.nodes != null) {
            return;
        }

        Supplier<? extends DNode> dStructSupplier;

        if (appId() == VINDICTUS) {
            dStructSupplier = DNodeVin::new;
        } else {
            dStructSupplier = DNode::new;
        }

        bspData.nodes = readDStructChunksLump(LumpType.LUMP_NODES, dStructSupplier);
        L.fine(String.format("%d nodes", bspData.nodes.size()));
    }

    public void loadLeaves() {
        if (bspData.leaves != null) {
            return;
        }

        Function<Integer, Supplier<? extends DLeaf>> dStructSupplierCreator = lumpVersion -> {
            if (appId() == VINDICTUS) {
                // use special struct for Vindictus
                return DLeafVin::new;
            } else if (lumpVersion == 0 && bspFile.getVersion() == 19) {
                // read AmbientLighting, it was used in initial Half-Life 2 maps
                // only and doesn't exist in newer or older versions
                return DLeafV0::new;
            } else {
                return DLeafV1::new;
            }
        };

        bspData.leaves = readDStructChunksLump(LumpType.LUMP_LEAFS, dStructSupplierCreator);
        L.fine(String.format("%d leaves", bspData.leaves.size()));
    }

    public void loadLeafFaces() {
        if (bspData.leafFaces != null) {
            return;
        }

        LumpReader<List<Integer>> lumpReader =
                appId() != VINDICTUS ? new UShortChunksLumpReader() : new IntegerChunksLumpReader();

        bspData.leafFaces = readLump(LumpType.LUMP_LEAFFACES, lumpReader);
        L.fine(String.format("%d leaf faces", bspData.leafFaces.size()));
    }

    public void loadLeafBrushes() {
        if (bspData.leafBrushes != null) {
            return;
        }

        LumpReader<List<Integer>> lumpReader =
                appId() != VINDICTUS ? new UShortChunksLumpReader() : new IntegerChunksLumpReader();

        bspData.leafBrushes = readLump(LumpType.LUMP_LEAFBRUSHES, lumpReader);
        L.fine(String.format("%d leaf brushes", bspData.leafBrushes.size()));
    }

    public void loadOverlays() {
        if (bspData.overlays != null) {
            return;
        }

        Supplier<? extends DOverlay> dStructSupplier;

        if (appId() == VINDICTUS) {
            dStructSupplier = DOverlayVin::new;
        } else if (appId() == DOTA_2_BETA) {
            dStructSupplier = DOverlayDota2::new;
        } else {
            dStructSupplier = DOverlay::new;
        }

        bspData.overlays = readDStructChunksLump(LumpType.LUMP_OVERLAYS, dStructSupplier);
        L.fine(String.format("%d overlays", bspData.overlays.size()));

        // read fade distances
        if (bspData.overlayFades == null) {
            bspData.overlayFades = readDStructChunksLump(LumpType.LUMP_OVERLAY_FADES, DOverlayFade::new);
            L.fine(String.format("%d overlay fades", bspData.overlayFades.size()));
        }

        // read CPU/GPU levels
        if (bspData.overlaySysLevels == null) {
            // black mesa uses this lump for displacement multiblend
            if (appId() == BLACK_MESA)
                bspData.overlaySysLevels = Collections.emptyList();
            else
                bspData.overlaySysLevels = readDStructChunksLump(LumpType.LUMP_OVERLAY_SYSTEM_LEVELS, DOverlaySystemLevel::new);

            L.fine(String.format("%d overlay sys levels", bspData.overlaySysLevels.size()));
        }
    }

    public void loadAreaportals() {
        if (bspData.areaportals != null) {
            return;
        }

        Supplier<? extends DAreaportal> dStructSupplier;

        if (appId() == VINDICTUS) {
            dStructSupplier = DAreaportalVin::new;
        } else {
            dStructSupplier = DAreaportal::new;
        }

        bspData.areaportals = readDStructChunksLump(LumpType.LUMP_AREAPORTALS, dStructSupplier);
        L.fine(String.format("%d areaportals", bspData.areaportals.size()));
    }

    public void loadOccluders() {
        if (bspData.occluderDatas != null) {
            return;
        }

        Function<Integer, LumpReader<? extends OcclusionLumpReader.OcclusionData<? extends DOccluderData>>>
                lumpReaderCreator = lumpVersion -> {

            int alteredLumpVersion = lumpVersion;

            // Contagion maps report lump version 0, but they're actually
            // using 1
            if (bspFile.getSourceApp().getAppId() == CONTAGION) {
                alteredLumpVersion = 1;
            }

            Supplier<? extends DOccluderData> dStructSupplier;
            if (alteredLumpVersion == 0) {
                dStructSupplier = DOccluderData::new;
            } else {
                dStructSupplier = DOccluderDataV1::new;
            }

            return new OcclusionLumpReader<>(dStructSupplier);
        };

        OcclusionLumpReader.OcclusionData<? extends DOccluderData> occlusionData = readLump(
                LumpType.LUMP_OCCLUSION,
                lumpReaderCreator,
                OcclusionLumpReader.OcclusionData::new
        );

        bspData.occluderDatas = occlusionData.dOccluderData;
        bspData.occluderPolyDatas = occlusionData.dOccluderPolyData;
        bspData.occluderVerts = occlusionData.vertexIndices;

        L.fine(String.format("%d occluders", bspData.occluderDatas.size()));
        L.fine(String.format("%d occluder poly data", bspData.occluderPolyDatas.size()));
        L.fine(String.format("%d occluder vertices", bspData.occluderVerts.size()));
    }

    public void loadFlags() {
        if (bspData.mapFlags != null) {
            return;
        }

        bspData.mapFlags = readLump(LumpType.LUMP_MAP_FLAGS, new MapFlagsLumpReader());
        L.fine(String.format("map flags: %s", bspData.mapFlags));
    }

    public void loadPrimitives() {
        if (bspData.prims != null) {
            return;
        }

        bspData.prims = readDStructChunksLump(LumpType.LUMP_PRIMITIVES, DPrimitive::new);
        L.fine(String.format("%d primitives", bspData.prims.size()));
    }

    public void loadPrimIndices() {
        if (bspData.primIndices != null) {
            return;
        }

        bspData.primIndices = readLump(LumpType.LUMP_PRIMINDICES, new UShortChunksLumpReader());
        L.fine(String.format("%d primitives indices", bspData.primIndices.size()));
    }

    public void loadPrimVerts() {
        if (bspData.primVerts != null) {
            return;
        }

        bspData.primVerts = readDStructChunksLump(LumpType.LUMP_PRIMVERTS, DVertex::new);
        L.fine(String.format("%d primitives vertices", bspData.primVerts.size()));
    }


    /**
     * Overloaded method for {@link #readDStructChunksLump(LumpType, Function)}.
     * Useful when the {@link DStruct} {@link Supplier} ist lump version independent.
     *
     * @see #readDStructChunksLump(LumpType, Function)
     */
    private <T extends DStruct> List<T> readDStructChunksLump(
            LumpType lumpType,
            Supplier<? extends T> dStructSupplier
    ) {
        return readDStructChunksLump(
                lumpType,
                integer -> dStructSupplier
        );
    }

    /**
     * Reads the lump specified by {@code lumpType} with a {@link DStructChunksLumpReader<T>}
     * and returns the result.
     *
     * @param lumpType the type of lump to read
     * @param dStructSupplierCreator creator function, that creates a {@link DStruct} {@link Supplier} for
     * a specific lump version
     * @param <T> the type of {@link DStruct}
     *
     * @return a list of read {@link DStruct}s
     */
    private <T extends DStruct> List<T> readDStructChunksLump(
            LumpType lumpType,
            Function<? super Integer, Supplier<? extends T>> dStructSupplierCreator
    ) {
        return readLump(
                lumpType,
                lumpVersion -> new DStructChunksLumpReader<>(dStructSupplierCreator.apply(lumpVersion)),
                Collections::emptyList
        );
    }

    /**
     * Reads the lump specified by {@code lumpType} with the {@link LumpReader} specified by {@code lumpReader}
     * and returns the result.
     *
     * @param lumpType the type of lump to read
     * @param lumpReader the {@link LumpReader} used to read the lump
     * @param <T> the lumpReaders result type
     *
     * @return the result produced by the lumpReader
     * @see #readLump(LumpType, Function, Supplier)
     */
    private <T> T readLump(
            LumpType lumpType,
            LumpReader<? extends T> lumpReader
    ) {
        return readLump(
                lumpType,
                lumpVersion -> lumpReader,
                lumpReader::defaultData
        );
    }

    /**
     * Reads the lump specified by {@code lumpType} with a {@link LumpReader} created by
     * passing the lump version to the function specified by {@code lumpReader}.
     *
     * @param lumpType the type of lump to read
     * @param lumpReaderCreator a function, that creates a suitable {@link LumpReader} for a specified lump version
     * @param defaultDataSupplier a supplier used to create a default data object, in case the lump cannot be read
     * @param <T> the lumpReaders result type
     *
     * @return the result produced by the lumpReader
     */
    private <T> T readLump(
            LumpType lumpType,
            Function<? super Integer, ? extends LumpReader<? extends T>> lumpReaderCreator,
            Supplier<? extends T> defaultDataSupplier
    ) {
        if (!bspFile.canReadLump(lumpType)) {
            L.warning(String.format("Tried reading lump '%s', but it is not supported by the bsp's version", lumpType));
            return defaultDataSupplier.get();
        }

        Lump lump = bspFile.getLump(lumpType);
        LumpReader<? extends T> lumpReader = lumpReaderCreator.apply(lump.getVersion());
        return readAbstractLump(lump, lumpReader);
    }

    /**
     * Same as {@link #readLump(LumpType, Function, Supplier)} but for gameLumps
     *
     * @see #readLump(LumpType, Function, Supplier)
     */
    private <T> T readGameLump(
            String sid,
            Function<Integer, ? extends LumpReader<? extends T>> lumpReaderCreator,
            Supplier<? extends T> defaultDataSupplier
    ) {
        GameLump gameLump = bspFile.getGameLump(sid);
        if (gameLump == null) {
            L.warning(String.format("Tried reading game lump '%s', but it was not present in the bsp", sid));
            return defaultDataSupplier.get();
        }

        LumpReader<? extends T> lumpReader = lumpReaderCreator.apply(gameLump.getVersion());
        return readAbstractLump(gameLump, lumpReader);
    }

    /**
     * Reads the specfied lump with the specified lumpReader and returns the result.
     *
     * @param lump the lump to read
     * @param lumpReader the lumpReader used to read the lump content
     * @param <T> the lumpReaders result type
     *
     * @return the result produced by the lumpReader
     */
    private <T> T readAbstractLump(AbstractLump lump, LumpReader<? extends T> lumpReader) {
        // don't try to read empty lumps
        if (lump.getLength() == 0) {
            L.warning(String.format("Lump %s is empty", lump));
            return lumpReader.defaultData();
        }

        L.fine(String.format("Reading %s", lump));

        ByteBuffer buffer = lump.getBuffer();
        buffer.rewind();

        T returnData;
        try {
            returnData = lumpReader.read(buffer);
        } catch (Exception e) {
            L.log(Level.WARNING, String.format("An error occurred while trying to read lump %s", lump), e);
            returnData = lumpReader.defaultData();
        }

        L.fine(String.format("Finished reading %s", lump));
        return returnData;
    }

    public BspFile getBspFile() {
        return bspFile;
    }

    public BspData getData() {
        return bspData;
    }
}

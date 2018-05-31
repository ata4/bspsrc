/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.lump;

/**
 * Enumeration of lump types that are or were used in BSP files.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum LumpType {

    LUMP_UNKNOWN(-1),
    // v21
    LUMP_PROPCOLLISION(22, 21),
    LUMP_PROPHULLS(23, 21),
    LUMP_PROPHULLVERTS(24, 21),
    LUMP_PROPTRIS(25, 21),
    LUMP_PROP_BLOB(49, 21),
    LUMP_PHYSLEVEL(62, 21),
    LUMP_DISP_MULTIBLEND(63, 21),
    // v20
    LUMP_FACEIDS(11, 20),
    LUMP_UNUSED0(22, 20),
    LUMP_UNUSED1(23, 20),
    LUMP_UNUSED2(24, 20),
    LUMP_UNUSED3(25, 20),
    LUMP_PHYSDISP(28, 20),
    LUMP_WATEROVERLAYS(50, 20),
    LUMP_LEAF_AMBIENT_INDEX_HDR(51, 20),
    LUMP_LEAF_AMBIENT_INDEX(52, 20),
    LUMP_LIGHTING_HDR(53, 20),
    LUMP_WORLDLIGHTS_HDR(54, 20),
    LUMP_LEAF_AMBIENT_LIGHTING_HDR(55, 20),
    LUMP_LEAF_AMBIENT_LIGHTING(56, 20),
    LUMP_XZIPPAKFILE(57, 20),
    LUMP_FACES_HDR(58, 20),
    LUMP_MAP_FLAGS(59, 20),
    LUMP_OVERLAY_FADES(60, 20),
    LUMP_OVERLAY_SYSTEM_LEVELS(61, 20),
    // v19 and previous
    LUMP_ENTITIES(0),
    LUMP_PLANES(1),
    LUMP_TEXDATA(2),
    LUMP_VERTEXES(3),
    LUMP_VISIBILITY(4),
    LUMP_NODES(5),
    LUMP_TEXINFO(6),
    LUMP_FACES(7),
    LUMP_LIGHTING(8),
    LUMP_OCCLUSION(9),
    LUMP_LEAFS(10),
    LUMP_UNDEFINED(11),
    LUMP_EDGES(12),
    LUMP_SURFEDGES(13),
    LUMP_MODELS(14),
    LUMP_WORLDLIGHTS(15),
    LUMP_LEAFFACES(16),
    LUMP_LEAFBRUSHES(17),
    LUMP_BRUSHES(18),
    LUMP_BRUSHSIDES(19),
    LUMP_AREAS(20),
    LUMP_AREAPORTALS(21),
    LUMP_PORTALS(22),
    LUMP_CLUSTERS(23),
    LUMP_PORTALVERTS(24),
    LUMP_CLUSTERPORTALS(25),
    LUMP_DISPINFO(26),
    LUMP_ORIGINALFACES(27),
    LUMP_UNUSED(28),
    LUMP_PHYSCOLLIDE(29),
    LUMP_VERTNORMALS(30),
    LUMP_VERTNORMALINDICES(31),
    LUMP_DISP_LIGHTMAP_ALPHAS(32),
    LUMP_DISP_VERTS(33),
    LUMP_DISP_LIGHTMAP_SAMPLE_POSITIONS(34),
    LUMP_GAME_LUMP(35),
    LUMP_LEAFWATERDATA(36),
    LUMP_PRIMITIVES(37),
    LUMP_PRIMVERTS(38),
    LUMP_PRIMINDICES(39),
    LUMP_PAKFILE(40),
    LUMP_CLIPPORTALVERTS(41),
    LUMP_CUBEMAPS(42),
    LUMP_TEXDATA_STRING_DATA(43),
    LUMP_TEXDATA_STRING_TABLE(44),
    LUMP_OVERLAYS(45),
    LUMP_LEAFMINDISTTOWATER(46),
    LUMP_FACE_MACRO_TEXTURE_INFO(47),
    LUMP_DISP_TRIS(48),
    LUMP_PHYSCOLLIDESURFACE(49);

    /**
     * Position of the lump type in the lump table
     */
    private final int index;

    /**
     * BSP version where this lump type has been used first.
     * -1 for unknown/since time being.
     */
    private final int bspVersion;

    private LumpType(int index, int bspVersion) {
        this.index = index;
        this.bspVersion = bspVersion;
    }

    private LumpType(int index) {
        this(index, -1);
    }

    /**
     * Returns the type for a lump name in a specific BSP version.
     *
     * @param name lump name
     * @param bspVersion use lump types that are used in this BSP version
     * @return lump type
     */
    public static LumpType get(String name, int bspVersion) {
        for (LumpType type : values()) {
            if (type.name().equals(name) && type.bspVersion <= bspVersion) {
                return type;
            }
        }
        return LUMP_UNKNOWN;
    }

    /**
     * Returns the type for a lump name in BSP v19. Lump types for BSP v20 and
     * above will not be considered.
     *
     * @param name lump name
     * @return lump type
     */
    public static LumpType get(String name) {
        return get(name, -1);
    }

    /**
     * Returns the type for a lump table index in a specific BSP version.
     *
     * @param index lump table index
     * @param bspVersion use lump types that are used in this BSP version
     * @return lump type
     */
    public static LumpType get(int index, int bspVersion) {
        for (LumpType type : values()) {
            if (type.index == index && type.bspVersion <= bspVersion) {
                return type;
            }
        }
        return LUMP_UNKNOWN;
    }

    /**
     * Returns the type for a lump table index in BSP v19. Lump types for
     * BSP v20 and above will not be considered.
     *
     * @param index lump table index
     * @return lump type
     */
    public static LumpType get(int index) {
        return get(index, -1);
    }

    /**
     * Returns Position of the lump type in the lump table
     *
     * @return lump table ID
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the position of the lump type in the lump table.
     * If the fist use of this type is unknown or if it has been used for the
     * time being, -1 is returned.
     *
     * @return lump table ID
     */
    public int getBspVersion() {
        return bspVersion;
    }
}

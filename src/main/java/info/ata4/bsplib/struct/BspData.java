/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.struct;

import info.ata4.bsplib.entity.Entity;

import java.util.List;
import java.util.Set;

/**
 * Data structure of the BSP file and their lumps.
 * It doesn't cover all known lumps yet. Only those that are used by
 * BSPSource are currently implemented.
 *
 * Yes, there are NO getters and setters and NO final fields!
 * Encapsulation just doesn't make much sense in this case...
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspData {

    public List<? extends DAreaportal> areaportals;
    public List<DBrush> brushes;
    public List<? extends DBrushSide> brushSides;
    public List<DCubemapSample> cubemaps;
    public List<? extends DDispInfo> dispinfos;
    public List<DDispTri> disptris;
    public List<DDispVert> dispverts;
    public List<? extends DDispMultiBlend> dispmultiblend;
    public List<? extends DEdge> edges;
    public List<? extends DFace> faces;
    public List<? extends DFace> origFaces;
    public List<? extends DLeaf> leaves;
    public List<? extends DModel> models;
    public List<? extends DNode> nodes;
    public List<? extends DOccluderData> occluderDatas;
    public List<DOccluderPolyData> occluderPolyDatas;
    public List<? extends DOverlay> overlays;
    public List<DOverlayFade> overlayFades;
    public List<DOverlaySystemLevel> overlaySysLevels;
    public List<DPlane> planes;
    public List<DPrimitive> prims;
    public List<Integer> primIndices;
    public List<DVertex> primVerts;
    public List<? extends DStaticProp> staticProps;
    public List<DTexData> texdatas;
    public List<? extends DTexInfo> texinfos;
    public List<DVertex> clipPortalVerts;
    public List<DVertex> verts;
    public List<Entity> entities;
    public List<Integer> leafBrushes;
    public List<Integer> leafFaces;
    public List<Integer> occluderVerts;
    public List<? extends Integer> surfEdges;
    public List<String> staticPropName;
    public List<Integer> staticPropLeaf;
    public List<String> texnames;
    public Set<LevelFlag> mapFlags;

}

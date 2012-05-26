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

import info.ata4.bsplib.BspFileReader;
import info.ata4.bsplib.struct.DBrush;
import info.ata4.bsplib.struct.DBrushSide;
import info.ata4.bsplib.struct.DModel;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bspsrc.BspSourceConfig;
import info.ata4.bspsrc.Texture;
import info.ata4.bspsrc.VmfWriter;
import info.ata4.bspsrc.util.TreeLimit;
import info.ata4.bspsrc.util.Winding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Decompiling module to rebuild brushes from the LUMP_BRUSHES and LUMP_BRUSHSIDES lumps.
 *
 * Based on Vmex.vmfbrushes() and Vmex.writebrush()
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BrushSource extends ModuleDecompile {

    // logger
    private static final Logger L = Logger.getLogger(BrushSource.class.getName());
    
    // parent module
    private final BspDecompiler parent;
    
    // sub-modules
    private final BspSourceConfig config;
    private final TextureSource texsrc;
    private final BspProtection bspprot;
    
    // additional model data
    private List<DBrushModel> models = new ArrayList<DBrushModel>();
    
    // amount of world brushes
    private int worldbrushes = 0;
    
    // brush side ID mappings
    Map<Integer, Integer> brushSideToID = new HashMap<Integer, Integer>();
    Map<Integer, Integer> brushIndexToID = new HashMap<Integer, Integer>();

    public BrushSource(BspFileReader reader, VmfWriter writer, BspSourceConfig config,
            BspDecompiler parent, TextureSource texsrc, BspProtection bspprot) {
        super(reader, writer);
        this.config = config;
        this.parent = parent;
        this.texsrc = texsrc;
        this.bspprot = bspprot;

        assignBrushes();
    }

    /**
     * Walks the map's BSP tree to associate brushes with entities and to find
     * the index of the last worldbrush.
     */
    private void assignBrushes() {
        L.fine("Walking BSP tree");
        
        // walk the BSP tree
        // to from the headnode of each model
        // to calculate the minimum and maximum brush in the tree
        // much simpler than the guessing method
        // plus this recovers null-faced brushes
        
        TreeLimit tl = new TreeLimit(bsp);
        
        // walk model 0 (worldspawn model)
        tl.walk(0);

        L.fine("Walked worldspawn tree");

        worldbrushes = tl.getMaxBrushLeaf() + 1;

        for (DModel model : bsp.models) {
            tl.reset();
            tl.walk(model.headnode);

            DBrushModel bmodel = new DBrushModel();
            bmodel.fstbrush = tl.getMinBrushLeaf();
            bmodel.numbrush = tl.getMaxBrushLeaf() - tl.getMinBrushLeaf() + 1;
            models.add(bmodel);
        }

        L.log(Level.FINE, "Largest worldbrush: {0}", worldbrushes);
    }

    /**
     * Writes all world brushes. Depending on the settings, some brushes may be
     * skipped so the entity decompiler can use them.
     */
    public void writeBrushes() {
        L.info("Writing brushes and planes");

        for (int i = 0; i < worldbrushes; i++) {
            DBrush brush = bsp.brushes.get(i);

            // skip details
            if (config.writeDetails && brush.isSolid() && brush.isDetail()) {
                continue;
            }

            // skip areaportals
            if (config.writeAreaportals && brush.isAreaportal()) {
                continue;
            }
            
            // NOTE: occluder brushes aren't worldbrushes

            writeBrush(i);
        }
    }
    
    public void writeBrush(int ibrush, Vector3f origin, Vector3f angles) {
        DBrush brush = bsp.brushes.get(ibrush);
        
        int brushID = parent.nextBrushID();
        
        // map brush index to ID
        brushIndexToID.put(ibrush, brushID);
        
        writer.start("solid");
        writer.put("id", brushID);

        if (config.isDebug()) {
            // write contents for debugging
            writer.put("bspsrc_brush_index", ibrush);
            writer.put("bspsrc_brush_contents", brush.contents.toString());
        }

        if (L.isLoggable(Level.FINER)) {
            L.log(Level.FINER, "Brush {0} {1}", new Object[]{brush, brush.contents});
        }

        int nullsides = 0;

        for (int i = 0; i < brush.numside; i++) {
            int ibrushside = brush.fstside + i;
            
            if (bsp.brushSides.get(ibrushside).bevel) {
                continue;
            }
            
            Winding wind = Winding.windFromSide(bsp, brush, i);

            // TODO: what exactly does this?
            // crunch faces
            wind.removeDegenerated();

            if (wind.isHuge() && L.isLoggable(Level.FINER)) {
                L.log(Level.FINER, "Side {0} of brush {1} is huge", new Object[]{i, brush});
            }
            
            // rotate
            if (angles != null) {
                wind.rotate(angles);
            }
            
            // translate to origin
            if (origin != null) {
                wind.translate(origin);
            }

            if (wind.isEmpty()) {
                // skip sides with no vertices
                nullsides++;
                if (L.isLoggable(Level.FINER)) {
                    L.log(Level.FINER, "Side {0} of brush {1} is null", new Object[]{i, brush});
                }
            } else if (wind.size() < 3) {
                // skip sides with too few vertices
                nullsides++;
                if (L.isLoggable(Level.FINER)) {
                    L.log(Level.FINER, "Side {0} of brush {1} was overcrunched", new Object[]{i, brush});
                }
            } else {
                writeSide(ibrushside, ibrush, wind, origin, angles);
            }
        }

        // all brush sides invalid = invalid brush
        if (nullsides == brush.numside) {
            L.log(Level.WARNING, "Brush {0} is null", brush);
        }
        
        if (bspprot.isProtectedBrush(brush)) {
            parent.writeMetaVisgroup("VMEX flagged brushes");
        }

        writer.end("solid");
    }
    
    public void writeBrush(int ibrush) {
        writeBrush(ibrush, null, null);
    }

    private void writeSide(int ibrushside, int ibrush, Winding wind, Vector3f origin, Vector3f angles) {
        DBrushSide brushSide = bsp.brushSides.get(ibrushside);
        
        if (brushSide.bevel) {
            // don't output bevel faces - they lead to bad brushes
            return;
        }
        
        // calculate plane vectors
        Vector3f[] plane = wind.getVertexPlane();
        
        Vector3f e1 = plane[0];
        Vector3f e2 = plane[1];
        Vector3f e3 = plane[2];
        
        if (!e1.isValid() || !e2.isValid() || !e3.isValid()) {
            L.log(Level.WARNING, "Brush side with wind {0} is invalid", wind);
        }
        
        // calculate plane normal
        // NOTE: the plane normal from the BSP could be invalid if the brush was
        //       rotated! better re-calculate it every time.
        Vector3f ev12 = e2.sub(e1);
        Vector3f ev13 = e3.sub(e1);
        Vector3f normal = ev12.cross(ev13).normalize();
        
        Texture texture = texsrc.getTexture(brushSide.texinfo, origin, angles, normal);
        
        // set face texture string
        if (!config.faceTexture.equals("")) {
            texture.setMaterial(config.faceTexture);
        } else {
            // fix tool textures
            if (config.fixToolTextures) {
                texsrc.fixToolTexture(ibrush, ibrushside, texture);
            }
        }
        
        int sideID = parent.nextSideID();
        
        // add side id to cubemap side list
        if (texture.getData() != null) {
            texsrc.addBrushSideID(texture.getData().texname, sideID);
        }

        // map brush side index to brush side ID
        brushSideToID.put(ibrushside, sideID);

        writer.start("side");
        writer.put("id", sideID);
        
        if (config.isDebug()) {
            writer.put("bspsrc_brushside_index", ibrushside);
            writer.put("bspsrc_normal", normal);
            writer.put("bspsrc_winding", wind.toString());
            
            if (brushSide.texinfo != -1) {
                writer.put("bspsrc_texinfo_index", brushSide.texinfo);
                writer.put("bspsrc_texinfo_flags", bsp.texinfos.get(brushSide.texinfo).flags.toString());
            }
        }
        
        writer.put("plane", e1, e2, e3);
        writer.put("smoothing_groups", 0);
        writer.put(texture);

        writer.end("side");
    }

    public void writeModel(int imodel, Vector3f origin, Vector3f angles) {
        DBrushModel bmodel;
        
        try {
            bmodel = models.get(imodel);
        } catch (IndexOutOfBoundsException ex) {
            L.log(Level.WARNING, "Invalid model index {0}", imodel);
            return;
        }
        
        for (int i = 0; i < bmodel.numbrush; i++) {
            writeBrush(bmodel.fstbrush + i, origin, angles);
        }
    }
    
    public void writeModel(int imodel) {
        writeModel(imodel);
    }

    public int getWorldbrushes() {
        return worldbrushes;
    }
    
    private class DBrushModel {
        private int fstbrush;
        private int numbrush;
    }
}

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

import info.ata4.bsplib.struct.DBrush;
import info.ata4.bsplib.struct.DBrushSide;
import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.struct.DPlane;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bspsrc.ToolTexture;
import info.ata4.bspsrc.util.Winding;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;

/**
 * A module to check if the map has been protected by the mapper with at least
 * one of these methods:
 * - "no_decomp" entity property
 * - "tools/locked" texture
 * - protection prefab
 * - encrypted entities by BSPProtect
 * - obfuscated entities by IID
 * - texinfo hack by IID_BSP
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspProtection extends BspSourceModule {

    // constants
    private static final float EPS_SIZE = 0.01f;
    private static final float ALIGNED_ALPHA = 0.99f;
    private static final float NODRAW_RATIO_LIMIT = 0.9f;
    private static final String BSPPROTECT_FILE = "entities.dat";
    private static final String VMEX_LOCKED_TEX = "tools/locked";
    private static final String VMEX_LOCKED_ENT = "no_decomp";
    private static final Vector3f PB1 = new Vector3f(1.0f, 4.0f, 9.0f);
    private static final Vector3f PB2 = new Vector3f(4.0f, 9.0f, 1.0f);
    private static final Vector3f PB3 = new Vector3f(9.0f, 1.0f, 4.0f);

    // logger
    private static final Logger L = Logger.getLogger(BspProtection.class.getName());

    // sub-modules
    private final TextureSource texsrc;

    // flags
    private boolean protectedFlag;
    private boolean flaggedEnt;
    private boolean flaggedTex;
    private boolean flaggedBrush;
    private boolean encryptedEnt;
    private boolean obfuscatedEnt;
    private boolean modifedTexinfo;

    public BspProtection(BspSourceModule parent, TextureSource texsrc) {
        super(parent);
        this.texsrc = texsrc;
    }
    
    public BspProtection(BspDecompiler parent) {
        super(parent);
        this.texsrc = parent.getTextureSource();
    }

    public boolean check() {
        protectedFlag = false;
        flaggedEnt = false;
        flaggedTex = false;
        flaggedBrush = false;
        encryptedEnt = false;
        obfuscatedEnt = false;
        modifedTexinfo = false;

        checkBrushes();
        checkBrushSides();
        checkEntities();
        checkTextures();
        checkPakfile();

        if (!protectedFlag) {
            L.fine("Nothing found");
        }

        return protectedFlag;
    }

    public boolean isProtected() {
        return protectedFlag;
    }

    public boolean hasEntityFlag() {
        return flaggedEnt;
    }

    public boolean hasTextureFlag() {
        return flaggedTex;
    }

    public boolean hasBrushFlag() {
        return flaggedBrush;
    }

    public boolean hasEncryptedEntities() {
        return encryptedEnt;
    }

    public boolean hasObfuscatedEntities() {
        return obfuscatedEnt;
    }

    public boolean hasModifiedTexinfo() {
        return modifedTexinfo;
    }
    
    public List<String> getProtectionMethods() {
        List<String> methods = new ArrayList<String>();
        
        if (hasEntityFlag()) {
            methods.add("VMEX entity flag (no_decomp)");
        }

        if (hasTextureFlag()) {
            methods.add("VMEX texture flag (tools/locked)");
        }

        if (hasBrushFlag()) {
            methods.add("VMEX protector brush flag");
        }

        if (hasEncryptedEntities()) {
            methods.add("BSPProtect entity encryption");
        }

        if (hasObfuscatedEntities()) {
            methods.add("IID entity obfuscation");
        }

        if (hasModifiedTexinfo()) {
            methods.add("IID nodraw texture hack");
        }
        
        return methods;
    }

    private void checkBrushes() {
        L.fine("Checking for protector prefab");

        boolean m1 = false;
        boolean m2 = false;
        boolean m3 = false;

        // check every brush
        for (DBrush b : bsp.brushes) {
            // ignore brushes that don't fit
            if (!isAlignedBrush(b) || !isSameTexBrush(b)) {
                continue;
            }

            // get brush dimensions
            Vector3f bsize = getBrushSize(b);
            
            // check brush dimensions with prefab constants
            if (PB1.sub(bsize).length() < EPS_SIZE) {
                m1 = true;
            }
            if (PB2.sub(bsize).length() < EPS_SIZE) {
                m2 = true;
            }
            if (PB3.sub(bsize).length() < EPS_SIZE) {
                m3 = true;
            }

            // check if all three brushes exists
            if (m1 && m2 && m3) {
                L.fine("Found protector prefab!");
                flaggedBrush = true;
                protectedFlag = true;
                break;
            }
        }
    }

    private void checkBrushSides() {
        L.log(Level.FINE, "Checking for nodraw brush sides (ratio limit: {0})", NODRAW_RATIO_LIMIT);

        double nodrawSides = 0;

        for (DBrushSide bs : bsp.brushSides) {
            if (bs.texinfo == 0) {
                nodrawSides++;
            }
        }

        double nodrawRatio = nodrawSides / bsp.brushSides.size();

        // check if there're too many nodraw brush sides
        modifedTexinfo = nodrawRatio > NODRAW_RATIO_LIMIT;
        if (modifedTexinfo) {
            L.fine("Found nodraw hack!");
            protectedFlag = true;
        }
    }

    private void checkEntities() {
        L.fine("Checking for entity lock key \"" + VMEX_LOCKED_ENT + "\" and obfuscated targetnames");
        
        int targetnames = 0;
        int targetnamesObfs = 0;

        for (Entity ent : bsp.entities) {
            String targetName = ent.getTargetName();

            // check for obfuscated target names
            if (targetName != null) {
                targetnames++;

                if (targetName.matches("^[0-9]+$")) {
                    targetnamesObfs++;
                }
            }

            // search for no_decomp entity property
            for (String key : ent.getKeys()) {
                if (key.equals(VMEX_LOCKED_ENT)) {
                    L.fine("Found lock key!");
                    flaggedEnt = true;
                    protectedFlag = true;
                }
            }
        }

        // all targetnames are numeric?
        obfuscatedEnt = targetnames > 0 && targetnames == targetnamesObfs;
        if (obfuscatedEnt) {
            L.fine("Found obfuscation!");
            protectedFlag = true;
        }
    }

    /**
     * Checks for the lock-texture
     */
    private void checkTextures() {
        L.fine("Checking for lock texture \"" + VMEX_LOCKED_TEX + "\"");

        // search for tools/locked texture
        for (String texname : bsp.texnames) {
            if (texname.equalsIgnoreCase(VMEX_LOCKED_TEX)) {
                L.fine("Found lock texture!");
                flaggedTex = true;
                protectedFlag = true;
                return;
            }
        }
    }

    /**
     * Searches for the "entities.dat" file in the pakfile, which contains the
     * ICE encrypted entity lump created by BSPProtect.
     * The visible entitly lump will contain the worldspawn only if the
     * map file has been encrypted with this tool.
     */
    private void checkPakfile() {
        L.fine("Checking for encrypted entities inside pakfile (file: \"" + BSPPROTECT_FILE + "\")");

        // BSPProtect currently works with Source 2007/2009 aka Orange Box only
        if (bspFile.getVersion() != 20) {
            encryptedEnt = false;
            return;
        }

        ZipArchiveInputStream zis = bspFile.getPakFile().getArchiveInputStream();

        try {
            ZipArchiveEntry ze;

            while ((ze = zis.getNextZipEntry()) != null) {
                if (ze.getName().equals(BSPPROTECT_FILE)) {
                    L.fine("Found encrypted entities!");
                    encryptedEnt = true;
                    protectedFlag = true;
                    break;
                }
            }
        } catch (IOException ex) {
            L.log(Level.WARNING, "Couldn't read pakfile", ex);

            // pakfile broken or missing?
            encryptedEnt = false;
        } finally {
            IOUtils.closeQuietly(zis);
        }
    }

    /**
     * Returns the x, y and z extents of a brush, as a vector.
     *
     * @param brush a brush
     * @return vector with sizes of the bounding box
     */
    private Vector3f getBrushSize(DBrush brush) {
        Vector3f min = Vector3f.MAX_VALUE;
        Vector3f max = Vector3f.MIN_VALUE;

        // get limits for all brush side windings
        for (int i = 0; i < brush.numside; i++) {
            Vector3f[] bounds = Winding.windFromSide(bsp, brush, i).getBounds();
            min = bounds[0].min(min);
            max = bounds[1].max(max);
        }

        return max.sub(min);
    }

    /**
     * Checks if a brush is aligned(?)
     *
     * @param brush a brush
     * @return true, if the brush is aligned
     */
    private boolean isAlignedBrush(DBrush brush) {
        if (brush.numside != 6) {
            return false;
        }

        for (int i = 0; i < 6; i++) {
            DBrushSide bs = bsp.brushSides.get(brush.fstside + i);
            DPlane bpl = bsp.planes.get(bs.pnum);

            for (int j = 0; j < 3; j++) {
                if (Math.abs(bpl.normal.getAxis(j)) > ALIGNED_ALPHA) {
                    return true;
                }
            }
        }

        return true;
    }

    /**
     * Checks if a brush shares the same texture string on all sides
     * 
     * @param brush a brush
     * @return true if all brush sides share the same texture
     */
    private boolean isSameTexBrush(DBrush brush) {
        DBrushSide bs = bsp.brushSides.get(brush.fstside);
        String texname = texsrc.getTextureName(bs.texinfo);

        if (texname.equals(ToolTexture.SKIP)) {
            // this side has no valid texture
            return false;
        }

        for (int i = 1; i < brush.numside; i++) {
            bs = bsp.brushSides.get(brush.fstside + i);
            String nexttexname = texsrc.getTextureName(bs.texinfo);
            
            if (!texname.equalsIgnoreCase(nexttexname)) {
                return false;
            }
        }

        return true;
    }
}

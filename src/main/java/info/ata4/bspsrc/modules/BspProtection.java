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
import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.struct.DBrush;
import info.ata4.bsplib.struct.DBrushSide;
import info.ata4.bsplib.struct.DPlane;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bspsrc.modules.geom.BrushUtils;
import info.ata4.bspsrc.modules.texture.TextureSource;
import info.ata4.bspsrc.modules.texture.ToolTexture;
import info.ata4.log.LogUtils;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class BspProtection extends ModuleRead {

    // constants
    public static final String BSPPROTECT_FILE = "entities.dat";
    public static final String VMEX_LOCKED_TEX = "tools/locked";
    public static final String VMEX_LOCKED_ENT = "no_decomp";

    private static final float EPS_SIZE = 0.01f;
    private static final float ALIGNED_ALPHA = 0.99f;
    private static final float NODRAW_RATIO_LIMIT = 0.9f;

    private static final Vector3f PB1 = new Vector3f(1, 4, 9);
    private static final Vector3f PB2 = new Vector3f(4, 9, 1);
    private static final Vector3f PB3 = new Vector3f(9, 1, 4);

    // logger
    private static final Logger L = LogUtils.getLogger();

    // sub-modules
    private final TextureSource texsrc;

    // flags
    private boolean flaggedEnt;
    private boolean flaggedTex;
    private boolean flaggedBrush;
    private boolean encryptedEnt;
    private boolean obfuscatedEnt;
    private boolean modifedTexinfo;

    // lists of protecting elements
    private List<DBrush> protBrushes = new ArrayList<>();
    private List<Entity> protEntities = new ArrayList<>();

    public BspProtection(BspFileReader reader, TextureSource texsrc) {
        super(reader);

        reader.loadEntities();
        reader.loadPlanes();
        reader.loadBrushes();
        reader.loadBrushSides();

        this.texsrc = texsrc;
    }

    public boolean check() {
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

        boolean prot = isProtected();

        if (!prot) {
            L.fine("Nothing found");
        }

        return prot;
    }

    public boolean isProtected() {
        return flaggedEnt || flaggedTex || flaggedBrush || encryptedEnt || obfuscatedEnt || modifedTexinfo;
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

    /**
     * Returns all found protection methods in string form.
     * 
     * @return list of method strings
     */
    public List<String> getProtectionMethods() {
        List<String> methods = new ArrayList<>();

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

    /**
     * Returns all found protector brushes.
     * 
     * @return list of protector brushes
     */
    public List<DBrush> getProtectedBrushes() {
        List<DBrush> list = new ArrayList<>();
        list.addAll(protBrushes);
        return list;
    }

    /**
     * Checks if the given brush is a protector brush.
     * 
     * @param brush
     * @return true if the brush is part of the protection prefab.
     */
    public boolean isProtectedBrush(DBrush brush) {
        return protBrushes.contains(brush);
    }

    /**
     * Returns all found protector entities.
     * 
     * @return list of protector entities
     */
    public List<Entity> getProtectedEntities() {
        List<Entity> list = new ArrayList<>();
        list.addAll(protEntities);
        return list;
    }

    /**
     * Checks if an entitiy contains protection keyvalues.
     * 
     * @param entity
     * @return true if the entity contains protection keyvalues
     */
    public boolean isProtectedEntity(Entity entity) {
        return protEntities.contains(entity);
    }

    private void checkBrushes() {
        L.fine("Checking for protector prefab");

        DBrush b1 = null;
        DBrush b2 = null;
        DBrush b3 = null;

        // check every brush
        for (DBrush b : bsp.brushes) {
            // ignore brushes that don't fit
            if (!isAlignedBrush(b) || !isSameTexBrush(b)) {
                continue;
            }

            // get brush dimensions
            Vector3f bsize = BrushUtils.getBounds(bsp, b).getSize();

            // check brush dimensions with prefab constants
            if (PB1.sub(bsize).length() < EPS_SIZE) {
                b1 = b;
            }
            if (PB2.sub(bsize).length() < EPS_SIZE) {
                b2 = b;
            }
            if (PB3.sub(bsize).length() < EPS_SIZE) {
                b3 = b;
            }

            // check if all three brushes exists
            if (b1 != null && b2 != null && b3 != null) {
                L.fine("Found protector prefab!");
                flaggedBrush = true;

                protBrushes.add(b1);
                protBrushes.add(b2);
                protBrushes.add(b3);

                b1 = null;
                b2 = null;
                b3 = null;
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
                    protEntities.add(ent);
                    flaggedEnt = true;
                }
            }
        }

        // all targetnames are numeric?
        obfuscatedEnt = targetnames > 0 && targetnames == targetnamesObfs;
        if (obfuscatedEnt) {
            L.fine("Found obfuscation!");
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

        try (ZipFile zip = bspFile.getPakFile().getZipFile()) {
            if (zip.getEntries(BSPPROTECT_FILE).iterator().hasNext()) {
                L.fine("Found encrypted entities!");
                encryptedEnt = true;
            }
        } catch (IOException ex) {
            L.log(Level.WARNING, "Couldn't read pakfile", ex);

            // pakfile broken or missing?
            encryptedEnt = false;
        }
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

            for (float value : bpl.normal) {
                if (Math.abs(value) > ALIGNED_ALPHA) {
                    return true;
                }
            }
        }

        return false;
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

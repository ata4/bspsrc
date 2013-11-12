/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.modules.texture;

import info.ata4.bsplib.BspFileReader;
import info.ata4.bsplib.app.SourceAppID;
import info.ata4.bsplib.struct.*;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bspsrc.modules.ModuleRead;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decompiling module to create Texture objects from texture data and to fix
 * texture strings.
 * 
 * Based on texture building part of Vmex.writeside(), Vmex.fixtexturenames()
 * and BSP.gettex()
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TextureSource extends ModuleRead {
    
    // logger
    private static final Logger L = Logger.getLogger(TextureSource.class.getName());

    // regex patterns
    private static final Pattern originPattern = Pattern.compile("_(-?\\d+)_(-?\\d+)_(-?\\d+)?$"); // cubemap position
    private static final Pattern wvtPatchPattern = Pattern.compile("_wvt_patch$"); // world vertex patch
    private static final Pattern waterPatchPattern = Pattern.compile("_depth_(-?\\d+)$"); // water texture patch
    private static final Pattern mapPattern = Pattern.compile("^(maps/[^/]+/)+");
    
    private static final Set<SurfaceFlag> SURFFLAGS_NODRAW = EnumSet.of(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_NOLIGHT);
    
    private Map<Integer, Set<Integer>> cubemapToSideList = new HashMap<Integer, Set<Integer>>();
    private int[] texnameToCubemap;
    
    public TextureSource(BspFileReader reader) {
        super(reader);
        
        reader.loadTexInfo();
        reader.loadTexData();
        reader.loadCubemaps();
        
        texnameToCubemap = new int[bsp.texnames.size()];
        Arrays.fill(texnameToCubemap, -1);
    }

    /**
     * Returns the texture name string for a texinfo index.
     *
     * @param itexinfo texinfo index
     * @return texture name string
     */
    public String getTextureName(short itexinfo) {
        try {
            int ti = bsp.texinfos.get(itexinfo).texdata;
            int td = bsp.texdatas.get(ti).texname;
            return bsp.texnames.get(td);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return ToolTexture.SKIP;
        }
    }

    /**
     * Returns the compiled texture for a texinfo index.
     *
     * @param itexinfo texinfo index
     * @param origin optional texture origin vector, null otherwise
     * @param angles optional texture rotation vector, null otherwise
     * @param normal optional face normal vector, used to fix texture axes
     *        perpendicular to face normals. Null for no correction.
     * @return texture
     */
    public Texture getTexture(short itexinfo, Vector3f origin, Vector3f angles, Vector3f normal) {
        Texture texture = new Texture();

        // align to preset axes
        if (normal != null && !texture.isPerpendicularTo(normal)) {
            texture.alignTo(normal);
        }

        // no texinfo
        if (itexinfo == DTexInfo.TEXINFO_NODE) {
            return texture;
        }

        DTexInfo texinfo;

        try {
            texinfo = bsp.texinfos.get(itexinfo);
        } catch (ArrayIndexOutOfBoundsException ex) {
            L.log(Level.WARNING, "Invalid texinfo index: {0}", itexinfo);
            return texture;
        }

        DTexData texdata;

        try {
            texdata = bsp.texdatas.get(texinfo.texdata);
            texture.setData(texdata);
        } catch (ArrayIndexOutOfBoundsException ex) {
            L.log(Level.WARNING, "Invalid texdata index: {0}", texinfo.texdata);
            return texture;
        }

        try {
            texture.setMaterial(bsp.texnames.get(texdata.texname));
        } catch (ArrayIndexOutOfBoundsException ex) {
            L.log(Level.WARNING, "Invalid texname index: {0}", texdata.texname);
        }
        
        // calculate lightmap scale
        float[][] lvec = texinfo.lightmapVecsLuxels;
        
        Vector3f uaxis = new Vector3f(lvec[0]);
        Vector3f vaxis = new Vector3f(lvec[1]);
        
        float ls = (uaxis.length() + vaxis.length()) / 2.0f;

        if (ls > 0.001f) {
            texture.setLightmapScale(Math.round(1.0f / ls));
        }

        // map texel coordinates to world coordinates
        float[][] tvec = texinfo.textureVecsTexels;
        
        uaxis = new Vector3f(tvec[0]);
        vaxis = new Vector3f(tvec[1]);
        
        float utw = 1.0f / uaxis.length();
        float vtw = 1.0f / vaxis.length();

        uaxis = uaxis.scalar(utw);
        vaxis = vaxis.scalar(vtw);

        float ushift = tvec[0][3];
        float vshift = tvec[1][3];

        // translate to origin
        if (origin != null) {
            ushift -= origin.dot(uaxis) / utw;
            vshift -= origin.dot(vaxis) / vtw;
        }
        
        // rotate texture axes
        if (angles != null) {
            uaxis = uaxis.rotate(angles);
            vaxis = vaxis.rotate(angles);

            // calculate the shift in U/V space due to this rotation
            Vector3f shift = Vector3f.NULL;

            if (origin != null) {
                shift = shift.sub(origin);
            }

            shift = shift.rotate(angles);

            if (origin != null) {
                shift = shift.add(origin);
            }

            ushift -= shift.dot(uaxis) / utw;
            vshift -= shift.dot(vaxis) / vtw;
        }
        
        // normalize shift values
        if (texdata.width != 0) {
            ushift %= texdata.width;
        }
        if (texdata.height != 0) {
            vshift %= texdata.height;
        }
        
        // round scales to 4 decimal digits to fix round-off errors
        // (e.g.: 0.25000018 -> 0.25)
        utw = Math.round(utw * 10000) / 10000f;
        vtw = Math.round(vtw * 10000) / 10000f;

        // create texture axes
        texture.setUAxis(new TextureAxis(uaxis, Math.round(ushift), utw));
        texture.setVAxis(new TextureAxis(vaxis, Math.round(vshift), vtw));

        // re-align texture axes if perpendicular or invalid
        if (normal != null && !texture.isPerpendicularTo(normal)) {
            texture.alignTo(normal);

            if (L.isLoggable(Level.FINE)) {
                L.log(Level.FINE, "Texture TI {0} M {1} was perpendicular to face normal {2}",
                        new Object[] {itexinfo, texture.getMaterial(), normal});
            }
        }

        return texture;
    }
    
    public Texture getTexture(short itexinfo) {
        return getTexture(itexinfo, null, null, null);
    }

    /**
     * Creates a texture with the selected material and default alignment.
     * 
     * @param material material
     * @param normal optional face normal vector, used to fix texture axes
     *               perpendicular to face normals. Null for no correction.
     * @return texture
     */
    public Texture getTexture(String material, Vector3f normal) {
        Texture texture = new Texture();
        
        // align to preset axes
        if (normal != null && !texture.isPerpendicularTo(normal)) {
            texture.alignTo(normal);
        }
        
        texture.setMaterial(material);
        
        return texture;
    }

    /**
     * Converts environment-mapped texture names to original texture names and
     * performs some cleanups. It also assigns cubemap IDs to texname IDs which
     * is later used to create the "sides" properties of env_cubemap entities.
     * 
     * Following operations will be performed:
     * - chop "maps/mapname/[maps/mapname/]" from start of names
     * - chop "_n_n_n or _n_n_n_depth_n from ends of names
     */
    public void fixTexturePaths() {
        for (int i = 0; i < bsp.texnames.size(); i++) {
            String textname = bsp.texnames.get(i);
            
            // search for "maps/<mapname>" prefix
            Matcher matcher = mapPattern.matcher(textname);
            if (matcher.find()) {
                // remove it
                textname = matcher.replaceFirst("");
                
                // search for "_wvt_patch" suffix
                matcher = wvtPatchPattern.matcher(textname);
                if (matcher.find()) {
                    // remove it
                    textname = matcher.replaceFirst("");
                }
                
                // search for "_depth_xxx" suffix
                matcher = waterPatchPattern.matcher(textname);
                if (matcher.find()) {
                    // remove it
                    textname = matcher.replaceFirst("");
                }

                // search for origin coordinates
                matcher = originPattern.matcher(textname);
                if (matcher.find()) {
                    // get origin
                    int cx = Integer.valueOf(matcher.group(1));
                    int cy = Integer.valueOf(matcher.group(2));
                    int cz = Integer.valueOf(matcher.group(3));
                    
                    setCubemapForTexname(i, cx, cy, cz);
                    
                    // remove origin coordinates
                    textname = matcher.replaceFirst("");
                }
            }

            if (!textname.equals(bsp.texnames.get(i))) {
                if (L.isLoggable(Level.FINEST)) {
                    // display differences
                    L.log(Level.FINEST, "{0} -> {1}", new Object[] {bsp.texnames.get(i), textname});
                }

                bsp.texnames.set(i, textname);
            }
        }
    }
    
    /**
     * Corrects the texture for a single brush side by checking the flags.
     * Required for some tool textures that are altered by vbsp.
     * 
     * @param texture texture to fix
     * @param ibrush brush index
     * @param ibrushside brush side index
     * @return previous material name or null if the material wasn't changed
     */
    public String fixToolTexture(Texture texture, int ibrush, int ibrushside) {
        String oldTex = texture.getMaterial();
        String newTex = getToolTexture(ibrush, ibrushside);
        
        if (newTex != null && !newTex.equalsIgnoreCase(oldTex)) {
            if (L.isLoggable(Level.FINEST)) {
                // display differences
                L.log(Level.FINEST, "{0} -> {1}", new Object[] {oldTex, texture.getMaterial()});
            }
            
            texture.setMaterial(newTex);
            return oldTex;
        }
        
        return null;
    }

    private String getToolTexture(int ibrush, int ibrushside) {
        DBrush brush = bsp.brushes.get(ibrush);
        DBrushSide brushSide = bsp.brushSides.get(ibrushside);
        
        Set<SurfaceFlag> surfFlags = EnumSet.noneOf(SurfaceFlag.class);
        
        if (brushSide.texinfo != -1) {
            surfFlags = bsp.texinfos.get(brushSide.texinfo).flags;
        }
        
        if (brush.isDetail()) {
            // Clip
            if (brush.isPlayerClip() && brush.isNpcClip()) {
                return ToolTexture.CLIP;
            }
            
            // Player clip
            if (brush.isPlayerClip()) {
                return ToolTexture.PLAYERCLIP;
            }

            // NPC clip
            if (brush.isNpcClip()) {
                return ToolTexture.NPCCLIP;
            }

            // block light
            if (brush.isOpaque()) {
                return ToolTexture.BLOCKLIGHT;
            }

            // block line of sight
            if (brush.isBlockLos()) {
                return ToolTexture.BLOCKLOS;
            }
            
            return null;
        }
        
        if (bspFile.getSourceApp().getAppID() == SourceAppID.VAMPIRE_BLOODLINES) {
            // too many crucial game-specific tool textures, stop here
            return null;
        }
        
        // nodraw
        if (!brush.isPlayerClip() && !brush.isNpcClip() && surfFlags.equals(SURFFLAGS_NODRAW)) {
            return ToolTexture.NODRAW;
        }

        // areaportal
        if (brush.isAreaportal()) {
            return ToolTexture.AREAPORTAL;
        }

        // invisible
//        if (brush.isGrate() && brush.isTranslucent()) {
//            return ToolTexture.INVIS;
//        }
        
        return null;
    }
    
    private void setCubemapForTexname(int itexname, int cx, int cy, int cz) {
        // search for cubemap with these coordinates
        for (int i = 0; i < bsp.cubemaps.size(); i++) {
            int[] origin = bsp.cubemaps.get(i).origin;

            if (cx == origin[0] || cy == origin[1] || cz == origin[2]) {
                if (L.isLoggable(Level.FINEST)) {
                    L.log(Level.FINEST, "TN: {0} C: {1}", new Object[]{itexname, i});
                }

                // set cubemap index used by this texdata/texname
                texnameToCubemap[itexname] = i;
                return;
            }
        }

        L.log(Level.FINER, "Couldn''t find cubemap for coordinates ({0}, {1}, {2})",
                new Object[]{cx, cy, cz});
    }
    
    public void addBrushSideID(int itexname, int side) {
        int icubemap = texnameToCubemap[itexname];
        
        if (icubemap == -1) {
            // not environment mapped
            return;
        }
        
        Set<Integer> sides = cubemapToSideList.get(icubemap);
        
        // create new side list if required
        if (sides == null) {
            cubemapToSideList.put(icubemap, sides = new HashSet<Integer>());
        }
        
        sides.add(side);
    }
    
    public Set<Integer> getBrushSidesForCubemap(int icubemap) {
        return cubemapToSideList.get(icubemap);
    }
}

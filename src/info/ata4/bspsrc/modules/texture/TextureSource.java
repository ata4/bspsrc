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
import info.ata4.bspsrc.modules.ModuleRead;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

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

    // regex patterns for texture name fixing
    private static final Pattern originPattern = Pattern.compile("_(-?\\d+)_(-?\\d+)_(-?\\d+)?$"); // cubemap position
    private static final Pattern wvtPatchPattern = Pattern.compile("_wvt_patch$"); // world vertex patch
    private static final Pattern waterPatchPattern = Pattern.compile("_depth_(-?\\d+)$"); // water texture patch
    private static final Pattern mapPattern = Pattern.compile("^maps/[^/]+/");
    
    // ID mappings
    private Map<Integer, Set<Integer>> cubemapToSideList = new HashMap<Integer, Set<Integer>>();
    private Map<Integer, Integer> texnameToCubemap = new HashMap<Integer, Integer>();
    public List<String> texnamesFixed = new ArrayList<String>();
    
    // settings
    public boolean useFixedTexnames;
    
    public TextureSource(BspFileReader reader) {
        super(reader);
        
        reader.loadTexInfo();
        reader.loadTexData();
        reader.loadCubemaps();
        
        processTextureNames();
    }

    /**
     * Converts environment-mapped texture names to original texture names and
     * performs some cleanups. It also assigns cubemap IDs to texname IDs which
     * is later used to create the "sides" properties of env_cubemap entities.
     * 
     * Following operations will be performed:
     * - chop "maps/mapname/" from start of names
     * - chop "_n_n_n or _n_n_n_depth_n from end of names
     */
    private void processTextureNames() {
        for (int i = 0; i < bsp.texnames.size(); i++) {
            String textureOld = bsp.texnames.get(i);
            String textureNew = textureOld;
            
            textureNew = canonizeTextureName(textureNew);
            
            // search for "maps/<mapname>" prefix
            Matcher matcher = mapPattern.matcher(textureNew);
            if (matcher.find()) {
                // remove it
                textureNew = matcher.replaceFirst("");
                
                // search for "_wvt_patch" suffix
                matcher = wvtPatchPattern.matcher(textureNew);
                if (matcher.find()) {
                    // remove it
                    textureNew = matcher.replaceFirst("");
                }
                
                // search for "_depth_xxx" suffix
                matcher = waterPatchPattern.matcher(textureNew);
                if (matcher.find()) {
                    // remove it
                    textureNew = matcher.replaceFirst("");
                }

                // search for origin coordinates
                matcher = originPattern.matcher(textureNew);
                if (matcher.find()) {
                    // get origin
                    int cx = Integer.valueOf(matcher.group(1));
                    int cy = Integer.valueOf(matcher.group(2));
                    int cz = Integer.valueOf(matcher.group(3));
                    
                    setCubemapForTexname(i, cx, cy, cz);
                    
                    // remove origin coordinates
                    textureNew = matcher.replaceFirst("");
                }
            }

            // log differences
            if (!textureNew.equalsIgnoreCase(textureOld)) {
                L.log(Level.FINEST, "{0} -> {1}", new Object[] {textureOld, textureNew});
            }
            
            texnamesFixed.add(textureNew);
        }
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
                texnameToCubemap.put(itexname, i);
                return;
            }
        }

        L.log(Level.FINER, "Couldn''t find cubemap for coordinates ({0}, {1}, {2})",
                new Object[]{cx, cy, cz});
    }
    
    public TextureBuilder getTextureBuilder() {
        return new TextureBuilder(this, bsp);
    }
    
    public void addBrushSideID(int itexname, int side) {
        Integer icubemap = texnameToCubemap.get(itexname);
        if (icubemap == null) {
            // not environment mapped
            return;
        }
        
        Set<Integer> sides = cubemapToSideList.get(icubemap);
        
        // create new side list if required
        if (sides == null) {
            sides = new HashSet<Integer>();
            cubemapToSideList.put(icubemap, sides);
        }
        
        sides.add(side);
    }
    
    public Set<Integer> getBrushSidesForCubemap(int icubemap) {
        return cubemapToSideList.get(icubemap);
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
            if (useFixedTexnames) {
                return texnamesFixed.get(td);
            } else {
                return bsp.texnames.get(td);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            return ToolTexture.SKIP;
        }
    }
    
    public String canonizeTextureName(String textureNew) {
        // convert to lower case
        textureNew = textureNew.toLowerCase();

        // fix separators
        textureNew = FilenameUtils.separatorsToUnix(textureNew);

        return textureNew;
    }

    public List<String> getFixedTextureNames() {
        return Collections.unmodifiableList(texnamesFixed);
    }

    public boolean isUseFixedTexnames() {
        return useFixedTexnames;
    }

    public void setUseFixedTexnames(boolean useFixedTexnames) {
        this.useFixedTexnames = useFixedTexnames;
    }
}

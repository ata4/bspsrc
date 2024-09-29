/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.decompiler.modules.texture;

import info.ata4.bspsrc.decompiler.modules.ModuleRead;
import info.ata4.bspsrc.decompiler.modules.texture.tooltextures.ToolTextureSet;
import info.ata4.bspsrc.lib.BspFileReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
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
    private static final Logger L = LogManager.getLogger();

    // regex capturing group names
    private static final String CONTENT_GROUP = "content";
    private static final String CUBEMAP_X_GROUP = "x";
    private static final String CUBEMAP_Y_GROUP = "y";
    private static final String CUBEMAP_Z_GROUP = "z";

    // regex matching any bsp name
    // Theoretically we could use the actual bsp file name here. This is however not fool proof
    // as theoretically nothing prevents someone from renaming an already compiled bsp file
    // this would cause the pattern to no longer apply for embedded texture names and would
    // break texture fixing. See https://github.com/ata4/bspsrc/issues/162
    // This could probably be more restrictive, but unsure what characters are allowed in names
    private static final String MAP_NAME_PATTERN = "[^/]*";

    // regex patterns for texture name fixing
    // cubemap position
    public static final Pattern originPattern = Pattern.compile(String.format(
            "maps/%s/(?<%s>.+)_(?<%s>-?\\d+)_(?<%s>-?\\d+)_(?<%s>-?\\d+)",
            MAP_NAME_PATTERN,
            CONTENT_GROUP,
            CUBEMAP_X_GROUP,
            CUBEMAP_Y_GROUP,
            CUBEMAP_Z_GROUP
    ));
    // world vertex patch
    public static final Pattern wvtPatchPattern = Pattern.compile(String.format(
            "maps/%s/(?<%s>.+)_wvt_patch",
            MAP_NAME_PATTERN,
            CONTENT_GROUP
    ));
    // water texture patch
    public static final Pattern waterPatchPattern = Pattern.compile(String.format(
            "maps/%s/(?<%s>.+)_depth_-?\\d+",
            MAP_NAME_PATTERN,
            CONTENT_GROUP
    ));

    // ID mappings
    private Map<Integer, Set<Integer>> cubemapToSideList = new HashMap<>();
    private Map<Integer, Integer> texnameToCubemap = new HashMap<>();
    private List<String> texnamesFixed = new ArrayList<>();

    // settings
    private boolean fixTextureNames;
    private boolean fixToolTextures;
    private ToolTextureMatcher toolTextureMatcher = new ToolTextureMatcher(ToolTextureSet.forGame(bspFile.getAppId()));

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
     * <p>Following operations will be performed:
     * <ul>
     *     <li>chop {@code maps/mapname/.../_n_n_n} from names</li>
     *     <li>chop {@code maps/mapname/.../_depth_n} from names</li>
     *     <li>chop {@code maps/mapname/.../_wvt_patch} from names</li>
     * </ul>
     */
    private void processTextureNames() {
        for (int i = 0; i < bsp.texnames.size(); i++) {
            String textureOld = bsp.texnames.get(i);
            String textureNew = canonizeTextureName(textureOld);

            Matcher matcher = wvtPatchPattern.matcher(textureNew);
            if (matcher.find()) {
                textureNew = removeMatchedPrefixSuffix(matcher);
            }
            matcher = waterPatchPattern.matcher(textureNew);
            if (matcher.find()) {
                textureNew = removeMatchedPrefixSuffix(matcher);
            }
            matcher = originPattern.matcher(textureNew);
            if (matcher.find()) {
                try {
                    int cx = Integer.valueOf(matcher.group(CUBEMAP_X_GROUP));
                    int cy = Integer.valueOf(matcher.group(CUBEMAP_Y_GROUP));
                    int cz = Integer.valueOf(matcher.group(CUBEMAP_Z_GROUP));

                    setCubemapForTexname(i, cx, cy, cz);
                } catch (NumberFormatException e) {
                    L.warn("Error parsing cubemap position from regex. Matcher: " + matcher.pattern().pattern() + ", input: " + textureNew, e);
                }

                textureNew = removeMatchedPrefixSuffix(matcher);
            }

            // log differences
            if (!textureNew.equalsIgnoreCase(textureOld)) {
                L.trace("{} -> {}", textureOld, textureNew);
            }

            texnamesFixed.add(textureNew);
        }
    }

    private String removeMatchedPrefixSuffix(Matcher matcher) {
        return matcher.replaceFirst("${" + CONTENT_GROUP + "}");
    }

    private void setCubemapForTexname(int itexname, int cx, int cy, int cz) {
        // search for cubemap with these coordinates
        for (int i = 0; i < bsp.cubemaps.size(); i++) {
            int[] origin = bsp.cubemaps.get(i).origin;

            if (cx == origin[0] || cy == origin[1] || cz == origin[2]) {
                if (L.isTraceEnabled()) {
                    L.trace("TN: {} C: {}", itexname, i);
                }

                // set cubemap index used by this texdata/texname
                texnameToCubemap.put(itexname, i);
                return;
            }
        }

        L.trace("Couldn't find cubemap for coordinates ({}, {}, {})", cx, cy, cz);
    }

    public ToolTextureMatcher getToolTextureMatcher() {
        return toolTextureMatcher;
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
            sides = new HashSet<>();
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
    public String getTextureName(int itexinfo) {
        try {
            int ti = bsp.texinfos.get(itexinfo).texdata;
            int td = bsp.texdatas.get(ti).texname;
            if (fixTextureNames) {
                return texnamesFixed.get(td);
            } else {
                return bsp.texnames.get(td);
            }
        } catch (IndexOutOfBoundsException ex) {
            return ToolTexture.SKIP;
        }
    }

    public static String canonizeTextureName(String textureNew) {
        // convert to lower case
        textureNew = textureNew.toLowerCase(Locale.ROOT);

        // fix separators (convert separators to unix)
        textureNew = textureNew.replace('\\', '/');

        return textureNew;
    }

    public List<String> getFixedTextureNames() {
        return Collections.unmodifiableList(texnamesFixed);
    }

    public boolean isFixTextureNames() {
        return fixTextureNames;
    }

    public void setFixTextureNames(boolean fixTextureNames) {
        this.fixTextureNames = fixTextureNames;
    }

    public boolean isFixToolTextures() {
        return fixToolTextures;
    }

    public void setFixToolTextures(boolean fixToolTextures) {
        this.fixToolTextures = fixToolTextures;
    }

    public static boolean isPatchedMaterial(String fileName) {
        String canonizedName = canonizeTextureName(fileName);
        return TextureSource.originPattern.matcher(canonizedName).find()
                || TextureSource.wvtPatchPattern.matcher(canonizedName).find()
                || TextureSource.waterPatchPattern.matcher(canonizedName).find();
    }
}

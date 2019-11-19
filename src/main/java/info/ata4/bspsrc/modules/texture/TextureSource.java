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
import info.ata4.log.LogUtils;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
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
    private static final Logger L = LogUtils.getLogger();

    // regex capturing group names
    private static final String PREFIX_GROUP = "prefix";
    private static final String SUFFIX_GROUP = "suffix";
    private static final String CUBEMAP_X_GROUP = "x";
    private static final String CUBEMAP_Y_GROUP = "y";
    private static final String CUBEMAP_Z_GROUP = "z";

    // regex patterns for texture name fixing
    private final Pattern originPattern; // cubemap position
    private final Pattern wvtPatchPattern; // world vertex patch
    private final Pattern waterPatchPattern; // water texture patch

    // ID mappings
    private Map<Integer, Set<Integer>> cubemapToSideList = new HashMap<>();
    private Map<Integer, Integer> texnameToCubemap = new HashMap<>();
    private List<String> texnamesFixed = new ArrayList<>();

    // settings
    private boolean fixTextureNames;
    private boolean fixToolTextures;

    public TextureSource(BspFileReader reader) {
        super(reader);

        String bspFileName = reader.getBspFile().getName();

        originPattern = compileOriginPattern(bspFileName);
        wvtPatchPattern = compileWvtPatchPattern(bspFileName);
        waterPatchPattern = compileWaterPatchPattern(bspFileName);

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
            String textureNew = canonizeTextureName(textureOld);

            Matcher matcher = wvtPatchPattern.matcher(textureNew);
            if (matcher.find()) {
                textureNew = removeMatchedPrefixSuffix(matcher, textureNew);
            }
            matcher = waterPatchPattern.matcher(textureNew);
            if (matcher.find()) {
                textureNew = removeMatchedPrefixSuffix(matcher, textureNew);
            }
            matcher = originPattern.matcher(textureNew);
            if (matcher.find()) {
                try {
                    int cx = Integer.valueOf(matcher.group(CUBEMAP_X_GROUP));
                    int cy = Integer.valueOf(matcher.group(CUBEMAP_Y_GROUP));
                    int cz = Integer.valueOf(matcher.group(CUBEMAP_Z_GROUP));

                    setCubemapForTexname(i, cx, cy, cz);
                } catch (NumberFormatException e) {
                    L.log(Level.WARNING, "Error parsing cubemap position from regex. Matcher: " + matcher.pattern().pattern() + ", input: " + textureNew, e);
                }

                textureNew = removeMatchedPrefixSuffix(matcher, textureNew);
            }

            // log differences
            if (!textureNew.equalsIgnoreCase(textureOld)) {
                L.log(Level.FINEST, "{0} -> {1}", new Object[] {textureOld, textureNew});
            }

            texnamesFixed.add(textureNew);
        }
    }

    private String removeMatchedPrefixSuffix(Matcher matcher, String input) {
        String prefix = matcher.group(PREFIX_GROUP);
        String suffix = matcher.group(SUFFIX_GROUP);

        if (prefix == null || suffix == null) {
            L.log(Level.WARNING, "Passed matcher to 'removeMatchedPrefixSuffix' without either a prefix or suffix capturing group. This means my code sucks. Matcher: " + matcher.pattern().pattern(), new Exception());
            return input;
        }

        return input.replace(prefix, "").replace(suffix, "");
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
        return new TextureBuilder(this, bsp, bspFile.getSourceApp().getAppID());
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
    public String getTextureName(short itexinfo) {
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

        // fix separators
        textureNew = FilenameUtils.separatorsToUnix(textureNew);

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

    private static Pattern compileOriginPattern(String bspFileName) {
        return Pattern.compile(String.format("(?<%s>maps/%s/).+(?<%s>_(?<%s>-?\\d+)_(?<%s>-?\\d+)_(?<%s>-?\\d+))", PREFIX_GROUP, bspFileName, SUFFIX_GROUP, CUBEMAP_X_GROUP, CUBEMAP_Y_GROUP, CUBEMAP_Z_GROUP));
    }
    private static Pattern compileWvtPatchPattern(String bspFileName) {
        return Pattern.compile(String.format("(?<%s>maps/%s/).+(?<%s>_wvt_patch)", PREFIX_GROUP, bspFileName, SUFFIX_GROUP));
    }
    private static Pattern compileWaterPatchPattern(String bspFileName) {
        return Pattern.compile(String.format("(?<%s>maps/%s/).+(?<%s>_depth_(-?\\d+))", PREFIX_GROUP, bspFileName, SUFFIX_GROUP));
    }

    public static Predicate<Path> isPatchedMaterial(String bspFileName) {
        Pattern originPattern = compileOriginPattern(bspFileName);
        Pattern wvtPatchPattern = compileWvtPatchPattern(bspFileName);
        Pattern waterPatchPattern = compileWaterPatchPattern(bspFileName);


        return path -> {
            String canonizedName = canonizeTextureName(path.toString());
            return originPattern.matcher(canonizedName).find()
                        || wvtPatchPattern.matcher(canonizedName).find()
                        || waterPatchPattern.matcher(canonizedName).find();
        };
    }
}

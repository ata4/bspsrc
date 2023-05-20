package info.ata4.bspsrc.decompiler.modules.texture;

import info.ata4.bspsrc.decompiler.modules.texture.tooltextures.ToolTextureDefinition;
import info.ata4.bspsrc.lib.struct.BrushFlag;
import info.ata4.bspsrc.lib.struct.SurfaceFlag;

import java.util.*;

import static info.ata4.bspsrc.common.util.StringUtil.equalsIgnoreCase;

/**
 * Class for reversing texture names to their original tooltexture names/surface flags/brush flags.
 *
 * <p> In the vbsp optimizing process, every brushside that points to a texinfo, that is <b>not</b> referenced by any
 * face, will be changed to point to an already referenced texinfo. This is probably done to save space in the
 * texinfo lump.
 *
 * <p> This has the effect that all brushsides, that don't create a visible face, have their textures messed up.
 * Especially tooltextures are affected by this, as they are invisible and consequently don't generate any faces.
 *
 * <p> This class uses the surface property and surface/brush flags to reverse the original texture names. This is
 * achieved, by having a set of all possible textures mapped to what brush/surface flags they're required/forbidden to
 * have + which surface flag the require.
 *
 * <p> While surface/brush flags can be directly retrieved, the surface property is not directly saved in the bsp, but
 * rather has to be looked up through its texture/material. Remembering that the texture name changes makes this seem
 * useless. However, the algorithm for optimizing texinfos can only choose different textures with the <b>same surface
 * property</b>, enabling us to directly lookup the surface property by the texture name, even if the texture name
 * isn't the one originally used on the brush side.
 *
 * @see <a href="https://github.com/ValveSoftware/source-sdk-2013/blob/0d8dceea4310fde5706b3ce1c70609d72a38efdf/sp/src/utils/vbsp/writebsp.cpp#L768">
 *     https://github.com/ValveSoftware/source-sdk-2013/blob/0d8dceea4310fde5706b3ce1c70609d72a38efdf/sp/src/utils/vbsp/writebsp.cpp#L768</a>
 * @see <a href="https://github.com/ValveSoftware/source-sdk-2013/blob/0d8dceea4310fde5706b3ce1c70609d72a38efdf/sp/src/utils/vbsp/writebsp.cpp#L676">
 *     https://github.com/ValveSoftware/source-sdk-2013/blob/0d8dceea4310fde5706b3ce1c70609d72a38efdf/sp/src/utils/vbsp/writebsp.cpp#L676</a>
 */
public class ToolTextureMatcher {

    private final Map<String, ToolTextureDefinition> toolTextureDefinitions;

    public ToolTextureMatcher(Map<String, ToolTextureDefinition> toolTextureDefinitions) {
        this.toolTextureDefinitions = Collections.unmodifiableMap(new HashMap<>(toolTextureDefinitions));
    }

    /**
     * Tries to make the best guess, which texture name the specified surface property, brush/surface flags represent.
     *
     * @param originalTextureName the original texture name
     * @param surfFlags a set of {@link SurfaceFlag}s
     * @param brushFlags a set of {@link BrushFlag}s
     *
     * @return an empty optional if no texture name could be found or the best guess
     */
    public Optional<String> fixToolTexture(String originalTextureName, Set<SurfaceFlag> surfFlags,
                                           Set<BrushFlag> brushFlags) {

        // Optional of optional of string -> first optional empty if we don't have a definition of originalTextureName
        // second optional empty if we do have a definition but it doesn't have a surfaceproperty specified
        Optional<Optional<String>> optOriginalSurfaceProperty = Optional.ofNullable(toolTextureDefinitions.get(originalTextureName))
                .map(ToolTextureDefinition::getSurfaceProperty);

        return toolTextureDefinitions.entrySet().stream()
                .filter(ttEntry -> optOriginalSurfaceProperty
                        .map(surfaceProperty -> equalsIgnoreCase( // are surface properties case sensitive?
                                surfaceProperty.orElse(null),
                                ttEntry.getValue()
                                        .getSurfaceProperty()
                                        .orElse(null)
                        ))
                        .orElse(true))
                .filter(ttEntry -> ttEntry.getValue().getBrushFlagsRequirements()
                        .entrySet()
                        .stream()
                        .allMatch(entry -> brushFlags.contains(entry.getKey()) == entry.getValue()))
                .filter(ttEntry -> ttEntry.getValue().getSurfaceFlagsRequirements()
                        .entrySet()
                        .stream()
                        .allMatch(entry -> surfFlags.contains(entry.getKey()) == entry.getValue()))
                .max(Comparator.comparingInt(ttEntry -> {
                    ToolTextureDefinition definition = ttEntry.getValue();
                    int brushFlagRequirements = definition.getBrushFlagsRequirements().size();
                    int surfaceFlagRequirements = definition.getSurfaceFlagsRequirements().size();
                    return brushFlagRequirements + surfaceFlagRequirements;
                }))
                .map(Map.Entry::getKey);
    }
}

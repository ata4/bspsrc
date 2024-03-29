package info.ata4.bspsrc.decompiler.modules.texture;

import info.ata4.bspsrc.decompiler.modules.texture.tooltextures.ToolTextureDefinition;
import info.ata4.bspsrc.lib.struct.BrushFlag;
import info.ata4.bspsrc.lib.struct.SurfaceFlag;

import java.util.*;

import static info.ata4.bspsrc.common.util.StringUtil.equalsIgnoreCase;
import static java.util.Objects.requireNonNull;

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
        this.toolTextureDefinitions = Map.copyOf(toolTextureDefinitions);
    }

    /**
     * Tries to make the best guess, which texture name the specified surface property, brush/surface flags represent.
     *
     * @param originalTextureName the original texture name or null if unknown
     * @param brushFlags a set of {@link BrushFlag}s
     * @param surfFlags a set of {@link SurfaceFlag}s or null if unknown
     *
     * @return an empty optional if no texture name could be found or the best guess
     */
    public Optional<String> fixToolTexture(
            String originalTextureName,
            Set<BrushFlag> brushFlags,
            Set<SurfaceFlag> surfFlags
    ) {
        requireNonNull(brushFlags);

        return toolTextureDefinitions.entrySet().stream()
                .filter(ttEntry -> matchesSurfaceProperty(ttEntry.getValue(), originalTextureName))
                .filter(ttEntry -> matchesRequirements(ttEntry.getValue().getBrushFlagsRequirements(), brushFlags))
                .filter(ttEntry -> matchesRequirements(ttEntry.getValue().getSurfaceFlagsRequirements(), surfFlags))
                .max(Comparator.comparingInt(ttEntry -> ttDefinitionScore(ttEntry, surfFlags == null)))
                // accepting scores of 0 makes no sense, because nothing was matched
                .filter(ttEntry -> ttDefinitionScore(ttEntry, surfFlags == null) > 0)
                .map(Map.Entry::getKey);
    }

    /**
     * Because the optimization process in vbsp only reassigns texture with matching surface properties,
     * we check if the original textures surface property (incase we know it), matches the proposed
     * tooltexture definition.
     *
     * @param definition the proposed {@link ToolTextureDefinition}
     * @param originalTextureName the original texture name or {@code null} if unknown
     * @return {@code false}, if we know the surface properties don't match, otherwise {@code true}
     */
    private boolean matchesSurfaceProperty(ToolTextureDefinition definition, String originalTextureName) {
        if (originalTextureName == null)
            return true;

        var originalSurfaceDefinition = toolTextureDefinitions.get(originalTextureName);
        if (originalSurfaceDefinition == null)
            return true;

        return equalsIgnoreCase(
                originalSurfaceDefinition.getSurfaceProperty().orElse(null),
                definition.getSurfaceProperty().orElse(null)
        );
    }

    /**
     * Helper method to check if brush/surface-flag requirements match given one.
     */
    private <T> boolean matchesRequirements(Map<T, Boolean> requirements, Set<T> set) {
        if (set == null)
            return true;

        return requirements
                .entrySet()
                .stream()
                .allMatch(entry -> set.contains(entry.getKey()) == entry.getValue());
    }

    /**
     * In case we have multiple proposed tooltexture definitions, which match our requirements,
     * we score them to select the 'best' one. We consider the tooltexture with the most requirements
     * to be the best fit.
     * @return score, where bigger is a better fit
     */
    private static int ttDefinitionScore(Map.Entry<String, ToolTextureDefinition> ttEntry, boolean ignoreSurfaceFlags) {
        ToolTextureDefinition definition = ttEntry.getValue();
        int brushFlagRequirements = definition.getBrushFlagsRequirements().size();
        int surfaceFlagRequirements = definition.getSurfaceFlagsRequirements().size();
        return brushFlagRequirements + (ignoreSurfaceFlags ? 0 : surfaceFlagRequirements);
    }
}

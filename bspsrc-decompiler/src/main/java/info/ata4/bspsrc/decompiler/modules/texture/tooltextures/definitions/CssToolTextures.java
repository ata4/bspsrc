package info.ata4.bspsrc.decompiler.modules.texture.tooltextures.definitions;

import info.ata4.bspsrc.decompiler.modules.texture.ToolTexture;
import info.ata4.bspsrc.decompiler.modules.texture.tooltextures.ToolTextureDefinition;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public enum CssToolTextures {
    // Same as standard source2013 but doesn't have any surface flags
    CLIP(
            ToolTexture.CLIP,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .clearSurfaceFlagRequirements()
                    .build()
    ),
    // Same as standard source2013 but doesn't have any surface flags
    NPC_CLIP(
            ToolTexture.NPCCLIP,
            new ToolTextureDefinition.Builder(SourceToolTextures.NPC_CLIP.definition)
                    .clearSurfaceFlagRequirements()
                    .build()
    ),
    // Same as standard source2013 but doesn't have any surface flags
    PLAYER_CLIP(
            ToolTexture.PLAYERCLIP,
            new ToolTextureDefinition.Builder(SourceToolTextures.PLAYER_CLIP.definition)
                    .clearSurfaceFlagRequirements()
                    .build()
    ),;

    public final String materialName;
    public final ToolTextureDefinition definition;
    
    CssToolTextures(
            String materialName,
            ToolTextureDefinition definition
    ) {
        this.materialName = requireNonNull(materialName);
        this.definition = requireNonNull(definition);
    }

    public static Map<String, ToolTextureDefinition> getAll() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(texture -> texture.materialName, texture -> texture.definition));
    }
}

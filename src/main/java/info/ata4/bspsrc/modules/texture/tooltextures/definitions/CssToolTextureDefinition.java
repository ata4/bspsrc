package info.ata4.bspsrc.modules.texture.tooltextures.definitions;

import info.ata4.bsplib.struct.BrushFlag;
import info.ata4.bsplib.struct.SurfaceFlag;
import info.ata4.bspsrc.modules.texture.ToolTexture;
import info.ata4.bspsrc.modules.texture.tooltextures.ToolTextureDefinition;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public enum CssToolTextureDefinition implements ToolTextureDefinition {
    // Same as standard source2013 but doesn't have any surface flags
    CLIP(
            ToolTexture.CLIP,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .clearSurfaceFlagRequirements()
                    .build()
    ),
    // Same as standard source2013 but doesn't have any surface flags
    NPC_CLIP(
            ToolTexture.NPCCLIP,
            new Builder(SourceToolTextureDefinition.NPC_CLIP)
                    .clearSurfaceFlagRequirements()
                    .build()
    ),
    // Same as standard source2013 but doesn't have any surface flags
    PLAYER_CLIP(
            ToolTexture.PLAYERCLIP,
            new Builder(SourceToolTextureDefinition.PLAYER_CLIP)
                    .clearSurfaceFlagRequirements()
                    .build()
    ),;

    private final String materialName;
    private final ToolTextureDefinition toolTextureDefinition;

    CssToolTextureDefinition(String materialName, ToolTextureDefinition toolTextureDefinition) {
        this.materialName = Objects.requireNonNull(materialName);
        this.toolTextureDefinition = Objects.requireNonNull(toolTextureDefinition);
    }

    public String getMaterialName() {
        return materialName;
    }

    @Override
    public Optional<String> getSurfaceProperty() {
        return toolTextureDefinition.getSurfaceProperty();
    }

    @Override
    public Map<BrushFlag, Boolean> getBrushFlagsRequirements() {
        return toolTextureDefinition.getBrushFlagsRequirements();
    }

    @Override
    public Map<SurfaceFlag, Boolean> getSurfaceFlagsRequirements() {
        return toolTextureDefinition.getSurfaceFlagsRequirements();
    }

    public static Map<String, ToolTextureDefinition> getAll() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(CssToolTextureDefinition::getMaterialName, definition -> definition));
    }
}

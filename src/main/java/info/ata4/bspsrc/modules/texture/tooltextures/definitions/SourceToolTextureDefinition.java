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

/**
 * Standard tooltexture definitions. By default we use these for every game.
 * These are based on the source code found at <a href="github.com/ValveSoftware/source-sdk-2013">
 *     github.com/ValveSoftware/source-sdk-2013</a>
 *
 * <p> Notable code references:
 * <ul>
 *     <li>Hardcoded Brush/Surface flag conversion. This is the reason why some brushes have additional/fewer flags
 *     than the material specifies!
 *     <p><a href="https://github.com/ValveSoftware/source-sdk-2013/blob/0d8dceea4310fde5706b3ce1c70609d72a38efdf/sp/src/utils/vbsp/map.cpp#L2711-L2730">
 *         https://github.com/ValveSoftware/source-sdk-2013/blob/0d8dceea4310fde5706b3ce1c70609d72a38efdf/sp/src/utils/vbsp/map.cpp#L2711-L2730</a></li>
 *     <li>Material definitions to flags conversion.
 *     <p><a href="https://github.com/ValveSoftware/source-sdk-2013/blob/master/sp/src/utils/vbsp/textures.cpp#L85-L293">
 *         https://github.com/ValveSoftware/source-sdk-2013/blob/master/sp/src/utils/vbsp/textures.cpp#L85-L293</a></li>
 * </ul>
 */
public enum SourceToolTextureDefinition implements ToolTextureDefinition {
    // General
    AREAPORTAL(
            ToolTexture.AREAPORTAL,
            new Builder()
                    .setRequiredFlags(BrushFlag.CONTENTS_AREAPORTAL)
                    .setRequiredFlags(SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),
    BLOCK_BULLETS(
            ToolTexture.BLOCKBULLETS,
            new Builder()
                    .setRequiredFlags(BrushFlag.CONTENTS_WINDOW, BrushFlag.CONTENTS_TRANSLUCENT)
                    .setRequiredFlags(SurfaceFlag.SURF_TRANS, SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),
    BLOCK_LIGHT(
            ToolTexture.BLOCKLIGHT,
            new Builder()
                    .setRequiredFlags(BrushFlag.CONTENTS_OPAQUE, BrushFlag.CONTENTS_DETAIL)
                    .setRequiredFlags(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),
    BLOCK_LOS(
            ToolTexture.BLOCKLOS,
            new Builder()
                    .setRequiredFlags(BrushFlag.CONTENTS_BLOCKLOS, BrushFlag.CONTENTS_DETAIL)
                    .setRequiredFlags(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),
    INVISIBLE(
            ToolTexture.INVIS,
            new Builder()
                    .setRequiredFlags(BrushFlag.CONTENTS_GRATE, BrushFlag.CONTENTS_TRANSLUCENT)
                    .setForbiddenFlags(BrushFlag.CONTENTS_SOLID)
                    .setRequiredFlags(SurfaceFlag.SURF_TRANS, SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),
    INVISIBLE_LADDER(
            ToolTexture.INVISLADDER,
            new Builder()
                    .setRequiredFlags(BrushFlag.CONTENTS_GRATE, BrushFlag.CONTENTS_TRANSLUCENT, BrushFlag.CONTENTS_LADDER)
                    .setForbiddenFlags(BrushFlag.CONTENTS_SOLID)
                    .setRequiredFlags(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),
    NODRAW(
            ToolTexture.NODRAW,
            new Builder()
                    .setRequiredFlags(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),
    TRIGGER(
            ToolTexture.TRIGGER,
            new Builder()
                    .setRequiredFlags(SurfaceFlag.SURF_NOLIGHT, SurfaceFlag.SURF_TRIGGER)
                    .build()
    ),

    // Optimisation
    HINT(
            ToolTexture.HINT,
            new Builder()
                    .setRequiredFlags(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_HINT, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),
    SKIP(
            ToolTexture.SKIP,
            new Builder()
                    .setRequiredFlags(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_SKIP, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),

    // Clips
    // CONTENTS_DETAIL in all games?
    // surface property default_silent in all games?
    CLIP(
            ToolTexture.CLIP,
            new Builder("default_silent")
                    .setRequiredFlags(BrushFlag.CONTENTS_PLAYERCLIP, BrushFlag.CONTENTS_MONSTERCLIP, BrushFlag.CONTENTS_DETAIL)
                    .setRequiredFlags(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),
    // CONTENTS_DETAIL in all games?
    // surface property default_silent in all games?
    NPC_CLIP(
            ToolTexture.NPCCLIP,
            new Builder("default_silent")
                    .setRequiredFlags(BrushFlag.CONTENTS_MONSTERCLIP, BrushFlag.CONTENTS_DETAIL)
                    .setRequiredFlags(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),
    // CONTENTS_DETAIL in all games?
    PLAYER_CLIP(
            ToolTexture.PLAYERCLIP,
            new Builder()
                    .setRequiredFlags(BrushFlag.CONTENTS_PLAYERCLIP, BrushFlag.CONTENTS_DETAIL)
                    .setRequiredFlags(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),

    // Sky
    // surface property default_silent in all games?
    SKYBOX(
            ToolTexture.SKYBOX,
            new Builder("default_silent")
                    .setRequiredFlags(BrushFlag.CONTENTS_SOLID)
                    .setRequiredFlags(SurfaceFlag.SURF_SKY, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),
    // surface property default_silent in all games?
    SKYBOX_2D(
            ToolTexture.SKYBOX2D,
            new Builder("default_silent")
                    .setRequiredFlags(BrushFlag.CONTENTS_SOLID)
                    .setRequiredFlags(SurfaceFlag.SURF_SKY, SurfaceFlag.SURF_SKY2D, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    );

    private final String materialName;
    private final ToolTextureDefinition toolTextureDefinition;

    SourceToolTextureDefinition(String materialName, ToolTextureDefinition toolTextureDefinition) {
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
                .collect(Collectors.toMap(SourceToolTextureDefinition::getMaterialName, definition -> definition));
    }
}

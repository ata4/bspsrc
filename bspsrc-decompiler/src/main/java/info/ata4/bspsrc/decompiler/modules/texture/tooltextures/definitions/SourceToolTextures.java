package info.ata4.bspsrc.decompiler.modules.texture.tooltextures.definitions;

import info.ata4.bspsrc.decompiler.modules.texture.ToolTexture;
import info.ata4.bspsrc.decompiler.modules.texture.tooltextures.ToolTextureDefinition;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static info.ata4.bspsrc.lib.struct.BrushFlag.*;
import static info.ata4.bspsrc.lib.struct.SurfaceFlag.*;
import static java.util.Objects.requireNonNull;

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
public enum SourceToolTextures {
    // General
    AREAPORTAL(
            ToolTexture.AREAPORTAL,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(CONTENTS_AREAPORTAL)
                    .setRequiredFlags(SURF_NOLIGHT)
                    .build()
    ),
    BLOCK_BULLETS(
            ToolTexture.BLOCKBULLETS,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(CONTENTS_WINDOW, CONTENTS_TRANSLUCENT)
                    .setRequiredFlags(SURF_TRANS, SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    ),
    BLOCK_LIGHT(
            ToolTexture.BLOCKLIGHT,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(CONTENTS_OPAQUE, CONTENTS_DETAIL)
                    .setRequiredFlags(SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    ),
    BLOCK_LOS(
            ToolTexture.BLOCKLOS,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(CONTENTS_BLOCKLOS, CONTENTS_DETAIL)
                    .setRequiredFlags(SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    ),
    INVISIBLE(
            ToolTexture.INVIS,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(CONTENTS_GRATE, CONTENTS_TRANSLUCENT)
                    .setForbiddenFlags(CONTENTS_SOLID)
                    .setRequiredFlags(SURF_TRANS, SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    ),
    INVISIBLE_LADDER(
            ToolTexture.INVISLADDER,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(CONTENTS_GRATE, CONTENTS_TRANSLUCENT, CONTENTS_LADDER)
                    .setForbiddenFlags(CONTENTS_SOLID)
                    .setRequiredFlags(SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    ),
    NODRAW(
            ToolTexture.NODRAW,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    ),
    TRIGGER(
            ToolTexture.TRIGGER,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(SURF_NODRAW, SURF_NOLIGHT, SURF_TRIGGER)
                    .build()
    ),

    // Optimisation
    HINT(
            ToolTexture.HINT,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(SURF_NODRAW, SURF_HINT, SURF_NOLIGHT)
                    .build()
    ),
    SKIP(
            ToolTexture.SKIP,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(SURF_NODRAW, SURF_SKIP, SURF_NOLIGHT)
                    .build()
    ),

    // Clips
    // CONTENTS_DETAIL in all games?
    // surface property default_silent in all games?
    CLIP(
            ToolTexture.CLIP,
            new ToolTextureDefinition.Builder("default_silent")
                    .setRequiredFlags(CONTENTS_PLAYERCLIP, CONTENTS_MONSTERCLIP, CONTENTS_DETAIL)
                    .setRequiredFlags(SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    ),
    // CONTENTS_DETAIL in all games?
    // surface property default_silent in all games?
    NPC_CLIP(
            ToolTexture.NPCCLIP,
            new ToolTextureDefinition.Builder("default_silent")
                    .setRequiredFlags(CONTENTS_MONSTERCLIP, CONTENTS_DETAIL)
                    .setRequiredFlags(SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    ),
    // CONTENTS_DETAIL in all games?
    PLAYER_CLIP(
            ToolTexture.PLAYERCLIP,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(CONTENTS_PLAYERCLIP, CONTENTS_DETAIL)
                    .setRequiredFlags(SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    ),

    // Sky
    // surface property default_silent in all games?
    SKYBOX(
            ToolTexture.SKYBOX,
            new ToolTextureDefinition.Builder("default_silent")
                    .setRequiredFlags(CONTENTS_SOLID)
                    .setRequiredFlags(SURF_SKY, SURF_NOLIGHT)
                    .build()
    ),
    // surface property default_silent in all games?
    SKYBOX_2D(
            ToolTexture.SKYBOX2D,
            new ToolTextureDefinition.Builder("default_silent")
                    .setRequiredFlags(CONTENTS_SOLID)
                    .setRequiredFlags(SURF_SKY, SURF_SKY2D, SURF_NOLIGHT)
                    .build()
    );

    public final String materialName;
    public final ToolTextureDefinition definition;

    SourceToolTextures(
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

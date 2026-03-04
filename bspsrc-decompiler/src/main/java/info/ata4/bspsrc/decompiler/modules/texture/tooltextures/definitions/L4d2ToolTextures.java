package info.ata4.bspsrc.decompiler.modules.texture.tooltextures.definitions;

import info.ata4.bspsrc.decompiler.modules.texture.ToolTexture;
import info.ata4.bspsrc.decompiler.modules.texture.tooltextures.ToolTextureDefinition;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static info.ata4.bspsrc.lib.struct.BrushFlag.*;
import static info.ata4.bspsrc.lib.struct.SurfaceFlag.*;
import static java.util.Objects.requireNonNull;

public enum L4d2ToolTextures {
    
    NODRAW_METAL(
            ToolTexture.NODRAW_METAL,
            new ToolTextureDefinition.Builder(SourceToolTextures.NODRAW.definition)
                    .setSurfaceProperty("metal")
                    .build()
    ),
    CLIMB(
            ToolTexture.CLIMB,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(CONTENTS_TEAM2, CONTENTS_GRATE, CONTENTS_TRANSLUCENT, CONTENTS_LADDER)
                    .setRequiredFlags(SURF_TRANS, SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    ),
    INVISMETAL(
            ToolTexture.INVISMETAL,
            new ToolTextureDefinition.Builder("metal")
                    .setRequiredFlags(CONTENTS_WINDOW, CONTENTS_TRANSLUCENT)
                    .setRequiredFlags(SURF_TRANS, SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    );

    public final String materialName;
    public final ToolTextureDefinition definition;

    L4d2ToolTextures(
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

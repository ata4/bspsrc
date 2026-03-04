package info.ata4.bspsrc.decompiler.modules.texture.tooltextures.definitions;

import info.ata4.bspsrc.decompiler.modules.texture.ToolTexture;
import info.ata4.bspsrc.decompiler.modules.texture.tooltextures.ToolTextureDefinition;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static info.ata4.bspsrc.lib.struct.BrushFlag.*;
import static info.ata4.bspsrc.lib.struct.SurfaceFlag.SURF_NODRAW;
import static info.ata4.bspsrc.lib.struct.SurfaceFlag.SURF_NOLIGHT;
import static java.util.Objects.requireNonNull;

public enum CsgoToolTextures {

    GRENADE_CLIP(
            ToolTexture.CSGO_GRENADECLIP,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(CONTENTS_CURRENT_90, CONTENTS_DETAIL)
                    .setRequiredFlags(SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    ),
    DRONE_CLIP(
            ToolTexture.CSGO_DRONECLIP,
            new ToolTextureDefinition.Builder()
                    .setRequiredFlags(CONTENTS_CURRENT_180, CONTENTS_DETAIL)
                    .setRequiredFlags(SURF_NODRAW, SURF_NOLIGHT)
                    .build()
    ),

    //Material specific clip textures
    CLIP_CONCRETE(
            ToolTexture.CLIP_CONCRETE,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("concrete")
                    .build()
    ),
    CLIP_DIRT(
            ToolTexture.CLIP_DIRT,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("dirt")
                    .build()
    ),
    CLIP_GLASS(
            ToolTexture.CLIP_GLASS,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("glassfloor")
                    .build()
    ),
    CLIP_GRASS(
            ToolTexture.CLIP_GRASS,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("grass")
                    .build()
    ),
    CLIP_GRAVEL(
            ToolTexture.CLIP_GRAVEL,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("gravel")
                    .build()
    ),
    CLIP_METAL(
            ToolTexture.CLIP_METAL,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("metal")
                    .build()
    ),
    CLIP_METAL_SAND_BARREL(
            ToolTexture.CLIP_METAL_SAND_BARREL,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("metal_sand_barrel")
                    .build()
    ),
    CLIP_METALGRATE(
            ToolTexture.CLIP_METALGRATE,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("metalgrate")
                    .build()
    ),
    CLIP_METALVEHICLE(
            ToolTexture.CLIP_METALVEHICEL,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("metalvehicle")
                    .build()
    ),
    CLIP_PLASTIC(
            ToolTexture.CLIP_PLASTIC,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("plastic")
                    .build()
    ),
    CLIP_RUBBER(
            ToolTexture.CLIP_RUBBER,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("rubber")
                    .build()
    ),
    CLIP_RUBBERTIRE(
            ToolTexture.CLIP_RUBBERTIRE,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("rubbertire")
                    .build()
    ),
    CLIP_SAND(
            ToolTexture.CLIP_SAND,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("sand")
                    .build()
    ),
    CLIP_SNOW(
            ToolTexture.CLIP_SNOW,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("snow")
                    .build()
    ),
    CLIP_TILE(
            ToolTexture.CLIP_TILE,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("tile")
                    .build()
    ),
    CLIP_WOOD(
            ToolTexture.CLIP_WOOD,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("wood")
                    .build()
    ),
    CLIP_WOOD_BASKET(
            ToolTexture.CLIP_WOOD_BASKET,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("Wood_Basket")
                    .build()
    ),
    CLIP_WOOD_CRATE(
            ToolTexture.CLIP_WOOD_CRATE,
            new ToolTextureDefinition.Builder(SourceToolTextures.CLIP.definition)
                    .setSurfaceProperty("Wood_Crate")
                    .build()
    );

    public final String materialName;
    public final ToolTextureDefinition definition;

    CsgoToolTextures(
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

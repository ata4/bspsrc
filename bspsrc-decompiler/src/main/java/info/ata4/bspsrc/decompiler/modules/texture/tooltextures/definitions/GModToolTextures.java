package info.ata4.bspsrc.decompiler.modules.texture.tooltextures.definitions;

import info.ata4.bspsrc.decompiler.modules.texture.ToolTexture;
import info.ata4.bspsrc.decompiler.modules.texture.tooltextures.ToolTextureDefinition;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static info.ata4.bspsrc.lib.struct.BrushFlag.CONTENTS_DETAIL;
import static java.util.Objects.requireNonNull;

public enum GModToolTextures {
    INVISIBLE_LADDER(
            ToolTexture.INVISLADDER,
            new ToolTextureDefinition.Builder(SourceToolTextures.INVISIBLE_LADDER.definition)
                    .setRequiredFlags(CONTENTS_DETAIL)
                    .build()
    ),
    CLIP_CONCRETE(ToolTexture.CLIP_CONCRETE, CsgoToolTextures.CLIP_CONCRETE.definition),
    CLIP_DIRT(ToolTexture.CLIP_DIRT, CsgoToolTextures.CLIP_DIRT.definition),
    CLIP_GLASS(ToolTexture.CLIP_GLASS, CsgoToolTextures.CLIP_GLASS.definition),
    CLIP_GRASS(ToolTexture.CLIP_GRASS, CsgoToolTextures.CLIP_GRASS.definition),
    CLIP_GRAVEL(ToolTexture.CLIP_GRAVEL, CsgoToolTextures.CLIP_GRAVEL.definition),
    CLIP_METAL(ToolTexture.CLIP_METAL, CsgoToolTextures.CLIP_METAL.definition),
    CLIP_METAL_SAND_BARREL(ToolTexture.CLIP_METAL_SAND_BARREL, CsgoToolTextures.CLIP_METAL_SAND_BARREL.definition),
    CLIP_METALGRATE(ToolTexture.CLIP_METALGRATE, CsgoToolTextures.CLIP_METALGRATE.definition),
    CLIP_METALVEHICLE(ToolTexture.CLIP_METALVEHICEL, CsgoToolTextures.CLIP_METALVEHICLE.definition),
    CLIP_PLASTIC(ToolTexture.CLIP_PLASTIC, CsgoToolTextures.CLIP_PLASTIC.definition),
    CLIP_RUBBER(ToolTexture.CLIP_RUBBER, CsgoToolTextures.CLIP_RUBBER.definition),
    CLIP_RUBBERTIRE(ToolTexture.CLIP_RUBBERTIRE, CsgoToolTextures.CLIP_RUBBERTIRE.definition),
    CLIP_SAND(ToolTexture.CLIP_SAND, CsgoToolTextures.CLIP_SAND.definition),
//    CLIP_SNOW(ToolTexture.CLIP_SNOW, CsgoToolTextures.CLIP_SNOW.definition), // gmod doesn't have this one
    CLIP_TILE(ToolTexture.CLIP_TILE, CsgoToolTextures.CLIP_TILE.definition),
    CLIP_WOOD(ToolTexture.CLIP_WOOD, CsgoToolTextures.CLIP_WOOD.definition),
    CLIP_WOOD_BASKET(ToolTexture.CLIP_WOOD_BASKET, CsgoToolTextures.CLIP_WOOD_BASKET.definition),
    CLIP_WOOD_CRATE(ToolTexture.CLIP_WOOD_CRATE, CsgoToolTextures.CLIP_WOOD_CRATE.definition);

    public final String materialName;
    public final ToolTextureDefinition definition;

    GModToolTextures(
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

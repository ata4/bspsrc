package info.ata4.bspsrc.decompiler.modules.texture.tooltextures.definitions;

import info.ata4.bspsrc.decompiler.modules.texture.ToolTexture;
import info.ata4.bspsrc.decompiler.modules.texture.tooltextures.ToolTextureDefinition;
import info.ata4.bspsrc.lib.struct.BrushFlag;
import info.ata4.bspsrc.lib.struct.SurfaceFlag;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public enum CsgoToolTextureDefinitions implements ToolTextureDefinition {

    GRENADE_CLIP(
            ToolTexture.CSGO_GRENADECLIP,
            new Builder()
                    .setRequiredFlags(BrushFlag.CONTENTS_CURRENT_90, BrushFlag.CONTENTS_DETAIL)
                    .setRequiredFlags(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),
    DRONE_CLIP(
            ToolTexture.CSGO_DRONECLIP,
            new Builder()
                    .setRequiredFlags(BrushFlag.CONTENTS_CURRENT_180, BrushFlag.CONTENTS_DETAIL)
                    .setRequiredFlags(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_NOLIGHT)
                    .build()
    ),

    //Material specific clip textures
    CLIP_CONCRETE(
            ToolTexture.CSGO_CLIP_CONCRETE,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("concrete")
                    .build()
    ),
    CLIP_DIRT(
            ToolTexture.CSGO_CLIP_DIRT,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("dirt")
                    .build()
    ),
    CLIP_GLASS(
            ToolTexture.CSGO_CLIP_GLASS,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("glassfloor")
                    .build()
    ),
    CLIP_GRASS(
            ToolTexture.CSGO_CLIP_GRASS,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("grass")
                    .build()
    ),
    CLIP_GRAVEL(
            ToolTexture.CSGO_CLIP_GRAVEL,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("gravel")
                    .build()
    ),
    CLIP_METAL(
            ToolTexture.CSGO_CLIP_METAL,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("metal")
                    .build()
    ),
    CLIP_METAL_SAND_BARREL(
            ToolTexture.CSGO_CLIP_METAL_SAND_BARREL,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("metal_sand_barrel")
                    .build()
    ),
    CLIP_METALGRATE(
            ToolTexture.CSGO_CLIP_METALGRATE,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("metalgrate")
                    .build()
    ),
    CLIP_METALVEHICLE(
            ToolTexture.CSGO_CLIP_METALVEHICEL,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("metalvehicle")
                    .build()
    ),
    CLIP_PLASTIC(
            ToolTexture.CSGO_CLIP_PLASTIC,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("plastic")
                    .build()
    ),
    CLIP_RUBBER(
            ToolTexture.CSGO_CLIP_RUBBER,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("rubber")
                    .build()
    ),
    CLIP_RUBBERTIRE(
            ToolTexture.CSGO_CLIP_RUBBERTIRE,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("rubbertire")
                    .build()
    ),
    CLIP_SAND(
            ToolTexture.CSGO_CLIP_SAND,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("sand")
                    .build()
    ),
    CLIP_SNOW(
            ToolTexture.CSGO_CLIP_SNOW,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("snow")
                    .build()
    ),
    CLIP_TILE(
            ToolTexture.CSGO_CLIP_TILE,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("tile")
                    .build()
    ),
    CLIP_WOOD(
            ToolTexture.CSGO_CLIP_WOOD,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("wood")
                    .build()
    ),
    CLIP_WOOD_BASKET(
            ToolTexture.CSGO_CLIP_WOOD_BASKET,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("Wood_Basket")
                    .build()
    ),
    CLIP_WOOD_CRATE(
            ToolTexture.CSGO_CLIP_WOOD_CRATE,
            new Builder(SourceToolTextureDefinition.CLIP)
                    .setSurfaceProperty("Wood_Crate")
                    .build()
    );

    private final String materialName;
    private final ToolTextureDefinition toolTextureDefinition;

    CsgoToolTextureDefinitions(String materialName, ToolTextureDefinition toolTextureDefinition) {
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
                .collect(Collectors.toMap(CsgoToolTextureDefinitions::getMaterialName, definition -> definition));
    }
}

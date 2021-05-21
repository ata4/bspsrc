/*
 ** 2011 April 5
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */

package info.ata4.bspsrc.modules.texture;

/**
 * Enumeration of (mostly) game-independent tool textures.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public final class ToolTexture {

    private ToolTexture() {
    }

    public static final String EMPTY = null;
    public static final String NODRAW = "tools/toolsnodraw";
    public static final String WHITE = "skybox/sky_fake_white";
    public static final String BLACK = "tools/toolsblack";
    public static final String INVIS = "tools/toolsinvisible";
    public static final String ORANGE = "dev/dev_measuregeneric01";
    public static final String SKIP = "tools/toolsskip";
    public static final String HINT = "tools/toolshint";
    public static final String CLIP = "tools/toolsclip";
    public static final String PLAYERCLIP = "tools/toolsplayerclip";
    public static final String NPCCLIP = "tools/toolsnpcclip";
    public static final String AREAPORTAL = "tools/toolsareaportal";
    public static final String BLOCKLIGHT = "tools/toolsblocklight";
    public static final String BLOCKBULLETS = "tools/toolsblockbullets";
    public static final String BLOCKLOS = "tools/toolsblock_los";
    public static final String INVISLADDER = "tools/toolsinvisibleladder";
    public static final String DOTTED = "tools/toolsdotted";
    public static final String OCCLUDER = "tools/toolsoccluder";
    public static final String TRIGGER = "tools/toolstrigger";
    public static final String FOG = "tools/toolsfog";
    public static final String SKYBOX = "tools/toolsskybox";
    public static final String SKYBOX2D = "tools/toolsskybox2d";

    //CSGO Only
    public static final String CSGO_GRENADECLIP = "tools/toolsgrenadeclip";
    public static final String CSGO_DRONECLIP = "tools/toolsdroneclip";

    public static final String CSGO_CLIP_CONCRETE = "tools/toolsclip_concrete";
    public static final String CSGO_CLIP_DIRT = "tools/toolsclip_dirt";
    public static final String CSGO_CLIP_GLASS = "tools/toolsclip_glass";
    public static final String CSGO_CLIP_GRASS = "tools/toolsclip_grass";
    public static final String CSGO_CLIP_GRAVEL = "tools/toolsclip_gravel";
    public static final String CSGO_CLIP_METAL = "tools/toolsclip_metal";
    public static final String CSGO_CLIP_METAL_SAND_BARREL = "tools/toolsclip_metal_sand_barrel";
    public static final String CSGO_CLIP_METALGRATE = "tools/toolsclip_metalgrate";
    public static final String CSGO_CLIP_METALVEHICEL = "tools/toolsclip_metalvehicle";
    public static final String CSGO_CLIP_PLASTIC = "tools/toolsclip_plastic";
    public static final String CSGO_CLIP_RUBBER = "tools/toolsclip_rubber";
    public static final String CSGO_CLIP_RUBBERTIRE = "tools/toolsclip_rubbertire";
    public static final String CSGO_CLIP_SAND = "tools/toolsclip_sand";
    public static final String CSGO_CLIP_SNOW = "tools/toolsclip_snow";
    public static final String CSGO_CLIP_TILE = "tools/toolsclip_tile";
    public static final String CSGO_CLIP_WOOD = "tools/toolsclip_wood";
    public static final String CSGO_CLIP_WOOD_BASKET = "tools/toolsclip_wood_basket";
    public static final String CSGO_CLIP_WOOD_CRATE = "tools/toolsclip_wood_crate";
}

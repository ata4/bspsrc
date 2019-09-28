/*
 ** 2013 December 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.modules.texture;

import info.ata4.bsplib.struct.*;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.log.LogUtils;

import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static info.ata4.bsplib.app.SourceAppID.COUNTER_STRIKE_GO;

/**
 * A builder to create Texture objects.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TextureBuilder {

    private static final Logger L = LogUtils.getLogger();
    private static final float EPS_PERP = 0.02f;

    // surface/brush flags
    private static final EnumSet<SurfaceFlag> SURF_FLAGS_CLIP = EnumSet.of(SurfaceFlag.SURF_NOLIGHT, SurfaceFlag.SURF_NODRAW);
    private static final EnumSet<SurfaceFlag> SURF_FLAGS_AREAPORTAL = EnumSet.of(SurfaceFlag.SURF_NOLIGHT);
    private static final EnumSet<BrushFlag> BRUSH_FLAGS_AREAPORTAL = EnumSet.of(BrushFlag.CONTENTS_AREAPORTAL);
    private static final EnumSet<SurfaceFlag> SURF_FLAGS_NEEDS_REALIGNMENT = EnumSet.of(SurfaceFlag.SURF_NODRAW, SurfaceFlag.SURF_SKY, SurfaceFlag.SURF_SKY2D);

    private final BspData bsp;
    private final TextureSource texsrc;

    private final int appID;

    private Texture texture;
    private Vector3f origin;
    private Vector3f angles;
    private Vector3f normal;
    private DTexInfo texinfo;
    private DTexData texdata;

    // indices
    private short itexinfo = DTexInfo.TEXINFO_NODE;
    private int ibrush = -1;
    private int ibrushside = -1;

    TextureBuilder(TextureSource texsrc, BspData bsp, int appID) {
        this.texsrc = texsrc;
        this.bsp = bsp;
        this.appID = appID;
    }

    public Texture build() {
        texture = new Texture();
        texture.setOriginalTexture(ToolTexture.SKIP);

        // align to face
        fixTextureAxes();

        // no texinfo
        if (itexinfo == DTexInfo.TEXINFO_NODE) {
            return texture;
        }

        try {
            texinfo = bsp.texinfos.get(itexinfo);
        } catch (IndexOutOfBoundsException ex) {
            L.log(Level.WARNING, "Invalid texinfo index: {0}", itexinfo);
            return texture;
        }

        try {
            texdata = bsp.texdatas.get(texinfo.texdata);
            texture.setData(texdata);
        } catch (IndexOutOfBoundsException ex) {
            L.log(Level.WARNING, "Invalid texdata index: {0}", texinfo.texdata);
            return texture;
        }

        // get texture paths
        String textureOriginal = ToolTexture.SKIP;

        try {
            textureOriginal = bsp.texnames.get(texdata.texname);
        } catch (IndexOutOfBoundsException ex) {
            L.log(Level.WARNING, "Invalid texname index: {0}", texdata.texname);
        }

        String textureOverride = texsrc.getFixedTextureNames().get(texdata.texname);

        if (texsrc.isFixToolTextures()) {
            String textureFix = fixToolTexture();

            if (textureFix != null) {
                textureOverride = textureFix;
            }
        }

        // assign texture paths
        texture.setOriginalTexture(textureOriginal);
        texture.setOverrideTexture(textureOverride);

        // some calculations
        buildLightmapScale();

        // if enabled in config, fix texture axes for tool texture if necessary
        if (texsrc.isFixToolTextureAxes() && isToolTextureNeedsRealignment()) {
            fixTextureAxes();
        } else {
            // otherwise build UV from texture vectors and fix perpendicular
            // texture axes if necessary
            buildUV();
            fixPerpendicular();
        }

        return texture;
    }

    private String fixToolTexture() {
        if (ibrush == -1 || ibrushside == -1) {
            return null;
        }

        DBrush brush = bsp.brushes.get(ibrush);
        DBrushSide brushSide = bsp.brushSides.get(ibrushside);

        Set<SurfaceFlag> surfFlags;

        if (brushSide.texinfo == DTexInfo.TEXINFO_NODE) {
            surfFlags = EnumSet.noneOf(SurfaceFlag.class);
        } else {
            surfFlags = bsp.texinfos.get(brushSide.texinfo).flags;
        }

        Set<BrushFlag> brushFlags = brush.contents;

        // fix clip textures
        if (surfFlags.equals(SURF_FLAGS_CLIP)) {
            if (brush.isDetail()) {
                // clip
                if (brush.isPlayerClip() && brush.isNpcClip()) {
                    return ToolTexture.CLIP;
                }

                // player clip
                if (brush.isPlayerClip()) {
                    return ToolTexture.PLAYERCLIP;
                }

                // NPC clip
                if (brush.isNpcClip()) {
                    return ToolTexture.NPCCLIP;
                }

                // block line of sight
                if (brush.isBlockLos()) {
                    return ToolTexture.BLOCKLOS;
                }

                // block light, tested in csgo, portal 2, garrysmod, black mesa - 03.09.2019
                if (brush.isOpaque()) {
                    return ToolTexture.BLOCKLIGHT;
                }

                if (appID == COUNTER_STRIKE_GO) {
                    if (brush.isCurrent90()) {
                        return ToolTexture.CSGO_GRENADECLIP;
                    }

                    if (brush.isCurrent180()) {
                        return ToolTexture.CSGO_DRONECLIP;
                    }
                }
            }

            // nodraw
            return ToolTexture.NODRAW;
        }

        // fix areaportal textures
        if (brushFlags.equals(BRUSH_FLAGS_AREAPORTAL) && surfFlags.equals(SURF_FLAGS_AREAPORTAL)) {
            return ToolTexture.AREAPORTAL;
        }

        // fix occluder textures
        String textureName = bsp.texnames.get(texture.getData().texname);
        if (brush.isFlaggedAsOccluder() && !textureName.equalsIgnoreCase(ToolTexture.NODRAW))
            return ToolTexture.OCCLUDER;

        return null;
    }

    private boolean isToolTextureNeedsRealignment() {
    /**
     * Checks if the current texture is a tool texture likely to have unsuitable
     * or invalid texture vectors as a result of texinfo optimization performed
     * by VBSP, and therefore needs to be realigned.
     * 
     * @return <code>true</code> if it is a tool texture that needs to be realigned,
     *         <code>false</code> otherwise.
     */
        if (ibrush == -1 || ibrushside == -1) {
            return false;
        }

        DBrushSide brushSide = bsp.brushSides.get(ibrushside);

        if (brushSide.texinfo == DTexInfo.TEXINFO_NODE) {
            return false;
        }

        DBrush brush = bsp.brushes.get(ibrush);
        Set<BrushFlag> brushFlags = brush.contents;
        Set<SurfaceFlag> surfFlags = bsp.texinfos.get(brushSide.texinfo).flags;

        return (surfFlags.stream().anyMatch(flag -> SURF_FLAGS_NEEDS_REALIGNMENT.contains(flag))
                || brush.isFlaggedAsOccluder())
                || (brushFlags.equals(BRUSH_FLAGS_AREAPORTAL) && surfFlags.equals(SURF_FLAGS_AREAPORTAL));
    }

    /**
     * Calculates new UV axes based on the normal vector of the face.
     * 
     * It should produce the same result as face alignment in Hammer.
     */
    private void fixTextureAxes() {
        if (normal == null) {
            return;
        }

        // calculate the projections of the surface normal onto the world axes
        float dotX = Math.abs(Vector3f.BASE_VECTOR_X.dot(normal));
        float dotY = Math.abs(Vector3f.BASE_VECTOR_Y.dot(normal));
        float dotZ = Math.abs(Vector3f.BASE_VECTOR_Z.dot(normal));

        Vector3f vdir;

        // if the projection of the surface normal onto the z-axis is greatest
        if (dotZ > dotX && dotZ > dotY) {
            // use y-axis as basis
            vdir = Vector3f.BASE_VECTOR_Y;
        } else {
            // otherwise use z-axis as basis
            vdir = Vector3f.BASE_VECTOR_Z;
        }

        Vector3f tv1 = normal.cross(vdir).normalize(); // 1st tex vector
        Vector3f tv2 = normal.cross(tv1).normalize();  // 2nd tex vector

        texture.setUAxis(new TextureAxis(tv1));
        texture.setVAxis(new TextureAxis(tv2));
    }

    /**
     * Checks for texture axes that are perpendicular to the normal and fixes them.
     */
    private void fixPerpendicular() {
        if (normal == null) {
            return;
        }

        Vector3f texNorm = texture.getUAxis().axis.cross(texture.getVAxis().axis);
        if (Math.abs(normal.dot(texNorm)) >= EPS_PERP) {
            return;
        }

        fixTextureAxes();
    }

    private void buildLightmapScale() {
        // extract lightmap vectors
        float[][] lvec = texinfo.lightmapVecsLuxels;
        Vector3f uaxis = new Vector3f(lvec[0]);
        Vector3f vaxis = new Vector3f(lvec[1]);

        float ls = (uaxis.length() + vaxis.length()) / 2.0f;

        if (ls > 0.001f) {
            texture.setLightmapScale(Math.round(1.0f / ls));
        }
    }

    private void buildUV() {        
        // extract texture vectors
        float[][] tvec = texinfo.textureVecsTexels;
        Vector3f uaxis = new Vector3f(tvec[0]);
        Vector3f vaxis = new Vector3f(tvec[1]);

        float utw = 1.0f / uaxis.length();
        float vtw = 1.0f / vaxis.length();

        uaxis = uaxis.scalar(utw);
        vaxis = vaxis.scalar(vtw);

        float ushift = tvec[0][3];
        float vshift = tvec[1][3];

        // translate to origin
        if (origin != null) {
            ushift -= origin.dot(uaxis) / utw;
            vshift -= origin.dot(vaxis) / vtw;
        }

        // rotate texture axes
        if (angles != null) {
            uaxis = uaxis.rotate(angles);
            vaxis = vaxis.rotate(angles);

            // calculate the shift in U/V space due to this rotation
            Vector3f shift = Vector3f.NULL;

            if (origin != null) {
                shift = shift.sub(origin);
            }

            shift = shift.rotate(angles);

            if (origin != null) {
                shift = shift.add(origin);
            }

            ushift -= shift.dot(uaxis) / utw;
            vshift -= shift.dot(vaxis) / vtw;
        }

        // normalize shift values
        if (texdata.width != 0) {
            ushift %= texdata.width;
        }
        if (texdata.height != 0) {
            vshift %= texdata.height;
        }

        // round scales to 4 decimal digits to fix round-off errors
        // (e.g.: 0.25000018 -> 0.25)
        utw = Math.round(utw * 10000) / 10000f;
        vtw = Math.round(vtw * 10000) / 10000f;

        // create texture axes
        texture.setUAxis(new TextureAxis(uaxis, Math.round(ushift), utw));
        texture.setVAxis(new TextureAxis(vaxis, Math.round(vshift), vtw));
    }

    public void setOrigin(Vector3f origin) {
        this.origin = origin;
    }

    public void setAngles(Vector3f angles) {
        this.angles = angles;
    }

    public void setNormal(Vector3f normal) {
        this.normal = normal;
    }

    public void setBrushIndex(int ibrush) {
        this.ibrush = ibrush;
    }

    public void setBrushSideIndex(int ibrushside) {
        this.ibrushside = ibrushside;
    }

    public void setTexinfoIndex(short itexinfo) {
        this.itexinfo = itexinfo;
    }
}

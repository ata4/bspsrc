/*
 ** 2013 December 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.decompiler.modules.texture;

import info.ata4.bspsrc.decompiler.util.OccluderMapper;
import info.ata4.bspsrc.lib.struct.*;
import info.ata4.bspsrc.lib.vector.Vector3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * A builder to create Texture objects.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TextureBuilder {

    private static final Logger L = LogManager.getLogger();
    private static final float EPS_PERP = 0.02f;

    private final BspData bsp;
    private final TextureSource texsrc;
    private final OccluderMapper.ReallocationData occReallocationData;

    private Texture texture;
    private Vector3f origin;
    private Vector3f angles;
    private Vector3f normal;
    private DTexInfo texinfo;
    private DTexData texdata;

    // indices
    private int itexinfo = DTexInfo.TEXINFO_NODE;
    private int ibrush = -1;
    private int ibrushside = -1;

    private boolean enableTextureFixing = false;

    public TextureBuilder(
            BspData bsp,
            TextureSource texsrc,
            OccluderMapper.ReallocationData occReallocationData
    ) {
        this.bsp = requireNonNull(bsp);
        this.texsrc = requireNonNull(texsrc);
        this.occReallocationData = requireNonNull(occReallocationData);
    }

    public Texture build() {
        texture = new Texture();
        texture.setOriginalTexture(ToolTexture.SKIP);

        // align to face
        fixTextureAxes();

        // no texinfo
        if (itexinfo == DTexInfo.TEXINFO_NODE) {
            // still try to fix textures even if we have no texinfo
            // (Some tooltextures in css/hl2:d seem to have no texinfo)
            if (texsrc.isFixToolTextures() && enableTextureFixing)
                texture.setOverrideTexture(fixToolTexture(null));

            return texture;
        }

        try {
            texinfo = bsp.texinfos.get(itexinfo);
        } catch (IndexOutOfBoundsException ex) {
            L.warn("Invalid texinfo index: {}", itexinfo);
            return texture;
        }

        try {
            texdata = bsp.texdatas.get(texinfo.texdata);
            texture.setData(texdata);
        } catch (IndexOutOfBoundsException ex) {
            L.warn("Invalid texdata index: {}", texinfo.texdata);
            return texture;
        }

        // get texture paths
        String textureOriginal = ToolTexture.SKIP;

        try {
            textureOriginal = bsp.texnames.get(texdata.texname);
        } catch (IndexOutOfBoundsException ex) {
            L.warn("Invalid texname index: {}", texdata.texname);
        }

        String textureOverride = texsrc.getFixedTextureNames().get(texdata.texname);

        boolean usesFixedTexture = false;
        if (texsrc.isFixToolTextures() && enableTextureFixing) {
            String textureFix = fixToolTexture(textureOverride);

            if (textureFix != null) {
                textureOverride = textureFix;
                usesFixedTexture = true;
            }
        }

        // assign texture paths
        texture.setOriginalTexture(textureOriginal);
        texture.setOverrideTexture(textureOverride);

        // some calculations
        buildLightmapScale();

        boolean needsTextureRealignment = usesFixedTexture
                || texinfo.flags.contains(SurfaceFlag.SURF_SKY)
                || texinfo.flags.contains(SurfaceFlag.SURF_SKY2D);

        // fix texture axes for tool texture if necessary
        if (needsTextureRealignment) {
            fixTextureAxes();
        } else {
            // otherwise build UV from texture vectors and fix perpendicular
            // texture axes if necessary
            buildUV();
            fixPerpendicular();
        }

        return texture;
    }

    private String fixToolTexture(String originalTextureName) {
        if (ibrush == -1 || ibrushside == -1) {
            return null;
        }

        DBrush brush = bsp.brushes.get(ibrush);

        // We do not explicitly fix areaportal textures here, because the toolTextureMatcher is already
        // able to identify them. This is due to areaportal brushes having the AREAPORTAL brush flag.
        // Occluders on the other hand do not have any indicative brush flag, which is why we do it manually.

        // fix occluder textures
        boolean isOccluderBrush = occReallocationData.isOccluderBrush(ibrush);
        boolean isOccluderBrushSide = occReallocationData.isOccluderBrushSide(ibrush, ibrushside - brush.fstside);

        if (isOccluderBrush) {
            return isOccluderBrushSide ? ToolTexture.OCCLUDER : ToolTexture.NODRAW;
        }
        Set<BrushFlag> brushFlags = brush.contents;
        Set<SurfaceFlag> surfFlags = itexinfo == DTexInfo.TEXINFO_NODE ? null : bsp.texinfos.get(itexinfo).flags;

        return texsrc.getToolTextureMatcher().fixToolTexture(originalTextureName, brushFlags, surfFlags)
                .orElse(null);
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

    public void setTexinfoIndex(int itexinfo) {
        this.itexinfo = itexinfo;
    }

    public void setEnableTextureFixing(boolean shouldTextureFix) {
        this.enableTextureFixing = shouldTextureFix;
    }
}

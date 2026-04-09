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

import info.ata4.bspsrc.lib.struct.BrushFlag;
import info.ata4.bspsrc.lib.struct.DTexData;
import info.ata4.bspsrc.lib.struct.DTexInfo;
import info.ata4.bspsrc.lib.struct.SurfaceFlag;
import info.ata4.bspsrc.lib.vector.Vector3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
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

    public static DTexInfo lookupTexinfo(
            int texinfoId,
            List<? extends DTexInfo> texinfos
    ) {
        if (texinfoId == DTexInfo.TEXINFO_NODE)
            return null;

        try {
            return texinfos.get(texinfoId);
        } catch (IndexOutOfBoundsException ex) {
            L.warn("Invalid texinfo index: {}", texinfoId);
            return null;
        }
    }

    public static DTexData lookupTexdata(
            int texdataId,
            List<? extends DTexData> texdatas
    ) {
        try {
            return texdatas.get(texdataId);
        } catch (IndexOutOfBoundsException ex) {
            L.warn("Invalid texdata index: {}", texdataId);
            return null;
        }
    }

    public static String lookupTexname(
            int texnameId,
            List<? extends String> texnames
    ) {
        try {
            return texnames.get(texnameId);
        } catch (IndexOutOfBoundsException ex) {
            L.warn("Invalid texname index: {}", texnameId);
            return null;
        }
    }
    
    public static Texture buildFromTexinfo(
            DTexInfo texinfo,
            DTexData texdata,
            String material,
            Vector3d origin,
            Vector3d angles,
            Vector3d normal
    ) {
        var lightmapscale = buildLightmapScale(texinfo);
        var uv = buildUV(texinfo, texdata, origin, angles);
        if (isPerpendicular(uv.u().axis, uv.v().axis, normal))
            uv = defaultUV(normal);

        var texture = new Texture();
        texture.texture = material;
        texture.u = uv.u();
        texture.v = uv.v();
        texture.lmscale = lightmapscale;
        return texture;
    }
    
    public static Texture buildFromNormal(
            Vector3d normal,
            String material
    ) {
        var uv = defaultUV(normal);
        
        var texture = new Texture();
        texture.texture = material;
        texture.u = uv.u();
        texture.v = uv.v();
        return texture;
    }
    
    public record Axes(
            TextureAxis u,
            TextureAxis v
    ) {
        public Axes {
            requireNonNull(u);
            requireNonNull(v);
        }
    }

    public static Axes defaultUV(Vector3d normal) {
        // calculate the projections of the surface normal onto the world axes
        var dotX = Math.abs(Vector3d.BASE_VECTOR_X.dot(normal));
        var dotY = Math.abs(Vector3d.BASE_VECTOR_Y.dot(normal));
        var dotZ = Math.abs(Vector3d.BASE_VECTOR_Z.dot(normal));

        Vector3d vdir;

        // if the projection of the surface normal onto the z-axis is greatest
        if (dotZ > dotX && dotZ > dotY) {
            // use y-axis as basis
            vdir = Vector3d.BASE_VECTOR_Y;
        } else {
            // otherwise use z-axis as basis
            vdir = Vector3d.BASE_VECTOR_Z;
        }

        var tv1 = normal.cross(vdir).normalize(); // 1st tex vector
        var tv2 = normal.cross(tv1).normalize();  // 2nd tex vector

        return new Axes(
                new TextureAxis(tv1, 0, 0.25),
                new TextureAxis(tv2, 0, 0.25)
        );
    }
    
    public static Axes buildUV(
            DTexInfo texinfo,
            DTexData texdata,
            Vector3d origin,
            Vector3d angles
    ) {
        var tvec = texinfo.textureVecsTexels;
        var uaxis = new Vector3d(tvec[0][0], tvec[0][1], tvec[0][2]);
        var vaxis = new Vector3d(tvec[1][0], tvec[1][1], tvec[1][2]);

        var utw = 1.0 / uaxis.length();
        var vtw = 1.0 / vaxis.length();

        uaxis = uaxis.scalar(utw);
        vaxis = vaxis.scalar(vtw);

        var ushift = (double) tvec[0][3];
        var vshift = (double) tvec[1][3];

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
            var shift = Vector3d.NULL;

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

        return new Axes(
                new TextureAxis(uaxis, (int) Math.round(ushift), utw),
                new TextureAxis(vaxis, (int) Math.round(vshift), vtw)
        );
    }

    public static int buildLightmapScale(DTexInfo texinfo) {
        float[][] lvec = texinfo.lightmapVecsLuxels;
        var uaxis = new Vector3d(lvec[0][0], lvec[0][1], lvec[0][2]);
        var vaxis = new Vector3d(lvec[1][0], lvec[1][1], lvec[1][2]);

        var ls = (uaxis.length() + vaxis.length()) / 2.0;
        return ls > 0.001 ? (int) Math.round(1.0 / ls) : 16;
    }
    
    public static boolean isPerpendicular(
            Vector3d uAxis,
            Vector3d vAxis,
            Vector3d normal
    ) {
        var texNorm = uAxis.cross(vAxis);
        return Math.abs(normal.dot(texNorm)) < EPS_PERP;
    }

    public static String fixToolTexture(
            String originalTextureName,
            boolean isOccluderBrush,
            boolean isOccluderBrushSide,
            Set<BrushFlag> brushFlags,
            Set<SurfaceFlag> surfaceFlags,
            ToolTextureMatcher toolTextureMatcher
    ) {
        // We do not explicitly fix areaportal textures here, because the toolTextureMatcher is already
        // able to identify them. This is due to areaportal brushes having the AREAPORTAL brush flag.
        // Occluders on the other hand do not have any indicative brush flag, which is why we do it manually.

        if (isOccluderBrush) {
            return isOccluderBrushSide ? ToolTexture.OCCLUDER : ToolTexture.NODRAW;
        }
        return toolTextureMatcher.fixToolTexture(originalTextureName, brushFlags, surfaceFlags)
                .orElse(null);
    }
}

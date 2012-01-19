/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc;

import info.ata4.bsplib.struct.DTexData;
import info.ata4.bsplib.vector.Vector3f;

/**
 * A compiled texture data structure with simulated Hammer functions.
 *
 * Based on texture building part of Vmex.writeside()
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Texture {
    
    private static final double SIN_45_DEGREES = Math.sin(45);
    private static final float EPS_PERP = 0.02f;

    private DTexData data;
    private String material = ToolTexture.SKIP;
    private TextureAxis u = new TextureAxis(1, 0, 0);
    private TextureAxis v = new TextureAxis(0, 1, 0);
    private int lmscale = 16;

    /**
     * Checks for a texture axis perpendicular to the face.
     *
     * @param normal plane normal vector
     * @return false if the texture axis is perpendicular to the face
     */
    public boolean isPerpendicularTo(Vector3f normal) {
        if (u == null || v == null) {
            return false;
        }

        Vector3f texNorm = v.axis.cross(u.axis);

        return Math.abs(normal.dot(texNorm)) >= EPS_PERP;
    }

    /**
     * Recalculates the texture axes based on a normal.
     *
     * @param normal plane normal vector
     */
    public void alignTo(Vector3f normal) {
        // z is z-direction unit vector
        Vector3f udir = new Vector3f(0, 0, 1);

        // if angle of normal to z axis is less than ~25 degrees
        if (Math.abs(udir.dot(normal)) > SIN_45_DEGREES) {
            // use x unit-vector as basis
            udir = new Vector3f(1, 0, 0);
        }

        Vector3f tv1 = udir.cross(normal).normalize(); // 1st tex vector
        Vector3f tv2 = tv1.cross(normal).normalize();  // 2nd tex vector

        u = new TextureAxis(tv1);
        v = new TextureAxis(tv2);
    }

    public TextureAxis getUAxis() {
        return u;
    }

    public void setUAxis(TextureAxis u) {
        this.u = u;
    }

    public TextureAxis getVAxis() {
        return v;
    }

    public void setVAxis(TextureAxis v) {
        this.v = v;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int getLightmapScale() {
        return lmscale;
    }
    
    public void setLightmapScale(int lmscale) {
        this.lmscale = lmscale;
    }

    public DTexData getData() {
        return data;
    }

    public void setData(DTexData texdata) {
        this.data = texdata;
    }
}

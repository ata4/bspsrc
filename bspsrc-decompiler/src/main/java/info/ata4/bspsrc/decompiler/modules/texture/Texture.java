/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.decompiler.modules.texture;

/**
 * A simple texture data structure.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Texture {

    private String texture;
    private TextureAxis u = new TextureAxis(1, 0, 0);
    private TextureAxis v = new TextureAxis(0, 1, 0);
    private int lmscale = 16;

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

    public String getTexture() {
        return texture;
    }

    public String getOriginalTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }
    
    public int getLightmapScale() {
        return lmscale;
    }

    public void setLightmapScale(int lmscale) {
        this.lmscale = lmscale;
    }
}

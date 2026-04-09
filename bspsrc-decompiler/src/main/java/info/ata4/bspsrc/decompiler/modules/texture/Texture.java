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
    public String texture;
    public TextureAxis u = new TextureAxis(1, 0, 0);
    public TextureAxis v = new TextureAxis(0, 1, 0);
    public int lmscale = 16;
}

/*
 ** 2012 Januar 18
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.app.util;

import info.ata4.bspsrc.decompiler.modules.texture.ToolTexture;

/**
 * Enumeration for some tool textures.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public enum EnumToolTexture {

    DEFAULT("Default", ""),
    WHITE("White", ToolTexture.WHITE),
    BLACK("Black", ToolTexture.BLACK),
    NODRAW("Nodraw", ToolTexture.NODRAW),
    ORANGE("Orange", ToolTexture.ORANGE),
    SKIP("Skip", ToolTexture.SKIP);

    public final String texName;
    public final String texPath;

    private EnumToolTexture(String texName, String texPath) {
        this.texName = texName;
        this.texPath = texPath;
    }

    @Override
    public String toString() {
        return texName;
    }
}

/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.lib.lump;

import info.ata4.bspsrc.lib.util.StringMacroUtils;

/**
 * Lump extension for game lumps that are stored inside LUMP_GAME_LUMP.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class GameLump extends AbstractLump {

    private int flags;

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    @Override
    public String getName() {
        return StringMacroUtils.unmakeID(Integer.reverseBytes(getFourCC()));
    }

    @Override
    public void setCompressed(boolean compressed) {
        super.setCompressed(compressed);
        setFlags(compressed ? 1 : 0);
    }
}

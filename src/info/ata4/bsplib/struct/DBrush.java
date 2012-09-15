/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.struct;

import info.ata4.bsplib.lump.LumpDataInput;
import info.ata4.bsplib.lump.LumpDataOutput;
import info.ata4.util.io.EnumConverter;
import java.io.IOException;
import java.util.Set;

/**
 * Brush data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DBrush implements DStruct {

    public int fstside;
    public int numside;
    public Set<BrushFlag> contents;

    public boolean isSolid() {
        return contents.contains(BrushFlag.CONTENTS_SOLID);
    }

    public boolean isDetail() {
        return contents.contains(BrushFlag.CONTENTS_DETAIL);
    }

    public boolean isOpaque() {
        return contents.contains(BrushFlag.CONTENTS_OPAQUE);
    }

    public boolean isGrate() {
        return contents.contains(BrushFlag.CONTENTS_GRATE);
    }

    public boolean isLadder() {
        return contents.contains(BrushFlag.CONTENTS_LADDER);
    }

    public boolean isAreaportal() {
        return contents.contains(BrushFlag.CONTENTS_AREAPORTAL);
    }

    public boolean isPlayerClip() {
        return contents.contains(BrushFlag.CONTENTS_PLAYERCLIP);
    }

    public boolean isNpcClip() {
        return contents.contains(BrushFlag.CONTENTS_MONSTERCLIP);
    }

    public boolean isBlockLos() {
        return contents.contains(BrushFlag.CONTENTS_BLOCKLOS);
    }

    public boolean isTranslucent() {
        return contents.contains(BrushFlag.CONTENTS_TRANSLUCENT);
    }
    
    public boolean isWindow() {
        return contents.contains(BrushFlag.CONTENTS_WINDOW);
    }

    public int getSize() {
        return 12;
    }

    public void read(LumpDataInput li) throws IOException {
        fstside = li.readInt();
        numside = li.readInt();
        contents = EnumConverter.fromInteger(BrushFlag.class, li.readInt());
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeInt(fstside);
        lo.writeInt(numside);
        lo.writeInt(EnumConverter.toInteger(contents));
    }
}

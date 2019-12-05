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

import info.ata4.bsplib.app.SourceAppID;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
import info.ata4.util.EnumConverter;

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

    private boolean flagOccluder = false;

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

    public void flagAsOccluder(boolean value) {
        flagOccluder = value;
    }
  
    public boolean isFlaggedAsOccluder() {
        return flagOccluder;
    }

    public boolean isCurrent180() {
        return contents.contains(BrushFlag.CONTENTS_CURRENT_180);
    }

    public boolean isCurrent90() {
        return contents.contains(BrushFlag.CONTENTS_CURRENT_90);
    }

    public boolean isFuncDetail(int appId) {
        if (appId == SourceAppID.COUNTER_STRIKE_GO) {
            // Note: For the game csgo, ladders can also be considered to be func_detail
            //       even though their solid flag is always false
            return (isSolid() || isLadder()) && isDetail();
        } else {
            return isSolid() && isDetail();
        }
    }

    @Override
    public int getSize() {
        return 12;
    }

    @Override
    public void read(DataReader in) throws IOException {
        fstside = in.readInt();
        numside = in.readInt();
        contents = EnumConverter.fromInteger(BrushFlag.class, in.readInt());
    }

    @Override
    public void write(DataWriter out) throws IOException {
        out.writeInt(fstside);
        out.writeInt(numside);
        out.writeInt(EnumConverter.toInteger(contents));
    }
}

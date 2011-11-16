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
import info.ata4.bsplib.vector.Vector3f;
import java.io.IOException;

/**
 * Texture data structure.
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DTexData implements DStruct {
    public Vector3f reflectivity;
    public int texname;
    public int width, height;
    public int viewWidth, viewHeight;

    public int getSize() {
        return 32;
    }

    public void read(LumpDataInput li) throws IOException {
        reflectivity = li.readVector3f();
        texname = li.readInt();
        width = li.readInt();
        height = li.readInt();
        viewWidth = li.readInt();
        viewHeight = li.readInt();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeVector3f(reflectivity);
        lo.writeInt(texname);
        lo.writeInt(width);
        lo.writeInt(height);
        lo.writeInt(viewWidth);
        lo.writeInt(viewHeight);
    }
}

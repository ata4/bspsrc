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

import info.ata4.bsplib.lump.LumpIO;
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

    @Override
    public int getSize() {
        return 32;
    }

    @Override
    public void read(LumpIO lio) throws IOException {
        reflectivity = lio.readVector3f();
        texname = lio.readInt();
        width = lio.readInt();
        height = lio.readInt();
        viewWidth = lio.readInt();
        viewHeight = lio.readInt();
    }

    @Override
    public void write(LumpIO lio) throws IOException {
        lio.writeVector3f(reflectivity);
        lio.writeInt(texname);
        lio.writeInt(width);
        lio.writeInt(height);
        lio.writeInt(viewWidth);
        lio.writeInt(viewHeight);
    }
}

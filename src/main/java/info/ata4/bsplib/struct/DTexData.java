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

import info.ata4.bsplib.vector.Vector3f;
import info.ata4.io.DataReader;
import info.ata4.io.DataWriter;
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
    public void read(DataReader in) throws IOException {
        reflectivity = Vector3f.read(in);
        texname = in.readInt();
        width = in.readInt();
        height = in.readInt();
        viewWidth = in.readInt();
        viewHeight = in.readInt();
    }

    @Override
    public void write(DataWriter out) throws IOException {
        Vector3f.write(out, reflectivity);
        out.writeInt(texname);
        out.writeInt(width);
        out.writeInt(height);
        out.writeInt(viewWidth);
        out.writeInt(viewHeight);
    }
}

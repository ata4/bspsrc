/*
 ** 2011 August 21
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.lump;

import info.ata4.bsplib.io.ByteBufferDataOutput;
import info.ata4.bsplib.struct.Color32;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bsplib.vector.Vector4f;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LumpDataOutput extends ByteBufferDataOutput {
    
    public LumpDataOutput(AbstractLump lump) {
        super(lump.getBuffer());
        buf.position(0);
    }
    
    /**
     * Writes a 4 byte RGBA value
     *
     * @return Color32
     * @throws IOException on reading errors
     */
    public void writeColor32(Color32 c) throws IOException {
        writeInt(c.rgba);
    }
    
    /**
     * Writes a 12 byte 3-float vector
     *
     * @return vector
     * @throws IOException on reading errors
     */
    public void writeVector3f(Vector3f v) throws IOException {
        writeFloat(v.x);
        writeFloat(v.y);
        writeFloat(v.z);
    }
    
    /**
     * Writes a 16 byte 4-float vector
     *
     * @return vector
     * @throws IOException on reading errors
     */
    public void writeVector4f(Vector4f v) throws IOException {
        writeFloat(v.x);
        writeFloat(v.y);
        writeFloat(v.z);
        writeFloat(v.w);
    }
}

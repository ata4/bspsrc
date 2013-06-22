/*
 ** 2013 Juni 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.lump;

import info.ata4.bsplib.struct.Color32;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bsplib.vector.Vector4f;
import info.ata4.util.io.ByteBufferInput;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LumpInput extends ByteBufferInput {

    public LumpInput(AbstractLump lump) {
        super(lump.getBuffer());
    }
    
    /**
     * Checks the byte buffer for remaining bytes. Should always be called when
     * no remaining bytes are expected.
     *
     * @throws IOException if remaining bytes are found
     */
    public void checkRemaining() throws IOException {
        if (buf.hasRemaining()) {          
            throw new IOException(buf.remaining()
                    + " bytes remaining");
        }
    }

    /**
     * Reads a 4 byte RGBA value
     *
     * @return Color32
     * @throws IOException on reading errors
     */
    public Color32 readColor32() throws IOException {
        return new Color32(readInt());
    }

    /**
     * Reads a 12 byte 3-float vector
     *
     * @return vector
     * @throws IOException on reading errors
     */
    public Vector3f readVector3f() throws IOException {
        return new Vector3f(readFloat(), readFloat(), readFloat());
    }
    
    /**
     * Reads a 16 byte 4-float vector
     *
     * @return vector
     * @throws IOException on reading errors
     */
    public Vector4f readVector4f() throws IOException {
        return new Vector4f(readFloat(), readFloat(), readFloat(), readFloat());
    }
    
    /**
     * Reads a fixed size NUL-padded string
     *
     * @param limit total length of the string including NUL padding
     * @return String, without padding
     * @throws IOException on reading errors
     */
    public String readString(int limit) throws IOException {
        byte[] raw = new byte[limit];
        int length = 0;
        for (byte b; length < raw.length && (b = readByte()) != 0; length++) {
            raw[length] = b;
        }
        
        skipBytes(limit - length - 1);

        return new String(raw, 0, length, "ASCII");
    }
}

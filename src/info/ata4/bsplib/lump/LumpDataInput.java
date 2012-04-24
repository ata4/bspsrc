/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.lump;

import info.ata4.bsplib.io.ByteBufferDataInput;
import info.ata4.bsplib.struct.Color32;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.bsplib.vector.Vector4f;
import java.io.IOException;

/**
 * A wrapper for ByteBuffer with additional reading methods and error checking.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LumpDataInput extends ByteBufferDataInput {

    public LumpDataInput(AbstractLump lump) {
        super(lump.getBuffer());
        buf.position(0);
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
     * @param limit maximum length of the string, including padding
     * @return String, without padding
     * @throws IOException on reading errors
     */
    public String readString(int limit) throws IOException {
        StringBuilder sb = new StringBuilder();
        int startPos = buf.position();

        while (hasRemaining()) {
            // stop on string end
            if (sb.length() >= limit) {
                break;
            }

            byte c;

            // stop on NUL
            if ((c = readByte()) == 0) {
                break;
            }

            sb.append((char) c);
        }

        // skip padding, - 1 because of the first NUL that has already been read
        int remaining = limit - sb.length() - 1;
        if (remaining > 0) {
            skipBytes(remaining);
        }

        // check buffer position
        int bytesRead = buf.position() - startPos;
        if (bytesRead != limit) {
            throw new IOException("String reading error: expected length "
                    + limit + ", got " + bytesRead);
        }

        return sb.toString();
    }
}

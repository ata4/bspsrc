/*
 ** 2013 May 14
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
import info.ata4.util.io.ByteBufferIO;
import java.io.IOException;

/**
 * Lump data input/output class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LumpIO extends ByteBufferIO {

    public LumpIO(AbstractLump lump) {
        super(lump.getBuffer());
    }
    
    /**
     * Checks the byte buffer for remaining bytes. Should always be called when
     * no remaining bytes are expected.
     *
     * @throws IOException if remaining bytes are found
     */
    public void checkRemaining() throws IOException {
        if (hasRemaining()) {          
            throw new IOException(remaining()
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
     * @param length total length of the string including NUL padding
     * @return String, without padding
     * @throws IOException on reading errors
     */
    public String readString(int length) throws IOException {
        int startPos = position();
        
        String string = readString("ASCII", length, true);

        // check buffer position
        int bytesRead = position() - startPos;
        if (bytesRead != length) {
            throw new IOException("String reading error: expected length "
                    + length + ", got " + bytesRead);
        }

        return string;
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
    
    /**
     * Writes a fixed size NUL-padded string
     *
     * @param string string to write
     * @param length total length of the string including NUL padding
     * @throws IOException on writing errors
     * @throws IllegalArgumentException if the string lenght exceeds the limit
     */
    public void writeString(String string, int length) throws IOException {
        byte[] stringBytes = string.getBytes();
        
        if (length <= 0) {
            throw new IllegalArgumentException("Invalid length");
        }
        
        if (stringBytes.length > length) {
            throw new IllegalArgumentException("String is too long");
        }
        
        byte[] paddingBytes = new byte[length - string.length()];
        
        write(stringBytes);
        write(paddingBytes);
    }
    
}

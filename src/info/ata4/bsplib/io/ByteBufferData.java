/*
 ** 2011 August 22
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.HexDump;

/**
 * Common DataInput/DataOutput class for ByteBuffer
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class ByteBufferData {
    
    protected ByteBuffer buf;
    
    public ByteBufferData(ByteBuffer buf) {
        this.buf = buf;
    }
    
    public void dump(File file) throws IOException {
        ByteBuffer tmpBuf = buf.duplicate();
        InputStream is = new ByteBufferInputStream(tmpBuf);
        FileUtils.copyInputStreamToFile(is, file);
    }
    
    public void hexDump(OutputStream out) throws IOException {
        ByteBuffer tmpBuf = buf.duplicate();
        tmpBuf.rewind();
        byte[] data = new byte[tmpBuf.remaining()];
        buf.get(data);
        HexDump.dump(data, 0, out, 0);
    }

    public void hexDump() throws IOException {
        hexDump(System.out);
    }
    
    /**
     * Checks the lump buffer for remaining bytes. Should always be called when
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
     * Same as {@link java.nio.ByteBuffer}.hasRemaining()
     *
     * @return true, if there are remaining bytes in the lump buffer
     */
    public boolean hasRemaining() {
        return buf.hasRemaining();
    }

    /**
     * Same as {@link java.nio.ByteBuffer}.remaining()
     *
     * @return remaining bytes in the lump buffer
     */
    public int remaining() {
        return buf.remaining();
    }

    /**
     * Same as {@link java.nio.ByteBuffer}.position()
     *
     * @return position in the lump buffer
     */
    public int position() {
        return buf.position();
    }

    /**
     * Same as {@link java.nio.ByteBuffer}.position(int newPosition)
     * 
     * @param pos new buffer position
     * @throws IOException 
     */
    public void position(int pos) throws IOException {
        try {
            buf.position(pos);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Sets the buffer's position relative to the current one. 
     * 
     * @param pos new relative buffer position
     * @throws IOException 
     */
    public void move(int pos) throws IOException {
        position(position() + pos);
    }
}

/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.lib.lump;

import info.ata4.bspsrc.lib.io.LzmaUtil;
import info.ata4.io.buffer.ByteBufferInputStream;
import info.ata4.io.buffer.ByteBufferOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * A generic lump class for the normal lump and the game lump.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class AbstractLump {

    private static final Logger L = LogManager.getLogger();

    private ByteBuffer buffer = ByteBuffer.allocate(0);
    private int offset;
    private int version = 0;
    private int fourCC = 0;
    private boolean compressed = false;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Returns the number of bytes present in this lump. If the lump is
     * compressed, the uncompressed size will be returned.
     * 
     * @return lump length
     */
    public int getLength() {
        return buffer.limit();
    }

    /**
     * Returns a view of the buffer for this lump.
     * Changes to it are reflected in the lumps buffer.
     * 
     * @return byte buffer of this lump
     */
    public ByteBuffer getBuffer() {
        return buffer.duplicate().order(buffer.order());
    }

    /**
     * Set data is the data between current position of this buffer and its limit
     * @param buf
     */
    public void setBuffer(ByteBuffer buf) {
        buffer = buf.duplicate().order(buf.order());
        setCompressed(LzmaUtil.isCompressed(buffer));
    }

    public InputStream getInputStream() {
        return new ByteBufferInputStream(getBuffer());
    }

    public OutputStream getOutputStream() {
        return new ByteBufferOutputStream(getBuffer());
    }

    public void setVersion(int vers) {
        this.version = vers;
    }

    public int getVersion() {
        return version;
    }

    public int getFourCC() {
        return fourCC;
    }

    public void setFourCC(int fourCC) {
        this.fourCC = fourCC;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void compress() {
        if (compressed) {
            return;
        }

        try {
            buffer = LzmaUtil.compress(buffer);
        } catch (IOException ex) {
            L.error("Couldn't compress lump " + this, ex);
        }

        setCompressed(true);
    }

    public void uncompress() {
        if (!compressed) {
            return;
        }

        try {
            buffer = LzmaUtil.uncompress(buffer);
        } catch (IOException ex) {
            L.error("Couldn't uncompress lump " + this, ex);
        }

        setCompressed(false);
    }

    protected void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public abstract String getName();

    @Override
    public String toString() {
        return getName();
    }
}

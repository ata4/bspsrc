/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * InputStream wrapper for a ByteBuffer object.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ByteBufferInputStream extends InputStream {

    private final ByteBuffer buf;

    public ByteBufferInputStream(ByteBuffer buf, boolean rewind) {
        if (rewind) {
            buf.rewind();
        }
        
        this.buf = buf;
    }
    
    public ByteBufferInputStream(ByteBuffer buf) {
        this(buf, false);
    }

    @Override
    public synchronized int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }

        try {
            return 0xff & buf.get();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public synchronized int read(byte[] bytes, int off, int len) throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buf.remaining());

        try {
            buf.get(bytes, off, len);
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
        
        return len;
    }
}

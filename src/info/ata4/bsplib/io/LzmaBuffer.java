/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.io;

import lzma.LzmaDecoder;
import lzma.LzmaEncoder;
import info.ata4.util.io.ByteBufferInputStream;
import info.ata4.util.io.ByteBufferOutputStream;
import info.ata4.bsplib.util.StringMacroUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 * LZMA encoding and decoding helper class.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LzmaBuffer {
    
    private static final Logger L = Logger.getLogger(LzmaBuffer.class.getName());
    
    public final static int LZMA_ID = StringMacroUtils.makeID("LZMA");
    public final static int HEADER_SIZE = 17;

    public static ByteBuffer uncompress(ByteBuffer buffer) throws IOException {
        ByteOrder bo = buffer.order();
        ByteBuffer buf = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        buf.rewind();
        
        // ensure that this buffer is actually compressed
        if (buf.remaining() < HEADER_SIZE || buf.getInt() != LZMA_ID) {
            throw new IOException("Buffer is not compressed");
        }
        
        // read more from the header
        int actualSize = buf.getInt();
        int lzmaSize = buf.getInt();
        byte[] props = new byte[5];
        buf.get(props);
        
        int lzmaSizeBuf = buf.limit() - HEADER_SIZE;
        
        // check the size of the compressed buffer
        if (lzmaSizeBuf != lzmaSize) {
            L.log(Level.WARNING, "Difference in LZMA data length: found {0} bytes, expected {1}", new Object[]{lzmaSizeBuf, lzmaSize});
        }
        
        ByteBuffer bbUnpacked = ByteBuffer.allocateDirect(actualSize);

        ByteBufferInputStream is = new ByteBufferInputStream(buf);
        ByteBufferOutputStream os = new ByteBufferOutputStream(bbUnpacked);

        try {
            LzmaDecoder decoder = new LzmaDecoder();

            // set properties
            if (!decoder.setDecoderProperties(props)) {
                throw new IOException("Incorrect stream properties");
            }

            // decompress buffer
            if (!decoder.code(is, os, actualSize)) {
                throw new IOException("Error in LZMA stream");
            }
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
        
        // reset buffer
        bbUnpacked.rewind();
        bbUnpacked.order(bo);

        return bbUnpacked;
    }
    
    public static ByteBuffer compress(ByteBuffer buffer) throws IOException {
        ByteOrder bo = buffer.order();
        ByteBuffer buf = buffer.duplicate();
        buf.rewind();
        
        byte[] props;
        
        ByteBufferInputStream is = new ByteBufferInputStream(buf);
        ByteArrayOutputStream os = new ByteArrayOutputStream(buf.limit() / 8);
        
        try {
            LzmaEncoder encoder = new LzmaEncoder();
            
            // get properties
            props = encoder.getCoderProperties();
            
            // compress buffer
            encoder.code(is, os);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
        
        byte[] lzma = os.toByteArray();
        int size = HEADER_SIZE + lzma.length;
        
        ByteBuffer bbPacked = ByteBuffer.allocateDirect(size);
        bbPacked.order(ByteOrder.LITTLE_ENDIAN);
        
        // write header
        bbPacked.putInt(LZMA_ID);
        bbPacked.putInt(buf.limit());
        bbPacked.putInt(lzma.length);
        bbPacked.put(props);
        
        // write lzma data
        bbPacked.put(lzma);
        
        // reset buffer
        bbPacked.rewind();
        bbPacked.order(bo);
        
        return bbPacked;
    }

    public static boolean isCompressed(ByteBuffer buffer) {
        ByteBuffer bb = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        bb.rewind();
        
        // check if this buffer is compressed
        return bb.remaining() >= HEADER_SIZE && bb.getInt() == LZMA_ID;
    }
}

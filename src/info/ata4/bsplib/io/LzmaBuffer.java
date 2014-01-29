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

import info.ata4.bsplib.util.StringMacroUtils;
import info.ata4.io.ByteBufferInputStream;
import info.ata4.io.ByteBufferOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import lzma.LzmaDecoder;
import lzma.LzmaEncoder;
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
    
    private LzmaBuffer() {
    }

    public static ByteBuffer uncompress(ByteBuffer buffer) throws IOException {
        ByteOrder bo = buffer.order();
        ByteBuffer bbc = buffer.duplicate();
        bbc.order(ByteOrder.LITTLE_ENDIAN);
        bbc.rewind();
        
        // ensure that this buffer is actually compressed
        if (bbc.remaining() < HEADER_SIZE || bbc.getInt() != LZMA_ID) {
            throw new IOException("Buffer is not compressed");
        }
        
        // read more from the header
        int actualSize = bbc.getInt();
        int lzmaSize = bbc.getInt();
        byte[] props = new byte[5];
        bbc.get(props);
        
        int lzmaSizeBuf = bbc.limit() - HEADER_SIZE;
        
        // check the size of the compressed buffer
        if (lzmaSizeBuf != lzmaSize) {
            L.log(Level.WARNING, "Difference in LZMA data length: found {0} bytes, expected {1}", new Object[]{lzmaSizeBuf, lzmaSize});
        }
        
        ByteBuffer bbu = ByteBuffer.allocateDirect(actualSize);

        ByteBufferInputStream is = new ByteBufferInputStream(bbc);
        ByteBufferOutputStream os = new ByteBufferOutputStream(bbu);

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
        bbu.order(bo);
        bbu.rewind();

        return bbu;
    }
    
    public static ByteBuffer compress(ByteBuffer buffer) throws IOException {
        ByteOrder bo = buffer.order();
        ByteBuffer bbu = buffer.duplicate();
        bbu.rewind();
        
        byte[] props;
        
        ByteBufferInputStream is = new ByteBufferInputStream(bbu);
        ByteArrayOutputStream os = new ByteArrayOutputStream(bbu.limit() / 8);
        
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
        
        ByteBuffer bbc = ByteBuffer.allocateDirect(size);
        bbc.order(ByteOrder.LITTLE_ENDIAN);
        
        // write header
        bbc.putInt(LZMA_ID);
        bbc.putInt(bbu.limit());
        bbc.putInt(lzma.length);
        bbc.put(props);
        
        // write lzma data
        bbc.put(lzma);
        
        // reset buffer
        bbc.order(bo);
        bbc.rewind();
        
        return bbc;
    }

    public static boolean isCompressed(ByteBuffer buffer) {
        ByteBuffer bb = buffer.duplicate();
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.rewind();
        
        // check if this buffer is compressed
        return bb.remaining() >= HEADER_SIZE && bb.getInt() == LZMA_ID;
    }
}

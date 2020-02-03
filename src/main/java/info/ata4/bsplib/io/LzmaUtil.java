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
import info.ata4.io.buffer.ByteBufferInputStream;
import info.ata4.io.buffer.ByteBufferOutputStream;
import info.ata4.io.lzma.LzmaDecoderProps;
import info.ata4.io.lzma.LzmaEncoderProps;
import info.ata4.log.LogUtils;
import lzma.LzmaDecoder;
import lzma.LzmaEncoder;
import org.apache.commons.io.IOUtils;
import org.tukaani.xz.LZMAInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LZMA encoding and decoding helper class.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LzmaUtil {

    private static final Logger L = LogUtils.getLogger();

    public final static int LZMA_ID = StringMacroUtils.makeID("LZMA");
    public final static int HEADER_SIZE = 17;

    private LzmaUtil() {
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
        byte[] propsData = new byte[5];
        bbc.get(propsData);

        int lzmaSizeBuf = bbc.limit() - HEADER_SIZE;

        // check the size of the compressed buffer
        if (lzmaSizeBuf != lzmaSize) {
            L.log(Level.WARNING, "Difference in LZMA data length: found {0} bytes, expected {1}", new Object[]{lzmaSizeBuf, lzmaSize});
        }

        ByteBuffer bbu = ByteBuffer.allocateDirect(actualSize);

        try (
            ByteBufferInputStream is = new ByteBufferInputStream(bbc);
            ByteBufferOutputStream os = new ByteBufferOutputStream(bbu);        
        ) {
            LzmaDecoder decoder = new LzmaDecoder();

            // set properties
            LzmaDecoderProps props = new LzmaDecoderProps();
            props.setIncludeSize(false);
            props.fromArray(propsData);

            props.apply(decoder);

            // decompress buffer
            if (!decoder.code(is, os, actualSize)) {
                throw new IOException("Error in LZMA stream");
            }
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

        LzmaEncoderProps props = new LzmaEncoderProps();
        props.setIncludeSize(false);
        byte[] lzma;

        try (
            ByteBufferInputStream is = new ByteBufferInputStream(bbu);
            ByteArrayOutputStream os = new ByteArrayOutputStream(bbu.limit() / 8);
        ) {
            LzmaEncoder encoder = new LzmaEncoder();

            props.apply(encoder);

            // compress buffer
            encoder.code(is, os);

            lzma = os.toByteArray();
        }

        int size = HEADER_SIZE + lzma.length;

        ByteBuffer bbc = ByteBuffer.allocateDirect(size);
        bbc.order(ByteOrder.LITTLE_ENDIAN);

        // write header
        bbc.putInt(LZMA_ID);
        bbc.putInt(bbu.limit());
        bbc.putInt(lzma.length);
        bbc.put(props.toArray());

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

    public static LZMAInputStream fromZipEntry(InputStream rawInputStream, long uncompressedSize) throws IOException {
        // Lzma compressed zip is not supported by common-compress, so we do it manually
        // View https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT for specifications
        // (4.4.4 general purpose bit flag, 5.8 LZMA - Method 14)
        ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(rawInputStream, 9))
                .order(ByteOrder.LITTLE_ENDIAN);

        // Lzma version used to compress this data
        int majorVersion = buffer.get();
        int minorVersion = buffer.get();

        // Byte count of the following data represent as an unsigned short.
        // Should be = 5 (propByte + dictSize)
        int size = buffer.getShort() & 0xffff;
        if (size != 5) {
            L.warning(String.format("Unsupported lzma header size %ds. Version: %d.%d",
                    size, majorVersion, minorVersion));
        }

        byte propByte = buffer.get();

        // Dictionary size is an unsigned 32-bit little endian integer.
        int dictSize = buffer.getInt();

        return new LZMAInputStream(rawInputStream, uncompressedSize, propByte, dictSize);
    }
}

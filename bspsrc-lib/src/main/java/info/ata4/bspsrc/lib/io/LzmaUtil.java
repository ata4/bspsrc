/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.lib.io;

import info.ata4.bspsrc.lib.util.StringMacroUtils;
import info.ata4.io.buffer.ByteBufferInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAInputStream;
import org.tukaani.xz.LZMAOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * LZMA encoding and decoding helper class.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LzmaUtil {

    private static final Logger L = LogManager.getLogger();

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

        // properties data
        byte probByte = bbc.get();
        int dictSize = bbc.getInt();

        int lzmaSizeBuf = bbc.limit() - HEADER_SIZE;

        // check the size of the compressed buffer
        if (lzmaSizeBuf != lzmaSize) {
            L.warn("Difference in LZMA data length: found {} bytes, expected {}", lzmaSizeBuf, lzmaSize);
        }

        try (LZMAInputStream lzmaIn = new LZMAInputStream(new ByteBufferInputStream(bbc), actualSize, probByte, dictSize)) {
            return ByteBuffer.wrap(lzmaIn.readAllBytes()).order(bo);
        }
    }

    public static ByteBuffer compress(ByteBuffer buffer) throws IOException {
        ByteOrder bo = buffer.order();
        ByteBuffer bbu = buffer.duplicate();
        bbu.rewind();

        LZMA2Options options = new LZMA2Options();
        byte[] lzma;
        int props;
        try (
                InputStream bufferIn = new ByteBufferInputStream(bbu);
                ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
                LZMAOutputStream lzmaOut = new LZMAOutputStream(arrayOut, options, false)
        ) {
            bufferIn.transferTo(lzmaOut);
            lzma = arrayOut.toByteArray();
            props = lzmaOut.getProps();
        }

        int size = HEADER_SIZE + lzma.length;

        ByteBuffer bbc = ByteBuffer.allocateDirect(size);
        bbc.order(ByteOrder.LITTLE_ENDIAN);

        // write header
        bbc.putInt(LZMA_ID);
        bbc.putInt(bbu.limit());
        bbc.putInt(lzma.length);
        bbc.put((byte) props);
        bbc.putInt(options.getDictSize());

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
        ByteBuffer buffer = ByteBuffer.wrap(rawInputStream.readNBytes(9))
                .order(ByteOrder.LITTLE_ENDIAN);

        // Lzma version used to compress this data
        int majorVersion = buffer.get();
        int minorVersion = buffer.get();

        // Byte count of the following data represent as an unsigned short.
        // Should be = 5 (propByte + dictSize)
        int size = buffer.getShort() & 0xffff;
        if (size != 5) {
            L.warn(String.format("Unsupported lzma header size %ds. Version: %d.%d",
                    size, majorVersion, minorVersion));
        }

        byte propByte = buffer.get();

        // Dictionary size is an unsigned 32-bit little endian integer.
        int dictSize = buffer.getInt();

        return new LZMAInputStream(rawInputStream, uncompressedSize, propByte, dictSize);
    }
}

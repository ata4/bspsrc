/*
 ** 2013 April 17
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.lib.io;

import java.nio.ByteBuffer;

/**
 * Quick and simple XOR encryption/decryption utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class XORUtils {

    private static int BUFFER_SIZE = 4096;

    private XORUtils() {
    }

    public static int xor(int data, byte[] key) {
        byte[] dataBytes = new byte[]{
            (byte) data,
            (byte) (data >>> 8),
            (byte) (data >>> 16),
            (byte) (data >>> 24)
        };

        xor(dataBytes, key);

        return (dataBytes[3] << 24)
                | (dataBytes[2] << 16)
                | (dataBytes[1] << 8)
                | dataBytes[0];
    }

    public static void xor(byte[] data, byte[] key) {
        for (int j = 0; j < data.length; j++) {
            data[j] ^= key[j % key.length];
        }
    }

    public static void xor(ByteBuffer bb, byte[] key) {
        int bufSize = BUFFER_SIZE;
        bufSize -= bufSize % key.length;
        byte[] buf = new byte[bufSize];
        int len = buf.length;
        bb.rewind();

        while (bb.hasRemaining()) {
            len = Math.min(len, bb.remaining());
            bb.get(buf, 0, len);
            xor(buf, key);
            bb.position(bb.position() - len);
            bb.put(buf, 0, len);
        }
    }
}

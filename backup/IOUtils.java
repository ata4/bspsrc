/*
** 2011 June 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package com.nuclearvelocity.barracuda.bsplib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 * File and stream I/O utility class.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class IOUtils {
    
    /**
     * Buffer for read and write operations. 4-8K should be suitable for most
     * modern data volumes.
     */
    private final static int BUFFER_SIZE = 4096;
    private static byte[] buffer = new byte[BUFFER_SIZE];

    private IOUtils() {
    }

    /**
     * Calculates the CRC-32 checksum of a file.
     *
     * @return CRC-32 checksum
     * @throws IOException if the file couldn't be read
     */
    public static long getCrc32(File file) throws IOException {
        CRC32 crc32 = new CRC32();
        InputStream cis = new CheckedInputStream(new FileInputStream(file), crc32);
        long value = 0;

        try {
            while (cis.read(buffer) != -1);

            value = crc32.getValue();
        } finally {
            try {
                cis.close();
            } catch (Exception ex) {
            }
        }

        return value;
    }
    
    /**
     * Copies a file using file streams.
     * 
     * @param in input file
     * @param out output file
     * @return total bytes written
     * @throws IOException on I/O errors
     */
    public static long copyFile(final File in, final File out) throws IOException {
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
 
        long bytes = copyStream(fis, fos);

        // sync modified time
        out.setLastModified(in.lastModified());
        
        return bytes;
    }
    
    /**
     * Copies a file using file channels.
     * 
     * @param in input file
     * @param out output file
     * @return total bytes written
     * @throws IOException on I/O errors
     */
    public static long copyFileChanneled(final File in, final File out) throws IOException {
        FileChannel inChannel = new FileInputStream(in).getChannel();
        FileChannel outChannel = new FileOutputStream(out).getChannel();
        long bytes = 0;
        
        try {
            bytes = inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            try {
                inChannel.close();
                outChannel.close();
            } catch (Exception ex) {
            }
        }

        // sync modified time
        out.setLastModified(in.lastModified());
        
        return bytes;
    }

    /**
     * Copies an input stream to an output stream and closes the streams after
     * completion.
     * 
     * @param in input stream
     * @param out output stream
     * @return total bytes written
     * @throws IOException on I/O errors
     */
    public static long copyStream(final InputStream in, final OutputStream out) throws IOException {
        return copyStream(in, out, true);
    }

    /**
     * Copies an input stream to an output stream.
     * 
     * @param in input stream
     * @param out output stream
     * @param close close streams after completion?
     * @return total bytes written
     * @throws IOException on I/O errors
     */
    public static long copyStream(final InputStream in, final OutputStream out, boolean close) throws IOException {
        if (in == null || out == null) {
            throw new NullPointerException();
        }
        
        long bytes = 0;

        try {
            for (int i = 0; (i = in.read(buffer)) != -1;) {
                out.write(buffer, 0, i);
                bytes += i;
            }
        } finally {
            if (close) {
                try {
                    in.close();
                    out.close();
                } catch (Exception ex){
                }
            }
        }
        
        return bytes;
    }
}

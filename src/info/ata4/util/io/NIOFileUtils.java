/*
 ** 2011 August 20
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Utility class to open files via NIO buffers.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class NIOFileUtils {
    
    private NIOFileUtils() {
    }
    
    public static ByteBuffer load(File file) throws IOException {
        return load(file, 0, 0);
    }
    
    public static ByteBuffer load(File file, int offset, int length) throws IOException {
        ByteBuffer bb = ByteBuffer.allocateDirect(length > 0 ? length : (int) file.length());
        
        // read file into the buffer
        load(file, offset, length, bb);
        
        // prepare buffer to be read from the start
        bb.rewind();
        
        return bb;
    }
    
    public static void load(File file, int offset, int length, ByteBuffer dest) throws IOException {
        FileChannel fc = null;

        try {
            // fill the buffer with the file channel
            fc = FileUtils.openInputStream(file).getChannel();
            fc.read(dest, offset);
        } finally {
            IOUtils.closeQuietly(fc);
        }
    }
    
    public static ByteBuffer openReadOnly(File file) throws IOException {
        return openReadOnly(file, 0, 0);
    }
    
    public static ByteBuffer openReadOnly(File file, int offset, int length) throws IOException {
        ByteBuffer bb;
        FileChannel fc = null;
        
        try {
            fc = FileUtils.openInputStream(file).getChannel();
            // map entire file as byte buffer
            bb = fc.map(FileChannel.MapMode.READ_ONLY, offset, length > 0 ? length : fc.size());
        } finally {
            IOUtils.closeQuietly(fc);
        }
        
        return bb;
    }
    
    public static ByteBuffer openReadWrite(File file) throws IOException {
        return openReadWrite(file, 0, 0);
    }
    
    public static ByteBuffer openReadWrite(File file, int offset, int size) throws IOException {
        ByteBuffer bb;
        RandomAccessFile raf = null;
        
        try {
            // open random access file
            raf = new RandomAccessFile(file, "rw");
            
            int fileSize = offset + size;
            
            // reset file if a new size is set
            if (size > 0 && fileSize != raf.length()) {
                raf.setLength(0);
                raf.setLength(fileSize);
            } else {
                size = (int) raf.length() - offset;
            }
            
            // get file channel
            FileChannel fc = raf.getChannel();
            // map file as byte buffer
            bb = fc.map(FileChannel.MapMode.READ_WRITE, offset, size);
        } finally {
            IOUtils.closeQuietly(raf);
        }
        
        return bb;
    }
}

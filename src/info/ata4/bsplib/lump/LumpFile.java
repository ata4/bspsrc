/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bsplib.lump;

import info.ata4.bsplib.BspFile;
import info.ata4.util.io.ByteBufferUtils;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Low-level LMP file class for header and lump data access.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LumpFile {

    private static final Logger L = Logger.getLogger(LumpFile.class.getName());
    
    public static final int HEADER_SIZE = 20;

    // lump data
    private Lump lump;
    
    // .lmp source file
    private File file;

    // required to create the lump object
    private int bspVersion;
    
    // fields from lumpfileheader_t
    private int mapRev;
    
    public LumpFile(BspFile bsp) {
        this(bsp.getVersion());
        mapRev = bsp.getRevision();
    }

    public LumpFile(int bspVersion) {
        this.bspVersion = bspVersion;
    }

    public void load(File file, ByteOrder bo) throws IOException {
        this.file = file;
        
        L.log(Level.FINE, "Loading lump header from {0}", file.getName());
        
        ByteBuffer bb = ByteBufferUtils.openReadOnly(file.toPath());
        bb.order(bo);

        // make sure we have enough room for reading
        if (bb.capacity() < HEADER_SIZE) {
            throw new LumpException("Invalid or missing lump header");
        }

        // header
        int lumpOffset = bb.getInt();
        int lumpIndex = bb.getInt();
        int lumpVersion = bb.getInt();
        int lumpSize = bb.getInt();
        mapRev = bb.getInt();

        L.log(Level.FINER, "Lump offset: {0}", lumpOffset);
        L.log(Level.FINER, "Lump ID: {0}", lumpIndex);
        L.log(Level.FINER, "Lump version: {0}", lumpVersion);
        L.log(Level.FINER, "Lump size: {0}", lumpSize);
        L.log(Level.FINER, "Map revision: {0}", mapRev);

        if (lumpOffset != HEADER_SIZE) {
            throw new LumpException("Unexpected lump offset: " + lumpOffset);
        }

        if (lumpIndex < 0 || lumpIndex > BspFile.HEADER_LUMPS) {
            throw new LumpException("Invalid lump ID: " + lumpIndex);
        }

        if (lumpSize < 0 || lumpOffset > bb.limit()) {
            throw new LumpException("Invalid lump size: " + lumpOffset);
        }

        // lump data
        lump = new Lump(lumpIndex, LumpType.get(lumpIndex, bspVersion));
        lump.setBuffer(bb, lumpOffset, lumpSize);
        lump.setParentFile(file);
    }
    
    public void load(File file) throws IOException {
        load(file, ByteOrder.LITTLE_ENDIAN);
    }
    
    public void save(File file) throws IOException {
        if (lump == null) {
            throw new NullPointerException("Lump is undefined");
        }
        
        L.log(Level.FINE, "Saving lump header to {0}", file.getName());
        
        int size = HEADER_SIZE + lump.getLength();
        
        ByteBuffer bb = ByteBufferUtils.openReadWrite(file.toPath(), 0, size);
        
        bb.order(lump.getBuffer().order());
        
        // header
        bb.putInt(HEADER_SIZE);
        bb.putInt(lump.getIndex());
        bb.putInt(lump.getVersion());
        bb.putInt(lump.getLength());
        bb.putInt(mapRev);
        
        L.log(Level.FINE, "Saving lump data to {0}", file.getName());
        
        // lump data
        bb.put(lump.getBuffer());
    }

    public Lump getLump() {
        return lump;
    }
    
    public void setLump(Lump lump) {
        this.lump = lump;
    }

    public File getFile() {
        return file;
    }

    public int getMapRev() {
        return mapRev;
    }
    
    public void setMapRev(int mapRev) {
        this.mapRev = mapRev;
    }

    public int getBspVersion() {
        return bspVersion;
    }

    public void setBspVersion(int bspVersion) {
        this.bspVersion = bspVersion;
    }
}

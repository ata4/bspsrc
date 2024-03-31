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

import info.ata4.bspsrc.lib.BspFile;
import info.ata4.io.buffer.ByteBufferUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;

/**
 * Low-level LMP file class for header and lump data access.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class LumpFile {

    private static final Logger L = LogManager.getLogger();

    public static final int HEADER_SIZE = 20;

    // lump data
    private Lump lump;

    // .lmp source file
    private Path file;

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

    public void load(Path file, ByteOrder bo) throws IOException {
        this.file = file;

        L.debug("Loading lump header from {}", file.getFileName());

        ByteBuffer bb = ByteBufferUtils.openReadOnly(file);
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

        L.trace("Lump offset: {}", lumpOffset);
        L.trace("Lump ID: {}", lumpIndex);
        L.trace("Lump version: {}", lumpVersion);
        L.trace("Lump size: {}", lumpSize);
        L.trace("Map revision: {}", mapRev);

        if (lumpIndex < 0 || lumpIndex > BspFile.HEADER_LUMPS) {
            throw new LumpException("Invalid lump ID: " + lumpIndex);
        }
        if (lumpOffset < 0 || lumpOffset > bb.limit()) {
            L.warn("Invalid offset %d for lump %d, assuming %d".formatted(lumpOffset, lumpIndex, HEADER_SIZE));
            lumpOffset = HEADER_SIZE;
        }
        if (lumpSize < 0 || lumpOffset + lumpSize > bb.limit()) {
            int newLumpSize = bb.limit() - lumpOffset;
            L.warn("Invalid size %d for lump %d, assuming %d".formatted(lumpSize, lumpIndex, newLumpSize));
            lumpSize = newLumpSize;
        }

        // lump data
        lump = new Lump(lumpIndex, LumpType.get(lumpIndex, bspVersion));
        lump.setBuffer(bb.slice(lumpOffset, lumpSize).order(bb.order()));
        lump.setOffset(lumpOffset);
        lump.setParentFile(file);
    }

    public void load(Path file) throws IOException {
        load(file, ByteOrder.LITTLE_ENDIAN);
    }

    public void save(Path file) throws IOException {
        if (lump == null) {
            throw new NullPointerException("Lump is undefined");
        }

        L.debug("Saving lump header to {}", file.getFileName());

        int size = HEADER_SIZE + lump.getLength();

        ByteBuffer bb = ByteBufferUtils.openReadWrite(file, 0, size);

        bb.order(lump.getBuffer().order());

        // header
        bb.putInt(HEADER_SIZE);
        bb.putInt(lump.getIndex());
        bb.putInt(lump.getVersion());
        bb.putInt(lump.getLength());
        bb.putInt(mapRev);

        L.debug("Saving lump data to {}", file.getFileName());

        // lump data
        bb.put(lump.getBuffer());
    }

    public Lump getLump() {
        return lump;
    }

    public void setLump(Lump lump) {
        this.lump = lump;
    }

    public Path getFile() {
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

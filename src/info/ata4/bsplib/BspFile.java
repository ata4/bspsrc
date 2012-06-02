/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
**/

package info.ata4.bsplib;

import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppDB;
import info.ata4.bsplib.app.SourceAppID;
import info.ata4.bsplib.io.LzmaBuffer;
import info.ata4.bsplib.lump.*;
import info.ata4.bsplib.util.StringUtils;
import info.ata4.util.io.MappedFileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.EndianUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Low-level BSP file class for header and lump access.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspFile {
    
    // logger
    private static final Logger L = Logger.getLogger(BspFile.class.getName());

    // big-endian "VBSP"
    public static final int BSP_ID = StringUtils.makeID("VBSP");
    
    // endianness
    private ByteOrder bo;

    // version limits
    public static final int VERSION_MIN = 17;
    public static final int VERSION_MAX = 23;

    // lump limits
    public static final int HEADER_LUMPS = 64;
    public static final int HEADER_SIZE = 1036;

    // bsp source file
    private File file;

    // lump table
    private List<Lump> lumps = new ArrayList<Lump>(HEADER_LUMPS);
    
    // game lump data
    private List<GameLump> gameLumps = new ArrayList<GameLump>();

    // fields from dheader_t
    private int version;
    private int mapRev;
    
    private SourceApp app = SourceApp.UNKNOWN;
    
    /**
     * Opens the BSP file and loads its headers
     *
     * @param file BSP file to open
     * @param useLumpFiles load lump files associated to the BSP file, if true
     * @throws IOException if the file can't be opened or read
     */
    public void load(File file) throws IOException {
        this.file = file;

        L.log(Level.FINE, "Loading headers from {0}", file.getName());
        
        ByteBuffer bb = MappedFileUtils.openReadOnly(file);
        
        // make sure we have enough room for reading
        if (bb.capacity() < HEADER_SIZE) {
            throw new BspException("Invalid or missing header");
        }

        // read ident
        int ident = bb.getInt();
        
        // check ident and endianness...
        if (ident == BSP_ID) {
            bo = ByteOrder.BIG_ENDIAN;
        } else {
            // probably little-endian, swap before doing more tests
            ident = EndianUtils.swapInteger(ident);

            if (ident == BSP_ID) {
                bo = ByteOrder.LITTLE_ENDIAN;
            } else if (ident == 0x1E) {
                // No GoldSrc! Please!
                throw new BspException("The GoldSrc format is not supported");
            } else {
                throw new BspException("Unknown file ident: " + StringUtils.unmakeID(ident));
            }
        }

        L.log(Level.FINER, "Ident: {0} ({1})", new Object[]{ident, StringUtils.unmakeID(ident)});
        L.log(Level.FINER, "Endianness: {0}", bo);

        // set byte order
        bb.order(bo);

        // read version
        version = bb.getInt();

        L.log(Level.FINER, "Version: {0}", version);

        // Dark Messiah maps use 14 00 04 00 as version.
        // The actual BSP version is probably stored in the first two bytes...
        if (version == 0x40014) {
            L.finer("Found Dark Messiah header");
            app = SourceAppDB.getInstance().fromID(SourceAppID.DARK_MESSIAH);
            version &= 0xff;
        } else if (version > VERSION_MAX || version < VERSION_MIN) {
            throw new BspException("Unsupported version: " + version);
        }

        // hack for L4D2 BSPs
        if (version == 21 && bb.getInt(8) == 0) {
            L.finer("Found Left 4 Dead 2 header");
            app = SourceAppDB.getInstance().fromID(SourceAppID.LEFT_4_DEAD_2);
        }

        loadLumps(bb);
        loadGameLumps();

        mapRev = bb.getInt();

        L.log(Level.FINER, "Map revision: {0}", mapRev);
    }
    
    public void save(File file) throws IOException {
        int size = fixLumpOffsets();
        
        L.log(Level.FINE, "Saving headers to {0}", file.getName());
        
        ByteBuffer bb = MappedFileUtils.openReadWrite(file, size);
        
        bb.order(bo);
        bb.putInt(BSP_ID);
        bb.putInt(version);
        
        saveGameLumps();
        saveLumps(bb);
        
        bb.putInt(mapRev);
    }

    private void loadLumps(ByteBuffer bb) {
        L.fine("Loading lumps");

        for (int i = 0; i < HEADER_LUMPS; i++) {
            int vers, ofs, len, fourCC;

            if (app.getAppID() == SourceAppID.LEFT_4_DEAD_2) {
                vers = bb.getInt();
                ofs = bb.getInt();
                len = bb.getInt();
            } else {
                ofs = bb.getInt();
                len = bb.getInt();
                vers = bb.getInt();
            }

            // length of the uncompressed lump, 0 if not compressed
            fourCC = bb.getInt();

            LumpType ltype = LumpType.get(i, version);

            // fix invalid offsets
            if (ofs > bb.limit()) {
                int ofsOld = ofs;
                ofs = bb.limit();
                len = 0;
                L.log(Level.WARNING, "Invalid lump offset {0} in {1}, assuming {2}",
                        new Object[]{ofsOld, ltype, ofs});
            } else if (ofs < 0) {
                int ofsOld = ofs;
                ofs = 0;
                len = 0;
                L.log(Level.WARNING, "Negative lump offset {0} in {1}, assuming {2}",
                        new Object[]{ofsOld, ltype, ofs});
            }

            // fix invalid lengths
            if (ofs + len > bb.limit()) {
                int lenOld = len;
                len = bb.limit() - ofs;
                L.log(Level.WARNING, "Invalid lump length {0} in {1}, assuming {2}",
                        new Object[]{lenOld, ltype, len});
            } else if (len < 0) {
                int lenOld = len;
                len = 0;
                L.log(Level.WARNING, "Negative lump length {0} in {1}, assuming {2}",
                        new Object[]{lenOld, ltype, len});
            }

            Lump l = new Lump(i, ltype);
            l.setBuffer(bb, ofs, len);
            l.setParentFile(file);
            l.setFourCC(fourCC);
            l.setVersion(vers);
            lumps.add(l);
        }
    }
    
    /**
     * Writes all lumps to the given buffer.
     * 
     * @param bb destination buffer to write lumps into
     */
    private void saveLumps(ByteBuffer bb) {
        L.fine("Saving lumps");
        
        for (Lump l : lumps) {
            // write header
            if (app.getAppID() == SourceAppID.LEFT_4_DEAD_2) {
                bb.putInt(l.getVersion());
                bb.putInt(l.getOffset());
                bb.putInt(l.getLength());
            } else {
                bb.putInt(l.getOffset());
                bb.putInt(l.getLength());
                bb.putInt(l.getVersion());
            }
            
            bb.putInt(l.getFourCC());
            
            if (l.getLength() == 0) {
                continue;
            }
            
            // write buffer data
            ByteBuffer lbb = l.getBuffer();
            lbb.rewind();
            int tmpPos = bb.position();
            bb.position(l.getOffset());
            bb.put(lbb);
            bb.position(tmpPos);
        }
    }

    public void loadLumpFiles() {
        L.fine("Loading lump files");

        File parentFile = file.getAbsoluteFile().getParentFile();
        File[] lumpFiles = parentFile.listFiles(new LumpFileFilter(file));

        // load all found lump files
        for (File lumpFile : lumpFiles) {
            L.log(Level.INFO, "Loading {0}", lumpFile);
            try {
                // load lump from file
                LumpFile lumpFileExt = new LumpFile(version);
                lumpFileExt.load(lumpFile, bo);
                
                // override internal lump
                Lump l = lumpFileExt.getLump();
                lumps.set(l.getIndex(), l);
                
                if (l.getType() == LumpType.LUMP_GAME_LUMP) {
                    // reload game lumps
                    gameLumps.clear();
                    loadGameLumps();
                }
            } catch (IOException ex) {
                L.log(Level.WARNING, "Unable to load lump file " + lumpFile.getName(), ex);
            }
        }
    }
    
    private void loadGameLumps() {
        L.fine("Loading game lumps");
        
        try {
            Lump l = getLump(LumpType.LUMP_GAME_LUMP);
            LumpDataInput lr = l.getDataInput();
            
            int glumps = lr.readInt();

            for (int i = 0; i < glumps; i++) {
                int ofs, len, flags, vers, fourCC;

                if (app.getAppID() == SourceAppID.DARK_MESSIAH) {
                    lr.readInt(); // unknown
                }

                fourCC = lr.readInt();

                if (app.getAppID() == SourceAppID.VINDICTUS) {
                    flags = lr.readInt();
                    vers = lr.readInt();
                } else {
                    flags = lr.readUnsignedShort();
                    vers = lr.readUnsignedShort();
                }

                ofs = lr.readInt();

                if (flags == 1) {
                    // game lump is compressed, use next entry offset to determine
                    // compressed size
                    lr.move(12);
                    int nextOfs = lr.readInt();
                    len = nextOfs - ofs;
                    lr.move(-12);
                } else {
                    len = lr.readInt();
                }

                // Offset is relative to the beginning of the BSP file,
                // not to the game lump.
                // FIXME: this isn't the case for the console version of Portal 2,
                // is there a better way to detect this?
                if (ofs - l.getOffset() > 0) {
                    ofs -= l.getOffset();
                }

                String glName = StringUtils.unmakeID(fourCC);

                // give dummy entries more useful names
                if (glName.trim().isEmpty()) {
                    glName = "<dummy>";
                }

                // fix invalid offsets
                if (ofs > l.getLength()) {
                    int ofsOld = ofs;
                    ofs = l.getLength();
                    len = 0;
                    L.log(Level.WARNING, "Invalid game lump offset {0} in {1}, assuming {2}",
                            new Object[]{ofsOld, glName, ofs});
                } else if (ofs < 0) {
                    int ofsOld = ofs;
                    ofs = 0;
                    len = 0;
                    L.log(Level.WARNING, "Negative game lump offset {0} in {1}, assuming {2}",
                            new Object[]{ofsOld, glName, ofs});
                }

                // fix invalid lengths
                if (ofs + len > l.getLength()) {
                    int lenOld = len;
                    len = l.getLength() - ofs;
                    L.log(Level.WARNING, "Invalid game lump length {0} in {1}, assuming {2}",
                            new Object[]{lenOld, glName, len});
                } else if (len < 0) {
                    int lenOld = len;
                    len = 0;
                    L.log(Level.WARNING, "Negative game lump length {0} in {1}, assuming {2}",
                            new Object[]{lenOld, glName, len});
                }

                GameLump gl = new GameLump();
                gl.setBuffer(l.getBuffer(), ofs, len);
                gl.setFourCC(fourCC);
                gl.setFlags(flags);
                gl.setVersion(vers);
                gameLumps.add(gl);
            }
            
            L.log(Level.FINE, "Game lumps: {0}", glumps);
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Couldn't load game lumps", ex);
        }
    }
    
    private void saveGameLumps() {
        L.fine("Saving game lumps");
        
        int size = getGameLumpHeaderSize();
        
        // add game lump sizes
        for (GameLump gl : gameLumps) {
            size += gl.getLength();
        }
        
        try {
            Lump l = getLump(LumpType.LUMP_GAME_LUMP);
            LumpDataOutput lw = l.getDataOutput(size);
            lw.writeInt(gameLumps.size());

            for (GameLump gl : gameLumps) {
                // write header
                lw.writeInt(gl.getFourCC());
                lw.writeShort(gl.getFlags());
                lw.writeShort(gl.getVersion());
                lw.writeInt(gl.getOffset());
                lw.writeInt(gl.getLength());

                int ofs = gl.getOffset();

                // Offset is relative to the beginning of the BSP file,
                // not to the game lump.
                // FIXME: this isn't the case for the console version of Portal 2,
                // is there a better way to detect this?
                if (ofs - l.getOffset() > 0) {
                    ofs -= l.getOffset();
                }

                // write buffer data
                int tmpPos = lw.position();
                lw.position(ofs);
                lw.write(gl.getBuffer());
                lw.position(tmpPos);
            }
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Couldn''t save game lumps", ex);
        }
    }
    
    /**
     * Returns the header size of all currently loaded game lumps.
     * 
     * @return game lump header size
     */
    private int getGameLumpHeaderSize() {
        // lumpCount + dgamelump_t[lumpCount]
        return 4 + 16 * gameLumps.size();
    }
    
    /**
     * Recalculates all lump offsets while retaining their order to ensure
     * that there will be no gaps in the BSP file when written.
     * 
     * @return offset of the last lump, equals the BSP file size
     */
    private int fixLumpOffsets() {
        List<Lump> lumpsTmp = getLumpsSorted();
        
        // always start behind the header or terrible things will happen!
        int offset = HEADER_SIZE;
        
        for (Lump l : lumpsTmp) {
            if (l.getLength() == 0) {
                l.setOffset(0); // some maps have offsets for empty lumps
                continue;
            }
            
            l.setOffset(offset);
            
            if (l.getType() == LumpType.LUMP_GAME_LUMP) {
                l.setOffset(offset);
                offset += getGameLumpHeaderSize();
                for (GameLump gl : gameLumps) {
                    gl.setOffset(offset);
                    offset += gl.getLength();
                }
            } else {
                offset += l.getLength();
            }
        }
        
        return offset;
    }
    
    /**
     * Returns the array for all currently loaded lumps.
     *
     * @return lump array
     */
    public List<Lump> getLumps() {
        return new ArrayList<Lump>(lumps);
    }

    /**
     * Returns the array for all currently loaded lumps, sorted by their offsets.
     *
     * @return lump array
     */
    public List<Lump> getLumpsSorted() {
        List<Lump> lumpsSorted = getLumps();

        Collections.sort(lumpsSorted, new Comparator<Lump>() {
            public int compare(Lump l1, Lump l2) {
                if (l1.getOffset() == l2.getOffset()) {
                    return 0;
                } else if (l1.getOffset() > l2.getOffset()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        return lumpsSorted;
    }

    /**
     * Returns the lump for the given lump type.
     *
     * @param type
     * @return lump
     */
    public Lump getLump(LumpType type) {
        return lumps.get(type.getIndex());
    }
    
    /**
     * Returns the game lump list
     *
     * @return game lump list
     */
    public List<GameLump> getGameLumps() {
        return new ArrayList<GameLump>(gameLumps);
    }

    /**
     * Returns the game lump for the matching fourCC
     *
     * @param sid game lump fourCC
     * @return game lump, if found. otherwise null
     */
    public GameLump getGameLump(String sid) {
        for (GameLump gl : gameLumps) {
            if (gl.getName().equalsIgnoreCase(sid)) {
                return gl;
            }
        }

        return null;
    }
    
    /**
     * Compresses all lumps with exception for the pakfile lump.
     */
    public void compress() {
        L.info("Compressing lumps");
        
        for (Lump l : lumps) {
            // don't compress the game lump here and skip the pakfile
            if (l.getType() == LumpType.LUMP_GAME_LUMP ||
                    l.getType() == LumpType.LUMP_PAKFILE) {
                continue;
            }
            
            // don't compress if the result will always be bigger than uncompressed
            if (l.getLength() <= LzmaBuffer.HEADER_SIZE) {
                continue;
            }
            
            if (!l.isCompressed()) {
                L.log(Level.FINE, "Compressing {0}", l.getName());
                l.compress();
            }
        }
        
        for (GameLump gl : gameLumps) {
            // don't compress if the result will always be bigger than uncompressed
            if (gl.getLength() <= LzmaBuffer.HEADER_SIZE) {
                continue;
            }
            
            if (!gl.isCompressed()) {
                L.log(Level.FINE, "Compressing {0}", gl.getName());
                gl.compress();
            }
        }
        
        // add dummy game lump
        gameLumps.add(new GameLump());
    }
    
    /**
     * Uncompresses all compressed lumps.
     */
    public void uncompress() {
        L.info("Uncompressing lumps");
        
        for (Lump l : lumps) {
            if (l.isCompressed()) {
                l.uncompress();
            }
        }
        
        for (GameLump gl : gameLumps) {
            if (gl.isCompressed()) {
                gl.uncompress();
            }
        }
        
        // remove dummy game lump
        if (!gameLumps.isEmpty()) {
            gameLumps.remove(gameLumps.size() - 1);
        }
    }
    
    /**
     * Checks if the map contains compressed lumps.
     * 
     * @return true if there's at least one compressed lump
     */
    public boolean isCompressed() {
        for (Lump l : lumps) {
            if (l.isCompressed()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns the PakFile object for this BSP file to access the uncompressed
     * pakfile.
     * 
     * @return PakFile
     */
    public PakFile getPakFile() {
        return new PakFile(this);
    }
    
    /**
     * Creates a ZipArchiveInputStream instance for the uncompressed pakfile.
     *
     * @return the pakfile's ZipArchiveInputStream
     * @throws IOException
     * 
     * @deprecated use getPakFile().getArchiveInputStream() instead
     */
    @Deprecated
    public ZipArchiveInputStream getPakfileInputStream() {
        return getPakFile().getArchiveInputStream();
    }

    /**
     * Lump type compatibility check against the BSP version.
     *
     * @param type
     * @return true if the lump type is available for this BSP file
     */
    public boolean canReadLump(LumpType type) {
        return type.getBspVersion() == -1 || type.getBspVersion() <= version;
    }

    /**
     * Generates the file name for the next new lump file
     *
     * @return new lump file
     */
    public File getNextLumpFile() {
        File parentFile = file.getAbsoluteFile().getParentFile();

        LumpFileFilter filter = new LumpFileFilter(file);
        parentFile.listFiles(filter);
        
        // get highest index
        int lumpIndex = filter.getHighestLumpIndex() + 1;
        
        String lumpFile = getName() + "_l_" + lumpIndex + ".lmp";

        L.log(Level.FINE, "Next lump file: {0}", lumpFile);

        return new File(parentFile, lumpFile);
    }
    
    /**
     * Returns the name of this file, i.e. the file name without the .bsp extension.
     * 
     * @return BSP name
     */
    public String getName() {
        return FilenameUtils.getBaseName(file.getPath());
    }

    /**
     * Returns the file on the file system for this BSP file.
     * 
     * @return file
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns the BSP version
     *
     * @return BSP version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns the map revision, usually equals the "mapversion" keyvalue in the
     * world spawn of the entity lump and the associated VMF file. 
     *
     * @return map revision
     */
    public int getRevision() {
        return mapRev;
    }

    /**
     * Returns the endianness of the BSP file, detected by the order of the
     * ident value in the header.
     *
     * @return byte order of the BSP
     */
    public ByteOrder getByteOrder() {
        return bo;
    }

    public SourceApp getSourceApp() {
        return app;
    }

    public void setSourceApp(SourceApp appID) {
        this.app = appID;
    }
}

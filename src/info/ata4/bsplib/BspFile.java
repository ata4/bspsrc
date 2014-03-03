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
import info.ata4.bsplib.util.StringMacroUtils;
import info.ata4.io.DataInputReader;
import info.ata4.io.DataOutputWriter;
import static info.ata4.io.SeekOrigin.*;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.io.util.XORUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public static final int BSP_ID = StringMacroUtils.makeID("VBSP");
    
    // endianness
    private ByteOrder bo;
    
    // version limits
    public static final int VERSION_MIN = 17;
    public static final int VERSION_MAX = 23;

    // lump limits
    public static final int HEADER_LUMPS = 64;
    public static final int HEADER_SIZE = 1036;
    public static final int MAX_LUMPFILES = 128;

    // BSP source file
    private Path file;
    
    // BSP name, usually the file name without ".bsp"
    private String name;

    // lump table
    private List<Lump> lumps = new ArrayList<>(HEADER_LUMPS);
    
    // game lump data
    private List<GameLump> gameLumps = new ArrayList<>();

    // fields from dheader_t
    private int version;
    private int mapRev;
    
    private SourceApp app = SourceApp.UNKNOWN;
    
    public BspFile() {
    }
    
    public BspFile(Path file, boolean memMapping) throws IOException {
        loadImpl(file, memMapping);
    }
    
    public BspFile(Path file) throws IOException {
        loadImpl(file);
    }
    
    private void loadImpl(Path file) throws IOException {
        load(file);
    }
    
    private void loadImpl(Path file, boolean memMapping) throws IOException {
        load(file, memMapping);
    }
    
    /**
     * Opens the BSP file and loads its headers and lumps.
     *
     * @param file BSP file to open
     * @param memMapping if set to true, the file will be mapped to memory. This
     *            is faster than loading it entirely to memory first, but the map
     *            can't be saved in the same file because of memory management
     *            restrictions of the JVM.
     * @throws IOException if the file can't be opened or read
     */
    public void load(Path file, boolean memMapping) throws IOException {
        this.file = file;
        this.name = FilenameUtils.removeExtension(file.getFileName().toString());
        
        L.log(Level.FINE, "Loading headers from {0}", name);
        
        ByteBuffer bb = createBuffer(memMapping);
        
        L.log(Level.FINER, "Endianness: {0}", bo);

        // set byte order
        bb.order(bo);

        // read version
        version = bb.getInt();

        L.log(Level.FINER, "Version: {0}", version);

        if (version == 0x40014) {
            // Dark Messiah maps use 14 00 04 00 as version.
            // The actual BSP version is probably stored in the first two bytes...
            L.finer("Found Dark Messiah header");
            app = SourceAppDB.getInstance().fromID(SourceAppID.DARK_MESSIAH);
            version &= 0xff;
        } else if (version == 27) {
            // Contagion maps use version 27, ignore VERSION_MAX in this case 
            L.finer("Found Contagion header");
            app = SourceAppDB.getInstance().fromID(SourceAppID.CONTAGION);
        } else if (version > VERSION_MAX || version < VERSION_MIN) {
            throw new BspException("Unsupported version: " + version);
        }

        // hack for L4D2 BSPs
        if (version == 21 && bb.getInt(8) == 0) {
            L.finer("Found Left 4 Dead 2 header");
            app = SourceAppDB.getInstance().fromID(SourceAppID.LEFT_4_DEAD_2);
        }
        
        // extra int for Contagion
        if (app.getAppID() == SourceAppID.CONTAGION) {
            bb.getInt(); // always 0?
        }

        loadLumps(bb);
        loadGameLumps();

        mapRev = bb.getInt();

        L.log(Level.FINER, "Map revision: {0}", mapRev);
    }
    
    /**
     * Opens the BSP file and loads its headers and lumps. The map is loaded
     * with memory-mapping for efficiency.
     *
     * @param file BSP file to open
     * @throws IOException if the file can't be opened or read
     */
    public void load(Path file) throws IOException {
        load(file, true);
    }
    
    public void save(Path file) throws IOException {
        this.file = file;
        this.name = file.getFileName().toString();
        
        L.log(Level.FINE, "Saving headers to {0}", name);
        
        int size = fixLumpOffsets();
        ByteBuffer bb = ByteBufferUtils.openReadWrite(file, 0, size);
        
        bb.order(bo);
        bb.putInt(BSP_ID);
        bb.putInt(version);
        
        saveGameLumps();
        saveLumps(bb);
        
        bb.putInt(mapRev);
    }
    
    /**
     * Creates a byte buffer for the BSP file, checks its ident, detects its
     * endianness and performs other low-level I/O operations if required.
     * 
     * @param memMapping true if the map should be loaded as a memory-mapped file
     * @throws IOException if the buffer couldn't be created
     * @throws BspException if the header or file format is invalid
     */
    private ByteBuffer createBuffer(boolean memMapping) throws IOException, BspException {
        ByteBuffer bb;
        
        if (memMapping) {
            bb = ByteBufferUtils.openReadOnly(file);
        } else {
            bb = ByteBufferUtils.load(file);
        }
        
        // make sure we have enough room for reading
        if (bb.capacity() < HEADER_SIZE) {
            throw new BspException("Invalid or missing header");
        }
        
        int ident = bb.getInt();

        if (ident == BSP_ID) {
            // ordinary big-endian ident
            bo = ByteOrder.BIG_ENDIAN;
            return bb;
        }
        
        // probably little-endian, swap before doing more tests
        ident = EndianUtils.swapInteger(ident);

        if (ident == BSP_ID) {
            // ordinary little-endian ident
            bo = ByteOrder.LITTLE_ENDIAN;
            return bb;
        }

        if (ident == 0x1E) {
            // No GoldSrc! Please!
            throw new BspException("The GoldSrc format is not supported");
        }
        
        // check for XOR encryption
        // right now, only Tactical Intervention uses this, for whatever reason
        byte[] mapKey = new byte[32];
        
        // grab the key from a location where the deciphered map always(?) stores 
        // at least 32 null bytes
        bb.position(384);
        bb.get(mapKey);
        
        // try to decrypt only the ident for now, it's much faster...
        int identXor = XORUtils.xor(ident, mapKey);

        if (identXor == BSP_ID) {
            bo = ByteOrder.LITTLE_ENDIAN;
            
            L.log(Level.FINE, "Found Tactical Intervention XOR encryption using the key \"{0}\"", new String(mapKey));
            
            // fully reload the map into memory if that isn't the case already
            if (memMapping || bb.isReadOnly()) {
                bb = ByteBufferUtils.load(file);
            }
            
            // then decrypt it
            XORUtils.xor(bb, mapKey);
            
            // go back to the position after the ident
            bb.position(4);
            
            return bb;
        }
        
        throw new BspException("Unknown file ident: " + ident + " (" +
                StringMacroUtils.unmakeID(ident) + ")");
    }

    private void loadLumps(ByteBuffer bb) {
        L.fine("Loading lumps");

        for (int i = 0; i < HEADER_LUMPS; i++) {
            int vers, ofs, len, fourCC;
            
            // L4D2 maps use a different order
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
            l.setBuffer(ByteBufferUtils.getSlice(bb, ofs, len));
            l.setOffset(ofs);
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

        for (int i = 0; i < MAX_LUMPFILES; i++) {
            Path lumpFile = file.resolveSibling(String.format("%s_l_%d.lmp", name, i));
            
            if (!Files.exists(lumpFile)) {
                break;
            }
            
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
                L.log(Level.WARNING, "Unable to load lump file " + lumpFile.getFileName(), ex);
            }
        }
    }
    
    private void loadGameLumps() {
        L.fine("Loading game lumps");
        
        try {
            Lump lump = getLump(LumpType.LUMP_GAME_LUMP);
            DataInputReader in = DataInputReader.newReader(lump.getBuffer());
            
            // hack for Vindictus
            if (version == 20 && bo == ByteOrder.LITTLE_ENDIAN
                    && checkInvalidHeaders(in, false)
                    && !checkInvalidHeaders(in, true)) {
                L.finer("Found Vindictus game lump header");
                app = SourceAppDB.getInstance().fromID(SourceAppID.VINDICTUS);
            }
            
            int glumps = in.readInt();
            
            for (int i = 0; i < glumps; i++) {
                int ofs, len, flags, vers, fourCC;

                if (app.getAppID() == SourceAppID.DARK_MESSIAH) {
                    in.readInt(); // unknown
                }

                fourCC = in.readInt();
                
                // Vindictus uses integers rather than unsigned shorts
                if (app.getAppID() == SourceAppID.VINDICTUS) {
                    flags = in.readInt();
                    vers = in.readInt();
                } else {
                    flags = in.readUnsignedShort();
                    vers = in.readUnsignedShort();
                }

                ofs = in.readInt();

                if (flags == 1) {
                    // game lump is compressed, use next entry offset to determine
                    // compressed size
                    in.seek(12, CURRENT);
                    int nextOfs = in.readInt();
                    len = nextOfs - ofs;
                    in.seek(-12, CURRENT);
                } else {
                    len = in.readInt();
                }

                // Offset is relative to the beginning of the BSP file,
                // not to the game lump.
                // FIXME: this isn't the case for the console version of Portal 2,
                // is there a better way to detect this?
                if (ofs - lump.getOffset() > 0) {
                    ofs -= lump.getOffset();
                }
                
                String glName = StringMacroUtils.unmakeID(fourCC);

                // give dummy entries more useful names
                if (glName.trim().isEmpty()) {
                    glName = "<dummy>";
                }

                // fix invalid offsets
                if (ofs > lump.getLength()) {
                    int ofsOld = ofs;
                    ofs = lump.getLength();
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
                if (ofs + len > lump.getLength()) {
                    int lenOld = len;
                    len = lump.getLength() - ofs;
                    L.log(Level.WARNING, "Invalid game lump length {0} in {1}, assuming {2}",
                            new Object[]{lenOld, glName, len});
                } else if (len < 0) {
                    int lenOld = len;
                    len = 0;
                    L.log(Level.WARNING, "Negative game lump length {0} in {1}, assuming {2}",
                            new Object[]{lenOld, glName, len});
                }

                GameLump gl = new GameLump();
                gl.setBuffer(ByteBufferUtils.getSlice(lump.getBuffer(), ofs, len));
                gl.setOffset(ofs);
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
            ByteBuffer bb = ByteBuffer.allocateDirect(size);
            
            Lump l = getLump(LumpType.LUMP_GAME_LUMP);
            l.setBuffer(bb);
            
            DataOutputWriter out = DataOutputWriter.newWriter(bb);
            out.writeInt(gameLumps.size());

            for (GameLump gl : gameLumps) {
                // write header
                out.writeInt(gl.getFourCC());
                if (app.getAppID() == SourceAppID.VINDICTUS) {
                    out.writeInt(gl.getFlags());
                    out.writeInt(gl.getVersion());
                } else {
                    out.writeShort(gl.getFlags());
                    out.writeShort(gl.getVersion());
                }
                out.writeInt(gl.getOffset());
                out.writeInt(gl.getLength());

                int ofs = gl.getOffset();

                // Offset is relative to the beginning of the BSP file,
                // not to the game lump.
                // FIXME: this isn't the case for the console version of Portal 2,
                // is there a better way to detect this?
                if (ofs - l.getOffset() > 0) {
                    ofs -= l.getOffset();
                }

                // write buffer data
                int tmpPos = bb.position();
                bb.position(ofs);
                bb.put(gl.getBuffer());
                bb.position(tmpPos);
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
     * Heuristic detection of Vindictus game lump headers.
     * 
     * @param in DataInputReader for the game lump.
     * @param vin if true, test with Vindictus struct
     * @return true if the game lump header probably wasn't read correctly
     * @throws IOException 
     */
    private boolean checkInvalidHeaders(DataInputReader in, boolean vin) throws IOException {
        int glumps = in.readInt();
        
        for (int i = 0; i < glumps; i++) {
            String glName = StringMacroUtils.unmakeID(in.readInt());

            // check for unusual chars that indicate a reading error
            if (!glName.matches("^[a-zA-Z0-9]{4}$")) {
                in.position(0);
                return true;
            }

            in.skipBytes(vin ? 16 : 12);
        }

        in.position(0);
        return false;
    }
    
    /**
     * Returns the array for all currently loaded lumps.
     *
     * @return lump array
     */
    public List<Lump> getLumps() {
        return Collections.unmodifiableList(lumps);
    }

    /**
     * Returns the array for all currently loaded lumps, sorted by their offsets.
     *
     * @return lump array
     */
    public List<Lump> getLumpsSorted() {
        List<Lump> lumpsSorted = getLumps();

        Collections.sort(lumpsSorted, new Comparator<Lump>() {
            @Override
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
        return Collections.unmodifiableList(gameLumps);
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
        if (!gameLumps.isEmpty()
                && gameLumps.get(gameLumps.size() - 1).getLength() == 0) {
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
     * Lump type compatibility check against the BSP version.
     *
     * @param type
     * @return true if the lump type is available for this BSP file
     */
    public boolean canReadLump(LumpType type) {
        return type.getBspVersion() == -1 || type.getBspVersion() <= version;
    }

    /**
     * Generates the file name for the next new lump file based on the name of
     * this file.
     *
     * @return new lump file
     */
    public Path getNextLumpFile() {
        for (int i = 0; i < MAX_LUMPFILES; i++) {
            Path lumpFile = file.resolveSibling(String.format("%s_l_%d.lmp", name, i));
            
            if (!Files.exists(lumpFile)) {
                return lumpFile;
            }
        }
        
        return null;
    }
    
    /**
     * Returns the name of this file, i.e. the file name without the .bsp extension.
     * 
     * @return BSP name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Manually sets a new name for this file. This won't rename the actual file,
     * but changes the result of <code>{@link #getNextLumpFile}</code>.
     * 
     * @param name new BSP name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the file on the file system for this BSP file.
     * 
     * @return file
     */
    public Path getFile() {
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
     * Sets a new BSP version. Note that this won't convert any lump structures
     * to ensure compatibility between Source engine games that use this
     * version!
     *
     * @param version new BSP version
     * @throws BspException if the version is outside the allowed range of
     *                      VERSION_MIN to VERSION_MIN.
     */
    public void setVersion(int version) throws BspException {
        if (version > VERSION_MAX || version < VERSION_MIN) {
            throw new BspException("Unsupported version");
        }
        
        this.version = version;
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
     * Sets a new map revision number.
     * 
     * @param mapRev new map revision
     */
    public void setRevision(int mapRev) {
        this.mapRev = mapRev;
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

    /**
     * Returns the detected Source engine application for this file.
     * 
     * @return Source engine application
     */
    public SourceApp getSourceApp() {
        return app;
    }

    /**
     * Manually set the Source engine application used for file handling.
     * 
     * @param appID new Source engine application
     */
    public void setSourceApp(SourceApp appID) {
        this.app = appID;
    }
    
    /**
     * Returns the BSP reader for this file.
     * 
     * @return BSP reader for this file
     * @throws IOException on IO errors
     */
    public BspFileReader getReader() throws IOException {
        return new BspFileReader(this);
    }
}

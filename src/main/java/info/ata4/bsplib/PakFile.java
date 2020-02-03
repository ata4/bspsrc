/*
 ** 2011 November 4
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib;

import info.ata4.bsplib.io.LzmaUtil;
import info.ata4.bsplib.lump.Lump;
import info.ata4.bsplib.lump.LumpType;
import info.ata4.bspsrc.modules.texture.TextureSource;
import info.ata4.io.buffer.ByteBufferChannel;
import info.ata4.log.LogUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipMethod;
import org.tukaani.xz.LZMAInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Class to read BSP-embedded zip files (pakiles).
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class PakFile {

    private static final Logger L = LogUtils.getLogger();

    private static Pattern vhvPattern = Pattern.compile("sp(_hdr)?_\\d+\\.vhv");
    private static Pattern cubemapVtfPattern = Pattern.compile("c(-?\\d+)_(-?\\d+)_(-?\\d+)(\\.hdr)?\\.vtf");

    private final Lump pakLump;

    public PakFile(BspFile bspFile) {
        pakLump = bspFile.getLump(LumpType.LUMP_PAKFILE);
    }

    public ZipFile getZipFile() throws IOException {
        return new ZipFile(new ByteBufferChannel(pakLump.getBuffer()),
                "PakLump", "Cp437", false);
    }

    public void unpack(Path dest) throws IOException {
        unpack(dest, false);
    }

    public void unpack(Path dest, boolean direct) throws IOException {
        if (direct) {
            L.log(Level.INFO, "Extracting pakfile to {0}", dest);

            try (InputStream is = pakLump.getInputStream()) {
                Files.copy(is, dest);
            }
        } else {
            unpack(dest, s -> true);
        }
    }

    public void unpack(Path dest, Predicate<String> fileFilter) throws IOException {
        Files.createDirectories(dest);

        try (ZipFile zipFile = getZipFile()) {
            for (Enumeration<ZipArchiveEntry> enumeration = zipFile.getEntries(); enumeration.hasMoreElements();) {
                ZipArchiveEntry ze = enumeration.nextElement();
                String entryName = ze.getName();

                if (!fileFilter.test(entryName)) {
                    continue;
                }

                // create file path for zip entry and canonize it
                Path entryFile = dest.resolve(entryName).normalize();

                // don't allow file path to exit the extraction directory
                if (!entryFile.startsWith(dest)) {
                    L.log(Level.WARNING, "Skipped {0} (path traversal attempt)", entryName);
                    continue;
                }

                // don't overwrite any files
                if (Files.exists(entryFile)) {
                    L.log(Level.WARNING, "Skipped {0} (exists)", entryName);
                    continue;
                }

                if (zipFile.canReadEntryData(ze)) {
                    try (InputStream stream = zipFile.getInputStream(ze)) {
                        extract(stream, entryFile, entryName);
                    }
                } else if (ZipMethod.getMethodByCode(ze.getMethod()) == ZipMethod.LZMA) {
                    try (InputStream rawStream = zipFile.getRawInputStream(ze)) {
                        long uncompressedSize;
                        if ((ze.getRawFlag() & (1 << 1)) != 0) {
                            // If the entry uses EOS marker, use -1 to indicate
                            uncompressedSize = -1;
                        } else {
                            uncompressedSize = ze.getSize();
                        }

                        try (LZMAInputStream lzmaStream = LzmaUtil.fromZipEntry(rawStream, uncompressedSize)) {
                            extract(lzmaStream, entryFile, entryName);
                        }
                    }
                } else {
                    L.warning(String.format("Cannot extract unsupported: %s| method: %s(%s)| encryption: %b",
                            entryName,
                            ZipMethod.getMethodByCode(ze.getMethod()),
                            ze.getMethod(),
                            ze.getGeneralPurposeBit().usesEncryption()));
                }
            }
        }
    }

    private static void extract(InputStream stream, Path path, String entryName) throws IOException {
        L.log(Level.INFO, "Extracting {0}", entryName);
        Files.createDirectories(path.getParent());
        Files.copy(stream, path);
    }

    /**
     * Matches the specified {@code embeddedFileName} to a list of vbsp generated file name signatures:
     *
     * <ul>
     *     <li>Patched materials: {@link TextureSource#isPatchedMaterial(String)}
     *     <li>.vhv files: {@link #vhvPattern}
     *     <li>cubemap data: {@link #cubemapVtfPattern}
     *     <li>"cubemapdefault.vtf"
     *     <li>"cubemapdefault.hdr.vtf"
     *
     *
     * @param bspFileName The bsp file name
     * @param embeddedFileName The embedded file name
     * @return {@code true} if the specified {@code embeddedFileName} matches a vbsp generated file name,
     *         otherwise {@code false}
     */
    public static boolean isVBSPGeneratedFile(String bspFileName, String embeddedFileName) {
        return TextureSource
                .isPatchedMaterial(bspFileName)
                .or(fileName -> vhvPattern.matcher(fileName).matches()
                        || cubemapVtfPattern.matcher(fileName).matches()
                        || fileName.equalsIgnoreCase("cubemapdefault.vtf")
                        || fileName.equalsIgnoreCase("cubemapdefault.hdr.vtf"))
                .test(embeddedFileName);
    }
}

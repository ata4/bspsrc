/*
 ** 2011 November 4
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.lib;

import info.ata4.bspsrc.lib.io.LzmaUtil;
import info.ata4.bspsrc.lib.lump.Lump;
import info.ata4.bspsrc.lib.lump.LumpType;
import info.ata4.io.buffer.ByteBufferChannel;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tukaani.xz.LZMAInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Class to read BSP-embedded zip files (pakiles).
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class PakFile {

    private static final Logger L = LogManager.getLogger();

    private static Pattern vhvPattern = Pattern.compile("sp(_hdr)?_\\d+\\.vhv");
    private static Pattern cubemapVtfPattern = Pattern.compile("c(-?\\d+)_(-?\\d+)_(-?\\d+)(\\.hdr)?\\.vtf");

    private final Lump pakLump;

    public PakFile(BspFile bspFile) {
        pakLump = bspFile.getLump(LumpType.LUMP_PAKFILE);
    }

    public ZipFile getZipFile() throws IOException {
        return ZipFile.builder()
                .setSeekableByteChannel(new ByteBufferChannel(pakLump.getBuffer()))
                .setCharset("Cp437")
                .setUseUnicodeExtraFields(false)
                .get();
    }

    public void unpack(Path dest) throws IOException {
        unpack(dest, false);
    }

    public void unpack(Path dest, boolean direct) throws IOException {
        if (direct) {
            L.info("Extracting pakfile to {}", dest);

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
                Path entryFile;
                try {
                    entryFile = dest.resolve(entryName).normalize();
                } catch (InvalidPathException e) {
                    L.warn("Skipped %s (contains invalid characters)".formatted(entryName));

                    // we only care for the exception in debug mode.
                    // Users don't have to see the stacktrace in normal operation
                    L.debug(e);
                    continue;
                }

                // don't allow file path to exit outside the extraction directory
                if (!entryFile.startsWith(dest)) {
                    L.warn("Skipped {} (path traversal attempt)", entryName);
                    continue;
                }

                // don't overwrite any files
                if (Files.exists(entryFile)) {
                    L.warn("Skipped {} (exists)", entryName);
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
                    L.warn(String.format("Cannot extract unsupported: %s| method: %s(%s)| encryption: %b",
                            entryName,
                            ZipMethod.getMethodByCode(ze.getMethod()),
                            ze.getMethod(),
                            ze.getGeneralPurposeBit().usesEncryption()));
                }
            }
        }
    }

    private static void extract(InputStream stream, Path path, String entryName) throws IOException {
        L.info("Extracting {}", entryName);
        Files.createDirectories(path.getParent());
        Files.copy(stream, path);
    }

    /**
     * Matches the specified {@code fileName} to a list of vbsp generated file name signatures:
     *
     * <ul>
     *     <li>.vhv files: {@link #vhvPattern}
     *     <li>cubemap data: {@link #cubemapVtfPattern}
     *     <li>"cubemapdefault.vtf"
     *     <li>"cubemapdefault.hdr.vtf"
     *
     * @param fileName The embedded file name
     * @return {@code true} if the specified {@code fileName} matches a vbsp generated file name,
     *         otherwise {@code false}
     */
    public static boolean isVBSPGeneratedFile(String fileName) {
        return vhvPattern.matcher(fileName).find()
                || cubemapVtfPattern.matcher(fileName).find()
                || fileName.endsWith("cubemapdefault.vtf")
                || fileName.endsWith("cubemapdefault.hdr.vtf");
    }
}

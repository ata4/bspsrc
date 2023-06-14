/*
 ** 2011 November 4
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.decompiler;

import info.ata4.bspsrc.common.util.PathUtil;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspFileEntry {

    private final Path bspFile;
    private Path vmfFile;
    private Path pakfileDir;

    //No More Room in Hell only
    private Path nmoFile;
    private Path nmosFile;

    public BspFileEntry(Path bspFile, Path vmfFile) {
    	this.bspFile = requireNonNull(bspFile);
    	this.vmfFile = requireNonNull(vmfFile);
    	this.pakfileDir = replaceExtension(vmfFile, "");
        this.nmoFile = replaceExtension(bspFile, ".nmo");
        this.nmosFile = replaceExtension(vmfFile, ".nmos");
    }

    private static Path replaceExtension(Path path, String newExt) {
        String base = PathUtil.nameWithoutExtension(path).orElse("");
        return path.resolveSibling(base + newExt);
    }

    public Path getBspFile() {
        return bspFile;
    }

    public Path getVmfFile() {
        return vmfFile;
    }

    public void setVmfFile(Path vmfFile) {
        this.vmfFile = vmfFile;
    }

    public Path getPakDir() {
        return pakfileDir;
    }

    public void setPakDir(Path pakfileDir) {
        this.pakfileDir = pakfileDir;
    }

    //No More Room in Hell only
    public Path getNmoFile() {
        return nmoFile;
    }

    public Path getNmosFile() {
    	return nmosFile;
    }

	public void setNmosFile(Path nmosFile)
	{
		requireNonNull(nmosFile);
		this.nmosFile = nmosFile;
	}

    @Override
    public String toString() {
        return bspFile.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BspFileEntry other = (BspFileEntry) obj;
        return this.bspFile == other.bspFile || this.bspFile.equals(other.bspFile);
    }

    @Override
    public int hashCode() {
        return bspFile.hashCode();
    }
}

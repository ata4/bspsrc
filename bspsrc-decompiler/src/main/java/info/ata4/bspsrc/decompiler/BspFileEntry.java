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

import java.io.File;
import java.util.Objects;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspFileEntry {

    private final File bspFile;
    private File vmfFile;
    private File pakfileDir;

    //No More Room in Hell only
    private File nmoFile;
    private File nmosFile;

    public BspFileEntry(File bspFile) {
        this(bspFile, null);
    }

    public BspFileEntry(File bspFile, File vmfFile) {
    	Objects.requireNonNull(bspFile);

    	if (vmfFile == null) {
    	    vmfFile = replaceExtension(bspFile, "_d.vmf");
        }

    	this.bspFile = bspFile;
    	this.vmfFile = vmfFile;
    	this.pakfileDir = replaceExtension(vmfFile, "");
        this.nmoFile = replaceExtension(bspFile, ".nmo");
        this.nmosFile = replaceExtension(vmfFile, ".nmos");
    }

    private static File replaceExtension(File file, String newExt) {
        String base = PathUtil.nameWithoutExtension(file.toPath()).orElse("");
        File parentFile = file.getAbsoluteFile().getParentFile();

        return new File(parentFile, base + newExt);
    }

    public File getBspFile() {
        return bspFile;
    }

    public File getVmfFile() {
        return vmfFile;
    }

    public void setVmfFile(File vmfFile) {
        this.vmfFile = vmfFile;
    }

    public File getPakDir() {
        return pakfileDir;
    }

    public void setPakDir(File pakfileDir) {
        this.pakfileDir = pakfileDir;
    }

    //No More Room in Hell only
    public File getNmoFile() {
        return nmoFile;
    }

    public File getNmosFile() {
    	return nmosFile;
    }

	public void setNmosFile(File nmosFile)
	{
		Objects.requireNonNull(nmosFile);
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

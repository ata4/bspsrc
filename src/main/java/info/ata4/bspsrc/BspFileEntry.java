/*
 ** 2011 November 4
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc;

import java.io.File;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspFileEntry {

    private final File bspFile;
    private File vmfFile;
    private File pakfileDir;

    public BspFileEntry(File bspFile) {
        if (bspFile == null) {
            throw new NullPointerException();
        }
        
        this.bspFile = bspFile;
        this.vmfFile = replaceExtension(bspFile, "_d.vmf");
        this.pakfileDir = replaceExtension(bspFile, "");
    }
    
    private File replaceExtension(File file, String newExt) {
        String base = FilenameUtils.removeExtension(file.getName());
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
        if (this.bspFile != other.bspFile && !this.bspFile.equals(other.bspFile)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return bspFile.hashCode();
    }
}

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

import java.io.File;

/**
 * Default lump type for lumps inside a BSP file.
 * 
 * Original class name: unmap.Lump
 * Original author: Bob (Mellish?)
 * Original creation date: December 15, 2004, 8:48 PM
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Lump extends AbstractLump {

    private final LumpType type;
    private final int index;
    private File parentFile;

    public Lump(int index, LumpType type) {
        this.index = index;
        this.type = type;
    }

    public Lump(LumpType type) {
        this(type.getIndex(), type);
    }

    public void setParentFile(File parentFile) {
        this.parentFile = parentFile;
    }

    public File getParentFile() {
        return parentFile;
    }

    @Override
    public String getName() {
        return type.name();
    }

    public int getIndex() {
        return index;
    }

    public LumpType getType() {
        return type;
    }
    
    @Override
    public void setCompressed(boolean compressed) {
        super.setCompressed(compressed);
        setFourCC(compressed ? getLength() : 0);
    }
}

/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.modules;

import info.ata4.bsplib.BspFile;
import info.ata4.bsplib.BspFileReader;
import info.ata4.bsplib.struct.BspData;

/**
 * Basic abstract class for all modules that are reading BSP files with the
 * BspFileReader.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class ModuleRead {

    protected final BspFileReader reader;
    protected final BspData bsp;
    protected final BspFile bspFile;

    public ModuleRead(BspFileReader reader) {
        this.reader = reader;
        this.bsp = reader.getData();
        this.bspFile = reader.getBspFile();
    }
}

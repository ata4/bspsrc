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

import info.ata4.bsplib.BspFileReader;
import info.ata4.bspsrc.BspSourceConfig;
import info.ata4.bspsrc.VmfWriter;

/**
 * An extension of BspSourceModule for modules that also output VMF data
 * via the VmfFileWriter.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class BspSourceVmfModule extends BspSourceModule {

    protected final VmfWriter writer;

    public BspSourceVmfModule(BspSourceConfig config, BspFileReader reader, VmfWriter writer) {
        super(config, reader);
        this.writer = writer;
    }
    
    public BspSourceVmfModule(BspSourceVmfModule parent) {
        this(parent.config, parent.reader, parent.writer);
    }
}

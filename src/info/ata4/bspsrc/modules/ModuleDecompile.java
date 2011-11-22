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
 * An extension of ReadingModule for modules that also output VMF data
 * via the VmfWriter.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class ModuleDecompile extends ModuleRead {

    protected final VmfWriter writer;
    protected final BspSourceConfig config;

    public ModuleDecompile(BspFileReader reader, VmfWriter writer, BspSourceConfig config) {
        super(reader);
        this.writer = writer;
        this.config = config;
    }
    
    public ModuleDecompile(ModuleDecompile parent) {
        this(parent.reader, parent.writer, parent.config);
    }
}

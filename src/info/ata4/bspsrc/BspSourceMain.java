/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc;

import info.ata4.bspsrc.gui.BspSourceGui;
import info.ata4.bspsrc.cli.BspSourceCli;
import info.ata4.log.LogUtils;

/**
 * Main class to create either the CLI or the GUI version of BspSource
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspSourceMain {
    public static void main(String[] args) {
        LogUtils.configureLogger();
        
        // start GUI if no arguments were passed
        if (args.length == 0) {
            BspSourceGui.create();
        } else {
            BspSourceCli.create(args);
        }
    }
}

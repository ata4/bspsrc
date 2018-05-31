/*
 ** 2014 May 15
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc;

import info.ata4.bspsrc.cli.BspSourceCli;
import info.ata4.bspsrc.gui.BspSourceFrame;

/**
 * Simple launcher that starts the CLI if there's a console available or the GUI
 * otherwise. 
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspSourceLauncher {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        if (System.console() == null) {
            BspSourceFrame.main(args);
        } else {
            BspSourceCli.main(args);
        }
    }
}

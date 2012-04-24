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

import info.ata4.bspsrc.cli.BspSourceCli;
import info.ata4.bspsrc.gui.BspSourceFrame;
import info.ata4.log.LogUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * Main class to create either the CLI or the GUI version of BspSource
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspSourceMain {
    
    private static final Logger L = Logger.getLogger(BspSourceCli.class.getName());
    
    public static void main(String[] args) {
        LogUtils.configure();
        
        // start GUI if no arguments were passed
        if (args.length == 0) {
            startGui();
        } else {
            startCli(args);
        }
    }
    
    public static void startGui() {
        L.info("For command line usage, use the parameter -h");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            L.warning("Failed to set SystemLookAndFeel");
        }

        try {
            JFrame frame = new BspSourceFrame();
            frame.setTitle("BSPSource " + BspSource.VERSION);
            frame.setVisible(true);
        } catch (Throwable t) {
            // "Oh this is bad!"
            L.log(Level.SEVERE, "Fatal BSPSource error", t);
        }
    }
    
    public static void startCli(String[] args) {
        try {
            BspSourceCli cli = new BspSourceCli();
            BspSourceConfig cfg = cli.getConfig(args);
            
            BspSource bspsrc = new BspSource(cfg);
            bspsrc.run();
        } catch (Throwable t) {
            // "Really bad!"
            L.log(Level.SEVERE, "Fatal BSPSource error", t);
        }
    }
}

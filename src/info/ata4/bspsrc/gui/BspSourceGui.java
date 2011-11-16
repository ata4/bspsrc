/*
** 2011 April 5
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.bspsrc.gui;

import info.ata4.bspsrc.BspSource;
import javax.swing.JFrame;
import javax.swing.UIManager;
import java.util.logging.Logger;

/**
 * GUI creation class.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspSourceGui {

    private static final Logger L = Logger.getLogger(BspSourceGui.class.getName());
    
    private BspSourceGui() {
    }

    public static void create() {
        L.info("For command line usage, use the parameter -h");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            L.warning("Failed to set SystemLookAndFeel");
        }

        JFrame frame = new BspSourceFrame();
        frame.setTitle("BSPSource " + BspSource.VERSION);
        frame.setVisible(true);
    }
}

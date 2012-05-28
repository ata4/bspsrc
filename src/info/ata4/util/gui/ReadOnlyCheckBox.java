/*
 ** 2012 March 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBox;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ReadOnlyCheckBox extends JCheckBox {

    public ReadOnlyCheckBox() {
        super();
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
    }
}

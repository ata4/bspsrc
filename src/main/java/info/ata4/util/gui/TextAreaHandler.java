/*
 ** 2011 September 12
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.gui;

import javax.swing.*;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Logging handler for two JTextAreas, one for normal messages and one for errors.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class TextAreaHandler extends Handler {

    private JTextArea out;
    private JTextArea err;
    private boolean doneHeader;

    public TextAreaHandler(JTextArea out, JTextArea err) {
        this.out = out;
        this.err = err;
    }

    private void doHeaders() {
        if (!doneHeader) {
            String head = getFormatter().getHead(this);
            out.append(head);
            err.append(head);
            doneHeader = true;
        }
    }

    @Override
    public void publish(LogRecord record) {
        String msg;
        try {
            msg = getFormatter().format(record);
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }

        try {
            doHeaders();
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                err.append(msg);
            }

            out.append(msg);
            // make sure the last line is always visible
            out.setCaretPosition(out.getDocument().getLength());
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }
    }

    @Override
    public void flush() {
        // not required
    }

    @Override
    public void close() throws SecurityException {
        doHeaders();
    }
}

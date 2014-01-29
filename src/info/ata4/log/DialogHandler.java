/*
 ** 2012 June 3
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.log;

import java.awt.Component;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JOptionPane;

/**
 * Log handler for dialog messages.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DialogHandler extends Handler {

    private final Component parentComponent;
    
    public DialogHandler(Component parentComponent) {
        this.parentComponent = parentComponent;
        setFormatter(new DialogFormatter());
        // don't spam dialog messages
        setLevel(Level.WARNING);
    }

    @Override
    public void publish(LogRecord record) {
        if (record.getLevel().intValue() < getLevel().intValue()) {
            return;
        }
        
        int dialogType;
        String title;
        String message;
        
         try {
            message = getFormatter().format(record);
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }
        
        if (record.getLevel() == Level.WARNING) {
            dialogType = JOptionPane.WARNING_MESSAGE;
            title = "Warning";
        } else if (record.getLevel() == Level.SEVERE) {
            dialogType = JOptionPane.ERROR_MESSAGE;
            title = "Error";
        } else {
            dialogType = JOptionPane.INFORMATION_MESSAGE;
            title = "Information";
        }
        
        JOptionPane.showMessageDialog(parentComponent, message, title, dialogType);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
    
}

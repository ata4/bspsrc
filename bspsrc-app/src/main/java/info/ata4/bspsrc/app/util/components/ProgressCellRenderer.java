/*
 ** 2012 June 2
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.app.util.components;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Progress bar table cell renderer.
 * 
 * Based on http://www.informit.com/articles/article.aspx?p=24142&seqNum=3
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ProgressCellRenderer extends JProgressBar
        implements TableCellRenderer {

    public ProgressCellRenderer() {
        // Initialize the progress bar renderer to use a horizontal
        // progress bar.
        super(JProgressBar.HORIZONTAL);

        // Ensure that the progress bar border is not painted. (The
        // result is ugly when it appears in a table cell.)
        setBorderPainted(false);

        // Ensure that percentage text is painted on the progress bar.
        setStringPainted(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {
        if (value instanceof Integer) {
            // Ensure that the nonselected background portion of a
            // progress bar is assigned the same color as the table's 
            // background color. The resulting progress bar fits more
            // naturally (from a visual perspective) into the overall 
            // table's appearance.
            setBackground(table.getBackground());

            // Save the current progress bar value for subsequent
            // rendering.
            setValue((Integer) value);
        }

        return this;
    }
}
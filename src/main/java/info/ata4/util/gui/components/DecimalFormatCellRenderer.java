/*
 ** 2012 June 2
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.gui.components;

import java.awt.Component;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DecimalFormatCellRenderer extends DefaultTableCellRenderer {

    private final DecimalFormat formatter;

    public DecimalFormatCellRenderer(DecimalFormat formatter) {
        this.formatter = formatter;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {
        if (value instanceof Number) {
            value = formatter.format((Number) value);
        }

        JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected,
                hasFocus, row, col);

        c.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        return c;
    }
}

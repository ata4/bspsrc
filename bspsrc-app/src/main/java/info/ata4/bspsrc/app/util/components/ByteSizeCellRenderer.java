/*
 ** 2012 June 7
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.app.util.components;

import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ByteSizeCellRenderer extends DefaultTableCellRenderer {

    private boolean si;

    public ByteSizeCellRenderer(boolean si) {
        this.si = si;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Long) {
            value = FileUtils.byteCountToDisplaySize((Long) value);
        } else if (value instanceof Integer) {
            value = FileUtils.byteCountToDisplaySize((Integer) value);
        }

        JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        return c;
    }
}

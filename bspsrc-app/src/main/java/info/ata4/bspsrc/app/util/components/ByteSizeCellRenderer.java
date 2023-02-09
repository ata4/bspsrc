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

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ByteSizeCellRenderer extends DefaultTableCellRenderer {

    public ByteSizeCellRenderer() {

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Long l) {
            value = humanReadableByteCountSI(l);
        } else if (value instanceof Integer i) {
            value = humanReadableByteCountSI(i);
        }

        JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        return c;
    }

    // https://stackoverflow.com/a/3758880/7426899
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}

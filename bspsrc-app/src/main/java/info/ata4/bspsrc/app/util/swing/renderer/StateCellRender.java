package info.ata4.bspsrc.app.util.swing.renderer;

import info.ata4.bspsrc.app.src.gui.data.Task;
import info.ata4.bspsrc.app.util.swing.ui.Icons;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StateCellRender extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(
			JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row,
			int column
	) {
		JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		int rowHeight = table.getRowHeight(row);
		if (value instanceof Task.State state) {
			var icon = switch (state) {
				case PENDING -> null;
				case RUNNING -> Icons.PENDING_ICON.derive(rowHeight, rowHeight);
				case FINISHED -> Icons.SUCCESS_ICON.derive(rowHeight, rowHeight);
				case FAILED -> Icons.FAILED_ICON.derive(rowHeight, rowHeight);
			};

			cell.setText(null);
			cell.setIcon(icon);
		}

		return cell;
	}
}

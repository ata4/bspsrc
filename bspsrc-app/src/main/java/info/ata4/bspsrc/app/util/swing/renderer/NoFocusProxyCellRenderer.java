package info.ata4.bspsrc.app.util.swing.renderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

import static java.util.Objects.requireNonNull;

public class NoFocusProxyCellRenderer implements TableCellRenderer {

	private final TableCellRenderer tableCellRenderer;

	public NoFocusProxyCellRenderer(TableCellRenderer tableCellRenderer) {
		this.tableCellRenderer = requireNonNull(tableCellRenderer);
	}

	@Override
	public Component getTableCellRendererComponent(
			JTable table,
			Object value,
			boolean isSelected,
			boolean hasFocus,
			int row,
			int column
	) {
		return tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
	}
}

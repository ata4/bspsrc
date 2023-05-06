package info.ata4.bspsrc.app.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.jthemedetecor.OsThemeDetector;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiUtil {

	public static void debugDisplay(Supplier<Container> containerSupplier) {
		GuiUtil.setupFlatlaf();

		SwingUtilities.invokeLater(() -> {
			var frame = new JFrame();
			frame.setContentPane(containerSupplier.get());
			frame.pack();
			frame.setMinimumSize(frame.getSize());
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setVisible(true);
		});
	}

	public static void setupFlatlaf() {
		Consumer<Boolean> changeTheme = isDark -> SwingUtilities.invokeLater(() -> {
			if (isDark)
				FlatDarkLaf.setup();
			else
				FlatLightLaf.setup();

			FlatLaf.updateUI();
		});

		var detector = OsThemeDetector.getDetector();
		detector.registerListener(changeTheme);
		changeTheme.accept(detector.isDark());
	}

	public static void setColumnWidth(JTable table, int columnIndex, Object value, boolean alsoSetMax) {
		TableColumn column = table.getColumnModel().getColumn(columnIndex);

		var headerRenderer = column.getHeaderRenderer();
		if (headerRenderer == null)
			headerRenderer = table.getTableHeader().getDefaultRenderer();

		var headerCell = headerRenderer.getTableCellRendererComponent(
				table,
				column.getHeaderValue(),
				false,
				false,
				-1,
				columnIndex
		);
		int headerCellWidth = headerCell.getPreferredSize().width;

		var rowSorter = table.getRowSorter();
		if (rowSorter != null) {
			var sortKeys = rowSorter.getSortKeys();

			for (SortOrder sortOrder : SortOrder.values())
			{
				rowSorter.setSortKeys(List.of(new RowSorter.SortKey(columnIndex, sortOrder)));

				headerCell = headerRenderer.getTableCellRendererComponent(
						table,
						column.getHeaderValue(),
						false,
						false,
						-1,
						columnIndex
				);
				headerCellWidth = Math.max(headerCellWidth, headerCell.getPreferredSize().width);
			}

			rowSorter.setSortKeys(sortKeys);
		}

		var cell = table.getCellRenderer(-1, columnIndex)
				.getTableCellRendererComponent(
						table,
						value,
						false,
						false,
						-1,
						columnIndex
				);



		int width = Math.max(
				cell.getPreferredSize().width,
				headerCellWidth
		);

		column.setPreferredWidth(width);
		if (alsoSetMax) {
			column.setMaxWidth(width);
		}
	}
}

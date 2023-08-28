package info.ata4.bspsrc.app.util.swing.renderer;

import info.ata4.bspsrc.app.util.swing.ui.CustomFlatLabelUI;

import javax.swing.table.DefaultTableCellRenderer;

public class PathCellRenderer extends DefaultTableCellRenderer {

	public PathCellRenderer() {
		putClientProperty(CustomFlatLabelUI.LEADING_ELLIPSIS, true);
	}
}

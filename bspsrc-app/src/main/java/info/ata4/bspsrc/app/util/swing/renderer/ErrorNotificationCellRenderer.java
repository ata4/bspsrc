package info.ata4.bspsrc.app.util.swing.renderer;

import com.formdev.flatlaf.ui.FlatLineBorder;
import info.ata4.bspsrc.app.src.gui.data.ErrorNotification;
import info.ata4.bspsrc.app.util.swing.ui.Icons;

import javax.swing.*;
import java.awt.*;

public class ErrorNotificationCellRenderer implements ListCellRenderer<ErrorNotification> {

	private final JPanel pnlSpacing = new JPanel(new BorderLayout());
	private final JPanel pnl = new JPanel(new BorderLayout());
	private final JLabel lblMessage = new JLabel();

	public ErrorNotificationCellRenderer() {
		lblMessage.setIcon(Icons.FAILED_ICON.derive(20, 20));

		pnl.add(lblMessage);

		pnlSpacing.setOpaque(false);
		pnlSpacing.add(pnl);
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends ErrorNotification> list,
			ErrorNotification value,
			int index,
			boolean isSelected,
			boolean cellHasFocus
	) {
		pnl.applyComponentOrientation(list.getComponentOrientation());


		if (isSelected) {
			pnl.setBackground(list.getSelectionBackground());
			pnl.setForeground(list.getSelectionForeground());
			lblMessage.setBackground(list.getSelectionBackground());
			lblMessage.setForeground(list.getSelectionForeground());
		} else {
			pnl.setBackground(list.getBackground());
			pnl.setForeground(list.getForeground());
			lblMessage.setBackground(list.getBackground());
			lblMessage.setForeground(list.getForeground());
		}

		lblMessage.setText(value.message());

		// recursive?
		lblMessage.setEnabled(list.isEnabled());
		lblMessage.setFont(list.getFont());

		pnl.setBorder(new FlatLineBorder(
				new Insets(2, 2, 2, 2),
				Color.RED,
				0f,
				10
		));
		pnlSpacing.setBorder(BorderFactory.createEmptyBorder(
				index == 0 ? 2 : 0,
				2,
				index == list.getModel().getSize() - 1 ? 2 : 0,
				2
		));

		return pnlSpacing;
	}
}

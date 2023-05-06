package info.ata4.bspsrc.app.info.gui;

import info.ata4.bspsrc.app.util.GridBagConstraintsBuilder;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class Util {

	public static final int PANEL_INTERNAL_PADDING = 8;
	public static final Insets PANEL_COMPONENT_INSETS = new Insets(3, 3, 3, 3);

	public static Border createPanelBorder(String title) {
		return BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(title),
				createInternalPaddingBorder()
		);
	}
	public static Border createInternalPaddingBorder() {
		return BorderFactory.createEmptyBorder(
				PANEL_INTERNAL_PADDING,
				PANEL_INTERNAL_PADDING,
				PANEL_INTERNAL_PADDING,
				PANEL_INTERNAL_PADDING
		);
	}

	public static JTextField createDisplayTxtField(int columns) {
		var textField = new JTextField(columns);
		textField.setEditable(false);
		textField.setTransferHandler(null);
		return textField;
	}

	public static JCheckBox createDisplayCheckbox() {
		var checkBox = new JCheckBox();
		checkBox.setEnabled(false);
		return checkBox;
	}

	public static JPanel wrapWithAlign(
			Container container,
			GridBagConstraintsBuilder.Anchor anchor,
			GridBagConstraintsBuilder.Fill fill
	) {
		var panel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraintsBuilder()
				.weightX(1)
				.weightY(1)
				.anchor(anchor)
				.fill(fill)
				.build();

		panel.add(container, constraints);
		return panel;
	}
}

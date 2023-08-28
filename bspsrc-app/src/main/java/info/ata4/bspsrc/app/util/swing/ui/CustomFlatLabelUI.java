package info.ata4.bspsrc.app.util.swing.ui;

import com.formdev.flatlaf.ui.FlatLabelUI;
import com.formdev.flatlaf.ui.FlatUIUtils;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

public class CustomFlatLabelUI extends FlatLabelUI {

	public static final String LEADING_ELLIPSIS = "leadingEllipsis";

	public static ComponentUI createUI(JComponent c) {
		return FlatUIUtils.canUseSharedUI(c)
				? FlatUIUtils.createSharedUI(FlatLabelUI.class, () -> new CustomFlatLabelUI( true))
				: new CustomFlatLabelUI(false);
	}

	protected CustomFlatLabelUI(boolean shared) {
		super(shared);
	}

	@Override
	protected String layoutCL(
			JLabel label,
			FontMetrics fontMetrics,
			String text,
			Icon icon,
			Rectangle viewR,
			Rectangle iconR,
			Rectangle textR
	) {
		boolean leadingEllipsis = label.getClientProperty(LEADING_ELLIPSIS) == Boolean.TRUE;
		if (leadingEllipsis) {
			text = text == null ? null : new StringBuilder(text).reverse().toString();
		}

		String out = super.layoutCL(label, fontMetrics, text, icon, viewR, iconR, textR);

		if (leadingEllipsis) {
			out = out == null ? null : new StringBuilder(out).reverse().toString();
		}

		return out;
	}
}

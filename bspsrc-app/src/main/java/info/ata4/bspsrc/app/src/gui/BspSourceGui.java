package info.ata4.bspsrc.app.src.gui;

import info.ata4.bspsrc.app.src.gui.components.main.BspSourceFrame;
import info.ata4.bspsrc.app.src.gui.models.BspSourceModel;
import info.ata4.bspsrc.app.util.log.Log4jUtil;
import info.ata4.bspsrc.app.util.swing.GuiUtil;

import javax.swing.*;

import static java.util.Objects.requireNonNull;

public class BspSourceGui {
	public static void main(String[] args) {
		Log4jUtil.configure(requireNonNull(BspSourceGui.class.getResource("log4j2.xml")));
		GuiUtil.setupFlatlaf();

		SwingUtilities.invokeLater(() -> {
			var frame = new BspSourceFrame(new BspSourceModel());
			frame.setVisible(true);
		});
	}
}

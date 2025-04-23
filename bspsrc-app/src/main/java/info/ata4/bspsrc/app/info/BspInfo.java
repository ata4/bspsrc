package info.ata4.bspsrc.app.info;

import info.ata4.bspsrc.app.info.gui.BspInfoFrame;
import info.ata4.bspsrc.app.info.gui.models.BspInfoModel;
import info.ata4.bspsrc.app.util.log.Log4jUtil;
import info.ata4.bspsrc.app.util.swing.GuiUtil;

import javax.swing.*;

import static java.util.Objects.requireNonNull;

public class BspInfo {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		Log4jUtil.configure(requireNonNull(BspInfo.class.getResource("log4j2.xml")));
		GuiUtil.setupFlatlaf();

		SwingUtilities.invokeLater(() -> {
			var frame = new BspInfoFrame(new BspInfoModel());
			frame.setVisible(true);
		});
	}
}

package info.ata4.bspsrc.app.info;

import info.ata4.bspsrc.app.info.gui.BspInfoFrame;
import info.ata4.bspsrc.app.info.gui.models.BspInfoModel;
import info.ata4.bspsrc.app.util.GuiUtil;
import info.ata4.log.LogUtils;

import javax.swing.*;
import java.util.logging.Logger;

public class BspInfo {

	private static final Logger L = LogUtils.getLogger();

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		LogUtils.configure();
		GuiUtil.setupFlatlaf();

		SwingUtilities.invokeLater(() -> {
			var frame = new BspInfoFrame(new BspInfoModel());
			frame.setVisible(true);
		});
	}
}

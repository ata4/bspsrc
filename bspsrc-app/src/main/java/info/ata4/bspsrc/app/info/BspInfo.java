package info.ata4.bspsrc.app.info;

import info.ata4.bspsrc.app.info.gui.BspInfoFrame;
import info.ata4.bspsrc.app.info.gui.models.BspInfoModel;
import info.ata4.bspsrc.app.util.GuiUtil;
import info.ata4.bspsrc.app.util.log.Log4jUtil;

import javax.swing.*;
import java.io.IOException;
import java.net.URISyntaxException;

import static java.util.Objects.requireNonNull;

public class BspInfo {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException, URISyntaxException {
		Log4jUtil.configure(requireNonNull(BspInfo.class.getResource("log4j2.xml")));
		GuiUtil.setupFlatlaf();

		SwingUtilities.invokeLater(() -> {
			var frame = new BspInfoFrame(new BspInfoModel());
			frame.setVisible(true);
		});
	}
}

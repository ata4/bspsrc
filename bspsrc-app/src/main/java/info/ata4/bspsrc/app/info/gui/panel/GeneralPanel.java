package info.ata4.bspsrc.app.info.gui.panel;

import info.ata4.bspsrc.app.info.gui.models.BspInfoModel;
import info.ata4.bspsrc.app.util.swing.GridBagConstraintsBuilder;
import info.ata4.bspsrc.app.util.swing.GuiUtil;
import info.ata4.bspsrc.app.util.swing.components.URILabel;
import info.ata4.bspsrc.decompiler.modules.BspCompileParams;
import info.ata4.bspsrc.lib.BspFile;
import info.ata4.bspsrc.lib.app.SourceAppDB;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.nio.ByteOrder;

import static info.ata4.bspsrc.app.info.gui.Util.*;
import static info.ata4.bspsrc.app.util.swing.GridBagConstraintsBuilder.Anchor.LINE_END;
import static info.ata4.bspsrc.app.util.swing.GridBagConstraintsBuilder.Anchor.LINE_START;
import static info.ata4.bspsrc.app.util.swing.GridBagConstraintsBuilder.Fill.BOTH;
import static info.ata4.bspsrc.app.util.swing.GridBagConstraintsBuilder.Fill.HORIZONTAL;

public class GeneralPanel extends JPanel {

	// Headers
	public final JTextField txtName = createDisplayTxtField(0);
	public final JTextField txtVersion = createDisplayTxtField(3);
	public final JTextField txtRevision = createDisplayTxtField(4);
	public final JTextField txtCompressed = createDisplayTxtField(5);
	public final JTextField txtEndianness = createDisplayTxtField(13);
	public final JTextField txtComment = createDisplayTxtField(0);

	// Game
	public final JTextField txtGameName = createDisplayTxtField(0);
	public final JTextField txtAppId = createDisplayTxtField(8);
	public final URILabel lblSteamLink = new URILabel();

	// Checksums
	public final JTextField txtFileCrc = createDisplayTxtField(8);
	public final JTextField txtMapCrc = createDisplayTxtField(8);

	// Compiler Parameters
	public final JTextField txtVbsp = createDisplayTxtField(0);
	public final JTextField txtVvis = createDisplayTxtField(0);
	public final JTextField txtVrad = createDisplayTxtField(0);

	public GeneralPanel() {
		setLayout(new GridBagLayout());

		var builder = new GridBagConstraintsBuilder()
				.insets(PANEL_COMPONENT_INSETS)
				.weightX(1);

		add(createHeadersPanel(), builder.position(0, 0).fill(BOTH).build());
		add(createGamePanel(), builder.position(0, 1).fill(BOTH).build());
		add(createChecksumsPanel(), builder.position(0, 2).fill(BOTH).build());
		add(createCompilerParams(), builder.position(0, 3).fill(BOTH).build());
	}

	private JPanel createHeadersPanel() {
		var panel = new JPanel();
		panel.setBorder(createPanelBorder("Headers"));
		panel.setLayout(new GridBagLayout());

		var builder = new GridBagConstraintsBuilder()
				.insets(PANEL_COMPONENT_INSETS);

		panel.add(new JLabel("Name"), builder.position(0, 0).anchor(LINE_END).build());
		panel.add(txtName, builder.position(1, 0).anchor(LINE_START).width(4).weightX(1).fill(BOTH).build());

		panel.add(new JLabel("Version"), builder.position(0, 1).anchor(LINE_END).build());
		panel.add(txtVersion, builder.position(1, 1).anchor(LINE_START).fill(BOTH).build());
		panel.add(new JLabel("Revision"), builder.position(2, 1).anchor(LINE_END).build());
		panel.add(txtRevision, builder.position(3, 1).anchor(LINE_START).fill(BOTH).build());
		panel.add(Box.createGlue(), builder.position(4, 1).weightX(1).build());

		panel.add(new JLabel("Compressed"), builder.position(0, 2).anchor(LINE_END).build());
		panel.add(txtCompressed, builder.position(1, 2).anchor(LINE_START).fill(BOTH).build());
		panel.add(new JLabel("Endianness"), builder.position(2, 2).anchor(LINE_END).build());
		panel.add(txtEndianness, builder.position(3, 2).anchor(LINE_START).fill(BOTH).build());
		panel.add(Box.createGlue(), builder.position(4, 2).weightX(1).build());

		panel.add(new JLabel("Comment"), builder.position(0, 3).anchor(LINE_END).build());
		panel.add(txtComment, builder.position(1, 3).anchor(LINE_START).width(4).weightX(1).fill(BOTH).build());
		return panel;
	}

	private JPanel createGamePanel() {
		var panel = new JPanel();
		panel.setBorder(createPanelBorder("Game"));
		panel.setLayout(new GridBagLayout());

		var builder = new GridBagConstraintsBuilder()
				.insets(PANEL_COMPONENT_INSETS);

		panel.add(new JLabel("Name"), builder.position(0, 0).anchor(LINE_END).build());
		panel.add(txtGameName, builder.position(1, 0).width(2).anchor(LINE_START).weightX(1).fill(HORIZONTAL).build());
		panel.add(new JLabel("App-ID"), builder.position(0, 1).anchor(LINE_END).build());
		panel.add(txtAppId, builder.position(1, 1).fill(HORIZONTAL).build());
		panel.add(lblSteamLink, builder.position(2, 1).anchor(LINE_START).build());
		return panel;
	}

	private JPanel createChecksumsPanel() {
		var panel = new JPanel();
		panel.setBorder(createPanelBorder("Checksums"));
		panel.setLayout(new GridBagLayout());

		var builder = new GridBagConstraintsBuilder()
				.insets(PANEL_COMPONENT_INSETS);

		panel.add(new JLabel("File CRC"), builder.position(0, 0).anchor(LINE_END).build());
		panel.add(txtFileCrc, builder.position(1, 0).anchor(LINE_START).build());
		panel.add(new JLabel("Map CRC"), builder.position(2, 0).anchor(LINE_END).build());
		panel.add(txtMapCrc, builder.position(3, 0).anchor(LINE_START).build());
		panel.add(Box.createGlue(), builder.position(4, 0).weightX(1).build());
		return panel;
	}

	private JPanel createCompilerParams() {
		var panel = new JPanel();
		panel.setBorder(createPanelBorder("Detected compile parameters"));
		panel.setLayout(new GridBagLayout());

		var builder = new GridBagConstraintsBuilder()
				.insets(PANEL_COMPONENT_INSETS);

		panel.add(new JLabel("vbsp"), builder.position(0, 0).anchor(LINE_END).build());
		panel.add(txtVbsp, builder.position(1, 0).anchor(LINE_START).weightX(1).fill(HORIZONTAL).build());
		panel.add(new JLabel("vvis"), builder.position(0, 1).anchor(LINE_END).build());
		panel.add(txtVvis, builder.position(1, 1).anchor(LINE_START).weightX(1).fill(HORIZONTAL).build());
		panel.add(new JLabel("vrad"), builder.position(0, 2).anchor(LINE_END).build());
		panel.add(txtVrad, builder.position(1, 2).anchor(LINE_START).weightX(1).fill(HORIZONTAL).build());
		return panel;
	}

	public void update(BspInfoModel model) {
		updateGeneral(model);
		updateGame(model);
		updateChecksums(model);
		updateCompileParameters(model);
	}

	private void updateGeneral(BspInfoModel model) {
		String comment = model.getBspData()
				.flatMap(bspData -> bspData.entities.stream().findFirst())
				.map(entity -> entity.getValue("comment"))
				.orElse("");

		txtName.setText(model.getBspFile().map(BspFile::getName).orElse(""));
		txtVersion.setText(model.getBspFile().map(BspFile::getVersion).map(Object::toString).orElse(""));
		txtRevision.setText(model.getBspFile().map(BspFile::getRevision).map(Object::toString).orElse(""));
		txtCompressed.setText(model.getBspFile().map(BspFile::isCompressed).map(bool -> bool ? "Yes" : "No").orElse(""));
		txtEndianness.setText(model.getBspFile().map(bspFile -> bspFile.getByteOrder() == ByteOrder.LITTLE_ENDIAN ? "Little endian" : "Big endian").orElse(null));
		txtComment.setText(comment);
	}

	private void updateGame(BspInfoModel model) {
		String gameName = model.getBspFile()
				.map(bspFile -> SourceAppDB.getInstance().getName(bspFile.getAppId()).orElse("Unknown"))
				.orElse("");

		txtGameName.setText(gameName);
		txtAppId.setText(model.getBspFile().map(BspFile::getAppId).map(Object::toString).orElse(""));

		model.getBspFile().ifPresentOrElse(
				bspFile -> lblSteamLink.setURI("Steam store link", SourceAppDB.getSteamStoreURI(bspFile.getAppId())),
				() -> lblSteamLink.setURI("", (URI) null)
		);
	}

	private void updateChecksums(BspInfoModel model) {
		txtFileCrc.setText(model.getFileCrc().map(checksum -> String.format("%x", checksum)).orElse(""));
		txtMapCrc.setText(model.getMapCrc().map(checksum -> String.format("%x", checksum)).orElse(""));
	}

	private void updateCompileParameters(BspInfoModel model) {
		String vbsp = model.getCparams()
				.map(BspCompileParams::getVbspParams)
				.map(strings -> String.join(" ", strings))
				.orElse("");

		String vvis = model.getCparams()
				.map(params -> params.isVvisRun() ? String.join(" ", params.getVvisParams()) : "")
				.orElse("");

		String vrad = model.getCparams()
				.map(params -> params.isVradRun() ? String.join(" ", params.getVradParams()) : "")
				.orElse("");

		txtVbsp.setText(vbsp);
		txtVvis.setText(vvis);
		txtVrad.setText(vrad);
	}

	public static void main(String[] args) {
		GuiUtil.debugDisplay(() -> new GeneralPanel());
	}
}

package info.ata4.bspsrc.app.info.gui.panel;

import info.ata4.bspsrc.app.info.gui.models.BspInfoModel;
import info.ata4.bspsrc.app.util.GridBagConstraintsBuilder;
import info.ata4.bspsrc.app.util.GuiUtil;
import info.ata4.bspsrc.decompiler.modules.BspProtection;

import javax.swing.*;
import java.awt.*;

import static info.ata4.bspsrc.app.info.gui.Util.*;
import static info.ata4.bspsrc.app.util.GridBagConstraintsBuilder.Anchor.LINE_END;
import static info.ata4.bspsrc.app.util.GridBagConstraintsBuilder.Fill.BOTH;

public class ProtectionPanel extends JPanel {

	public final JCheckBox chkEntityFlag = createDisplayCheckbox();
	public final JCheckBox chkTextureFlag = createDisplayCheckbox();
	public final JCheckBox chkProtectorBrush = createDisplayCheckbox();
	public final JCheckBox chkEntityObfuscation = createDisplayCheckbox();
	public final JCheckBox chkNodrawTextureHack = createDisplayCheckbox();
	public final JCheckBox chkBspProtect = createDisplayCheckbox();

	public ProtectionPanel() {
		setLayout(new GridBagLayout());

		var builder = new GridBagConstraintsBuilder()
				.insets(PANEL_COMPONENT_INSETS)
				.weightX(1)
				.fill(BOTH);

		add(createVmexPanel(), builder.position(0, 0).build());
		add(createIidPanel(), builder.position(0, 1).build());
		add(createOtherPanel(), builder.position(0, 2).build());
	}

	private JPanel createVmexPanel() {
		var panel = new JPanel();
		panel.setBorder(createPanelBorder("VMEX"));
		panel.setLayout(new GridBagLayout());

		var builder = new GridBagConstraintsBuilder()
				.insets(PANEL_COMPONENT_INSETS);

		panel.add(Box.createGlue(), builder.position(0, 0).weightX(1).build());
		panel.add(new JLabel("Entity flag"), builder.position(1, 0).anchor(LINE_END).build());
		panel.add(chkEntityFlag, builder.position(2, 0).build());
		panel.add(new JLabel("Texture flag"), builder.position(1, 1).anchor(LINE_END).build());
		panel.add(chkTextureFlag, builder.position(2, 1).build());
		panel.add(new JLabel("Protector brush"), builder.position(1, 2).anchor(LINE_END).build());
		panel.add(chkProtectorBrush, builder.position(2, 2).build());

		return panel;
	}

	private JPanel createIidPanel() {
		var panel = new JPanel();
		panel.setBorder(createPanelBorder("IID"));
		panel.setLayout(new GridBagLayout());

		var builder = new GridBagConstraintsBuilder()
				.insets(PANEL_COMPONENT_INSETS)
				.anchor(LINE_END);

		panel.add(Box.createGlue(), builder.position(0, 0).weightX(1).build());
		panel.add(new JLabel("Entity obfuscation"), builder.position(1, 0).anchor(LINE_END).build());
		panel.add(chkEntityObfuscation, builder.position(2, 0).build());
		panel.add(new JLabel("Nodraw texture hack"), builder.position(1, 1).anchor(LINE_END).build());
		panel.add(chkNodrawTextureHack, builder.position(2, 1).build());

		return panel;
	}

	private JPanel createOtherPanel() {
		var panel = new JPanel();
		panel.setBorder(createPanelBorder("Other"));
		panel.setLayout(new GridBagLayout());

		var builder = new GridBagConstraintsBuilder()
				.insets(PANEL_COMPONENT_INSETS)
				.anchor(LINE_END);

		panel.add(Box.createGlue(), builder.position(0, 0).weightX(1).build());
		panel.add(new JLabel("BSPProtect"), builder.position(1, 0).anchor(LINE_END).build());
		panel.add(chkBspProtect, builder.position(2, 0).build());

		return panel;
	}

	public void update(BspInfoModel model) {
		updateVmex(model);
		updateIid(model);
		updateOther(model);
	}

	private void updateVmex(BspInfoModel model) {
		chkEntityFlag.setSelected(model.getProt().map(BspProtection::hasEntityFlag).orElse(false));
		chkTextureFlag.setSelected(model.getProt().map(BspProtection::hasTextureFlag).orElse(false));
		chkProtectorBrush.setSelected(model.getProt().map(BspProtection::hasBrushFlag).orElse(false));
	}

	private void updateIid(BspInfoModel model) {
		chkEntityObfuscation.setSelected(model.getProt().map(BspProtection::hasObfuscatedEntities).orElse(false));
		chkNodrawTextureHack.setSelected(model.getProt().map(BspProtection::hasModifiedTexinfo).orElse(false));
	}

	private void updateOther(BspInfoModel model) {
		chkBspProtect.setSelected(model.getProt().map(BspProtection::hasEncryptedEntities).orElse(false));
	}

	public static void main(String[] args) {
		GuiUtil.debugDisplay(() -> new ProtectionPanel());
	}
}


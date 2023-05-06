package info.ata4.bspsrc.app.info.gui.panel;

import info.ata4.bspsrc.app.info.gui.models.BspInfoModel;
import info.ata4.bspsrc.decompiler.modules.BspDependencies;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.Set;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

public class DependenciesPanel extends JTabbedPane {

	public final JTextArea txtMaterials = new JTextArea(5, 20);
	public final JTextArea txtSounds = new JTextArea(5, 20);
	public final JTextArea txtSoundScripts = new JTextArea(5, 20);
	public final JTextArea txtSoundscapes = new JTextArea(5, 20);
	public final JTextArea txtModels = new JTextArea(5, 20);
	public final JTextArea txtParticles = new JTextArea(5, 20);

	public DependenciesPanel() {
		var font = new Font("Monospaced", Font.PLAIN, 12);

		txtMaterials.setEditable(false);
		txtSounds.setEditable(false);
		txtSoundScripts.setEditable(false);
		txtSoundscapes.setEditable(false);
		txtModels.setEditable(false);
		txtParticles.setEditable(false);

		txtMaterials.setFont(font);
		txtSounds.setFont(font);
		txtSoundScripts.setFont(font);
		txtSoundscapes.setFont(font);
		txtModels.setFont(font);
		txtParticles.setFont(font);

		addTab("Materials", new JScrollPane(txtMaterials, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
		addTab("Sounds", new JScrollPane(txtSounds, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
		addTab("Sound scripts", new JScrollPane(txtSoundScripts, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
		addTab("Soundscapes", new JScrollPane(txtSoundscapes, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
		addTab("Models", new JScrollPane(txtModels, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
		addTab("Particles", new JScrollPane(txtParticles, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
	}

	public void update(BspInfoModel model) {
		txtMaterials.setText(optionalSetToString(model.getBspres().map(BspDependencies::getMaterials)));
		txtSounds.setText(optionalSetToString(model.getBspres().map(BspDependencies::getSoundFiles)));
		txtSoundScripts.setText(optionalSetToString(model.getBspres().map(BspDependencies::getSoundScripts)));
		txtSoundscapes.setText(optionalSetToString(model.getBspres().map(BspDependencies::getSoundscapes)));
		txtModels.setText(optionalSetToString(model.getBspres().map(BspDependencies::getModels)));
		txtParticles.setText(optionalSetToString(model.getBspres().map(BspDependencies::getParticles)));
	}

	private static String optionalSetToString(Optional<Set<String>> set) {
		return set
				.map(strings -> String.join("\n", strings))
				.orElse("");
	}
}

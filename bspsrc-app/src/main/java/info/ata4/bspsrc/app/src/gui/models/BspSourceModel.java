package info.ata4.bspsrc.app.src.gui.models;

import info.ata4.bspsrc.app.src.ObservableBspSourceConfig;
import info.ata4.bspsrc.decompiler.BspFileEntry;
import info.ata4.bspsrc.decompiler.BspSourceConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class BspSourceModel {

	private final ObservableBspSourceConfig config = new ObservableBspSourceConfig(new BspSourceConfig());
	private final Set<Consumer<DecompileTaskModel>> onDecompileTaskListener = new HashSet<>();

	public void addOnDecompileTask(Consumer<DecompileTaskModel> onDecompileTask) {
		onDecompileTaskListener.add(requireNonNull(onDecompileTask));
	}

	public ObservableBspSourceConfig getConfig() {
		return config;
	}

	public void setDefaults() {
		config.setConfig(new BspSourceConfig());
	}

	public void decompile(List<BspFileEntry> entries) {
		if (entries.isEmpty())
			return;

		var c = config.getCopy(); // copy config so no modifications can happen afterward

		var decompileTaskModel = new DecompileTaskModel(c, entries);
		onDecompileTaskListener.forEach(consumer -> consumer.accept(decompileTaskModel));
	}
}

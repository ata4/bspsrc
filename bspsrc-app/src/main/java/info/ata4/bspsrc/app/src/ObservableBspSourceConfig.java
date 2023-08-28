package info.ata4.bspsrc.app.src;

import info.ata4.bspsrc.decompiler.BspSourceConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Wrapper around a {@link BspSourceConfig} that allows observers
 * to get notified of changes made to it.
 */
public class ObservableBspSourceConfig {

	private final Set<Runnable> listeners = new HashSet<>();
	private BspSourceConfig config;

	public ObservableBspSourceConfig(BspSourceConfig config) {
		this.config = requireNonNull(config);
	}

	public void addListener(Runnable runnable) {
		listeners.add(runnable);
	}

	public void setConfig(BspSourceConfig config) {
		this.config = requireNonNull(config);
		listeners.forEach(Runnable::run);
	}

	public void updateConfig(Consumer<BspSourceConfig> consumer) {
		consumer.accept(config);
		listeners.forEach(Runnable::run);
	}

	public <T> T get(Function<BspSourceConfig, T> getter) {
		return getter.apply(config);
	}

	public BspSourceConfig getCopy() {
		return new BspSourceConfig(config);
	}
}

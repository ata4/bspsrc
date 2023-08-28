package info.ata4.bspsrc.app.src.gui.models;

import java.nio.file.Path;
import java.util.*;

public class FilesModel {

	private final List<Path> bspPaths = new ArrayList<>();
	private final Set<Listener> onChangeListeners = new HashSet<>();

	public List<Path> getBspPaths() {
		return Collections.unmodifiableList(bspPaths);
	}

	public void addListener(Listener listener) {
		onChangeListeners.add(listener);
	}

	public void addEntries(Collection<Path> bspPaths) {
		if (bspPaths.isEmpty())
			return;

		int minIndex = this.bspPaths.size();
		this.bspPaths.addAll(bspPaths);
		int maxIndex = this.bspPaths.size() - 1;

		onChangeListeners.forEach(listener -> listener.added(minIndex, maxIndex));
	}

	public void removeEntries(int[] selectedIndices) {
		Arrays.stream(selectedIndices)
				.boxed()
				.sorted(Comparator.reverseOrder())
				.forEachOrdered(i -> {
					bspPaths.remove((int) i);
					onChangeListeners.forEach(listener -> listener.removed(i, i));
				});
	}

	public void removeAllEntries() {
		int maxIndex = bspPaths.size() - 1;
		bspPaths.clear();

		onChangeListeners.forEach(listener -> listener.removed(0, maxIndex));
	}

	public interface Listener {
		void added(int minIndex, int maxIndex);
		void removed(int minIndex, int maxIndex);
	}
}

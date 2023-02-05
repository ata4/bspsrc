package info.ata4.bsplib.nmo;

import info.ata4.io.DataReader;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * <h3>A nmo anti objective used in nmo files.</h3>
 * <p>
 * Consists of:
 * <ul>
 *     <li>{@link #id} visgroup id</li>
 *     <li>{@link #name} visgroup name</li>
 *     <li>{@link #entities} a set of entity names, which belong to this objective</li>
 * </ul>
 */
public class NmoAntiObjective extends NmoNode {

	public Set<String> entities = new HashSet<>();

	public NmoAntiObjective(DataReader reader) throws IOException {
		super(reader);

		int entityCount = reader.readInt();
		for (int i = 0; i < entityCount; i++) {
			entities.add(reader.readStringNull());
		}
	}
}

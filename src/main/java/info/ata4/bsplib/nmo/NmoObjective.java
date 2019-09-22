package info.ata4.bsplib.nmo;

import info.ata4.io.DataReader;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * <h3>A nmo objective used in nmo files.</h3>
 * <p>
 * Consists of:
 * <ul>
 *     <li>{@link #id} visgroup id</li>
 *     <li>{@link #name} visgroup name</li>
 *     <li>{@link #comment}</li>
 *     <li>{@link #entityName} name of the nmrih_objective_boundary entity, every objective must have</li>
 *     <li>{@link #entities} a set of entity names, which also belong to this objective</li>
 *     <li>{@link #children} a set of ids linking to it child nodes(objectives)</li>
 * </ul>
 */
public class NmoObjective extends NmoNode {

	public String comment;
	public String entityName;

	public Set<String> entities = new HashSet<>();
	public Set<Integer> children = new HashSet<>();

	public NmoObjective(DataReader reader) throws IOException {
		super(reader);

		comment = reader.readStringNull();
		entityName = reader.readStringNull();

		int entityCount = reader.readInt();
		for (int i = 0; i < entityCount; i++) {
			entities.add(reader.readStringNull());
		}

		int childrenCount = reader.readInt();
		for (int i = 0; i < childrenCount; i++) {
			children.add(reader.readInt());
		}
	}
}

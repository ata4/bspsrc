package info.ata4.bspsrc.lib.io.lumpreader;

import info.ata4.bspsrc.lib.entity.Entity;
import info.ata4.bspsrc.lib.io.EntityInputStream;
import info.ata4.bspsrc.lib.lump.LumpType;
import info.ata4.io.buffer.ByteBufferInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lump reader for {@link LumpType#LUMP_ENTITIES}
 */
public class EntityLumpReader implements LumpReader<List<Entity>> {

	private final boolean allowEscSeq;

	public EntityLumpReader(boolean allowEscSeq) {
		this.allowEscSeq = allowEscSeq;
	}

	@Override
	public List<Entity> read(ByteBuffer buffer) throws IOException {
		List<Entity> entities = new ArrayList<>();

		try (InputStream in = new ByteBufferInputStream(buffer);
		     EntityInputStream entReader = new EntityInputStream(in)) {

			// allow escaped quotes for VTBM
			entReader.setAllowEscSeq(allowEscSeq);

			Entity ent;
			while ((ent = entReader.readEntity()) != null) {
				entities.add(ent);
			}
		}

		return entities;
	}

	@Override
	public List<Entity> defaultData() {
		return Collections.emptyList();
	}
}

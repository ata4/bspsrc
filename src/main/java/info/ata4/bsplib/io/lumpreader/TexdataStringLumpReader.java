package info.ata4.bsplib.io.lumpreader;

import info.ata4.bsplib.lump.LumpType;
import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Lump reader for {@link LumpType#LUMP_TEXDATA_STRING_DATA}
 */
public class TexdataStringLumpReader implements LumpReader<List<String>> {

	private final List<Integer> stringTableIndices;

	public TexdataStringLumpReader(List<Integer> stringTableIndices) {
		this.stringTableIndices = Objects.requireNonNull(stringTableIndices);
	}

	@Override
	public List<String> read(ByteBuffer buffer) throws IOException {
		DataReader dataReader = DataReaders.forByteBuffer(buffer);

		List<String> texnames = new ArrayList<>(stringTableIndices.size());
		for (int stringTableIndex : stringTableIndices) {
			dataReader.position(stringTableIndex);
			texnames.add(dataReader.readStringNull(Math.toIntExact(buffer.remaining())));
		}

		return texnames;
	}

	@Override
	public List<String> defaultData() {
		return Collections.emptyList();
	}
}

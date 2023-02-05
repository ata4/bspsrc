package info.ata4.bspsrc.lib.io.lumpreader;

import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for lump readers consisting of sequentially read chunks
 *
 * @param <T> the chunk type
 */
public abstract class ChunksLumpReader<T> implements LumpReader<List<T>> {

	@Override
	public List<T> read(ByteBuffer buffer) throws IOException {
		DataReader dataReader = DataReaders.forByteBuffer(buffer);

		List<T> chunks = new ArrayList<>();
		while (dataReader.hasRemaining()) {
			T chunk = readChunk(dataReader);
			chunks.add(chunk);
		}

		return chunks;
	}

	/**
	 * Reads and returns a single chunk
	 *
	 * @param reader {@link DataReader} to read data from
	 * @return the read chunk
	 *
	 * @throws IOException if an IO exception occurs
	 */
	protected abstract T readChunk(DataReader reader) throws IOException;

	@Override
	public List<T> defaultData() {
		return Collections.emptyList();
	}
}

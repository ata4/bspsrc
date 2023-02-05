package info.ata4.bsplib.io;

import info.ata4.bsplib.struct.DStruct;
import info.ata4.io.DataReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Static helper methods for common {@link DataReader} io reading code
 */
public class DataReaderUtil {

	/**
	 * Reads {@code count} amount of 'data chunks' into a list and returns it.
	 * A 'data chunk' is read by calling the passed {@code chunkReader}'s {@link ChunkReader#read(DataReader)}
	 * method.
	 *
	 * @param reader a {@link DataReader} object to read chunks from
	 * @param chunkReader a {@link ChunkReader<E>}, defining how to read an individual chunk
	 * @param count the amount of chunks, that should be read
	 * @param <E> the chunk type
	 * @return a list of {@code count} amount of read chunks
	 *
	 * @throws IOException if an IO exception occurs
	 */
	public static <E> List<E> readChunks(DataReader reader, ChunkReader<E> chunkReader, int count)
			throws IOException {

		List<E> chunks = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			E chunk = chunkReader.read(reader);
			chunks.add(chunk);
		}

		return chunks;
	}

	/**
	 * Reads data into {@link DStruct}.
	 * <p>
	 * This is just a helper method, for calling {@link DStruct#read(DataReader)} with the supplied
	 * {@link DataReader} and {@link DStruct}, but additionally throwing an {@link IOException},
	 * if the amount of read bytes mismatches {@link DStruct#getSize()}
	 *
	 * @param reader a {@link DataReader} object to read data into the specified {@link DStruct}
	 * @param dStruct a {@link DStruct} which data will be read by calling {@link DStruct#read(DataReader)}
	 * @param <T> the type of the {@link DStruct}
	 * @return the {@link DStruct} passed from the arguments
	 *
	 * @throws IOException if an IO exception occurs
	 */
	public static <T extends DStruct> T readDStruct(DataReader reader, T dStruct)
			throws IOException {

		long position = reader.position();

		dStruct.read(reader);

		if (reader.position() - position != dStruct.getSize()) {
			throw new IOException(String.format(
					"DStruct '%s' bytes read: %d; expected: %d",
					dStruct.getClass().getSimpleName(),
					position,
					dStruct.getSize()
			));
		}

		return dStruct;
	}

	@FunctionalInterface
	public interface ChunkReader<T> {
		T read(DataReader reader) throws IOException;
	}
}

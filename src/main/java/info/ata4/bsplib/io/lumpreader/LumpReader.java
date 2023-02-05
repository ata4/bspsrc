package info.ata4.bsplib.io.lumpreader;

import info.ata4.io.DataReader;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Defines an interface for reading the content of lump to usable data instances.
 * <p>
 * The interface serves the purpose of extracting common reading logic between
 * different lumps and is used in {@link info.ata4.bsplib.BspFileReader}.
 *
 * @param <T> the type this lumpreader returns by reading the lumps content
 */
public interface LumpReader<T> {

	/**
	 * Reads data from the passed {@link ByteBuffer} and returns an instance of type T from it.
	 *
	 * @param buffer a {@link ByteBuffer} to read data from
	 * @return an instance of type {@code T} created by reading and parsing data from the passed {@link DataReader}
	 *
	 * @throws IOException if an IO exception occurs
	 */
	T read(ByteBuffer buffer) throws IOException;

	/**
	 * A sensible default value for this lump reader.
	 * This may be used in cases where reading the lump content throws an exception,
	 * and we still want to try to proceed with
	 *
	 * @return a sensible default value for this lump reader.
	 */
	T defaultData();
}

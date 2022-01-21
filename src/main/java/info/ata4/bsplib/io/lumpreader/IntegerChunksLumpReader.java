package info.ata4.bsplib.io.lumpreader;

import info.ata4.io.DataReader;

import java.io.IOException;

/**
 * Lump reader for lumps consisting of sequentially read integers
 */
public class IntegerChunksLumpReader extends ChunksLumpReader<Integer> {

	@Override
	protected Integer readChunk(DataReader reader) throws IOException {
		return reader.readInt();
	}
}

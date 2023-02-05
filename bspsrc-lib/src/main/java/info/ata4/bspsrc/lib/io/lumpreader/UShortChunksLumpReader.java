package info.ata4.bspsrc.lib.io.lumpreader;

import info.ata4.io.DataReader;

import java.io.IOException;

/**
 * Lump reader for lumps consisting of sequentially read unsigned shorts
 */
public class UShortChunksLumpReader extends ChunksLumpReader<Integer> {

	@Override
	protected Integer readChunk(DataReader reader) throws IOException {
		return reader.readUnsignedShort();
	}
}

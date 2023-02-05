package info.ata4.bspsrc.lib.io.lumpreader;

import info.ata4.bspsrc.lib.io.DataReaderUtil;
import info.ata4.bspsrc.lib.struct.DStruct;
import info.ata4.io.DataReader;

import java.io.IOException;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Lump reader for lumps consisting of sequentially read {@link DStruct}s
 *
 * @param <T> the {@link DStruct} type
 */
public class DStructChunksLumpReader<T extends DStruct> extends ChunksLumpReader<T> {

	private final Supplier<? extends T> dStructSupplier;

	public DStructChunksLumpReader(Supplier<? extends T> dStructSupplier) {
		this.dStructSupplier = requireNonNull(dStructSupplier);
	}

	@Override
	protected T readChunk(DataReader reader) throws IOException {
		return DataReaderUtil.readDStruct(reader, dStructSupplier.get());
	}
}

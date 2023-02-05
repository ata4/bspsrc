package info.ata4.bspsrc.lib.io.lumpreader;

import info.ata4.bspsrc.lib.io.DataReaderUtil;
import info.ata4.bspsrc.lib.lump.LumpType;
import info.ata4.bspsrc.lib.struct.DOccluderData;
import info.ata4.bspsrc.lib.struct.DOccluderPolyData;
import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import info.ata4.log.LogUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static info.ata4.bspsrc.common.util.JavaUtil.listCopyOf;
import static java.util.Objects.requireNonNull;

/**
 * Lump reader for {@link LumpType#LUMP_OCCLUSION}
 *
 * @param <T> the {@link DOccluderData} type
 */
public class OcclusionLumpReader<T extends DOccluderData>
		implements LumpReader<OcclusionLumpReader.OcclusionData<T>> {

	private static final Logger L = LogUtils.getLogger();

	private final Supplier<? extends T> dOccluderDataSupplier;

	public OcclusionLumpReader(Supplier<? extends T> dOccluderDataSupplier) {
		this.dOccluderDataSupplier = requireNonNull(dOccluderDataSupplier);
	}

	@Override
	public OcclusionData<T> read(ByteBuffer buffer) throws IOException {
		DataReader dataReader = DataReaders.forByteBuffer(buffer);

		int dOcculderDataCount = dataReader.readInt();
		List<T> dOccluderData = DataReaderUtil.readChunks(
				dataReader,
				r -> DataReaderUtil.readDStruct(r, dOccluderDataSupplier.get()),
				dOcculderDataCount
		);

		int dOccluderPolyCount = dataReader.readInt();
		List<DOccluderPolyData> dOccluderPolyData = DataReaderUtil.readChunks(
				dataReader,
				r -> DataReaderUtil.readDStruct(r, new DOccluderPolyData()),
				dOccluderPolyCount
		);

		int vertexCount = dataReader.readInt();
		List<Integer> vertexIndices = DataReaderUtil.readChunks(
				dataReader,
				DataReader::readInt,
				vertexCount
		);

		if (dataReader.hasRemaining()) {
			L.warning(String.format("%d bytes remaining after reading", dataReader.remaining()));
		}

		return new OcclusionData<>(dOccluderData, dOccluderPolyData, vertexIndices);
	}

	@Override
	public OcclusionData<T> defaultData() {
		return new OcclusionData<>();
	}

	public static class OcclusionData<T extends DOccluderData> {

		public final List<T> dOccluderData;
		public final List<DOccluderPolyData> dOccluderPolyData;
		public final List<Integer> vertexIndices;

		public OcclusionData() {
			this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
		}

		public OcclusionData(
				List<T> dOccluderData,
				List<DOccluderPolyData> dOccluderPolyData,
				List<Integer> vertexIndices
		) {
			this.dOccluderData = listCopyOf(dOccluderData);
			this.dOccluderPolyData = listCopyOf(dOccluderPolyData);
			this.vertexIndices = listCopyOf(vertexIndices);
		}
	}
}

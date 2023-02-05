package info.ata4.bsplib.io.lumpreader;

import info.ata4.bsplib.io.DataReaderUtil;
import info.ata4.bsplib.lump.LumpType;
import info.ata4.bsplib.struct.*;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import info.ata4.log.LogUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static info.ata4.bsplib.app.SourceAppId.*;
import static info.ata4.io.Seekable.Origin.CURRENT;
import static info.ata4.util.JavaUtil.listCopyOf;
import static java.util.Objects.requireNonNull;

/**
 * Lump reader for {@link LumpType#LUMP_AREAPORTALS}
 */
public class StaticPropLumpReader implements LumpReader<StaticPropLumpReader.StaticPropData> {

	private static final Logger L = LogUtils.getLogger();

	private static final int PAD_SIZE = 128;

	private final int sprpVersion;
	private final int appId;

	public StaticPropLumpReader(int sprpVersion, int appId) {
		this.sprpVersion = sprpVersion;
		this.appId = appId;
	}

	@Override
	public StaticPropData read(ByteBuffer buffer) throws IOException {
		DataReader dataReader = DataReaders.forByteBuffer(buffer);

		List<String> staticPropDict = readNames(dataReader);

		// model path strings in Zeno Clash
		if (appId == ZENO_CLASH) {
			int psextra = dataReader.readInt();
			dataReader.seek((long) psextra * PAD_SIZE, CURRENT);
		}

		List<Integer> staticPropLeafs = readLeafs(dataReader);

		Map<Integer, Vector3f> scaling;
		if (appId == VINDICTUS && sprpVersion > 5) {
			scaling = readVindictusScaling(dataReader);
		} else {
			scaling = Collections.emptyMap();
		}

		List<? extends DStaticProp> staticProps = readStaticProps(dataReader);

		for (int i = 0; i < staticProps.size(); i++) {
			DStaticProp sp = staticProps.get(i);
			if (scaling.containsKey(i) && sp instanceof DStaticPropVinScaling) {
				((DStaticPropVinScaling) sp).setScaling(scaling.get(i));
			}
		}

		if (dataReader.hasRemaining()) {
			L.warning(String.format("%d bytes remaining after reading", dataReader.remaining()));
		}

		return new StaticPropData(staticPropDict, staticPropLeafs, staticProps);
	}

	private List<String> readNames(DataReader reader) throws IOException {
		int nameCount = reader.readInt();
		return DataReaderUtil.readChunks(
				reader,
				r -> r.readStringFixed(PAD_SIZE),
				nameCount
		);
	}

	private List<Integer> readLeafs(DataReader reader) throws IOException {
		int leaveCount = reader.readInt();
		return DataReaderUtil.readChunks(
				reader,
				DataReader::readUnsignedShort,
				leaveCount
		);
	}

	private Map<Integer, Vector3f> readVindictusScaling(DataReader reader) throws IOException {
		int scalingCount = reader.readInt();

		Map<Integer, Vector3f> scaling = new HashMap<>();
		for (int i = 0; i < scalingCount; i++) {
			scaling.put(
					reader.readInt(),
					Vector3f.read(reader)
			);
		}

		return scaling;
	}

	private List<? extends DStaticProp> readStaticProps(DataReader reader) throws IOException {
		final int propStaticCount = reader.readInt();

		// don't try to read static props if there are none
		if (propStaticCount == 0) {
			return Collections.emptyList();
		}

		// calculate static prop struct size
		final int propStaticSize = (int) (reader.remaining() / propStaticCount);

		Supplier<? extends DStaticProp> dStaticPropSupplier = staticPropStructDescriptors.stream()
				.filter(descriptor -> descriptor.version == sprpVersion)
				.filter(descriptor -> descriptor.appId == -1 || descriptor.appId == appId)
				.filter(descriptor -> descriptor.size == propStaticSize)
				.max(Comparator.comparing(descriptor -> descriptor.appId != -1)) // favor game specific staticprop structs
				.<Supplier<? extends DStaticProp>>map(descriptor -> {
					L.info(String.format(
							"Using '%s' for sprp version %d",
							descriptor.structSupplier.get().getClass().getSimpleName(),
							sprpVersion
					));

					return descriptor.structSupplier;
				})
				.orElseGet(() -> {
					L.warning(String.format("Couldn't find static prop struct for appId %d, version %d, size %d",
							appId, sprpVersion, propStaticSize));

					L.warning("Falling back to static prop v4");
					return () -> new DStaticPropV4Padded(propStaticSize);
				});

		return DataReaderUtil.readChunks(
				reader,
				r -> DataReaderUtil.readDStruct(r, dStaticPropSupplier.get()),
				propStaticCount
		);
	}

	private static class DStaticPropV4Padded extends DStaticPropV4 {

		private final int propStaticSize;

		public DStaticPropV4Padded(int propStaticSize) {
			this.propStaticSize = propStaticSize;
		}

		@Override
		public int getSize() {
			return propStaticSize;
		}

		@Override
		public void read(DataReader in) throws IOException {
			super.read(in);
			in.seek(propStaticSize - super.getSize(), CURRENT);
		}
	}

	private static class StaticPropStructDescriptor {

		public final Supplier<? extends DStaticProp> structSupplier;
		public final int version;
		public final int appId;

		public final int size;

		private StaticPropStructDescriptor(Supplier<? extends DStaticProp> structSupplier, int version) {
			this(structSupplier, version, -1);
		}

		private StaticPropStructDescriptor(Supplier<? extends DStaticProp> structSupplier, int version, int appId) {
			this.structSupplier = requireNonNull(structSupplier);
			this.version = version;
			this.appId = appId;

			this.size = structSupplier.get().getSize();
		}
	}

	private final List<StaticPropStructDescriptor> staticPropStructDescriptors = Arrays.asList(
			new StaticPropStructDescriptor(DStaticPropV4::new, 4),
			new StaticPropStructDescriptor(DStaticPropV5::new, 5),
			new StaticPropStructDescriptor(DStaticPropV6::new, 6),
			new StaticPropStructDescriptor(DStaticPropV8::new, 8),
			new StaticPropStructDescriptor(DStaticPropV9::new, 9),
			new StaticPropStructDescriptor(DStaticPropV10::new, 10),
			new StaticPropStructDescriptor(DStaticPropV11lite::new, 11),
			new StaticPropStructDescriptor(DStaticPropV11::new, 11),
			new StaticPropStructDescriptor(DStaticPropV5Ship::new, 5, THE_SHIP),
			new StaticPropStructDescriptor(DStaticPropV6BGT::new, 6, BLOODY_GOOD_TIME),
			new StaticPropStructDescriptor(DStaticPropV7ZC::new, 7, ZENO_CLASH),
			new StaticPropStructDescriptor(DStaticPropV6DM::new, 6, DARK_MESSIAH),
			new StaticPropStructDescriptor(DStaticPropV9DE::new, 9, DEAR_ESTHER),
			// vindictus:
			// newer maps report v6 even though their structure is identical to DStaticPropV5, probably because
			// they additional have scaling array saved before the static prop array
			// Consequently, their v7 seems to be a standard DStaticPropV6 with an additional scaling array
			new StaticPropStructDescriptor(DStaticPropV6VIN::new, 6, VINDICTUS),
			new StaticPropStructDescriptor(DStaticPropV7VIN::new, 7, VINDICTUS),
			new StaticPropStructDescriptor(DStaticPropV7L4D::new, 7, LEFT_4_DEAD),
			// there's been a short period where TF2 used v7, which later became v10 in all Source 2013 game
			new StaticPropStructDescriptor(DStaticPropV10::new, 7, TEAM_FORTRESS_2),
			new StaticPropStructDescriptor(DStaticPropV10CSGO::new, 10, COUNTER_STRIKE_GO),
			new StaticPropStructDescriptor(DStaticPropV11CSGO::new, 11, COUNTER_STRIKE_GO),
			// Insurgency is based of the csgo engine branch, so we can use DStaticPropV10CSGO
			new StaticPropStructDescriptor(DStaticPropV10CSGO::new, 10, INSURGENCY)
	);


	@Override
	public StaticPropData defaultData() {
		return new StaticPropData();
	}

	public static class StaticPropData {

		public final List<String> names;
		public final List<Integer> leafs;
		public final List<? extends DStaticProp> props;

		public StaticPropData() {
			this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
		}

		public StaticPropData(
				List<String> names,
				List<Integer> leafs,
				List<? extends DStaticProp> props
		) {
			this.names = listCopyOf(names);
			this.leafs = listCopyOf(leafs);
			this.props = listCopyOf(props);
		}
	}
}

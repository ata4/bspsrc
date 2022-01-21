package info.ata4.bsplib.io.lumpreader;

import info.ata4.bsplib.io.DataReaderUtil;
import info.ata4.bsplib.lump.LumpType;
import info.ata4.bsplib.struct.*;
import info.ata4.bsplib.vector.Vector3f;
import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import info.ata4.log.LogUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static info.ata4.bsplib.app.SourceAppID.*;
import static info.ata4.io.Seekable.Origin.CURRENT;
import static info.ata4.util.JavaUtil.listCopyOf;

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

		Supplier<? extends DStaticProp> dStaticPropSupplier = null;

		// special cases where derivative lump structures are used
		switch (appId) {
			case THE_SHIP:
				if (propStaticSize == 188) {
					dStaticPropSupplier = DStaticPropV5Ship::new;
				}
				break;

			case BLOODY_GOOD_TIME:
				if (propStaticSize == 192) {
					dStaticPropSupplier = DStaticPropV6BGT::new;
				}
				break;

			case ZENO_CLASH:
				if (propStaticSize == 68) {
					dStaticPropSupplier = DStaticPropV7ZC::new;
				}
				break;

			case DARK_MESSIAH:
				if (propStaticSize == 136) {
					dStaticPropSupplier = DStaticPropV6DM::new;
				}
				break;

			case DEAR_ESTHER:
				if (propStaticSize == 76) {
					dStaticPropSupplier = DStaticPropV9DE::new;
				}
				break;

			case VINDICTUS:
				// newer maps report v6 even though their structure is identical to DStaticPropV5, probably because
				// they additional have scaling array saved before the static prop array
				// Consequently, their v7 seems to be a standard DStaticPropV6 with an additional scaling array
				if (sprpVersion == 6 && propStaticSize == 60) {
					dStaticPropSupplier = DStaticPropV6VIN::new;
				} else if (sprpVersion == 7 && propStaticSize == 64) {
					dStaticPropSupplier = DStaticPropV7VIN::new;
				}
				break;

			case LEFT_4_DEAD:
				// old L4D maps use v7 that is incompatible to the newer
				// Source 2013 v7
				if (sprpVersion == 7 && propStaticSize == 68) {
					dStaticPropSupplier = DStaticPropV7L4D::new;
				}
				break;

			case TEAM_FORTRESS_2:
				// there's been a short period where TF2 used v7, which later
				// became v10 in all Source 2013 game
				if (sprpVersion == 7 && propStaticSize == 72) {
					dStaticPropSupplier = DStaticPropV10::new;
				}
				break;

			case COUNTER_STRIKE_GO:
				//  (custom v10 for CS:GO, not compatible with Source 2013 v10)
				//  CS:GO now uses v11  since the addition of uniform prop scaling
				if (sprpVersion == 10) {
					dStaticPropSupplier = DStaticPropV10CSGO::new;
				} else if (sprpVersion == 11) {
					dStaticPropSupplier = DStaticPropV11CSGO::new;
				}
				break;

			case BLACK_MESA:
				// different structures used by Black Mesa
				if (sprpVersion == 10 && propStaticSize == 72) {
					dStaticPropSupplier = DStaticPropV10::new;
				} else if (sprpVersion == 11) {
					if (propStaticSize == 76) {
						dStaticPropSupplier = DStaticPropV11lite::new;
					} else if (propStaticSize == 80) {
						dStaticPropSupplier = DStaticPropV11::new;
					}
				}
				break;

			case INSURGENCY:
				// Insurgency is based of the csgo engine branch so we can use DStaticPropV10CSGO
				if (sprpVersion == 10 && propStaticSize == 76) {
					dStaticPropSupplier = DStaticPropV10CSGO::new;
				}
				break;

			default:
				// check for "lite" version of V11 struct in case it applies
				// to a game other than BM (or BM wasn't detected/selected)
				if (sprpVersion == 11 && propStaticSize == 76) {
					dStaticPropSupplier = DStaticPropV11lite::new;
				}
				break;
		}

		// get structure class for the static prop lump version if it's not
		// a special case
		if (dStaticPropSupplier == null) {
			try {
				String className = DStaticProp.class.getName();
				Class<? extends DStaticProp> dStaticPropClass =
						(Class<? extends DStaticProp>) Class.forName(className + "V" + sprpVersion);
				dStaticPropSupplier = () -> {
					try {
						return dStaticPropClass.getDeclaredConstructor().newInstance();
					} catch (InstantiationException | IllegalAccessException | InvocationTargetException
							| NoSuchMethodException e) {
						throw new RuntimeException(e);
					}
				};
			} catch (ClassNotFoundException ex) {
				L.log(Level.WARNING, "Couldn't find static prop struct for version {0}", sprpVersion);
			}
		}

		// check if the size is correct
		if (dStaticPropSupplier != null) {
			DStaticProp dStaticProp = dStaticPropSupplier.get();
			int propStaticSizeActual = dStaticProp.getSize();
			if (propStaticSizeActual != propStaticSize) {
				L.log(Level.WARNING, "Static prop struct size mismatch: expected {0}, got {1} (using {2})",
						new Object[]{propStaticSize, propStaticSizeActual, dStaticProp.getClass().getSimpleName()});
				dStaticPropSupplier = null;
			}
		}

		// if the correct class is still unknown at this point, fall back to
		// a very basic version that should hopefully work in most situations
		// (note: this will not work well if the struct is based on the V10
		// struct from the Source 2013 or the TF2 Source engine branches,
		// in which case the flags attribute will contain garbage data)
		if (dStaticPropSupplier == null) {
			L.log(Level.WARNING, "Falling back to static prop v4");

			dStaticPropSupplier = () -> new DStaticPropV4() {
				@Override
				public int getSize() {
					return propStaticSize;
				}

				@Override
				public void read(DataReader in) throws IOException {
					super.read(in);
					in.seek(propStaticSize - super.getSize(), CURRENT);
				}
			};
		}

		Supplier<? extends DStaticProp> finalDStaticPropSupplier = dStaticPropSupplier;
		return DataReaderUtil.readChunks(
				reader,
				r -> DataReaderUtil.readDStruct(r, finalDStaticPropSupplier.get()),
				propStaticCount
		);
	}

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

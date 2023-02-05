package info.ata4.bsplib.io.lumpreader;

import info.ata4.bsplib.lump.LumpType;
import info.ata4.bsplib.struct.LevelFlag;
import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import info.ata4.util.EnumConverter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;

/**
 * Lump reader for {@link LumpType#LUMP_MAP_FLAGS}
 */
public class MapFlagsLumpReader implements LumpReader<Set<LevelFlag>> {

	@Override
	public Set<LevelFlag> read(ByteBuffer buffer) throws IOException {
		DataReader dataReader = DataReaders.forByteBuffer(buffer);
		int bitFlag = dataReader.readInt();
		return EnumConverter.fromInteger(LevelFlag.class, bitFlag);
	}

	@Override
	public Set<LevelFlag> defaultData() {
		return Collections.emptySet();
	}
}

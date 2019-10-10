package info.ata4.bsplib.nmo;

import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import info.ata4.io.DataWriter;
import info.ata4.io.DataWriters;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class for reading/storing .nmo files.
 * <p>
 * Nmo files are used in the game 'No More Room in Hell' for storing information for their 'Objectives' game mode.
 * <p>
 * Specifically, they store a Set of 'Objectives'/ 'AntiObjectives'/ 'Extractions' referencing specific entities in the bsp.
 * @see <a href="https://wiki.nomoreroominhell.com/Objectives_Setup">https://wiki.nomoreroominhell.com/Objectives_Setup"</a>
 */
public class NmoFile {

	// logger
	private static final Logger L = LogUtils.getLogger();

	private final byte SIGNATURE = 118; //First byte in every file. Probably a magic constant to identify nmo files
	private final int VERSION = 1; //Second to fifth bytes. Probably nmo file version

	public final List<NmoObjective> nodes = new ArrayList<>();
	public final List<NmoAntiObjective> antiNodes = new ArrayList<>();
	public final List<NmoExtraction> extractions = new ArrayList<>();

	/**
	 * Load information from file into this object
	 *
	 * @param file A {@link Path} pointing to the .nmo file
	 * @param memMapping true if the map should be loaded as a memory-mapped file
	 * @throws IOException if the file can't be opened or read
	 * @throws NmoException if the file denoted by {@code file} is not an nmo file or is using a different version
	 */
	public void load(Path file, boolean memMapping) throws IOException, NmoException {
		L.info("Loading nmo file: " + file);

		DataReader dataReader = DataReaders.forByteBuffer(createBuffer(file, memMapping).order(ByteOrder.LITTLE_ENDIAN));

		byte signature = dataReader.readByte();
		int version = dataReader.readInt();

		if (signature != SIGNATURE)
			throw new NmoException("Invalid nmo file. Expected file signature '" + signature + "', got '" + signature + "'");
		if (version != VERSION)
			throw new NmoException("Unsupported nmo file version '" + version + "'");

		int nodeCount = dataReader.readInt();
		int antiNodeCount = dataReader.readInt();
		int extractionCount = dataReader.readInt();

		for (int i = 0; i < nodeCount; i++) {
			nodes.add(new NmoObjective(dataReader));
		}
		for (int i = 0; i < antiNodeCount; i++) {
			antiNodes.add(new NmoAntiObjective(dataReader));
		}
		for (int i = 0; i < extractionCount; i++) {
			extractions.add(new NmoExtraction(dataReader));
		}
	}

	/**
	 * Writes the nmo information as a nmos file.
	 *
	 * @see <a href="https://wiki.nomoreroominhell.com/Objectives_Setup">https://wiki.nomoreroominhell.com/Objectives_Setup"</a>
	 * @param path A {@link Path} denoting where to write the nmos file
	 * @throws IOException if the file can't be written
	 */
	public void writeAsNmos(Path path) throws IOException {
		L.info("Writing nmos file: " + path);

		try (DataWriter fileWriter = DataWriters.forFile(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			fileWriter.order(ByteOrder.LITTLE_ENDIAN);

			fileWriter.writeByte(SIGNATURE);
			fileWriter.writeInt(VERSION);

			fileWriter.writeInt(nodes.size() + extractions.size());

			int xCoordinate = 0;
			int spacing = 75;

			for (NmoObjective nmoObjective : nodes) {
				fileWriter.writeByte((byte) 1);
				fileWriter.writeInt(nmoObjective.id);
				fileWriter.writeInt(xCoordinate);
				fileWriter.writeInt(0);
				fileWriter.writeStringPrefixed(nmoObjective.comment, Byte.TYPE, StandardCharsets.UTF_8);

				fileWriter.writeInt(nmoObjective.children.size());
				for (Integer child : nmoObjective.children) {
					fileWriter.writeInt(child);
				}

				xCoordinate += spacing;
			}

			for (NmoExtraction extraction : extractions) {
				fileWriter.writeByte((byte) 0);
				fileWriter.writeInt(extraction.id);
				fileWriter.writeInt(xCoordinate);
				fileWriter.writeInt(0);
				fileWriter.writeInt(0); // unknown

				xCoordinate += spacing;
			}
		}
	}

	/**
	 * Creates a byte buffer for the Nmo file
	 *
	 * @param memMapping true if the map should be loaded as a memory-mapped file
	 * @throws IOException if the buffer couldn't be created
	 */
	private static ByteBuffer createBuffer(Path file, boolean memMapping) throws IOException {
		if (memMapping)
			return ByteBufferUtils.openReadOnly(file);
		else
			return ByteBufferUtils.load(file);
	}
}

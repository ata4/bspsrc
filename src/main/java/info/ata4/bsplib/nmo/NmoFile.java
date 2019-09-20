package info.ata4.bsplib.nmo;

import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

		//Really bad way to handle things, but i had no other idea how to write in little endian without a ByteBuffer
		ByteBuffer buffer = ByteBufferUtils.allocate(calculateNmosFileSize())
				.order(ByteOrder.LITTLE_ENDIAN);

		buffer.put(SIGNATURE);
		buffer.putInt(VERSION);

		buffer.putInt(nodes.size() + extractions.size());

		int xCoordinate = 0;
		int spacing = 75;

		for (NmoObjective nmoObjective : nodes) {
			buffer.put((byte) 1);
			buffer.putInt(nmoObjective.id);
			buffer.putInt(xCoordinate);
			buffer.putInt(0);
			buffer.put((byte) nmoObjective.comment.length());
			buffer.put(nmoObjective.comment.getBytes(StandardCharsets.UTF_8));

			buffer.putInt(nmoObjective.children.size());
			nmoObjective.children.forEach(buffer::putInt);

			xCoordinate += spacing;
		}

		for (NmoExtraction extraction : extractions) {
			buffer.put((byte) 0);
			buffer.putInt(extraction.id);
			buffer.putInt(xCoordinate);
			buffer.putInt(0);
			buffer.putInt(0); // unknown

			xCoordinate += spacing;
		}

		Files.write(path, buffer.array());
	}

	/**
	 * Method for calculating the total size in bytes a nmos file would use.
	 * <p>
	 * This is needed because i didn't know a nice way to write in little endian to a file without first writing to a {@link ByteBuffer}
	 *
	 * @return the total byte size, a nmos file would use
	 */
	private int calculateNmosFileSize() {

		int headerBytes = 9; // 1byte Signature + 4bytes version + 4bytes nodeCount

		int objectivesBytes = nodes.stream()
				.mapToInt(nmoObjective ->
						1 + // byte prefix
						4 + // 4byte id
						4 + // 4byte x
						4 + // 4byte y
						1 + // 1byte string length
						nmoObjective.comment.length() + // xbytes string
						4 + // 4byte children count
						nmoObjective.children.size() * 4 // childrenCount*4bytes childrens
				)
				.sum();

		int extractionsBytes = extractions.stream()
				.mapToInt(extraction ->
						1 + // byte prefix
						4 + // 4bytes id
						4 + // 4bytes x
						4 + // 4bytes y
						4 // 4bytes unknown
				)
				.sum();

		return headerBytes + objectivesBytes + extractionsBytes;
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

package info.ata4.bspsrc.lib.struct;

import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import info.ata4.io.DataWriter;
import info.ata4.io.DataWriters;
import info.ata4.io.channel.NullSeekableByteChannel;
import org.junit.jupiter.api.*;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;


@DisplayName("Test DStructs")
public class DynamicDStructTests {

	private static SortedSet<Class<? extends DStruct>> dStructClasses;

	@BeforeAll
	static void gatherDStructs() {
		Reflections reflections = new Reflections(DStruct.class);
		dStructClasses = reflections.getSubTypesOf(DStruct.class).stream()
				.filter(c -> !c.isInterface() && !Modifier.isAbstract(c.getModifiers()))
				.collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Class::toString))));
	}

	@DisplayName("Test DStruct integrity")
	@TestFactory
	Stream<DynamicNode> integrityTests() {
		return dStructClasses.stream()
				.map(dStructClass -> dynamicContainer(dStructClass.toString(), Stream.of(
						dynamicTest("Instantiates with Reflection", () -> dStructClass.getDeclaredConstructor().newInstance()),
						dynamicContainer("Matches size", Stream.of(
								dynamicTest("Read bytes matches size", () -> testReadBytesMatchesSize(dStructClass)),
								dynamicTest("Written bytes matches size", () -> testWrittenBytesMatchesSize(dStructClass))
						))
				)));
	}

	private void testReadBytesMatchesSize(Class<? extends DStruct> dStructClass) throws IOException {
		DataReader dataReader = DataReaders.forSeekableByteChannel(new NullSeekableByteChannel());

		long oldPosition = dataReader.position();

		DStruct dStruct = instantiateDStruct(dStructClass);
		try {
			dStruct.read(dataReader);
		} catch (UnsupportedOperationException e) {
			Assumptions.abort("Reading is not implemented");
		}
		assertEquals(dStruct.getSize(), dataReader.position() - oldPosition, "Read bytes unequal to struct size");
	}

	private void testWrittenBytesMatchesSize(Class<? extends DStruct> dStructClass) throws IOException {
		DataReader dataReader = DataReaders.forSeekableByteChannel(new NullSeekableByteChannel());
		DataWriter dataWriter = DataWriters.forSeekableByteChannel(new NullSeekableByteChannel());

		long oldPosition = dataWriter.position();

		DStruct dStruct = instantiateDStruct(dStructClass);
		try {
			dStruct.read(dataReader);
		} catch (Exception e) {
			Assumptions.assumeTrue(false, "Can't read data to DStruct");
		}
		try {
			dStruct.write(dataWriter);
		} catch (UnsupportedOperationException e) {
			Assumptions.abort("Writing is not implemented");
		}
		assertEquals(dStruct.getSize(), dataWriter.position() - oldPosition, "Written bytes unequal to struct size");
	}

	private DStruct instantiateDStruct(Class<? extends DStruct> c) {
		try {
			return c.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			Assumptions.assumeTrue(false, "DStruct not instantiable");
			return null; // not reachable
		}
	}
}

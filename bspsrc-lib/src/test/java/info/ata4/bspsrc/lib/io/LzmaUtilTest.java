package info.ata4.bspsrc.lib.io;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LzmaUtilTest {

    @ParameterizedTest
    @MethodSource("compressionCases")
    void compressionRoundTripPreservesData(int size, ByteOrder byteOrder) throws IOException {
        byte[] expected = new byte[size];
        new Random(0).nextBytes(expected);

        ByteBuffer input = ByteBuffer.allocate(size + 8).order(byteOrder);
        input.put(expected).flip();
        int position = Math.min(size, 2);
        input.position(position);

        ByteBuffer compressed = LzmaUtil.compress(input);
        assertTrue(LzmaUtil.isCompressed(compressed));
        assertEquals(byteOrder, compressed.order());
        assertEquals(position, input.position());
        assertEquals(size, input.limit());

        ByteBuffer header = compressed.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(size, header.getInt(4));
        assertEquals(compressed.remaining() - LzmaUtil.HEADER_SIZE, header.getInt(8));

        ByteBuffer uncompressed = LzmaUtil.uncompress(compressed);
        assertEquals(byteOrder, uncompressed.order());
        byte[] actual = new byte[uncompressed.remaining()];
        uncompressed.get(actual);
        assertArrayEquals(expected, actual);
    }

    private static Stream<Arguments> compressionCases() {
        return Stream.of(0, 1, 16, 4096, 65536)
                .flatMap(size -> Stream.of(ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN)
                        .map(order -> Arguments.of(size, order)));
    }
}

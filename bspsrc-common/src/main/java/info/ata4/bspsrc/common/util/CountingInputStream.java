package info.ata4.bspsrc.common.util;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

public class CountingInputStream extends InputStream
{
	private final InputStream delegate;
	private long bytesRead;

	public CountingInputStream(InputStream delegate) {
		this.delegate = requireNonNull(delegate);
	}

	@Override
	public int read() throws IOException {
		int read = delegate.read();
		if (read > 0)
			bytesRead += read;

		return read;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int read = delegate.read();
		if (read > 0)
			bytesRead += read;

		return read;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int read = delegate.read();
		if (read > 0)
			bytesRead += read;

		return read;
	}

	public long getBytesRead() {
		return bytesRead;
	}
}

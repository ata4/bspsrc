/*
 ** 2011 August 21
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.io;

import java.io.DataInput;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * DataInput wrapper for ByteBuffer
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ByteBufferDataInput extends ByteBufferData implements DataInput {
    
    public ByteBufferDataInput(ByteBuffer buf) {
        super(buf);
    }

    public void readFully(byte[] b) throws IOException {
        try {
            buf.get(b);
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        try {
            buf.get(b, off, len);
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    public int skipBytes(int n) throws IOException {
        n = Math.min(n, buf.remaining());
        buf.position(buf.position() + n);
        return n;
    }

    public boolean readBoolean() throws IOException {
        try {
            return buf.get() == 1;
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    public byte readByte() throws IOException {
        try {
            return buf.get();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    public int readUnsignedByte() throws IOException {
        return readByte() & 0xff;
    }

    public short readShort() throws IOException {
        try {
            return buf.getShort();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    public int readUnsignedShort() throws IOException {
        return readShort() & 0xffff;
    }

    public char readChar() throws IOException {
        try {
            return buf.getChar();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    public int readInt() throws IOException {
        try {
            return buf.getInt();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    public long readLong() throws IOException {
        try {
            return buf.getLong();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    public float readFloat() throws IOException {
        try {
            return buf.getFloat();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    public double readDouble() throws IOException {
        try {
            return buf.getDouble();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();

        for (byte c = 0; buf.hasRemaining() && c != '\n'; c = readByte()) {
            sb.append((char) c);
        }

        return sb.toString();
    }

    public String readUTF() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

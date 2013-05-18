/*
 ** 2013 May 14
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Wrapper to implement the DataInput and DataOutput interfaces on a ByteBuffer.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ByteBufferIO implements DataInput, DataOutput {
    
    private final ByteBuffer buf;

    public ByteBufferIO(ByteBuffer buf) {
        this.buf = buf;
    }
    
    public ByteBuffer getBuffer() {
        return buf;
    }
    
    // ByteBuffer wrapper start
    /**
     * Same as {@link java.nio.ByteBuffer}.hasRemaining()
     *
     * @return true, if there are remaining bytes in the byte buffer
     */
    public boolean hasRemaining() {
        return buf.hasRemaining();
    }

    /**
     * Same as {@link java.nio.ByteBuffer}.remaining()
     *
     * @return remaining bytes in the byte buffer
     */
    public int remaining() {
        return buf.remaining();
    }

    /**
     * Same as {@link java.nio.ByteBuffer}.position()
     *
     * @return position in the byte buffer
     */
    public int position() {
        return buf.position();
    }

    /**
     * Same as {@link java.nio.ByteBuffer}.position(int newPosition)
     * 
     * @param pos new buffer position
     * @throws IOException 
     */
    public void position(int pos) throws IOException {
        try {
            buf.position(pos);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Sets the buffer's position relative to the current one. 
     * 
     * @param pos new relative buffer position
     * @throws IOException 
     */
    public void seek(int pos) throws IOException {
        position(position() + pos);
    }
    // ByteBuffer wrapper end
 
    // DataInput start
    @Override
    public void readFully(byte[] b) throws IOException {
        try {
            buf.get(b);
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        try {
            buf.get(b, off, len);
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public int skipBytes(int n) throws IOException {
        try {
            n = Math.min(n, buf.remaining());
            buf.position(buf.position() + n);
            return n;
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public boolean readBoolean() throws IOException {
        try {
            return buf.get() == 1;
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public byte readByte() throws IOException {
        try {
            return buf.get();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return readByte() & 0xff;
    }

    @Override
    public short readShort() throws IOException {
        try {
            return buf.getShort();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return readShort() & 0xffff;
    }

    @Override
    public char readChar() throws IOException {
        try {
            return buf.getChar();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public int readInt() throws IOException {
        try {
            return buf.getInt();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public long readLong() throws IOException {
        try {
            return buf.getLong();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public float readFloat() throws IOException {
        try {
            return buf.getFloat();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public double readDouble() throws IOException {
        try {
            return buf.getDouble();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String readLine() throws IOException {
        try {
            StringBuilder sb = new StringBuilder();
            for (byte c = 0; buf.hasRemaining() && c != '\n'; c = readByte()) {
                sb.append((char) c);
            }
            return sb.toString();
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public String readUTF() throws IOException {
        try {
            return DataInputStream.readUTF(this);
        } catch (BufferUnderflowException ex) {
            throw new IOException(ex);
        }
    }
    // DataInput end
    
    // DataOutput start
    @Override
    public void write(int b) throws IOException {
        writeByte(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        try {
            buf.put(b);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            buf.put(b, off, len);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        try {
            write(v ? (byte) 1 : (byte) 0);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeByte(int v) throws IOException {
        try {
            buf.put((byte) (v & 0xff));
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeShort(int v) throws IOException {
        try {
            buf.putShort((short) (v & 0xffff));
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeChar(int v) throws IOException {
        try {
            buf.putChar((char) (v & 0xff));
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeInt(int v) throws IOException {
        try {
            buf.putInt(v);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeLong(long v) throws IOException {
        try {
            buf.putLong(v);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeFloat(float v) throws IOException {
        try {
            buf.putFloat(v);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeDouble(double v) throws IOException {
        try {
            buf.putDouble(v);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeBytes(String s) throws IOException {
        write(s.getBytes());
    }

    @Override
    public void writeChars(String s) throws IOException {
        try {
            final int len = s.length();
            for (int i = 0; i < len; i++) {
                writeChar(s.charAt(i));
            }
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeUTF(String s) throws IOException {
        write(s.getBytes("UTF-8"));
    }
    // DataOutput end
    
    // DataInput extensions start
    public void read(ByteBuffer b) throws IOException {
        try {
            b.put(buf);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }
    
    public void read(ByteBufferIO bio) throws IOException {
        read(bio.getBuffer());
    }
    
    public long readUnsignedInt() throws IOException {
        return readInt() & 0xffffffffL;
    }
    // DataInput extensions end

    // DataOutput extensions start
    public void write(ByteBuffer b) throws IOException {
        try {
            buf.put(b);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }
    
    public void write(ByteBufferIO bio) throws IOException {
        write(bio.getBuffer());
    }
    // DataOutput extensions end
}

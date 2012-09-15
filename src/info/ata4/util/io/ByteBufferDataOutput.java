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

import java.io.DataOutput;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 * DataOutput wrapper for ByteBuffer
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ByteBufferDataOutput extends ByteBufferData implements DataOutput {
    
    public ByteBufferDataOutput(ByteBuffer buf) {
        super(buf);
    }

    public void write(int b) throws IOException {
        writeByte(b);
    }

    public void write(byte[] b) throws IOException {
        try {
            buf.put(b);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    public void write(byte[] b, int off, int len) throws IOException {
        try {
            buf.put(b, off, len);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    public void writeBoolean(boolean v) throws IOException {
        try {
            write(v ? (byte) 1 : (byte) 0);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    public void writeByte(int v) throws IOException {
        try {
            buf.put((byte) (v & 0xff));
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    public void writeShort(int v) throws IOException {
        try {
            buf.putShort((short) (v & 0xffff));
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    public void writeChar(int v) throws IOException {
        try {
            buf.putChar((char) (v & 0xff));
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    public void writeInt(int v) throws IOException {
        try {
            buf.putInt(v);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    public void writeLong(long v) throws IOException {
        try {
            buf.putLong(v);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    public void writeFloat(float v) throws IOException {
        try {
            buf.putFloat(v);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    public void writeDouble(double v) throws IOException {
        try {
            buf.putDouble(v);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }

    public void writeBytes(String s) throws IOException {
        write(s.getBytes());
    }

    public void writeChars(String s) throws IOException {
        final int len = s.length();
        for (int i = 0; i < len; i++) {
            writeChar(s.charAt(i));
        }
    }

    public void writeUTF(String s) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(ByteBuffer b) throws IOException {
        try {
            buf.put(b);
        } catch (BufferOverflowException ex) {
            throw new IOException(ex);
        }
    }
    
    public void write(ByteBufferDataInput di) throws IOException {
        write(di.buf);
    }
}

/*    
    Copyright (C) 1997  David Flater.
    Java port Copyright (C) 2011 Chas Douglass

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created on Sep 15, 2005
 */
//package net.floogle.jTide;
package ahdt.tides.base;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Logger;

/**
 * @author hd214c
 */
public class XByteBuffer
{
	private MappedByteBuffer buffer;
	private long bitPos = 0;

	transient private Logger logger = Logger.getLogger(this.getClass().getName());

	/**
	 * This utility class is a decorator for MappedByteBuffer that adds comparison
	 * functions for ease of use.
	 * 
	 * @author Chas Douglass
	 */
	public XByteBuffer(MappedByteBuffer buffer, ByteOrder endian)
	{
		this.buffer = buffer;
		this.buffer.order(endian);
		this.buffer.load();
	}

	public void order(ByteOrder order)
	{
		buffer.order(order);
	}

	public void mustBe(char c) throws Exception
	{
		buffer.mark();
		if ((char) buffer.get() != c)
		{
			buffer.reset();
			throw new Exception(AHTideBaseStr.getString("XByteBuffer.0") + (int) c + AHTideBaseStr.getString("XByteBuffer.1") + buffer.get() + AHTideBaseStr.getString("XByteBuffer.2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public void mustBe(byte b) throws Exception
	{
		mustBe((char) b);
	}

	public void mustBe(String s) throws Exception
	{
		buffer.mark();
		if ( !this.getString(s.length()).equals(s))
		{
			buffer.reset();
			throw new Exception(AHTideBaseStr.getString("XByteBuffer.3") + s + AHTideBaseStr.getString("XByteBuffer.4") + this.getString(s.length()) + AHTideBaseStr.getString("XByteBuffer.5")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public String getString(int size)
	{
		mark();
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < size; i++ )
		{
			s.append((char) buffer.get());
		}
		return s.toString();
	}

	public String getStringTo(char delimiter)
	{
		StringBuilder s = new StringBuilder();
		do
		{
			char c = (char) buffer.get();
			if (c == delimiter)
				break;
			s.append(c);
		} while (true);
		return s.toString();
	}

	public CharBuffer asCharBuffer()
	{
		return this.buffer.asCharBuffer();
	}

	public DoubleBuffer asDoubleBuffer()
	{
		return this.buffer.asDoubleBuffer();
	}

	public FloatBuffer asFloatBuffer()
	{
		return this.buffer.asFloatBuffer();
	}

	public IntBuffer asIntBuffer()
	{
		return this.buffer.asIntBuffer();
	}

	public LongBuffer asLongBuffer()
	{
		return this.buffer.asLongBuffer();
	}

	public ByteBuffer asReadOnlyBuffer()
	{
		return this.buffer.asReadOnlyBuffer();
	}

	public ShortBuffer asShortBuffer()
	{
		return this.buffer.asShortBuffer();
	}

	public ByteBuffer compact()
	{
		return this.buffer.compact();
	}

	public int compareTo(ByteBuffer that)
	{
		return this.buffer.compareTo(that);
	}

	public ByteBuffer duplicate()
	{
		return this.buffer.duplicate();
	}

	@Override
	public boolean equals(Object ob)
	{
		return this.buffer.equals(ob);
	}

	public byte get()
	{
		return this.buffer.get();
	}

	public ByteBuffer get(byte[] dst, int offset, int length)
	{
		return this.buffer.get(dst, offset, length);
	}

	public ByteBuffer get(byte[] dst)
	{
		return this.buffer.get(dst);
	}

	public byte get(int index)
	{
		return this.buffer.get(index);
	}

	public char getChar()
	{
		return this.buffer.getChar();
	}

	public char getChar(int index)
	{
		return this.buffer.getChar(index);
	}

	public double getDouble()
	{
		return this.buffer.getDouble();
	}

	public double getDouble(int index)
	{
		return this.buffer.getDouble(index);
	}

	public float getFloat()
	{
		return this.buffer.getFloat();
	}

	public float getFloat(int index)
	{
		return this.buffer.getFloat(index);
	}

	public int getInt()
	{
		return this.buffer.getInt();
	}

	public int getInt(int index)
	{
		return this.buffer.getInt(index);
	}

	public long getLong()
	{
		return this.buffer.getLong();
	}

	public long getLong(int index)
	{
		return this.buffer.getLong(index);
	}

	public short getShort()
	{
		return this.buffer.getShort();
	}

	public short getShort(int index)
	{
		return this.buffer.getShort(index);
	}

	@Override
	public int hashCode()
	{
		return this.buffer.hashCode();
	}

	public boolean isDirect()
	{
		return this.buffer.isDirect();
	}

	public boolean isReadOnly()
	{
		return this.buffer.isReadOnly();
	}

	public ByteBuffer put(byte b)
	{
		return this.buffer.put(b);
	}

	public ByteBuffer put(byte[] src, int offset, int length)
	{
		return this.buffer.put(src, offset, length);
	}

	public ByteBuffer put(ByteBuffer src)
	{
		return this.buffer.put(src);
	}

	public ByteBuffer put(int index, byte b)
	{
		return this.buffer.put(index, b);
	}

	public ByteBuffer putChar(char value)
	{
		return this.buffer.putChar(value);
	}

	public ByteBuffer putChar(int index, char value)
	{
		return this.buffer.putChar(index, value);
	}

	public ByteBuffer putDouble(double value)
	{
		return this.buffer.putDouble(value);
	}

	public ByteBuffer putDouble(int index, double value)
	{
		return this.buffer.putDouble(index, value);
	}

	public ByteBuffer putFloat(float value)
	{
		return this.buffer.putFloat(value);
	}

	public ByteBuffer putFloat(int index, float value)
	{
		return this.buffer.putFloat(index, value);
	}

	public ByteBuffer putInt(int index, int value)
	{
		return this.buffer.putInt(index, value);
	}

	public ByteBuffer putInt(int value)
	{
		return this.buffer.putInt(value);
	}

	public ByteBuffer putLong(int index, long value)
	{
		return this.buffer.putLong(index, value);
	}

	public ByteBuffer putLong(long value)
	{
		return this.buffer.putLong(value);
	}

	public ByteBuffer putShort(int index, short value)
	{
		return this.buffer.putShort(index, value);
	}

	public ByteBuffer putShort(short value)
	{
		return this.buffer.putShort(value);
	}

	public ByteBuffer slice()
	{
		return this.buffer.slice();
	}

	@Override
	public String toString()
	{
		return this.buffer.toString();
	}

	public int position()
	{
		return buffer.position();
	}

	public void position(int nPos)
	{
		buffer.position(nPos);
		bitPos = nPos * 8;
	}

	public void mark()
	{
		buffer.mark();
	}

	public void reset()
	{
		buffer.reset();
	}

	private int mask[] =
	{ 0x00, 0x80, 0xc0, 0xe0, 0xf0, 0xf8, 0xfc, 0xfe };
	private int notmask[] =
	{ 0xff, 0x7f, 0x3f, 0x1f, 0x0f, 0x07, 0x03, 0x01 };

	/**
	 * Set the bit pointer to match the current byte pointer
	 */
	public void bitMark()
	{
		bitPos = buffer.position() * 8;
	}

	public void roundUpToNextByte()
	{
		if (bitPos % 8 != 0)
		{
			buffer.position((int) (bitPos + 8) / 8);
			bitMark();
		}
	}

	public int unpack(long numBits)
	{
		if ((int) (bitPos / 8) != buffer.position())
		{
			throw new RuntimeException(String.format(AHTideBaseStr.getString("XByteBuffer.6"), bitPos, bitPos / 8, //$NON-NLS-1$
					buffer.position()));
		}
		int value = 0;
		// logger.debug("reading " + numBits + " bits starting at " +
		// buffer.position());
		int startByteIndex = (int) (bitPos / 8);
		long lastBitIndex = bitPos + numBits;
		long endByteIndex = lastBitIndex / 8;

		/* AND the start and end bit positions with 7, this is the same as */
		/* doing a mod with 8 but is faster. Here we are computing the start */
		/* and end bits within the start and end bytes for the field. */
		int startBitWithinByte = (int) (bitPos % 8);
		int endBitWithinByte = (int) (lastBitIndex % 8);

		/* Compute the number of bytes covered. */
		int bytesCovered = (int) (endByteIndex - startByteIndex - 1);

		/* If the value is stored in one byte, retrieve it. */
		if (startByteIndex == endByteIndex)
		{
			/* Mask out anything prior to the start bit and after the end bit. */
			int x = buffer.get(startByteIndex) & 0xFF;
			int maskx = (mask[endBitWithinByte] & notmask[startBitWithinByte]);
			value = (buffer.get(startByteIndex) & 0xFF) & (mask[endBitWithinByte] & notmask[startBitWithinByte]);

			/* Now we shift the value to the right. */
			value >>= (8 - endBitWithinByte);
		}
		/* If the value covers more than 1 byte, retrieve it. */
		else
		{
			/* Here we mask out data prior to the start bit of the first byte */
			/* and shift to the left the necessary amount. */
			value = (buffer.get(startByteIndex++ ) & 0xFF & notmask[startBitWithinByte]) << (numBits - (8 - startBitWithinByte));

			/* Loop while decrementing the byte counter. */
			while (bytesCovered-- != 0)
			{
				/* Get the next 8 bits from the buffer. */
				value += (buffer.get(startByteIndex++ ) & 0xFF) << ((bytesCovered << 3) + endBitWithinByte);
			}
			/* For the last byte we mask out anything after the end bit and */
			/* then shift to the right (8 - end_bit) bits. */
			value |= ((buffer.get(startByteIndex) & 0xFF) & mask[endBitWithinByte]) >> (8 - endBitWithinByte);
		}

		bitPos = lastBitIndex;
		buffer.position((int) (bitPos / 8));

		return (value);
	}

	public int signedUnpack(long numBits)
	{
		int extendMask = 0x7fffffff;
		int value;

		/* This function is not used anywhere that this case could arise. */
		assert (numBits > 0);

		value = unpack(numBits);

		if ((value & (1 << (numBits - 1))) != 0)
			value |= (extendMask << numBits);

		return (value);

	}

	public String unpackString(int size, String key)
	{
		int c = unpack(8L);
		StringBuffer thing = new StringBuffer();
		while (c != 0)
		{
			thing.append((char) c);
			c = unpack(8L);
		}
		return thing.toString();
	}
}

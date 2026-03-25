package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.MetaspaceObj;

public class Array extends MetaspaceObj
{
	long length_offset;

	protected Array(String name, long length_offset, long data_offset, long address)
	{
		super(name, address, data_offset);
		this.length_offset = length_offset;
	}

	public int length()
	{
		return super.read_int(length_offset);
	}

	public static class Array_int extends Array
	{
		private static final long _length = vm_struct.entry.find("Array<int>", "_length").offset;
		private static final long _data = vm_struct.entry.find("Array<int>", "_data").offset;

		public Array_int(long address)
		{
			super("Array<int>", _length, _data, address);
		}

		public int get(int idx)
		{
			return super.read_int_idx(idx);
		}

		public void set(int idx, int value)
		{
			super.write_idx(idx, value);
		}
	}

	public static class Array_u1 extends Array
	{
		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find("Array<u1>", "_data").offset;

		public Array_u1(long address)
		{
			super("Array<u1>", _length, _data, address);
		}

		public byte get(int idx)
		{
			return super.read_byte_idx(idx);
		}

		public void set(int idx, byte value)
		{
			super.write_idx(idx, value);
		}
	}

	public static class Array_u2 extends Array
	{
		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find("Array<u2>", "_data").offset;

		public Array_u2(long address)
		{
			super("Array<u2>", _length, _data, address);
		}

		public short get(int idx)
		{
			return super.read_short_idx(idx);
		}

		public void set(int idx, short value)
		{
			super.write_idx(idx, value);
		}
	}

	public static class Array_pMethod extends Array
	{
		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find("Array<Method*>", "_data").offset;

		public Array_pMethod(long address)
		{
			super("Array<Method*>", _length, _data, address);
		}

		public Method get(int idx)
		{
			return super.read_memory_operator_ptr_idx(Method.class, idx);
		}

		public void set(int idx, Method value)
		{
			super.write_pointer_idx(idx, value);
		}
	}
}
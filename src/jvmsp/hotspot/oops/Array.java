package jvmsp.hotspot.oops;

import jvmsp.type.cxx_type;
import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.MetaspaceObj;

public class Array extends MetaspaceObj
{
	long data_offset;
	long length_offset;
	long element_size;

	protected Array(String name, long length_offset, long data_offset, long element_size, long address)
	{
		super(name, address);
		this.data_offset = data_offset;
		this.length_offset = length_offset;
		this.element_size = element_size;
	}

	/**
	 * 访问和存储的索引不能大于等于此长度，否则会访问野指针。<br>
	 * 
	 * @return
	 */
	public int length()
	{
		return super.read_cint(length_offset);
	}

	public long element_offset(int idx)
	{
		return data_offset + idx * element_size;
	}

	public static class Array_int extends Array
	{
		private static final long _length = vm_struct.entry.find("Array<int>", "_length").offset;
		private static final long _data = vm_struct.entry.find("Array<int>", "_data").offset;

		public static final long size = sizeof("Array<int>");

		public Array_int(long address)
		{
			super("Array<int>", _length, _data, cxx_type._int.size(), address);
		}

		public int at(int idx)
		{
			return super.read_int(element_offset(idx));
		}

		public void at_put(int idx, int value)
		{
			super.write(element_offset(idx), value);
		}
	}

	public static class Array_u1 extends Array
	{
		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find("Array<u1>", "_data").offset;

		public static final long size = sizeof("Array<u1>");

		public Array_u1(long address)
		{
			super("Array<u1>", _length, _data, cxx_type.uint8_t.size(), address);
		}

		public byte at(int idx)
		{
			return super.read_byte(element_offset(idx));
		}

		public void at_put(int idx, byte value)
		{
			super.write(element_offset(idx), value);
		}
	}

	public static class Array_u2 extends Array
	{
		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find("Array<u2>", "_data").offset;

		public static final long size = sizeof("Array<u2>");

		public Array_u2(long address)
		{
			super("Array<u2>", _length, _data, cxx_type.uint16_t.size(), address);
		}

		public short at(int idx)
		{
			return super.read_short(element_offset(idx));
		}

		public void at_put(int idx, short value)
		{
			super.write(element_offset(idx), value);
		}
	}

	public static class Array_pVMStruct<_T extends vm_struct> extends Array
	{
		Class<_T> struct_clazz;

		public Array_pVMStruct(String name, Class<_T> struct_clazz, long length_offset, long data_offset, long address)
		{
			super(name, length_offset, data_offset, cxx_type.pvoid.size(), address);
			this.struct_clazz = struct_clazz;
		}

		public _T at(int idx)
		{
			return super.read_memory_object_ptr(struct_clazz, element_offset(idx));
		}

		public void at_put(int idx, _T value)
		{
			super.write_pointer(element_offset(idx), value);
		}
	}

	public static class Array_pMethod extends Array_pVMStruct<Method>
	{
		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find("Array<Method*>", "_data").offset;

		public static final long size = sizeof("Array<Method*>");

		public Array_pMethod(long address)
		{
			super("Array<Method*>", Method.class, _length, _data, address);
		}
	}

	public static class Array_pKlass extends Array_pVMStruct<Klass>
	{
		private static final long _length = vm_struct.entry.find("Array<Klass*>", "_length").offset;
		private static final long _data = vm_struct.entry.find("Array<Klass*>", "_data").offset;

		public static final long size = sizeof("Array<Klass*>");

		public Array_pKlass(long address)
		{
			super("Array<Klass*>", Klass.class, _length, _data, address);
		}
	}
}
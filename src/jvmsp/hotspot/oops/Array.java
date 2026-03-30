package jvmsp.hotspot.oops;

import jvmsp.type.cxx_type;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.MetaspaceObj;

public abstract class Array extends MetaspaceObj
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

	public long data()
	{
		return address + data_offset;
	}

	public boolean is_empty()
	{
		return length() == 0;
	}

	public long off_at(int idx)
	{
		return data_offset + idx * element_size;
	}

	public long adr_at(int i)
	{
		return address + off_at(i);
	}

	public long size()
	{
		return off_at(length() - 1);
	}

	@Override
	public int meta_type()
	{
		return Type.array_type((int) element_size);
	}

	public static class Array_int extends Array
	{
		public static final String type_name = "Array<int>";
		public static final long size = sizeof(type_name);

		private static final long _length = vm_struct.entry.find(type_name, "_length").offset;
		private static final long _data = vm_struct.entry.find(type_name, "_data").offset;

		public Array_int(long address)
		{
			super(type_name, _length, _data, cxx_type._int.size(), address);
		}

		public int at(int idx)
		{
			return super.read_int(off_at(idx));
		}

		public void at_put(int idx, int value)
		{
			super.write(off_at(idx), value);
		}
	}

	public static class Array_u1 extends Array
	{
		public static final String type_name = "Array<u1>";
		public static final long size = sizeof(type_name);

		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find(type_name, "_data").offset;

		public Array_u1(long address)
		{
			super(type_name, _length, _data, cxx_type.uint8_t.size(), address);
		}

		public byte at(int idx)
		{
			return super.read_byte(off_at(idx));
		}

		public void at_put(int idx, byte value)
		{
			super.write(off_at(idx), value);
		}
	}

	public static class Array_u2 extends Array
	{
		public static final String type_name = "Array<u2>";
		public static final long size = sizeof(type_name);

		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find(type_name, "_data").offset;

		public Array_u2(long address)
		{
			super(type_name, _length, _data, cxx_type.uint16_t.size(), address);
		}

		public short at(int idx)
		{
			return super.read_short(off_at(idx));
		}

		public void at_put(int idx, short value)
		{
			super.write(off_at(idx), value);
		}
	}

	public static class Array_pVMStruct<_T extends vm_struct> extends Array
	{
		public static final long size = Array_int.size;

		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find("Array<Method*>", "_data").offset;// 指针类型的Array::data[]地址

		Class<_T> struct_clazz;

		public Array_pVMStruct(String name, Class<_T> struct_clazz, long length_offset, long data_offset, long address)
		{
			super(name, length_offset, data_offset, cxx_type.pvoid.size(), address);
			this.struct_clazz = struct_clazz;
		}

		public Array_pVMStruct(String name, Class<_T> struct_clazz, long address)
		{
			this(name, struct_clazz, _length, _data, address);
		}

		public _T at(int idx)
		{
			return super.read_memory_object_ptr(struct_clazz, off_at(idx));
		}

		public void at_put(int idx, _T value)
		{
			super.write_memory_object_ptr(off_at(idx), value);
		}
	}

	public static class Array_pMethod extends Array_pVMStruct<Method>
	{
		public static final String type_name = "Array<Method*>";
		public static final long size = sizeof(type_name);

		public Array_pMethod(long address)
		{
			super(type_name, Method.class, address);
		}
	}

	public static class Array_pKlass extends Array_pVMStruct<Klass>
	{
		public static final String type_name = "Array<Klass*>";
		public static final long size = sizeof(type_name);

		public Array_pKlass(long address)
		{
			super(type_name, Klass.class, address);
		}
	}

	public static class Array_pInstanceKlass extends Array_pVMStruct<InstanceKlass>
	{
		public static final String type_name = "Array<InstanceKlass*>";
		public static final long size = sizeof(type_name);

		public Array_pInstanceKlass(long address)
		{
			super(type_name, InstanceKlass.class, address);
		}
	}

	public static class Array_pArray_u1 extends Array_pVMStruct<Array_u1>
	{
		public static final String type_name = "Array<Array<u1>*>";
		public static final long size = sizeof(type_name);

		public Array_pArray_u1(long address)
		{
			super(type_name, Array_u1.class, address);
		}
	}

	public static class Array_VMStruct<_T extends vm_struct> extends Array
	{
		public static final long size = Array_int.size;

		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find("Array<ResolvedIndyEntry>", "_data").offset;// 指针类型的Array::data[]地址

		Class<_T> struct_clazz;

		public Array_VMStruct(String name, Class<_T> struct_clazz, long length_offset, long data_offset, long type_size, long address)
		{
			super(name, length_offset, data_offset, type_size, address);
			this.struct_clazz = struct_clazz;
		}

		public Array_VMStruct(String name, Class<_T> struct_clazz, long type_size, long address)
		{
			this(name, struct_clazz, _length, _data, type_size, address);
		}

		public _T at(int idx)
		{
			return super.read_memory_object(struct_clazz, off_at(idx));
		}

		public void at_put(int idx, _T value)
		{
			super.write_memory_object(off_at(idx), value, super.element_size);
		}
	}

	public static class Array_ResolvedIndyEntry extends Array_VMStruct<ResolvedIndyEntry>
	{
		public static final String type_name = "Array<ResolvedIndyEntry*>";
		public static final long size = sizeof(type_name);

		public Array_ResolvedIndyEntry(long address)
		{
			super(type_name, ResolvedIndyEntry.class, size, address);
		}
	}

	public static class Array_ResolvedFieldEntry extends Array_VMStruct<ResolvedFieldEntry>
	{
		public static final String type_name = "Array<ResolvedFieldEntry*>";
		public static final long size = sizeof(type_name);

		public Array_ResolvedFieldEntry(long address)
		{
			super(type_name, ResolvedFieldEntry.class, size, address);
		}
	}

	public static class Array_ResolvedMethodEntry extends Array_VMStruct<ResolvedMethodEntry>
	{
		public static final String type_name = "Array<ResolvedMethodEntry*>";
		public static final long size = sizeof(type_name);

		public Array_ResolvedMethodEntry(long address)
		{
			super(type_name, ResolvedMethodEntry.class, size, address);
		}
	}
}
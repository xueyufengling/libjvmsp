package jvmsp;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.pointer;
import jvmsp.type.java_type;
import jvmsp.libso.libc;

public abstract class memory
{
	public static final pointer malloc(long size)
	{
		return pointer.to(unsafe.malloc(size));
	}

	public static final pointer malloc(long num, Class<?> type_clazz)
	{
		return pointer.to(unsafe.malloc(num * java_type.sizeof(type_clazz)), cxx_type.of(type_clazz));
	}

	public static final pointer malloc(long num, cxx_type type)
	{
		return pointer.to(unsafe.malloc(num * cxx_type.sizeof(type)), type);
	}

	public static final pointer malloc(cxx_type type)
	{
		return pointer.to(unsafe.malloc(cxx_type.sizeof(type)), type);
	}

	public static final pointer malloc(cxx_type... types)
	{
		return pointer.to(unsafe.malloc(cxx_type.sizeof(types)));
	}

	public static final void free(pointer ptr)
	{
		unsafe.free(ptr.address());
	}

	public static final void memset(pointer ptr, int value, long num)
	{
		unsafe.memset(null, ptr.address(), num, (byte) value);
	}

	public static final void memcpy(pointer ptr_dest, pointer ptr_src, long num)
	{
		unsafe.memcpy((Object) null, ptr_src.address(), (Object) null, ptr_dest.address(), num);
	}

	/**
	 * 从Java的字符串对象拷贝一个新的C字符串
	 * 
	 * @param str Java字符串对象
	 * @param cs  字符编码
	 * @return
	 */
	public static final pointer c_str(String str, Charset cs)
	{
		if (str == null)
			return pointer.nullptr;
		byte[] bytes = str.getBytes(cs);
		long cstr_addr = unsafe.malloc(bytes.length + 1);
		unsafe.memcpy(bytes, 0, cstr_addr, bytes.length);// Java的数组元素并不是从索引0开始的，而是从ARRAY_OBJECT_BASE_OFFSET开始
		unsafe.write(cstr_addr + bytes.length, (byte) 0);
		return pointer.to(cstr_addr, cxx_type._char);
	}

	public static final pointer c_str(String str)
	{
		return c_str(str, Charset.defaultCharset());
	}

	/**
	 * 从C字符串地址拷贝并构造一个新的Java字符串
	 * 
	 * @param cstr_addr C字符串指针
	 * @param cs        字符编码
	 * @return
	 */
	public static final String string(long cstr_addr, Charset cs)
	{
		if (cstr_addr == 0)// 空指针
		{
			return null;
		}
		else
		{
			long len = libc.strlen(cstr_addr);
			byte[] bytes = new byte[(int) len];// 不包含结尾的'\0'
			unsafe.memcpy(cstr_addr, bytes, 0, bytes.length);
			return new String(bytes, cs);
		}
	}

	public static final String string(pointer cstr, Charset cs)
	{
		return string(cstr.address(), cs);
	}

	public static final String string(long cstr_addr)
	{
		return string(cstr_addr, Charset.defaultCharset());
	}

	public static final String string(pointer cstr)
	{
		return string(cstr.address());
	}

	@SuppressWarnings("unchecked")
	public static final <_T> _T[] cat(_T[]... arrays)
	{
		int total_len = 0;
		for (int idx = 0; idx < arrays.length; ++idx)
			total_len += arrays[idx].length;
		Object[] result = new Object[total_len];
		int ptr = 0;
		for (int idx = 0; idx < arrays.length; ptr += arrays[idx].length, ++idx)
			System.arraycopy(arrays[idx], 0, result, ptr, arrays[idx].length);
		return (_T[]) result;
	}

	@SuppressWarnings("unchecked")
	public static final <_T> _T[] cat(_T t1, _T... ts)
	{
		Object[] result = new Object[ts.length + 1];
		result[0] = t1;
		System.arraycopy(ts, 0, result, 1, ts.length);
		return (_T[]) result;
	}

	@SuppressWarnings("unchecked")
	public static final <_T> _T[] cat(_T t1, _T t2, _T... ts)
	{
		Object[] result = new Object[ts.length + 2];
		result[0] = t1;
		result[1] = t2;
		System.arraycopy(ts, 0, result, 2, ts.length);
		return (_T[]) result;
	}

	@SuppressWarnings("unchecked")
	public static final <_T> _T[] to_array(Class<_T> c, List<_T> list)
	{
		_T[] arr = (_T[]) Array.newInstance(c, list.size());
		return list.toArray(arr);
	}

	public static final Class<?> get_list_type(Type list_field)
	{
		return reflection.first_generic_class(list_field);
	}

	public static final boolean flag_bit(int flags, int flag)
	{
		return (flags & flag) != 0;
	}

	public static final boolean flag_bit(short flags, short flag)
	{
		return (flags & flag) != 0;
	}

	public static final boolean flag_bit(byte flags, byte flag)
	{
		return (flags & flag) != 0;
	}

	/**
	 * 
	 * @param flags
	 * @param flag  只能有一个bit为1，其他为0
	 * @param mark
	 * @return
	 */
	public static final int set_flag_bit(int flags, int flag, boolean mark)
	{
		return mark ? flags | flag : flags & (~flag);
	}

	public static final short set_flag_bit(short flags, short flag, boolean mark)
	{
		return (short) (mark ? flags | flag : flags & (~flag));
	}

	public static final byte set_flag_bit(byte flags, byte flag, boolean mark)
	{
		return (byte) (mark ? flags | flag : flags & (~flag));
	}

	public static final String bits_str(byte value)
	{
		return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
	}

	public static final String bits_str(short value)
	{
		return String.format("%16s", Integer.toBinaryString(value & 0xFFFF)).replace(' ', '0');
	}

	public static final String bits_str(int value)
	{
		return String.format("%32s", Integer.toBinaryString(value & 0xFFFFFFFF)).replace(' ', '0');
	}

	public static final String bits_str(long value)
	{
		return String.format("%64s", Long.toBinaryString(value)).replace(' ', '0');
	}

	/**
	 * 内存操作封装。<br>
	 * 可用于Java层访问C++对象的接口。<br>
	 */
	public static class memory_operator
	{
		protected final long address;
		protected final long idx_base;// 使用索引访问时的前置偏移量

		public memory_operator(long address, long idx_base)
		{
			this.address = address;
			this.idx_base = idx_base;
		}

		public memory_operator(long address)
		{
			this(address, 0);
		}

		public final long address()
		{
			return this.address;
		}

		public final cxx_type.object read(long offset, cxx_type type)
		{
			return type.new object(address + offset);
		}

		/*
		 * 依照偏移量访问
		 */

		public final byte read_byte(long offset)
		{
			return unsafe.read_byte(address + offset);
		}

		public final void write(long offset, byte value)
		{
			unsafe.write(address + offset, value);
		}

		public final short read_short(long offset)
		{
			return unsafe.read_short(address + offset);
		}

		public final void write(long offset, short value)
		{
			unsafe.write(address + offset, value);
		}

		public final int read_int(long offset)
		{
			return unsafe.read_int(address + offset);
		}

		public final void write(long offset, int value)
		{
			unsafe.write(address + offset, value);
		}

		public final long read_long(long offset)
		{
			return unsafe.read_long(address + offset);
		}

		public final void write(long offset, long value)
		{
			unsafe.write(address + offset, value);
		}

		public final long read_pointer(long offset)
		{
			return unsafe.read_pointer(address + offset);
		}

		public final void write_pointer(long offset, long ptr)
		{
			unsafe.write_pointer(address + offset, ptr);
		}

		public static final <_Struct extends memory_operator> _Struct as_memory_operator(Class<_Struct> clazz, long addr)
		{
			try
			{
				return (_Struct) symbols.find_constructor(clazz, long.class).invoke(addr);
			}
			catch (Throwable ex)
			{
				throw new java.lang.InternalError("read '" + clazz + "' at '" + addr + "' faield", ex);
			}
		}

		public final <_Struct extends memory_operator> _Struct read_memory_operator_ptr(Class<_Struct> clazz, long offset)
		{
			return as_memory_operator(clazz, read_pointer(offset));
		}

		public final <_Struct extends memory_operator> _Struct read_memory_operator(Class<_Struct> clazz, long offset)
		{
			return as_memory_operator(clazz, address + offset);
		}

		public final void write(long offset, memory_operator struct)
		{
			write_pointer(offset, struct.address);
		}

		/*
		 * 依照索引访问
		 */

		public final byte read_byte_idx(int idx)
		{
			return read_byte(idx_base + idx * cxx_type.unsigned_char.size());
		}

		public final short read_short_idx(int idx)
		{
			return read_short(idx_base + idx * cxx_type._short.size());
		}

		public final int read_int_idx(int idx)
		{
			return read_int(idx_base + idx * cxx_type._int.size());
		}

		public final long read_pointer_idx(int idx)
		{
			return read_pointer(idx_base + idx * cxx_type.pvoid.size());
		}

		public final <_Struct extends memory_operator> _Struct read_memory_operator_ptr_idx(Class<_Struct> clazz, int idx)
		{
			return read_memory_operator_ptr(clazz, idx_base + idx * cxx_type.pvoid.size());
		}

		public final void write_idx(int idx, byte value)
		{
			write(idx_base + idx * cxx_type.unsigned_char.size(), value);
		}

		public final void write_idx(int idx, short value)
		{
			write(idx_base + idx * cxx_type._short.size(), value);
		}

		public final void write_idx(int idx, int value)
		{
			write(idx_base + idx * cxx_type._int.size(), value);
		}

		public final void write_idx(int idx, long value)
		{
			write(idx_base + idx * cxx_type._long_long.size(), value);
		}

		public final void write_pointer_idx(int idx, long ptr)
		{
			write_pointer(idx_base + idx * cxx_type.pvoid.size(), ptr);
		}

		public final void write_idx(int idx, memory_operator struct)
		{
			write(idx_base + idx * cxx_type.pvoid.size(), struct);
		}
	}
}

package jvmsp;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;

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
		unsafe.memcpy(cstr_addr, bytes, 0, bytes.length);// Java的数组元素并不是从索引0开始的，而是从ARRAY_OBJECT_BASE_OFFSET开始
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
			unsafe.memcpy(bytes, 0, cstr_addr, bytes.length);
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

	public static final boolean flag_bit(long flags, long flag)
	{
		return (flags & flag) != 0;
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
	public static final long set_flag_bit(long flags, long flag, boolean mark)
	{
		return mark ? flags | flag : flags & (~flag);
	}

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
	public static abstract class memory_object
	{
		protected final long address;

		protected memory_object(long address)
		{
			this.address = address;
		}

		public long address()
		{
			return address;
		}

		public void delete()
		{
			unsafe.free(address);
		}

		protected long offset_addr(long offset)
		{
			return address + offset;
		}

		protected cxx_type.object read(long offset, cxx_type type)
		{
			return type.new object(address + offset);
		}

		/*
		 * 依照偏移量访问
		 */

		protected byte read_byte(long offset)
		{
			return unsafe.read_byte(address + offset);
		}

		protected void write(long offset, byte value)
		{
			unsafe.write(address + offset, value);
		}

		protected short read_short(long offset)
		{
			return unsafe.read_short(address + offset);
		}

		protected void write(long offset, short value)
		{
			unsafe.write(address + offset, value);
		}

		protected int read_int(long offset)
		{
			return unsafe.read_int(address + offset);
		}

		protected void write(long offset, int value)
		{
			unsafe.write(address + offset, value);
		}

		protected long read_long(long offset)
		{
			return unsafe.read_long(address + offset);
		}

		protected void write(long offset, long value)
		{
			unsafe.write(address + offset, value);
		}

		protected long read_pointer(long offset)
		{
			return unsafe.read_pointer(address + offset);
		}

		protected void write_pointer(long offset, long ptr)
		{
			unsafe.write_pointer(address + offset, ptr);
		}

		protected String read_cstr(long offset)
		{
			return unsafe.read_cstr(address + offset);
		}

		protected pointer write_cstr(long offset, String str)
		{
			return unsafe.write_cstr(address + offset, str);
		}

		protected int read_cint(long offset)
		{
			return unsafe.read_cint(address + offset);
		}

		protected void write_cint(long offset, int value)
		{
			unsafe.write_cint(address + offset, value);
		}

		protected int read_uint16_t(long offset)
		{
			return unsafe.read_uint16_t(address + offset);
		}

		protected void write_uint16_t(long offset, int value)
		{
			unsafe.write_uint16_t(address + offset, value);
		}

		protected boolean read_cbool(long offset)
		{
			return unsafe.read_cbool(address + offset);
		}

		protected void write_cbool(long offset, boolean value)
		{
			unsafe.write_cbool(address + offset, value);
		}

		public static final <_MemObject extends memory_object> _MemObject as_memory_object(Class<_MemObject> clazz, long addr)
		{
			try
			{
				return (_MemObject) symbols.find_constructor(clazz, long.class).invoke(addr);
			}
			catch (Throwable ex)
			{
				throw new java.lang.InternalError("read '" + clazz + "' at '" + addr + "' faield", ex);
			}
		}

		public static final <_MemObject extends memory_object> _MemObject as_memory_object_ptr(Class<_MemObject> clazz, long ptr_addr)
		{
			return as_memory_object(clazz, unsafe.read_pointer(ptr_addr));
		}

		public static final void write_memory_object_ptr(long addr, memory_object obj)
		{
			unsafe.write_pointer(addr, obj.address);
		}

		@SuppressWarnings("unchecked")
		public static final <_MemObject extends memory_object> _MemObject[] as_memory_object_arr(Class<_MemObject> clazz, long addr, long size, int num)
		{
			_MemObject[] arr = (_MemObject[]) new memory_object[num];
			for (int idx = 0; idx < num; ++idx)
			{
				arr[idx] = as_memory_object(clazz, addr + idx * size);
			}
			return arr;
		}

		public static final <_MemObject extends memory_object> _MemObject as_memory_object(Function<Long, _MemObject> ctor, long addr)
		{
			return ctor.apply(addr);
		}

		public static final <_MemObject extends memory_object> _MemObject as_memory_object_ptr(Function<Long, _MemObject> ctor, long ptr_addr)
		{
			return as_memory_object(ctor, unsafe.read_pointer(ptr_addr));
		}

		@SuppressWarnings("unchecked")
		public static final <_MemObject extends memory_object> _MemObject[] as_memory_object_arr(Function<Long, _MemObject> ctor, long addr, long size, int num)
		{
			_MemObject[] arr = (_MemObject[]) new memory_object[num];
			for (int idx = 0; idx < num; ++idx)
			{
				arr[idx] = as_memory_object(ctor, addr + idx * size);
			}
			return arr;
		}

		protected <_MemObject extends memory_object> _MemObject read_memory_object_ptr(Class<_MemObject> clazz, long offset)
		{
			return as_memory_object_ptr(clazz, address + offset);
		}

		/**
		 * 读取数组中的元素
		 * 
		 * @param <_MemObject>
		 * @param clazz
		 * @param offset       数组首个元素的偏移量
		 * @param size
		 * @param idx
		 * @return
		 */
		protected <_MemObject extends memory_object> _MemObject read_memory_object_at(Class<_MemObject> clazz, long offset, long size, int idx)
		{
			return as_memory_object(clazz, address + offset + size * idx);
		}

		/**
		 * 读取数组，数组地址为&_MemObject[0]，即数组的第一个元素。<br>
		 * 数组的每个元素都是一个对象，而非其指针。<br>
		 * 
		 * @param <_MemObject>
		 * @param clazz
		 * @param offset
		 * @param size
		 * @param num
		 * @return
		 */
		protected <_MemObject extends memory_object> _MemObject[] read_memory_object_arr(Class<_MemObject> clazz, long offset, long size, int num)
		{
			return as_memory_object_arr(clazz, address + offset, size, num);
		}

		protected <_MemObject extends memory_object> _MemObject read_memory_object_ptr(Function<Long, _MemObject> ctor, long offset)
		{
			return as_memory_object_ptr(ctor, address + offset);
		}

		protected <_MemObject extends memory_object> _MemObject read_memory_object(Class<_MemObject> clazz, long offset)
		{
			return as_memory_object(clazz, address + offset);
		}

		protected <_MemObject extends memory_object> _MemObject read_memory_object(Function<Long, _MemObject> ctor, long offset)
		{
			return as_memory_object(ctor, address + offset);
		}

		protected void write_pointer(long offset, memory_object struct)
		{
			write_pointer(offset, struct.address);
		}
	}

	/**
	 * 固定位置的内存操作
	 */
	public static class memory_operator extends memory_object
	{
		public memory_operator(long address)
		{
			super(address);
		}

		@Override
		public long address()
		{
			return super.address();
		}

		@Override
		public long offset_addr(long offset)
		{
			return super.offset_addr(offset);
		}

		@Override
		public cxx_type.object read(long offset, cxx_type type)
		{
			return super.read(offset, type);
		}

		@Override
		public byte read_byte(long offset)
		{
			return super.read_byte(offset);
		}

		@Override
		public void write(long offset, byte value)
		{
			super.write(offset, value);
		}

		@Override
		public short read_short(long offset)
		{
			return super.read_short(offset);
		}

		@Override
		public void write(long offset, short value)
		{
			super.write(offset, value);
		}

		@Override
		public int read_int(long offset)
		{
			return super.read_int(offset);
		}

		@Override
		public void write(long offset, int value)
		{
			super.write(offset, value);
		}

		@Override
		public long read_long(long offset)
		{
			return super.read_long(offset);
		}

		@Override
		public void write(long offset, long value)
		{
			super.write(offset, value);
		}

		@Override
		public long read_pointer(long offset)
		{
			return super.read_pointer(offset);
		}

		@Override
		public void write_pointer(long offset, long ptr)
		{
			super.write_pointer(offset, ptr);
		}

		@Override
		public String read_cstr(long offset)
		{
			return super.read_cstr(offset);
		}

		@Override
		public pointer write_cstr(long offset, String str)
		{
			return super.write_cstr(offset, str);
		}

		@Override
		public int read_cint(long offset)
		{
			return super.read_cint(offset);
		}

		@Override
		public void write_cint(long offset, int value)
		{
			super.write_cint(offset, value);
		}

		@Override
		public int read_uint16_t(long offset)
		{
			return super.read_uint16_t(offset);
		}

		@Override
		public void write_uint16_t(long offset, int value)
		{
			super.write_uint16_t(offset, value);
		}

		@Override
		public boolean read_cbool(long offset)
		{
			return super.read_cbool(offset);
		}

		@Override
		public void write_cbool(long offset, boolean value)
		{
			super.write_cbool(offset, value);
		}

		@Override
		public <_MemObject extends memory_object> _MemObject read_memory_object_ptr(Class<_MemObject> clazz, long offset)
		{
			return super.read_memory_object_ptr(clazz, offset);
		}

		@Override
		public <_MemObject extends memory_object> _MemObject read_memory_object_at(Class<_MemObject> clazz, long offset, long size, int idx)
		{
			return super.read_memory_object_at(clazz, offset, size, idx);
		}

		@Override
		public <_MemObject extends memory_object> _MemObject[] read_memory_object_arr(Class<_MemObject> clazz, long offset, long size, int num)
		{
			return super.read_memory_object_arr(clazz, offset, size, num);
		}

		@Override
		public <_MemObject extends memory_object> _MemObject read_memory_object_ptr(Function<Long, _MemObject> ctor, long offset)
		{
			return super.read_memory_object_ptr(ctor, offset);
		}

		@Override
		public <_MemObject extends memory_object> _MemObject read_memory_object(Class<_MemObject> clazz, long offset)
		{
			return super.read_memory_object(clazz, offset);
		}

		@Override
		public <_MemObject extends memory_object> _MemObject read_memory_object(Function<Long, _MemObject> ctor, long offset)
		{
			return super.read_memory_object(ctor, offset);
		}

		@Override
		public void write_pointer(long offset, memory_object struct)
		{
			super.write_pointer(offset, struct);
		}
	}
}

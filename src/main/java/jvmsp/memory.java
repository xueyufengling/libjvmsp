package jvmsp;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;

import jvmsp.abi.call_convention;
import jvmsp.arch.os;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.function_type;
import jvmsp.type.cxx_type.pointer;
import jvmsp.type.java_type;
import jvmsp.libso.libc;
import jvmsp.libso.win.libkernel32;
import jvmsp.libso.win.winnt;

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
		unsafe.memcpy(cstr_addr, bytes, 0, bytes.length);// Java的数组元素并不是从偏移量0开始的，而是从ARRAY_OBJECT_BASE_OFFSET开始
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
	public static final <_T> _T[] cat(_T[] arr, _T... ts)
	{
		Object[] result = new Object[ts.length + arr.length];
		System.arraycopy(arr, 0, result, 0, arr.length);
		System.arraycopy(ts, 0, result, arr.length, ts.length);
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

	/**
	 * 构建右侧n个位的掩码，例如0x00001111。<br>
	 * 
	 * @param n
	 * @return
	 */
	public static final long right_bits_mask(int n)
	{
		return (1L << n) - 1;
	}

	public static final byte byte_flag(int bit)
	{
		return (byte) (1 << bit);
	}

	public static final short short_flag(int bit)
	{
		return (short) (1 << bit);
	}

	public static final int int_flag(int bit)
	{
		return (int) (1 << bit);
	}

	public static final long long_flag(int bit)
	{
		return (long) (1 << bit);
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
	 * 具有内存地址的对象
	 */
	public static interface pointer_type
	{
		public abstract long address();

		public default boolean addr_equals(pointer_type o)
		{
			return this.address() == o.address();
		}
	}

	/**
	 * 内存操作封装。<br>
	 * 可用于Java层访问C++对象的接口。<br>
	 */
	public static class memory_object implements pointer_type
	{
		protected final long address;

		/**
		 * 新分配对象或从现有对象上创建
		 * 
		 * @param address_or_size
		 * @param alloc_size
		 */
		protected memory_object(long address_or_size, boolean alloc_size)
		{
			if (alloc_size)
				this.address = unsafe.malloc(address_or_size);
			else
				this.address = address_or_size;
		}

		protected memory_object(long address)
		{
			this(address, false);
		}

		@Override
		public long address()
		{
			return address;
		}

		public static final memory_object nullptr = new memory_object(0);

		public void delete()
		{
			unsafe.free(address);
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof memory_object mo ? this.addr_equals(mo) : false;
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

		protected long read_ptr(long offset)
		{
			return unsafe.read_ptr(address + offset);
		}

		protected void write_ptr(long offset, long ptr)
		{
			unsafe.write_ptr(address + offset, ptr);
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

		protected long read_cuint(long offset)
		{
			return unsafe.read_cuint(address + offset);
		}

		protected void write_cuint(long offset, long value)
		{
			unsafe.write_cuint(address + offset, value);
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

		private static final Class<?>[] _memory_object_ctor_arg_types = new Class<?>[]
		{ long.class };

		/**
		 * 将该地址解释为指定类型的对象，类型可以为抽象类。<br>
		 * 如果构建为抽象类，则不要调用抽象方法，否则会引发运行时异常。<br>
		 * 
		 * @param <_MemObject>
		 * @param clazz
		 * @param addr
		 * @return
		 */
		public static final <_MemObject extends memory_object> _MemObject as_memory_object(Class<_MemObject> clazz, long addr)
		{
			try
			{
				if (addr == 0)
					return null;// 空指针则不构造对象
				else
					return (_MemObject) unsafe.force_construct(clazz, _memory_object_ctor_arg_types, addr);
			}
			catch (Throwable ex)
			{
				throw new java.lang.InternalError("read '" + clazz + "' at '" + addr + "' faield", ex);
			}
		}

		public static final <_MemObject extends memory_object> _MemObject as_memory_object_ptr(Class<_MemObject> clazz, long ptr_addr)
		{
			return as_memory_object(clazz, unsafe.read_ptr(ptr_addr));
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

		protected <_MemObject extends memory_object> _MemObject read_memory_object(Class<_MemObject> clazz, long offset)
		{
			return as_memory_object(clazz, address + offset);
		}

		/**
		 * 强制转换
		 * 
		 * @param <_MemObject>
		 * @param clazz
		 * @return
		 */
		public <_MemObject extends memory_object> _MemObject cast(Class<_MemObject> clazz)
		{
			return as_memory_object(clazz, address);
		}

		protected void write_memory_object_ptr(long offset, memory_object struct)
		{
			unsafe.write_ptr(address + offset, struct);
		}

		protected <_MemObject extends memory_object> void write_memory_object(long offset, memory_object struct, long size)
		{
			unsafe.memcpy(address + offset, struct.address, size);
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
		public long read_ptr(long offset)
		{
			return super.read_ptr(offset);
		}

		@Override
		public void write_ptr(long offset, long ptr)
		{
			super.write_ptr(offset, ptr);
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
		public <_MemObject extends memory_object> _MemObject read_memory_object(Class<_MemObject> clazz, long offset)
		{
			return super.read_memory_object(clazz, offset);
		}

		@Override
		public void write_memory_object_ptr(long offset, memory_object struct)
		{
			super.write_memory_object_ptr(offset, struct);
		}

		@Override
		public <_MemObject extends memory_object> void write_memory_object(long offset, memory_object struct, long size)
		{
			super.write_memory_object(offset, struct, size);
		}
	}

	/**
	 * 具有执行权限的内存。<br>
	 * 执行权限的管理是因操作系统而异的，无法通过标准C API直接分配。<br>
	 */
	public static final class os_memory
	{
		// 内存权限与标志常量
		public static final int MEM_PROT_NOACC = 0x0;
		public static final int MEM_PROT_READ = int_flag(0);
		public static final int MEM_PROT_WRITE = int_flag(1);
		public static final int MEM_PROT_EXEC = int_flag(2);
		public static final int MEM_PROT_RW = MEM_PROT_READ | MEM_PROT_WRITE;
		public static final int MEM_PROT_RX = MEM_PROT_READ | MEM_PROT_EXEC;
		public static final int MEM_PROT_RWX = MEM_PROT_READ | MEM_PROT_WRITE | MEM_PROT_EXEC;

		// Windows标志
		public static final int MEM_FLAG_TOP_DOWN = int_flag(3);
		public static final int MEM_FLAG_LARGE_PAGES = int_flag(4);
		public static final int MEM_FLAG_PHYSICAL = int_flag(5);
		public static final int MEM_FLAG_RESET = int_flag(6);

		// Linux标志
		public static final int MEM_FLAG_LOCKED = int_flag(7);
		public static final int MEM_FLAG_HUGETLB = int_flag(8);
		public static final int MEM_FLAG_POPULATE = int_flag(9);
		public static final int MEM_FLAG_FIXED = int_flag(10);
		public static final int MEM_FLAG_SHARED = int_flag(11);

		private static long __windows_to_os_mem_protect(int flags)
		{
			boolean read = flag_bit(flags, MEM_PROT_READ);
			boolean write = flag_bit(flags, MEM_PROT_WRITE);
			boolean exec = flag_bit(flags, MEM_PROT_EXEC);
			if (read && write && exec)
				return winnt.PAGE_EXECUTE_READWRITE;
			if (read && write && !exec)
				return winnt.PAGE_READWRITE;
			if (read && !write && exec)
				return winnt.PAGE_EXECUTE_READ;
			if (read && !write && !exec)
				return winnt.PAGE_READONLY;
			if (!read && !write && exec)
				return winnt.PAGE_EXECUTE;
			return winnt.PAGE_NOACCESS;
		}

		private static long __windows_to_os_mem_flags(int flags)
		{
			long result = winnt.MEM_COMMIT | winnt.MEM_RESERVE; // 预留内存并立即分配
			if (flag_bit(flags, MEM_FLAG_TOP_DOWN))
				result |= winnt.MEM_TOP_DOWN;
			if (flag_bit(flags, MEM_FLAG_LARGE_PAGES))
				result |= winnt.MEM_LARGE_PAGES;
			if (flag_bit(flags, MEM_FLAG_PHYSICAL))
				result |= winnt.MEM_PHYSICAL;
			if (flag_bit(flags, MEM_FLAG_RESET))
				result |= winnt.MEM_RESET;
			return result;
		}

		private long mem;

		private os_memory(long raw_mem, long size)
		{
			unsafe.write(raw_mem, size);// 内存头写入内存大小
			this.mem = raw_mem + cxx_type.size_t.size();
		}

		private os_memory(long mem)
		{
			this.mem = mem;
		}

		public final long header()
		{
			return mem - cxx_type.size_t.size();
		}

		public final long size()
		{
			return unsafe.read_long(header());
		}

		public final long mem()
		{
			return mem;
		}

		public final void free()
		{
			switch (os.host)
			{
			case windows:
				libkernel32.VirtualFree(header(), 0, winnt.MEM_RELEASE);
			case linux:
			case macos:
			default:
			}
		}

		public final void copy_from(long offset, byte[] bytes)
		{
			unsafe.memcpy(mem + offset, bytes, 0, bytes.length);
		}

		/**
		 * 将本内存视作机器码数组转换为可以调用的函数指针
		 * 
		 * @param machine_code
		 * @param func_type
		 * @return
		 */
		public final MethodHandle func(long offset, call_convention call_conv, function_type func_type)
		{
			return abi.func(mem + offset, call_conv, func_type);
		}

		public final MethodHandle func(long offset, function_type func_type)
		{
			return func(offset, call_convention.host, func_type);
		}

		/**
		 * 分配具有执行权限的内存
		 * 
		 * @param size
		 * @return
		 */
		public static final os_memory os_alloc(long size, int flags)
		{
			switch (os.host)
			{
			case windows:
				return new os_memory(libkernel32.VirtualAlloc(0, size + cxx_type.size_t.size(), __windows_to_os_mem_flags(flags), __windows_to_os_mem_protect(flags)), size);
			case linux:
			case macos:
			default:
				return null;
			}
		}

		public static final os_memory of(byte[] arr, int flags)
		{
			os_memory exec_mem = os_memory.os_alloc(arr.length, flags);
			exec_mem.copy_from(0, arr);
			return exec_mem;
		}

		public static final os_memory of(byte[] arr)
		{
			return of(arr, MEM_PROT_RWX);
		}
	}
}

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
		return pointer.at(unsafe.allocate(size));
	}

	public static final pointer malloc(long num, Class<?> type_clazz)
	{
		return pointer.at(unsafe.allocate(num * java_type.sizeof(type_clazz)), cxx_type.of(type_clazz));
	}

	public static final pointer malloc(long num, cxx_type type)
	{
		return pointer.at(unsafe.allocate(num * cxx_type.sizeof(type)), type);
	}

	public static final pointer malloc(cxx_type type)
	{
		return pointer.at(unsafe.allocate(cxx_type.sizeof(type)), type);
	}

	public static final pointer malloc(cxx_type... types)
	{
		return pointer.at(unsafe.allocate(cxx_type.sizeof(types)));
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
		unsafe.memcpy(null, ptr_src.address(), null, ptr_dest.address(), num);
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
		byte[] bytes = str.getBytes(cs);
		long cstr_addr = unsafe.allocate(bytes.length + 1);
		unsafe.memcpy(bytes, unsafe.ARRAY_OBJECT_BASE_OFFSET, null, cstr_addr, bytes.length);// Java的数组元素并不是从索引0开始的，而是从ARRAY_OBJECT_BASE_OFFSET开始
		unsafe.write(cstr_addr + bytes.length, (byte) 0);
		return pointer.at(cstr_addr, cxx_type._char);
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
		long len = libc.strlen(cstr_addr);
		byte[] bytes = new byte[(int) len];// 不包含结尾的'\0'
		unsafe.memcpy(null, cstr_addr, bytes, unsafe.ARRAY_OBJECT_BASE_OFFSET, bytes.length);
		return new String(bytes, cs);
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
}

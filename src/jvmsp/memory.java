package jvmsp;

import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.util.List;

import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.pointer;
import jvmsp.type.java_type;

public abstract class memory
{

	/**
	 * 字节序
	 */
	public static enum endian
	{
		LITTLE, BIG;
	}

	public static final endian LOCAL_ENDIAN;

	static
	{
		LOCAL_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ? endian.BIG : endian.LITTLE;
	}

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

	public static final void memset(pointer ptr, int value, long bytes)
	{
		unsafe.memset(null, ptr.address(), bytes, (byte) value);
	}

	public static final void memcpy(pointer ptrDest, pointer ptrSrc, long bytes)
	{
		unsafe.memcpy(null, ptrSrc.address(), null, ptrDest.address(), bytes);
	}

	public static final void memcpy(long addrDest, long addrSrc, long bytes)
	{
		unsafe.memcpy(null, addrSrc, null, addrDest, bytes);
	}

	private static Class<?> jdk_internal_foreign_NativeMemorySegmentImpl;
	private static Class<?> jdk_internal_foreign_MemorySessionImpl;

	private static MethodHandle NativeMemorySegmentImpl_constructor;
	private static MethodHandle MemorySessionImpl_heapSession;

	static
	{
		try
		{
			jdk_internal_foreign_NativeMemorySegmentImpl = Class.forName("jdk.internal.foreign.NativeMemorySegmentImpl");
			jdk_internal_foreign_MemorySessionImpl = Class.forName("jdk.internal.foreign.MemorySessionImpl");
			NativeMemorySegmentImpl_constructor = symbols.find_constructor(jdk_internal_foreign_NativeMemorySegmentImpl, long.class, long.class, boolean.class, jdk_internal_foreign_MemorySessionImpl);
			MemorySessionImpl_heapSession = symbols.find_static_method(jdk_internal_foreign_MemorySessionImpl, "heapSession", jdk_internal_foreign_MemorySessionImpl, Object.class);
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 创建GlobalSession
	 * 
	 * @param base 基地址
	 * @return
	 */
	public static final Object memsess(Object base)
	{
		try
		{
			return MemorySessionImpl_heapSession.invoke(base);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("create heap memory session of '" + base + "' failed", ex);
		}
	}

	/**
	 * 机器本地内存的GlobalSession，直接配合native地址使用。
	 */
	public static final Object native_session = memsess(null);

	/**
	 * 为native地址创建MemorySegment对象，该对象为JVM内部FFI相关功能使用。<br>
	 * FFI的MethodHandle的构成为，第一个参数固定为函数指针的MemorySegment，后续参数类型按照函数的形参顺序依次传递。<br>
	 * 其中，参数和返回值中的C++类型不论大小和是否primitive均为MemorySegment类型，Java类型则为对应的Class<?>。
	 * 
	 * @param addr      native地址
	 * @param size      该地址储存的值的内存大小
	 * @param read_only 是否只读
	 * @param scope     地址的域，通常来说就是机器的全局域
	 * @return
	 */
	public static final MemorySegment memseg(long addr, long size, boolean read_only, Object scope)
	{
		try
		{
			return (MemorySegment) NativeMemorySegmentImpl_constructor.invoke(addr, size, read_only, scope);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("create memory segment of native address '" + addr + "' failed", ex);
		}
	}

	public static final MemorySegment memseg(long addr, long size, boolean read_only)
	{
		return memseg(addr, size, read_only, native_session);
	}

	public static final MemorySegment memseg(long addr, long size)
	{
		return memseg(addr, size, false);
	}

	public static final MemorySegment memseg(pointer ptr)
	{
		return memseg(ptr.address(), cxx_type.sizeof(ptr.type()));
	}

	public static final MemorySegment memseg(pointer ptr, long size)
	{
		return memseg(ptr.address(), size);
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

	public static final Class<?> get_list_type(Type listField)
	{
		return reflection.first_generic_class(listField);
	}
}

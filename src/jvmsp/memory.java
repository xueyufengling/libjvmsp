package jvmsp;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.ByteOrder;
import java.util.List;

public abstract class memory {

	/**
	 * 字节序
	 */
	public static enum endian {
		LITTLE, BIG;
	}

	public static final endian LOCAL_ENDIAN;

	static {
		LOCAL_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ? endian.BIG : endian.LITTLE;
	}

	public static final pointer malloc(long size) {
		return pointer.at(unsafe.allocate(size));
	}

	public static final pointer malloc(long size, Class<?> type_cls) {
		return pointer.at(unsafe.allocate(size * java_type.sizeof(type_cls)), type_cls);
	}

	public static final void free(pointer ptr) {
		unsafe.free(ptr.address());
	}

	public static final void memset(pointer ptr, int value, long bytes) {
		unsafe.memset(null, ptr.address(), bytes, (byte) value);
	}

	public static void memcpy(pointer ptrDest, pointer ptrSrc, long bytes) {
		unsafe.memcpy(null, ptrSrc.address(), null, ptrDest.address(), bytes);
	}

	public static void memcpy(long addrDest, long addrSrc, long bytes) {
		unsafe.memcpy(null, addrSrc, null, addrDest, bytes);
	}

	@SuppressWarnings("unchecked")
	public static final <_T> _T[] cat(_T[]... arrays) {
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
	public static final <_T> _T[] cat(_T t1, _T... ts) {
		Object[] result = new Object[ts.length + 1];
		result[0] = t1;
		System.arraycopy(ts, 0, result, 1, ts.length);
		return (_T[]) result;
	}

	@SuppressWarnings("unchecked")
	public static final <_T> _T[] cat(_T t1, _T t2, _T... ts) {
		Object[] result = new Object[ts.length + 2];
		result[0] = t1;
		result[1] = t2;
		System.arraycopy(ts, 0, result, 2, ts.length);
		return (_T[]) result;
	}

	@SuppressWarnings("unchecked")
	public static final <_T> _T[] to_array(Class<_T> c, List<_T> list) {
		_T[] arr = (_T[]) Array.newInstance(c, list.size());
		return list.toArray(arr);
	}

	public static final Class<?> get_list_type(Type listField) {
		return reflection.first_generic_class(listField);
	}
}

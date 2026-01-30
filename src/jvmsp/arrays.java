package jvmsp;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;

public class arrays {
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

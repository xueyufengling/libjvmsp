package jvmsp;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Java类型所占字节数
 */
public abstract class jtype {
	/**
	 * 在32位JVM或64位JVM中UseCompressedOops开启的情况下，对象引用占4字节
	 */
	public static final long object_reference_size;

	static {

		object_reference_size = unsafe.OOP_SIZE;
	}

	/**
	 * 是否是基本类型
	 * 
	 * @param type
	 * @return
	 */
	public static final boolean is_primitive(Class<?> type) {
		return type == void.class || type == byte.class || type == char.class || type == boolean.class || type == short.class || type == int.class || type == float.class || type == long.class || type == double.class;
	}

	/**
	 * 计算类型的大小，任何非基本类型均为引用类型。<br>
	 * 引用类型相当于指针，指向对象的实际内存。引用的值会因为GC而移动。
	 * 
	 * @param type
	 * @return
	 */
	public static final long sizeof(Class<?> type) {
		if (type == void.class || type == byte.class || type == char.class || type == boolean.class)
			return 1;
		else if (type == short.class)
			return 2;
		else if (type == int.class || type == float.class)
			return 4;
		else if (type == long.class || type == double.class)
			return 8;
		else
			return object_reference_size;
	}

	/**
	 * 基本类型包装类的拆箱
	 * 
	 * @param b
	 * @return
	 */
	public static final byte byte_value(Object b) {
		return ((Number) b).byteValue();
	}

	public static final char char_value(Object c) {
		return ((Character) c).charValue();
	}

	public static final boolean boolean_value(Object bool) {
		return ((Boolean) bool).booleanValue();
	}

	public static final short short_value(Object s) {
		return ((Number) s).shortValue();
	}

	public static final int int_value(Object i) {
		return ((Number) i).intValue();
	}

	public static final float float_value(Object f) {
		return ((Number) f).floatValue();
	}

	public static final long long_value(Object l) {
		return ((Number) l).longValue();
	}

	public static final double double_value(Object d) {
		return ((Number) d).doubleValue();
	}

	private static final HashMap<Class<?>, Long> cachedSize = new HashMap<>();

	/**
	 * Java对象所占用内存的大小，无对齐大小。每个Class<?>计算一次后将缓存。
	 * 
	 * @param type
	 * @return
	 */
	public static final long sizeof_object(Class<?> jtype) {
		return cachedSize.computeIfAbsent(jtype, (Class<?> type) -> {
			long max_offset = 0;
			Class<?> max_offset_field_type = null;
			Field[] fields = reflection.get_declared_fields(type);
			for (Field f : fields) {
				if (!Modifier.isStatic(f.getModifiers())) {
					Class<?> field_type = f.getType();
					long current_field_offset = unsafe.object_field_offset(f);
					if (max_offset < current_field_offset) {
						max_offset = current_field_offset;
						max_offset_field_type = field_type;
					}
				}
			}
			return max_offset + sizeof(max_offset_field_type);
		});
	}

	/**
	 * 对齐的大小
	 * 
	 * @param size
	 * @return
	 */
	public static final long padding_size(int size) {
		if (size % 8 != 0)// 对象所占字节数必须是8的整数倍，如果不到则需要padding
			size = (size / 8 + 1) * 8;
		return size;
	}
}

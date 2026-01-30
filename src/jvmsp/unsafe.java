package jvmsp;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

public final class unsafe {
	private static Class<?> class_jdk_internal_misc_Unsafe;
	static Object instance_jdk_internal_misc_Unsafe;

	private static MethodHandle objectFieldOffset$Field;// 没有检查的jdk.internal.misc.Unsafe.objectFieldOffset()
	private static MethodHandle objectFieldOffset$Class$String;
	private static MethodHandle staticFieldBase;
	private static MethodHandle staticFieldOffset;

	private static MethodHandle getAddress;
	private static MethodHandle putAddress;
	private static MethodHandle addressSize;
	private static MethodHandle getUncompressedObject;
	private static MethodHandle allocateMemory;
	private static MethodHandle freeMemory;
	private static MethodHandle setMemory;
	private static MethodHandle copyMemory;
	private static MethodHandle copyMemory0;

	private static MethodHandle defineClass;
	private static MethodHandle allocateInstance;

	private static MethodHandle arrayBaseOffset;
	private static MethodHandle arrayIndexScale;

	private static MethodHandle putReference;
	private static MethodHandle getReference;

	private static MethodHandle putByte;
	private static MethodHandle getByte;

	private static MethodHandle putChar;
	private static MethodHandle getChar;

	private static MethodHandle putBoolean;
	private static MethodHandle getBoolean;

	private static MethodHandle putShort;
	private static MethodHandle getShort;

	private static MethodHandle putInt;
	private static MethodHandle getInt;

	private static MethodHandle putLong;
	private static MethodHandle getLong;

	private static MethodHandle putDouble;
	private static MethodHandle getDouble;

	private static MethodHandle putFloat;
	private static MethodHandle getFloat;

	public static final long INVALID_FIELD_OFFSET = -1;

	public static final int ADDRESS_SIZE;

	public static final int ARRAY_OBJECT_BASE_OFFSET;
	public static final int ARRAY_OBJECT_INDEX_SCALE;

	public static final int ARRAY_BYTE_BASE_OFFSET;
	public static final int ARRAY_BYTE_INDEX_SCALE;
	/**
	 * OOP大小，只会是4或8.<br>
	 * 32位JVM和开启压缩OOP的64位JVM上为4，未开启压缩OOP的64位JVM上为8.<br>
	 */
	public static final long OOP_SIZE;

	static {
		try {
			class_jdk_internal_misc_Unsafe = Class.forName("jdk.internal.misc.Unsafe");
			instance_jdk_internal_misc_Unsafe = symbols.find_static_var(class_jdk_internal_misc_Unsafe, "theUnsafe", class_jdk_internal_misc_Unsafe).get();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		if (instance_jdk_internal_misc_Unsafe == null)
			System.err.println("get jdk.internal.misc.Unsafe instance failed! library will be broken.");

		objectFieldOffset$Field = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "objectFieldOffset", long.class, Field.class);
		objectFieldOffset$Class$String = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "objectFieldOffset", long.class, Class.class, String.class);
		staticFieldBase = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "staticFieldBase", Object.class, Field.class);
		staticFieldOffset = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "staticFieldOffset", long.class, Field.class);

		getAddress = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "getAddress", long.class, Object.class, long.class);
		putAddress = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "putAddress", void.class, Object.class, long.class, long.class);
		addressSize = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "addressSize", int.class);
		getUncompressedObject = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "getUncompressedObject", Object.class, long.class);
		allocateMemory = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "allocateMemory", long.class, long.class);
		freeMemory = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "freeMemory", void.class, long.class);
		setMemory = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "setMemory", void.class, Object.class, long.class, long.class, byte.class);
		copyMemory = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "copyMemory", void.class, Object.class, long.class, Object.class, long.class, long.class);
		copyMemory0 = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "copyMemory0", void.class, Object.class, long.class, Object.class, long.class, long.class);

		defineClass = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "defineClass", Class.class, String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
		allocateInstance = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "allocateInstance", Object.class, Class.class);

		arrayBaseOffset = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "arrayBaseOffset", int.class, Class.class);
		arrayIndexScale = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "arrayIndexScale", int.class, Class.class);

		putReference = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "putReference", void.class, Object.class, long.class, Object.class);
		getReference = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "getReference", Object.class, Object.class, long.class);

		putByte = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "putByte", void.class, Object.class, long.class, byte.class);
		getByte = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "getByte", byte.class, Object.class, long.class);

		putChar = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "putChar", void.class, Object.class, long.class, char.class);
		getChar = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "getChar", char.class, Object.class, long.class);

		putBoolean = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "putBoolean", void.class, Object.class, long.class, boolean.class);
		getBoolean = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "getBoolean", boolean.class, Object.class, long.class);

		putShort = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "putShort", void.class, Object.class, long.class, short.class);
		getShort = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "getShort", short.class, Object.class, long.class);

		putInt = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "putInt", void.class, Object.class, long.class, int.class);
		getInt = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "getInt", int.class, Object.class, long.class);

		putLong = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "putLong", void.class, Object.class, long.class, long.class);
		getLong = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "getLong", long.class, Object.class, long.class);

		putFloat = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "putFloat", void.class, Object.class, long.class, float.class);
		getFloat = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "getFloat", float.class, Object.class, long.class);

		putDouble = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "putDouble", void.class, Object.class, long.class, double.class);
		getDouble = symbols.find_special_method(class_jdk_internal_misc_Unsafe, "getDouble", double.class, Object.class, long.class);

		ADDRESS_SIZE = address_size();
		ARRAY_OBJECT_BASE_OFFSET = array_base_offset(Object[].class);
		ARRAY_OBJECT_INDEX_SCALE = array_index_scale(Object[].class);
		ARRAY_BYTE_BASE_OFFSET = array_base_offset(byte[].class);
		ARRAY_BYTE_INDEX_SCALE = array_index_scale(byte[].class);
		OOP_SIZE = ARRAY_OBJECT_INDEX_SCALE;
	}

	public static final class methods {
		/**
		 * 调用internalUnsafe的方法
		 * 
		 * @param method_name 方法名称
		 * @param arg_types   参数类型
		 * @param args        实参
		 * @return
		 */
		public static final Object call(String method_name, Class<?>[] arg_types, Object... args) {
			try {
				return ObjectManipulator.invoke(unsafe.instance_jdk_internal_misc_Unsafe, method_name, arg_types, args);
			} catch (SecurityException ex) {
				ex.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * 没有任何安全检查的Unsafe.objectFieldOffset方法，可以获取record的成员offset
	 * 
	 * @param field
	 * @return
	 */
	public static long object_field_offset(Field field) {
		try {
			return (long) objectFieldOffset$Field.invoke(instance_jdk_internal_misc_Unsafe, field);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_LONG;
	}

	/**
	 * 获取目标类本身声明的字段的偏移量，其继承的字段偏移量无法获取
	 */
	public static long object_field_offset(Class<?> cls, String field_name) {
		try {
			return (long) objectFieldOffset$Class$String.invoke(instance_jdk_internal_misc_Unsafe, cls, field_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_LONG;
	}

	public static Object static_field_base(Field field) {
		try {
			return staticFieldBase.invoke(instance_jdk_internal_misc_Unsafe, field);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static long static_field_offset(Field field) {
		try {
			return (long) staticFieldOffset.invoke(instance_jdk_internal_misc_Unsafe, field);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_LONG;
	}

	/**
	 * 不调用构造函数创建一个对象
	 * 
	 * @param cls 对象类
	 * @return 分配的对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T allocate(Class<T> cls) {
		try {
			return (T) allocateInstance.invoke(instance_jdk_internal_misc_Unsafe, cls);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return (T) symbols.UNREACHABLE_REFERENCE;
	}

	/**
	 * 内存地址操作
	 * 
	 * @param base
	 * @param offset
	 * @param x
	 */
	public static void store_address(Object base, long offset, long x) {
		try {
			putAddress.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Unsafe的getAddress方法，令人不解的是即便开启压缩OOP，ADDRESS_SIZE也总是8，正常来说应该是4.
	 * 
	 * @param base
	 * @param offset
	 * @return
	 */
	public static long address_of(Object base, long offset) {
		try {
			return (long) getAddress.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_LONG;
	}

	/**
	 * 一个根据是否开启压缩OOP动态决定地址大小的方法，可能这才是正确的获取对象地址的方式。
	 * 
	 * @param base
	 * @param offset
	 * @return
	 */
	public static long native_address_of(Object base, long offset) {
		try {
			if (OOP_SIZE == 4) {
				int addr = (int) getInt.invoke(instance_jdk_internal_misc_Unsafe, base, offset);// 地址是个32位无符号整数，不能直接强转成有符号的long整数。
				if (virtual_machine.ON_64_BIT_JVM)// 64位的JVM上，对象地址却只有4字节，就说明需要向左位移来得到真实地址。
					return oops.decode(addr);
				else
					return cxx_stdtypes.uint_ptr(addr);
			} else
				return (long) getLong.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_LONG;
	}

	public static void store_native_address(Object base, long offset, long addr) {
		try {
			if (OOP_SIZE == 4) {
				if (virtual_machine.ON_64_BIT_JVM)
					putInt.invoke(instance_jdk_internal_misc_Unsafe, base, offset, oops.encode(addr));// 向右位移并丢弃高32位
				else
					putInt.invoke(instance_jdk_internal_misc_Unsafe, base, offset, addr);
			} else
				putLong.invoke(instance_jdk_internal_misc_Unsafe, base, offset, addr);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 获取字段的内存地址
	 * 
	 * @param obj
	 * @param field
	 * @return
	 */
	public static long address_of(Object obj, Field field) {
		if (Modifier.isStatic(field.getModifiers()))
			return address_of(static_field_base(field), static_field_offset(field));
		else
			return address_of(obj, object_field_offset(field));
	}

	/**
	 * 获取字段的内存地址
	 * 
	 * @param obj
	 * @param field
	 * @return
	 */
	public static long address_of(Object obj, String field) {
		Field f = reflection.find_field(obj, field);
		if (Modifier.isStatic(f.getModifiers()))
			return address_of(static_field_base(f), static_field_offset(f));
		else
			return address_of(obj, object_field_offset(obj.getClass(), field));
	}

	public static int address_size() {
		try {
			return (int) addressSize.invoke(instance_jdk_internal_misc_Unsafe);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_INT;
	}

	public static Object uncompressed_object(Object base, long offset) {
		try {
			return getUncompressedObject.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_REFERENCE;
	}

	public static long allocate(long bytes) {
		try {
			return (long) allocateMemory.invoke(instance_jdk_internal_misc_Unsafe, bytes);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_LONG;
	}

	public static void free(long address) {
		try {
			freeMemory.invoke(instance_jdk_internal_misc_Unsafe, address);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static void memset(Object base, long offset, long bytes, byte value) {
		try {
			setMemory.invoke(instance_jdk_internal_misc_Unsafe, base, offset, bytes, value);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static void memcpy(Object src_base, long src_offset, Object dest_base, long dest_offset, long bytes) {
		try {
			copyMemory.invoke(instance_jdk_internal_misc_Unsafe, src_base, src_offset, dest_base, dest_offset, bytes);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static void __memcpy(Object src_base, long src_offset, Object dest_base, long dest_offset, long bytes) {
		try {
			copyMemory0.invoke(instance_jdk_internal_misc_Unsafe, src_base, src_offset, dest_base, dest_offset, bytes);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 获取数组的数据部分起始地址
	 * 
	 * @param array_class
	 * @return
	 */
	public static int array_base_offset(Class<?> array_class) {
		try {
			return (int) arrayBaseOffset.invoke(instance_jdk_internal_misc_Unsafe, array_class);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_INT;
	}

	/**
	 * 获取数组元素占用内存的大小，单位字节。
	 * 
	 * @param array_class
	 * @return
	 */
	public static int array_index_scale(Class<?> array_class) {
		try {
			return (int) arrayIndexScale.invoke(instance_jdk_internal_misc_Unsafe, array_class);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_INT;
	}

	/**
	 * 存引用字段
	 * 
	 * @param o
	 * @param offset
	 * @param x
	 */
	public static void write(Object base, long offset, Object x) {
		try {
			putReference.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static Object read_reference(Object base, long offset) {
		try {
			return getReference.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_REFERENCE;
	}

	public static void write(Object base, long offset, byte x) {
		try {
			putByte.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static byte read_byte(Object base, long offset) {
		try {
			return (byte) getByte.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_BYTE;
	}

	public static void write(Object base, long offset, char x) {
		try {
			putChar.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static char read_char(Object base, long offset) {
		try {
			return (char) getChar.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_CHAR;
	}

	public static void write(Object base, long offset, boolean x) {
		try {
			putBoolean.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static boolean read_bool(Object base, long offset) {
		try {
			return (boolean) getBoolean.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_BOOLEAN;
	}

	public static void write(Object base, long offset, short x) {
		try {
			putShort.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static short read_short(Object base, long offset) {
		try {
			return (short) getShort.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_SHORT;
	}

	public static void write(Object base, long offset, int x) {
		try {
			putInt.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static int read_int(Object base, long offset) {
		try {
			return (int) getInt.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_INT;
	}

	public static void write(Object base, long offset, long x) {
		try {
			putLong.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static long read_long(Object base, long offset) {
		try {
			return (long) getLong.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_LONG;
	}

	public static void write(Object base, long offset, double x) {
		try {
			putDouble.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static double read_double(Object base, long offset) {
		try {
			return (double) getDouble.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_DOUBLE;
	}

	public static void write(Object base, long offset, float x) {
		try {
			putFloat.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}

	public static float read_float(Object base, long offset) {
		try {
			return (float) getFloat.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return symbols.UNREACHABLE_FLOAT;
	}

	/**
	 * 在字节数组中写入float<br>
	 * 一般用于操作缓冲区
	 * 
	 * @param byte_arr
	 * @param offset
	 * @param x
	 */
	public static void write_array(byte[] byte_arr, long arr_idx, float x) {
		write(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx, x);
	}

	public static void write_array(byte[] byte_arr, long arr_idx, int x) {
		write(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx, x);
	}

	public static void write_array(byte[] byte_arr, long arr_idx, short x) {
		write(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx, x);
	}

	public static void write_array(byte[] byte_arr, long arr_idx, long x) {
		write(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx, x);
	}

	public static void write_array(byte[] byte_arr, long arr_idx, double x) {
		write(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx, x);
	}

	/**
	 * 在字节数组中读取float<br>
	 * 一般用于操作缓冲区
	 * 
	 * @param byte_arr
	 * @param arr_idx
	 * @param x
	 * @return
	 */
	public static float read_array_float(byte[] byte_arr, long arr_idx) {
		return read_float(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx);
	}

	public static int read_array_int(byte[] byte_arr, long arr_idx) {
		return read_int(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx);
	}

	public static short read_array_short(byte[] byte_arr, long arr_idx) {
		return read_short(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx);
	}

	public static long read_array_long(byte[] byte_arr, long arr_idx) {
		return read_long(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx);
	}

	public static double read_array_double(byte[] byte_arr, long arr_idx) {
		return read_double(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx);
	}

	/**
	 * 无视访问权限和修饰符修改Object值，如果是静态成员忽略obj参数.此方法对于HiddenClass和record同样有效
	 * 
	 * @param obj   要修改值的对象
	 * @param field 要修改的Field
	 * @param value 要修改的值
	 * @return
	 */
	public static void write(Object obj, Field field, Object value) {
		if (Modifier.isStatic(field.getModifiers()))
			write(static_field_base(field), static_field_offset(field), value);
		else
			write(obj, object_field_offset(field), value);
	}

	public static void write(Object obj, String field, Object value) {
		Field f = reflection.find_field(obj, field);
		if (Modifier.isStatic(f.getModifiers()))
			write(static_field_base(f), static_field_offset(f), value);
		else
			write(obj, object_field_offset(f), value);
	}

	public static void write_member(Object obj, String field, Object value) {
		write(obj, object_field_offset(obj.getClass(), field), value);
	}

	public static void write_static(Class<?> cls, String field, Object value) {
		Field f = reflection.find_field(cls, field);
		write(static_field_base(f), static_field_offset(f), value);
	}

	public static Object read_reference(Object obj, Field field) {
		if (Modifier.isStatic(field.getModifiers()))
			return read_reference(static_field_base(field), static_field_offset(field));
		else
			return read_reference(obj, object_field_offset(field));
	}

	public static Object read_reference(Object obj, String field) {
		Field f = reflection.find_field(obj, field);
		if (Modifier.isStatic(f.getModifiers()))
			return read_reference(static_field_base(f), static_field_offset(f));
		else
			return read_reference(obj, object_field_offset(f));
	}

	public static Object read_member_reference(Object obj, String field) {
		return read_reference(obj, object_field_offset(obj.getClass(), field));
	}

	public static Object read_static_reference(Class<?> cls, String field) {
		Field f = reflection.find_field(cls, field);
		return read_reference(static_field_base(f), static_field_offset(f));
	}

	public static void write(Object obj, Field field, long value) {
		if (Modifier.isStatic(field.getModifiers()))
			write(static_field_base(field), static_field_offset(field), value);
		else
			write(obj, object_field_offset(field), value);
	}

	public static void write(Object obj, String field, long value) {
		write(obj, reflection.find_field(obj, field), value);
	}

	public static long read_long(Object obj, Field field) {
		if (Modifier.isStatic(field.getModifiers()))
			return read_long(static_field_base(field), static_field_offset(field));
		else
			return read_long(obj, object_field_offset(field));
	}

	public static long read_long(Object obj, String field) {
		Field f = reflection.find_field(obj, field);
		if (Modifier.isStatic(f.getModifiers()))
			return read_long(static_field_base(f), static_field_offset(f));
		else
			return read_long(obj, object_field_offset(f));
	}

	/**
	 * 获取指定对象声明的类成员long
	 * 
	 * @param obj
	 * @param field
	 * @return
	 */
	public static long read_member_long(Object obj, String field) {
		return read_long(obj, object_field_offset(obj.getClass(), field));
	}

	public static void write_member_long(Object obj, String field, long value) {
		write(obj, object_field_offset(obj.getClass(), field), value);
	}

	public static void write(Object obj, Field field, boolean value) {
		if (Modifier.isStatic(field.getModifiers()))
			write(static_field_base(field), static_field_offset(field), value);
		else
			write(obj, object_field_offset(field), value);
	}

	public static void write(Object obj, String field, boolean value) {
		write(obj, reflection.find_field(obj, field), value);
	}

	public static void write_member(Object obj, String field, boolean value) {
		write(obj, object_field_offset(obj.getClass(), field), value);
	}

	public static void write_static(Class<?> cls, String field, boolean value) {
		Field f = reflection.find_field(cls, field);
		write(static_field_base(f), static_field_offset(f), value);
	}

	public static boolean read_bool(Object obj, Field field) {
		if (Modifier.isStatic(field.getModifiers()))
			return read_bool(static_field_base(field), static_field_offset(field));
		else
			return read_bool(obj, object_field_offset(field));
	}

	public static boolean read_bool(Object obj, String field) {
		Field f = reflection.find_field(obj, field);
		if (Modifier.isStatic(f.getModifiers()))
			return read_bool(static_field_base(f), static_field_offset(f));
		else
			return read_bool(obj, object_field_offset(f));
	}

	public static boolean read_member_bool(Object obj, String field) {
		return read_bool(obj, object_field_offset(obj.getClass(), field));
	}

	public static boolean read_static_bool(Class<?> cls, String field) {
		Field f = reflection.find_field(cls, field);
		return read_bool(static_field_base(f), static_field_offset(f));
	}

	public static void write(Object obj, Field field, int value) {
		if (Modifier.isStatic(field.getModifiers()))
			write(static_field_base(field), static_field_offset(field), value);
		else
			write(obj, object_field_offset(field), value);
	}

	public static void write(Object obj, String field, int value) {
		write(obj, reflection.find_field(obj, field), value);
	}

	public static void write_member(Object obj, String field, int value) {
		write(obj, object_field_offset(obj.getClass(), field), value);
	}

	public static void write_static(Class<?> cls, String field, int value) {
		Field f = reflection.find_field(cls, field);
		write(static_field_base(f), static_field_offset(f), value);
	}

	public static int read_int(Object obj, Field field) {
		if (Modifier.isStatic(field.getModifiers()))
			return read_int(static_field_base(field), static_field_offset(field));
		else
			return read_int(obj, object_field_offset(field));
	}

	public static int read_int(Object obj, String field) {
		Field f = reflection.find_field(obj, field);
		if (Modifier.isStatic(f.getModifiers()))
			return read_int(static_field_base(f), static_field_offset(f));
		else
			return read_int(obj, object_field_offset(f));
	}

	public static int read_member_int(Object obj, String field) {
		return read_int(obj, object_field_offset(obj.getClass(), field));
	}

	public static void write(Object obj, Field field, double value) {
		if (Modifier.isStatic(field.getModifiers()))
			write(static_field_base(field), static_field_offset(field), value);
		else
			write(obj, object_field_offset(field), value);
	}

	public static void write(Object obj, String field, double value) {
		write(obj, reflection.find_field(obj, field), value);
	}

	public static double read_double(Object obj, Field field) {
		if (Modifier.isStatic(field.getModifiers()))
			return read_double(static_field_base(field), static_field_offset(field));
		else
			return read_double(obj, object_field_offset(field));
	}

	public static double read_double(Object obj, String field) {
		Field f = reflection.find_field(obj, field);
		if (Modifier.isStatic(f.getModifiers()))
			return read_double(static_field_base(f), static_field_offset(f));
		else
			return read_double(obj, object_field_offset(f));
	}

	public static void write(Object obj, Field field, float value) {
		if (Modifier.isStatic(field.getModifiers()))
			write(static_field_base(field), static_field_offset(field), value);
		else
			write(obj, object_field_offset(field), value);
	}

	public static void write(Object obj, String field, float value) {
		write(obj, reflection.find_field(obj, field), value);
	}

	public static float read_float(Object obj, Field field) {
		if (Modifier.isStatic(field.getModifiers()))
			return read_float(static_field_base(field), static_field_offset(field));
		else
			return read_float(obj, object_field_offset(field));
	}

	public static float read_float(Object obj, String field) {
		Field f = reflection.find_field(obj, field);
		if (Modifier.isStatic(f.getModifiers()))
			return read_float(static_field_base(f), static_field_offset(f));
		else
			return read_float(obj, object_field_offset(f));
	}

	/**
	 * 直接令loader加载指定class<br>
	 * 绕过类加载器： 直接向JVM注册类，不经过ClassLoader体系.<br>
	 * 无依赖解析：不自动加载依赖类，如果依赖类不存在则直接抛出java.lang.NoClassDefFoundError<br>
	 * 无安全检查： 跳过字节码验证、包可见性检查等<br>
	 * 内存驻留： 定义的类不会被 GC 回收<br>
	 * 
	 * @param name
	 * @param b
	 * @param off
	 * @param len
	 * @param loader
	 * @param protection_domain
	 * @return
	 */
	public static Class<?> define_class(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protection_domain) {
		try {
			return (Class<?>) defineClass.invoke(instance_jdk_internal_misc_Unsafe, name, b, off, len, loader, protection_domain);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}
}

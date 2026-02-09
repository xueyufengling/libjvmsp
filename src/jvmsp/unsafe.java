package jvmsp;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

import jvmsp.type.cxx_type;
import jvmsp.type.java_type;

/**
 * jdk.internal.misc.Unsafe的相关操作。 无空指针及参数检查，需要自行确保参数正确性确保不会引发JVM崩溃 注：对于final修饰的变量，基本类型和String会内联，因此修改变量内存无效
 */
public final class unsafe
{
	private static Class<?> jdk_internal_misc_Unsafe;
	static Object instance_jdk_internal_misc_Unsafe;

	private static MethodHandle objectFieldOffset0;// 没有检查的jdk.internal.misc.Unsafe.objectFieldOffset()
	private static MethodHandle objectFieldOffset1;
	private static MethodHandle staticFieldBase0;
	private static MethodHandle staticFieldOffset0;

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

	private static MethodHandle arrayBaseOffset0;
	private static MethodHandle arrayIndexScale0;

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

	// cmpxchg & cas
	private static MethodHandle compareAndSetReference;
	private static MethodHandle compareAndExchangeReference;

	private static MethodHandle compareAndSetByte;
	private static MethodHandle compareAndExchangeByte;

	private static MethodHandle compareAndSetChar;
	private static MethodHandle compareAndExchangeChar;

	private static MethodHandle compareAndSetBoolean;
	private static MethodHandle compareAndExchangeBoolean;

	private static MethodHandle compareAndSetShort;
	private static MethodHandle compareAndExchangeShort;

	private static MethodHandle compareAndSetInt;
	private static MethodHandle compareAndExchangeInt;

	private static MethodHandle compareAndSetLong;
	private static MethodHandle compareAndExchangeLong;

	private static MethodHandle compareAndSetDouble;
	private static MethodHandle compareAndExchangeDouble;

	private static MethodHandle compareAndSetFloat;
	private static MethodHandle compareAndExchangeFloat;

	private static MethodHandle loadFence;
	private static MethodHandle storeFence;
	private static MethodHandle fullFence;
	private static MethodHandle loadLoadFence;
	private static MethodHandle storeStoreFence;

	private static MethodHandle shouldBeInitialized0;
	private static MethodHandle ensureClassInitialized0;

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

	static
	{
		try
		{
			jdk_internal_misc_Unsafe = Class.forName("jdk.internal.misc.Unsafe");
			instance_jdk_internal_misc_Unsafe = symbols.find_static_var(jdk_internal_misc_Unsafe, "theUnsafe", jdk_internal_misc_Unsafe).get();
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
		}
		if (instance_jdk_internal_misc_Unsafe == null)
			throw new java.lang.InternalError("get jdk.internal.misc.Unsafe instance failed! library will be broken");

		objectFieldOffset0 = symbols.find_special_method(jdk_internal_misc_Unsafe, "objectFieldOffset0", long.class, Field.class);
		objectFieldOffset1 = symbols.find_special_method(jdk_internal_misc_Unsafe, "objectFieldOffset1", long.class, Class.class, String.class);
		staticFieldBase0 = symbols.find_special_method(jdk_internal_misc_Unsafe, "staticFieldBase0", Object.class, Field.class);
		staticFieldOffset0 = symbols.find_special_method(jdk_internal_misc_Unsafe, "staticFieldOffset0", long.class, Field.class);

		getAddress = symbols.find_special_method(jdk_internal_misc_Unsafe, "getAddress", long.class, Object.class, long.class);
		putAddress = symbols.find_special_method(jdk_internal_misc_Unsafe, "putAddress", void.class, Object.class, long.class, long.class);
		addressSize = symbols.find_special_method(jdk_internal_misc_Unsafe, "addressSize", int.class);
		getUncompressedObject = symbols.find_special_method(jdk_internal_misc_Unsafe, "getUncompressedObject", Object.class, long.class);
		allocateMemory = symbols.find_special_method(jdk_internal_misc_Unsafe, "allocateMemory", long.class, long.class);
		freeMemory = symbols.find_special_method(jdk_internal_misc_Unsafe, "freeMemory", void.class, long.class);
		setMemory = symbols.find_special_method(jdk_internal_misc_Unsafe, "setMemory", void.class, Object.class, long.class, long.class, byte.class);
		copyMemory = symbols.find_special_method(jdk_internal_misc_Unsafe, "copyMemory", void.class, Object.class, long.class, Object.class, long.class, long.class);
		copyMemory0 = symbols.find_special_method(jdk_internal_misc_Unsafe, "copyMemory0", void.class, Object.class, long.class, Object.class, long.class, long.class);

		defineClass = symbols.find_special_method(jdk_internal_misc_Unsafe, "defineClass", Class.class, String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class);
		allocateInstance = symbols.find_special_method(jdk_internal_misc_Unsafe, "allocateInstance", Object.class, Class.class);

		arrayBaseOffset0 = symbols.find_special_method(jdk_internal_misc_Unsafe, "arrayBaseOffset0", int.class, Class.class);
		arrayIndexScale0 = symbols.find_special_method(jdk_internal_misc_Unsafe, "arrayIndexScale0", int.class, Class.class);

		// 内存读写
		putReference = symbols.find_special_method(jdk_internal_misc_Unsafe, "putReference", void.class, Object.class, long.class, Object.class);
		getReference = symbols.find_special_method(jdk_internal_misc_Unsafe, "getReference", Object.class, Object.class, long.class);

		putByte = symbols.find_special_method(jdk_internal_misc_Unsafe, "putByte", void.class, Object.class, long.class, byte.class);
		getByte = symbols.find_special_method(jdk_internal_misc_Unsafe, "getByte", byte.class, Object.class, long.class);

		putChar = symbols.find_special_method(jdk_internal_misc_Unsafe, "putChar", void.class, Object.class, long.class, char.class);
		getChar = symbols.find_special_method(jdk_internal_misc_Unsafe, "getChar", char.class, Object.class, long.class);

		putBoolean = symbols.find_special_method(jdk_internal_misc_Unsafe, "putBoolean", void.class, Object.class, long.class, boolean.class);
		getBoolean = symbols.find_special_method(jdk_internal_misc_Unsafe, "getBoolean", boolean.class, Object.class, long.class);

		putShort = symbols.find_special_method(jdk_internal_misc_Unsafe, "putShort", void.class, Object.class, long.class, short.class);
		getShort = symbols.find_special_method(jdk_internal_misc_Unsafe, "getShort", short.class, Object.class, long.class);

		putInt = symbols.find_special_method(jdk_internal_misc_Unsafe, "putInt", void.class, Object.class, long.class, int.class);
		getInt = symbols.find_special_method(jdk_internal_misc_Unsafe, "getInt", int.class, Object.class, long.class);

		putLong = symbols.find_special_method(jdk_internal_misc_Unsafe, "putLong", void.class, Object.class, long.class, long.class);
		getLong = symbols.find_special_method(jdk_internal_misc_Unsafe, "getLong", long.class, Object.class, long.class);

		putFloat = symbols.find_special_method(jdk_internal_misc_Unsafe, "putFloat", void.class, Object.class, long.class, float.class);
		getFloat = symbols.find_special_method(jdk_internal_misc_Unsafe, "getFloat", float.class, Object.class, long.class);

		putDouble = symbols.find_special_method(jdk_internal_misc_Unsafe, "putDouble", void.class, Object.class, long.class, double.class);
		getDouble = symbols.find_special_method(jdk_internal_misc_Unsafe, "getDouble", double.class, Object.class, long.class);

		// cmpxchg及cas
		compareAndSetReference = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndSetReference", boolean.class, Object.class, long.class, Object.class, Object.class);
		compareAndExchangeReference = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndExchangeReference", Object.class, Object.class, long.class, Object.class, Object.class);

		compareAndSetByte = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndSetByte", boolean.class, Object.class, long.class, byte.class, byte.class);
		compareAndExchangeByte = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndExchangeByte", byte.class, Object.class, long.class, byte.class, byte.class);

		compareAndSetChar = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndSetChar", boolean.class, Object.class, long.class, char.class, char.class);
		compareAndExchangeChar = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndExchangeChar", char.class, Object.class, long.class, char.class, char.class);

		compareAndSetBoolean = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndSetBoolean", boolean.class, Object.class, long.class, boolean.class, boolean.class);
		compareAndExchangeBoolean = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndExchangeBoolean", boolean.class, Object.class, long.class, boolean.class, boolean.class);

		compareAndSetShort = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndSetShort", boolean.class, Object.class, long.class, short.class, short.class);
		compareAndExchangeShort = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndExchangeShort", short.class, Object.class, long.class, short.class, short.class);

		compareAndSetInt = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndSetInt", boolean.class, Object.class, long.class, int.class, int.class);
		compareAndExchangeInt = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndExchangeInt", int.class, Object.class, long.class, int.class, int.class);

		compareAndSetLong = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndSetLong", boolean.class, Object.class, long.class, long.class, long.class);
		compareAndExchangeLong = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndExchangeLong", long.class, Object.class, long.class, long.class, long.class);

		compareAndSetFloat = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndSetFloat", boolean.class, Object.class, long.class, float.class, float.class);
		compareAndExchangeFloat = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndExchangeFloat", float.class, Object.class, long.class, float.class, float.class);

		compareAndSetDouble = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndSetDouble", boolean.class, Object.class, long.class, double.class, double.class);
		compareAndExchangeDouble = symbols.find_special_method(jdk_internal_misc_Unsafe, "compareAndExchangeDouble", double.class, Object.class, long.class, double.class, double.class);

		loadFence = symbols.find_special_method(jdk_internal_misc_Unsafe, "loadFence", void.class);
		storeFence = symbols.find_special_method(jdk_internal_misc_Unsafe, "storeFence", void.class);
		fullFence = symbols.find_special_method(jdk_internal_misc_Unsafe, "fullFence", void.class);
		loadLoadFence = symbols.find_special_method(jdk_internal_misc_Unsafe, "loadLoadFence", void.class);
		storeStoreFence = symbols.find_special_method(jdk_internal_misc_Unsafe, "storeStoreFence", void.class);

		shouldBeInitialized0 = symbols.find_special_method(jdk_internal_misc_Unsafe, "shouldBeInitialized0", boolean.class, Class.class);
		ensureClassInitialized0 = symbols.find_special_method(jdk_internal_misc_Unsafe, "ensureClassInitialized0", void.class, Class.class);

		ADDRESS_SIZE = address_size();
		ARRAY_OBJECT_BASE_OFFSET = array_base_offset(Object[].class);
		ARRAY_OBJECT_INDEX_SCALE = array_index_scale(Object[].class);
		ARRAY_BYTE_BASE_OFFSET = array_base_offset(byte[].class);
		ARRAY_BYTE_INDEX_SCALE = array_index_scale(byte[].class);
		OOP_SIZE = ARRAY_OBJECT_INDEX_SCALE;
	}

	public static final class methods
	{
		/**
		 * 调用internalUnsafe的方法
		 * 
		 * @param method_name 方法名称
		 * @param arg_types   参数类型
		 * @param args        实参
		 * @return
		 */
		public static final Object call(String method_name, Class<?>[] arg_types, Object... args)
		{
			try
			{
				return reflection.call(unsafe.instance_jdk_internal_misc_Unsafe, method_name, arg_types, args);
			}
			catch (SecurityException ex)
			{
				throw new java.lang.InternalError("call jdk.internal.misc.Unsafe." + method_name + "() failed", ex);
			}
		}
	}

	/**
	 * 没有任何安全检查的Unsafe.objectFieldOffset方法，可以获取record的成员offset
	 * 
	 * @param field
	 * @return
	 */
	public static final long object_field_offset(Field field)
	{
		try
		{
			return (long) objectFieldOffset0.invoke(instance_jdk_internal_misc_Unsafe, field);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get object field offset of '" + field.toString() + "' failed", ex);
		}
	}

	/**
	 * 获取目标类本身声明的字段的偏移量，其继承的字段偏移量无法获取
	 */
	public static final long object_field_offset(Class<?> clazz, String field_name)
	{
		try
		{
			return (long) objectFieldOffset1.invoke(instance_jdk_internal_misc_Unsafe, clazz, field_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get object field offset of '" + field_name + "' failed", ex);
		}
	}

	public static final Object static_field_base(Field field)
	{
		try
		{
			return staticFieldBase0.invoke(instance_jdk_internal_misc_Unsafe, field);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get static field base of '" + field.toString() + "' failed", ex);
		}
	}

	public static final long static_field_offset(Field field)
	{
		try
		{
			return (long) staticFieldOffset0.invoke(instance_jdk_internal_misc_Unsafe, field);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get static field offset of '" + field.toString() + "' failed", ex);
		}
	}

	/**
	 * 不调用构造函数创建一个对象
	 * 
	 * @param clazz 对象类
	 * @return 分配的对象
	 */
	public static final <T> T allocate(Class<T> clazz)
	{
		try
		{
			return (T) allocateInstance.invoke(instance_jdk_internal_misc_Unsafe, clazz);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("allocate object of '" + clazz + "' failed", ex);
		}
	}

	/**
	 * 内存地址操作
	 * 
	 * @param base
	 * @param offset
	 * @param x
	 */
	public static final void store_address(Object base, long offset, long x)
	{
		try
		{
			putAddress.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("store jvm address of '" + base + "' failed", ex);
		}
	}

	/**
	 * Unsafe的getAddress方法，令人不解的是即便开启压缩OOP，ADDRESS_SIZE也总是8，正常来说应该是4.
	 * 
	 * @param base
	 * @param offset
	 * @return
	 */
	public static final long address_of(Object base, long offset)
	{
		try
		{
			return (long) getAddress.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get jvm address of '" + base + "' failed", ex);
		}
	}

	/**
	 * 一个根据是否开启压缩OOP动态决定地址大小的方法，可能这才是正确的获取对象地址的方式。
	 * 
	 * @param base
	 * @param offset
	 * @return
	 */
	public static final long native_address_of(Object base, long offset)
	{
		try
		{
			if (OOP_SIZE == 4)
			{
				int addr = (int) getInt.invoke(instance_jdk_internal_misc_Unsafe, base, offset);// 地址是个32位无符号整数，不能直接强转成有符号的long整数。
				if (virtual_machine.ON_64_BIT_JVM)// 64位的JVM上，对象地址却只有4字节，就说明需要向左位移来得到真实地址。
					return java_type.decode_oop(addr);
				else
					return cxx_type.uint_ptr(addr);
			}
			else
				return (long) getLong.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get native address of '" + base + "' failed", ex);
		}
	}

	public static final void store_native_address(Object base, long offset, long addr)
	{
		try
		{
			if (OOP_SIZE == 4)
			{
				if (virtual_machine.ON_64_BIT_JVM)
					putInt.invoke(instance_jdk_internal_misc_Unsafe, base, offset, java_type.encode_oop(addr));// 向右位移并丢弃高32位
				else
					putInt.invoke(instance_jdk_internal_misc_Unsafe, base, offset, addr);
			}
			else
				putLong.invoke(instance_jdk_internal_misc_Unsafe, base, offset, addr);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("store native address of '" + base + "' failed", ex);
		}
	}

	/**
	 * 获取字段的内存地址
	 * 
	 * @param obj
	 * @param field
	 * @return
	 */
	public static final long address_of(Object obj, Field field)
	{
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
	public static final long address_of(Object obj, String field)
	{
		return address_of(obj, reflection.find_declared_field(obj, field));
	}

	public static final int address_size()
	{
		try
		{
			return (int) addressSize.invoke(instance_jdk_internal_misc_Unsafe);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get address size failed", ex);
		}
	}

	public static final Object uncompressed_object(Object base, long offset)
	{
		try
		{
			return getUncompressedObject.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get uncompressed object failed", ex);
		}
	}

	public static final long allocate(long bytes)
	{
		try
		{
			return (long) allocateMemory.invoke(instance_jdk_internal_misc_Unsafe, bytes);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("allocate memory failed, size = " + bytes, ex);
		}
	}

	public static final void free(long address)
	{
		try
		{
			freeMemory.invoke(instance_jdk_internal_misc_Unsafe, address);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("free memory failed, address = " + address, ex);
		}
	}

	public static final void memset(Object base, long offset, long bytes, byte value)
	{
		try
		{
			setMemory.invoke(instance_jdk_internal_misc_Unsafe, base, offset, bytes, value);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("memset failed", ex);
		}
	}

	public static final void memcpy(Object src_base, long src_offset, Object dest_base, long dest_offset, long bytes)
	{
		try
		{
			copyMemory.invoke(instance_jdk_internal_misc_Unsafe, src_base, src_offset, dest_base, dest_offset, bytes);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("memcpy failed", ex);
		}
	}

	public static final void __memcpy(Object src_base, long src_offset, Object dest_base, long dest_offset, long bytes)
	{
		try
		{
			copyMemory0.invoke(instance_jdk_internal_misc_Unsafe, src_base, src_offset, dest_base, dest_offset, bytes);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("raw memcpy failed", ex);
		}
	}

	/**
	 * 获取数组的数据部分起始地址
	 * 
	 * @param array_class
	 * @return
	 */
	public static final int array_base_offset(Class<?> array_class)
	{
		try
		{
			return (int) arrayBaseOffset0.invoke(instance_jdk_internal_misc_Unsafe, array_class);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get array base offset of '" + array_class + "' failed", ex);
		}
	}

	/**
	 * 获取数组元素占用内存的大小，单位字节。
	 * 
	 * @param array_class
	 * @return
	 */
	public static final int array_index_scale(Class<?> array_class)
	{
		try
		{
			return (int) arrayIndexScale0.invoke(instance_jdk_internal_misc_Unsafe, array_class);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get array index scale of '" + array_class + "' failed", ex);
		}
	}

	/**
	 * 存引用字段
	 * 
	 * @param o
	 * @param offset
	 * @param x
	 */
	public static final void write(Object base, long offset, Object x)
	{
		try
		{
			putReference.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("put reference '" + x + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final void write(long native_addr, Object x)
	{
		write(null, native_addr, x);
	}

	public static final Object read_reference(Object base, long offset)
	{
		try
		{
			return getReference.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get reference at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final Object read_reference(long native_addr)
	{
		return read_reference(null, native_addr);
	}

	public static final void write(Object base, long offset, byte x)
	{
		try
		{
			putByte.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("put byte '" + x + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final void write(long native_addr, byte x)
	{
		write(null, native_addr, x);
	}

	public static final byte read_byte(Object base, long offset)
	{
		try
		{
			return (byte) getByte.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get byte at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final byte read_byte(long native_addr)
	{
		return read_byte(null, native_addr);
	}

	public static final void write(Object base, long offset, char x)
	{
		try
		{
			putChar.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("put char '" + x + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final void write(long native_addr, char x)
	{
		write(null, native_addr, x);
	}

	public static final char read_char(Object base, long offset)
	{
		try
		{
			return (char) getChar.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get char at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final char read_char(long native_addr)
	{
		return read_char(null, native_addr);
	}

	public static final void write(Object base, long offset, boolean x)
	{
		try
		{
			putBoolean.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("put bool '" + x + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final void write(long native_addr, boolean x)
	{
		write(null, native_addr, x);
	}

	public static final boolean read_bool(Object base, long offset)
	{
		try
		{
			return (boolean) getBoolean.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get bool at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final boolean read_bool(long native_addr)
	{
		return read_bool(null, native_addr);
	}

	public static final void write(Object base, long offset, short x)
	{
		try
		{
			putShort.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("put short '" + x + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final void write(long native_addr, short x)
	{
		write(null, native_addr, x);
	}

	public static final short read_short(Object base, long offset)
	{
		try
		{
			return (short) getShort.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get short at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final short read_short(long native_addr)
	{
		return read_short(null, native_addr);
	}

	public static final void write(Object base, long offset, int x)
	{
		try
		{
			putInt.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("put int '" + x + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final void write(long native_addr, int x)
	{
		write(null, native_addr, x);
	}

	public static final int read_int(Object base, long offset)
	{
		try
		{
			return (int) getInt.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get int at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final int read_int(long native_addr)
	{
		return read_int(null, native_addr);
	}

	public static final void write(Object base, long offset, long x)
	{
		try
		{
			putLong.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("put long '" + x + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final void write(long native_addr, long x)
	{
		write(null, native_addr, x);
	}

	public static final long read_long(Object base, long offset)
	{
		try
		{
			return (long) getLong.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get long at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final long read_long(long native_addr)
	{
		return read_long(null, native_addr);
	}

	public static final void write(Object base, long offset, double x)
	{
		try
		{
			putDouble.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("put double '" + x + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final void write(long native_addr, double x)
	{
		write(null, native_addr, x);
	}

	public static final double read_double(Object base, long offset)
	{
		try
		{
			return (double) getDouble.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get double at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final double read_double(long native_addr)
	{
		return read_double(null, native_addr);
	}

	public static final void write(Object base, long offset, float x)
	{
		try
		{
			putFloat.invoke(instance_jdk_internal_misc_Unsafe, base, offset, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("put float '" + x + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final void write(long native_addr, float x)
	{
		write(null, native_addr, x);
	}

	public static final float read_float(Object base, long offset)
	{
		try
		{
			return (float) getFloat.invoke(instance_jdk_internal_misc_Unsafe, base, offset);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get float at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final float read_float(long native_addr)
	{
		return read_float(null, native_addr);
	}

	// cmpxchg & cas
	public static final boolean cas(Object base, long offset, Object expected, Object x)
	{
		try
		{
			return (boolean) compareAndSetReference.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and swap reference '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final boolean cas(long native_addr, Object expected, Object x)
	{
		return cas(null, native_addr, expected, x);
	}

	public static final Object cmpxchg(Object base, long offset, Object expected, Object x)
	{
		try
		{
			return (Object) compareAndExchangeReference.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and exchange reference '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final Object cmpxchg(long native_addr, Object expected, Object x)
	{
		return cmpxchg(null, native_addr, expected, x);
	}

	public static final boolean cas(Object base, long offset, byte expected, byte x)
	{
		try
		{
			return (boolean) compareAndSetByte.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and swap byte '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final boolean cas(long native_addr, byte expected, byte x)
	{
		return cas(null, native_addr, expected, x);
	}

	public static final byte cmpxchg(Object base, long offset, byte expected, byte x)
	{
		try
		{
			return (byte) compareAndExchangeByte.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and exchange byte '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final byte cmpxchg(long native_addr, byte expected, byte x)
	{
		return cmpxchg(null, native_addr, expected, x);
	}

	public static final boolean cas(Object base, long offset, char expected, char x)
	{
		try
		{
			return (boolean) compareAndSetChar.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and swap char '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final boolean cas(long native_addr, char expected, char x)
	{
		return cas(null, native_addr, expected, x);
	}

	public static final char cmpxchg(Object base, long offset, char expected, char x)
	{
		try
		{
			return (char) compareAndExchangeChar.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and exchange char '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final char cmpxchg(long native_addr, char expected, char x)
	{
		return cmpxchg(null, native_addr, expected, x);
	}

	public static final boolean cas(Object base, long offset, short expected, short x)
	{
		try
		{
			return (boolean) compareAndSetShort.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and swap short '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final boolean cas(long native_addr, short expected, short x)
	{
		return cas(null, native_addr, expected, x);
	}

	public static final short cmpxchg(Object base, long offset, short expected, short x)
	{
		try
		{
			return (short) compareAndExchangeShort.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and exchange short '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final short cmpxchg(long native_addr, short expected, short x)
	{
		return cmpxchg(null, native_addr, expected, x);
	}

	public static final boolean cas(Object base, long offset, int expected, int x)
	{
		try
		{
			return (boolean) compareAndSetInt.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and swap int '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final boolean cas(long native_addr, int expected, int x)
	{
		return cas(null, native_addr, expected, x);
	}

	public static final int cmpxchg(Object base, long offset, int expected, int x)
	{
		try
		{
			return (int) compareAndExchangeInt.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and exchange int '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final int cmpxchg(long native_addr, int expected, int x)
	{
		return cmpxchg(null, native_addr, expected, x);
	}

	public static final boolean cas(Object base, long offset, long expected, long x)
	{
		try
		{
			return (boolean) compareAndSetLong.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and swap long '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final boolean cas(long native_addr, long expected, long x)
	{
		return cas(null, native_addr, expected, x);
	}

	public static final long cmpxchg(Object base, long offset, long expected, long x)
	{
		try
		{
			return (long) compareAndExchangeLong.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and exchange long '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final long cmpxchg(long native_addr, long expected, long x)
	{
		return cmpxchg(null, native_addr, expected, x);
	}

	public static final boolean cas(Object base, long offset, boolean expected, boolean x)
	{
		try
		{
			return (boolean) compareAndSetBoolean.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and swap bool '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final boolean cas(long native_addr, boolean expected, boolean x)
	{
		return cas(null, native_addr, expected, x);
	}

	public static final boolean cmpxchg(Object base, long offset, boolean expected, boolean x)
	{
		try
		{
			return (boolean) compareAndExchangeBoolean.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and exchange bool '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final boolean cmpxchg(long native_addr, boolean expected, boolean x)
	{
		return cmpxchg(null, native_addr, expected, x);
	}

	public static final boolean cas(Object base, long offset, float expected, float x)
	{
		try
		{
			return (boolean) compareAndSetFloat.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and swap float '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final boolean cas(long native_addr, float expected, float x)
	{
		return cas(null, native_addr, expected, x);
	}

	public static final float cmpxchg(Object base, long offset, float expected, float x)
	{
		try
		{
			return (float) compareAndExchangeFloat.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and exchange float '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final float cmpxchg(long native_addr, float expected, float x)
	{
		return cmpxchg(null, native_addr, expected, x);
	}

	public static final boolean cas(Object base, long offset, double expected, double x)
	{
		try
		{
			return (boolean) compareAndSetDouble.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and swap double '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final boolean cas(long native_addr, double expected, double x)
	{
		return cas(null, native_addr, expected, x);
	}

	public static final double cmpxchg(Object base, long offset, double expected, double x)
	{
		try
		{
			return (double) compareAndExchangeDouble.invoke(instance_jdk_internal_misc_Unsafe, base, offset, expected, x);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("compare and exchange double '" + x + "' with expected '" + expected + "' at '" + base + "' offset '" + offset + "' failed", ex);
		}
	}

	public static final double cmpxchg(long native_addr, double expected, double x)
	{
		return cmpxchg(null, native_addr, expected, x);
	}

	/**
	 * 在字节数组中写入float<br>
	 * 一般用于操作缓冲区
	 * 
	 * @param byte_arr
	 * @param offset
	 * @param x
	 */
	public static final void write_array(byte[] byte_arr, long arr_idx, float x)
	{
		write(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx, x);
	}

	public static final void write_array(byte[] byte_arr, long arr_idx, int x)
	{
		write(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx, x);
	}

	public static final void write_array(byte[] byte_arr, long arr_idx, short x)
	{
		write(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx, x);
	}

	public static final void write_array(byte[] byte_arr, long arr_idx, long x)
	{
		write(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx, x);
	}

	public static final void write_array(byte[] byte_arr, long arr_idx, double x)
	{
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
	public static final float read_array_float(byte[] byte_arr, long arr_idx)
	{
		return read_float(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx);
	}

	public static final int read_array_int(byte[] byte_arr, long arr_idx)
	{
		return read_int(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx);
	}

	public static final short read_array_short(byte[] byte_arr, long arr_idx)
	{
		return read_short(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx);
	}

	public static final long read_array_long(byte[] byte_arr, long arr_idx)
	{
		return read_long(byte_arr, ARRAY_BYTE_BASE_OFFSET + arr_idx);
	}

	public static final double read_array_double(byte[] byte_arr, long arr_idx)
	{
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
	public static final void write(Object obj, Field field, Object value)
	{
		if (Modifier.isStatic(field.getModifiers()))
			write(static_field_base(field), static_field_offset(field), value);
		else
			write(obj, object_field_offset(field), value);
	}

	public static final void write(Object obj, String field, Object value)
	{
		write(obj, reflection.find_declared_field(obj, field), value);
	}

	public static final void write(Class<?> clazz, String field, Object value)
	{
		write(null, reflection.find_declared_field(clazz, field), value);
	}

	public static final void write_member(Object obj, String field, Object value)
	{
		write(obj, object_field_offset(obj.getClass(), field), value);
	}

	public static final void write_static(Class<?> clazz, String field, Object value)
	{
		Field f = reflection.find_declared_field(clazz, field);
		write(static_field_base(f), static_field_offset(f), value);
	}

	public static final Object read_reference(Object obj, Field field)
	{
		if (Modifier.isStatic(field.getModifiers()))
			return read_reference(static_field_base(field), static_field_offset(field));
		else
			return read_reference(obj, object_field_offset(field));
	}

	public static final Object read_reference(Object obj, String field)
	{
		return read_reference(obj, reflection.find_declared_field(obj, field));
	}

	public static final Object read_reference(Class<?> clazz, String field)
	{
		return read_reference(null, reflection.find_declared_field(clazz, field));
	}

	public static final Object read_member_reference(Object obj, String field)
	{
		return read_reference(obj, object_field_offset(obj.getClass(), field));
	}

	public static final Object read_static_reference(Class<?> clazz, String field)
	{
		Field f = reflection.find_declared_field(clazz, field);
		return read_reference(static_field_base(f), static_field_offset(f));
	}

	public static final void write(Object obj, Field field, long value)
	{
		if (Modifier.isStatic(field.getModifiers()))
			write(static_field_base(field), static_field_offset(field), value);
		else
			write(obj, object_field_offset(field), value);
	}

	public static final void write(Object obj, String field, long value)
	{
		write(obj, reflection.find_declared_field(obj, field), value);
	}

	public static final void write(Class<?> clazz, String field, long value)
	{
		write(null, reflection.find_declared_field(clazz, field), value);
	}

	public static final long read_long(Object obj, Field field)
	{
		if (Modifier.isStatic(field.getModifiers()))
			return read_long(static_field_base(field), static_field_offset(field));
		else
			return read_long(obj, object_field_offset(field));
	}

	public static final long read_long(Object obj, String field)
	{
		return read_long(obj, reflection.find_declared_field(obj, field));
	}

	public static final long read_long(Class<?> clazz, String field)
	{
		return read_long(null, reflection.find_declared_field(clazz, field));
	}

	/**
	 * 获取指定对象声明的类成员long
	 * 
	 * @param obj
	 * @param field
	 * @return
	 */
	public static final long read_member_long(Object obj, String field)
	{
		return read_long(obj, object_field_offset(obj.getClass(), field));
	}

	public static final void write_member(Object obj, String field, long value)
	{
		write(obj, object_field_offset(obj.getClass(), field), value);
	}

	public static final void write(Object obj, Field field, boolean value)
	{
		if (Modifier.isStatic(field.getModifiers()))
			write(static_field_base(field), static_field_offset(field), value);
		else
			write(obj, object_field_offset(field), value);
	}

	public static final void write(Object obj, String field, boolean value)
	{
		write(obj, reflection.find_declared_field(obj, field), value);
	}

	public static final void write(Class<?> clazz, String field, boolean value)
	{
		write(null, reflection.find_declared_field(clazz, field), value);
	}

	public static final void write_member(Object obj, String field, boolean value)
	{
		write(obj, object_field_offset(obj.getClass(), field), value);
	}

	public static final void write_static(Class<?> clazz, String field, boolean value)
	{
		Field f = reflection.find_declared_field(clazz, field);
		write(static_field_base(f), static_field_offset(f), value);
	}

	public static final boolean read_bool(Object obj, Field field)
	{
		if (Modifier.isStatic(field.getModifiers()))
			return read_bool(static_field_base(field), static_field_offset(field));
		else
			return read_bool(obj, object_field_offset(field));
	}

	public static final boolean read_bool(Object obj, String field)
	{
		return read_bool(obj, reflection.find_declared_field(obj, field));
	}

	public static final boolean read_bool(Class<?> clazz, String field)
	{
		return read_bool(null, reflection.find_declared_field(clazz, field));
	}

	public static final boolean read_member_bool(Object obj, String field)
	{
		return read_bool(obj, object_field_offset(obj.getClass(), field));
	}

	public static final boolean read_static_bool(Class<?> clazz, String field)
	{
		Field f = reflection.find_declared_field(clazz, field);
		return read_bool(static_field_base(f), static_field_offset(f));
	}

	public static final void write(Object obj, Field field, int value)
	{
		if (Modifier.isStatic(field.getModifiers()))
			write(static_field_base(field), static_field_offset(field), value);
		else
			write(obj, object_field_offset(field), value);
	}

	public static final void write(Object obj, String field, int value)
	{
		write(obj, reflection.find_declared_field(obj, field), value);
	}

	public static final void write(Class<?> clazz, String field, int value)
	{
		write(null, reflection.find_declared_field(clazz, field), value);
	}

	public static final void write_member(Object obj, String field, int value)
	{
		write(obj, object_field_offset(obj.getClass(), field), value);
	}

	public static final void write_static(Class<?> clazz, String field, int value)
	{
		Field f = reflection.find_declared_field(clazz, field);
		write(static_field_base(f), static_field_offset(f), value);
	}

	public static final int read_int(Object obj, Field field)
	{
		if (Modifier.isStatic(field.getModifiers()))
			return read_int(static_field_base(field), static_field_offset(field));
		else
			return read_int(obj, object_field_offset(field));
	}

	public static final int read_int(Object obj, String field)
	{
		return read_int(obj, reflection.find_declared_field(obj, field));
	}

	public static final int read_int(Class<?> clazz, String field)
	{
		return read_int(null, reflection.find_declared_field(clazz, field));
	}

	public static final int read_member_int(Object obj, String field)
	{
		return read_int(obj, object_field_offset(obj.getClass(), field));
	}

	public static final void write(Object obj, Field field, double value)
	{
		if (Modifier.isStatic(field.getModifiers()))
			write(static_field_base(field), static_field_offset(field), value);
		else
			write(obj, object_field_offset(field), value);
	}

	public static final void write(Object obj, String field, double value)
	{
		write(obj, reflection.find_declared_field(obj, field), value);
	}

	public static final void write(Class<?> clazz, String field, double value)
	{
		write(null, reflection.find_declared_field(clazz, field), value);
	}

	public static final double read_double(Object obj, Field field)
	{
		if (Modifier.isStatic(field.getModifiers()))
			return read_double(static_field_base(field), static_field_offset(field));
		else
			return read_double(obj, object_field_offset(field));
	}

	public static final double read_double(Object obj, String field)
	{
		return read_double(obj, reflection.find_declared_field(obj, field));
	}

	public static final double read_double(Class<?> clazz, String field)
	{
		return read_double(null, reflection.find_declared_field(clazz, field));
	}

	public static final void write(Object obj, Field field, float value)
	{
		if (Modifier.isStatic(field.getModifiers()))
			write(static_field_base(field), static_field_offset(field), value);
		else
			write(obj, object_field_offset(field), value);
	}

	public static final void write(Object obj, String field, float value)
	{
		write(obj, reflection.find_declared_field(obj, field), value);
	}

	public static final void write(Class<?> clazz, String field, float value)
	{
		write(null, reflection.find_declared_field(clazz, field), value);
	}

	public static final float read_float(Object obj, Field field)
	{
		if (Modifier.isStatic(field.getModifiers()))
			return read_float(static_field_base(field), static_field_offset(field));
		else
			return read_float(obj, object_field_offset(field));
	}

	public static final float read_float(Object obj, String field)
	{
		return read_float(obj, reflection.find_declared_field(obj, field));
	}

	public static final float read_float(Class<?> clazz, String field)
	{
		return read_float(null, reflection.find_declared_field(clazz, field));
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
	public static final Class<?> define_class(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protection_domain)
	{
		try
		{
			return (Class<?>) defineClass.invoke(instance_jdk_internal_misc_Unsafe, name, b, off, len, loader, protection_domain);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("define class '" + name + "' failed", ex);
		}
	}

	/**
	 * 内存屏障，保证屏障后的所有读写操作不会重排到屏障前的全部读操作完成
	 */
	public static final void load_fence()
	{
		try
		{
			loadFence.invoke(instance_jdk_internal_misc_Unsafe);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("insert load fence failed", ex);
		}
	}

	/**
	 * 内存屏障，保证屏障后的所有读写操作不会重排到屏障前的全部写操作完成
	 */
	public static final void store_fence()
	{
		try
		{
			storeFence.invoke(instance_jdk_internal_misc_Unsafe);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("insert store fence failed", ex);
		}
	}

	/**
	 * 全内存屏障，保证屏障后的所有读写操作不会重排到屏障前的全部读写操作完成
	 */
	public static final void full_fence()
	{
		try
		{
			fullFence.invoke(instance_jdk_internal_misc_Unsafe);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("insert full fence failed", ex);
		}
	}

	/**
	 * 读内存屏障，保证屏障后的所有读操作不会重排到屏障前的全部读操作完成
	 */
	public static final void load_load_fence()
	{
		try
		{
			loadLoadFence.invoke(instance_jdk_internal_misc_Unsafe);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("insert load-load fence failed", ex);
		}
	}

	/**
	 * 写内存屏障，保证屏障后的所有写操作不会重排到屏障前的全部写操作完成
	 */
	public static final void store_store_fence()
	{
		try
		{
			storeStoreFence.invoke(instance_jdk_internal_misc_Unsafe);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("insert store-store fence failed", ex);
		}
	}

	/**
	 * 判断目标类是否未初始化
	 * 
	 * @param clazz
	 * @return
	 */
	public static final boolean should_be_initialized(Class<?> clazz)
	{
		try
		{
			return (boolean) shouldBeInitialized0.invoke(instance_jdk_internal_misc_Unsafe, clazz);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("class '" + clazz + "' check should be initialized failed", ex);
		}
	}

	/**
	 * 如果目标类未初始化则初始化目标类
	 * 
	 * @param clazz
	 */
	public static final void ensure_class_initialized(Class<?> clazz)
	{
		try
		{
			ensureClassInitialized0.invoke(instance_jdk_internal_misc_Unsafe, clazz);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("class '" + clazz + "' ensure initialized failed", ex);
		}
	}
}

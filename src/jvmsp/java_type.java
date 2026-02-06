package jvmsp;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Java类型所占字节数
 */
public abstract class java_type
{
	/**
	 * 在32位JVM或64位JVM中UseCompressedOops开启的情况下，对象引用占4字节
	 */
	public static final long object_reference_size;

	static
	{
		object_reference_size = unsafe.OOP_SIZE;
	}

	/**
	 * 是否是基本类型
	 * 
	 * @param type
	 * @return
	 */
	public static final boolean is_primitive(Class<?> type)
	{
		return type == void.class || type == byte.class || type == char.class || type == boolean.class || type == short.class || type == int.class || type == float.class || type == long.class || type == double.class;
	}

	/**
	 * 是否是基本类型的Boxing Type
	 * 
	 * @param type
	 * @return
	 */
	public static final boolean is_primitive_boxing(Class<?> type)
	{
		return type == Integer.class || type == Long.class || type == Boolean.class || type == Double.class || type == Float.class || type == Byte.class || type == Short.class || type == Character.class;
	}

	public static final boolean is_primitive_boxing(Object obj)
	{
		return is_primitive_boxing(obj.getClass());
	}

	/**
	 * 计算类型的大小，任何非基本类型均为引用类型。<br>
	 * 引用类型相当于指针，指向对象的实际内存。引用的值会因为GC而移动。
	 * 
	 * @param type
	 * @return
	 */
	public static final long sizeof(Class<?> type)
	{
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
	public static final byte byte_value(Object b)
	{
		return ((Number) b).byteValue();
	}

	public static final char char_value(Object c)
	{
		return ((Character) c).charValue();
	}

	public static final boolean boolean_value(Object bool)
	{
		return ((Boolean) bool).booleanValue();
	}

	public static final short short_value(Object s)
	{
		return ((Number) s).shortValue();
	}

	public static final int int_value(Object i)
	{
		return ((Number) i).intValue();
	}

	public static final float float_value(Object f)
	{
		return ((Number) f).floatValue();
	}

	public static final long long_value(Object l)
	{
		return ((Number) l).longValue();
	}

	public static final double double_value(Object d)
	{
		return ((Number) d).doubleValue();
	}

	private static final HashMap<Class<?>, Long> cached_size = new HashMap<>();

	/**
	 * Java对象所占用内存的大小，无对齐大小。每个Class<?>计算一次后将缓存。
	 * 
	 * @param type
	 * @return
	 */
	public static final long sizeof_object(Class<?> jtype)
	{
		return cached_size.computeIfAbsent(jtype, (Class<?> type) ->
		{
			long max_offset = 0;
			Class<?> max_offset_field_type = null;
			Field[] fields = reflection.find_declared_fields(type);
			for (Field f : fields)
			{
				if (!Modifier.isStatic(f.getModifiers()))
				{
					Class<?> field_type = f.getType();
					long current_field_offset = unsafe.object_field_offset(f);
					if (max_offset < current_field_offset)
					{
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
	public static final long padding_size(int size)
	{
		if (size % 8 != 0)// 对象所占字节数必须是8的整数倍，如果不到则需要padding
			size = (size / 8 + 1) * 8;
		return size;
	}

	/**
	 * 在给定指针处调用对象的构造函数，不会设置对象头，仅初始化字段。父类的构造函数也会被调用。
	 * 
	 * @param ptr
	 * @param target_type
	 * @param arg_types
	 * @return
	 */
	public static final pointer placement_new(pointer ptr, Class<?> target_type, Class<?>[] arg_types, Object... args)
	{
		Object target = ptr.cast(target_type).dereference();
		MethodHandle constructor = symbols.callable.constructor(target_type, arg_types);
		try
		{
			symbols.call(constructor, target, args);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("placement new for " + target_type + " failed", ex);
		}
		return ptr;
	}

	public static final pointer placement_new(pointer ptr, Class<?>[] arg_types, Object... args)
	{
		Class<?> target_type = ptr.ptr_jtype;
		MethodHandle constructor = symbols.callable.constructor(target_type, arg_types);
		try
		{
			symbols.call(constructor, ptr.dereference(), args);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("placement new for " + target_type + " failed", ex);
		}
		return ptr;
	}

	/**
	 * 在已实例化的对象上再次调用构造函数，不会设置对象头，仅初始化字段。父类的构造函数也会被调用。
	 * 
	 * @param jobject
	 * @param arg_types
	 * @param args
	 * @return
	 */
	public static final <T> T placement_new(T jobject, Class<?>[] arg_types, Object... args)
	{
		Class<?> target_type = jobject.getClass();
		MethodHandle constructor = symbols.callable.constructor(target_type, arg_types);
		try
		{
			symbols.call(constructor, jobject, args);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("placement new for " + target_type + " failed", ex);
		}
		return jobject;
	}

	@SuppressWarnings("unchecked")
	public static final <T> T copy(T jobject)
	{
		Class<T> clazz = (Class<T>) jobject.getClass();
		T o = unsafe.allocate(clazz);
		unsafe.__memcpy(jobject, HEADER_BYTE_LENGTH, o, HEADER_BYTE_LENGTH, java_type.sizeof_object(clazz) - HEADER_BYTE_LENGTH);// 只拷贝字段，不覆盖对象头
		return o;
	}

	/**
	 * OOP相关操作 https://github.com/openjdk/jdk/blob/9586817cea3f1cad8a49d43e9106e25dafa04765/src/hotspot/share/oops/compressedOops.cpp#L49<br>
	 * oop压缩相关常量。是否会运行时动态变更未知。<br>
	 * oop压缩指将绝对地址取相对于堆base的偏移量并位移构成一个32位的oop。<br>
	 * 对象头压缩/Klass压缩是指将对象头的Klass Word从64位压缩到32位的narrowKlass。<br>
	 * 开启UseCompressedOops后，默认开启Klass压缩，但oop是否压缩取决于分配的堆内存大小。
	 */

	/**
	 * 压缩模式
	 */
	public static enum oops_mode
	{
		UnscaledNarrowOop, // 无压缩
		ZeroBasedNarrowOop, // 压缩，基地址为0
		DisjointBaseNarrowOop, //
		HeapBasedNarrowOop;// 压缩，基地址非0
	};

	oops_mode mode;

	/**
	 * 最大堆内存大小
	 */
	public static final long max_heap_size;

	/**
	 * 堆内存末尾在内存中的绝对地址
	 */
	public static final long heap_space_end;

	/**
	 * 堆内存的起始地址
	 */
	public static final long base;

	/**
	 * 压缩oop时的位移
	 */
	public static final long shift;

	/**
	 * 堆内存相对地址范围
	 */
	public static final long heap_address_range;

	static
	{
		max_heap_size = virtual_machine.max_heap_size();
		heap_space_end = virtual_machine.HeapBaseMinAddress + max_heap_size;// 这是最大的范围，实际范围可能只是其中一段区间，这种方法或许并不准确。
		if (heap_space_end > virtual_machine.UnscaledOopHeapMax)
		{// 实际堆内存大小大于不压缩oop时支持的最大地址，则需要压缩oop，哪怕没启用UseCompressedOops也会自动开启压缩。
			shift = virtual_machine.OOP_ENCODE_ADDRESS_SHIFT;
		}
		else if (virtual_machine.UseCompressedOops)// 指定了UseCompressedOops后则必定压缩。
			shift = virtual_machine.OOP_ENCODE_ADDRESS_SHIFT;
		else// 堆内存的末尾绝对地址小于4GB就不压缩
			shift = 0;
		if (heap_space_end <= virtual_machine.OopEncodingHeapMax)
		{
			base = 0;
		}
		else
		{
			base = pointer.nullptr.address();// 这对吗？
		}
		heap_address_range = heap_space_end - base;
	}

	/**
	 * 编码压缩oop<br>
	 * oop.encode_heap_oop_not_null
	 * 
	 * @param native_addr
	 * @return
	 */
	public static final int encode_oop(long native_addr)
	{
		return (int) ((native_addr - base) >> shift);
	}

	public static final long pointer_delta(long native_addr)
	{
		return native_addr - base;
	}

	/**
	 * 解码压缩oop，位移可能为0，此时表示未压缩的相对于堆起始位置的相对地址.
	 * 
	 * @param oop_addr
	 * @return
	 */
	public static final long decode_oop(int oop_addr)
	{
		return ((oop_addr & cxx_type.UINT32_T_MASK) << shift) + base;
	}

	/**
	 * markWord
	 */

	private static abstract class __obj_header_base
	{
		// public static final void
	}

	// @formatter:off
	/**
	* 对象头的结构<br>
	* Object Header由Mark Word和Klass Word组成<br>
	* ObjectHeader 32-bit JVM<br>
	* |----------------------------------------------------------------------------------------|--------------------|<br>
	* |                                    Object Header (64 bits)                             |        State       |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |                  Mark Word (32 bits)                  |      Klass Word (32 bits)      |                    |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* | identity_hashcode:25 | age:4 | biased_lock:1 | lock:2 |      OOP to metadata object    |       Normal       |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |  thread:23 | epoch:2 | age:4 | biased_lock:1 | lock:2 |      OOP to metadata object    |       Biased       |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |               ptr_to_lock_record:30          | lock:2 |      OOP to metadata object    | Lightweight Locked |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |               ptr_to_heavyweight_monitor:30  | lock:2 |      OOP to metadata object    | Heavyweight Locked |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |                                              | lock:2 |      OOP to metadata object    |    Marked for GC   |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	*/
	// @formatter:on
	@SuppressWarnings("unused")
	private static final class __32_bit extends __obj_header_base
	{
		// 32位JVM无OOP指针压缩
		public static final int HEADER_OFFSET = 0;
		public static final int HEADER_LENGTH = 64;

		public static final int MARKWORD_OFFSET = HEADER_OFFSET;
		public static final int MARKWORD_LENGTH = 32;
		public static final int KLASS_OFFSET = MARKWORD_OFFSET + MARKWORD_LENGTH;
		public static final int KLASS_LENGTH = 32;

		public static final int IDENTITY_HASHCODE_OFFSET = MARKWORD_OFFSET;
		public static final int IDENTITY_HASHCODE_LENGTH = 25;
		public static final int AGE_OFFSET = IDENTITY_HASHCODE_OFFSET + IDENTITY_HASHCODE_LENGTH;
		public static final int AGE_LENGTH = 4;
		public static final int BIASED_LOCK_OFFSET = AGE_OFFSET + AGE_LENGTH;
		public static final int BIASED_LOCK_LENGTH = 1;

		public static final int LOCK_OFFSET = BIASED_LOCK_OFFSET + BIASED_LOCK_LENGTH;
		public static final int LOCK_LENGTH = 2;

		public static final int THREAD_OFFSET = MARKWORD_OFFSET;
		public static final int THREAD_LENGTH = 23;
		public static final int EPOCH_OFFSET = THREAD_OFFSET + THREAD_LENGTH;
		public static final int EPOCH_LENGTH = 2;

		public static final int PTR_TO_LOCK_RECORD_OFFSET = MARKWORD_OFFSET;
		public static final int PTR_TO_LOCK_RECORD_LENGTH = 30;

		public static final int PTR_TO_HEAVYWEIGHT_MONITOR_OFFSET = MARKWORD_OFFSET;
		public static final int PTR_TO_HEAVYWEIGHT_MONITOR_LENGTH = 30;
	}

	// @formatter:off
	/**
	* ObjectHeader 64-bit JVM<br>
	* |------------------------------------------------------------------------------------------------------------|--------------------|<br>
	* |                                            Object Header (128 bits)                                        |        State       |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                                  Mark Word (64 bits)                         |    Klass Word (64 bits)     |                    |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* | unused:25 | identity_hashcode:31 | unused:1 | age:4 | biased_lock:1 | lock:2 |    OOP to metadata object   |       Normal       |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* | thread:54 |       epoch:2        | unused:1 | age:4 | biased_lock:1 | lock:2 |    OOP to metadata object   |       Biased       |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                       ptr_to_lock_record:62                         | lock:2 |    OOP to metadata object   | Lightweight Locked |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                     ptr_to_heavyweight_monitor:62                   | lock:2 |    OOP to metadata object   | Heavyweight Locked |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                                                                     | lock:2 |    OOP to metadata object   |    Marked for GC   |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	*/
	// @formatter:on
	@SuppressWarnings("unused")
	private static final class __64_bit_no_UseCompressedOops extends __obj_header_base
	{

		// 64位JVM无OOP指针压缩
		public static final int HEADER_OFFSET = 0;
		public static final int HEADER_LENGTH = 128;

		public static final int MARKWORD_OFFSET = HEADER_OFFSET;
		public static final int MARKWORD_LENGTH = 64;
		public static final int KLASS_OFFSET = MARKWORD_OFFSET + MARKWORD_LENGTH;
		public static final int KLASS_LENGTH = 64;

		public static final int UNUSED_1_NORMAL_OFFSET = MARKWORD_OFFSET;
		public static final int UNUSED_1_NORMAL_LENGTH = 25;
		public static final int IDENTITY_HASHCODE_OFFSET = UNUSED_1_NORMAL_OFFSET + UNUSED_1_NORMAL_LENGTH;
		public static final int IDENTITY_HASHCODE_LENGTH = 31;
		public static final int UNUSED_2_NORMAL_OFFSET = IDENTITY_HASHCODE_OFFSET + IDENTITY_HASHCODE_LENGTH;
		public static final int UNUSED_2_NORMAL_LENGTH = 1;
		public static final int AGE_OFFSET = UNUSED_2_NORMAL_OFFSET + UNUSED_2_NORMAL_LENGTH;
		public static final int AGE_LENGTH = 4;
		public static final int BIASED_LOCK_OFFSET = AGE_OFFSET + AGE_LENGTH;
		public static final int BIASED_LOCK_LENGTH = 1;
		public static final int LOCK_OFFSET = BIASED_LOCK_OFFSET + BIASED_LOCK_LENGTH;
		public static final int LOCK_LENGTH = 2;

		public static final int THREAD_OFFSET = MARKWORD_OFFSET;
		public static final int THREAD_LENGTH = 54;
		public static final int EPOCH_OFFSET = THREAD_OFFSET + THREAD_LENGTH;
		public static final int EPOCH_LENGTH = 2;
		public static final int UNUSED_1_BIASED_OFFSET = EPOCH_OFFSET + EPOCH_LENGTH;
		public static final int UNUSED_1_BIASED_LENGTH = 1;

		public static final int PTR_TO_LOCK_RECORD_OFFSET = MARKWORD_OFFSET;
		public static final int PTR_TO_LOCK_RECORD_LENGTH = 62;

		public static final int PTR_TO_HEAVYWEIGHT_MONITOR_OFFSET = MARKWORD_OFFSET;
		public static final int PTR_TO_HEAVYWEIGHT_MONITOR_LENGTH = 62;
	}

	// @formatter:off
	/** <br>
	* ObjectHeader 64-bit JVM UseCompressedOops=true<br>
	* |--------------------------------------------------------------------------------------------------------------|--------------------|<br>
	* |                                            Object Header (96 bits)                                           |        State       |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                                  Mark Word (64 bits)                           |    Klass Word (32 bits)     |                    |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* | unused:25 | identity_hashcode:31 | cms_free:1 | age:4 | biased_lock:1 | lock:2 |    OOP to metadata object   |       Normal       |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* | thread:54 |       epoch:2        | cms_free:1 | age:4 | biased_lock:1 | lock:2 |    OOP to metadata object   |       Biased       |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                         ptr_to_lock_record                            | lock:2 |    OOP to metadata object   | Lightweight Locked |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                     ptr_to_heavyweight_monitor                        | lock:2 |    OOP to metadata object   | Heavyweight Locked |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                                                                       | lock:2 |    OOP to metadata object   |    Marked for GC   |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	 */
	// @formatter:on
	@SuppressWarnings("unused")
	private static final class __64_bit_with_UseCompressedOops extends __obj_header_base
	{
		// 64位JVM开启OOP指针压缩，JVM默认是开启的
		public static final int HEADER_OFFSET = 0;
		public static final int HEADER_LENGTH = 96;

		public static final int MARKWORD_OFFSET = HEADER_OFFSET;
		public static final int MARKWORD_LENGTH = 64;
		public static final int KLASS_OFFSET = MARKWORD_OFFSET + MARKWORD_LENGTH;
		public static final int KLASS_LENGTH = 32;

		public static final int UNUSED_1_NORMAL_OFFSET = MARKWORD_OFFSET;
		public static final int UNUSED_1_NORMAL_LENGTH = 25;
		public static final int IDENTITY_HASHCODE_OFFSET = UNUSED_1_NORMAL_OFFSET + UNUSED_1_NORMAL_LENGTH;
		public static final int IDENTITY_HASHCODE_LENGTH = 31;
		public static final int CMS_FREE_OFFSET = IDENTITY_HASHCODE_OFFSET + IDENTITY_HASHCODE_LENGTH;
		public static final int CMS_FREE_LENGTH = 1;
		public static final int AGE_OFFSET = CMS_FREE_OFFSET + CMS_FREE_LENGTH;
		public static final int AGE_LENGTH = 4;
		public static final int BIASED_LOCK_OFFSET = AGE_OFFSET + AGE_LENGTH;
		public static final int BIASED_LOCK_LENGTH = 1;
		public static final int LOCK_OFFSET = BIASED_LOCK_OFFSET + BIASED_LOCK_LENGTH;
		public static final int LOCK_LENGTH = 2;

		public static final int THREAD_OFFSET = MARKWORD_OFFSET;
		public static final int THREAD_LENGTH = 54;
		public static final int EPOCH_OFFSET = THREAD_OFFSET + THREAD_LENGTH;
		public static final int EPOCH_LENGTH = 2;

		public static final int PTR_TO_LOCK_RECORD_OFFSET = MARKWORD_OFFSET;
		public static final int PTR_TO_LOCK_RECORD_LENGTH = 62;

		public static final int PTR_TO_HEAVYWEIGHT_MONITOR_OFFSET = MARKWORD_OFFSET;
		public static final int PTR_TO_HEAVYWEIGHT_MONITOR_LENGTH = 62;

	}

	public static final int INVALID_OFFSET = -1;
	public static final int INVALID_LENGTH = -1;

	/**
	 * Mark Word的长度，单位bit
	 */
	public static final int MARKWORD_LENGTH;

	/**
	 * Klass Word的偏移量，单位bit
	 */
	public static final int KLASS_WORD_OFFSET;

	/**
	 * Klass Word的长度，单位bit
	 */
	public static final int KLASS_WORD_LENGTH;

	public static final int HEADER_LENGTH;

	/**
	 * Mark Word的长度，单位byte
	 */
	public static final int MARKWORD_BYTE_LENGTH;

	/**
	 * Klass Word的偏移量，单位byte
	 */
	public static final int KLASS_WORD_BYTE_OFFSET;

	/**
	 * Klass Word的长度，单位byte
	 */
	public static final int KLASS_WORD_BYTE_LENGTH;

	/**
	 * header总长度
	 */
	public static final int HEADER_BYTE_LENGTH;

	static
	{
		if (virtual_machine.NATIVE_JVM_BIT_VERSION == 32)
		{
			MARKWORD_LENGTH = __32_bit.MARKWORD_LENGTH;
			KLASS_WORD_OFFSET = __32_bit.KLASS_OFFSET;
			KLASS_WORD_LENGTH = __32_bit.KLASS_LENGTH;
			HEADER_LENGTH = __32_bit.HEADER_LENGTH;
		}
		else if (virtual_machine.NATIVE_JVM_BIT_VERSION == 64)
		{
			if (virtual_machine.UseCompressedOops)
			{
				MARKWORD_LENGTH = __64_bit_with_UseCompressedOops.MARKWORD_LENGTH;
				KLASS_WORD_OFFSET = __64_bit_with_UseCompressedOops.KLASS_OFFSET;
				KLASS_WORD_LENGTH = __64_bit_with_UseCompressedOops.KLASS_LENGTH;
				HEADER_LENGTH = __64_bit_with_UseCompressedOops.HEADER_LENGTH;
			}
			else
			{
				MARKWORD_LENGTH = __64_bit_no_UseCompressedOops.MARKWORD_LENGTH;
				KLASS_WORD_OFFSET = __64_bit_no_UseCompressedOops.KLASS_OFFSET;
				KLASS_WORD_LENGTH = __64_bit_no_UseCompressedOops.KLASS_LENGTH;
				HEADER_LENGTH = __64_bit_no_UseCompressedOops.HEADER_LENGTH;
			}
		}
		else
		{
			MARKWORD_LENGTH = INVALID_LENGTH;
			KLASS_WORD_OFFSET = INVALID_OFFSET;
			KLASS_WORD_LENGTH = INVALID_LENGTH;
			HEADER_LENGTH = INVALID_LENGTH;
		}
		MARKWORD_BYTE_LENGTH = MARKWORD_LENGTH / 8;
		KLASS_WORD_BYTE_OFFSET = KLASS_WORD_OFFSET / 8;
		KLASS_WORD_BYTE_LENGTH = KLASS_WORD_LENGTH / 8;
		HEADER_BYTE_LENGTH = HEADER_LENGTH / 8;
	}

	public static final long get_klass_word(Class<?> c)
	{
		return get_klass_word(unsafe.allocate(c));
	}

	/**
	 * 获取对象头
	 * 
	 * @param obj
	 * @return
	 */
	public static final long get_klass_word(Object obj)
	{
		if (KLASS_WORD_LENGTH == 32)
			return unsafe.read_int(obj, KLASS_WORD_BYTE_OFFSET);
		else if (KLASS_WORD_LENGTH == 64)
			return unsafe.read_long(obj, KLASS_WORD_BYTE_OFFSET);
		else
			throw new IllegalStateException("unknown klass word length");
	}

	/**
	 * 强制改写对象头
	 * 
	 * @param obj
	 * @param klass_word
	 * @return
	 */
	public static final void set_klass_word(Object obj, long klass_word)
	{
		if (KLASS_WORD_LENGTH == 32)
		{
			unsafe.write(obj, KLASS_WORD_BYTE_OFFSET, (int) klass_word);
		}
		else if (KLASS_WORD_LENGTH == 64)
		{
			unsafe.write(obj, KLASS_WORD_BYTE_OFFSET, klass_word);
		}
		throw new IllegalStateException("unknown klass word length");
	}

	public static final void set_klass_word(long obj_base, long klass_word)
	{
		if (KLASS_WORD_LENGTH == 32)
		{
			unsafe.write(null, obj_base + KLASS_WORD_BYTE_OFFSET, (int) klass_word);
		}
		else if (KLASS_WORD_LENGTH == 64)
		{
			unsafe.write(null, obj_base + KLASS_WORD_BYTE_OFFSET, klass_word);
		}
		throw new IllegalStateException("unknown klass word length");
	}

	public static final Object cast(Object obj, long cast_type_klass_word)
	{
		set_klass_word(obj, cast_type_klass_word);
		return obj;
	}

	public static final Object cast(Object obj, Object cast_type_obj)
	{
		return cast(obj, get_klass_word(cast_type_obj));
	}

	public static final Object cast(Object obj, Class<?> cast_type)
	{
		return cast(obj, get_klass_word(cast_type));
	}

	public static final Object cast(Object obj, String cast_type)
	{
		return cast(obj, get_klass_word(cast_type));
	}

	@SuppressWarnings("unchecked")
	public static final <_T> _T safe_cast(Object obj, _T cast_type_obj)
	{
		return safe_cast(obj, (Class<_T>) cast_type_obj.getClass());
	}

	/**
	 * 安全的强制转换，没有继承关系的独立类的转换会抛出Exception。<br>
	 * 主要用于Mixin。<br>
	 * 
	 * @param obj
	 * @param cast_type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <_T> _T safe_cast(Object obj, Class<_T> cast_type)
	{
		return (_T) (Object) obj;
	}

	@SuppressWarnings("unchecked")
	public static final <T> T undefined(long cast_type_klass_word)
	{
		return (T) java_type.cast(new Object(), cast_type_klass_word);
	}

	/**
	 * 用于作为Object类型的static final变量初始值，防止变量字面值或null值被内联
	 * 
	 * @param <T>
	 * @param klass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T undefined(T dest_type_obj)
	{
		return (T) java_type.cast(new Object(), dest_type_obj);
	}

	@SuppressWarnings("unchecked")
	public static final <T> T undefined(Object obj, Class<?> cast_type)
	{
		return (T) java_type.cast(new Object(), cast_type);
	}

	@SuppressWarnings("unchecked")
	public static final <T> T undefined(Object obj, String cast_type)
	{
		return (T) java_type.cast(new Object(), cast_type);
	}

	/**
	 * 防止Object类型的static final变量初始null字面值被内联<br>
	 * 当跨类修改目标类字段，且static final Object被初始化为null字面值时：如果不在修改之前在本类使用这个变量，那么这个值的修改就不会成功（会被内联）。
	 * 
	 * @param var
	 * @return
	 */
	public static final void not_inlined(Object var)
	{

	}

	/**
	 * 任何枚举类型的占位符
	 */
	public static enum enum_placeholder
	{
		Null;

		/**
		 * 将该占位符转换为实际的枚举类型值
		 * 
		 * @param <T>
		 * @param target_class
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public final <T extends Enum<T>> T as(Class<T> target_class)
		{
			return (T) cast(this, target_class);
		}

		/**
		 * 将一个枚举类型值包装为占位符
		 * 
		 * @param <T>
		 * @param enumeration
		 * @return
		 */
		public static final <T extends Enum<T>> enum_placeholder pack(T enumeration)
		{
			return (enum_placeholder) cast(enumeration, enum_placeholder.class);
		}
	}

	/**
	 * 用于lambda表达式内更改表达式外的局部变量值
	 * 
	 * @param <T>
	 */
	public static final class type_wrapper<T>
	{
		public T value;

		public type_wrapper(T value)
		{
			this.value = value;
		}

		public static final <T> type_wrapper<T> wrap(T value)
		{
			return new type_wrapper<T>(value);
		}

		@SuppressWarnings(
		{ "rawtypes", "unchecked" })
		public static final type_wrapper wrap()
		{
			return new type_wrapper(null);
		}
	}

	/**
	 * 父类实现此接口，即可在父类中使用子类Class<_Derived>对象
	 * 
	 * @param <_Derived>
	 */
	public static interface _crtp<_Derived>
	{
		@SuppressWarnings("unchecked")
		public default Class<_Derived> derived_class()
		{
			return (Class<_Derived>) this.getClass();
		}
	}

	/**
	 * 通过定义的枚举的构造函数类型获取实际的构造函数类型。<br>
	 * 这是因为编译器会在枚举类的构造函数声明的构造函数参数最前方自动添加两个额外参数，如果不加入这两个额外参数，就找不到枚举的构造函数。<br>
	 * 自动添加的两个参数是String name：枚举值的字符串名称、int ordinal枚举值的序号，从0开始计数。
	 * 
	 * @param ctor_arg_types
	 * @return
	 */
	private static Class<?>[] _enum_constructor_arg_types(Class<?>... ctor_arg_types)
	{
		return memory.cat(String.class, int.class, ctor_arg_types);
	}

	/**
	 * 获取enum内部定义的枚举值数组，此为原始数组，对该数组的任何修改都会反映到Enum的相关方法
	 * 
	 * @param <_T>
	 * @param target_enum
	 * @return
	 */
	public static <_T extends Enum<_T>> VarHandle __enum_values(Class<_T> target_enum)
	{
		return symbols.find_static_var(target_enum, "ENUM$VALUES", target_enum.arrayType());
	}

	/**
	 * 为目标枚举设置values()
	 * 
	 * @param <_T>
	 * @param target_enum
	 * @param values
	 */
	@SuppressWarnings("unchecked")
	public static <_T extends Enum<_T>> void set_enum_values(Class<_T> target_enum, _T... values)
	{
		__enum_values(target_enum).set(null, values);
	}

	public static <_T extends Enum<_T>> _T[] get_enum_values(Class<_T> target_enum)
	{
		return (_T[]) __enum_values(target_enum).get(null);
	}

	/**
	 * 修改枚举、创建新的枚举值实例
	 * 
	 * @param <_T>
	 */
	public static interface mutable_enum<_T extends Enum<_T>> extends _crtp<_T>
	{
		public default _T of(Class<?>[] arg_types, Object... args)
		{
			return of("$tmp", -1, arg_types, args);
		}

		public default _T of(String name, int ordinal, Class<?>[] arg_types, Object... args)
		{
			return of(this.derived_class(), name, ordinal, arg_types, args);
		}

		/**
		 * 新建一个枚举值实例，该枚举值为自由对象，没有被添加到枚举类的values()中。
		 * 
		 * @param <_T>
		 * @param target_enum
		 * @param arg_types
		 * @param args
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static <_T extends Enum<_T>> _T of(Class<_T> target_enum, String name, int ordinal, Class<?>[] arg_types, Object... args)
		{
			MethodHandle constructor = symbols.find_constructor(target_enum, _enum_constructor_arg_types(arg_types));
			try
			{
				return (_T) constructor.invokeWithArguments(memory.cat(name, ordinal, args));
			}
			catch (Throwable ex)
			{
				ex.printStackTrace();
			}
			return null;
		}

		public static <_T extends Enum<_T>> _T of(Class<_T> target_enum, Class<?>[] arg_types, Object... args)
		{
			return of(target_enum, "$tmp", -1, arg_types, args);
		}
	}

	/**
	 * 多重继承解决方案<br>
	 * 将所有接口所需字段定义到接口的内部类，这个内部类实例可以绑定到一个接口对象上，接口通过definition()方法获取字段内部类实例。
	 * 
	 * @param <_Def>
	 */
	public static interface base<_Def extends base.definition<? extends base<?>>>
	{
		/**
		 * 接口内部类都是隐式public static final嵌套类<br>
		 * 接口字段定义父类
		 * 
		 * @param <_Derived>
		 */
		abstract class definition<_Derived extends base<? extends definition<?>>>
		{
			/**
			 * (derived_obj, base_type)->base_type_definition
			 */
			private static final HashMap<base<?>, HashMap<Class<?>, Object>> definitions = new HashMap<>();

			public final _Derived this_;

			private static Field this_field;

			/**
			 * 初始化对象时使用
			 */
			private static final HashMap<definition<?>, base<?>> this_preinit_refs = new HashMap<>();

			static
			{
				this_field = reflection.find_declared_field(definition.class, "this_");
			}

			@SuppressWarnings("unchecked")
			protected definition()
			{
				this_ = (_Derived) this_preinit_refs.remove(this);// 在执行子类构造函数之前先设置好子类的this_引用
			}

			/**
			 * 将本实例绑定在一个base对象上
			 * 
			 * @param obj
			 * @return
			 */
			@SuppressWarnings(
			{ "rawtypes" })
			public final definition move(_Derived obj)
			{
				Class<?> base_type = this.getClass();
				if (this_ != null)
				{
					HashMap<Class<?>, Object> orig_base_defs = definition.definitions.computeIfAbsent(this_, (base) -> new HashMap<>());
					orig_base_defs.remove(base_type);// 从原绑定对象移除该实例
				}
				reflection.write(this, this_field, obj);
				HashMap<Class<?>, Object> new_base_defs = definition.definitions.computeIfAbsent(obj, (base) -> new HashMap<>());
				new_base_defs.put(base_type, this);// 将指定基类定义加入Map
				return this;
			}
		}

		default _Def construct(Class<_Def> base_type, Class<?>[] arg_types, Object... args)
		{
			_Def def = unsafe.allocate(base_type);// 先分配对象内存
			definition.this_preinit_refs.put(def, this);// 为目标对象指定this_引用
			try
			{
				def = placement_new(def, arg_types, args);// 调用目标构造函数
			}
			catch (Throwable ex)
			{
				ex.printStackTrace();
			}
			definition.definitions.computeIfAbsent(this, (base) -> new HashMap<>()).put(base_type, def);
			return def;
		}

		@SuppressWarnings("unchecked")
		default _Def construct(Object base_type, Class<?>[] arg_types, Object... args)
		{
			return construct((Class<_Def>) base_type, arg_types, args);
		}

		/**
		 * 子类需要有正确的Definition对象
		 * 
		 * @return
		 */
		@SuppressWarnings("unchecked")
		default _Def definition(Class<?> type)
		{
			return (_Def) definition.definitions.get(this).get(type);
		}

		@SuppressWarnings("unchecked")
		default _Def definition()
		{
			HashMap<Class<?>, Object> base_defs = definition.definitions.get(this);
			if (base_defs.size() == 1)
				return (_Def) base_defs.values().iterator().next();// 只继承了一个base则直接返回其字段定义实例
			else
				throw new IllegalArgumentException("Class " + this.getClass() + " have multipe base class, specify the target base class type.");
		}
	}

	/**
	 * 遍历类的工具，无视访问修饰符和反射过滤<br>
	 * 提供原始Field、Method、Constructor等，对其进行修改会导致反射获取到的所有副本都被修改
	 */

	@FunctionalInterface
	public static interface field_operation<_F>
	{
		/**
		 * 遍历每个字段，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param f
		 * @param is_static 目标字段是否是静态的
		 * @param value     字段值，无效则为null
		 * @return 是否继续迭代，返回true代表继续迭代，false则终止迭代
		 */
		public boolean operate(Field f, boolean is_static, _F value);

		@FunctionalInterface
		public static interface simple<_F>
		{
			/**
			 * 遍历每个字段，处理的是root原对象，即反射缓存的对象。
			 * 
			 * @param f
			 * @param is_static 目标字段是否是静态的
			 * @param value     字段值，无效则为null
			 */
			public boolean operate(String field_name, Class<?> field_type, boolean is_static, _F value);
		}

		@FunctionalInterface
		public static interface annotated<_F, _T extends Annotation>
		{
			/**
			 * 遍历每个具有某注解的字段
			 * 
			 * @param f
			 * @param is_static 目标字段是否是静态的
			 * @param value     字段值，无效则为null
			 */
			public boolean operate(Field f, boolean is_static, _F value, _T annotation);

			@FunctionalInterface
			public static interface simple<_F, _T extends Annotation>
			{
				/**
				 * 遍历每个具有某注解的字段
				 * 
				 * @param f
				 * @param is_static 目标字段是否是静态的
				 * @param value     字段值，无效则为null
				 */
				public boolean operate(String field_name, Class<?> field_type, boolean is_static, _F value, _T annotation);
			}
		}

		@FunctionalInterface
		public static interface generic<_F>
		{
			/**
			 * 遍历具有单个泛型参数的字段
			 * 
			 * @param f
			 * @param is_static 目标字段是否是静态的
			 * @param value     字段值，无效则为null
			 * @return 是否继续迭代，返回true代表继续迭代，false则终止迭代
			 */
			public boolean operate(Field f, boolean is_static, Class<?> genericType, _F value);
		}
	}

	/**
	 * op()中形参value为字段值<br>
	 * 字段如果是静态的，则传入值；如果是非静态字段则传入target的该字段值（若target为Class<?>则表示无对象，传入null）
	 * 
	 * @param obj
	 * @param op
	 */
	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public static final <_F> void walk_fields(Object target, field_operation<_F> op)
	{
		Class<?> clazz;
		Object obj;
		if (target instanceof Class c)
		{
			clazz = c;
			obj = null;
		}
		else
		{
			clazz = target.getClass();
			obj = target;
		}
		Field[] fields = reflection.find_declared_fields(clazz);
		field_operation rop = (field_operation) op;
		for (Field f : fields)
		{
			boolean is_static = Modifier.isStatic(f.getModifiers());
			if (!rop.operate(f, is_static, is_static ? reflection.read(clazz, f) : (obj == null ? null : reflection.read(obj, f))))
				return;
		}
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	public static final <_F> void walk_fields(Object target, field_operation.simple<_F> op)
	{
		field_operation.simple rop = (field_operation.simple) op;
		walk_fields(target, (Field f, boolean is_static, Object value) ->
		{
			return rop.operate(f.getName(), f.getType(), is_static, value);
		});
	}

	/**
	 * 遍历含有某个注解的全部字段
	 * 
	 * @param clazz
	 * @param annotation
	 * @param op
	 */
	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public static final <_F, _T extends Annotation> void walk_fields(Object target, Class<_T> annotation_clazz, field_operation.annotated<_F, _T> op)
	{
		field_operation.annotated rop = (field_operation.annotated) op;
		walk_fields(target, (Field f, boolean is_static, Object value) ->
		{
			_T annotation = f.getAnnotation(annotation_clazz);
			if (annotation != null)
				return rop.operate(f, is_static, value, annotation);
			return true;
		});
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public static final <_F, _T extends Annotation> void walk_fields(Object target, Class<_T> annotation_clazz, field_operation.annotated.simple<_F, _T> op)
	{
		field_operation.annotated.simple rop = (field_operation.annotated.simple) op;
		walk_fields(target, annotation_clazz, (Field f, boolean is_static, Object value, _T annotation) ->
		{
			return rop.operate(f.getName(), f.getType(), is_static, value, annotation);
		});
	}

	/**
	 * 遍历指定类的目标类型或其子类的字段
	 * 
	 * @param <_T>
	 * @param clazz
	 * @param targetType
	 * @param op
	 */
	@SuppressWarnings("unchecked")
	public static final <_T> void walk_fields(Object target, Class<_T> targetType, field_operation<_T> op)
	{
		walk_fields(target, (Field f, boolean is_static, Object value) ->
		{
			if (reflection.is(f, targetType))
				return op.operate(f, is_static, (_T) value);
			return true;
		});
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	public static final <_F> void walk_fields(Object target, Class<_F> field_type, field_operation.generic<_F> op)
	{
		field_operation.generic rop = (field_operation.generic) op;
		walk_fields(target, field_type, (Field f, boolean is_static, _F value) ->
		{
			return rop.operate(f, is_static, reflection.first_generic_class(f), value);
		});
	}

	/**
	 * 遍历target中全部第一个泛型参数为single_generic_type的field_type类型的字段
	 * 
	 * @param <_F>
	 * @param target
	 * @param field_type
	 * @param single_generic_type
	 * @param op
	 */
	public static final <_F, _G> void walk_fields(Object target, Class<_F> field_type, Class<_G> single_generic_type, field_operation<_F> op)
	{
		walk_fields(target, field_type, (Field f, boolean is_static, Class<?> genericType, _F value) ->
		{
			if (reflection.is(genericType, single_generic_type))
			{
				return op.operate(f, is_static, (_F) value);
			}
			return true;
		});
	}

	@FunctionalInterface
	public static interface method_operation<_M>
	{
		/**
		 * 遍历每个方法，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param m
		 * @param is_static 目标字段是否是静态的
		 * @param value     方法所属对象实例，静态方法则为null
		 */
		public boolean operate(Method m, boolean is_static, _M obj);

		@FunctionalInterface
		public static interface annotated<_M, _T extends Annotation>
		{
			/**
			 * 遍历每个具有某注解的方法
			 * 
			 * @param m
			 * @param is_static 目标字段是否是静态的
			 * @param value     方法所属对象实例，静态方法则为null
			 */
			public boolean operate(Method m, boolean is_static, _M obj, _T annotation);
		}
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public static final <_M> void walk_methods(Object target, method_operation<_M> op)
	{
		Class<?> clazz;
		Object obj;
		if (target instanceof Class c)
		{
			clazz = c;
			obj = null;
		}
		else
		{
			clazz = target.getClass();
			obj = target;
		}
		Method[] methods = reflection.find_declared_methods(clazz);
		method_operation rop = (method_operation) op;
		for (Method m : methods)
		{
			boolean is_static = Modifier.isStatic(m.getModifiers());
			if (!rop.operate(m, is_static, obj))
				return;
		}
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public static final <_M, _T extends Annotation> void walk_methods(Object target, Class<_T> annotation_clazz, method_operation.annotated<_M, _T> op)
	{
		method_operation.annotated rop = (method_operation.annotated) op;
		walk_methods(target, (Method m, boolean is_static, Object obj) ->
		{
			_T annotation = m.getAnnotation(annotation_clazz);
			if (annotation != null)
				return rop.operate(m, is_static, obj, annotation);
			return true;
		});
	}

	@FunctionalInterface
	public static interface constructor_operation<_C>
	{
		/**
		 * 遍历每个构造函数，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param c
		 */
		public boolean operate(Constructor<_C> c);

		@FunctionalInterface
		public static interface annotated<_C, _T extends Annotation>
		{
			/**
			 * 遍历每个具有某注解的构造函数
			 * 
			 * @param c
			 * @param annotation
			 */
			public boolean operate(Constructor<_C> c, _T annotation);
		}
	}

	public static final <_C> void walk_constructors(Class<_C> clazz, constructor_operation<_C> op)
	{
		Constructor<_C>[] constructors = reflection.__find_declared_constructors(clazz);
		for (Constructor<_C> c : constructors)
		{
			if (!op.operate(c))
				return;
		}
	}

	public static final <_C, _T extends Annotation> void walk_constructors(Class<_C> clazz, Class<_T> annotation_clazz, constructor_operation.annotated<_C, _T> op)
	{
		walk_constructors(clazz, (Constructor<_C> c) ->
		{
			_T annotation = c.getAnnotation(annotation_clazz);
			if (annotation != null)
				return op.operate(c, annotation);
			return true;
		});
	}

	@FunctionalInterface
	public static interface executable_operation<_E>
	{
		/**
		 * 遍历每个方法或构造函数，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param e
		 * @param is_static 目标字段是否是静态的
		 * @param value     字段值，无效则为null
		 */
		public boolean operate(Executable e, boolean is_static, _E obj);
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	public static final <_E> void walk_executables(Object target, executable_operation<_E> op)
	{
		Class<?> clazz;
		Object obj;
		if (target instanceof Class c)
		{
			clazz = c;
			obj = null;
		}
		else
		{
			clazz = target.getClass();
			obj = target;
		}
		Method[] methods = reflection.find_declared_methods(clazz);
		Constructor<?>[] constructors = reflection.__find_declared_constructors(clazz);
		executable_operation rop = (executable_operation) op;
		for (Constructor<?> c : constructors)
		{
			if (!rop.operate(c, Modifier.isStatic(c.getModifiers()), obj))
				return;
		}
		for (Method m : methods)
		{
			if (!rop.operate(m, Modifier.isStatic(m.getModifiers()), obj))
				return;
		}
	}

	@FunctionalInterface
	public static interface accessible_object_operation<_A>
	{
		/**
		 * 遍历每个字段，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param ao
		 * @param is_static 目标字段是否是静态的
		 * @param value     字段值，无效则为null
		 */
		public boolean operate(AccessibleObject ao, boolean is_static, _A obj);
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	public static final <_A> void walk_accessible_objects(Object target, accessible_object_operation<_A> op)
	{
		Class<?> clazz;
		Object obj;
		if (target instanceof Class c)
		{
			clazz = c;
			obj = null;
		}
		else
		{
			clazz = target.getClass();
			obj = target;
		}
		Field[] fields = reflection.find_declared_fields(clazz);
		Method[] methods = reflection.find_declared_methods(clazz);
		Constructor<?>[] constructors = reflection.__find_declared_constructors(clazz);
		accessible_object_operation rop = (accessible_object_operation) op;
		for (Field f : fields)
		{
			if (!rop.operate(f, Modifier.isStatic(f.getModifiers()), obj))
				return;
		}
		for (Constructor<?> c : constructors)
		{
			if (!rop.operate(c, Modifier.isStatic(c.getModifiers()), obj))
				return;
		}
		for (Method m : methods)
		{
			if (!rop.operate(m, Modifier.isStatic(m.getModifiers()), obj))
				return;
		}
	}

	/**
	 * 扫描过滤
	 */
	@FunctionalInterface
	public static interface filter
	{
		/**
		 * 过滤AnnotatedElement的条件。
		 * 
		 * @param scanned_clazz
		 * @return 返回为true才收集该元素
		 */
		public boolean condition(AnnotatedElement scanned_ae);

		public static final filter RESERVE_ALL = (AnnotatedElement scanned_ae) -> true;

		@FunctionalInterface
		public static interface _class
		{
			/**
			 * 过滤扫描到的类，只有返回true的类才被保留。
			 * 
			 * @param scanned_clazz
			 * @return
			 */
			public boolean condition(Class<?> scanned_clazz);

			public static final _class RESERVE_ALL = (Class<?> scanned_clazz) -> true;
		}
	}

	/**
	 * 扫描所有被注解的元素，包括Class，Field，Method，Constructor等<br>
	 * 
	 * @param loader
	 * @param filter
	 * @return
	 */
	public static final ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader, filter filter)
	{
		ArrayList<AnnotatedElement> annotated = new ArrayList<>();
		ArrayList<Class<?>> classes = class_loader.loaded_classes_copy(loader);
		for (Class<?> clazz : classes)
		{
			if (clazz.getAnnotations().length > 0)
			{
				if (filter.condition(clazz))
					annotated.add(clazz);
			}
			walk_accessible_objects(clazz, (AccessibleObject ao, boolean isStatic, Object obj) ->
			{
				if (ao.getAnnotations().length > 0)
				{
					if (filter.condition(ao))
						annotated.add(ao);
				}
				return true;
			});
		}
		return annotated;
	}

	public static final ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader)
	{
		return scan_annotated_elements(loader, filter.RESERVE_ALL);
	}

	/**
	 * 扫描指定ClassLoader的指定注解的元素
	 * 
	 * @param <_T>
	 * @param loader           要扫描的ClassLoader
	 * @param annotation_clazz 注解类，不要求必须是Annotation子类，可以是任何类型
	 * @param filter           过滤条件
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <_T> ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader, Class<_T> annotation_clazz, filter filter)
	{
		ArrayList<AnnotatedElement> annotated = new ArrayList<>();
		ArrayList<Class<?>> classes = class_loader.loaded_classes_copy(loader);
		for (Class<?> clazz : classes)
		{
			if (clazz.isAnnotationPresent((Class<? extends Annotation>) annotation_clazz))
			{
				if (filter.condition(clazz))// 不满足条件的AnnotatedElement不放入结果
					annotated.add(clazz);
			}
			walk_accessible_objects(clazz, (AccessibleObject ao, boolean isStatic, Object obj) ->
			{
				if (ao.isAnnotationPresent((Class<? extends Annotation>) annotation_clazz))
				{
					if (filter.condition(ao))
						annotated.add(ao);
				}
				return true;
			});
		}
		return annotated;
	}

	public static final <_T> ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader, Class<_T> annotation_clazz)
	{
		return scan_annotated_elements(loader, annotation_clazz, filter.RESERVE_ALL);
	}

	@SuppressWarnings("unchecked")
	public static final <_T> ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader, Class<_T> annotation_clazz, filter._class filter)
	{
		ArrayList<AnnotatedElement> annotated = new ArrayList<>();
		ArrayList<Class<?>> classes = class_loader.loaded_classes_copy(loader);
		for (Class<?> clazz : classes)
		{
			// 不满足条件的类直接略过
			if (!filter.condition(clazz))
				continue;
			if (clazz.isAnnotationPresent((Class<? extends Annotation>) annotation_clazz))
				annotated.add(clazz);
			walk_accessible_objects(clazz, (AccessibleObject ao, boolean isStatic, Object obj) ->
			{
				if (ao.isAnnotationPresent((Class<? extends Annotation>) annotation_clazz))
					annotated.add(ao);
				return true;
			});
		}
		return annotated;
	}

	public static final <_T> ArrayList<AnnotatedElement> scan_annotated_classes(ClassLoader loader, Class<_T> annotation_clazz)
	{
		return scan_annotated_elements(loader, annotation_clazz, filter._class.RESERVE_ALL);
	}
}

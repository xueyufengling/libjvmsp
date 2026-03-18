package jvmsp;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.function.IntFunction;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import jvmsp.type.cxx_type;

/**
 * 管理JVM的相关功能
 */
public class virtual_machine
{
	/**
	 * uint32_t的最大值，用于掩码和计算32位机器最大寻址地址。
	 */
	public static final long juint_max = 0xFFFFFFFFL;

	/**
	 * 64或32位JVM
	 */
	public static final int jvm_bit_version;

	/**
	 * 是否运行在64位JVM，该变量为缓存值，用于指针的快速条件判断
	 */
	public static final boolean on_64bit_jvm;

	/**
	 * JVM中未压缩的32位oop时支持的最大堆内存大小，类型为uint，该值为常数，即固定为4G
	 */
	public static final long UnscaledOopHeapMax;

	/**
	 * HotSpotDiagnosticMXBean的实现类是 com.sun.management.internal.HotSpotDiagnostic
	 */
	private static final HotSpotDiagnosticMXBean instance_HotSpotDiagnosticMXBean;

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

	static
	{
		// 获取JVM位数，支持多种JVM实现
		String arch = System.getProperty("os.arch");
		if (arch == null)
			throw new java.lang.UnknownError("system property 'os.arch' found null, this property is guaranteed Java Specification");
		if (arch.contains("64"))
			jvm_bit_version = 64;
		else
			jvm_bit_version = 32;

		if (jvm_bit_version == 64)
			on_64bit_jvm = true;
		else
			on_64bit_jvm = false;
		UnscaledOopHeapMax = juint_max + 1;// 2^32+1
		// 获取HotSpotDiagnosticMXBean实例
		instance_HotSpotDiagnosticMXBean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
		if (instance_HotSpotDiagnosticMXBean == null)
		{
			throw new java.lang.InternalError("only Hotspot JVM supported");
		}
	}

	/**
	 * 求2为底的对数，用于2的整数次幂的快速算法
	 * 
	 * @param num
	 * @return -1为无效结果
	 */
	public static final int uint64_log2(long uint64)
	{
		if (uint64 == 0)// 非法值
			return -1;
		int power = 0;
		long i = 0x01;
		while (i != uint64)
		{
			++power;
			if (i == 0)// 溢出
				return -1;
			i <<= 1;
		}
		return power;
	}

	public static final VMOption get_option(String name)
	{
		return instance_HotSpotDiagnosticMXBean.getVMOption(name);
	}

	/**
	 * 获取指定的boolean类型的VM参数
	 * 
	 * @param option_name 参数名称，例如UseCompressedOops
	 * @return
	 */
	public static final boolean get_bool_option(String option_name)
	{
		return Boolean.parseBoolean(get_option(option_name).getValue().toString());
	}

	public static final int get_int_option(String option_name)
	{
		return Integer.parseInt(get_option(option_name).getValue().toString());
	}

	public static final long get_long_option(String option_name)
	{
		return Long.parseLong(get_option(option_name).getValue().toString());
	}

	public static final void dump_heap(String file_name, boolean live)
	{
		try
		{
			instance_HotSpotDiagnosticMXBean.dumpHeap(file_name, live);
		}
		catch (IOException ex)
		{
			throw new java.lang.InternalError("dump heap to '" + file_name + "' faield", ex);
		}
	}

	/**
	 * 堆内存的最大大小
	 * 
	 * @return
	 */
	public static final long max_heap_size()
	{
		return Runtime.getRuntime().maxMemory();
	}

	/**
	 * 堆内存的当前大小
	 * 
	 * @return
	 */
	public static final long current_heap_size()
	{
		return Runtime.getRuntime().totalMemory();
	}

	/**
	 * 堆内存的当前空闲空间，创建新对象时空闲空间减小，GC后空闲空间增加
	 * 
	 * @return
	 */
	public static final long free_heap_size()
	{
		return Runtime.getRuntime().freeMemory();
	}

	/**
	 * 实际类型为sun.management.RuntimeImpl
	 */
	private static Object instance_java_lang_management_RuntimeMXBean = null;

	/**
	 * 在HotSpot虚拟机中VMManagement是sun.management.VMManagementImpl
	 */
	private static Class<?> sun_management_VMManagementImpl = null;
	/**
	 * JVM的管理类，实现是sun.management.VMManagementImpl，是sun.management.RuntimeImpl的成员jvm
	 */
	private static Object instance_sun_management_VMManagementImpl = null;

	private static MethodHandle VMManagementImpl_getProcessId;

	/**
	 * 系统属性System.props
	 */
	private static Properties instance_Properties = null;

	private static Class<?> jdk_internal_loader_ClassLoaders = null;

	/**
	 * JVM参数 com.sun.management.internal.Flag
	 */
	private static Class<?> class_Flag;
	private static MethodHandle Flag_getFlag;
	private static MethodHandle Flag_getValue;
	private static MethodHandle Flag_setLongValue;
	private static MethodHandle Flag_setDoubleValue;
	private static MethodHandle Flag_setBooleanValue;
	private static MethodHandle Flag_setStringValue;

	static
	{
		instance_java_lang_management_RuntimeMXBean = ManagementFactory.getRuntimeMXBean();
		try
		{
			instance_sun_management_VMManagementImpl = reflection.read(instance_java_lang_management_RuntimeMXBean, "jvm");// 获取JVM管理类
			sun_management_VMManagementImpl = instance_sun_management_VMManagementImpl.getClass();
			VMManagementImpl_getProcessId = symbols.find_special_method(sun_management_VMManagementImpl, sun_management_VMManagementImpl, "getProcessId", int.class);// 获取进程ID的native方法
			// 获取系统属性引用
			instance_Properties = (Properties) reflection.read(System.class, "props");
			// 获取所有系统ClassLoaders
			jdk_internal_loader_ClassLoaders = Class.forName("jdk.internal.loader.ClassLoaders");
			// 虚拟机参数Flag类及其成员方法
			class_Flag = Class.forName("com.sun.management.internal.Flag");
			Flag_getFlag = symbols.find_static_method(class_Flag, "getFlag", class_Flag, String.class);
			Flag_setLongValue = symbols.find_static_method(class_Flag, "setLongValue", void.class, String.class, long.class);
			Flag_setDoubleValue = symbols.find_static_method(class_Flag, "setDoubleValue", void.class, String.class, double.class);
			Flag_setBooleanValue = symbols.find_static_method(class_Flag, "setBooleanValue", void.class, String.class, boolean.class);
			Flag_setStringValue = symbols.find_static_method(class_Flag, "setStringValue", void.class, String.class, String.class);
			Flag_getValue = symbols.find_special_method(class_Flag, class_Flag, "getValue", Object.class);
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * ------------------------ Java内存模型 --------------------------
	 * 每个Java对象都具有内存native地址，这个地址会随着GC移动而变化。<br>
	 * Java对象起始地址储存的是Object Header，即对象的JVM内部信息和元信息。对象头之后的连续内存是Java对象的成员数据。<br>
	 * 为了表征对象，需要为每个对象指定一个标识符，这个标识符就是oop。<br>
	 * oop本质只是个标识符，并不强制要求必须是根据native地址计算的，它也可以是JVM内部对象真实地址表的索引。但在Hotspot中，该值就是根据native地址计算的。<br>
	 * Java层操作的所有对象都是引用，即oop。<br>
	 * 以下结论均适用于Hotspot实现。<br>
	 * 1. oop通过native地址计算，因此oop会随着GC移动而变化。可以通过JVM实现中的算法直接根据oop的值计算出真实native地址。<br>
	 * 如果将java.lang.Object对象置于Object[]数组内，并读取数组中对应索引的long值，则该值是对象当前的实际oop。<br>
	 * oop压缩指将native地址取相对于JVM的堆内存基地址的偏移量并位移对象字节对齐值构成一个32位的oop，此时计算native地址还需要堆内存基地址。<br>
	 * 如果开启了oop压缩，则通过Object[]数组读取到的是压缩后的oop，需要解压缩才能使用。<br>
	 * 2. 未压缩的oop是一个指针，定义为oopDesc*，而oopDesc正是Object Header对象，即未压缩的oop实际指向了Java对象本身的内存的对象头地址。<br>
	 * 3. jobject是oop的别名，而未压缩的oop又直接指向Java对象内存。<br>
	 * oop可以通过JNIHandles::make_local()等函数转换成jobject，jobject通过JNIHandles::resolve()可转换为oop。<br>
	 * 对于local的jobject，jobject正是指向oop的指针。对于global和weak global的引用，则需要通过NativeAccess<>::oop_load(weak_global_ptr(jobject))函数获取oop。<br>
	 */

	/**
	 * OOP相关操作 https://github.com/openjdk/jdk/blob/9586817cea3f1cad8a49d43e9106e25dafa04765/src/hotspot/share/oops/compressedOops.cpp#L49<br>
	 * 对象头压缩/Klass压缩是指将对象头的Klass Word从64位压缩到32位的narrowKlass。<br>
	 * 开启UseCompressedOops后，默认开启Klass压缩，但oop是否压缩取决于分配的堆内存大小。
	 */

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
	private static final class __32_bit
	{
		// 32位JVM无OOP指针压缩
		public static final int header_offset = 0;
		public static final int header_length = 64;

		public static final int markword_offset = header_offset;
		public static final int markword_length = 32;
		public static final int klass_offset = markword_offset + markword_length;
		public static final int klassword_length = 32;

		public static final int identity_hashcode_offset = markword_offset;
		public static final int identity_hashcode_length = 25;
		public static final int age_offset = identity_hashcode_offset + identity_hashcode_length;
		public static final int age_length = 4;
		public static final int biased_lock_offset = age_offset + age_length;
		public static final int biased_lock_length = 1;

		public static final int lock_offset = biased_lock_offset + biased_lock_length;
		public static final int lock_length = 2;

		public static final int thread_offset = markword_offset;
		public static final int thread_length = 23;
		public static final int epoch_offset = thread_offset + thread_length;
		public static final int epoch_length = 2;

		public static final int ptr_to_lock_record_offset = markword_offset;
		public static final int ptr_to_lock_record_length = 30;

		public static final int ptr_to_heavyweight_monitor_offset = markword_offset;
		public static final int ptr_to_heavyweight_monitor_length = 30;
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
	private static final class __64_bit
	{
		// 64位JVM无OOP指针压缩
		public static final int header_offset = 0;
		public static final int header_length = 128;

		public static final int markword_offset = header_offset;
		public static final int markword_length = 64;
		public static final int klass_offset = markword_offset + markword_length;
		public static final int klassword_length = 64;

		public static final int unused_1_normal_offset = markword_offset;
		public static final int unused_1_normal_length = 25;
		public static final int identity_hashcode_offset = unused_1_normal_offset + unused_1_normal_length;
		public static final int identity_hashcode_length = 31;
		public static final int unused_2_normal_offset = identity_hashcode_offset + identity_hashcode_length;
		public static final int unused_2_normal_length = 1;
		public static final int age_offset = unused_2_normal_offset + unused_2_normal_length;
		public static final int age_length = 4;
		public static final int biased_lock_offset = age_offset + age_length;
		public static final int biased_lock_length = 1;
		public static final int lock_offset = biased_lock_offset + biased_lock_length;
		public static final int lock_length = 2;

		public static final int thread_offset = markword_offset;
		public static final int thread_length = 54;
		public static final int epoch_offset = thread_offset + thread_length;
		public static final int epoch_length = 2;
		public static final int unused_1_biased_offset = epoch_offset + epoch_length;
		public static final int unused_1_biased_length = 1;

		public static final int ptr_to_lock_record_offset = markword_offset;
		public static final int ptr_to_lock_record_length = 62;

		public static final int ptr_to_heavyweight_monitor_offset = markword_offset;
		public static final int ptr_to_heavyweight_monitor_length = 62;
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
	private static final class __64_bit_UseCompressedOops_UseCompressedClassPointers
	{
		// 64位JVM开启OOP指针压缩，JVM默认是开启的
		public static final int header_offset = 0;
		public static final int header_length = 96;

		public static final int markword_offset = header_offset;
		public static final int markword_length = 64;
		public static final int klass_offset = markword_offset + markword_length;
		public static final int klassword_length = 32;

		public static final int unused_1_normal_offset = markword_offset;
		public static final int unused_1_normal_length = 25;
		public static final int identity_hashcode_offset = unused_1_normal_offset + unused_1_normal_length;
		public static final int identity_hashcode_length = 31;
		public static final int cmd_free_offset = identity_hashcode_offset + identity_hashcode_length;
		public static final int cmd_free_length = 1;
		public static final int age_offset = cmd_free_offset + cmd_free_length;
		public static final int age_length = 4;
		public static final int biased_lock_offset = age_offset + age_length;
		public static final int biased_lock_length = 1;
		public static final int lock_offset = biased_lock_offset + biased_lock_length;
		public static final int lock_length = 2;

		public static final int thread_offset = markword_offset;
		public static final int thread_length = 54;
		public static final int epoch_offset = thread_offset + thread_length;
		public static final int epoch_length = 2;

		public static final int ptr_to_lock_record_offset = markword_offset;
		public static final int ptr_to_lock_record_length = 62;

		public static final int ptr_to_heavyweight_monitor_offset = markword_offset;
		public static final int ptr_to_heavyweight_monitor_length = 62;

	}

	/**
	 * 是否开启oop压缩，默认顺带开启对象头的klass word压缩（UseCompressedClassPointers）。
	 */
	private boolean UseCompressedOops;

	private boolean UseCompactObjectHeaders;

	private boolean UseCompressedClassPointers;

	/**
	 * 对象字节对齐，默认为8,必须是2的幂，一般来说是机器的数据字大小，即int类型大小。
	 */
	private long ObjectAlignmentInBytes;

	/**
	 * 堆内存base的最小地址。即堆的基地址
	 */
	private long HeapBaseMinAddress;
	/**
	 * 64位JVM开启UseCompressedOops的情况下，如果oop被压缩时，指向的地址有按位偏移。数值为log2(ObjectAlignmentInBytes)
	 */
	private int oop_encode_address_shift;

	/**
	 * JVM中压缩了oop时支持的最大堆内存大小，类型为ulong，实际上是32G
	 */
	private long OopEncodingHeapMax;

	/**
	 * 最大堆内存大小
	 */
	private long max_heap_size;

	/**
	 * 堆内存末尾在内存中的绝对地址
	 */
	private long heap_end_address;

	/**
	 * 堆内存的实际起始地址，大于等于HeapBaseMinAddress
	 */
	private long heap_base_address;

	/**
	 * 压缩oop时的位移
	 */
	private long oops_shift;

	/**
	 * 堆内存相对地址范围
	 */
	private long heap_address_range;

	/**
	 * 对象layout类型
	 */
	public static enum object_layout
	{
		/**
		 * JDK 24+<br>
		 * 开启对象头压缩，包含压缩klass pointer，即+UseCompressedClassPointers<br>
		 * +UseCompactObjectHeaders
		 */
		Compact(false,
				10,
				0,
				0,
				0,
				0),

		/**
		 * 压缩klass pointer。<br>
		 * +UseCompressedClassPointers，如果开启了+UseCompressedOops则该选项默认就是开启的
		 */
		Compressed(
				true,
				3, // static constexpr int max_shift_noncoh = 3;
				__64_bit_UseCompressedOops_UseCompressedClassPointers.markword_length,
				__64_bit_UseCompressedOops_UseCompressedClassPointers.klass_offset,
				__64_bit_UseCompressedOops_UseCompressedClassPointers.klassword_length,
				__64_bit_UseCompressedOops_UseCompressedClassPointers.header_length),
		/**
		 * 32位未压缩klass pointer，也未压缩对象头<br>
		 */
		Uncompressed32(
				false,
				0,
				__32_bit.markword_length,
				__32_bit.klass_offset,
				__32_bit.klassword_length,
				__32_bit.header_length),
		Uncompressed64(
				false,
				0,
				__64_bit.markword_length,
				__64_bit.klass_offset,
				__64_bit.klassword_length,
				__64_bit.header_length);

		public final boolean has_klass_gap;

		/**
		 * Klass Pointer位移多少位才能得到对象头的narrow klass。<br>
		 * 开启+UseCompressedClassPointers后该值为3，开启+UseCompactObjectHeaders后该值为10.<br>
		 * 此值为OpenJDk中硬编码的固定值，与OOP压缩的位移位数可能不同。<br>
		 */
		public final int narrow_klass_shift;

		/**
		 * Mark Word的长度，单位bit
		 */
		public final int markword_length;

		/**
		 * Klass Word的偏移量，单位bit
		 */
		public final int klass_word_offset;

		/**
		 * Klass Word的长度，单位bit
		 */
		public final int klass_word_length;

		/**
		 * 对象头总长度，单位bit
		 */
		public final int header_length;

		public final int header_byte_length;

		public long decode_narrow_klass(long heap_base, int narrow_klass)
		{
			return heap_base + ((narrow_klass & cxx_type.uint32_t_mask) << narrow_klass_shift);
		}

		public final long encode_narrow_klass(long heap_base, long klass_ptr)
		{
			return (int) ((klass_ptr - heap_base) >> narrow_klass_shift);
		}

		/**
		 * bit索引为8对齐的时使用的读取klass word的字节索引
		 */
		private int aligned_klass_word_byte_offset;

		private object_layout(boolean has_klass_gap, int narrow_klass_shift, int markword_length, int klass_word_offset, int klass_word_length, int header_length)
		{
			this.has_klass_gap = has_klass_gap;
			this.narrow_klass_shift = narrow_klass_shift;
			this.markword_length = markword_length;
			this.klass_word_offset = klass_word_offset;
			this.klass_word_length = klass_word_length;
			this.header_length = header_length;
			this.aligned_klass_word_byte_offset = klass_word_offset / 8;
			this.header_byte_length = header_length / 8;
		}

		/**
		 * 获取对象头
		 * 
		 * @param obj
		 * @return
		 */
		public final long get_klass_word(Object obj)
		{
			switch (klass_word_length)
			{
			case 32:
				return unsafe.read_int(obj, aligned_klass_word_byte_offset);
			case 22:
				return unsafe.le_read_bits(obj, 0, klass_word_offset, klass_word_length);
			case 64:
				return unsafe.read_long(obj, aligned_klass_word_byte_offset);
			default:
				throw new java.lang.InternalError("get klass word of '" + obj + "' failed");
			}
		}

		/**
		 * 强制改写对象头
		 * 
		 * @param obj
		 * @param klass_word
		 * @return
		 */
		public final void set_klass_word(Object obj, long klass_word)
		{
			switch (klass_word_length)
			{
			case 32:
				unsafe.write(obj, aligned_klass_word_byte_offset, (int) klass_word);
				break;
			case 22:
				unsafe.le_write_bits(obj, 0, klass_word_offset, klass_word, klass_word_length);
			case 64:
				unsafe.write(obj, aligned_klass_word_byte_offset, klass_word);
				break;
			default:
				throw new java.lang.InternalError("set klass word of '" + obj + "' failed");
			}
		}

		public final void set_klass_word(long addr, long klass_word)
		{
			switch (klass_word_length)
			{
			case 32:
				unsafe.write(null, addr + aligned_klass_word_byte_offset, (int) klass_word);
				break;
			case 22:
				unsafe.le_write_bits(null, addr, klass_word_offset, klass_word, klass_word_length);
			case 64:
				unsafe.write(null, addr + aligned_klass_word_byte_offset, klass_word);
				break;
			default:
				throw new java.lang.InternalError("set klass word of oop '" + addr + "' to '" + klass_word + "' failed");
			}
		}
	}

	private object_layout object_layout_type;

	private virtual_machine()
	{
		this.update_vm_info();
	}

	public static final virtual_machine host = new virtual_machine();

	public long get_header_byte_length()
	{
		return object_layout_type.header_byte_length;
	}

	/**
	 * Java的null地址，对应堆内存的基地址0偏移处。堆内存起始地址并不一定是0.
	 */
	public final long heap_base()
	{
		return oop_of(null);
	}

	public final void update_vm_info()
	{
		if (on_64bit_jvm)
		{
			// 64位JVM需要检查是否启用了指针压缩
			UseCompressedOops = get_bool_option("UseCompressedOops");
			try
			{
				UseCompressedClassPointers = virtual_machine.get_bool_option("UseCompressedClassPointers");
			}
			catch (Throwable ex)
			{
				if (UseCompressedOops)
				{
					UseCompressedClassPointers = true;// 当UseCompressedOops为true时，该选项默认打开，除非手动关闭
				}
				else
				{
					UseCompressedClassPointers = false;
				}
			}
		}
		ObjectAlignmentInBytes = get_long_option("ObjectAlignmentInBytes");
		HeapBaseMinAddress = get_long_option("HeapBaseMinAddress");
		try
		{
			UseCompactObjectHeaders = virtual_machine.get_bool_option("UseCompactObjectHeaders");// JDK25+压缩对象头，压缩后变为8字节
		}
		catch (Throwable ex)
		{
			// 获取不存在的Flag时会抛出异常，为了适配低版本JVM可能没有相应的标志，需要捕获错误但不操作
		}
		oop_encode_address_shift = uint64_log2(ObjectAlignmentInBytes);
		OopEncodingHeapMax = UnscaledOopHeapMax << oop_encode_address_shift;// 使用OOP压缩编码后支持的最大的堆内存
		// 堆相关信息
		max_heap_size = virtual_machine.max_heap_size();
		heap_end_address = HeapBaseMinAddress + max_heap_size;// 这是最大的范围，实际范围可能只是其中一段区间。
		if (heap_end_address > UnscaledOopHeapMax || UseCompressedOops)
		{
			// 实际堆内存终止地址大于不压缩oop时支持的最大地址，则需要压缩oop，哪怕没启用UseCompressedOops也会自动开启压缩。
			// 指定了UseCompressedOops后则必定压缩。
			oops_shift = oop_encode_address_shift;
			UseCompressedOops = true;
		}
		else
		{
			// 堆内存的末尾绝对地址小于不压缩oop时支持的最大地址就不压缩
			oops_shift = 0;
		}
		if (heap_end_address <= OopEncodingHeapMax)
		{
			// 只要未压缩或者压缩后的指针的最大地址仍然小于堆的OOP编码地址范围(ObjectAlignmentInBytes为8时，此值为32G)，就可以将堆的基地址设置为0
			heap_base_address = 0;
		}
		else
		{
			// 当堆的终止地址大于OOP编码范围后，堆的起始地址必须加上偏移量，不能从0开始。同时，Java中null的地址就是堆地址的起始地址，其相对于堆的地址为0.
			heap_base_address = heap_base();
		}
		heap_address_range = heap_end_address - heap_base_address;
		// 对象头信息内存布局
		switch (jvm_bit_version)
		{
		case 32:
		{
			object_layout_type = object_layout.Uncompressed32;
			break;
		}
		case 64:
		{
			if (UseCompactObjectHeaders)
			{
				object_layout_type = object_layout.Compact;
			}
			else if (UseCompressedOops && UseCompressedClassPointers)
			{
				object_layout_type = object_layout.Compressed;
			}
			else
			{
				object_layout_type = object_layout.Uncompressed64;
			}
			break;
		}
		default:
		{
			throw new java.lang.InternalError("unknown native jvm bit-version '" + jvm_bit_version + "'");
		}
		}
	}

	public final long get_heap_address_range()
	{
		return heap_address_range;
	}

	public final object_layout get_object_layout()
	{
		return object_layout_type;
	}

	/**
	 * 堆上的地址
	 * 
	 * @param native_addr
	 * @return
	 */
	public final long address_on_heap(long native_addr)
	{
		return native_addr - heap_base_address;
	}

	/**
	 * 编码压缩oop<br>
	 * oop.encode_heap_oop_not_null
	 * 
	 * @param native_addr
	 * @return
	 */
	public final int encode_oop(long native_addr)
	{
		return (int) (address_on_heap(native_addr) >> oops_shift);
	}

	/**
	 * 解码压缩oop，位移可能为0，此时表示未压缩的相对于堆起始位置的相对地址.
	 * 
	 * @param oop
	 * @return
	 */
	public final long decode_oop(int oop)
	{
		return heap_base_address + ((oop & cxx_type.uint32_t_mask) << oops_shift);
	}

	/**
	 * 从压缩或未压缩的OOP获取对象真实内存地址
	 * 
	 * @param oop
	 * @return
	 */
	public final long address_of_oop(long oop)
	{
		if (UseCompressedOops)
		{
			return decode_oop((int) oop);
		}
		else
		{
			return oop;
		}
	}

	/**
	 * 获取对象（压缩后的）的oop，返回long<br>
	 * 利用Object[]的元素为oop指针的事实来间接取oop。<br>
	 * 在32位和未启用UseCompressedOops的64位JVM上，取的地址是未压缩的oop，直接指向Java对象本身的内存（对象头）。<br>
	 * 在开启UseCompressedOops的64位JVM上，取的oop是压缩后的，需要乘以字节对齐量（字节对齐默认为8）或者左移（3位）+堆的基地址（即Java中null的绝对地址）才是绝对地址。
	 * 
	 * @param object
	 * @return
	 */
	public static final long oop_of(Object object)
	{
		Object[] _a = new Object[]
		{ object };
		switch (unsafe.array_object_index_scale)
		{
		case 4:
			return unsafe.read_int(_a, unsafe.array_object_base_offset);
		case 8:
			return unsafe.read_long(_a, unsafe.array_object_base_offset);
		default:
			return 0;
		}
	}

	public final int oop_of_address(long addr)
	{
		if (UseCompressedOops)
		{
			return encode_oop(addr);
		}
		else
		{
			return (int) addr;
		}
	}

	/**
	 * 从（压缩后的）oop获取Java对象。<br>
	 * 如果开启了oop压缩，则必须传入压缩后的oop。<br>
	 * 
	 * @param oop
	 * @return
	 */
	public static final Object resolve_oop(long oop)
	{
		Object[] _a = new Object[1];
		switch (unsafe.array_object_index_scale)
		{
		case 4:
			unsafe.write(_a, unsafe.array_object_base_offset, (int) oop);
		case 8:
			unsafe.write(_a, unsafe.array_object_base_offset, oop);
		}
		return _a[0];
	}

	/**
	 * 获取未压缩的oop，该地址为Java对象的实际内存地址
	 * 
	 * @param object
	 * @return
	 */
	public final long address_of(Object object)
	{
		return address_of_oop((int) oop_of(object));
	}

	public final Object resolve_address(long addr)
	{
		return resolve_oop(oop_of_address(addr));
	}

	/**
	 * 压缩Klass Pointer，实际上压缩方式和OOP一致
	 * 
	 * @param klass_ptr
	 * @return
	 */
	public final long encode_narrow_klass(long klass_ptr)
	{
		return object_layout_type.encode_narrow_klass(heap_base_address, klass_ptr);
	}

	/**
	 * 如果开启了UseCompressedClassPointers或+UseCompactObjectHeaders，则需要用此方法解压缩对象头的Klass Word才能得到Klass*指针。
	 * 从压缩或未压缩的KlassWord获取Klass*地址。<br>
	 * 该地址位于metaspace，地址是不变的，可以长期使用。<br>
	 * 
	 * @param narrow_klass
	 * @return
	 */
	public final long decode_narrow_klass(int narrow_klass)
	{
		return object_layout_type.decode_narrow_klass(heap_base_address, narrow_klass);
	}

	/**
	 * 从压缩或未压缩的KlassWord获取Klass*地址。<br>
	 * 该地址位于metaspace，地址是不变的，可以长期使用。<br>
	 * 
	 * @param oop
	 * @return
	 */
	public final long klass_pointer_of_klass_word(long klass_word)
	{
		return decode_narrow_klass((int) klass_word);
	}

	public final long klass_word_of_klass_pointer(long klass_ptr)
	{
		return encode_narrow_klass(klass_ptr);
	}

	/**
	 * 获取Klass*指针
	 * 
	 * @param clazz
	 * @return
	 */
	public final long klass_pointer_of(Class<?> clazz)
	{
		return klass_pointer_of_klass_word(get_klass_word(clazz));
	}

	/**
	 * Klass Word缓存，频繁通过分配对象获取Klass Word性能开销大
	 */
	private final HashMap<Class<?>, Long> klass_word_cache = new HashMap<>();

	public final long get_klass_word(Class<?> clazz)
	{
		return klass_word_cache.computeIfAbsent(clazz, (c) -> get_klass_word(unsafe.allocate(c)));
	}

	public final long get_klass_word(Object obj)
	{
		return object_layout_type.get_klass_word(obj);
	}

	public final void set_klass_word(Object obj, long klass_word)
	{
		object_layout_type.set_klass_word(obj, klass_word);
	}

	public final void set_klass_word(long oop, long klass_word)
	{
		object_layout_type.set_klass_word(oop, klass_word);
	}

	/**
	 * 使用反射获取系统的内建ClassLoader
	 * 
	 * @param class_loader_name
	 * @return
	 */
	public static final Object builtin_class_loaders(String class_loader_name)
	{
		try
		{
			return reflection.read(jdk_internal_loader_ClassLoaders, class_loader_name);
		}
		catch (SecurityException | IllegalArgumentException ex)
		{
			throw new java.lang.InternalError("class loader name can only be BOOT_LOADER | PLATFORM_LOADER | APP_LOADER", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public static final ArrayList<URL> builtin_class_loader_classpath(String class_loader_name)
	{
		try
		{
			Object class_loader = builtin_class_loaders(class_loader_name);
			Object url_classpath = reflection.read(class_loader, "ucp");// jdk.internal.loader.URLClassPath
			if (url_classpath != null)
				return (ArrayList<URL>) reflection.read(url_classpath, "path");
			else
				return null;// BOOT_LOADER的ucp为null
		}
		catch (SecurityException | IllegalArgumentException ex)
		{
			throw new java.lang.InternalError("get class loader '" + class_loader_name + "' classpath failed", ex);
		}
	}

	/**
	 * 无视权限获取系统属性
	 * 
	 * @param key
	 * @return
	 */
	public static final String get_property(String key)
	{
		return instance_Properties.getProperty(key);
	}

	/**
	 * 获取运行时的Java版本号
	 * 
	 * @return
	 */
	public static final String java_version()
	{
		return get_property("java.runtime.version");
	}

	/**
	 * 无视权限设置系统属性
	 * 
	 * @param key
	 * @param value
	 */
	public static final void set_property(String key, String value)
	{
		instance_Properties.setProperty(key, value);
	}

	/**
	 * 无视操作权限设置JVM的参数设置，必须自己确保value的类型与JVM的参数类型一致！调用的是native方法，但不是所有参数都支持运行时修改。大部分标志无法成功设置，因为检测可写标志在native方法内，无法干涉
	 * 
	 * @param name
	 * @param value
	 */
	public static final void set_option(String name, Object value)
	{
		try
		{
			Object flag = Flag_getFlag.invoke(name);
			Object v = Flag_getValue.invoke(class_Flag.cast(flag));
			VarHandle writeable = symbols.find_var(class_Flag, "writeable", boolean.class);
			writeable.set(flag, true);
			if (v instanceof Long lv)
				Flag_setLongValue.invokeExact(name, lv.longValue());
			if (v instanceof Double dv)
				Flag_setDoubleValue.invokeExact(name, dv.doubleValue());
			if (v instanceof Boolean bv)
				Flag_setBooleanValue.invokeExact(name, bv.booleanValue());
			if (v instanceof String sv)
				Flag_setStringValue.invokeExact(name, (String) sv);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("set option '" + name + "' to '" + value + "' failed", ex);
		}
	}

	/**
	 * 获取当前JVM的进程ID（也称PID）
	 * 
	 * @return
	 */
	public static final long process_id()
	{
		try
		{
			return (long) VMManagementImpl_getProcessId.invoke(instance_sun_management_VMManagementImpl);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get process id failed", ex);
		}
	}

	/**
	 * Java Agent相关功能
	 */
	private static Class<?> sun_tools_attach_HotSpotVirtualMachine;// sun.tools.attach.HotSpotVirtualMachine
	static
	{
		virtual_machine.set_property("jdk.attach.allowAttachSelf", "true");// 并非实际允许调用，只是记录在系统中
		try
		{
			sun_tools_attach_HotSpotVirtualMachine = Class.forName("sun.tools.attach.HotSpotVirtualMachine");
			unsafe.write(sun_tools_attach_HotSpotVirtualMachine, "ALLOW_ATTACH_SELF", true);// 设置instrument可以从程序attach，不需要在启动JVM时传入参数jdk.attach.allowAttachSelf=true
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 为指定的进程添加Agent
	 * 
	 * @param PID        要添加的JVM进程ID
	 * @param agent_path Agent的jar文件绝对路径
	 * @param args       传递给Agent的参数
	 */
	public static final void attach(long pid, String agent_path, String args)
	{
		try
		{
			VirtualMachine jvm = VirtualMachine.attach(String.valueOf(pid));
			jvm.loadAgent(agent_path, args);
			jvm.detach();
		}
		catch (AttachNotSupportedException | IOException | AgentLoadException | AgentInitializationException ex)
		{
			throw new java.lang.InternalError("attach agent '" + agent_path + "' to process '" + pid + "' failed", ex);
		}
	}

	public static final void attach(long pid, String agent_path)
	{
		attach(pid, agent_path, null);
	}

	/**
	 * 为当前进程添加Agent
	 * 
	 * @param agent_path Agent的jar文件绝对路径
	 * @param args       传递给Agent的参数
	 */
	public static final void attach(String agent_path, String args)
	{
		attach(virtual_machine.process_id(), agent_path, args);
	}

	public static final void attach(String agent_path)
	{
		attach(agent_path, null);
	}

	/**
	 * 宿主机的操作系统信息
	 */
	public static enum platform
	{
		windows, linux, macos;

		public static final platform host;

		private static final platform get_host_platform()
		{
			switch (shared_object.abi.host_cabi())
			{
			case "SYS_V":
			case "LINUX_AARCH_64":
			case "LINUX_PPC_64_LE":
			case "LINUX_RISCV_64":
			case "LINUX_S390":
				return linux;
			case "WIN_64":
			case "WIN_AARCH_64":
				return windows;
			case "MAC_OS_AARCH_64":
				return macos;
			case "FALLBACK":
			case "UNSUPPORTED":
			default:
				return null;
			}
		}

		static
		{
			host = get_host_platform();
		}
	}

	/**
	 * 宿主机的架构信息
	 */
	public static enum architecture
	{
		x86_64("x64", "X86_64Architecture"),
		aarch64("aarch64", "AArch64Architecture"),
		power_pc("ppc64", "PPC64Architecture"),
		riscv64("riscv64", "RISCV64Architecture"),
		s390("s390", "S390Architecture");

		Class<?> _internal_class;
		Class<?> _internal_storage_class;

		public static final architecture host;

		private static final architecture get_host_architecture()
		{
			switch (shared_object.abi.host_cabi())
			{
			case "SYS_V":
			case "WIN_64":
				return x86_64;
			case "LINUX_AARCH_64":
			case "MAC_OS_AARCH_64":
			case "WIN_AARCH_64":
				return aarch64;
			case "LINUX_PPC_64_LE":
				return power_pc;
			case "LINUX_RISCV_64":
				return riscv64;
			case "LINUX_S390":
				return s390;
			case "FALLBACK":
			case "UNSUPPORTED":
			default:
				return null;
			}
		}

		static
		{
			host = get_host_architecture();
		}

		private architecture(String abi_pkg_name, String architecture_class_name)
		{
			try
			{
				// jdk.internal.foreign.abi.<abi_pkg_name>.<architecture_class_name>为实际类名
				_internal_class = Class.forName("jdk.internal.foreign.abi." + abi_pkg_name + "." + architecture_class_name);
				_internal_storage_class = _internal_inner_class("StorageType");
			}
			catch (ClassNotFoundException ex)
			{
				throw new java.lang.InternalError("get internal architecture of '" + this.name() + "' failed", ex);
			}
		}

		Class<?> _internal_inner_class(String inner_class_name)
		{
			try
			{
				return Class.forName(_internal_class.getName() + "$" + inner_class_name);
			}
			catch (ClassNotFoundException ex)
			{
				throw new java.lang.InternalError("get inner class '" + inner_class_name + "' of '" + _internal_class + "' failed", ex);
			}
		}
	}

	public static final class storage
	{
		public static enum type
		{
			INTEGER,
			FLOAT,
			VECTOR, // x86_64专属
			X87, // x86_64专属
			STACK,
			PLACEHOLDER;

			private byte type_id;

			private type()
			{
				try
				{
					type_id = (byte) symbols.find_static_var(architecture.host._internal_storage_class, this.name(), byte.class).get();
				}
				catch (Throwable ex)
				{
					// 未找到则说明该架构无该类型的StorageType
					type_id = -1;
				}
			}

			public final boolean is_available()
			{
				return type_id >= 0;
			}

			public final byte type_id()
			{
				return type_id;
			}

			public final Object allocate(short segment_mask_or_size, int index_or_offset, String debug_name)
			{
				if (this.is_available())
				{
					return storage.allocate(type_id, segment_mask_or_size, index_or_offset, debug_name);
				}
				else
				{
					throw new java.lang.InternalError("storage '" + debug_name + "' of type '" + this.name() + "' is not available on " + architecture.host);
				}
			}

			public final Object allocate(short segment_mask_or_size, int index_or_offset)
			{
				return allocate(segment_mask_or_size, index_or_offset, this.name() + "@" + index_or_offset + "$" + segment_mask_or_size);
			}
		}

		private static Class<?> jdk_internal_foreign_abi_VMStorage;

		private static Field VMStorage_type;
		private static Field VMStorage_segmentMaskOrSize;
		private static Field VMStorage_indexOrOffset;
		private static Field VMStorage_debugName;

		static
		{
			try
			{
				jdk_internal_foreign_abi_VMStorage = Class.forName("jdk.internal.foreign.abi.VMStorage");
				VMStorage_type = reflection.find_declared_field(jdk_internal_foreign_abi_VMStorage, "type");
				VMStorage_segmentMaskOrSize = reflection.find_declared_field(jdk_internal_foreign_abi_VMStorage, "segmentMaskOrSize");
				VMStorage_indexOrOffset = reflection.find_declared_field(jdk_internal_foreign_abi_VMStorage, "indexOrOffset");
				VMStorage_debugName = reflection.find_declared_field(jdk_internal_foreign_abi_VMStorage, "debugName");
			}
			catch (ClassNotFoundException ex)
			{
				ex.printStackTrace();
			}
		}

		public static final Object set_type(Object vm_storage, byte type)
		{
			unsafe.write(vm_storage, VMStorage_type, type);
			return vm_storage;
		}

		public static final Object set_type(Object vm_storage, short segment_mask_or_size)
		{
			unsafe.write(vm_storage, VMStorage_segmentMaskOrSize, segment_mask_or_size);
			return vm_storage;
		}

		public static final Object set_type(Object vm_storage, int index_or_offset)
		{
			unsafe.write(vm_storage, VMStorage_indexOrOffset, index_or_offset);
			return vm_storage;
		}

		public static final Object set_type(Object vm_storage, String debug_name)
		{
			unsafe.write(vm_storage, VMStorage_debugName, debug_name);
			return vm_storage;
		}

		public static final Object[] new_array(int size)
		{
			return (Object[]) Array.newInstance(jdk_internal_foreign_abi_VMStorage, size);
		}

		public static final IntFunction<Object[]> _new = (int size) -> new_array(size);

		public static final Object allocate(byte type, short segment_mask_or_size, int index_or_offset, String debug_name)
		{
			Object vm_storage = unsafe.allocate(jdk_internal_foreign_abi_VMStorage);
			unsafe.write(vm_storage, VMStorage_type, type);
			unsafe.write(vm_storage, VMStorage_segmentMaskOrSize, segment_mask_or_size);
			unsafe.write(vm_storage, VMStorage_indexOrOffset, index_or_offset);
			unsafe.write(vm_storage, VMStorage_debugName, debug_name);
			return vm_storage;
		}

		/**
		 * 为C++类型分配VMStrorage栈空间
		 * 
		 * @param types
		 * @return
		 */
		public static final Object[] allocate_stack(cxx_type... types)
		{
			Object[] vm_storage_arr = new_array(types.length);
			for (int idx = 0; idx < types.length; ++idx)
			{
				vm_storage_arr[idx] = type.STACK.allocate((short) cxx_type.sizeof(types[idx]), 0, types[idx].typename());
			}
			return vm_storage_arr;
		}

		public static final Object[] wrap_memory(cxx_type... types)
		{
			Object[] vm_storage_arr = new_array(types.length);
			for (int idx = 0; idx < types.length; ++idx)
			{
				vm_storage_arr[idx] = type.PLACEHOLDER.allocate((short) cxx_type.sizeof(types[idx]), idx, types[idx].typename());
			}
			return vm_storage_arr;
		}
	}
}

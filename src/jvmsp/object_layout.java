package jvmsp;

import jvmsp.hotspot.vm_struct.CompressedKlassPointers;

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
public class object_layout
{
	/**
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
	* |                  Mark Word (32 bits)                  |       Klass Word (32 bits)     |                    |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* | identity_hashcode:25 | age:4 | biased_lock:1 | lock:2 |          Klass pointer         |       Normal       |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |  thread:23 | epoch:2 | age:4 | biased_lock:1 | lock:2 |          Klass pointer         |       Biased       |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |               ptr_to_lock_record:30          | lock:2 |          Klass pointer         | Lightweight Locked |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |               ptr_to_heavyweight_monitor:30  | lock:2 |          Klass pointer         | Heavyweight Locked |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |                                              | lock:2 |          Klass pointer         |    Marked for GC   |<br>
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
	* |                                  Mark Word (64 bits)                         |     Klass Word (64 bits)    |                    |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* | unused:25 | identity_hashcode:31 | unused:1 | age:4 | biased_lock:1 | lock:2 |        Klass pointer        |       Normal       |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* | thread:54 |       epoch:2        | unused:1 | age:4 | biased_lock:1 | lock:2 |        Klass pointer        |       Biased       |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                       ptr_to_lock_record:62                         | lock:2 |        Klass pointer        | Lightweight Locked |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                     ptr_to_heavyweight_monitor:62                   | lock:2 |        Klass pointer        | Heavyweight Locked |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                                                                     | lock:2 |        Klass pointer        |    Marked for GC   |<br>
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
	* ObjectHeader 64-bit JVM +UseCompressedOops +UseCompressedClassPointers<br>
	* |--------------------------------------------------------------------------------------------------------------|--------------------|<br>
	* |                                            Object Header (96 bits)                                           |        State       |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                                  Mark Word (64 bits)                           |     Klass Word (32 bits)    |                    |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* | unused:25 | identity_hashcode:31 | cms_free:1 | age:4 | biased_lock:1 | lock:2 |   Compressed Klass pointer  |       Normal       |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* | thread:54 |       epoch:2        | cms_free:1 | age:4 | biased_lock:1 | lock:2 |   Compressed Klass pointer  |       Biased       |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                         ptr_to_lock_record                            | lock:2 |   Compressed Klass pointer  | Lightweight Locked |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                     ptr_to_heavyweight_monitor                        | lock:2 |   Compressed Klass pointer  | Heavyweight Locked |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                                                                       | lock:2 |   Compressed Klass pointer  |    Marked for GC   |<br>
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

	public static enum object_header_layout
	{
		/**
		 * JDK 24+<br>
		 * 开启对象头压缩，包含压缩klass pointer，即+UseCompressedClassPointers<br>
		 * +UseCompactObjectHeaders
		 */
		Compact(
				false,
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
				__64_bit_UseCompressedOops_UseCompressedClassPointers.markword_length,
				__64_bit_UseCompressedOops_UseCompressedClassPointers.klass_offset,
				__64_bit_UseCompressedOops_UseCompressedClassPointers.klassword_length,
				__64_bit_UseCompressedOops_UseCompressedClassPointers.header_length),
		/**
		 * 32位未压缩klass pointer，也未压缩对象头<br>
		 */
		Uncompressed32(
				false,
				__32_bit.markword_length,
				__32_bit.klass_offset,
				__32_bit.klassword_length,
				__32_bit.header_length),
		Uncompressed64(
				false,
				__64_bit.markword_length,
				__64_bit.klass_offset,
				__64_bit.klassword_length,
				__64_bit.header_length);

		public final boolean has_klass_gap;

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

		public static final long decode_narrow_klass(int narrow_klass)
		{
			return CompressedKlassPointers.decode(narrow_klass);
		}

		public static final long encode_narrow_klass(long klass_ptr)
		{
			return CompressedKlassPointers.encode(klass_ptr);
		}

		/**
		 * bit索引为8对齐的时使用的读取klass word的字节索引
		 */
		private final int klass_word_begin_byte_offset;

		/**
		 * Klass Pointer位移多少位才能得到对象头的narrow klass。<br>
		 * 开启+UseCompressedClassPointers后该值为3，开启+UseCompactObjectHeaders后该值为10.<br>
		 * 此值为OpenJDk中硬编码的固定值，与OOP压缩的位移位数可能不同。<br>
		 */

		private object_header_layout(boolean has_klass_gap, int markword_length, int klass_word_offset, int klass_word_length, int header_length)
		{
			this.has_klass_gap = has_klass_gap;
			this.markword_length = markword_length;
			this.klass_word_offset = klass_word_offset;
			this.klass_word_length = klass_word_length;
			this.header_length = header_length;
			this.klass_word_begin_byte_offset = (int) Math.floor(klass_word_offset / 8.0);
			this.header_byte_length = (int) Math.ceil(header_length / 8.0);
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
				return unsafe.read_int(obj, klass_word_begin_byte_offset);
			case 22:
				return unsafe.le_read_bits(obj, 0, klass_word_offset, klass_word_length);
			case 64:
				return unsafe.read_long(obj, klass_word_begin_byte_offset);
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
				unsafe.write(obj, klass_word_begin_byte_offset, (int) klass_word);
				break;
			case 22:
				unsafe.le_write_bits(obj, 0, klass_word_offset, klass_word, klass_word_length);
			case 64:
				unsafe.write(obj, klass_word_begin_byte_offset, klass_word);
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
				unsafe.write(null, addr + klass_word_begin_byte_offset, (int) klass_word);
				break;
			case 22:
				unsafe.le_write_bits(null, addr, klass_word_offset, klass_word, klass_word_length);
			case 64:
				unsafe.write(null, addr + klass_word_begin_byte_offset, klass_word);
				break;
			default:
				throw new java.lang.InternalError("set klass word of oop '" + addr + "' to '" + klass_word + "' failed");
			}
		}
	}
}
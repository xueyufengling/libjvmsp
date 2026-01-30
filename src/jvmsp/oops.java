package jvmsp;

/**
 * https://github.com/openjdk/jdk/blob/9586817cea3f1cad8a49d43e9106e25dafa04765/src/hotspot/share/oops/compressedOops.cpp#L49<br>
 * oop压缩相关常量。是否会运行时动态变更未知。<br>
 * oop压缩指将绝对地址取相对于堆base的偏移量并位移构成一个32位的oop。<br>
 * 对象头压缩/Klass压缩是指将对象头的Klass Word从64位压缩到32位的narrowKlass。<br>
 * 开启UseCompressedOops后，默认开启Klass压缩，但oop是否压缩取决于分配的堆内存大小。
 */
public class oops {
	/**
	 * 压缩模式
	 */
	public static enum Mode {
		UnscaledNarrowOop, // 无压缩
		ZeroBasedNarrowOop, // 压缩，基地址为0
		DisjointBaseNarrowOop, //
		HeapBasedNarrowOop;// 压缩，基地址非0
	};

	Mode mode;

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

	static {
		max_heap_size = virtual_machine.max_heap_size();
		heap_space_end = virtual_machine.HeapBaseMinAddress + max_heap_size;// 这是最大的范围，实际范围可能只是其中一段区间，这种方法或许并不准确。
		if (heap_space_end > virtual_machine.UnscaledOopHeapMax) {// 实际堆内存大小大于不压缩oop时支持的最大地址，则需要压缩oop，哪怕没启用UseCompressedOops也会自动开启压缩。
			shift = virtual_machine.OOP_ENCODE_ADDRESS_SHIFT;
		} else if (virtual_machine.UseCompressedOops)// 指定了UseCompressedOops后则必定压缩。
			shift = virtual_machine.OOP_ENCODE_ADDRESS_SHIFT;
		else// 堆内存的末尾绝对地址小于4GB就不压缩
			shift = 0;
		if (heap_space_end <= virtual_machine.OopEncodingHeapMax) {
			base = 0;
		} else {
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
	public static final int encode(long native_addr) {
		return (int) ((native_addr - base) >> shift);
	}

	public static final long pointer_delta(long native_addr) {
		return native_addr - base;
	}

	/**
	 * 解码压缩oop，位移可能为0，此时表示未压缩的相对于堆起始位置的相对地址.
	 * 
	 * @param oop_addr
	 * @return
	 */
	public static final long decode(int oop_addr) {
		return ((oop_addr & cxx_stdtypes.UINT32_T_MASK) << shift) + base;
	}

	/**
	 * markWord
	 */

	private static abstract class __obj_header_base {
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
	private static final class __32_bit extends __obj_header_base {
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
	private static final class __64_bit_no_UseCompressedOops extends __obj_header_base {

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
	private static final class __64_bit_with_UseCompressedOops extends __obj_header_base {
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

	static {
		if (virtual_machine.NATIVE_JVM_BIT_VERSION == 32) {
			MARKWORD_LENGTH = __32_bit.MARKWORD_LENGTH;
			KLASS_WORD_OFFSET = __32_bit.KLASS_OFFSET;
			KLASS_WORD_LENGTH = __32_bit.KLASS_LENGTH;
			HEADER_LENGTH = __32_bit.HEADER_LENGTH;
		} else if (virtual_machine.NATIVE_JVM_BIT_VERSION == 64) {
			if (virtual_machine.UseCompressedOops) {
				MARKWORD_LENGTH = __64_bit_with_UseCompressedOops.MARKWORD_LENGTH;
				KLASS_WORD_OFFSET = __64_bit_with_UseCompressedOops.KLASS_OFFSET;
				KLASS_WORD_LENGTH = __64_bit_with_UseCompressedOops.KLASS_LENGTH;
				HEADER_LENGTH = __64_bit_with_UseCompressedOops.HEADER_LENGTH;
			} else {
				MARKWORD_LENGTH = __64_bit_no_UseCompressedOops.MARKWORD_LENGTH;
				KLASS_WORD_OFFSET = __64_bit_no_UseCompressedOops.KLASS_OFFSET;
				KLASS_WORD_LENGTH = __64_bit_no_UseCompressedOops.KLASS_LENGTH;
				HEADER_LENGTH = __64_bit_no_UseCompressedOops.HEADER_LENGTH;
			}
		} else {
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

	public static final long get_klass_word(Class<?> c) {
		return get_klass_word(unsafe.allocate(c));
	}

	/**
	 * 获取对象头
	 * 
	 * @param obj
	 * @return
	 */
	public static final long get_klass_word(Object obj) {
		if (KLASS_WORD_LENGTH == 32)
			return unsafe.read_int(obj, KLASS_WORD_BYTE_OFFSET);
		else if (KLASS_WORD_LENGTH == 64)
			return unsafe.read_long(obj, KLASS_WORD_BYTE_OFFSET);
		else
			return 0;
	}

	/**
	 * 强制改写对象头
	 * 
	 * @param obj
	 * @param klassWord
	 * @return
	 */
	public static final boolean set_klass_word(Object obj, long klassWord) {
		if (KLASS_WORD_LENGTH == 32) {
			unsafe.write(obj, KLASS_WORD_BYTE_OFFSET, (int) klassWord);
			return true;
		} else if (KLASS_WORD_LENGTH == 64) {
			unsafe.write(obj, KLASS_WORD_BYTE_OFFSET, klassWord);
			return true;
		}
		return false;
	}

	public static final boolean set_klass_word(long obj_base, long klassWord) {
		if (KLASS_WORD_LENGTH == 32) {
			unsafe.write(null, obj_base + KLASS_WORD_BYTE_OFFSET, (int) klassWord);
			return true;
		} else if (KLASS_WORD_LENGTH == 64) {
			unsafe.write(null, obj_base + KLASS_WORD_BYTE_OFFSET, klassWord);
			return true;
		}
		return false;
	}
}

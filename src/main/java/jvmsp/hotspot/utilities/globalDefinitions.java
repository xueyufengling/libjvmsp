package jvmsp.hotspot.utilities;

import jvmsp.algorithms;
import jvmsp.type.cxx_type;
import jvmsp.virtual_machine;
import jvmsp.hotspot.vm_constant;

public class globalDefinitions
{
	public static final int ASSERT = vm_constant.find_int("ASSERT");

	public static final int oopSize = vm_constant.find_int("oopSize");

	public static final int LogBitsPerByte = 3;
	public static final int BitsPerByte = 1 << LogBitsPerByte;

	public static final int LogBytesPerInt = 2;
	public static final int BytesPerInt = 1 << LogBytesPerInt;
	public static final int BitsPerInt = BytesPerInt * BitsPerByte;

	public static final int LogBytesPerWord = vm_constant.find_int("LogBytesPerWord");
	public static final int BytesPerWord = vm_constant.find_int("BytesPerWord");
	public static final int BytesPerLong = vm_constant.find_int("BytesPerLong");
	// wordSize == HeapWordSize 且 wordSize == BytesPerWord
	public static final int HeapWordSize = vm_constant.find_int("HeapWordSize");

	public static final int LogHeapWordSize = vm_constant.find_int("LogHeapWordSize");

	/**
	 * uint32_t的最大值，用于掩码和计算32位机器最大寻址地址。
	 */
	public static final long max_juint = 0xFFFFFFFFL;

	/**
	 * JVM中未压缩的32位oop时支持的最大堆内存大小，类型为uint，该值为常数，即固定为4G
	 */
	public static final long UnscaledOopHeapMax = max_juint + 1;// 2^32+1;

	/**
	 * 对象的对齐字节数，一般是8字节对齐
	 */
	public static final long ObjectAlignmentInBytes;
	public static final long MinObjAlignmentInBytes;
	/**
	 * 对象的最小对齐
	 */
	public static final long MinObjAlignment;

	/**
	 * 对齐的掩码
	 */
	public static final long MinObjAlignmentInBytesMask;

	/**
	 * 对象对齐字节长度为2的LogMinObjAlignmentInBytes次幂
	 */
	public static final long LogMinObjAlignmentInBytes;

	public static final long LogMinObjAlignment;

	/**
	 * JVM中压缩了oop时支持的最大堆内存大小，类型为ulong，实际上是32G
	 */
	public static final long OopEncodingHeapMax;

	static
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/runtime/arguments.cpp#L1400
		ObjectAlignmentInBytes = virtual_machine.get_long_option("ObjectAlignmentInBytes");
		MinObjAlignmentInBytes = ObjectAlignmentInBytes;
		MinObjAlignment = MinObjAlignmentInBytes / HeapWordSize;
		MinObjAlignmentInBytesMask = MinObjAlignmentInBytes - 1;

		LogMinObjAlignmentInBytes = algorithms.uint64_log2(ObjectAlignmentInBytes);
		LogMinObjAlignment = LogMinObjAlignmentInBytes - LogHeapWordSize;

		OopEncodingHeapMax = UnscaledOopHeapMax << LogMinObjAlignmentInBytes;// 使用OOP压缩编码后支持的最大的堆内存
	}

	// 额外定义
	public static final long K = 1024;
	public static final long M = K * K;
	public static final long G = M * K;

	public static final int max_method_code_size = (int) (64 * K - 1); // JVM spec, 2nd ed. section 4.8.1 (p.134)
	public static final int max_method_parameter_length = 255; // JVM spec, 22nd ed. section 4.3.3 (p.83)

	// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/utilities/globalDefinitions.hpp#L187
	public static final int LogBytesPerLong = 3;

	public static final long HeapWordsPerLong = BytesPerLong / HeapWordSize;
	public static final long LogHeapWordsPerLong = LogBytesPerLong - LogHeapWordSize;;

	public static final int heapOopSize;

	// wordSize == HeapWordSize 且 wordSize == BytesPerWord
	public static final int wordSize = HeapWordSize;

	static
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/utilities/globalDefinitions.cpp#L186
		if (virtual_machine.UseCompressedOops)
		{
			heapOopSize = (int) cxx_type.jint.size();
		}
		else
		{
			heapOopSize = oopSize;
		}
	}

	public static final int LogBitsPerWord = LogBitsPerByte + LogBytesPerWord;
	public static final int LogBitsPerLong = LogBitsPerByte + LogBytesPerLong;
	public static final int BitsPerWord = 1 << LogBitsPerWord;
	public static final int BitsPerLong = 1 << LogBitsPerLong;

	/**
	 * Java线程的状态
	 */
	public static abstract class JavaThreadState
	{
		public static final int _thread_uninitialized = vm_constant.find_int("_thread_uninitialized");
		public static final int _thread_new = vm_constant.find_int("_thread_new");
		public static final int _thread_new_trans = vm_constant.find_int("_thread_new_trans");
		public static final int _thread_in_native = vm_constant.find_int("_thread_in_native");
		public static final int _thread_in_native_trans = vm_constant.find_int("_thread_in_native_trans");
		public static final int _thread_in_vm = vm_constant.find_int("_thread_in_vm");
		public static final int _thread_in_vm_trans = vm_constant.find_int("_thread_in_vm_trans");
		public static final int _thread_in_Java = vm_constant.find_int("_thread_in_Java");
		public static final int _thread_in_Java_trans = vm_constant.find_int("_thread_in_Java_trans");
		public static final int _thread_blocked = vm_constant.find_int("_thread_blocked");
		public static final int _thread_blocked_trans = vm_constant.find_int("_thread_blocked_trans");

		public static final int _thread_max_state = _thread_blocked_trans + 1;
	}

	public static final cxx_type intx = cxx_type.typedef(cxx_type.intptr_t, "intx");
	public static final cxx_type uintx = cxx_type.typedef(cxx_type.uintptr_t, "uintx");

	public static abstract class BasicType
	{
		public static final byte T_BOOLEAN = (byte) vm_constant.find_int("T_BOOLEAN");
		public static final byte T_CHAR = (byte) vm_constant.find_int("T_CHAR");
		public static final byte T_FLOAT = (byte) vm_constant.find_int("T_FLOAT");
		public static final byte T_DOUBLE = (byte) vm_constant.find_int("T_DOUBLE");
		public static final byte T_BYTE = (byte) vm_constant.find_int("T_BYTE");
		public static final byte T_SHORT = (byte) vm_constant.find_int("T_SHORT");
		public static final byte T_INT = (byte) vm_constant.find_int("T_INT");
		public static final byte T_LONG = (byte) vm_constant.find_int("T_LONG");
		public static final byte T_OBJECT = (byte) vm_constant.find_int("T_OBJECT");
		public static final byte T_ARRAY = (byte) vm_constant.find_int("T_ARRAY");
		public static final byte T_VOID = (byte) vm_constant.find_int("T_VOID");
		public static final byte T_ADDRESS = (byte) vm_constant.find_int("T_ADDRESS");
		public static final byte T_NARROWOOP = (byte) vm_constant.find_int("T_NARROWOOP");
		public static final byte T_METADATA = (byte) vm_constant.find_int("T_METADATA");
		public static final byte T_NARROWKLASS = (byte) vm_constant.find_int("T_NARROWKLASS");
		public static final byte T_CONFLICT = (byte) vm_constant.find_int("T_CONFLICT");
		public static final byte T_ILLEGAL = (byte) vm_constant.find_int("T_ILLEGAL");
	}

	public static abstract class BasicTypeSize
	{
		// BasicType值的长度，除T_VOID是0以外都是1
		public static final int T_BOOLEAN_size = vm_constant.find_int("T_BOOLEAN_size");
		public static final int T_CHAR_size = vm_constant.find_int("T_CHAR_size");
		public static final int T_FLOAT_size = vm_constant.find_int("T_FLOAT_size");
		public static final int T_DOUBLE_size = vm_constant.find_int("T_DOUBLE_size");
		public static final int T_BYTE_size = vm_constant.find_int("T_BYTE_size");
		public static final int T_SHORT_size = vm_constant.find_int("T_SHORT_size");
		public static final int T_INT_size = vm_constant.find_int("T_INT_size");
		public static final int T_LONG_size = vm_constant.find_int("T_LONG_size");
		public static final int T_OBJECT_size = vm_constant.find_int("T_OBJECT_size");
		public static final int T_ARRAY_size = vm_constant.find_int("T_ARRAY_size");
		public static final int T_NARROWOOP_size = vm_constant.find_int("T_NARROWOOP_size");
		public static final int T_NARROWKLASS_size = vm_constant.find_int("T_NARROWKLASS_size");
		public static final int T_VOID_size = vm_constant.find_int("T_VOID_size");
	};

	public static abstract class ArrayElementSize
	{
		public static final int T_BOOLEAN_aelem_bytes = 1;
		public static final int T_CHAR_aelem_bytes = 2;
		public static final int T_FLOAT_aelem_bytes = 4;
		public static final int T_DOUBLE_aelem_bytes = 8;
		public static final int T_BYTE_aelem_bytes = 1;
		public static final int T_SHORT_aelem_bytes = 2;
		public static final int T_INT_aelem_bytes = 4;
		public static final int T_LONG_aelem_bytes = 8;
		public static final int T_OBJECT_aelem_bytes = 8;
		public static final int T_ARRAY_aelem_bytes = 8;
		public static final int T_NARROWOOP_aelem_bytes = 4;
		public static final int T_NARROWKLASS_aelem_bytes = 4;
		public static final int T_VOID_aelem_bytes = 0;
	}

	private static final int[] _type2aelembytes = new int[]
	{
			0, // 0
			0, // 1
			0, // 2
			0, // 3
			ArrayElementSize.T_BOOLEAN_aelem_bytes, // T_BOOLEAN = 4,
			ArrayElementSize.T_CHAR_aelem_bytes, // T_CHAR = 5,
			ArrayElementSize.T_FLOAT_aelem_bytes, // T_FLOAT = 6,
			ArrayElementSize.T_DOUBLE_aelem_bytes, // T_DOUBLE = 7,
			ArrayElementSize.T_BYTE_aelem_bytes, // T_BYTE = 8,
			ArrayElementSize.T_SHORT_aelem_bytes, // T_SHORT = 9,
			ArrayElementSize.T_INT_aelem_bytes, // T_INT = 10,
			ArrayElementSize.T_LONG_aelem_bytes, // T_LONG = 11,
			ArrayElementSize.T_OBJECT_aelem_bytes, // T_OBJECT = 12,
			ArrayElementSize.T_ARRAY_aelem_bytes, // T_ARRAY = 13,
			0, // T_VOID = 14,
			ArrayElementSize.T_OBJECT_aelem_bytes, // T_ADDRESS = 15,
			ArrayElementSize.T_NARROWOOP_aelem_bytes, // T_NARROWOOP= 16,
			ArrayElementSize.T_OBJECT_aelem_bytes, // T_METADATA = 17,
			ArrayElementSize.T_NARROWKLASS_aelem_bytes, // T_NARROWKLASS= 18,
			0 // T_CONFLICT = 19,
	};

	public static final int type2aelembytes(byte basic_type, boolean allow_address)
	{
		return _type2aelembytes[basic_type];
	}

	public static final int type2aelembytes(byte basic_type)
	{
		return type2aelembytes(basic_type, false);
	}
}

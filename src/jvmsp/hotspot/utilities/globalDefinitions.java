package jvmsp.hotspot.utilities;

import jvmsp.algorithms;
import jvmsp.type.cxx_type;
import jvmsp.virtual_machine;
import jvmsp.hotspot.vm_constant;

public class globalDefinitions
{
	public static final int oopSize = vm_constant.oopSize;

	public static final int LogBytesPerWord = vm_constant.LogBytesPerWord;
	public static final int BytesPerWord = vm_constant.BytesPerWord;
	public static final int BytesPerLong = vm_constant.BytesPerLong;
	// wordSize == HeapWordSize 且 wordSize == BytesPerWord
	public static final int HeapWordSize = vm_constant.HeapWordSize;

	public static final int LogHeapWordSize = vm_constant.LogHeapWordSize;

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
	public static final long LogBytesPerLong = 3;

	public static final long HeapWordsPerLong = BytesPerLong / HeapWordSize;
	public static final long LogHeapWordsPerLong = LogBytesPerLong - LogHeapWordSize;;

	public static final int heapOopSize;

	// wordSize == HeapWordSize 且 wordSize == BytesPerWord
	public static final int wordSize = HeapWordSize;

	public static final boolean UseCompressedOops;

	static
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/utilities/globalDefinitions.cpp#L186
		UseCompressedOops = virtual_machine.get_bool_option("UseCompressedOops");
		if (UseCompressedOops)
		{
			heapOopSize = (int) cxx_type.jint.size();
		}
		else
		{
			heapOopSize = oopSize;
		}
	}
}

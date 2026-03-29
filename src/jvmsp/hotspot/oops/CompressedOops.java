package jvmsp.hotspot.oops;

import jvmsp.type.cxx_type;
import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.AllStatic;

public class CompressedOops extends AllStatic
{
	public static final String type_name = "CompressedOops";

	// 实际堆内存终止地址大于不压缩oop时支持的最大地址，则需要压缩oop，哪怕没启用UseCompressedOops也会自动开启压缩。
	// 指定了UseCompressedOops后则必定压缩。
	// 堆内存的末尾绝对地址小于不压缩oop时支持的最大地址就不压缩

	private static final long _base = vm_struct.switch_address(
			() -> vm_struct.entry.find(type_name, "_narrow_oop._base").address, // JDK21
			() -> vm_struct.entry.find(type_name, "_base").address// JDK25
	);

	private static final long _shift = vm_struct.switch_address(
			() -> vm_struct.entry.find(type_name, "_narrow_oop._shift").address, // JDK21
			() -> vm_struct.entry.find(type_name, "_shift").address// JDK25
	);

	private CompressedOops()
	{
		super(type_name);
	}

	public static final long base()
	{
		return unsafe.read_pointer(_base);
	}

	public static final int shift()
	{
		return unsafe.read_int(_shift);
	}

	public static final int encode(long native_addr)
	{
		return (int) ((native_addr - base()) >> shift());
	}

	public static final long decode(int oop)
	{
		return base() + ((oop & cxx_type.uint32_t_mask) << shift());
	}

	/**
	 * 压缩模式
	 */
	public static abstract class Mode
	{
		public static final int UnscaledNarrowOop = 0;// 无压缩
		public static final int ZeroBasedNarrowOop = 1;// 压缩，基地址为0
		public static final int DisjointBaseNarrowOop = 2;
		public static final int HeapBasedNarrowOop = 3;// 压缩，基地址非0
	}
}
package jvmsp.hotspot.oops;

import jvmsp.type.cxx_type;
import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;

public abstract class CompressedOops
{
	private static final long _base = vm_struct.switch_address(
			() -> vm_struct.entry.find("CompressedOops", "_narrow_oop._base").address, // JDK21
			() -> vm_struct.entry.find("CompressedOops", "_base").address// JDK25
	);

	private static final long _shift = vm_struct.switch_address(
			() -> vm_struct.entry.find("CompressedOops", "_narrow_oop._shift").address, // JDK21
			() -> vm_struct.entry.find("CompressedOops", "_shift").address// JDK25
	);

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
}
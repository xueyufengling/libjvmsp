package jvmsp.hotspot.oops;

import jvmsp.type.cxx_type;
import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.AllStatic;

/**
 * Klass指针压缩，用于对象头的Klass Word计算
 */
public class CompressedKlassPointers extends AllStatic
{
	public static final String type_name = "CompressedKlassPointers";

	private static final long _base = vm_struct.switch_address(
			() -> vm_struct.entry.find(type_name, "_narrow_klass._base").address, // JDK21
			() -> vm_struct.entry.find(type_name, "_base").address// JDK25
	);

	private static final long _shift = vm_struct.switch_address(
			() -> vm_struct.entry.find(type_name, "_narrow_klass._shift").address, // JDK21
			() -> vm_struct.entry.find(type_name, "_shift").address// JDK25
	);

	private CompressedKlassPointers()
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

	public static final int encode(long klass_ptr)
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/compressedKlass.inline.hpp#L39
		return (int) ((klass_ptr - base()) >> shift());
	}

	public static final long decode(int narrow_klass)
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/compressedKlass.inline.hpp#L35
		return base() + ((narrow_klass & cxx_type.uint32_t_mask) << shift());
	}
}
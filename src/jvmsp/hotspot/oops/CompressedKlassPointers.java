package jvmsp.hotspot.oops;

import static jvmsp.versions.jdk_versions;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.type.cxx_type;

/**
 * Klass指针压缩，用于对象头的Klass Word计算
 */
public class CompressedKlassPointers
{
	private static final long _base = jdk_versions.switch_execute(
			() -> vm_struct.entry.find("CompressedKlassPointers", "_narrow_klass._base").address, // JDK21
			() -> vm_struct.entry.find("CompressedKlassPointers", "_base").address// JDK25
	);

	private static final long _shift = jdk_versions.switch_execute(
			() -> vm_struct.entry.find("CompressedKlassPointers", "_narrow_klass._shift").address, // JDK21
			() -> vm_struct.entry.find("CompressedKlassPointers", "_shift").address// JDK25
	);

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
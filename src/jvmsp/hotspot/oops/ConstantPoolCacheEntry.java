package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;

public class ConstantPoolCacheEntry
{
	private static final long _indices = vm_struct.entry.find("ConstantPoolCacheEntry", "_indices").offset;
	private static final long _f1 = vm_struct.entry.find("ConstantPoolCacheEntry", "_f1").offset;
	private static final long _f2 = vm_struct.entry.find("ConstantPoolCacheEntry", "_f2").offset;
	private static final long _flags = vm_struct.entry.find("ConstantPoolCacheEntry", "_flags").offset;
}
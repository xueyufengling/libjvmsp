package jvmsp.hotspot.gc.shared;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;

public class OopStorage extends CHeapObj
{
	public static final String type_name = "OopStorage";
	public static final long size = sizeof(type_name);

	private static final long _bottom = vm_struct.entry.find(type_name, "_bottom").offset;
	private static final long _end = vm_struct.entry.find(type_name, "_end").offset;

	public abstract class EntryStatus
	{
		public static final int INVALID_ENTRY = 0;
		public static final int UNALLOCATED_ENTRY = 1;
		public static final int ALLOCATED_ENTRY = 2;
	};

	public OopStorage(long address)
	{
		super(type_name, address);
	}

}

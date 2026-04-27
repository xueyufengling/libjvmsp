package jvmsp.hotspot.gc.serial;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;

public class Generation extends CHeapObj
{
	public static final String type_name = "Generation";
	public static final long size = sizeof(type_name);

	private static final long _reserved = vm_struct.entry.find(type_name, "_reserved").offset;
	private static final long _virtual_space = vm_struct.entry.find(type_name, "_virtual_space").offset;
	private static final long _stat_record = vm_struct.entry.find(type_name, "_stat_record").offset;

	public Generation(long address)
	{
		super(type_name, address);
	}
}

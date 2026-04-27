package jvmsp.hotspot.compiler;

import jvmsp.hotspot.vm_struct;

public class ImmutableOopMapPair extends vm_struct
{
	public static final String type_name = "ImmutableOopMapPair";
	public static final long size = sizeof(type_name);

	private static final long _pc_offset = vm_struct.entry.find(type_name, "_pc_offset").offset;
	private static final long _oopmap_offset = vm_struct.entry.find(type_name, "_oopmap_offset").offset;

	public ImmutableOopMapPair(long address)
	{
		super(type_name, address);
	}

	public int pc_offset()
	{
		return super.read_cint(_pc_offset);
	}

	public int oopmap_offset()
	{
		return super.read_cint(_oopmap_offset);
	}
}

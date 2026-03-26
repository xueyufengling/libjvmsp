package jvmsp.hotspot.compiler;

import jvmsp.hotspot.vm_struct;

public class ImmutableOopMapPair extends vm_struct
{
	private static final long _pc_offset = vm_struct.entry.find("ImmutableOopMapPair", "_pc_offset").offset;
	private static final long _oopmap_offset = vm_struct.entry.find("ImmutableOopMapPair", "_oopmap_offset").offset;

	public static final long size = sizeof("ImmutableOopMapPair");

	public ImmutableOopMapPair(long address)
	{
		super("ImmutableOopMapPair", address);
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

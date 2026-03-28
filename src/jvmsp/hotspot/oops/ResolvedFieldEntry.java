package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;

/**
 * @since jdk-25
 */
public class ResolvedFieldEntry extends vm_struct
{
	public static final String type_name = "ResolvedFieldEntry";
	public static final long size = sizeof(type_name);

	private static final long _cpool_index = vm_struct.entry.find(type_name, "_cpool_index").offset;

	public ResolvedFieldEntry(long address)
	{
		super(type_name, address);
	}

	public int constant_pool_index()
	{
		return super.read_uint16_t(_cpool_index);
	}

	public void set_cpool_index(int cpool_index)
	{
		super.write_uint16_t(_cpool_index, cpool_index);
	}
}
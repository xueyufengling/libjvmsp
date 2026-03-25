package jvmsp.hotspot.memory;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;

public class MetaspaceObj extends vm_struct
{
	public static final long _shared_metaspace_base = vm_struct.entry.find("MetaspaceObj", "_shared_metaspace_base").address;
	public static final long _shared_metaspace_top = vm_struct.entry.find("MetaspaceObj", "_shared_metaspace_top").address;

	public MetaspaceObj(String name, long address, long idx_base)
	{
		super(name, address, idx_base);
	}

	public MetaspaceObj(String name, long address)
	{
		this(name, address, 0);
	}

	public MetaspaceObj(long address)
	{
		this("MetaspaceObj", address);
	}

	public static final long _shared_metaspace_base()
	{
		return unsafe.read_pointer(_shared_metaspace_base);
	}

	public static final long _shared_metaspace_top()
	{
		return unsafe.read_pointer(_shared_metaspace_top);
	}
}

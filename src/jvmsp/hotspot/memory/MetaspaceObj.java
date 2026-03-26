package jvmsp.hotspot.memory;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;

public class MetaspaceObj extends vm_struct
{
	// JDK26+改为_aot_metaspace_base
	public static final long _shared_metaspace_base = vm_struct.entry.find("MetaspaceObj", "_shared_metaspace_base").address;
	public static final long _shared_metaspace_top = vm_struct.entry.find("MetaspaceObj", "_shared_metaspace_top").address;

	public MetaspaceObj(String name, long address)
	{
		super(name, address);
	}

	public MetaspaceObj(long address)
	{
		this("MetaspaceObj", address);
	}

	public static final long shared_metaspace_base()
	{
		return unsafe.read_pointer(_shared_metaspace_base);
	}

	public static final void set_shared_metaspace_base(long base)
	{
		unsafe.write_pointer(_shared_metaspace_base, base);
	}

	public static final long shared_metaspace_top()
	{
		return unsafe.read_pointer(_shared_metaspace_top);
	}

	public static final void set_shared_metaspace_top(long top)
	{
		unsafe.write_pointer(_shared_metaspace_top, top);
	}

	public static final void set_shared_metaspace_range(long base, long top)
	{
		set_shared_metaspace_base(base);
		set_shared_metaspace_top(top);
	}
}

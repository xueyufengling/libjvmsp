package jvmsp.hotspot.memory;

import jvmsp.hotspot.vm_struct;

public abstract class CHeapObj extends vm_struct
{
	protected CHeapObj(String name, long address)
	{
		super(name, address);
	}
}

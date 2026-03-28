package jvmsp.hotspot.memory;

import jvmsp.hotspot.vm_struct;

public class CHeapObj extends vm_struct
{
	protected CHeapObj(String name, long address)
	{
		super(name, address);
	}
}

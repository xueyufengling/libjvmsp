package jvmsp.hotspot.memory;

import jvmsp.hotspot.vm_struct;

public abstract class StackObj extends vm_struct
{
	protected StackObj(String name, long address)
	{
		super(name, address);
	}

	@Override
	public final int allocation_type()
	{
		return StackObj;
	}
}

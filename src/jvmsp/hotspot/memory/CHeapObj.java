package jvmsp.hotspot.memory;

import jvmsp.hotspot.vm_struct;

/**
 * 任何CHeapObj的直接派生类的首字段都具有不确定的偏移量。<br>
 */
public abstract class CHeapObj extends vm_struct
{
	protected CHeapObj(String name, long address)
	{
		super(name, address);
	}

	@Override
	public final int allocation_type()
	{
		return CHeapObj;
	}
}

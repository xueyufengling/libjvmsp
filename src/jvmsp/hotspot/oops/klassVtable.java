package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;

/**
 * 继承的超类虚方法表
 */
public class klassVtable extends vm_struct
{
	public klassVtable(long address)
	{
		super("klassVtable", address);
	}
}
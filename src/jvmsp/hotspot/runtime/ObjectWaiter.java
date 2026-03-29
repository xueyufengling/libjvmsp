package jvmsp.hotspot.runtime;

import jvmsp.hotspot.memory.CHeapObj;

public class ObjectWaiter extends CHeapObj
{
	public static final String type_name = "ObjectMonitor";
	public static final long size = sizeof(type_name);

	public ObjectWaiter(long address)
	{
		super(type_name, address);
	}
}
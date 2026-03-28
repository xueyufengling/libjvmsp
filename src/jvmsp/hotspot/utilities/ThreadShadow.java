package jvmsp.hotspot.utilities;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;

public class ThreadShadow extends CHeapObj
{
	public static final String type_name = "ThreadShadow";
	public static final long size = sizeof(type_name);

	private static final long _pending_exception = vm_struct.entry.find(type_name, "_pending_exception").offset;
	private static final long _exception_file = vm_struct.entry.find(type_name, "_exception_file").offset;
	private static final long _exception_line = vm_struct.entry.find(type_name, "_exception_line").offset;

	protected ThreadShadow(String name, long address)
	{
		super(name, address);
	}

	public ThreadShadow(long address)
	{
		super(type_name, address);
	}

}
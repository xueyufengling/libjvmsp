package jvmsp.hotspot.runtime;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.utilities.ThreadShadow;

public class Thread extends ThreadShadow
{
	public static final String type_name = "Thread";
	public static final long size = sizeof(type_name);

	private static final long _tlab = vm_struct.entry.find(type_name, "_tlab").offset;
	private static final long _allocated_bytes = vm_struct.entry.find(type_name, "_allocated_bytes").offset;
	private static final long _resource_area = vm_struct.entry.find(type_name, "_resource_area").offset;

	protected Thread(String name, long address)
	{
		super(name, address);
	}

	public Thread(long address)
	{
		super(type_name, address);
	}
}
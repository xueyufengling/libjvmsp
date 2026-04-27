package jvmsp.hotspot.runtime;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.AllStatic;

public class JNIHandles extends AllStatic
{
	public static final String type_name = "JNIHandles";

	private static final long _global_handles = vm_struct.entry.find(type_name, "_global_handles").address;
	private static final long _weak_global_handles = vm_struct.entry.find(type_name, "_weak_global_handles").address;

	private JNIHandles()
	{
		super(type_name);
	}

	public static final long _global_handles()
	{
		return unsafe.read_ptr(_global_handles);
	}

	public static final void set_global_handles(long global_handles)
	{
		unsafe.write_ptr(_global_handles, global_handles);
	}

	public static final long _weak_global_handles()
	{
		return unsafe.read_ptr(_weak_global_handles);
	}

	public static final void set_weak_global_handles(long weak_global_handles)
	{
		unsafe.write_ptr(_weak_global_handles, weak_global_handles);
	}
}

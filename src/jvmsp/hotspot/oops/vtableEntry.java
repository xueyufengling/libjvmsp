package jvmsp.hotspot.oops;

import jvmsp.memory.memory_object;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.utilities.globalDefinitions;

/**
 * vtable条目
 */
public class vtableEntry extends vm_struct
{
	public static final String type_name = "vtableEntry";
	public static final long size = sizeof(type_name);

	private static final long _method = vm_struct.entry.find(type_name, "_method").offset;

	public vtableEntry(long klass_ptr)
	{
		super(type_name, klass_ptr);
	}

	public Method method()
	{
		return super.read_memory_object_ptr(Method.class, _method);
	}

	public void set_method(Method method)
	{
		super.write_memory_object_ptr(_method, method);
	}

	public void clear()
	{
		super.write_memory_object_ptr(_method, memory_object.nullptr);
	}

	public static final int size()
	{
		return (int) (size / globalDefinitions.wordSize);
	}

	public static final int size_in_bytes()
	{
		return (int) size;
	}
}
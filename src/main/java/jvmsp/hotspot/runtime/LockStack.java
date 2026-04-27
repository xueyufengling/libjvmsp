package jvmsp.hotspot.runtime;

import jvmsp.type.cxx_type;
import jvmsp.hotspot.vm_struct;

public class LockStack extends vm_struct
{
	public static final String type_name = "LockStack";
	public static final long size = sizeof(type_name);

	private static final long _top = vm_struct.entry.find(type_name, "_top").offset;
	private static final long _base_0 = vm_struct.entry.find(type_name, "_base[0]").offset;

	public LockStack(long address)
	{
		super(type_name, address);
	}

	// The offset of the next element, in bytes, relative to the JavaThread structure.
	// We do this instead of a simple index into the array because this allows for
	// efficient addressing in generated code.
	// uint32_t _top;
	public int top()
	{
		return super.read_int(_top);
	}

	public void set_top(int top)
	{
		super.write(_top, top);
	}

	public int base_at(int idx)
	{
		return super.read_int(_base_0 + idx * cxx_type.oop.size());
	}

	public void set_base_at(int idx, int oop)
	{
		super.write(_base_0 + idx * cxx_type.oop.size(), oop);
	}
}
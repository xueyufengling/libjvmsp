package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;

public class TypeArrayKlass extends ArrayKlass
{
	public static final String type_name = "TypeArrayKlass";
	public static final long size = sizeof(type_name);

	private static final long _max_length = vm_struct.entry.find(type_name, "_max_length").offset;

	public TypeArrayKlass(long address)
	{
		super(type_name, address);
	}

	public int max_length()
	{
		return super.read_int(_max_length);
	}

	public void set_max_length(int max_length)
	{
		super.write(_max_length, max_length);
	}
}

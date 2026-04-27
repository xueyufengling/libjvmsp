package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;

/**
 * 对象数组类型
 */
public class ObjArrayKlass extends ArrayKlass
{
	public static final String type_name = "ObjArrayKlass";
	public static final long size = sizeof(type_name);

	private static final long _element_klass = vm_struct.entry.find(type_name, "_element_klass").offset;
	private static final long _bottom_klass = vm_struct.entry.find(type_name, "_bottom_klass").offset;

	public ObjArrayKlass(long address)
	{
		super(type_name, address);
	}

	public Klass element_klass()
	{
		return super.read_memory_object_ptr(Klass.class, _element_klass);
	}

	public void set_element_klass(Klass element_klass)
	{
		super.write_memory_object_ptr(_element_klass, element_klass);
	}

	public Klass bottom_klass()
	{
		return super.read_memory_object_ptr(Klass.class, _bottom_klass);
	}

	public void set_bottom_klass(Klass bottom_klass)
	{
		super.write_memory_object_ptr(_bottom_klass, bottom_klass);
	}
}
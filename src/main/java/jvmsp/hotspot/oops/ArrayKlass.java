package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;

/**
 * 数组类型
 */
public class ArrayKlass extends Klass
{
	public static final String type_name = "ArrayKlass";
	public static final long size = sizeof(type_name);

	private static final long _dimension = vm_struct.entry.find(type_name, "_dimension").offset;
	private static final long _higher_dimension = vm_struct.entry.find(type_name, "_higher_dimension").offset;
	private static final long _lower_dimension = vm_struct.entry.find(type_name, "_lower_dimension").offset;

	protected ArrayKlass(String name, long address)
	{
		super(name, address);
	}

	public ArrayKlass(long address)
	{
		this(type_name, address);
	}

	@Override
	public String toString()
	{
		return '[' + super.toString();
	}

	public int dimension()
	{
		return super.read_cint(_dimension);
	}

	public void set_dimension(int dimension)
	{
		super.write_cint(_dimension, dimension);
	}

	public ObjArrayKlass higher_dimension()
	{
		return super.read_memory_object_ptr(ObjArrayKlass.class, _higher_dimension);
	}

	public void set_higher_dimension(ObjArrayKlass higher_dimension)
	{
		super.write_memory_object_ptr(_higher_dimension, higher_dimension);
	}

	public ArrayKlass lower_dimension()
	{
		return super.read_memory_object_ptr(ArrayKlass.class, _lower_dimension);
	}

	public void set_lower_dimension(ArrayKlass lower_dimension)
	{
		super.write_memory_object_ptr(_lower_dimension, lower_dimension);
	}

	@Override
	public long size()
	{
		return 0;
	}

	public int array_header_in_bytes()
	{
		return layout_helper_header_size(layout_helper());
	}

	public int log2_element_size()
	{
		return layout_helper_log2_element_size(layout_helper());
	}

	// type of elements (T_OBJECT for both oop arrays and array-arrays)
	public byte element_type()
	{
		return layout_helper_element_type(layout_helper());
	}
}

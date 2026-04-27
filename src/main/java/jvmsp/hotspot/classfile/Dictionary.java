package jvmsp.hotspot.classfile;

import jvmsp.type.cxx_type;
import jvmsp.hotspot.memory.CHeapObj;

public class Dictionary extends CHeapObj
{
	public static final String type_name = "Dictionary";

	public static final cxx_type Dictionary = cxx_type.define(type_name)
			.decl_field("_number_of_entries", cxx_type._int)
			.decl_field("_table", cxx_type.pvoid)
			.decl_field("_loader_data", cxx_type.pvoid)
			.resolve();

	public static final long size = Dictionary.size();

	private static final long _number_of_entries = Dictionary.field("_number_of_entries").offset();
	private static final long _table = Dictionary.field("_table").offset();
	private static final long _loader_data = Dictionary.field("_loader_data").offset();

	public Dictionary(long address)
	{
		super(type_name, address);
	}

	public int number_of_entries()
	{
		return super.read_cint(_number_of_entries);
	}

	public void set_number_of_entries(int number_of_entries)
	{
		super.write_cint(_number_of_entries, number_of_entries);
	}
}

package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;
import jvmsp.type.cxx_type;

/**
 * 接口方法表
 */
public class klassItable extends vm_struct
{

	public static final cxx_type klassItable = cxx_type.define("klassItable")
			.decl_field("_klass", cxx_type.pvoid)
			.decl_field("_table_offset", cxx_type._int)
			.decl_field("_size_offset_table", cxx_type._int)
			.decl_field("_size_method_table", cxx_type._int)
			.resolve();

	public klassItable(long address)
	{
		super("klassItable", address);
	}

}
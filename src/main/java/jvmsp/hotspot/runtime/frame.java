package jvmsp.hotspot.runtime;

import jvmsp.hotspot.vm_struct;
import jvmsp.type.cxx_type;

public class frame extends vm_struct
{
	public static final String type_name = "frame";
	public static final long size = sizeof(type_name);

	public static final cxx_type frame = cxx_type.define(type_name)
			.decl_field("_sp", cxx_type.pvoid)
			.decl_field("_sp", cxx_type.pvoid)
			.decl_field("_sp", cxx_type.pvoid)
			.resolve();

	private static final long _flags = frame.field("_flags").offset();

	public frame(long address)
	{
		super(type_name, address);
	}
}
package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;
import jvmsp.type.cxx_type;

public class WeakHandle extends vm_struct
{
	public static final String type_name = "WeakHandle";

	public static final cxx_type WeakHandle = cxx_type.define(type_name)
			.decl_field("_obj", cxx_type.pvoid)
			.resolve();

	public static final long size = WeakHandle.size();

	// oop* _obj;
	private static final long _obj = WeakHandle.field("_obj").offset();

	public WeakHandle(long address)
	{
		super(type_name, address);
	}

	public long _obj()
	{
		return super.read_ptr(_obj);
	}

	public void set_obj(long header)
	{
		super.write_ptr(_obj, header);
	}
}

package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;

public class OopHandle extends vm_struct
{
	public static final String type_name = "OopHandle";
	public static final long size = sizeof(type_name);

	private static final long _obj = vm_struct.entry.find(type_name, "_obj").offset;

	public OopHandle(long address)
	{
		super(type_name, address);
	}

	public long _obj()
	{
		return super.read_int(_obj);
	}

	/**
	 * https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/oopHandle.inline.hpp#L33
	 * 解析OOP
	 * 
	 * @return
	 */
	public long resolve()
	{
		long obj = _obj();
		if (obj != 0)
		{
			// long oop = NativeAccess<>::oop_load(_obj);
			// return oop;
		}
		return 0;
	}
}
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

	public int obj()
	{
		return super.read_int(_obj);
	}

	/**
	 * https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/oopHandle.inline.hpp#L33<br>
	 * 解析OOP将其还原为对象的内存地址。<br>
	 * 
	 * @return
	 */
	public long resolve()
	{
		int obj = obj();
		if (obj != 0)
		{
			return CompressedOops.decode(obj);
		}
		else
		{
			return 0;
		}
	}
}
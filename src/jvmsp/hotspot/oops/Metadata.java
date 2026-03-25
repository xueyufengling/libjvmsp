package jvmsp.hotspot.oops;

import jvmsp.hotspot.memory.MetaspaceObj;

/**
 * 元数据
 */
public class Metadata extends MetaspaceObj
{
	protected Metadata(String name, long address)
	{
		super(name, address);
	}

	protected Metadata(long address)
	{
		this("Metadata", address);
	}

	public int identity_hash()
	{
		return (int) address;
	}

	public final boolean is_metadata()
	{
		return true;
	}

	public boolean is_klass()
	{
		return false;
	}

	public boolean is_method()
	{
		return false;
	}

	public boolean is_methodData()
	{
		return false;
	}

	public boolean is_constantPool()
	{
		return false;
	}

	public boolean is_methodCounters()
	{
		return false;
	}
}
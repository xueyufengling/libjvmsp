package jvmsp.hotspot.memory;

public class Arena extends CHeapObj
{
	public static final String type_name = "Arena";
	public static final long size = sizeof(type_name);

	protected Arena(String name, long address)
	{
		super(name, address);
	}

	public Arena(long address)
	{
		this(type_name, address);
	}
}
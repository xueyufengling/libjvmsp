package jvmsp.hotspot.memory;

public class ResourceArea extends Arena
{
	public static final String type_name = "ResourceArea";
	public static final long size = sizeof(type_name);

	protected ResourceArea(String name, long address)
	{
		super(name, address);
	}

	public ResourceArea(long address)
	{
		this(type_name, address);
	}
}
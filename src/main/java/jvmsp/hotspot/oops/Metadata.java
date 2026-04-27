package jvmsp.hotspot.oops;

import jvmsp.hotspot.memory.MetaspaceObj;

/**
 * 元数据
 */
public abstract class Metadata extends MetaspaceObj
{
	public static final String type_name = "Metadata";
	public static final long size = sizeof(type_name);

	protected Metadata(String name, long address)
	{
		super(name, address);
	}

	protected Metadata(long address)
	{
		this(type_name, address);
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

	public final Klass as_klass()
	{
		return super.cast(Klass.class);
	}

	public boolean is_method()
	{
		return false;
	}

	public final Method as_method()
	{
		return super.cast(Method.class);
	}

	public boolean is_methodData()
	{
		return false;
	}

	public final MethodData as_methodData()
	{
		return super.cast(MethodData.class);
	}

	public boolean is_constantPool()
	{
		return false;
	}

	public final ConstantPool as_constantPool()
	{
		return super.cast(ConstantPool.class);
	}

	public boolean is_methodCounters()
	{
		return false;
	}

	public final MethodCounters as_methodCounters()
	{
		return super.cast(MethodCounters.class);
	}

	public abstract long size();

	public abstract String internal_name();
}
package jvmsp.hotspot.memory;

import java.util.HashMap;

import jvmsp.hotspot.vm_type;

public abstract class AllStatic implements vm_type
{
	public static final String type_name = "AllStatic";
	public static final long size = 0;

	private static final HashMap<Class<?>, String> all_static_names = new HashMap<>();

	protected AllStatic(String name)
	{
		throw new java.lang.InternalError("AllStatic type '" + all_static_names.computeIfAbsent(getClass(), (c) -> name) + "' cannot be instantiated");
	}

	protected AllStatic()
	{
		this(type_name);
	}

	@Override
	public final int allocation_type()
	{
		return AllStatic;
	}

	@Override
	public vm_type.entry type()
	{
		return vm_type.entry.find(all_static_names.get(this.getClass()));
	}
}

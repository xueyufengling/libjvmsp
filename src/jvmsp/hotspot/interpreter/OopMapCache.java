package jvmsp.hotspot.interpreter;

import jvmsp.hotspot.memory.CHeapObj;
import jvmsp.type.cxx_type;

public class OopMapCache extends CHeapObj
{
	public static final String type_name = "OopMapCache";

	public static final int array_size = 32; // Use fixed size for now
	public static final int probe_depth = 3; // probe depth in case of collisions

	public static final cxx_type OopMapCache = cxx_type.define(type_name)
			.decl_field("_flags", cxx_type.pvoid.array(array_size))
			.resolve();

	public static final long size = OopMapCache.size();

	private static final long _array = OopMapCache.field("_array").offset();

	public OopMapCache(long address)
	{
		super(type_name, address);
	}
}

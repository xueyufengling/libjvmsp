package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.MetaspaceObj;
import jvmsp.hotspot.oops.Array.Array_u2;

/**
 * 常量池的运行时数据缓存
 */
public class ConstantPoolCache extends MetaspaceObj
{
	private static final long _resolved_references = vm_struct.entry.find("ConstantPoolCache", "_resolved_references").offset;
	private static final long _reference_map = vm_struct.entry.find("ConstantPoolCache", "_reference_map").offset;
	private static final long _length = vm_struct.entry.find("ConstantPoolCache", "_length").offset;
	private static final long _constant_pool = vm_struct.entry.find("ConstantPoolCache", "_constant_pool").offset;
	private static final long _resolved_indy_entries = vm_struct.entry.find("ConstantPoolCache", "_resolved_indy_entries").offset;

	public static final long size = sizeof("ConstantPoolCache");
	
	public ConstantPoolCache(long address)
	{
		super("ConstantPoolCache", address);
	}

	public OopHandle _resolved_references()
	{
		return super.read_memory_object(OopHandle.class, _resolved_references);
	}

	public Array_u2 _reference_map()
	{
		return super.read_memory_object_ptr(Array_u2.class, _reference_map);
	}

	public int length()
	{
		return super.read_int(_length);
	}

	public ConstantPool _constant_pool()
	{
		return super.read_memory_object_ptr(ConstantPool.class, _constant_pool);
	}

	public long constant_pool_addr()
	{
		return super.offset_addr(_constant_pool);
	}

	public void set_constant_pool(ConstantPool constant_pool)
	{
		super.write_pointer(_constant_pool, constant_pool);
	}
}
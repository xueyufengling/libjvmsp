package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.oops.Array.Array_u1;

/**
 * 常量池
 */
public class ConstantPool extends Metadata
{
	private static final long _tags = vm_struct.entry.find("ConstantPool", "_tags").offset;
	private static final long _cache = vm_struct.entry.find("ConstantPool", "_cache").offset;
	private static final long _pool_holder = vm_struct.entry.find("ConstantPool", "_pool_holder").offset;
	private static final long _operands = vm_struct.entry.find("ConstantPool", "_operands").offset;
	private static final long _resolved_klasses = vm_struct.entry.find("ConstantPool", "_resolved_klasses").offset;
	private static final long _length = vm_struct.entry.find("ConstantPool", "_length").offset;
	private static final long _minor_version = vm_struct.entry.find("ConstantPool", "_minor_version").offset;
	private static final long _major_version = vm_struct.entry.find("ConstantPool", "_major_version").offset;
	private static final long _generic_signature_index = vm_struct.entry.find("ConstantPool", "_generic_signature_index").offset;
	private static final long _source_file_name_index = vm_struct.entry.find("ConstantPool", "_source_file_name_index").offset;

	public ConstantPool(long address)
	{
		super("ConstantPool", address);
	}

	@Override
	public final boolean is_constantPool()
	{
		return true;
	}

	public Array_u1 tags()
	{
		return super.read_memory_object_ptr(Array_u1.class, _tags);
	}

	public void set_tags(Array_u1 tags)
	{
		super.write_pointer(_tags, tags);
	}

	public ConstantPoolCache cache()
	{
		return super.read_memory_object_ptr(ConstantPoolCache.class, _cache);
	}

	public void set_cache(ConstantPoolCache cache)
	{
		super.write_pointer(_cache, cache);
	}

	public InstanceKlass pool_holder()
	{
		return super.read_memory_object_ptr(InstanceKlass.class, _pool_holder);
	}

	public void set_pool_holder(InstanceKlass ik)
	{
		super.write_pointer(_pool_holder, ik);
	}
}

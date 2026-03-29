package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.MetaspaceObj;
import jvmsp.hotspot.oops.Array.Array_ResolvedFieldEntry;
import jvmsp.hotspot.oops.Array.Array_ResolvedIndyEntry;
import jvmsp.hotspot.oops.Array.Array_ResolvedMethodEntry;
import jvmsp.hotspot.oops.Array.Array_u2;

/**
 * 常量池的运行时数据缓存。<br>
 * 这些数值可能在运行时被JVM修改，JVM解释器运行时实际上就是使用该缓存的值。<br>
 * JDK21: https://github.com/openjdk/jdk/blob/jdk-21%2B35/src/hotspot/share/oops/cpCache.hpp
 * jdk25: https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/cpCache.hpp
 */
public class ConstantPoolCache extends MetaspaceObj
{
	public static final String type_name = "ConstantPoolCache";
	public static final long size = sizeof(type_name);

	private static final long _constant_pool = vm_struct.entry.find(type_name, "_constant_pool").offset;
	private static final long _resolved_references = vm_struct.entry.find(type_name, "_resolved_references").offset;
	private static final long _reference_map = vm_struct.entry.find(type_name, "_reference_map").offset;

	private static final long _resolved_indy_entries = vm_struct.entry.find(type_name, "_resolved_indy_entries").offset;

	// *** JDK21 Only ***
	private static final long _length = vm_struct.switch_offset(
			() -> vm_struct.entry.find(type_name, "_length").offset // JDK21
	);

	// *** JDK25+ Only ***
	private static final long _resolved_field_entries = vm_struct.switch_offset(
			null, // JDK21
			() -> vm_struct.entry.find(type_name, "_resolved_field_entries").offset// JDK25
	);
	private static final long _resolved_method_entries = vm_struct.switch_offset(
			null, // JDK21
			() -> vm_struct.entry.find(type_name, "_resolved_method_entries").offset// JDK25
	);

	public ConstantPoolCache(long address)
	{
		super(type_name, address);
	}

	public OopHandle resolved_references()
	{
		return super.read_memory_object(OopHandle.class, _resolved_references);
	}

	public Array_u2 reference_map()
	{
		return super.read_memory_object_ptr(Array_u2.class, _reference_map);
	}

	public int length()
	{
		return super.read_int(_length);
	}

	public ConstantPool constant_pool()
	{
		return super.read_memory_object_ptr(ConstantPool.class, _constant_pool);
	}

	public long constant_pool_addr()
	{
		return super.offset_addr(_constant_pool);
	}

	public void set_constant_pool(ConstantPool constant_pool)
	{
		super.write_memory_object_ptr(_constant_pool, constant_pool);
	}

	public Array_ResolvedIndyEntry resolved_indy_entries()
	{
		return super.read_memory_object_ptr(Array_ResolvedIndyEntry.class, _resolved_indy_entries);
	}

	public ResolvedIndyEntry resolved_indy_entry_at(int index)
	{
		return resolved_indy_entries().at(index);
	}

	public int resolved_indy_entries_length()
	{
		return resolved_indy_entries().length();
	}

	// JDK25+
	public Array_ResolvedFieldEntry resolved_field_entries()
	{
		return super.read_memory_object_ptr(Array_ResolvedFieldEntry.class, _resolved_field_entries);
	}

	public ResolvedFieldEntry resolved_field_entry_at(int index)
	{
		return resolved_field_entries().at(index);
	}

	public int resolved_field_entries_length()
	{
		return resolved_field_entries().length();
	}

	public Array_ResolvedMethodEntry resolved_method_entries()
	{
		return super.read_memory_object_ptr(Array_ResolvedMethodEntry.class, _resolved_method_entries);
	}

	public ResolvedMethodEntry resolved_method_entry_at(int index)
	{
		return resolved_method_entries().at(index);
	}

	public int resolved_method_entries_length()
	{
		return resolved_method_entries().length();
	}

	@Override
	public int meta_type()
	{
		return Type.ConstantPoolCacheType;
	}
}
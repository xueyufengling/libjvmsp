package jvmsp.hotspot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jvmsp.shared_object;
import jvmsp.unsafe;
import jvmsp.libso.libjvm;

public class vm_type
{
	// VMTypes信息的起始地址
	private static final long gHotSpotVMTypes;
	private static final long jvmciHotSpotVMTypes;
	// VMTypes数组的元素步长
	private static final long gHotSpotVMTypeEntryArrayStride;

	private static final long gHotSpotVMTypeEntryTypeNameOffset;
	private static final long gHotSpotVMTypeEntrySuperclassNameOffset;
	private static final long gHotSpotVMTypeEntryIsOopTypeOffset;
	private static final long gHotSpotVMTypeEntryIsIntegerTypeOffset;
	private static final long gHotSpotVMTypeEntryIsUnsignedOffset;
	private static final long gHotSpotVMTypeEntrySizeOffset;

	static
	{
		gHotSpotVMTypes = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMTypes"));
		jvmciHotSpotVMTypes = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "jvmciHotSpotVMTypes"));
		gHotSpotVMTypeEntryArrayStride = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMTypeEntryArrayStride"));
		gHotSpotVMTypeEntryTypeNameOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMTypeEntryTypeNameOffset"));
		gHotSpotVMTypeEntrySuperclassNameOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMTypeEntrySuperclassNameOffset"));
		gHotSpotVMTypeEntryIsOopTypeOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMTypeEntryIsOopTypeOffset"));
		gHotSpotVMTypeEntryIsIntegerTypeOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMTypeEntryIsIntegerTypeOffset"));
		gHotSpotVMTypeEntryIsUnsignedOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMTypeEntryIsUnsignedOffset"));
		gHotSpotVMTypeEntrySizeOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMTypeEntrySizeOffset"));
	}

	public final String type_name;// 类型的名称
	public final String super_class_name;// 类型的超类的名称
	public final boolean is_oop_type;
	public final boolean is_integer_type;
	public final boolean is_unsigned;
	public final long size;

	private vm_type(long type_addr)
	{
		this.type_name = unsafe.read_cstr(type_addr + gHotSpotVMTypeEntryTypeNameOffset);
		this.super_class_name = unsafe.read_cstr(type_addr + gHotSpotVMTypeEntrySuperclassNameOffset);
		this.is_oop_type = unsafe.read_cbool(type_addr + gHotSpotVMTypeEntryIsOopTypeOffset);
		this.is_integer_type = unsafe.read_cbool(type_addr + gHotSpotVMTypeEntryIsIntegerTypeOffset);
		this.is_unsigned = unsafe.read_cbool(type_addr + gHotSpotVMTypeEntryIsUnsignedOffset);
		this.size = unsafe.read_long(type_addr + gHotSpotVMTypeEntrySizeOffset);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder().append("VMTypeEntry [")
				.append("type_name = ").append(type_name)
				.append(", super_class_name = ").append(super_class_name)
				.append(", is_oop_type = ").append(is_oop_type)
				.append(", is_integer_type = ").append(is_integer_type)
				.append(", is_unsigned = ").append(is_unsigned)
				.append(", size = ").append(size)
				.append(']');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (o instanceof vm_type other)
		{
			// 不判断is_oop_type、is_integer_type、is_unsigned，因为sa与jvmci对同一种类型的描述可能不同
			return size == other.size
					&& Objects.equals(type_name, other.type_name)
					&& Objects.equals(super_class_name, other.super_class_name);
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(
				type_name,
				super_class_name,
				is_oop_type,
				is_integer_type,
				is_unsigned,
				size);
	}

	public static final void print_type(String type_name)
	{
		vm_type t = vm_type_entries.get(type_name);
		if (t != null)
		{
			System.out.println(t);
		}
	}

	private static final Map<String, vm_type> vm_type_entries = new HashMap<>();

	private static final void collect_entries(Map<String, vm_type> vm_type_entries, long vm_types)
	{
		for (int idx = 0;; ++idx)
		{
			vm_type entry = new vm_type(vm_types + idx * gHotSpotVMTypeEntryArrayStride);
			if (entry.type_name == null)
			{
				break;
			}
			vm_type existed = vm_type_entries.get(entry.type_name);
			if (existed != null && !entry.equals(existed))
			{
				throw new java.lang.InternalError("conflict VMTypeEntry '" + entry + "' and '" + existed + "'");
			}
			else
			{
				vm_type_entries.put(entry.type_name, entry);
			}
		}
	}

	static
	{
		collect_entries(vm_type_entries, jvmciHotSpotVMTypes);
		collect_entries(vm_type_entries, gHotSpotVMTypes);
	}

	public static final vm_type find(String type_name)
	{
		return vm_type_entries.get(type_name);
	}

	public static final Collection<vm_type> all_types()
	{
		return vm_type_entries.values();
	}
}

package jvmsp.hotspot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jvmsp.shared_object;
import jvmsp.unsafe;
import jvmsp.libso.libjvm;

public interface vm_type
{
	public static class entry
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

		private entry(long type_addr)
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
			if (o instanceof entry other)
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
			entry t = vm_type_entries.get(type_name);
			if (t != null)
			{
				System.out.println(t);
			}
		}

		private static final Map<String, entry> vm_type_entries = new HashMap<>();

		private static final void collect_entries(Map<String, entry> vm_type_entries, long vm_types)
		{
			for (int idx = 0;; ++idx)
			{
				entry entry = new entry(vm_types + idx * gHotSpotVMTypeEntryArrayStride);
				if (entry.type_name == null)
				{
					break;
				}
				entry existed = vm_type_entries.get(entry.type_name);
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

		public static final entry find(String type_name)
		{
			return vm_type_entries.get(type_name);
		}

		public static final Collection<entry> all_types()
		{
			return vm_type_entries.values();
		}
	}

	public static final int ResourceObj = 0;
	public static final int CHeapObj = 1;
	public static final int StackObj = 2;
	public static final int AllStatic = 3;
	public static final int MetaspaceObj = 4;

	/**
	 * 其他非VM指定的类型
	 */
	public static final int CxxObj = 5;

	/**
	 * 分配的类型，必须是allocation中定义的常量。<br>
	 * 
	 * @return
	 */
	public abstract int allocation_type();

	/**
	 * VMType类型
	 * 
	 * @return
	 */
	public abstract entry type();

	/**
	 * 该类型是否在libjvm.so中导出给Serviceability Agent
	 * 
	 * @return
	 */
	public default boolean is_exported()
	{
		return this.type() != null;
	}

	public default long type_size()
	{
		return is_exported() ? 0 : type().size;
	}

	public default String type_name()
	{
		return is_exported() ? null : type().type_name;
	}

	public default String super_class_name()
	{
		return is_exported() ? null : type().super_class_name;
	}

	public default boolean is_oop_type()
	{
		return is_exported() ? false : type().is_oop_type;
	}

	public default boolean is_integer_type()
	{
		return is_exported() ? false : type().is_integer_type;
	}

	public default boolean is_unsigned()
	{
		return is_exported() ? false : type().is_unsigned;
	}
}
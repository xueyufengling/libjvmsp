package jvmsp;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.function.IntFunction;

import jvmsp.type.cxx_type;

/**
 * 宿主机的架构信息
 */
public enum arch
{
	x86_64("x64", "X86_64Architecture"),
	aarch64("aarch64", "AArch64Architecture"),
	power_pc("ppc64", "PPC64Architecture"),
	riscv64("riscv64", "RISCV64Architecture"),
	s390("s390", "S390Architecture");

	Class<?> _internal_class;
	Class<?> _internal_storage_class;

	public static final arch host;

	private static final arch get_host_arch()
	{
		switch (abi.host_cabi())
		{
		case "SYS_V":
		case "WIN_64":
			return x86_64;
		case "LINUX_AARCH_64":
		case "MAC_OS_AARCH_64":
		case "WIN_AARCH_64":
			return aarch64;
		case "LINUX_PPC_64_LE":
			return power_pc;
		case "LINUX_RISCV_64":
			return riscv64;
		case "LINUX_S390":
			return s390;
		case "FALLBACK":
		case "UNSUPPORTED":
		default:
			return null;
		}
	}

	static
	{
		host = get_host_arch();
	}

	private arch(String abi_pkg_name, String arch_class_name)
	{
		try
		{
			// jdk.internal.foreign.abi.<abi_pkg_name>.<arch_class_name>为实际类名
			_internal_class = Class.forName("jdk.internal.foreign.abi." + abi_pkg_name + "." + arch_class_name);
			_internal_storage_class = _internal_inner_class("StorageType");
		}
		catch (ClassNotFoundException ex)
		{
			throw new java.lang.InternalError("get internal arch of '" + this.name() + "' failed", ex);
		}
	}

	Class<?> _internal_inner_class(String inner_class_name)
	{
		try
		{
			return Class.forName(_internal_class.getName() + "$" + inner_class_name);
		}
		catch (ClassNotFoundException ex)
		{
			throw new java.lang.InternalError("get inner class '" + inner_class_name + "' of '" + _internal_class + "' failed", ex);
		}
	}

	/**
	 * 宿主机的操作系统信息
	 */
	public static enum os
	{
		linux, macos, windows, aix, unknown;

		public static final os host;

		private static Class<?> jdk_internal_util_PlatformProps;

		public static boolean TARGET_OS_IS_LINUX;
		public static boolean TARGET_OS_IS_MACOS;
		public static boolean TARGET_OS_IS_WINDOWS;
		public static boolean TARGET_OS_IS_AIX;

		static
		{
			try
			{
				jdk_internal_util_PlatformProps = Class.forName("jdk.internal.util.PlatformProps");
				TARGET_OS_IS_LINUX = (boolean) symbols.find_static_var(jdk_internal_util_PlatformProps, "TARGET_OS_IS_LINUX", boolean.class).get();
				TARGET_OS_IS_MACOS = (boolean) symbols.find_static_var(jdk_internal_util_PlatformProps, "TARGET_OS_IS_MACOS", boolean.class).get();
				TARGET_OS_IS_WINDOWS = (boolean) symbols.find_static_var(jdk_internal_util_PlatformProps, "TARGET_OS_IS_WINDOWS", boolean.class).get();
				TARGET_OS_IS_AIX = (boolean) symbols.find_static_var(jdk_internal_util_PlatformProps, "TARGET_OS_IS_AIX", boolean.class).get();
			}
			catch (ClassNotFoundException ex)
			{
				ex.printStackTrace();
			}
		}

		private static final os get_host_os()
		{
			if (TARGET_OS_IS_LINUX)
				return linux;
			else if (TARGET_OS_IS_MACOS)
				return macos;
			else if (TARGET_OS_IS_WINDOWS)
				return windows;
			else if (TARGET_OS_IS_AIX)
				return aix;
			else
				return unknown;
		}

		static
		{
			host = get_host_os();
		}
	}

	public static final class storage
	{
		public static enum type
		{
			INTEGER,
			FLOAT,
			VECTOR, // x86_64专属
			X87, // x86_64专属
			STACK,
			PLACEHOLDER;

			private byte type_id;

			private type()
			{
				try
				{
					type_id = (byte) symbols.find_static_var(arch.host._internal_storage_class, this.name(), byte.class).get();
				}
				catch (Throwable ex)
				{
					// 未找到则说明该架构无该类型的StorageType
					type_id = -1;
				}
			}

			public final boolean is_available()
			{
				return type_id >= 0;
			}

			public final byte type_id()
			{
				return type_id;
			}

			public final Object allocate(short segment_mask_or_size, int index_or_offset, String debug_name)
			{
				if (this.is_available())
				{
					return storage.allocate(type_id, segment_mask_or_size, index_or_offset, debug_name);
				}
				else
				{
					throw new java.lang.InternalError("storage '" + debug_name + "' of type '" + this.name() + "' is not available on " + arch.host);
				}
			}

			public final Object allocate(short segment_mask_or_size, int index_or_offset)
			{
				return allocate(segment_mask_or_size, index_or_offset, this.name() + "@" + index_or_offset + "$" + segment_mask_or_size);
			}
		}

		private static Class<?> jdk_internal_foreign_abi_VMStorage;

		private static Field VMStorage_type;
		private static Field VMStorage_segmentMaskOrSize;
		private static Field VMStorage_indexOrOffset;
		private static Field VMStorage_debugName;

		static
		{
			try
			{
				jdk_internal_foreign_abi_VMStorage = Class.forName("jdk.internal.foreign.abi.VMStorage");
				VMStorage_type = reflection.find_declared_field(jdk_internal_foreign_abi_VMStorage, "type");
				VMStorage_segmentMaskOrSize = reflection.find_declared_field(jdk_internal_foreign_abi_VMStorage, "segmentMaskOrSize");
				VMStorage_indexOrOffset = reflection.find_declared_field(jdk_internal_foreign_abi_VMStorage, "indexOrOffset");
				VMStorage_debugName = reflection.find_declared_field(jdk_internal_foreign_abi_VMStorage, "debugName");
			}
			catch (ClassNotFoundException ex)
			{
				ex.printStackTrace();
			}
		}

		public static final Object set_type(Object vm_storage, byte type)
		{
			unsafe.write(vm_storage, VMStorage_type, type);
			return vm_storage;
		}

		public static final Object set_type(Object vm_storage, short segment_mask_or_size)
		{
			unsafe.write(vm_storage, VMStorage_segmentMaskOrSize, segment_mask_or_size);
			return vm_storage;
		}

		public static final Object set_type(Object vm_storage, int index_or_offset)
		{
			unsafe.write(vm_storage, VMStorage_indexOrOffset, index_or_offset);
			return vm_storage;
		}

		public static final Object set_type(Object vm_storage, String debug_name)
		{
			unsafe.write(vm_storage, VMStorage_debugName, debug_name);
			return vm_storage;
		}

		public static final Object[] new_array(int size)
		{
			return (Object[]) Array.newInstance(jdk_internal_foreign_abi_VMStorage, size);
		}

		public static final IntFunction<Object[]> _new = (int size) -> new_array(size);

		public static final Object allocate(byte type, short segment_mask_or_size, int index_or_offset, String debug_name)
		{
			Object vm_storage = unsafe.allocate(jdk_internal_foreign_abi_VMStorage);
			unsafe.write(vm_storage, VMStorage_type, type);
			unsafe.write(vm_storage, VMStorage_segmentMaskOrSize, segment_mask_or_size);
			unsafe.write(vm_storage, VMStorage_indexOrOffset, index_or_offset);
			unsafe.write(vm_storage, VMStorage_debugName, debug_name);
			return vm_storage;
		}

		/**
		 * 为C++类型分配VMStrorage栈空间
		 * 
		 * @param types
		 * @return
		 */
		public static final Object[] allocate_stack(cxx_type... types)
		{
			Object[] vm_storage_arr = new_array(types.length);
			for (int idx = 0; idx < types.length; ++idx)
			{
				vm_storage_arr[idx] = type.STACK.allocate((short) cxx_type.sizeof(types[idx]), 0, types[idx].typename());
			}
			return vm_storage_arr;
		}

		public static final Object[] wrap_memory(cxx_type... types)
		{
			Object[] vm_storage_arr = new_array(types.length);
			for (int idx = 0; idx < types.length; ++idx)
			{
				vm_storage_arr[idx] = type.PLACEHOLDER.allocate((short) cxx_type.sizeof(types[idx]), idx, types[idx].typename());
			}
			return vm_storage_arr;
		}
	}
}

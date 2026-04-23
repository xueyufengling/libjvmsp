package jvmsp;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * 宿主机的架构信息
 */
public enum arch
{
	x86_64("x64", "X86_64Architecture"),
	aarch64("aarch64", "AArch64Architecture"),
	ppc64("ppc64", "PPC64Architecture"),
	riscv64("riscv64", "RISCV64Architecture"),
	s390("s390", "S390Architecture");

	Class<?> _internal_class;
	Class<?> _internal_storage_type_class;
	Class<?> _internal_reg_class;

	public static final arch host;

	private static final arch get_host_arch()
	{
		String arch = System.getProperty("os.arch");
		if (arch.equals("amd64") || arch.equals("x86_64"))
		{
			return x86_64;
		}
		else if (arch.equals("aarch64"))
		{
			return aarch64;
		}
		else if (arch.equals("ppc64le"))
		{
			return ppc64;
		}
		else if (arch.equals("riscv64"))
		{
			return riscv64;
		}
		else if (arch.equals("s390x"))
		{
			return s390;
		}
		else
		{
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
			_internal_storage_type_class = _internal_inner_class("StorageType");
			_internal_reg_class = _internal_inner_class("Regs");
			stack_storage = this.new storage_type(storage_type.classify_type.STACK);
			placeholder_storage = this.new storage_type(storage_type.classify_type.PLACEHOLDER);
		}
		catch (ClassNotFoundException ex)
		{
			throw new java.lang.InternalError("get internal arch of '" + this.name() + "' failed", ex);
		}
		try
		{
			int_reg_size = (int) symbols.find_static_var(_internal_class, "INTEGER_REG_SIZE", int.class).get();
			vec_reg_size = (int) symbols.find_static_var(_internal_class, "VECTOR_REG_SIZE", int.class).get();
		}
		catch (Throwable ex)
		{
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
			// 数据交换类型
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

	/**
	 * VMStorage相关操作，操作的目标，可以是寄存器、栈。<br>
	 * Java层NativeMethodHandle与C/C++层的数据交换操作需要使用VMStorage，即参数传递和返回值。<br>
	 * jdk.internal.foreign.abi.Binding.VMStore负责将值从Java层写入C/C++层；<br>
	 * jdk.internal.foreign.abi.Binding.VMLoad负责将值从C/C++层写入Java层。<br>
	 * 数值交换目标的大小和类型则由jdk.internal.foreign.abi.VMStorage决定。<br>
	 * jdk.internal.foreign.abi.Binding实际上正是定义了MethodHandle调用C/C++时的各种操作，包括参数、返回值的数据移动、转换操作等。<br>
	 * 而每个Binding都可以转换为一系列的VMStore和VMLoad操作，最终NativeEntryPoint接收的就是转换后的VMStorage操作。<br>
	 */
	public class storage_type
	{
		private classify_type classification;
		private byte type_id;

		public static enum classify_type
		{
			INTEGER,
			FLOAT,
			VECTOR, // x86_64专属
			X87, // x86_64专属
			STACK,
			PLACEHOLDER;
		}

		public storage_type(classify_type classification)
		{
			this.classification = classification;
			try
			{
				type_id = (byte) symbols.find_static_var(_internal_storage_type_class, classification.name(), byte.class).get();
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

		public final classify_type classification()
		{
			return classification;
		}

		public static final Object _new(byte type, short segment_mask_or_size, int index_or_offset, String debug_name)
		{
			Object vm_storage = unsafe.allocate(jdk_internal_foreign_abi_VMStorage);
			unsafe.write(vm_storage, VMStorage_type, type);
			unsafe.write(vm_storage, VMStorage_segmentMaskOrSize, segment_mask_or_size);
			unsafe.write(vm_storage, VMStorage_indexOrOffset, index_or_offset);
			unsafe.write(vm_storage, VMStorage_debugName, debug_name);
			return vm_storage;
		}

		public final Object _new(short segment_mask_or_size, int index_or_offset, String debug_name)
		{
			if (this.is_available())
			{
				return _new(type_id, segment_mask_or_size, index_or_offset, debug_name);
			}
			else
			{
				throw new java.lang.InternalError("storage '" + debug_name + "' of type '" + type_id + "' is not available on " + arch.this);
			}
		}

		public final Object _new(short segment_mask_or_size, int index_or_offset)
		{
			return _new(segment_mask_or_size, index_or_offset, classification + "@" + index_or_offset + ":" + segment_mask_or_size);
		}

		public static final void set_type(Object vm_storage, byte type)
		{
			unsafe.write(vm_storage, VMStorage_type, type);
		}

		public static final void set_type(Object vm_storage, short segment_mask_or_size)
		{
			unsafe.write(vm_storage, VMStorage_segmentMaskOrSize, segment_mask_or_size);
		}

		public static final void set_type(Object vm_storage, int index_or_offset)
		{
			unsafe.write(vm_storage, VMStorage_indexOrOffset, index_or_offset);
		}

		public static final void set_type(Object vm_storage, String debug_name)
		{
			unsafe.write(vm_storage, VMStorage_debugName, debug_name);
		}

		public static final Object[] new_array(int size)
		{
			return (Object[]) Array.newInstance(jdk_internal_foreign_abi_VMStorage, size);
		}
	}

	private int int_reg_size;
	private int vec_reg_size;
	private storage_type stack_storage;
	private storage_type placeholder_storage;
	private HashMap<String, Object> regs = new HashMap<>();

	public final int int_reg_size()
	{
		return int_reg_size;
	}

	public final int vec_reg_size()
	{
		return vec_reg_size;
	}

	/**
	 * 获取指定名称寄存器的VMStorage
	 * 
	 * @param name
	 * @return
	 */
	public final Object reg(String name)
	{
		return regs.computeIfAbsent(name, (reg_name ->
		{
			try
			{
				return symbols.find_static_var(_internal_reg_class, name, jdk_internal_foreign_abi_VMStorage).get();
			}
			catch (Throwable ex)
			{
				throw new java.lang.InternalError("get reg '" + reg_name + "' of '" + _internal_reg_class + "' failed", ex);
			}
		}));
	}

	/**
	 * 栈上的VMStorage
	 * 
	 * @param size
	 * @param offset
	 * @return
	 */
	public Object stack(short size, int offset)
	{
		return stack_storage._new(size, offset);
	}

	public Object placeholder(short size, int offset)
	{
		return placeholder_storage._new(size, offset);
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
}

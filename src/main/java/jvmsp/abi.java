package jvmsp;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

import jvmsp.arch.storage_type;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.function_type;

import static jvmsp.versions.jdk_versions;

/**
 * 调用约定，用于不同架构和操作系统的函数参数、返回值传递
 * JVM内部使用的ABIDescriptor对象 https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/cpu/x86/foreignGlobals_x86_64.cpp#L46
 */
public class abi
{
	private static Class<?> jdk_internal_foreign_CABI;

	private static MethodHandle CABI_computeCurrent;

	static
	{
		try
		{
			jdk_internal_foreign_CABI = Class.forName("jdk.internal.foreign.CABI");
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		CABI_computeCurrent = symbols.find_static_method(jdk_internal_foreign_CABI, "computeCurrent", jdk_internal_foreign_CABI);
	}

	/**
	 * 获取宿主机的CABI枚举值
	 * 
	 * @return
	 */
	@SuppressWarnings(
	{ "rawtypes" })
	public static final String host_cabi()
	{
		try
		{
			return ((Enum) (Object) CABI_computeCurrent.invoke()).name();
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get host abi descriptor failed", ex);
		}
	}

	/**
	 * JDK自带的调用约定
	 */

	private static final Object jdk_internal_abi(String abi_pkg_name, String abi_context_name, String abi_instance_name, boolean is_abi_static, String call_arranger_pkg_name, String call_arranger_name, String call_arranger_instance_name, boolean is_get_bindings_static)
	{
		try
		{
			// ABIDescriptor需要与CallArranger一起使用，CallArranger负责计算参数位置和重排以符合ABI
			Class<?> _internal_abi_context_class = Class.forName("jdk.internal.foreign.abi." + abi_pkg_name + "." + abi_context_name);
			Class<?> _internal_call_arranger_class = Class.forName("jdk.internal.foreign.abi." + call_arranger_pkg_name + "." + call_arranger_name);
			Object _internal_call_arranger_instance = null;
			if (!is_abi_static || !is_get_bindings_static)
			{
				if (call_arranger_instance_name == null)
				{
					throw new java.lang.InternalError("call arranger instance name of '" + abi_pkg_name + "' cannot be null");
				}
				else
				{
					// CallArranger的实例一定是静态单例
					_internal_call_arranger_instance = unsafe.read_reference(_internal_call_arranger_class, call_arranger_instance_name);
				}
			}
			if (is_abi_static)
			{
				return unsafe.read_reference(_internal_abi_context_class, abi_instance_name);
			}
			else
			{
				Field _internal_descriptor_field = reflection.find_declared_field(_internal_abi_context_class, abi_instance_name);
				return unsafe.read_reference(_internal_call_arranger_instance, _internal_descriptor_field);
			}
		}
		catch (ClassNotFoundException ex)
		{
			throw new java.lang.InternalError("get internal abi descriptor of '" + abi_pkg_name + "' failed", ex);
		}
	}

	private static final Object jdk_internal_abi(String abi_call_arranger_pkg_name, String abi_context_call_arranger_name, String abi_instance_name, boolean is_abi_static, String call_arranger_instance_name, boolean is_get_bindings_static)
	{
		return jdk_internal_abi(abi_call_arranger_pkg_name, abi_context_call_arranger_name, abi_instance_name, is_abi_static, abi_call_arranger_pkg_name, abi_context_call_arranger_name, call_arranger_instance_name, is_get_bindings_static);
	}

	private static final Object jdk_internal_abi(String abi_call_arranger_pkg_name, String abi_context_call_arranger_name, String static_abi_instance_name, String static_call_arranger_instance_name)
	{
		return jdk_internal_abi(abi_call_arranger_pkg_name, abi_context_call_arranger_name, static_abi_instance_name, true, static_call_arranger_instance_name, true);
	}

	private static final Object jdk_internal_abi(String abi_call_arranger_pkg_name, String abi_context_call_arranger_name, String static_abi_instance_name)
	{
		return jdk_internal_abi(abi_call_arranger_pkg_name, abi_context_call_arranger_name, static_abi_instance_name, null);
	}

	public static final Object x86_64_CSysV = jdk_internal_abi("x64.sysv", "CallArranger", "CSysV");

	public static final Object x86_64_CWindows = jdk_internal_abi("x64.windows", "CallArranger", "CWindows");

	public static final Object aarch64_CLinux = jdk_internal_abi("aarch64", "CallArranger", "C", true, "LINUX", false);
	public static final Object aarch64_CMacOS = jdk_internal_abi("aarch64", "CallArranger", "C", true, "MACOS", false);
	public static final Object aarch64_CWindows = jdk_internal_abi("aarch64.windows", "WindowsAArch64CallArranger", "WindowsAArch64AbiDescriptor", true, "aarch64", "CallArranger", "WINDOWS", false);
	public static final Object ppc64_C = jdk_internal_abi("ppc64", "CallArranger", "C", false, "ABIv2", false);
	public static final Object riscv64_CLinux = jdk_internal_abi("riscv64.linux", "LinuxRISCV64CallArranger", "CLinux");
	public static final Object s390_CLinux = jdk_internal_abi("s390.linux", "LinuxS390CallArranger", "CLinux");

	public static final Object host;

	/**
	 * 获取宿主机的架构ABI
	 * 
	 * @return
	 */
	private static final Object get_host_abi_descriptor()
	{
		switch (host_cabi())
		{
		case "SYS_V":
			return x86_64_CSysV;
		case "WIN_64":
			return x86_64_CWindows;
		case "LINUX_AARCH_64":
			return aarch64_CLinux;
		case "MAC_OS_AARCH_64":
			return aarch64_CMacOS;
		case "WIN_AARCH_64":
			return aarch64_CWindows;
		case "LINUX_PPC_64_LE":
			return ppc64_C;
		case "LINUX_RISCV_64":
			return riscv64_CLinux;
		case "LINUX_S390":
			return s390_CLinux;
		case "FALLBACK":
		case "UNSUPPORTED":
		default:
			return null;
		}
	}

	static
	{
		host = get_host_abi_descriptor();
	}

	private static Class<?> jdk_internal_foreign_abi_ABIDescriptor;

	private static Field ABIDescriptor_arch;
	private static Field ABIDescriptor_inputStorage;
	private static Field ABIDescriptor_outputStorage;
	private static Field ABIDescriptor_volatileStorage;
	private static Field ABIDescriptor_stackAlignment;
	private static Field ABIDescriptor_shadowSpace;
	private static Field ABIDescriptor_scratch1;
	private static Field ABIDescriptor_scratch2;
	private static Field ABIDescriptor_targetAddrStorage;
	private static Field ABIDescriptor_retBufAddrStorage;
	private static Field ABIDescriptor_capturedStateStorage;

	static
	{
		try
		{
			jdk_internal_foreign_abi_ABIDescriptor = Class.forName("jdk.internal.foreign.abi.ABIDescriptor");
			ABIDescriptor_arch = reflection.find_declared_field(jdk_internal_foreign_abi_ABIDescriptor, "arch");
			ABIDescriptor_inputStorage = reflection.find_declared_field(jdk_internal_foreign_abi_ABIDescriptor, "inputStorage");
			ABIDescriptor_outputStorage = reflection.find_declared_field(jdk_internal_foreign_abi_ABIDescriptor, "outputStorage");
			ABIDescriptor_volatileStorage = reflection.find_declared_field(jdk_internal_foreign_abi_ABIDescriptor, "volatileStorage");
			ABIDescriptor_stackAlignment = reflection.find_declared_field(jdk_internal_foreign_abi_ABIDescriptor, "stackAlignment");
			ABIDescriptor_shadowSpace = reflection.find_declared_field(jdk_internal_foreign_abi_ABIDescriptor, "shadowSpace");
			ABIDescriptor_scratch1 = reflection.find_declared_field(jdk_internal_foreign_abi_ABIDescriptor, "scratch1");
			ABIDescriptor_scratch2 = reflection.find_declared_field(jdk_internal_foreign_abi_ABIDescriptor, "scratch2");
			ABIDescriptor_targetAddrStorage = reflection.find_declared_field(jdk_internal_foreign_abi_ABIDescriptor, "targetAddrStorage");
			ABIDescriptor_retBufAddrStorage = reflection.find_declared_field(jdk_internal_foreign_abi_ABIDescriptor, "retBufAddrStorage");
			ABIDescriptor_capturedStateStorage = reflection.find_declared_field(jdk_internal_foreign_abi_ABIDescriptor, "capturedStateStorage");
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * jdk.internal.foreign.abi.StubLocations
	 */
	public static enum stub_locations
	{
		TARGET_ADDRESS,
		RETURN_BUFFER,
		CAPTURED_STATE_BUFFER;

		public final Object storage(byte type)
		{
			return storage_type._new(type, (short) 8, ordinal(), this.name());
		}

		public final Object storage(storage_type type)
		{
			return storage(type.type_id());
		}

		public final Object storage(arch arch, storage_type.classify_type type)
		{
			return storage(arch.storage(type));
		}
	}

	public static final Object abi_descriptor(Object internal_arch, Object[][] input_storage, Object[][] output_storage,
			Object[][] volatile_storage, int stack_alignment, int shadow_space,
			Object scratch1, Object scratch2,
			Object target_addr_storage, Object ret_buf_addr_storage,
			Object captured_state_storage)
	{
		Object abi = unsafe.allocate(jdk_internal_foreign_abi_ABIDescriptor);
		unsafe.write(abi, ABIDescriptor_arch, internal_arch);
		unsafe.write(abi, ABIDescriptor_inputStorage, input_storage);
		unsafe.write(abi, ABIDescriptor_outputStorage, output_storage);
		unsafe.write(abi, ABIDescriptor_volatileStorage, volatile_storage);
		unsafe.write(abi, ABIDescriptor_stackAlignment, stack_alignment);
		unsafe.write(abi, ABIDescriptor_shadowSpace, shadow_space);
		unsafe.write(abi, ABIDescriptor_scratch1, scratch1);
		unsafe.write(abi, ABIDescriptor_scratch2, scratch2);
		unsafe.write(abi, ABIDescriptor_targetAddrStorage, target_addr_storage);
		unsafe.write(abi, ABIDescriptor_retBufAddrStorage, ret_buf_addr_storage);
		unsafe.write(abi, ABIDescriptor_capturedStateStorage, captured_state_storage);
		return abi;
	}

	public static final Object abi_descriptor(arch arch,
			Object[] arg_int_regs,
			Object[] arg_vec_regs,
			Object[] ret_int_regs,
			Object[] ret_vec_regs,
			Object[] volatile_int_regs,
			Object[] volatile_vec_regs,
			int stack_alignment,
			int shadow_space,
			Object scratch1, Object scratch2)
	{
		return abi_descriptor(
				arch.internal_arch(),
				storage_type.new_2d_array(arg_int_regs, arg_vec_regs),
				storage_type.new_2d_array(ret_int_regs, ret_vec_regs),
				storage_type.new_2d_array(volatile_int_regs, volatile_vec_regs),
				stack_alignment,
				shadow_space,
				scratch1, scratch2,
				stub_locations.TARGET_ADDRESS.storage(arch, storage_type.classify_type.PLACEHOLDER),
				stub_locations.RETURN_BUFFER.storage(arch, storage_type.classify_type.PLACEHOLDER),
				stub_locations.CAPTURED_STATE_BUFFER.storage(arch, storage_type.classify_type.PLACEHOLDER));
	}

	/**
	 * ABI的调用参数存储寄存器，按顺序排布
	 * 
	 * @param abi
	 * @return
	 */
	public static final Object[][] arg_regs(Object abi)
	{
		try
		{
			return (Object[][]) unsafe.read_reference(abi, ABIDescriptor_inputStorage);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get arg regs of abi '" + abi + "' failed", ex);
		}
	}

	public static final Object[] arg_int_regs(Object abi)
	{
		return arg_regs(abi)[0];
	}

	public static final Object[] arg_vec_regs(Object abi)
	{
		return arg_regs(abi)[1];
	}

	public static final Object[][] ret_regs(Object abi)
	{
		try
		{
			return (Object[][]) unsafe.read_reference(abi, ABIDescriptor_outputStorage);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get ret regs of abi '" + abi + "' failed", ex);
		}
	}

	public static final Object[] ret_int_regs(Object abi)
	{
		return ret_regs(abi)[0];
	}

	public static final Object[] ret_vec_regs(Object abi)
	{
		return ret_regs(abi)[1];
	}

	public static final int stack_alignment(Object abi)
	{
		try
		{
			return (int) unsafe.read_int(abi, ABIDescriptor_stackAlignment);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get stack alignment of abi '" + abi + "' failed", ex);
		}
	}

	public static final int shadow_space(Object abi)
	{
		try
		{
			return (int) unsafe.read_int(abi, ABIDescriptor_shadowSpace);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get shadow space of abi '" + abi + "' failed", ex);
		}
	}

	public static final Object internal_arch(Object abi)
	{
		try
		{
			return unsafe.read_reference(abi, ABIDescriptor_arch);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get architecture of abi '" + abi + "' failed", ex);
		}
	}

	public static final arch arch_of(Object abi_descriptor)
	{
		return arch.of_internal(internal_arch(abi_descriptor));
	}

	/**
	 * NativeMethodHandle与stub函数之间的调用约定。<br>
	 * 此外，JVM层生成stub函数时需要使用ABIDescriptor的_shadow_space_bytes、_scratch1等参数。<br>
	 * ABIDescriptor∈abi∈call_convention，其中ABIDescriptor仅包含该调用约定使用的储存位置，abi包含了ABIDescriptor和架构信息及其操作，call_convention决定了具体的调用参数排布策略
	 */
	public static abstract class call_convention
	{
		protected final Object abi_descriptor;
		protected final arch abi_arch;
		protected final Object[] arg_int_regs;
		protected final Object[] arg_vec_regs;
		protected final Object[] ret_int_regs;
		protected final Object[] ret_vec_regs;

		protected final int stack_alignment;

		public call_convention(Object abi_descriptor)
		{
			this.abi_descriptor = abi_descriptor;
			this.abi_arch = arch_of(abi_descriptor);
			this.arg_int_regs = arg_int_regs(abi_descriptor);
			this.arg_vec_regs = arg_vec_regs(abi_descriptor);
			this.ret_int_regs = ret_int_regs(abi_descriptor);
			this.ret_vec_regs = ret_vec_regs(abi_descriptor);
			this.stack_alignment = stack_alignment(abi_descriptor);
		}

		public final Object abi_descriptor()
		{
			return abi_descriptor;
		}

		protected class context
		{
			protected int occupied_arg_int_reg_idx = -1;
			protected int occupied_arg_vec_reg_idx = -1;

			protected int occupied_ret_int_reg_idx = -1;
			protected int occupied_ret_vec_reg_idx = -1;

			protected int occupied_volatile_reg_idx = -1;
			protected int stack_top = 0;// 栈顶偏移量

			private context()
			{
			}

			public void sync_arg_vec_reg_idx_as_int()
			{
				occupied_arg_vec_reg_idx = occupied_arg_int_reg_idx;
			}

			public Object next_arg_int_reg()
			{
				if (++occupied_arg_int_reg_idx < arg_int_regs.length)
					return arg_int_regs[occupied_arg_int_reg_idx];
				else
					return null;
			}

			public Object next_arg_vec_reg()
			{
				if (++occupied_arg_vec_reg_idx < arg_vec_regs.length)
					return arg_vec_regs[occupied_arg_vec_reg_idx];
				else
					return null;
			}

			public Object next_ret_int_reg()
			{
				if (++occupied_ret_int_reg_idx < ret_int_regs.length)
					return ret_int_regs[occupied_ret_int_reg_idx];
				else
					return null;
			}

			public Object next_ret_vec_reg()
			{
				if (++occupied_ret_vec_reg_idx < ret_vec_regs.length)
					return ret_vec_regs[occupied_ret_vec_reg_idx];
				else
					return null;
			}

			public Object stack(short size)
			{
				Object stack_mem = abi_arch.stack((short) size, stack_top);
				// 偏移量对齐
				int mod = size % stack_alignment;
				stack_top += size;
				if (mod != 0)
					stack_top += stack_alignment - mod;
				return stack_mem;
			}
		}

		public abstract Object resolve_arg_op(int idx, cxx_type type, context ctx);

		public final Object[] resolve_arg_ops(boolean is_stub, cxx_type... arg_types)
		{
			int insert_delta = 0;// 插入目标函数指针后的索引增量
			Object[] arg_ops = storage_type.new_array(is_stub ? arg_types.length + 1 : arg_types.length);
			if (is_stub)
			{
				insert_delta = 1;
				arg_ops[0] = abi_arch.placeholder((short) cxx_type.pvoid.size(), 0);// 将传入NativeMethodHandle的第一个参数移入PLACEHOLDER偏移量为0的内存
			}
			context ctx = this.new context();
			for (int idx = 0; idx < arg_types.length; ++idx)
			{
				arg_ops[idx + insert_delta] = resolve_arg_op(idx, arg_types[idx], ctx);
			}
			return arg_ops;
		}

		public abstract Object resolve_ret_op(int idx, cxx_type type, context ctx);

		public final Object[] resolve_ret_ops(cxx_type... ret_types)
		{
			if (ret_types[0] == cxx_type._void)
			{
				return storage_type.new_array(0);// 无返回值
			}
			else
			{
				context ctx = this.new context();
				Object[] ret_ops = storage_type.new_array(ret_types.length);
				for (int idx = 0; idx < ret_types.length; ++idx)
				{
					cxx_type type = ret_types[idx];
					if (type.is_undefinable())
						throw new java.lang.InternalError("return type '" + type + "' is undefinable");
					ret_ops[idx] = resolve_ret_op(idx, type, ctx);
				}
				return ret_ops;
			}
		}

		public static final call_convention windows_x64 = new call_convention(abi.x86_64_CWindows)
		{
			@Override
			public Object resolve_arg_op(int idx, cxx_type type, context ctx)
			{
				// 前n个参数用寄存器传递，浮点数用vec寄存器，整数、指针用通用寄存器
				// 剩下的参数用栈传递
				Object reg = null;
				if (type.is_float())
				{
					reg = ctx.next_arg_vec_reg();
				}
				else
				{
					reg = ctx.next_arg_int_reg();
				}
				ctx.sync_arg_vec_reg_idx_as_int();
				return reg != null ? reg : ctx.stack((short) type.size());
			}

			@Override
			public Object resolve_ret_op(int idx, cxx_type type, context ctx)
			{
				Object reg = null;
				if (type.is_float())
				{
					reg = ctx.next_ret_vec_reg();
				}
				else
				{
					reg = ctx.next_ret_int_reg();
				}
				return reg != null ? reg : ctx.stack((short) type.size());
			}
		};

		public static final call_convention sysv_x64 = new call_convention(abi.x86_64_CSysV)
		{
			@Override
			public Object resolve_arg_op(int idx, cxx_type type, context ctx)
			{
				// 前n个参数用寄存器传递，浮点数用vec寄存器，整数、指针用通用寄存器
				// 剩下的参数用栈传递
				Object reg = null;
				if (type.is_float())
				{
					reg = ctx.next_arg_vec_reg();
				}
				else
				{
					reg = ctx.next_arg_int_reg();
				}
				ctx.sync_arg_vec_reg_idx_as_int();
				return reg != null ? reg : ctx.stack((short) type.size());
			}

			@Override
			public Object resolve_ret_op(int idx, cxx_type type, context ctx)
			{
				Object reg = null;
				if (type.is_float())
				{
					reg = ctx.next_ret_vec_reg();
				}
				else
				{
					reg = ctx.next_ret_int_reg();
				}
				return reg != null ? reg : ctx.stack((short) type.size());
			}
		};

		public static final call_convention host;

		private static final call_convention get_host_call_convention()
		{
			switch (host_cabi())
			{
			case "SYS_V":
			case "WIN_64":
				return windows_x64;
			case "LINUX_AARCH_64":
			case "MAC_OS_AARCH_64":
			case "WIN_AARCH_64":
			case "LINUX_PPC_64_LE":
			case "LINUX_RISCV_64":
			case "LINUX_S390":
			case "FALLBACK":
			case "UNSUPPORTED":
			default:
				return null;
			}
		}

		static
		{
			host = get_host_call_convention();
		}
	}

	/**
	 * stub函数的约束，类似GCC的内联汇编，需要指定参数和返回值存放位置
	 */
	public static class nmh_stub_constraint
	{
		private call_convention call_conv;
		private function_type func_type;
		private MethodType stub_method_type;

		private Object[] arg_vm_storage;
		private Object[] ret_vm_storage;

		private boolean needs_transition;

		/**
		 * 决议NativeMethodHandle与stub函数之间的约束
		 * 
		 * @param func_type        函数类型
		 * @param needs_transition 是否需要保存当前Java上下文的现场，如果目标函数不涉及Java相关操作则不需要保护现场
		 */
		public nmh_stub_constraint(call_convention call_conv, function_type func_type, boolean needs_transition)
		{
			this.call_conv = call_conv;
			this.func_type = func_type;
			this.stub_method_type = func_type.to_method_type().insertParameterTypes(0, long.class);// 添加一个函数指针首参数，指向实际待执行的函数
			this.arg_vm_storage = call_conv.resolve_arg_ops(true, func_type.argument_types());
			this.ret_vm_storage = call_conv.resolve_ret_ops(func_type.return_type());
			this.needs_transition = needs_transition;
		}

		public nmh_stub_constraint(call_convention call_conv, function_type func_type)
		{
			this(call_conv, func_type, false);
		}

		public nmh_stub_constraint(function_type func_type)
		{
			this(call_convention.host, func_type, false);
		}

		public final call_convention call_conv()
		{
			return call_conv;
		}

		public final function_type func_type()
		{
			return func_type;
		}

		public final boolean is_in_memory_return()
		{
			return ret_vm_storage.length > 1;// 返回目标位置大于1则需要内存中保存多个返回结果
		}

		public final MethodType stub_method_type()
		{
			return stub_method_type;
		}

		public final boolean needs_return_buffer()
		{
			return false;
		}

		public final long return_buffer_size()
		{
			return 0;
		}

		public final boolean needs_transition()
		{
			return needs_transition;
		}

		public final int captured_state_mask()
		{
			return 0;
		}

		/**
		 * 获取返回值的VMStorage[]
		 * 
		 * @return
		 */
		public final Object[] ret_vm_storage()
		{
			return ret_vm_storage;
		}

		/**
		 * 获取参数的VMStorage[]
		 * 
		 * @return
		 */
		public final Object[] arg_vm_storage()
		{
			return arg_vm_storage;
		}
	}

	/**
	 * native调用相关
	 */

	private static Class<?> jdk_internal_foreign_abi_NativeEntryPoint;

	private static Class<?> jdk_internal_foreign_abi_VMStorage;

	private static MethodHandle NativeEntryPoint_ctor;
	private static MethodHandle NativeEntryPoint_makeDowncallStub;
	private static MethodHandle NativeEntryPoint_freeDowncallStub0;
	private static MethodHandle NativeEntryPoint_make;

	private static VarHandle NativeEntryPoint_methodType;

	static
	{
		try
		{
			jdk_internal_foreign_abi_NativeEntryPoint = Class.forName("jdk.internal.foreign.abi.NativeEntryPoint");
			jdk_internal_foreign_abi_VMStorage = Class.forName("jdk.internal.foreign.abi.VMStorage");
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		NativeEntryPoint_ctor = symbols.find_constructor(jdk_internal_foreign_abi_NativeEntryPoint, MethodType.class, long.class);
		NativeEntryPoint_methodType = symbols.find_var(jdk_internal_foreign_abi_NativeEntryPoint, "methodType", MethodType.class);
		NativeEntryPoint_freeDowncallStub0 = symbols.find_static_method(jdk_internal_foreign_abi_NativeEntryPoint, "freeDowncallStub0", boolean.class, long.class);

		NativeEntryPoint_makeDowncallStub = jdk_versions.switch_execute_nonnull(
				// JDK21
				() -> symbols.find_static_method(jdk_internal_foreign_abi_NativeEntryPoint, "makeDowncallStub", long.class,
						MethodType.class, jdk_internal_foreign_abi_ABIDescriptor, jdk_internal_foreign_abi_VMStorage.arrayType(), jdk_internal_foreign_abi_VMStorage.arrayType(), boolean.class, int.class, boolean.class),
				// JDK25 新增参数 usingAddressPairs
				() -> symbols.find_static_method(jdk_internal_foreign_abi_NativeEntryPoint, "makeDowncallStub", long.class,
						MethodType.class, jdk_internal_foreign_abi_ABIDescriptor, jdk_internal_foreign_abi_VMStorage.arrayType(), jdk_internal_foreign_abi_VMStorage.arrayType(), boolean.class, int.class, boolean.class, boolean.class));

		NativeEntryPoint_make = jdk_versions.switch_execute_nonnull(
				// JDK21
				() -> symbols.find_static_method(jdk_internal_foreign_abi_NativeEntryPoint, "make", jdk_internal_foreign_abi_NativeEntryPoint,
						jdk_internal_foreign_abi_ABIDescriptor, jdk_internal_foreign_abi_VMStorage.arrayType(), jdk_internal_foreign_abi_VMStorage.arrayType(), MethodType.class, boolean.class, int.class, boolean.class),
				// JDK25 新增参数 usingAddressPairs
				() -> symbols.find_static_method(jdk_internal_foreign_abi_NativeEntryPoint, "make", jdk_internal_foreign_abi_NativeEntryPoint,
						jdk_internal_foreign_abi_ABIDescriptor, jdk_internal_foreign_abi_VMStorage.arrayType(), jdk_internal_foreign_abi_VMStorage.arrayType(), MethodType.class, boolean.class, int.class, boolean.class, boolean.class));
	}

	// @formatter:off
	/* 
	 * FFI使用动态生成机器码来生成native调用的stub，该stub负责传入MethodHandle.invoke()的参数的处理和返回值移交。
	 * stub函数的第一个参数必须是目标函数的函数指针，且只要函数签名相同，都使用同一个stub，区别只在于函数指针不同。
	 * 例如，对于JNI_GetCreatedJavaVMs()函数生成的隐藏类字节码为   
	public final class jdk.internal.foreign.abi.DowncallStub
	  minor version: 0
	  major version: 65
	  flags: (0x0031) ACC_PUBLIC, ACC_FINAL, ACC_SUPER
	  this_class: #2                          // jdk/internal/foreign/abi/DowncallStub
	  super_class: #4                         // java/lang/Object
	  interfaces: 0, fields: 0, methods: 1, attributes: 1
	Constant pool:
	   #1 = Utf8               jdk/internal/foreign/abi/DowncallStub
	   #2 = Class              #1             // jdk/internal/foreign/abi/DowncallStub
	   #3 = Utf8               java/lang/Object
	   #4 = Class              #3             // java/lang/Object
	   #5 = Utf8               invoke
	   #6 = Utf8               (Ljava/lang/foreign/SegmentAllocator;Ljava/lang/foreign/MemorySegment;Ljava/lang/foreign/MemorySegment;ILjava/lang/foreign/MemorySegment;)I
	   #7 = Utf8               jdk/internal/foreign/abi/SharedUtils
	   #8 = Class              #7             // jdk/internal/foreign/abi/SharedUtils
	   #9 = Utf8               DUMMY_ARENA
	  #10 = Utf8               Ljava/lang/foreign/Arena;
	  #11 = NameAndType        #9:#10         // DUMMY_ARENA:Ljava/lang/foreign/Arena;
	  #12 = Fieldref           #8.#11         // jdk/internal/foreign/abi/SharedUtils.DUMMY_ARENA:Ljava/lang/foreign/Arena;
	  #13 = Utf8               jdk/internal/foreign/AbstractMemorySegmentImpl
	  #14 = Class              #13            // jdk/internal/foreign/AbstractMemorySegmentImpl
	  #15 = Utf8               sessionImpl
	  #16 = Utf8               ()Ljdk/internal/foreign/MemorySessionImpl;
	  #17 = NameAndType        #15:#16        // sessionImpl:()Ljdk/internal/foreign/MemorySessionImpl;
	  #18 = Methodref          #14.#17        // jdk/internal/foreign/AbstractMemorySegmentImpl.sessionImpl:()Ljdk/internal/foreign/MemorySessionImpl;
	  #19 = Utf8               jdk/internal/foreign/MemorySessionImpl
	  #20 = Class              #19            // jdk/internal/foreign/MemorySessionImpl
	  #21 = Utf8               acquire0
	  #22 = Utf8               ()V
	  #23 = NameAndType        #21:#22        // acquire0:()V
	  #24 = Methodref          #20.#23        // jdk/internal/foreign/MemorySessionImpl.acquire0:()V
	  #25 = Utf8               unboxSegment
	  #26 = Utf8               (Ljava/lang/foreign/MemorySegment;)J
	  #27 = NameAndType        #25:#26        // unboxSegment:(Ljava/lang/foreign/MemorySegment;)J
	  #28 = Methodref          #8.#27         // jdk/internal/foreign/abi/SharedUtils.unboxSegment:(Ljava/lang/foreign/MemorySegment;)J
	  #29 = Utf8               java/lang/invoke/MethodHandles
	  #30 = Class              #29            // java/lang/invoke/MethodHandles
	  #31 = Utf8               classData
	  #32 = Utf8               (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
	  #33 = NameAndType        #31:#32        // classData:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
	  #34 = Methodref          #30.#33        // java/lang/invoke/MethodHandles.classData:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
	  #35 = MethodHandle       6:#34          // REF_invokeStatic java/lang/invoke/MethodHandles.classData:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
	  #36 = Utf8               _
	  #37 = Utf8               Ljava/lang/Object;
	  #38 = NameAndType        #36:#37        // _:Ljava/lang/Object;
	  #39 = Dynamic            #0:#38         // #0:_:Ljava/lang/Object; 动态常量，通过BootstrapMethods表中索引为0的引导方法计算，名称为"_"，类型为java.lang.Object
	  #40 = Utf8               java/lang/invoke/MethodHandle
	  #41 = Class              #40            // java/lang/invoke/MethodHandle
	  #42 = Utf8               invokeExact
	  #43 = Utf8               (JJIJ)I
	  #44 = NameAndType        #42:#43        // invokeExact:(JJIJ)I
	  #45 = Methodref          #41.#44        // java/lang/invoke/MethodHandle.invokeExact:(JJIJ)I
	  #46 = Utf8               java/lang/foreign/Arena
	  #47 = Class              #46            // java/lang/foreign/Arena
	  #48 = Utf8               close
	  #49 = NameAndType        #48:#22        // close:()V
	  #50 = InterfaceMethodref #47.#49        // java/lang/foreign/Arena.close:()V
	  #51 = Utf8               release0
	  #52 = NameAndType        #51:#22        // release0:()V
	  #53 = Methodref          #20.#52        // jdk/internal/foreign/MemorySessionImpl.release0:()V
	  #54 = Utf8               Code
	  #55 = Utf8               StackMapTable
	  #56 = Utf8               java/lang/foreign/SegmentAllocator
	  #57 = Class              #56            // java/lang/foreign/SegmentAllocator
	  #58 = Utf8               java/lang/foreign/MemorySegment
	  #59 = Class              #58            // java/lang/foreign/MemorySegment
	  #60 = Utf8               java/lang/Throwable
	  #61 = Class              #60            // java/lang/Throwable
	  #62 = Utf8               BootstrapMethods
	{
	  // Java层用户调用的native函数的MethodHandle，实际上是调用了此invoke()方法，此方法内又调用了stub函数。
	  // 原函数签名为jint JNI_GetCreatedJavaVMs(JavaVM **vm_buf, jsize bufLen, jsize *numVMs)
	  // 可以看到，invoke()方法中的C/C++地址从stub方法的long变为了MemorySegment，这是Java层对native地址做的包装。
	  public static int invoke(java.lang.foreign.SegmentAllocator, java.lang.foreign.MemorySegment, java.lang.foreign.MemorySegment, int, java.lang.foreign.MemorySegment);
	    descriptor: (Ljava/lang/foreign/SegmentAllocator;Ljava/lang/foreign/MemorySegment;Ljava/lang/foreign/MemorySegment;ILjava/lang/foreign/MemorySegment;)I
	    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
	    Code:
	      stack=8, locals=17, args_size=5
	         0: aconst_null
	         1: astore        12
	         3: aconst_null
	         4: astore        13
	         6: aconst_null
	         7: astore        14
	         9: getstatic     #12                 // Field jdk/internal/foreign/abi/SharedUtils.DUMMY_ARENA:Ljava/lang/foreign/Arena;
	        12: astore        15
	        14: aload_1
	        15: dup
	        16: checkcast     #14                 // class jdk/internal/foreign/AbstractMemorySegmentImpl
	        19: invokevirtual #18                 // Method jdk/internal/foreign/AbstractMemorySegmentImpl.sessionImpl:()Ljdk/internal/foreign/MemorySessionImpl;
	        22: dup
	        23: invokevirtual #24                 // Method jdk/internal/foreign/MemorySessionImpl.acquire0:()V
	        26: astore        12
	        28: invokestatic  #28                 // Method jdk/internal/foreign/abi/SharedUtils.unboxSegment:(Ljava/lang/foreign/MemorySegment;)J
	        31: lstore        5
	        33: aload_2
	        34: dup
	        35: checkcast     #14                 // class jdk/internal/foreign/AbstractMemorySegmentImpl
	        38: invokevirtual #18                 // Method jdk/internal/foreign/AbstractMemorySegmentImpl.sessionImpl:()Ljdk/internal/foreign/MemorySessionImpl;
	        41: dup
	        42: aload         12
	        44: if_acmpeq     56
	        47: dup
	        48: invokevirtual #24                 // Method jdk/internal/foreign/MemorySessionImpl.acquire0:()V
	        51: astore        13
	        53: goto          57
	        56: pop
	        57: invokestatic  #28                 // Method jdk/internal/foreign/abi/SharedUtils.unboxSegment:(Ljava/lang/foreign/MemorySegment;)J
	        60: lstore        7
	        62: iload_3
	        63: istore        9
	        65: aload         4
	        67: dup
	        68: checkcast     #14                 // class jdk/internal/foreign/AbstractMemorySegmentImpl
	        71: invokevirtual #18                 // Method jdk/internal/foreign/AbstractMemorySegmentImpl.sessionImpl:()Ljdk/internal/foreign/MemorySessionImpl;
	        74: dup
	        75: aload         12
	        77: if_acmpeq     95
	        80: dup
	        81: aload         13
	        83: if_acmpeq     95
	        86: dup
	        87: invokevirtual #24                 // Method jdk/internal/foreign/MemorySessionImpl.acquire0:()V
	        90: astore        14
	        92: goto          96
	        95: pop
	        96: invokestatic  #28                 // Method jdk/internal/foreign/abi/SharedUtils.unboxSegment:(Ljava/lang/foreign/MemorySegment;)J
	        99: lstore        10
	       101: ldc           #39                 // Dynamic #0:_:Ljava/lang/Object; 注意！这里是运行时加载了常量池中MethodHandles.classData()获取的动态的#39对象入栈，该对象实际是stub函数的MethodHandle
	       103: checkcast     #41                 // class java/lang/invoke/MethodHandle 检查压入栈的#39是否是#41类型
	       106: lload         5                   // 目标函数指针地址
	       108: lload         7                   // vm_buf指针地址
	       110: iload         9                   // bufLen
	       112: lload         10                  // numVMs指针地址
	       114: invokevirtual #45                 // Method java/lang/invoke/MethodHandle.invokeExact:(JJIJ)I
	       117: istore        16
	       119: iload         16
	       121: aload         15
	       123: checkcast     #47                 // class java/lang/foreign/Arena
	       126: invokeinterface #50,  1           // InterfaceMethod java/lang/foreign/Arena.close:()V
	       131: aload         12
	       133: ifnull        141
	       136: aload         12
	       138: invokevirtual #53                 // Method jdk/internal/foreign/MemorySessionImpl.release0:()V
	       141: aload         13
	       143: ifnull        151
	       146: aload         13
	       148: invokevirtual #53                 // Method jdk/internal/foreign/MemorySessionImpl.release0:()V
	       151: aload         14
	       153: ifnull        161
	       156: aload         14
	       158: invokevirtual #53                 // Method jdk/internal/foreign/MemorySessionImpl.release0:()V
	       161: ireturn
	       162: aload         15
	       164: checkcast     #47                 // class java/lang/foreign/Arena
	       167: invokeinterface #50,  1           // InterfaceMethod java/lang/foreign/Arena.close:()V
	       172: aload         12
	       174: ifnull        182
	       177: aload         12
	       179: invokevirtual #53                 // Method jdk/internal/foreign/MemorySessionImpl.release0:()V
	       182: aload         13
	       184: ifnull        192
	       187: aload         13
	       189: invokevirtual #53                 // Method jdk/internal/foreign/MemorySessionImpl.release0:()V
	       192: aload         14
	       194: ifnull        202
	       197: aload         14
	       199: invokevirtual #53                 // Method jdk/internal/foreign/MemorySessionImpl.release0:()V
	       202: athrow
	      Exception table:
	         from    to  target type
	            14   121   162   any
	      StackMapTable: number_of_entries = 11
	        frame_type = 255 // full_frame
	          offset_delta = 56
	          locals = [ class java/lang/foreign/SegmentAllocator, class java/lang/foreign/MemorySegment, class java/lang/foreign/MemorySegment, int, class java/lang/foreign/MemorySegment, long, top, top, top, top, top, class jdk/internal/foreign/MemorySessionImpl, null, null, class java/lang/foreign/Arena ]
	          stack = [ class java/lang/foreign/MemorySegment, class jdk/internal/foreign/MemorySessionImpl ]
	        frame_type = 255 // full_frame
	          offset_delta = 0
	          locals = [ class java/lang/foreign/SegmentAllocator, class java/lang/foreign/MemorySegment, class java/lang/foreign/MemorySegment, int, class java/lang/foreign/MemorySegment, long, top, top, top, top, top, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, null, class java/lang/foreign/Arena ]
	          stack = [ class java/lang/foreign/MemorySegment ]
	        frame_type = 255 // full_frame
	          offset_delta = 37
	          locals = [ class java/lang/foreign/SegmentAllocator, class java/lang/foreign/MemorySegment, class java/lang/foreign/MemorySegment, int, class java/lang/foreign/MemorySegment, long, long, int, top, top, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, null, class java/lang/foreign/Arena ]
	          stack = [ class java/lang/foreign/MemorySegment, class jdk/internal/foreign/MemorySessionImpl ]
	        frame_type = 255 // full_frame
	          offset_delta = 0
	          locals = [ class java/lang/foreign/SegmentAllocator, class java/lang/foreign/MemorySegment, class java/lang/foreign/MemorySegment, int, class java/lang/foreign/MemorySegment, long, long, int, top, top, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, class java/lang/foreign/Arena ]
	          stack = [ class java/lang/foreign/MemorySegment ]
	        frame_type = 255 // full_frame
	          offset_delta = 44
	          locals = [ class java/lang/foreign/SegmentAllocator, class java/lang/foreign/MemorySegment, class java/lang/foreign/MemorySegment, int, class java/lang/foreign/MemorySegment, long, long, int, long, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, class java/lang/foreign/Arena, int ]
	          stack = [ int ]
	        frame_type = 73 // same_locals_1_stack_item
	          stack = [ int ]
	        frame_type = 73 // same_locals_1_stack_item
	          stack = [ int ]
	        frame_type = 255 // full_frame
	          offset_delta = 0
	          locals = [ class java/lang/foreign/SegmentAllocator, class java/lang/foreign/MemorySegment, class java/lang/foreign/MemorySegment, int, class java/lang/foreign/MemorySegment, top, top, top, top, top, top, top, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, class java/lang/foreign/Arena ]
	          stack = [ class java/lang/Throwable ]
	        frame_type = 83 // same_locals_1_stack_item
	          stack = [ class java/lang/Throwable ]
	        frame_type = 73 // same_locals_1_stack_item
	          stack = [ class java/lang/Throwable ]
	        frame_type = 73 // same_locals_1_stack_item
	          stack = [ class java/lang/Throwable ]
	}
	BootstrapMethods: //0号方法，计算stub函数的MethodHandle
	  0: #35 REF_invokeStatic java/lang/invoke/MethodHandles.classData:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
	    Method arguments:
	    
	// 反编译后的源代码为
	
	package jdk.internal.foreign.abi;
	
	import java.lang.foreign.Arena;
	import java.lang.foreign.MemorySegment;
	import java.lang.foreign.SegmentAllocator;
	import java.lang.invoke.MethodHandle;
	import jdk.internal.foreign.AbstractMemorySegmentImpl;
	import jdk.internal.foreign.MemorySessionImpl;
	
	public final class DowncallStub {
	
	    public static int invoke(SegmentAllocator segmentallocator, MemorySegment func_ptr, MemorySegment vm_buf, int bufLen, MemorySegment numVMs) {
	        MemorySessionImpl memorysessionimpl = null;
	        MemorySessionImpl memorysessionimpl1 = null;
	        MemorySessionImpl memorysessionimpl2 = null;
	        Arena arena = SharedUtils.DUMMY_ARENA;
	        int invoke_return_value;
	        try {
	            MemorySessionImpl memorysessionimpl3 = ((AbstractMemorySegmentImpl) func_ptr).sessionImpl();
	            memorysessionimpl3.acquire0();
	            memorysessionimpl = memorysessionimpl3;
	            long func_ptr_address = SharedUtils.unboxSegment(func_ptr);
	            memorysessionimpl3 = ((AbstractMemorySegmentImpl) vm_buf).sessionImpl();
	            if (memorysessionimpl3 != memorysessionimpl) {
	                memorysessionimpl3.acquire0();
	                memorysessionimpl1 = memorysessionimpl3;
	            }
	            long vm_buf_address = SharedUtils.unboxSegment(vm_buf);
	            memorysessionimpl3 = ((AbstractMemorySegmentImpl) numVMs).sessionImpl();
	            if (memorysessionimpl3 != memorysessionimpl && memorysessionimpl3 != memorysessionimpl1) {
	                memorysessionimpl3.acquire0();
	                memorysessionimpl2 = memorysessionimpl3;
	            }
	            long numVMs_address = SharedUtils.unboxSegment(numVMs); // 获取numVMs数组的内存地址
	            int stub_return_value = ((MethodHandle) classData("_")).invokeExact(func_ptr_address, vm_buf_address, bufLen, numVMs_address); // 调用stub函数
	            invoke_return_value = stub_return_value;
	        } catch (Throwable throwable) {
	            ((Arena) arena).close();
	            if (memorysessionimpl != null) {
	                memorysessionimpl.release0();
	            }
	            if (memorysessionimpl1 != null) {
	                memorysessionimpl1.release0();
	            }
	            if (memorysessionimpl2 != null) {
	                memorysessionimpl2.release0();
	            }
	            throw throwable;
	        }
	        ((Arena) arena).close();
	        if (memorysessionimpl != null) {
	            memorysessionimpl.release0();
	        }
	        if (memorysessionimpl1 != null) {
	            memorysessionimpl1.release0();
	        }
	        if (memorysessionimpl2 != null) {
	            memorysessionimpl2.release0();
	        }
	        return invoke_return_value;
	    }
	}
	*/
	// @formatter:on

	/**
	 * 为指定类型的函数创建stub函数并返回其指针。<br>
	 * JVM调用so库中的函数时，首先为目标函数类型创建一个能调用目标函数类型的C函数，即stub函数。stub函数比目标函数多了一个首参数，即要调用的目标函数指针的地址，类型为long，且此参数不占用寄存器而是储存在PLACEHOLDER内存上偏移量为0处。<br>
	 * stub函数中全部的C/C++指针类型均为long，Java类型则为对应的Class<?>。<br>
	 * stub函数创建完成以后，可以使用NativeEntryPoint.make()和NativeMethodHandle.make()创建stub函数的MethodHandle。<br>
	 * 但在FFM实现中这个Handle是隐藏类中定义的，不能直接访问，因为JVM的FFM API实现中要将指针地址绑定到MemorySegment，
	 * 故内部实现为jdk.internal.foreign.abi.BindingSpecializer。specializeHelper()在运行时生成字节码定义一个隐藏类，
	 * 而该隐藏类调用了NativeMethodHandle.make()生成的stub函数的句柄。<br>
	 * https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/prims/nativeEntryPoint.cpp#L37<br>
	 * 关于vm_storage_enc_arg_moves、vm_storage_enc_return_moves的处理参见 https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/cpu/x86/downcallLinker_x86_64.cpp#L39<br>
	 * 关于具体生成机器码的逻辑参见 https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/prims/downcallLinker.hpp#L64<br>
	 * https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/cpu/x86/downcallLinker_x86_64.cpp#L143<br>
	 * 
	 * @param method_type              JVM层stub方法的类型。如果是C++指针类型则传入long。第一个参数必须是long，代表函数指针地址，后面按顺序传入参数
	 * @param abi_descriptor           ABI描述符
	 * @param vm_storage_enc_arg_moves 传入参数的按顺序的保存位置，对应的Java层的Binding是VMStore
	 * @param vm_storage_enc_ret_moves 接收返回值的保存位置，对应的Java层的Binding是VMLoad
	 * @param needs_return_buffer      返回值是否需要缓冲区
	 * @param captured_state_mask
	 * @param needs_transition
	 * @return
	 */
	public static final long downcall_stub(MethodType method_type, Object abi_descriptor, Object[] vm_storage_enc_arg_moves, Object[] vm_storage_enc_ret_moves, boolean needs_return_buffer, int captured_state_mask, boolean needs_transition, boolean nusing_address_pairs)
	{
		return jdk_versions.switch_execute_nonnull(
				() ->
				{
					try
					{
						return (long) NativeEntryPoint_makeDowncallStub.invoke(method_type, abi_descriptor, vm_storage_enc_arg_moves, vm_storage_enc_ret_moves, needs_return_buffer, captured_state_mask, needs_transition);
					}
					catch (Throwable ex)
					{
						throw new java.lang.InternalError("get downcall stub of type '" + method_type.toString() + "' failed [jdk21]", ex);
					}
				}, // JDK21
				() ->
				{
					try
					{
						// 新增参数using_address_pairs
						return (long) NativeEntryPoint_makeDowncallStub.invoke(method_type, abi_descriptor, vm_storage_enc_arg_moves, vm_storage_enc_ret_moves, needs_return_buffer, captured_state_mask, needs_transition, nusing_address_pairs);
					}
					catch (Throwable ex)
					{
						throw new java.lang.InternalError("get downcall stub of type '" + method_type.toString() + "' failed [jdk25]", ex);
					}
				}// JDK25
		);
	}

	public static final boolean free_downcall_stub(long downcall_stub)
	{
		try
		{
			return (boolean) NativeEntryPoint_freeDowncallStub0.invokeExact(downcall_stub);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("free downcall stub '" + downcall_stub + "' failed", ex);
		}
	}

	/**
	 * 获取本函数签名类型的stub函数的MethodHandle
	 * 
	 * @return
	 */
	public static final MethodHandle stub_function(call_convention call_conv, cxx_type.function_type func_type)
	{
		return native_entry_handle(stub_native_entry(new nmh_stub_constraint(call_conv, func_type)));
	}

	public static final MethodHandle stub_function(call_convention call_conv, cxx_type.function_pointer_type func_ptr_type)
	{
		return stub_function(call_conv, func_ptr_type.pointed_to_type());
	}

	// @formatter:off
	/*
	 * JVM调用native代码见https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/cpu/x86/methodHandles_x86.cpp#L247
	 * void MethodHandles::jump_to_native_invoker(MacroAssembler* _masm, Register nep_reg, Register temp_target) {
	 *   BLOCK_COMMENT("jump_to_native_invoker {");
	 *     assert_different_registers(nep_reg, temp_target);
	 *     assert(nep_reg != noreg, "required register");
	 *     // Load the invoker, as NEP -> .invoker
	 *     __ verify_oop(nep_reg);
	 *     __ access_load_at(T_ADDRESS, IN_HEAP, temp_target, Address(nep_reg, NONZERO(jdk_internal_foreign_abi_NativeEntryPoint::downcall_stub_address_offset_in_bytes())), noreg);
	 *     __ jmp(temp_target);
	 *   BLOCK_COMMENT("} jump_to_native_invoker");
	 * }
	 * 即直接让控制流跳转到NativeEntryPoint.downcallStubAddress字段
	 * 在generate_method_handle_dispatch()方法中（https://github.com/openjdk/jdk/blob/76a44b3e0341a9c59eaff0bfe8884ad104bda8d5/src/hotspot/cpu/x86/methodHandles_x86.cpp#L359）
	 * 将根据MethodHandle的vmIntrinsicsID决定如何调用MethodHandle，如下
	 * if (iid == vmIntrinsics::_invokeBasic) {
	 *   // indirect through MH.form.vmentry.vmtarget
	 *   jump_to_lambda_form(_masm, receiver_reg, rbx_method, temp1, for_compiler_entry);
	 * } else if (iid == vmIntrinsics::_linkToNative) {
	 *   assert(for_compiler_entry, "only compiler entry is supported");
	 *   jump_to_native_invoker(_masm, member_reg, temp1);
	 * } else ...
	 * 
	 */
	// @formatter:on
	/**
	 * 构建一个指定机器码地址的NativeEntryPoint。<br>
	 * method_type实际仅在NativeMethodHandle.make()中使用以生成可调用的MethodHandle。<br>
	 * 需要包装为NativeMethodHandle才能调用。<br>
	 * 
	 * @param method_type 函数类型
	 * @param mc_address  跳转目标点地址
	 * @return
	 */
	public static final Object native_entry(MethodType method_type, long mc_address)
	{
		try
		{
			return NativeEntryPoint_ctor.invoke(method_type, mc_address);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("wrap native entry pointer of address '" + mc_address + "' failed", ex);
		}
	}

	public static final MethodType native_entry_method_type(Object nep)
	{
		try
		{
			return (MethodType) NativeEntryPoint_methodType.get(nep);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get method type of native entry '" + nep + "' failed", ex);
		}
	}

	/**
	 * 为指定类型的函数创建入口点stub并缓存，stub函数中包含参数的传递和返回。<br>
	 * 
	 * @param method_type          JVM层stub方法的类型。如果是C++指针类型则传入long。第一个参数必须是long，代表函数指针地址，后面按顺序传入参数
	 * @param abi_descriptor       ABI描述符
	 * @param vm_storage_arg_moves 传入参数的保存位置
	 * @param vm_storage_ret_moves 接收返回值的保存位置
	 * @param needs_return_buffer  返回值是否需要缓冲区
	 * @param captured_state_mask
	 * @param needs_transition
	 * @param using_address_pairs  JDK25+新增参数，如果当前低于该版本则忽略此参数
	 * @return
	 */
	public static final Object stub_native_entry(MethodType method_type, Object abi_descriptor, Object[] vm_storage_arg_moves, Object[] vm_storage_ret_moves, boolean needs_return_buffer, int captured_state_mask, boolean needs_transition, boolean using_address_pairs)
	{
		return jdk_versions.switch_execute_nonnull(
				() ->
				{
					try
					{
						return NativeEntryPoint_make.invoke(abi_descriptor, vm_storage_arg_moves, vm_storage_ret_moves, method_type, needs_return_buffer, captured_state_mask, needs_transition);
					}
					catch (Throwable ex)
					{
						throw new java.lang.InternalError("wrap native entry point of type '" + method_type.toString() + "' failed [jdk21]", ex);
					}
				}, // JDK21
				() ->
				{
					try
					{
						// 新增参数using_address_pairs
						return NativeEntryPoint_make.invoke(abi_descriptor, vm_storage_arg_moves, vm_storage_ret_moves, method_type, needs_return_buffer, captured_state_mask, needs_transition, using_address_pairs);
					}
					catch (Throwable ex)
					{
						throw new java.lang.InternalError("wrap native entry point of type '" + method_type.toString() + "' failed [jdk25]", ex);
					}
				}// JDK25
		);
	}

	public static final Object stub_native_entry(MethodType method_type, Object abi_descriptor, Object[] vm_storage_arg_moves, Object[] vm_storage_ret_moves, boolean needs_return_buffer, int captured_state_mask, boolean needs_transition)
	{
		return stub_native_entry(method_type, abi_descriptor, vm_storage_arg_moves, vm_storage_ret_moves, needs_return_buffer, captured_state_mask, needs_transition, false);
	}

	/**
	 * stub函数的NativeEntryPoint
	 * 
	 * @param arch
	 * @param abi
	 * @param resolved_constraint
	 * @return
	 */
	public static final Object stub_native_entry(nmh_stub_constraint resolved_constraint)
	{
		return stub_native_entry(resolved_constraint.stub_method_type(),
				resolved_constraint.call_conv().abi_descriptor,
				resolved_constraint.arg_vm_storage(),
				resolved_constraint.ret_vm_storage(),
				resolved_constraint.needs_return_buffer(),
				resolved_constraint.captured_state_mask(),
				resolved_constraint.needs_transition());
	}

	/**
	 * NativeEntryPoint包装为NativeMethodHandle
	 */

	private static Class<?> java_lang_invoke_NativeMethodHandle;
	private static Class<?> java_lang_invoke_LambdaForm;

	private static MethodHandle NativeMethodHandle_ctor;
	private static MethodHandle NativeMethodHandle_preparedLambdaForm;

	static
	{
		try
		{
			java_lang_invoke_NativeMethodHandle = Class.forName("java.lang.invoke.NativeMethodHandle");
			java_lang_invoke_LambdaForm = Class.forName("java.lang.invoke.LambdaForm");
			NativeMethodHandle_ctor = symbols.find_constructor(java_lang_invoke_NativeMethodHandle, MethodType.class, java_lang_invoke_LambdaForm, jdk_internal_foreign_abi_NativeEntryPoint);
			NativeMethodHandle_preparedLambdaForm = symbols.find_static_method(java_lang_invoke_NativeMethodHandle, "preparedLambdaForm", java_lang_invoke_LambdaForm, MethodType.class);
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	public static final MethodHandle native_entry_handle(Object nep)
	{
		MethodType mt = native_entry_method_type(nep);
		try
		{
			// preparedLambdaForm用于生成MethodHandle的调用字节码
			return (MethodHandle) NativeMethodHandle_ctor.invoke(mt, NativeMethodHandle_preparedLambdaForm.invoke(mt), nep);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("wrap native method handle of '" + nep + "' with type '" + mt + "' failed", ex);
		}
	}

	/**
	 * 将指定地址解析为函数指针
	 * 
	 * @param fun_addr
	 * @param signature
	 * @return
	 */
	public static final MethodHandle func(long fun_addr, call_convention call_conv, function_type func_type)
	{
		return symbols.bind(abi.stub_function(call_conv, func_type), 0, fun_addr);// 绑定首参数，即stub函数要执行的目标函数指针
	}

	public static final MethodHandle func(long fun_addr, function_type func_type)
	{
		return func(fun_addr, call_convention.host, func_type);
	}
}
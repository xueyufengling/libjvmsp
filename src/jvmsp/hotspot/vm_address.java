package jvmsp.hotspot;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jvmsp.memory;
import jvmsp.shared_object;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.function_signature;
import jvmsp.type.cxx_type.object;
import jvmsp.type.cxx_type.pointer;
import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct.entry;
import jvmsp.libso.libjvm;

/**
 * Hotspot JVM暴露的函数地址
 */
@SuppressWarnings("unused")
public class vm_address
{
	public static final cxx_type VMAddressEntry = cxx_type.define("VMAddressEntry")
			.decl_field("name", cxx_type.pchar)
			.decl_field("value", cxx_type.pvoid)
			.resolve();

	public static class entry
	{
		// VMTypes信息的起始地址
		private static final long jvmciHotSpotVMAddresses;

		static
		{
			jvmciHotSpotVMAddresses = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "jvmciHotSpotVMAddresses"));
		}

		public final String name;// 地址的名称
		public final long value;// 地址的值

		private entry(long entry_addr)
		{
			object entry = VMAddressEntry.new object(entry_addr);
			this.name = memory.string((long) entry.read("name"));
			this.value = (long) entry.read("value");
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder().append("VMAddressEntry [")
					.append("name = ").append(name)
					.append(", value = ").append(value)
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
				return value == other.value &&
						Objects.equals(name, other.name);
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
					name,
					value);
		}

		private static final Map<String, entry> address_entries = new HashMap<>();

		private static final void collect_entries(Map<String, entry> address_entries, long vm_addresses)
		{
			for (int idx = 0;; ++idx)
			{
				entry entry = new entry(vm_addresses + idx * VMAddressEntry.size());
				if (entry.name == null)
				{
					break;
				}
				entry existed = address_entries.get(entry.name);
				if (existed != null && !entry.equals(existed))
				{
					throw new java.lang.InternalError("conflict VMAddressEntry '" + entry + "' and '" + existed + "'");
				}
				else
				{
					address_entries.put(entry.name, entry);
				}
			}
		}

		static
		{
			collect_entries(address_entries, jvmciHotSpotVMAddresses);
		}

		public static final entry get(String addr_name)
		{
			return address_entries.get(addr_name);
		}
	}

	public static final long get(String addr_name)
	{
		return entry.get(addr_name).value;
	}

	public static class SharedRuntime
	{
		private static final long SharedRuntime_register_finalizer = vm_address.get("SharedRuntime::register_finalizer");
		private static final long SharedRuntime_exception_handler_for_return_address = vm_address.get("SharedRuntime::exception_handler_for_return_address");
		private static final long SharedRuntime_OSR_migration_end = vm_address.get("SharedRuntime::OSR_migration_end");
		private static final long SharedRuntime_enable_stack_reserved_zone = vm_address.get("SharedRuntime::enable_stack_reserved_zone");
		private static final long SharedRuntime_frem = vm_address.get("SharedRuntime::frem");
		private static final long SharedRuntime_drem = vm_address.get("SharedRuntime::drem");
		private static final long SharedRuntime_notify_jvmti_vthread_start = vm_address.get("SharedRuntime::notify_jvmti_vthread_start");
		private static final long SharedRuntime_notify_jvmti_vthread_end = vm_address.get("SharedRuntime::notify_jvmti_vthread_end");
		private static final long SharedRuntime_notify_jvmti_vthread_mount = vm_address.get("SharedRuntime::notify_jvmti_vthread_mount");
		private static final long SharedRuntime_notify_jvmti_vthread_unmount = vm_address.get("SharedRuntime::notify_jvmti_vthread_unmount");
	}

	public static class os
	{
		private static final long os_dll_load = vm_address.get("os::dll_load");
		private static final long os_dll_lookup = vm_address.get("os::dll_lookup");
		private static final long os_javaTimeMillis = vm_address.get("os::javaTimeMillis");
		private static final long os_javaTimeNanos = vm_address.get("os::javaTimeNanos");
	}

	public static class Deoptimization
	{
		private static final long Deoptimization_fetch_unroll_info = vm_address.get("Deoptimization::fetch_unroll_info");
		private static final long Deoptimization_uncommon_trap = vm_address.get("Deoptimization::uncommon_trap");
		private static final long Deoptimization_unpack_frames = vm_address.get("Deoptimization::unpack_frames");
	}

	public static class JVMCIRuntime
	{
		private static final long JVMCIRuntime_new_instance = vm_address.get("JVMCIRuntime::new_instance");
		private static final long JVMCIRuntime_new_array = vm_address.get("JVMCIRuntime::new_array");
		private static final long JVMCIRuntime_new_multi_array = vm_address.get("JVMCIRuntime::new_multi_array");
		private static final long JVMCIRuntime_dynamic_new_array = vm_address.get("JVMCIRuntime::dynamic_new_array");
		private static final long JVMCIRuntime_dynamic_new_instance = vm_address.get("JVMCIRuntime::dynamic_new_instance");
		private static final long JVMCIRuntime_new_instance_or_null = vm_address.get("JVMCIRuntime::new_instance_or_null");
		private static final long JVMCIRuntime_new_array_or_null = vm_address.get("JVMCIRuntime::new_array_or_null");
		private static final long JVMCIRuntime_new_multi_array_or_null = vm_address.get("JVMCIRuntime::new_multi_array_or_null");
		private static final long JVMCIRuntime_dynamic_new_array_or_null = vm_address.get("JVMCIRuntime::dynamic_new_array_or_null");
		private static final long JVMCIRuntime_dynamic_new_instance_or_null = vm_address.get("JVMCIRuntime::dynamic_new_instance_or_null");
		private static final long JVMCIRuntime_invoke_static_method_one_arg = vm_address.get("JVMCIRuntime::invoke_static_method_one_arg");
		private static final long JVMCIRuntime_vm_message = vm_address.get("JVMCIRuntime::vm_message");
		private static final long JVMCIRuntime_identity_hash_code = vm_address.get("JVMCIRuntime::identity_hash_code");
		private static final long JVMCIRuntime_exception_handler_for_pc = vm_address.get("JVMCIRuntime::exception_handler_for_pc");
		private static final long JVMCIRuntime_monitorenter = vm_address.get("JVMCIRuntime::monitorenter");
		private static final long JVMCIRuntime_monitorexit = vm_address.get("JVMCIRuntime::monitorexit");
		private static final long JVMCIRuntime_object_notify = vm_address.get("JVMCIRuntime::object_notify");
		private static final long JVMCIRuntime_object_notifyAll = vm_address.get("JVMCIRuntime::object_notifyAll");
		private static final long JVMCIRuntime_throw_and_post_jvmti_exception = vm_address.get("JVMCIRuntime::throw_and_post_jvmti_exception");
		private static final long JVMCIRuntime_throw_klass_external_name_exception = vm_address.get("JVMCIRuntime::throw_klass_external_name_exception");
		private static final long JVMCIRuntime_throw_class_cast_exception = vm_address.get("JVMCIRuntime::throw_class_cast_exception");
		private static final long JVMCIRuntime_log_primitive = vm_address.get("JVMCIRuntime::log_primitive");
		private static final long JVMCIRuntime_log_object = vm_address.get("JVMCIRuntime::log_object");
		private static final long JVMCIRuntime_log_printf = vm_address.get("JVMCIRuntime::log_printf");
		private static final long JVMCIRuntime_vm_error = vm_address.get("JVMCIRuntime::vm_error");
		private static final long JVMCIRuntime_load_and_clear_exception = vm_address.get("JVMCIRuntime::load_and_clear_exception");
		private static final long JVMCIRuntime_write_barrier_pre = vm_address.get("JVMCIRuntime::write_barrier_pre");
		private static final long JVMCIRuntime_write_barrier_post = vm_address.get("JVMCIRuntime::write_barrier_post");
		private static final long JVMCIRuntime_validate_object = vm_address.get("JVMCIRuntime::validate_object");
		private static final long JVMCIRuntime_test_deoptimize_call_int = vm_address.get("JVMCIRuntime::test_deoptimize_call_int");

		private static final MethodHandle vm_message;

		static
		{
			vm_message = shared_object.func(JVMCIRuntime_vm_message, function_signature.of("vm_message", cxx_type._void, cxx_type.jboolean, cxx_type.jlong, cxx_type.jlong, cxx_type.jlong, cxx_type.jlong));
		}

		public static final void vm_message(boolean vmError, long format_cstr, long v1, long v2, long v3)
		{
			try
			{
				vm_message.invokeExact(vmError ? 1 : 0, format_cstr, v1, v2, v3);
			}
			catch (Throwable ex)
			{
				throw new java.lang.InternalError("call vm_message() failed", ex);
			}
		}

		public static final void vm_message(boolean vmError, String format, long addr, long v2, long v3)
		{
			try (pointer format_str = memory.c_str(format).auto())
			{
				vm_message(vmError, format_str.address(), addr, v2, v3);
			}
			catch (Throwable ex)
			{
				throw new java.lang.InternalError("call vm_message() failed", ex);
			}
		}
	}
}

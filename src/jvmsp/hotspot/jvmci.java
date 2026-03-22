package jvmsp.hotspot;

import java.lang.invoke.MethodHandle;

import jvmsp.memory;
import jvmsp.shared_object;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.function_signature;
import jvmsp.type.cxx_type.pointer;

public class jvmci
{
	private static final MethodHandle vm_message;

	static
	{
		vm_message = shared_object.func(vm_address.get("JVMCIRuntime::vm_message"), function_signature.of("vm_message", cxx_type._void, cxx_type.jboolean, cxx_type.jlong, cxx_type.jlong, cxx_type.jlong, cxx_type.jlong));
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

	public static final void vm_message(boolean vmError, String format, long v1, long v2, long v3)
	{
		try (pointer format_str = memory.c_str(format).auto())
		{
			vm_message(vmError, format_str.address(), v1, v2, v3);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call vm_message() failed", ex);
		}
	}
}

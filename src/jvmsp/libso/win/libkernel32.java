package jvmsp.libso.win;

import java.lang.invoke.MethodHandle;

import jvmsp.shared_object;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.function_signature;

public class libkernel32
{
	private static final long _libkernel32;

	private static final MethodHandle VirtualAlloc;
	private static final MethodHandle VirtualFree;

	static
	{
		_libkernel32 = shared_object.dlopen("kernel32");
		// 查找库函数
		VirtualAlloc = shared_object.dlsym(_libkernel32, function_signature.of("VirtualAlloc", cxx_type.pvoid, cxx_type.pvoid, cxx_type.size_t, cxx_type._long_long, cxx_type._long_long));
		VirtualFree = shared_object.dlsym(_libkernel32, function_signature.of("VirtualFree", cxx_type._int, cxx_type.pvoid, cxx_type.size_t, cxx_type._long_long));
	}

	public static final long VirtualAlloc(long lpAddress, long dwSize, long flAllocationType, long flProtect)
	{
		try
		{
			return (long) VirtualAlloc.invokeExact(lpAddress, dwSize, flAllocationType, flProtect);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call VirtualAlloc() failed", ex);
		}
	}

	public static final int VirtualFree(long lpAddress, long dwSize, long dwFreeType)
	{
		try
		{
			return (int) VirtualFree.invokeExact(lpAddress, dwSize, dwFreeType);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call VirtualFree() failed", ex);
		}
	}
}

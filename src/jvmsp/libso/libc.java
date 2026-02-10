package jvmsp.libso;

import java.lang.invoke.MethodHandle;

import jvmsp.shared_object;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.function_signature;
import jvmsp.virtual_machine;

/**
 * libc.so库相关函数封装
 */
public class libc
{
	private static final long _libc;

	private static final MethodHandle strlen;
	private static final MethodHandle strcpy;
	private static final MethodHandle strncpy;
	private static final MethodHandle strcmp;
	private static final MethodHandle strcat;
	private static final MethodHandle strchr;
	private static final MethodHandle strstr;

	private static final MethodHandle malloc;
	private static final MethodHandle free;
	private static final MethodHandle calloc;
	private static final MethodHandle realloc;
	private static final MethodHandle memcpy;
	private static final MethodHandle memset;
	private static final MethodHandle memcmp;

	static
	{
		String libc_filename = null;
		switch (virtual_machine.platform.host)
		{
		case virtual_machine.platform.linux:
			libc_filename = "c";
			break;
		case virtual_machine.platform.macos:
			libc_filename = "System";
			break;
		case virtual_machine.platform.windows:
			libc_filename = "msvcrt";
			break;
		}
		_libc = shared_object.dlopen(libc_filename);
		// 查找库函数
		strlen = shared_object.dlsym(_libc, function_signature.of("strlen", cxx_type.size_t, cxx_type.pchar));
		strcpy = shared_object.dlsym(_libc, function_signature.of("strcpy", cxx_type.pchar, cxx_type.pchar, cxx_type.pchar));
		strncpy = shared_object.dlsym(_libc, function_signature.of("strncpy", cxx_type.pchar, cxx_type.pchar, cxx_type.pchar, cxx_type.size_t));
		strcmp = shared_object.dlsym(_libc, function_signature.of("strcmp", cxx_type._int, cxx_type.pchar, cxx_type.pchar));
		strcat = shared_object.dlsym(_libc, function_signature.of("strcat", cxx_type.pchar, cxx_type.pchar, cxx_type.pchar));
		strchr = shared_object.dlsym(_libc, function_signature.of("strchr", cxx_type.pchar, cxx_type.pchar, cxx_type._int));
		strstr = shared_object.dlsym(_libc, function_signature.of("strstr", cxx_type.pchar, cxx_type.pchar, cxx_type.pchar));

		malloc = shared_object.dlsym(_libc, function_signature.of("malloc", cxx_type.pvoid, cxx_type.size_t));
		free = shared_object.dlsym(_libc, function_signature.of("free", cxx_type._void, cxx_type.pvoid));
		calloc = shared_object.dlsym(_libc, function_signature.of("calloc", cxx_type.pvoid, cxx_type.size_t, cxx_type.size_t));
		realloc = shared_object.dlsym(_libc, function_signature.of("realloc", cxx_type.pvoid, cxx_type.pvoid, cxx_type.size_t));
		memcpy = shared_object.dlsym(_libc, function_signature.of("memcpy", cxx_type.pvoid, cxx_type.pvoid, cxx_type.pvoid, cxx_type.size_t));
		memset = shared_object.dlsym(_libc, function_signature.of("memset", cxx_type.pvoid, cxx_type.pvoid, cxx_type._int, cxx_type.size_t));
		memcmp = shared_object.dlsym(_libc, function_signature.of("memcmp", cxx_type._int, cxx_type.pvoid, cxx_type.pvoid, cxx_type.size_t));
	}

	public static final long strlen(long cstr)
	{
		try
		{
			return (long) strlen.invokeExact(cstr);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call strlen() failed", ex);
		}
	}

	public static final long strcpy(long cstr_dest, long cstr_src)
	{
		try
		{
			return (long) strcpy.invokeExact(cstr_dest, cstr_src);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call strcpy() failed", ex);
		}
	}

	public static final long strncpy(long cstr_dest, long cstr_src, long n)
	{
		try
		{
			return (long) strncpy.invokeExact(cstr_dest, cstr_src, n);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call strncpy() failed", ex);
		}
	}

	public static final int strcmp(long cstr1, long cstr2)
	{
		try
		{
			return (int) strcmp.invokeExact(cstr1, cstr2);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call strcmp() failed", ex);
		}
	}

	public static final long strcat(long cstr_dest, long cstr_src)
	{
		try
		{
			return (long) strcat.invokeExact(cstr_dest, cstr_src);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call strcat() failed", ex);
		}
	}

	public static final long strchr(long cstr, int ch)
	{
		try
		{
			return (long) strchr.invokeExact(cstr, ch);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call strchr() failed", ex);
		}
	}

	public static final long strstr(long cstr_haystack, long cstr_needle)
	{
		try
		{
			return (long) strstr.invokeExact(cstr_haystack, cstr_needle);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call strstr() failed", ex);
		}
	}

	public static final long malloc(long size)
	{
		try
		{
			return (long) malloc.invokeExact(size);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call malloc() failed", ex);
		}
	}

	public static final void free(long ptr)
	{
		try
		{
			free.invokeExact(ptr);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call free() failed", ex);
		}
	}

	public static final long calloc(long nmemb, long size)
	{
		try
		{
			return (long) calloc.invokeExact(nmemb, size);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call calloc() failed", ex);
		}
	}

	public static final long realloc(long ptr, long size)
	{
		try
		{
			return (long) realloc.invokeExact(ptr, size);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call realloc() failed", ex);
		}
	}

	public static final long memcpy(long dest, long src, long n)
	{
		try
		{
			return (long) memcpy.invokeExact(dest, src, n);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call memcpy() failed", ex);
		}
	}

	public static final long memset(long ptr, int value, long n)
	{
		try
		{
			return (long) memset.invokeExact(ptr, value, n);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call memset() failed", ex);
		}
	}

	public static final int memcmp(long ptr1, long ptr2, long n)
	{
		try
		{
			return (int) memcmp.invokeExact(ptr1, ptr2, n);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call memcmp() failed", ex);
		}
	}
}

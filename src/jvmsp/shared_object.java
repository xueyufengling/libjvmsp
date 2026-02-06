package jvmsp;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

/**
 * 加载so库以及符号查找
 */
public class shared_object
{
	private static Class<?> jdk_internal_loader_NativeLibrary;
	private static MethodHandle NativeLibrary_findEntry0;

	private static Class<?> jdk_internal_loader_NativeLibraries;
	private static Class<?> jdk_internal_loader_NativeLibraries$NativeLibraryImpl;

	private static MethodHandle NativeLibraries_load;
	private static MethodHandle NativeLibraries_unload;

	private static VarHandle NativeLibraryImpl_handle;
	private static Field NativeLibraries_libraries;

	private static Class<?> jdk_internal_loader_RawNativeLibraries;
	private static Class<?> jdk_internal_loader_RawNativeLibraries$RawNativeLibraryImpl;

	private static MethodHandle RawNativeLibraries_load0;
	private static MethodHandle RawNativeLibraries_unload0;

	private static VarHandle RawNativeLibraryImpl_handle;
	private static Field RawNativeLibraries_libraries;

	static
	{
		jdk_internal_loader_NativeLibrary = reflection.find_class("jdk.internal.loader.NativeLibrary");
		NativeLibrary_findEntry0 = symbols.find_static_method(jdk_internal_loader_NativeLibrary, "findEntry0", long.class, long.class, String.class);
		// 加载JNI模块
		jdk_internal_loader_NativeLibraries = reflection.find_class("jdk.internal.loader.NativeLibraries");
		jdk_internal_loader_NativeLibraries$NativeLibraryImpl = reflection.find_class("jdk.internal.loader.NativeLibraries$NativeLibraryImpl");
		NativeLibraries_load = symbols.find_static_method(jdk_internal_loader_NativeLibraries, "load", boolean.class, jdk_internal_loader_NativeLibraries$NativeLibraryImpl, String.class, boolean.class, boolean.class);
		NativeLibraries_unload = symbols.find_static_method(jdk_internal_loader_NativeLibraries, "unload", void.class, String.class, boolean.class, long.class);
		NativeLibraries_libraries = reflection.find_declared_field(jdk_internal_loader_NativeLibraries, "libraries");
		NativeLibraryImpl_handle = symbols.find_var(jdk_internal_loader_NativeLibraries$NativeLibraryImpl, "handle", long.class);
		// 加载通用模块
		jdk_internal_loader_RawNativeLibraries = reflection.find_class("jdk.internal.loader.RawNativeLibraries");
		jdk_internal_loader_RawNativeLibraries$RawNativeLibraryImpl = reflection.find_class("jdk.internal.loader.RawNativeLibraries$RawNativeLibraryImpl");
		RawNativeLibraries_load0 = symbols.find_static_method(jdk_internal_loader_RawNativeLibraries, "load0", boolean.class, jdk_internal_loader_RawNativeLibraries$RawNativeLibraryImpl, String.class);
		RawNativeLibraries_unload0 = symbols.find_static_method(jdk_internal_loader_RawNativeLibraries, "unload0", void.class, String.class, long.class);
		RawNativeLibraries_libraries = reflection.find_declared_field(jdk_internal_loader_RawNativeLibraries, "libraries");
		RawNativeLibraryImpl_handle = symbols.find_var(jdk_internal_loader_RawNativeLibraries$RawNativeLibraryImpl, "handle", long.class);
	}

	/**
	 * 获取NativeLibraries中储存的具体的库名称-句柄Map
	 * 
	 * @param native_libraries
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final Map<String, Object> get_native_libraries_map(Object native_libraries)
	{
		return (Map<String, Object>) unsafe.read_reference(native_libraries, NativeLibraries_libraries);
	}

	public static final void set_native_libraries_map(Object native_libraries, Map<String, Object> new_libraries)
	{
		unsafe.write(native_libraries, NativeLibraries_libraries, new_libraries);
	}

	@SuppressWarnings("unchecked")
	public static final Set<Object> get_raw_native_libraries_map(Object raw_native_libraries)
	{
		return (Set<Object>) unsafe.read_reference(raw_native_libraries, RawNativeLibraries_libraries);
	}

	public static final void set_native_libraries_map(Object raw_native_libraries, Set<Object> new_libraries)
	{
		unsafe.write(raw_native_libraries, RawNativeLibraries_libraries, raw_native_libraries);
	}

	/**
	 * Java内部方法加载具有JNI_OnLoad的JNI模块so库 https://github.com/openjdk/jdk/blob/14a6e928ce9a10f6d85fae8db4ce303da20bde85/src/java.base/share/native/libjava/NativeLibraries.c#L119 注：若抛出错误 java.lang.UnsatisfiedLinkError: unsupported JNI version 0x00010001 required by xxx 则说明目标的JNI版本过低，至少要JNI_VERSION_1_8； 或者未查找到符号（未找到符号默认JNI版本为JNI_VERSION_1_1，即0x00010001）
	 * 
	 * @param native_library_impl     必须是NativeLibraryImpl对象
	 * @param name                    库名称，不可传入空指针，否则报错
	 * @param is_builtin              库是否是内建库。如果是则查找JNI进程的JNI_OnLoad_<name>函数，如果找到则直接返回JNI进程句柄，未找到则抛出 unsupported JNI version 0x00010001异常；如果不是内建库则直接加载name对应的so库
	 * @param throw_exception_if_fail 加载失败时是否抛出错误
	 * @return
	 */
	public static final boolean __dlopen_jni(Object native_library_impl, String name, boolean is_builtin, boolean throw_exception_if_fail)
	{
		boolean loaded = false;
		try
		{
			loaded = (boolean) NativeLibraries_load.invoke(native_library_impl, name, is_builtin, false);
		}
		catch (Throwable ex)
		{
			loaded = false;

		}
		if (!loaded && throw_exception_if_fail)
			throw new java.lang.UnsatisfiedLinkError("load " + (is_builtin ? "builtin" : "") + " jni shared object '" + name + "' failed");
		else
			return loaded;
	}

	public static final long dlopen_jni(String name, boolean is_builtin, boolean throw_exception_if_fail)
	{
		Object native_library_impl = unsafe.allocate(jdk_internal_loader_NativeLibraries$NativeLibraryImpl);// 构造一个空NativeLibraryImpl对象用于接收加载的so库的handle
		__dlopen_jni(native_library_impl, name, is_builtin, throw_exception_if_fail);
		return (long) NativeLibraryImpl_handle.get(native_library_impl);
	}

	/**
	 * 加载name指定的so库
	 * 
	 * @param name
	 * @param throw_exception_if_fail
	 * @return
	 */
	public static final long dlopen_jni(String name, boolean throw_exception_if_fail)
	{
		return dlopen_jni(name, false, throw_exception_if_fail);
	}

	public static final long dlopen_jni(String name)
	{
		return dlopen_jni(name, false, true);
	}

	public static final boolean dlclose_jni(String name, boolean is_builtin, long handle)
	{
		try
		{
			NativeLibraries_unload.invoke(name, is_builtin, handle);
			return true;
		}
		catch (Throwable ex)
		{
			return false;
		}
	}

	public static final boolean dlclose_jni(long handle)
	{
		return dlclose_jni(null, false, handle);
	}

	/**
	 * Java内部方法加载通用的so库
	 * 
	 * @param raw_native_library_impl
	 * @param name
	 * @param is_builtin
	 * @param throw_exception_if_fail
	 * @return
	 */
	public static final boolean __dlopen(Object raw_native_library_impl, String name, boolean throw_exception_if_fail)
	{
		boolean loaded = false;
		try
		{
			loaded = (boolean) RawNativeLibraries_load0.invoke(raw_native_library_impl, name);
		}
		catch (Throwable ex)
		{
			loaded = false;
		}
		if (!loaded && throw_exception_if_fail)
			throw new java.lang.UnsatisfiedLinkError("load shared object '" + name + "' failed");
		else
			return loaded;
	}

	/**
	 * 加载name指定的so库
	 * 
	 * @param name
	 * @param throw_exception_if_fail
	 * @return
	 */
	public static final long dlopen(String name, boolean throw_exception_if_fail)
	{
		Object raw_native_library_impl = unsafe.allocate(jdk_internal_loader_RawNativeLibraries$RawNativeLibraryImpl);
		__dlopen(raw_native_library_impl, name, throw_exception_if_fail);
		return (long) RawNativeLibraryImpl_handle.get(raw_native_library_impl);
	}

	public static final long dlopen(String name)
	{
		return dlopen(name, false);
	}

	public static final boolean dlclose(String name, long handle)
	{
		try
		{
			RawNativeLibraries_unload0.invoke(name, handle);
			return true;
		}
		catch (Throwable ex)
		{
			return false;
		}
	}

	public static final void dlclose(long handle)
	{
		dlclose("", handle);// name直接传入null则不会执行任何操作，但name值实际上也没有用到，因此传入任意字符串即可
	}

	/**
	 * 查找so库中的符号地址
	 * 
	 * @param handle
	 * @param symbol_name
	 * @return
	 */
	public static final long dlsym(long handle, String symbol_name)
	{
		try
		{
			return (long) NativeLibrary_findEntry0.invoke(handle, symbol_name);
		}
		catch (Throwable ex)
		{
			return 0;
		}
	}
}

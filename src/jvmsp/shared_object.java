package jvmsp;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jvmsp.type.cxx_type;
import jvmsp.type.java_type;

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

	/**
	 * native调用相关
	 */

	private static Class<?> jdk_internal_foreign_abi_NativeEntryPoint;
	private static Class<?> java_lang_invoke_NativeMethodHandle;

	private static Class<?> jdk_internal_foreign_abi_LinkerOptions;

	private static Class<?> jdk_internal_foreign_abi_ABIDescriptor;
	private static Class<?> jdk_internal_foreign_abi_VMStorage;

	private static MethodHandle NativeEntryPoint_makeDowncallStub;
	private static MethodHandle NativeEntryPoint_freeDowncallStub0;
	private static MethodHandle NativeEntryPoint_make;
	private static MethodHandle NativeMethodHandle_make;

	static
	{
		try
		{
			jdk_internal_foreign_abi_NativeEntryPoint = Class.forName("jdk.internal.foreign.abi.NativeEntryPoint");
			java_lang_invoke_NativeMethodHandle = Class.forName("java.lang.invoke.NativeMethodHandle");
			jdk_internal_foreign_abi_ABIDescriptor = Class.forName("jdk.internal.foreign.abi.ABIDescriptor");
			jdk_internal_foreign_abi_VMStorage = Class.forName("jdk.internal.foreign.abi.VMStorage");
			jdk_internal_foreign_abi_LinkerOptions = Class.forName("jdk.internal.foreign.abi.LinkerOptions");
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		NativeEntryPoint_makeDowncallStub = symbols.find_static_method(jdk_internal_foreign_abi_NativeEntryPoint, "makeDowncallStub", long.class,
				MethodType.class, jdk_internal_foreign_abi_ABIDescriptor, jdk_internal_foreign_abi_VMStorage.arrayType(), jdk_internal_foreign_abi_VMStorage.arrayType(), boolean.class, int.class, boolean.class);
		NativeEntryPoint_freeDowncallStub0 = symbols.find_static_method(jdk_internal_foreign_abi_NativeEntryPoint, "freeDowncallStub0", boolean.class, long.class);
		NativeEntryPoint_make = symbols.find_static_method(jdk_internal_foreign_abi_NativeEntryPoint, "make", jdk_internal_foreign_abi_NativeEntryPoint,
				jdk_internal_foreign_abi_ABIDescriptor, jdk_internal_foreign_abi_VMStorage.arrayType(), jdk_internal_foreign_abi_VMStorage.arrayType(), MethodType.class, boolean.class, int.class, boolean.class);
		NativeMethodHandle_make = symbols.find_static_method(java_lang_invoke_NativeMethodHandle, "make", MethodHandle.class, jdk_internal_foreign_abi_NativeEntryPoint);
	}

	/**
	 * 为指定类型的函数创建stub函数并返回其指针。<br>
	 * JVM调用so库中的函数时，首先为目标函数类型创建一个能调用目标函数类型的C函数，即stub函数。stub函数比目标函数多了一个首参数，即要调用的目标函数指针的地址，类型为long。<br>
	 * stub函数中全部的C/C++指针类型均为long，Java类型则为对应的Class<?>。<br>
	 * stub函数创建完成以后，可以使用NativeEntryPoint.make()和NativeMethodHandle.make()创建stub函数的MethodHandle。<br>
	 * 但在FFM实现中这个Handle是隐藏类中定义的，不能直接访问，因为JVM的FFM API实现中要将指针地址绑定到MemorySegment，
	 * 故内部实现为jdk.internal.foreign.abi.BindingSpecializer。specializeHelper()在运行时生成字节码定义一个隐藏类，
	 * 而该隐藏类调用了NativeMethodHandle.make()生成的stub函数的句柄。<br>
	 * 
	 * @param method_type             JVM层stub方法的类型。如果是C++指针类型则传入long。第一个参数必须是long，代表函数指针地址，后面按顺序传入参数
	 * @param abi_descriptor          ABI描述符
	 * @param vm_storage_arg_moves    传入参数的保存位置
	 * @param vm_storage_return_moves 接收返回值的保存位置
	 * @param needs_return_buffer     返回值是否需要缓冲区
	 * @param captured_state_mask
	 * @param needs_transition
	 * @return
	 */
	// @formatter:off
	/* 例如，对于JNI_GetCreatedJavaVMs()函数生成的隐藏类字节码为   
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
	public static final long downcall_stub(MethodType method_type, Object abi_descriptor, Object[] vm_storage_arg_moves_enc_arg_moves, Object[] vm_storage_enc_return_moves, boolean needs_return_buffer, int captured_state_mask, boolean needs_transition)
	{
		try
		{
			return (long) NativeEntryPoint_makeDowncallStub.invoke(method_type, abi_descriptor, vm_storage_arg_moves_enc_arg_moves, vm_storage_enc_return_moves, needs_return_buffer, captured_state_mask, needs_transition);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get downcall stub of type '" + method_type.toString() + "' failed", ex);
		}
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
	 * 为指定类型的函数创建入口点stub并缓存。<br>
	 * 
	 * @param method_type             JVM层stub方法的类型。如果是C++指针类型则传入long。第一个参数必须是long，代表函数指针地址，后面按顺序传入参数
	 * @param abi_descriptor          ABI描述符
	 * @param vm_storage_arg_moves    传入参数的保存位置
	 * @param vm_storage_return_moves 接收返回值的保存位置
	 * @param needs_return_buffer     返回值是否需要缓冲区
	 * @param captured_state_mask
	 * @param needs_transition
	 * @return
	 */
	public static final Object native_entry(MethodType method_type, Object abi_descriptor, Object[] vm_storage_arg_moves, Object[] vm_storage_return_moves, boolean needs_return_buffer, int captured_state_mask, boolean needs_transition)
	{
		try
		{
			return NativeEntryPoint_make.invoke(abi_descriptor, vm_storage_arg_moves, vm_storage_return_moves, method_type, needs_return_buffer, captured_state_mask, needs_transition);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("wrap native entry point of type '" + method_type.toString() + "' failed", ex);
		}
	}

	/**
	 * JVM内部使用的ABIDescriptor对象，不同架构和平台ABI描述对象不同。 https://github.com/openjdk/jdk/blob/a69409b0b7bcb4eb9a66327e1c6c53b3361ea1e9/src/hotspot/cpu/x86/foreignGlobals_x86_64.cpp#L46
	 */
	public static enum abi
	{
		x86_64_CSysV("x64.sysv", "CallArranger", "CSysV"),
		x86_64_CWindows("x64.windows", "CallArranger", "CWindows"),
		aarch64_CLinux("aarch64", "CallArranger", "C", true, "LINUX", false),
		aarch64_CMacOS("aarch64", "CallArranger", "C", true, "MACOS", false),
		aarch64_CWindows("aarch64.windows", "WindowsAArch64CallArranger", "WindowsAArch64AbiDescriptor", true, "aarch64", "CallArranger", "WINDOWS", false),
		power_pc_64_C("ppc64", "CallArranger", "C", false, "ABIv2", false),
		riscv64_CLinux("riscv64.linux", "LinuxRISCV64CallArranger", "CLinux"),
		s390_CLinux("s390.linux", "LinuxS390CallArranger", "CLinux");

		public static final abi host;

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
		@SuppressWarnings("rawtypes")
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
		 * 获取宿主机的架构ABI
		 * 
		 * @return
		 */
		private static final abi get_host_abi_descriptor()
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
				return power_pc_64_C;
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

		private Object _internal_descriptor = null;

		Class<?> _internal_abi_context_class;
		Class<?> _internal_call_arranger_class;
		Class<?> _internal_bindings_class;

		_get_bindings _get_bindings;

		private static abstract class _get_bindings
		{
			protected Object _internal_call_arranger_instance = null;
			protected MethodHandle CallArranger_getBindings;

			protected _get_bindings(Object _internal_call_arranger_instance, MethodHandle CallArranger_getBindings)
			{
				this._internal_call_arranger_instance = _internal_call_arranger_instance;
				this.CallArranger_getBindings = CallArranger_getBindings;
			}

			abstract Object call(FunctionDescriptor fd, boolean for_upcall, Object options);
		}

		private static class _static_get_bindings extends _get_bindings
		{
			protected _static_get_bindings(MethodHandle CallArranger_getBindings)
			{
				super(null, CallArranger_getBindings);
			}

			@Override
			Object call(FunctionDescriptor fd, boolean for_upcall, Object options)
			{
				try
				{
					return CallArranger_getBindings.invoke(fd.toMethodType(), fd, for_upcall, options);
				}
				catch (Throwable ex)
				{
					throw new java.lang.InternalError("static get bindings of '" + fd + "' failed", ex);
				}
			}
		}

		private static class _member_get_bindings extends _get_bindings
		{
			protected _member_get_bindings(Object _internal_call_arranger_instance, MethodHandle CallArranger_getBindings)
			{
				super(_internal_call_arranger_instance, CallArranger_getBindings);
			}

			@Override
			Object call(FunctionDescriptor fd, boolean for_upcall, Object options)
			{
				try
				{
					return CallArranger_getBindings.invoke(_internal_call_arranger_instance, fd.toMethodType(), fd, for_upcall, options);
				}
				catch (Throwable ex)
				{
					throw new java.lang.InternalError("member get bindings of '" + fd + "' failed", ex);
				}
			}
		}

		private abi(String abi_pkg_name, String abi_context_name, String abi_instance_name, boolean is_abi_static, String call_arranger_pkg_name, String call_arranger_name, String call_arranger_instance_name, boolean is_get_bindings_static)
		{
			try
			{
				// ABIDescriptor需要与CallArranger一起使用，CallArranger负责计算参数位置和重排以符合ABI
				_internal_abi_context_class = Class.forName("jdk.internal.foreign.abi." + abi_pkg_name + "." + abi_context_name);
				_internal_call_arranger_class = Class.forName("jdk.internal.foreign.abi." + call_arranger_pkg_name + "." + call_arranger_name);
				_internal_bindings_class = Class.forName(_internal_call_arranger_class.getName() + "$Bindings");
				Object _internal_call_arranger_instance = null;
				if (!is_abi_static || !is_get_bindings_static)
				{
					if (call_arranger_instance_name == null)
					{
						throw new java.lang.InternalError("call arranger instance name of '" + this.name() + "' cannot be null");
					}
					else
					{
						// CallArranger的实例一定是静态单例
						_internal_call_arranger_instance = unsafe.read_reference(_internal_call_arranger_class, call_arranger_instance_name);
					}
				}
				if (is_abi_static)
				{
					_internal_descriptor = unsafe.read_reference(_internal_abi_context_class, abi_instance_name);
				}
				else
				{
					Field _internal_descriptor_field = reflection.find_declared_field(_internal_abi_context_class, abi_instance_name);
					_internal_descriptor = unsafe.read_reference(_internal_call_arranger_instance, _internal_descriptor_field);
				}
				if (is_get_bindings_static)
				{
					// getBindings()为静态方法
					_get_bindings = new _static_get_bindings(symbols.find_static_method(_internal_call_arranger_class, "getBindings", _internal_bindings_class, MethodType.class, FunctionDescriptor.class, boolean.class, jdk_internal_foreign_abi_LinkerOptions));
				}
				else
				{
					// getBindings()为实例方法
					_get_bindings = new _member_get_bindings(_internal_call_arranger_instance, symbols.find_special_method(_internal_call_arranger_class, "getBindings", _internal_bindings_class, MethodType.class, FunctionDescriptor.class, boolean.class, jdk_internal_foreign_abi_LinkerOptions));
				}
			}
			catch (ClassNotFoundException ex)
			{
				throw new java.lang.InternalError("get internal abi descriptor of '" + this.name() + "' failed", ex);
			}
		}

		private abi(String abi_pkg_name, String abi_context_name, String abi_instance_name, String call_arranger_pkg_name, String call_arranger_name, String call_arranger_instance_name)
		{
			this(abi_pkg_name, abi_context_name, abi_instance_name, true, call_arranger_pkg_name, call_arranger_name, call_arranger_instance_name, true);
		}

		private abi(String abi_call_arranger_pkg_name, String abi_context_call_arranger_name, String abi_instance_name, boolean is_abi_static, String call_arranger_instance_name, boolean is_get_bindings_static)
		{
			this(abi_call_arranger_pkg_name, abi_context_call_arranger_name, abi_instance_name, is_abi_static, abi_call_arranger_pkg_name, abi_context_call_arranger_name, call_arranger_instance_name, is_get_bindings_static);
		}

		private abi(String abi_call_arranger_pkg_name, String abi_context_call_arranger_name, String static_abi_instance_name, String static_call_arranger_instance_name)
		{
			this(abi_call_arranger_pkg_name, abi_context_call_arranger_name, static_abi_instance_name, true, static_call_arranger_instance_name, true);
		}

		private abi(String abi_call_arranger_pkg_name, String abi_context_call_arranger_name, String static_abi_instance_name)
		{
			this(abi_call_arranger_pkg_name, abi_context_call_arranger_name, static_abi_instance_name, null);
		}

		public final Object descriptor()
		{
			return _internal_descriptor;
		}

		private static MethodHandle LinkerOptions_constructor;

		static
		{
			LinkerOptions_constructor = symbols.find_constructor(jdk_internal_foreign_abi_LinkerOptions, Map.class);
		}

		/**
		 * 创建jdk.internal.foreign.abi.LinkerOptions对象
		 * 
		 * @param options
		 * @return
		 */
		public static Object link_options(Map<Class<?>, Linker.Option> options)
		{
			try
			{
				return LinkerOptions_constructor.invoke(options);
			}
			catch (Throwable ex)
			{
				throw new java.lang.InternalError("create link options '" + options + "' failed", ex);
			}
		}

		/**
		 * 无特殊的Linker设置
		 */
		public static final Object none_link_options;

		static
		{
			none_link_options = link_options(Map.of());
		}

		public static class constraint
		{
			private static final long __klass_word;

			private static Class<?> jdk_internal_foreign_abi_CallingSequence;
			private static Class<?> jdk_internal_foreign_abi_Binding$Move;

			private static VarHandle CallingSequence_calleeMethodType;
			private static VarHandle CallingSequence_needsReturnBuffer;
			private static VarHandle CallingSequence_returnBufferSize;
			private static VarHandle CallingSequence_allocationSize;

			private static MethodHandle CallingSequence_needsTransition;
			private static MethodHandle CallingSequence_capturedStateMask;

			private static VarHandle CallingSequence_returnBindings;
			private static VarHandle CallingSequence_argumentBindings;

			private static MethodHandle Binding$Move_storage;

			static
			{
				try
				{
					jdk_internal_foreign_abi_CallingSequence = Class.forName("jdk.internal.foreign.abi.CallingSequence");
					jdk_internal_foreign_abi_Binding$Move = Class.forName("jdk.internal.foreign.abi.Binding$Move");
				}
				catch (ClassNotFoundException ex)
				{
					ex.printStackTrace();
				}
				__klass_word = java_type.get_klass_word(constraint.class);

				CallingSequence_calleeMethodType = symbols.find_var(jdk_internal_foreign_abi_CallingSequence, "calleeMethodType", MethodType.class);
				CallingSequence_needsReturnBuffer = symbols.find_var(jdk_internal_foreign_abi_CallingSequence, "needsReturnBuffer", boolean.class);
				CallingSequence_returnBufferSize = symbols.find_var(jdk_internal_foreign_abi_CallingSequence, "returnBufferSize", long.class);
				CallingSequence_allocationSize = symbols.find_var(jdk_internal_foreign_abi_CallingSequence, "allocationSize", long.class);
				CallingSequence_needsTransition = symbols.find_special_method(jdk_internal_foreign_abi_CallingSequence, "needsTransition", boolean.class);
				CallingSequence_capturedStateMask = symbols.find_special_method(jdk_internal_foreign_abi_CallingSequence, "capturedStateMask", int.class);

				CallingSequence_returnBindings = symbols.find_var(jdk_internal_foreign_abi_CallingSequence, "returnBindings", List.class);
				CallingSequence_argumentBindings = symbols.find_var(jdk_internal_foreign_abi_CallingSequence, "argumentBindings", List.class);

				Binding$Move_storage = symbols.find_virtual_method(jdk_internal_foreign_abi_Binding$Move, "storage", jdk_internal_foreign_abi_VMStorage);
			}

			/**
			 * 获取jdk.internal.foreign.abi.Binding.Move的storage()参数
			 * 
			 * @param move
			 * @return
			 */
			public static final Object get_binding_move_storage(Object move)
			{
				try
				{
					return Binding$Move_storage.invoke(move);
				}
				catch (Throwable ex)
				{
					throw new java.lang.InternalError("get storage of '" + move + "' failed", ex);
				}
			}

			private Object calling_sequence;
			private boolean is_in_memory_return;

			private constraint()
			{
				throw new java.lang.InstantiationError("constraint should not be instantiated");
			}

			public String toString()
			{
				return calling_sequence.toString();
			}

			/**
			 * 从CallArranger.Bindings转换为constraint
			 * 
			 * @param bindings
			 * @return
			 */
			public static constraint __from(Object bindings)
			{
				return (constraint) java_type.cast(bindings, __klass_word);
			}

			public final Object calling_sequence()
			{
				return calling_sequence;
			}

			public final boolean is_in_memory_return()
			{
				return is_in_memory_return;
			}

			public final MethodType stub_method_type()
			{
				return (MethodType) CallingSequence_calleeMethodType.get(calling_sequence);
			}

			public final boolean needs_return_buffer()
			{
				return (boolean) CallingSequence_needsReturnBuffer.get(calling_sequence);
			}

			public final long return_buffer_size()
			{
				return (long) CallingSequence_returnBufferSize.get(calling_sequence);
			}

			public final long allocation_size()
			{
				return (long) CallingSequence_allocationSize.get(calling_sequence);
			}

			public final boolean needs_transition()
			{
				try
				{
					return (boolean) CallingSequence_needsTransition.invoke(calling_sequence);
				}
				catch (Throwable ex)
				{
					throw new java.lang.InternalError("check needs transition of '" + calling_sequence + "' failed", ex);
				}
			}

			public final int captured_state_mask()
			{
				try
				{
					return (int) CallingSequence_capturedStateMask.invoke(calling_sequence);
				}
				catch (Throwable ex)
				{
					throw new java.lang.InternalError("get captured state mask of '" + calling_sequence + "' failed", ex);
				}
			}

			/**
			 * 获取返回值的VMStorage[]
			 * 
			 * @return
			 */
			public final Object[] ret_vm_storage()
			{
				List<Object> ret_bindings = (List<Object>) CallingSequence_returnBindings.get(calling_sequence);
				return ret_bindings.stream()
						.filter(jdk_internal_foreign_abi_Binding$Move::isInstance)
						.map(constraint::get_binding_move_storage)
						.toArray(virtual_machine.storage._new);
			}

			/**
			 * 获取参数的VMStorage[]
			 * 
			 * @return
			 */
			public final Object[] args_vm_storage()
			{
				List<List<Object>> args_bindings = (List<List<Object>>) CallingSequence_argumentBindings.get(calling_sequence);
				return args_bindings.stream().flatMap(List::stream)
						.filter(jdk_internal_foreign_abi_Binding$Move::isInstance)
						.map(constraint::get_binding_move_storage)
						.toArray(virtual_machine.storage._new);
			}
		}

		/**
		 * 为函数的参数进行C->Java的类型替换处理和重排以符合ABI标准
		 * 
		 * @param fd
		 * @param for_upcall 对于调用C/C++的downcall为false，否则为true
		 * @param options
		 * @return
		 */
		public final constraint resolve_constraints(FunctionDescriptor fd, boolean for_upcall, Object options)
		{
			return constraint.__from(_get_bindings.call(fd, for_upcall, options));

		}

		public final constraint resolve_constraints(FunctionDescriptor fd)
		{
			return resolve_constraints(fd, false, none_link_options);
		}

		public final Object native_entry(constraint resolved_constraint)
		{
			return shared_object.native_entry(resolved_constraint.stub_method_type(),
					this.descriptor(),
					resolved_constraint.args_vm_storage(),
					resolved_constraint.ret_vm_storage(),
					resolved_constraint.needs_return_buffer(),
					resolved_constraint.captured_state_mask(),
					resolved_constraint.needs_transition());
		}
	}

	/**
	 * 函数签名
	 */
	public static class function_signature
	{
		public String function_name;

		public cxx_type return_type;
		public cxx_type[] arg_types;

		public function_signature(String function_name, cxx_type return_type, cxx_type... arg_types)
		{
			this.function_name = function_name;
			this.return_type = return_type;
			this.arg_types = arg_types;
		}

		public function_signature(cxx_type return_type, cxx_type[] arg_types)
		{
			this(null, return_type, arg_types);
		}

		public static final function_signature of(String function_name, cxx_type return_type, cxx_type... arg_types)
		{
			return new function_signature(function_name, return_type, arg_types);
		}

		public static final function_signature of(cxx_type return_type, cxx_type... arg_types)
		{
			return new function_signature(return_type, arg_types);
		}

		public final FunctionDescriptor to_function_descriptor()
		{
			MemoryLayout[] arg_layouts = new MemoryLayout[arg_types.length];
			for (int idx = 0; idx < arg_types.length; ++idx)
				arg_layouts[idx] = arg_types[idx].memory_layout();
			if (return_type == cxx_type._void)
				return FunctionDescriptor.ofVoid(arg_layouts);
			else
				return FunctionDescriptor.of(return_type.memory_layout(), arg_layouts);
		}

		/**
		 * 获取本函数签名类型的stub函数的MethodHandle
		 * 
		 * @return
		 */
		public final MethodHandle resolve_stub()
		{
			return native_function(abi.host.native_entry(abi.host.resolve_constraints(this.to_function_descriptor())));
		}

		public final long resolve_addr(long handle)
		{
			return dlsym(handle, function_name);
		}
	}

	public static final MethodHandle native_function(Object downcall_entry)
	{
		try
		{
			return (MethodHandle) NativeMethodHandle_make.invoke(downcall_entry);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("wrap native method handle of '" + downcall_entry.toString() + "' failed", ex);
		}
	}

	public static final MethodHandle native_function(Object downcall_entry, long func_ptr)
	{
		return null;
		// return native_function(downcall_entry).(func_ptr);
	}
}

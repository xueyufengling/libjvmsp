package jvmsp;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.VMOption;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import jvmsp.object_model.header_layout;
import jvmsp.type.java_type;
import jvmsp.hotspot.classfile.java_lang_Class;
import jvmsp.hotspot.memory.Universe;
import jvmsp.hotspot.oops.CompressedKlassPointers;
import jvmsp.hotspot.oops.CompressedOops;

/**
 * 管理JVM的相关功能
 */
public class virtual_machine
{

	/**
	 * 64或32位JVM
	 */
	public static final int vm_arch;

	/**
	 * HotSpotDiagnosticMXBean的实现类是 com.sun.management.internal.HotSpotDiagnostic
	 */
	private static final HotSpotDiagnosticMXBean instance_HotSpotDiagnosticMXBean;

	static
	{
		// 获取JVM位数，支持多种JVM实现
		String arch = System.getProperty("sun.arch.data.model");
		if (arch == null)
			throw new java.lang.UnknownError("system property 'sun.arch.data.model' found null");
		if (arch.contains("64"))
			vm_arch = 64;
		else
			vm_arch = 32;
		// 获取HotSpotDiagnosticMXBean实例
		instance_HotSpotDiagnosticMXBean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
		if (instance_HotSpotDiagnosticMXBean == null)
		{
			throw new java.lang.InternalError("only Hotspot VM supported");
		}
	}

	public static final VMOption get_option(String name)
	{
		return instance_HotSpotDiagnosticMXBean.getVMOption(name);
	}

	/**
	 * 获取指定的boolean类型的VM参数
	 * 
	 * @param option_name 参数名称，例如UseCompressedOops
	 * @return
	 */
	public static final boolean get_bool_option(String option_name)
	{
		return Boolean.parseBoolean(get_option(option_name).getValue());
	}

	public static final boolean get_bool_option_or(String option_name, boolean default_value)
	{
		String value = get_option(option_name).getValue();
		return value == null ? default_value : Boolean.parseBoolean(value);
	}

	public static final int get_int_option(String option_name)
	{
		return Integer.parseInt(get_option(option_name).getValue());
	}

	public static final int get_int_option_or(String option_name, int default_value)
	{
		String value = get_option(option_name).getValue();
		return value == null ? default_value : Integer.parseInt(value);
	}

	public static final long get_long_option(String option_name)
	{
		return Long.parseLong(get_option(option_name).getValue());
	}

	public static final long get_long_option_or(String option_name, long default_value)
	{
		String value = get_option(option_name).getValue();
		return value == null ? default_value : Long.parseLong(value);
	}

	/**
	 * 无视权限获取系统属性
	 * 
	 * @param key
	 * @return
	 */
	public static final String get_property(String key)
	{
		return instance_Properties.getProperty(key);
	}

	/**
	 * 获取运行时的Java版本号
	 * 
	 * @return
	 */
	public static final String java_version()
	{
		return get_property("java.runtime.version");
	}

	/**
	 * 无视权限设置系统属性
	 * 
	 * @param key
	 * @param value
	 */
	public static final void set_property(String key, String value)
	{
		instance_Properties.setProperty(key, value);
	}

	/**
	 * 无视操作权限设置JVM的参数设置，必须自己确保value的类型与JVM的参数类型一致！调用的是native方法，但不是所有参数都支持运行时修改。大部分标志无法成功设置，因为检测可写标志在native方法内，无法干涉
	 * 
	 * @param name
	 * @param value
	 */
	public static final void set_option(String name, Object value)
	{
		try
		{
			Object flag = Flag_getFlag.invoke(name);
			Object v = Flag_getValue.invoke(class_Flag.cast(flag));
			VarHandle writeable = symbols.find_var(class_Flag, "writeable", boolean.class);
			writeable.set(flag, true);
			if (v instanceof Long lv)
				Flag_setLongValue.invokeExact(name, lv.longValue());
			if (v instanceof Double dv)
				Flag_setDoubleValue.invokeExact(name, dv.doubleValue());
			if (v instanceof Boolean bv)
				Flag_setBooleanValue.invokeExact(name, bv.booleanValue());
			if (v instanceof String sv)
				Flag_setStringValue.invokeExact(name, (String) sv);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("set option '" + name + "' to '" + value + "' failed", ex);
		}
	}

	/**
	 * 实际类型为sun.management.RuntimeImpl
	 */
	private static Object instance_java_lang_management_RuntimeMXBean = null;

	/**
	 * 在HotSpot虚拟机中VMManagement是sun.management.VMManagementImpl
	 */
	private static Class<?> sun_management_VMManagementImpl = null;
	/**
	 * JVM的管理类，实现是sun.management.VMManagementImpl，是sun.management.RuntimeImpl的成员jvm
	 */
	private static Object instance_sun_management_VMManagementImpl = null;

	private static MethodHandle VMManagementImpl_getProcessId;

	/**
	 * 系统属性System.props
	 */
	private static Properties instance_Properties = null;

	private static Class<?> jdk_internal_loader_ClassLoaders = null;

	/**
	 * JVM参数 com.sun.management.internal.Flag
	 */
	private static Class<?> class_Flag;
	private static MethodHandle Flag_getFlag;
	private static MethodHandle Flag_getValue;
	private static MethodHandle Flag_setLongValue;
	private static MethodHandle Flag_setDoubleValue;
	private static MethodHandle Flag_setBooleanValue;
	private static MethodHandle Flag_setStringValue;

	static
	{
		instance_java_lang_management_RuntimeMXBean = ManagementFactory.getRuntimeMXBean();
		try
		{
			instance_sun_management_VMManagementImpl = reflection.read(instance_java_lang_management_RuntimeMXBean, "jvm");// 获取JVM管理类
			sun_management_VMManagementImpl = instance_sun_management_VMManagementImpl.getClass();
			VMManagementImpl_getProcessId = symbols.find_special_method(sun_management_VMManagementImpl, sun_management_VMManagementImpl, "getProcessId", int.class);// 获取进程ID的native方法
			// 获取系统属性引用
			instance_Properties = (Properties) reflection.read(System.class, "props");
			// 获取所有系统ClassLoaders
			jdk_internal_loader_ClassLoaders = Class.forName("jdk.internal.loader.ClassLoaders");
			// 虚拟机参数Flag类及其成员方法
			class_Flag = Class.forName("com.sun.management.internal.Flag");
			Flag_getFlag = symbols.find_static_method(class_Flag, "getFlag", class_Flag, String.class);
			Flag_setLongValue = symbols.find_static_method(class_Flag, "setLongValue", void.class, String.class, long.class);
			Flag_setDoubleValue = symbols.find_static_method(class_Flag, "setDoubleValue", void.class, String.class, double.class);
			Flag_setBooleanValue = symbols.find_static_method(class_Flag, "setBooleanValue", void.class, String.class, boolean.class);
			Flag_setStringValue = symbols.find_static_method(class_Flag, "setStringValue", void.class, String.class, String.class);
			Flag_getValue = symbols.find_special_method(class_Flag, class_Flag, "getValue", Object.class);
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 获取JVM堆的基地址，大于等于HeapBaseMinAddress。<br>
	 * 
	 * @return
	 */
	public static final long heap_base()
	{
		return Universe.heap().reserved().start();
	}

	public static final void dump_heap(String file_name, boolean live)
	{
		try
		{
			instance_HotSpotDiagnosticMXBean.dumpHeap(file_name, live);
		}
		catch (IOException ex)
		{
			throw new java.lang.InternalError("dump heap to '" + file_name + "' faield", ex);
		}
	}

	/**
	 * https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/runtime/globals.hpp
	 * 运行时的VM全局变量
	 */

	/**
	 * 是否开启oop压缩，默认顺带开启对象头的klass word压缩（UseCompressedClassPointers）。
	 */
	public static final boolean UseCompressedOops = virtual_machine.get_bool_option_or("UseCompressedOops", false);

	public static final boolean UseCompactObjectHeaders = virtual_machine.get_bool_option_or("UseCompressedClassPointers", false);

	public static final boolean UseCompressedClassPointers = virtual_machine.get_bool_option_or("UseCompressedClassPointers", false);

	/**
	 * 堆上的地址
	 * 
	 * @param native_addr
	 * @return
	 */
	public static final long address_on_heap(long native_addr)
	{
		return native_addr - heap_base();
	}

	/**
	 * 压缩OOP的基地址
	 * 
	 * @return
	 */
	public static final long narrow_oop_base()
	{
		return CompressedOops.base();
	}

	/**
	 * 64位JVM开启UseCompressedOops的情况下，如果oop被压缩时，指向的地址有按位偏移。数值为log2(ObjectAlignmentInBytes)
	 */
	public static final int narrow_oop_shift()
	{
		return CompressedOops.shift();
	}

	/**
	 * 压缩narrow klass的基地址
	 * 
	 * @return
	 */
	public static final long narrow_klass_base()
	{
		return CompressedKlassPointers.base();
	}

	public static final int narrow_klass_shift()
	{
		return CompressedKlassPointers.shift();
	}

	/**
	 * 编码压缩oop<br>
	 * oop.encode_heap_oop_not_null()
	 * 
	 * @param native_addr
	 * @return
	 */
	public static final int encode_oop(long native_addr)
	{
		return CompressedOops.encode(native_addr);
	}

	/**
	 * 解码压缩oop，位移可能为0，此时表示未压缩的相对于堆起始位置的相对地址.
	 * 
	 * @param oop
	 * @return
	 */
	public static final long decode_oop(int oop)
	{
		return CompressedOops.decode(oop);
	}

	/**
	 * 从（压缩后的）oop获取Java对象。<br>
	 * 如果开启了oop压缩，则必须传入压缩后的oop。<br>
	 * 
	 * @param oop
	 * @return
	 */
	public static final Object resolve_oop(long oop)
	{
		Object[] _a = new Object[1];
		switch (unsafe.array_object_index_scale)
		{
		case 4:
			unsafe.write(_a, unsafe.array_object_base_offset, (int) oop);
		case 8:
			unsafe.write(_a, unsafe.array_object_base_offset, oop);
		}
		return _a[0];
	}

	/**
	 * 获取未压缩的oop，该地址为Java对象的实际内存地址
	 * 
	 * @param object
	 * @return
	 */
	public static final long address_of(Object object)
	{
		return decode_oop((int) java_type.oop_of(object));
	}

	public static final Object resolve_address(long addr)
	{
		return resolve_oop(encode_oop(addr));
	}

	/**
	 * 压缩Klass Pointer，实际上压缩方式和OOP一致
	 * 
	 * @param klass_ptr
	 * @return
	 */
	public static final long encode_narrow_klass(long klass_ptr)
	{
		return header_layout.encode_narrow_klass(klass_ptr);
	}

	/**
	 * 如果开启了UseCompressedClassPointers或+UseCompactObjectHeaders，则需要用此方法解压缩对象头的Klass Word才能得到Klass*指针。
	 * 从压缩或未压缩的KlassWord获取Klass*地址。<br>
	 * 该地址位于metaspace，地址是不变的，可以长期使用。<br>
	 * 
	 * @param narrow_klass
	 * @return
	 */
	public static final long decode_narrow_klass(int narrow_klass)
	{
		return header_layout.decode_narrow_klass(narrow_klass);
	}

	/**
	 * 获取Klass*指针
	 * 
	 * @param clazz
	 * @return
	 */
	public static final long klass_pointer_of(Class<?> clazz)
	{
		return java_lang_Class.klass_ptr(clazz);
	}

	/**
	 * 使用反射获取系统的内建ClassLoader
	 * 
	 * @param class_loader_name
	 * @return
	 */
	public static final Object builtin_class_loaders(String class_loader_name)
	{
		try
		{
			return reflection.read(jdk_internal_loader_ClassLoaders, class_loader_name);
		}
		catch (SecurityException | IllegalArgumentException ex)
		{
			throw new java.lang.InternalError("class loader name can only be BOOT_LOADER | PLATFORM_LOADER | APP_LOADER", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public static final ArrayList<URL> builtin_class_loader_classpath(String class_loader_name)
	{
		try
		{
			Object class_loader = builtin_class_loaders(class_loader_name);
			Object url_classpath = reflection.read(class_loader, "ucp");// jdk.internal.loader.URLClassPath
			if (url_classpath != null)
				return (ArrayList<URL>) reflection.read(url_classpath, "path");
			else
				return null;// BOOT_LOADER的ucp为null
		}
		catch (SecurityException | IllegalArgumentException ex)
		{
			throw new java.lang.InternalError("get class loader '" + class_loader_name + "' classpath failed", ex);
		}
	}

	/**
	 * 获取当前JVM的进程ID（也称PID）
	 * 
	 * @return
	 */
	public static final long process_id()
	{
		try
		{
			return (long) VMManagementImpl_getProcessId.invoke(instance_sun_management_VMManagementImpl);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get process id failed", ex);
		}
	}

	/**
	 * Java Agent相关功能
	 */
	private static Class<?> sun_tools_attach_HotSpotVirtualMachine;// sun.tools.attach.HotSpotVirtualMachine
	static
	{
		virtual_machine.set_property("jdk.attach.allowAttachSelf", "true");// 并非实际允许调用，只是记录在系统中
		try
		{
			sun_tools_attach_HotSpotVirtualMachine = Class.forName("sun.tools.attach.HotSpotVirtualMachine");
			unsafe.write(sun_tools_attach_HotSpotVirtualMachine, "ALLOW_ATTACH_SELF", true);// 设置instrument可以从程序attach，不需要在启动JVM时传入参数jdk.attach.allowAttachSelf=true
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 为指定的进程添加Agent
	 * 
	 * @param PID        要添加的JVM进程ID
	 * @param agent_path Agent的jar文件绝对路径
	 * @param args       传递给Agent的参数
	 */
	public static final void attach(long pid, String agent_path, String args)
	{
		try
		{
			VirtualMachine jvm = VirtualMachine.attach(String.valueOf(pid));
			jvm.loadAgent(agent_path, args);
			jvm.detach();
		}
		catch (AttachNotSupportedException | IOException | AgentLoadException | AgentInitializationException ex)
		{
			throw new java.lang.InternalError("attach agent '" + agent_path + "' to process '" + pid + "' failed", ex);
		}
	}

	public static final void attach(long pid, String agent_path)
	{
		attach(pid, agent_path, null);
	}

	/**
	 * 为当前进程添加Agent
	 * 
	 * @param agent_path Agent的jar文件绝对路径
	 * @param args       传递给Agent的参数
	 */
	public static final void attach(String agent_path, String args)
	{
		attach(virtual_machine.process_id(), agent_path, args);
	}

	public static final void attach(String agent_path)
	{
		attach(agent_path, null);
	}
}

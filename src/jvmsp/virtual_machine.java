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

/**
 * 管理JVM的相关功能
 */
public class virtual_machine
{
	/**
	 * 是否开启oop压缩，默认顺带开启对象头的klass word压缩（UseCompressedClassPointers）。
	 */
	public static final boolean UseCompressedOops;

	/**
	 * 对象字节对齐，默认为8,必须是2的幂，一般来说是机器的数据字大小，即int类型大小。
	 */
	public static final long ObjectAlignmentInBytes;

	/**
	 * 堆内存base的最小地址。
	 */
	public static final long HeapBaseMinAddress;

	/**
	 * 64或32，JVM有没有16位的还真不清楚
	 */
	public static final int NATIVE_JVM_BIT_VERSION;

	/**
	 * 是否运行在64位JVM，该变量为缓存值，用于指针的快速条件判断
	 */
	public static final boolean ON_64_BIT_JVM;

	/**
	 * JVM是否是HotSpot，如果是才能使用HotSpotDiagnostic获取JVM参数
	 */
	public static final boolean NATIVE_JVM_HOTSPOT;

	/**
	 * 64位JVM开启UseCompressedOops的情况下，如果oop被压缩时，指向的地址有按位偏移。NATIVE_ADDRESS_SHIFT=log2(ObjectAlignmentInBytes)
	 */
	public static final int OOP_ENCODE_ADDRESS_SHIFT;

	/**
	 * 机器CPU的数据字长度，也是native指针长度。根据JVM位数判断。
	 */
	public static final int NATIVE_WORD_SIZE;

	/**
	 * uint32_t的最大值，用于掩码和计算32位机器最大寻址地址。
	 */
	public static final long MAX_JUINT = 0xFFFFFFFFL;

	/**
	 * JVM中未压缩oop时支持的最大堆内存大小，类型为uint，实际上是4G
	 */
	public static final long UnscaledOopHeapMax;

	/**
	 * JVM中压缩了oop时支持的最大堆内存大小，类型为ulong，实际上是32G
	 */
	public static final long OopEncodingHeapMax;

	/**
	 * HotSpotDiagnosticMXBean的实现类是 com.sun.management.internal.HotSpotDiagnostic
	 */
	private static final HotSpotDiagnosticMXBean instance_HotSpotDiagnosticMXBean;

	static
	{
		String bit_version = System.getProperty("sun.arch.data.model");
		if (bit_version != null && bit_version.contains("64"))
			NATIVE_JVM_BIT_VERSION = 64;
		else
		{
			String arch = System.getProperty("os.arch");
			if (arch != null && arch.contains("64"))
				NATIVE_JVM_BIT_VERSION = 64;
			else
				NATIVE_JVM_BIT_VERSION = 32;
		}
		if (NATIVE_JVM_BIT_VERSION == 64)
			ON_64_BIT_JVM = true;
		else
			ON_64_BIT_JVM = false;
		NATIVE_WORD_SIZE = NATIVE_JVM_BIT_VERSION / 8;// 实际上还有16位机器，此时数据字长度只有2字节，但我们这无法获取本地机器的数据字长度，只能根据JVM位数去判断。
		boolean hotspot = false;
		boolean compressed_oops = false;
		long align_bytes = 8;
		long heap_base_min_addr = 0;
		instance_HotSpotDiagnosticMXBean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
		if (instance_HotSpotDiagnosticMXBean != null)
		{
			hotspot = true;// 获取HotSpotDiagnosticMXBean的getVMOption()方法
			if (NATIVE_JVM_BIT_VERSION == 64) // 64位JVM需要检查是否启用了指针压缩
				compressed_oops = get_bool_option("UseCompressedOops");
			align_bytes = get_long_option("ObjectAlignmentInBytes");
			heap_base_min_addr = get_long_option("HeapBaseMinAddress");
		}
		NATIVE_JVM_HOTSPOT = hotspot;

		UseCompressedOops = compressed_oops;
		ObjectAlignmentInBytes = align_bytes;
		HeapBaseMinAddress = heap_base_min_addr;

		OOP_ENCODE_ADDRESS_SHIFT = uint64_log2(ObjectAlignmentInBytes);

		UnscaledOopHeapMax = MAX_JUINT + 1;// 2^32
		OopEncodingHeapMax = UnscaledOopHeapMax << OOP_ENCODE_ADDRESS_SHIFT;// 使用OOP压缩编码后支持的最大的堆内存
	}

	/**
	 * 求2为底的对数，用于2的整数次幂的快速算法
	 * 
	 * @param num
	 * @return -1为无效结果
	 */
	public static final int uint64_log2(long uint64)
	{
		if (uint64 == 0)// 非法值
			return -1;
		int power = 0;
		long i = 0x01;
		while (i != uint64)
		{
			++power;
			if (i == 0)// 溢出
				return -1;
			i <<= 1;
		}
		return power;
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
		return Boolean.parseBoolean(get_option(option_name).getValue().toString());
	}

	public static final int get_int_option(String option_name)
	{
		return Integer.parseInt(get_option(option_name).getValue().toString());
	}

	public static final long get_long_option(String option_name)
	{
		return Long.parseLong(get_option(option_name).getValue().toString());
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
	 * 堆内存的最大大小
	 * 
	 * @return
	 */
	public static final long max_heap_size()
	{
		return Runtime.getRuntime().maxMemory();
	}

	/**
	 * 堆内存的当前大小
	 * 
	 * @return
	 */
	public static final long current_heap_size()
	{
		return Runtime.getRuntime().totalMemory();
	}

	/**
	 * 堆内存的当前空闲空间，创建新对象时空闲空间减小，GC后空闲空间增加
	 * 
	 * @return
	 */
	public static final long free_heap_size()
	{
		return Runtime.getRuntime().freeMemory();
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

	public static final long load_jvm()
	{
		return shared_object.dlopen("jvm");
	}

	/**
	 * 获取进程句柄
	 * 
	 * @return
	 */
	public static final long[] process_jvm_handles(long process_id)
	{
		return null;
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

	/**
	 * 对象layout类型
	 */
	public static enum object_layout
	{
		/**
		 * JDK 24+<br>
		 * 开启对象头压缩，包含压缩klass pointer，即+UseCompressedClassPointers<br>
		 * +UseCompactObjectHeaders
		 */
		Compact,
		/**
		 * 压缩klass pointer，但不压缩对象头<br>
		 * +UseCompressedClassPointers -UseCompactObjectHeaders
		 */
		Compressed,
		/**
		 * 未压缩klass pointer，也未压缩对象头<br>
		 * -UseCompressedClassPointers -UseCompactObjectHeaders
		 */
		Uncompressed,
		/**
		 * 未定义
		 */
		Undefined
	}

	public static final object_layout _klass_mode;
	public static final long _oop_base_offset_in_bytes;
	public static final boolean _oop_has_klass_gap;

	static
	{
		boolean UseCompactObjectHeaders = false;
		try
		{
			UseCompactObjectHeaders = get_bool_option("UseCompactObjectHeaders");
		}
		catch (Exception ex)
		{
		}
		if (UseCompactObjectHeaders)
		{
			_klass_mode = object_layout.Compact;
			_oop_has_klass_gap = false;
		}
		else
		{
			if (get_bool_option("UseCompressedClassPointers"))
			{
				_klass_mode = object_layout.Compressed;
				_oop_has_klass_gap = true;
			}
			else
			{
				_klass_mode = object_layout.Uncompressed;
				_oop_has_klass_gap = false;
			}
		}
		_oop_base_offset_in_bytes = java_type.HEADER_BYTE_LENGTH;
	}
}

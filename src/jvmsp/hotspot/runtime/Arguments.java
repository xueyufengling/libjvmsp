package jvmsp.hotspot.runtime;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;

/**
 * JVM启动参数读取
 */
public abstract class Arguments
{
	public static final String type_name = "Arguments";

	// JVM标志列表
	private static final long _jvm_flags_array = vm_struct.entry.find(type_name, "_jvm_flags_array").address;
	private static final long _num_jvm_flags = vm_struct.entry.find(type_name, "_num_jvm_flags").address;
	// JVM启动参数列表
	private static final long _jvm_args_array = vm_struct.entry.find(type_name, "_jvm_args_array").address;
	private static final long _num_jvm_args = vm_struct.entry.find(type_name, "_num_jvm_args").address;
	// 启动命令
	private static final long _java_command = vm_struct.entry.find(type_name, "_java_command").address;

	/**
	 * JVM标志数组，标志为字符串。<br>
	 * 返回一个char**，即字符串数组的指针。<br>
	 * 
	 * @return
	 */
	public static final long _jvm_flags_array()
	{
		return unsafe.read_pointer(_jvm_flags_array);
	}

	public static final long _jvm_args_array()
	{
		return unsafe.read_pointer(_jvm_args_array);
	}

	public static final long _java_command()
	{
		return unsafe.read_pointer(_java_command);
	}

	/**
	 * 读取JVM标志数组
	 * 
	 * @return
	 */
	public static final String[] jvm_flags_array()
	{
		return unsafe.read_cstr_arr(_jvm_flags_array(), num_jvm_flags());
	}

	/**
	 * 读取JVM参数数组
	 * 
	 * @return
	 */
	public static final String[] jvm_args_array()
	{
		return unsafe.read_cstr_arr(_jvm_args_array(), num_jvm_args());
	}

	public static final String java_command()
	{
		return unsafe.read_cstr(_java_command);
	}

	public static final int num_jvm_flags()
	{
		return unsafe.read_int(_num_jvm_flags);
	}

	public static final void set_num_jvm_flags(int num_jvm_flags)
	{
		unsafe.write(_num_jvm_flags, num_jvm_flags);
	}

	public static final int num_jvm_args()
	{
		return unsafe.read_int(_num_jvm_args);
	}

	public static final void set_num_jvm_args(int num_jvm_flags)
	{
		unsafe.write(_num_jvm_args, num_jvm_flags);
	}
}
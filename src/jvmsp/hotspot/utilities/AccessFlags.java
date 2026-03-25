package jvmsp.hotspot.utilities;

import jvmsp.memory;
import jvmsp.hotspot.vm_struct;

public class AccessFlags extends vm_struct
{
	private static final long _flags = vm_struct.entry.find("AccessFlags", "_flags").offset;

	// 类、字段、方法通用标记

	/**
	 * 是否是public修饰的
	 */
	public static final short JVM_ACC_PUBLIC = 0x0001;

	/**
	 * 是否是private修饰的
	 */
	public static final short JVM_ACC_PRIVATE = 0x0002;

	/**
	 * 是否是protected修饰的
	 */
	public static final short JVM_ACC_PROTECTED = 0x0004;

	/**
	 * 是否是final修饰的
	 */
	public static final short JVM_ACC_FINAL = 0x0010;

	/**
	 * 是否是编译器自动生成的
	 */
	public static final short JVM_ACC_SYNTHETIC = 0x1000;

	// 类、字段通用标记

	/**
	 * 是否是枚举
	 */
	public static final short JVM_ACC_ENUM = 0x4000;

	// 类、方法通用标记

	/**
	 * 是否是abstract修饰的
	 */
	public static final short JVM_ACC_ABSTRACT = 0x0400;

	// 字段、方法通用标记

	/**
	 * 是否是static修饰的
	 */
	public static final short JVM_ACC_STATIC = 0x0008;

	// 类专用标记

	/**
	 * 类专用，invokespecial使用
	 */
	public static final short JVM_ACC_SUPER = 0x0020;

	/**
	 * 类专用，是否是接口
	 */
	public static final short JVM_ACC_INTERFACE = 0x0200;

	/**
	 * 类专用，是否是注解
	 */
	public static final short JVM_ACC_ANNOTATION = 0x2000;

	// 字段专用标记

	/**
	 * 字段专用，是否是transient修饰的，序列化与反序列化使用
	 */
	public static final short JVM_ACC_TRANSIENT = 0x0080;

	/**
	 * 字段专用，是否是volatile修饰的
	 */
	public static final short JVM_ACC_VOLATILE = 0x0040;

	// 方法专用标记

	/**
	 * 方法专用，是否加锁
	 */
	public static final short JVM_ACC_SYNCHRONIZED = 0x0020;

	/**
	 * 方法专用，是否是泛型编译生成的桥接方法
	 */
	public static final short JVM_ACC_BRIDGE = 0x0040;

	/**
	 * 方法专用，是否是变长参数
	 */
	public static final short JVM_ACC_VARARGS = 0x0080;

	/**
	 * 方法专用，是否是native修饰的
	 */
	public static final short JVM_ACC_NATIVE = 0x0100;

	/**
	 * 方法专用，strictfp即严格浮点运算
	 */
	public static final short JVM_ACC_STRICT = 0x0800;

	/**
	 * JVM识别类的标记掩码
	 */
	public static final short JVM_RECOGNIZED_CLASS_MODIFIERS = (JVM_ACC_PUBLIC |
			JVM_ACC_FINAL |
			JVM_ACC_SUPER |
			JVM_ACC_INTERFACE |
			JVM_ACC_ABSTRACT |
			JVM_ACC_ANNOTATION |
			JVM_ACC_ENUM |
			JVM_ACC_SYNTHETIC);

	/**
	 * JVM识别字段的标记掩码
	 */
	public static final short JVM_RECOGNIZED_FIELD_MODIFIERS = (JVM_ACC_PUBLIC |
			JVM_ACC_PRIVATE |
			JVM_ACC_PROTECTED |
			JVM_ACC_STATIC |
			JVM_ACC_FINAL |
			JVM_ACC_VOLATILE |
			JVM_ACC_TRANSIENT |
			JVM_ACC_ENUM |
			JVM_ACC_SYNTHETIC);

	/**
	 * JVM识别方法的标记掩码
	 */
	public static final short JVM_RECOGNIZED_METHOD_MODIFIERS = (JVM_ACC_PUBLIC |
			JVM_ACC_PRIVATE |
			JVM_ACC_PROTECTED |
			JVM_ACC_STATIC |
			JVM_ACC_FINAL |
			JVM_ACC_SYNCHRONIZED |
			JVM_ACC_BRIDGE |
			JVM_ACC_VARARGS |
			JVM_ACC_NATIVE |
			JVM_ACC_ABSTRACT |
			JVM_ACC_STRICT |
			JVM_ACC_SYNTHETIC);

	public AccessFlags(long address)
	{
		super("AccessFlags", address);
	}

	public String toString()
	{
		return memory.bits_str(_flags());
	}

	public short _flags()
	{
		return super.read_short(_flags);
	}

	public void set_flags(short flags)
	{
		super.write(_flags, flags);
	}

	public boolean is_public()
	{
		return memory.flag_bit(_flags(), JVM_ACC_PUBLIC);
	}

	public boolean is_private()
	{
		return memory.flag_bit(_flags(), JVM_ACC_PRIVATE);
	}

	public boolean is_protected()
	{
		return memory.flag_bit(_flags(), JVM_ACC_PROTECTED);
	}

	public boolean is_static()
	{
		return memory.flag_bit(_flags(), JVM_ACC_STATIC);
	}

	public boolean is_final()
	{
		return memory.flag_bit(_flags(), JVM_ACC_FINAL);
	}

	public boolean is_synchronized()
	{
		return memory.flag_bit(_flags(), JVM_ACC_SYNCHRONIZED);
	}

	public boolean is_super()
	{
		return memory.flag_bit(_flags(), JVM_ACC_SUPER);
	}

	public boolean is_volatile()
	{
		return memory.flag_bit(_flags(), JVM_ACC_VOLATILE);
	}

	public boolean is_transient()
	{
		return memory.flag_bit(_flags(), JVM_ACC_TRANSIENT);
	}

	public boolean is_native()
	{
		return memory.flag_bit(_flags(), JVM_ACC_NATIVE);
	}

	public boolean is_interface()
	{
		return memory.flag_bit(_flags(), JVM_ACC_INTERFACE);
	}

	public boolean is_abstract()
	{
		return memory.flag_bit(_flags(), JVM_ACC_ABSTRACT);
	}

	public boolean is_synthetic()
	{
		return memory.flag_bit(_flags(), JVM_ACC_SYNTHETIC);
	}

	public void set_public(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_PUBLIC, value));
	}

	public void set_private(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_PRIVATE, value));
	}

	public void set_protected(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_PROTECTED, value));
	}

	public void set_static(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_STATIC, value));
	}

	public void set_final(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_FINAL, value));
	}

	public void set_synchronized(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_SYNCHRONIZED, value));
	}

	public void set_super(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_SUPER, value));
	}

	public void set_volatile(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_VOLATILE, value));
	}

	public void set_bridge(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_BRIDGE, value));
	}

	public void set_transient(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_TRANSIENT, value));
	}

	public void set_varargs(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_VARARGS, value));
	}

	public void set_native(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_NATIVE, value));
	}

	public void set_interface(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_INTERFACE, value));
	}

	public void set_abstract(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_ABSTRACT, value));
	}

	public void set_strict(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_STRICT, value));
	}

	public void set_synthetic(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_SYNTHETIC, value));
	}

	public void set_annotation(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_ANNOTATION, value));
	}

	public void set_enum(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), JVM_ACC_ENUM, value));
	}
}

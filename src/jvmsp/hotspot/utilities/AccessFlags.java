package jvmsp.hotspot.utilities;

import jvmsp.memory;
import jvmsp.hotspot.vm_struct;

/**
 * 访问标志
 */
public class AccessFlags extends vm_struct
{
	public static final String type_name = "AccessFlags";
	public static final long size = sizeof(type_name);

	public static final long _flags = vm_struct.entry.find(type_name, "_flags").offset;

	// 类、字段、方法通用标志

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

	// 类、字段通用标志

	/**
	 * 是否是枚举
	 */
	public static final short JVM_ACC_ENUM = 0x4000;

	// 类、方法通用标志

	/**
	 * 是否是abstract修饰的
	 */
	public static final short JVM_ACC_ABSTRACT = 0x0400;

	// 字段、方法通用标志

	/**
	 * 是否是static修饰的
	 */
	public static final short JVM_ACC_STATIC = 0x0008;

	// 类专用标志

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

	// 字段专用标志

	/**
	 * 字段专用，是否是transient修饰的，序列化与反序列化使用
	 */
	public static final short JVM_ACC_TRANSIENT = 0x0080;

	/**
	 * 字段专用，是否是volatile修饰的
	 */
	public static final short JVM_ACC_VOLATILE = 0x0040;

	// 方法专用标志

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
	 * JVM识别类的标志掩码
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
	 * JVM识别字段的标志掩码
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
	 * JVM识别方法的标志掩码
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
		super(type_name, address);
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

	public static final boolean is_public(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_PUBLIC);
	}

	public static final boolean is_private(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_PRIVATE);
	}

	public static final boolean is_protected(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_PROTECTED);
	}

	public static final boolean is_static(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_STATIC);
	}

	public static final boolean is_final(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_FINAL);
	}

	public static final boolean is_synchronized(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_SYNCHRONIZED);
	}

	public static final boolean is_super(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_SUPER);
	}

	public static final boolean is_volatile(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_VOLATILE);
	}

	public static final boolean is_transient(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_TRANSIENT);
	}

	public static final boolean is_native(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_NATIVE);
	}

	public static final boolean is_interface(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_INTERFACE);
	}

	public static final boolean is_abstract(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_ABSTRACT);
	}

	public static final boolean is_synthetic(short flags)
	{
		return memory.flag_bit(flags, JVM_ACC_SYNTHETIC);
	}

	/**
	 * 无访问修饰符的包级权限。<br>
	 * 
	 * @return
	 */
	public static final boolean is_package_private(short flags)
	{
		return !is_public(flags) && !is_private(flags) && !is_protected(flags);
	}

	public static final short set_public(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_PUBLIC, value);
	}

	public static final short set_private(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_PRIVATE, value);
	}

	public static final short set_protected(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_PROTECTED, value);
	}

	public static final short set_static(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_STATIC, value);
	}

	public static final short set_final(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_FINAL, value);
	}

	public static final short set_synchronized(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_SYNCHRONIZED, value);
	}

	public static final short set_super(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_SUPER, value);
	}

	public static final short set_volatile(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_VOLATILE, value);
	}

	public static final short set_bridge(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_BRIDGE, value);
	}

	public static final short set_transient(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_TRANSIENT, value);
	}

	public static final short set_varargs(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_VARARGS, value);
	}

	public static final short set_native(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_NATIVE, value);
	}

	public static final short set_interface(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_INTERFACE, value);
	}

	public static final short set_abstract(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_ABSTRACT, value);
	}

	public static final short set_strict(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_STRICT, value);
	}

	public static final short set_synthetic(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_SYNTHETIC, value);
	}

	public static final short set_annotation(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_ANNOTATION, value);
	}

	public static final short set_enum(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, JVM_ACC_ENUM, value);
	}

	public static final short set_package_private(short flags)
	{
		flags = set_public(flags, false);
		flags = set_private(flags, false);
		flags = set_protected(flags, false);
		return flags;
	}

	// 成员方法

	public boolean is_public()
	{
		return is_public(_flags());
	}

	public boolean is_private()
	{
		return is_private(_flags());
	}

	public boolean is_protected()
	{
		return is_protected(_flags());
	}

	public boolean is_static()
	{
		return is_static(_flags());
	}

	public boolean is_final()
	{
		return is_final(_flags());
	}

	public boolean is_synchronized()
	{
		return is_synchronized(_flags());
	}

	public boolean is_super()
	{
		return is_super(_flags());
	}

	public boolean is_volatile()
	{
		return is_volatile(_flags());
	}

	public boolean is_transient()
	{
		return is_transient(_flags());
	}

	public boolean is_native()
	{
		return is_native(_flags());
	}

	public boolean is_interface()
	{
		return is_interface(_flags());
	}

	public boolean is_abstract()
	{
		return is_abstract(_flags());
	}

	public boolean is_synthetic()
	{
		return is_synthetic(_flags());
	}

	public boolean is_package_private()
	{
		return is_package_private(_flags());
	}

	public void set_public(boolean value)
	{
		set_flags(set_public(_flags(), value));
	}

	public void set_private(boolean value)
	{
		set_flags(set_private(_flags(), value));
	}

	public void set_protected(boolean value)
	{
		set_flags(set_protected(_flags(), value));
	}

	public void set_static(boolean value)
	{
		set_flags(set_static(_flags(), value));
	}

	public void set_final(boolean value)
	{
		set_flags(set_final(_flags(), value));
	}

	public void set_synchronized(boolean value)
	{
		set_flags(set_synchronized(_flags(), value));
	}

	public void set_super(boolean value)
	{
		set_flags(set_super(_flags(), value));
	}

	public void set_volatile(boolean value)
	{
		set_flags(set_volatile(_flags(), value));
	}

	public void set_bridge(boolean value)
	{
		set_flags(set_bridge(_flags(), value));
	}

	public void set_transient(boolean value)
	{
		set_flags(set_transient(_flags(), value));
	}

	public void set_varargs(boolean value)
	{
		set_flags(set_varargs(_flags(), value));
	}

	public void set_native(boolean value)
	{
		set_flags(set_native(_flags(), value));
	}

	public void set_interface(boolean value)
	{
		set_flags(set_interface(_flags(), value));
	}

	public void set_abstract(boolean value)
	{
		set_flags(set_abstract(_flags(), value));
	}

	public void set_strict(boolean value)
	{
		set_flags(set_strict(_flags(), value));
	}

	public void set_synthetic(boolean value)
	{
		set_flags(set_synthetic(_flags(), value));
	}

	public void set_annotation(boolean value)
	{
		set_flags(set_annotation(_flags(), value));
	}

	public void set_enum(boolean value)
	{
		set_flags(set_enum(_flags(), value));
	}

	public void set_package_private()
	{
		set_flags(set_package_private(_flags()));
	}
}

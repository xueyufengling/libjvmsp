package jvmsp.hotspot.oops;

import jvmsp.memory;
import jvmsp.hotspot.vm_struct;
import jvmsp.type.cxx_type;

/**
 * JVM运行时Klass的JVM内部使用标志。<br>
 * JDK21尚不存在。<br>
 */
public class KlassFlags extends vm_struct
{
	public static final String type_name = "KlassFlags";

	public static final cxx_type KlassFlags = cxx_type.define(type_name)
			.decl_field("_flags", cxx_type.uint8_t)
			.resolve();

	public static final long size = KlassFlags.size();

	private static final long _flags = KlassFlags.field("_flags").offset();

	public static final byte is_hidden_class = 1 << 0;
	public static final byte is_value_based_class = 1 << 1;
	public static final byte has_finalizer = 1 << 2;
	public static final byte is_cloneable_fast = 1 << 3;

	public KlassFlags(long address)
	{
		super(type_name, address);
	}

	@Override
	public String toString()
	{
		return memory.bits_str(_flags());
	}

	public byte _flags()
	{
		return super.read_byte(_flags);
	}

	public void set_flags(byte flags)
	{
		super.write(_flags, flags);
	}

	public static final boolean is_hidden_class(byte flags)
	{
		return memory.flag_bit(flags, is_hidden_class);
	}

	public static final byte set_is_hidden_class(byte flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_hidden_class, value);
	}

	public static final boolean is_value_based_class(byte flags)
	{
		return memory.flag_bit(flags, is_value_based_class);
	}

	public static final byte set_is_value_based_class(byte flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_value_based_class, value);
	}

	public static final boolean has_finalizer(byte flags)
	{
		return memory.flag_bit(flags, has_finalizer);
	}

	public static final byte set_has_finalizer(byte flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_finalizer, value);
	}

	public static final boolean is_cloneable_fast(byte flags)
	{
		return memory.flag_bit(flags, is_cloneable_fast);
	}

	public static final byte set_is_cloneable_fast(byte flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_cloneable_fast, value);
	}

	// 成员方法

	public boolean is_hidden_class()
	{
		return is_hidden_class(_flags());
	}

	public void set_is_hidden_class(boolean value)
	{
		set_flags(set_is_hidden_class(_flags(), value));
	}

	public boolean is_value_based_class()
	{
		return is_value_based_class(_flags());
	}

	public void set_is_value_based_class(boolean value)
	{
		set_flags(set_is_value_based_class(_flags(), value));
	}

	public boolean has_finalizer()
	{
		return has_finalizer(_flags());
	}

	public void set_has_finalizer(boolean value)
	{
		set_flags(set_has_finalizer(_flags(), value));
	}

	public boolean is_cloneable_fast()
	{
		return is_cloneable_fast(_flags());
	}

	public void set_is_cloneable_fast(boolean value)
	{
		set_flags(set_is_cloneable_fast(_flags(), value));
	}
}
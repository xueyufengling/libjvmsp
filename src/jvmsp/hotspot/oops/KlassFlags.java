package jvmsp.hotspot.oops;

import jvmsp.memory;
import jvmsp.hotspot.vm_struct;
import jvmsp.type.cxx_type;

/**
 * JVM运行时Klass内部使用标志。<br>
 * JDK21尚不存在。<br>
 */
public class KlassFlags extends vm_struct
{
	public static final cxx_type KlassFlags = cxx_type.define("KlassFlags")
			.decl_field("_flags", cxx_type.uint8_t)
			.resolve();

	private static final long _flags = KlassFlags.field("_flags").offset();

	public static final byte is_hidden_class = 1 << 0;
	public static final byte is_value_based_class = 1 << 1;
	public static final byte has_finalizer = 1 << 2;
	public static final byte is_cloneable_fast = 1 << 3;

	public KlassFlags(long address)
	{
		super("KlassFlags", address);
	}

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

	public boolean is_is_hidden_class()
	{
		return memory.flag_bit(_flags(), is_hidden_class);
	}

	public void set_is_hidden_class(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), is_hidden_class, value));
	}

	public boolean is_is_value_based_class()
	{
		return memory.flag_bit(_flags(), is_value_based_class);
	}

	public void set_is_value_based_class(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), is_value_based_class, value));
	}

	public boolean is_has_finalizer()
	{
		return memory.flag_bit(_flags(), has_finalizer);
	}

	public void set_has_finalizer(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_finalizer, value));
	}

	public boolean is_is_cloneable_fast()
	{
		return memory.flag_bit(_flags(), is_cloneable_fast);
	}

	public void set_is_cloneable_fast(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), is_cloneable_fast, value));
	}
}
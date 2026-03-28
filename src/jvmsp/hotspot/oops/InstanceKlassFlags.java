package jvmsp.hotspot.oops;

import jvmsp.memory;
import jvmsp.hotspot.vm_struct;
import jvmsp.type.cxx_type;

public class InstanceKlassFlags extends vm_struct
{
	public static final String type_name = "InstanceKlassFlags";

	public static final cxx_type InstanceKlassFlags = cxx_type.define(type_name)
			.decl_field("_flags", cxx_type.uint16_t)
			.decl_field("_status", cxx_type.uint8_t)
			.resolve();

	public static final long size = InstanceKlassFlags.size();

	private static final long _flags = InstanceKlassFlags.field("_flags").offset();
	private static final long _status = InstanceKlassFlags.field("_status").offset();

	// _flags
	public static final short rewritten = 1 << 0;
	public static final short has_nonstatic_fields = 1 << 1;
	public static final short should_verify_class = 1 << 2;
	public static final short is_contended = 1 << 3;
	public static final short has_nonstatic_concrete_methods = 1 << 4;
	public static final short declares_nonstatic_concrete_methods = 1 << 5;
	public static final short shared_loading_failed = 1 << 6;
	public static final short is_shared_boot_class = 1 << 7;
	public static final short is_shared_platform_class = 1 << 8;
	public static final short is_shared_app_class = 1 << 9;
	public static final short has_contended_annotations = 1 << 10;
	public static final short has_localvariable_table = 1 << 11;
	public static final short has_miranda_methods = 1 << 12;
	public static final short has_vanilla_constructor = 1 << 13;
	public static final short has_final_method = 1 << 14;

	// _status
	public static final byte is_being_redefined = 1 << 0;
	public static final byte has_resolved_methods = 1 << 1;
	public static final byte has_been_redefined = 1 << 2;
	public static final byte is_scratch_class = 1 << 3;
	public static final byte is_marked_dependent = 1 << 4;

	public InstanceKlassFlags(long address)
	{
		super(type_name, address);
	}

	@Override
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

	public byte _status()
	{
		return super.read_byte(_status);
	}

	public void set_status(byte status)
	{
		super.write(_status, status);
	}

	// _flags
	public boolean rewritten()
	{
		return memory.flag_bit(_flags(), rewritten);
	}

	public void set_rewritten(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), rewritten, value));
	}

	public boolean has_nonstatic_fields()
	{
		return memory.flag_bit(_flags(), has_nonstatic_fields);
	}

	public void set_has_nonstatic_fields(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_nonstatic_fields, value));
	}

	public boolean should_verify_class()
	{
		return memory.flag_bit(_flags(), should_verify_class);
	}

	public void set_should_verify_class(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), should_verify_class, value));
	}

	public boolean is_contended()
	{
		return memory.flag_bit(_flags(), is_contended);
	}

	public void set_is_contended(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), is_contended, value));
	}

	public boolean has_nonstatic_concrete_methods()
	{
		return memory.flag_bit(_flags(), has_nonstatic_concrete_methods);
	}

	public void set_has_nonstatic_concrete_methods(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_nonstatic_concrete_methods, value));
	}

	public boolean declares_nonstatic_concrete_methods()
	{
		return memory.flag_bit(_flags(), declares_nonstatic_concrete_methods);
	}

	public void set_declares_nonstatic_concrete_methods(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), declares_nonstatic_concrete_methods, value));
	}

	public boolean shared_loading_failed()
	{
		return memory.flag_bit(_flags(), shared_loading_failed);
	}

	public void set_shared_loading_failed(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), shared_loading_failed, value));
	}

	public boolean is_shared_boot_class()
	{
		return memory.flag_bit(_flags(), is_shared_boot_class);
	}

	public void set_is_shared_boot_class(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), is_shared_boot_class, value));
	}

	public boolean is_shared_platform_class()
	{
		return memory.flag_bit(_flags(), is_shared_platform_class);
	}

	public void set_is_shared_platform_class(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), is_shared_platform_class, value));
	}

	public boolean is_shared_app_class()
	{
		return memory.flag_bit(_flags(), is_shared_app_class);
	}

	public void set_is_shared_app_class(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), is_shared_app_class, value));
	}

	public boolean has_contended_annotations()
	{
		return memory.flag_bit(_flags(), has_contended_annotations);
	}

	public void set_has_contended_annotations(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_contended_annotations, value));
	}

	public boolean has_localvariable_table()
	{
		return memory.flag_bit(_flags(), has_localvariable_table);
	}

	public void set_has_localvariable_table(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_localvariable_table, value));
	}

	public boolean has_miranda_methods()
	{
		return memory.flag_bit(_flags(), has_miranda_methods);
	}

	public void set_has_miranda_methods(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_miranda_methods, value));
	}

	public boolean has_vanilla_constructor()
	{
		return memory.flag_bit(_flags(), has_vanilla_constructor);
	}

	public void set_has_vanilla_constructor(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_vanilla_constructor, value));
	}

	public boolean has_final_method()
	{
		return memory.flag_bit(_flags(), has_final_method);
	}

	public void set_has_final_method(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_final_method, value));
	}

	// _status
	public boolean is_being_redefined()
	{
		return memory.flag_bit(_status(), is_being_redefined);
	}

	public void set_is_being_redefined(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), is_being_redefined, value));
	}

	public boolean has_resolved_methods()
	{
		return memory.flag_bit(_status(), has_resolved_methods);
	}

	public void set_has_resolved_methods(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), has_resolved_methods, value));
	}

	public boolean has_been_redefined()
	{
		return memory.flag_bit(_status(), has_been_redefined);
	}

	public void set_has_been_redefined(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), has_been_redefined, value));
	}

	public boolean is_scratch_class()
	{
		return memory.flag_bit(_status(), is_scratch_class);
	}

	public void set_is_scratch_class(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), is_scratch_class, value));
	}

	public boolean is_marked_dependent()
	{
		return memory.flag_bit(_status(), is_marked_dependent);
	}

	public void set_is_marked_dependent(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), is_marked_dependent, value));
	}
}
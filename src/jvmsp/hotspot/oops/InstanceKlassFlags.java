package jvmsp.hotspot.oops;

import jvmsp.memory;
import jvmsp.hotspot.vm_constant;
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
	public static final short has_nonstatic_concrete_methods = (short) vm_constant.find_int("InstanceKlassFlags::_misc_has_nonstatic_concrete_methods");// 1 << 4;
	public static final short declares_nonstatic_concrete_methods = (short) vm_constant.find_int("InstanceKlassFlags::_misc_has_nonstatic_concrete_methods"); // 1 << 5;
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
	public static final boolean rewritten(short flags)
	{
		return memory.flag_bit(flags, rewritten);
	}

	public static final short set_rewritten(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, rewritten, value);
	}

	public static final boolean has_nonstatic_fields(short flags)
	{
		return memory.flag_bit(flags, has_nonstatic_fields);
	}

	public static final short set_has_nonstatic_fields(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_nonstatic_fields, value);
	}

	public static final boolean should_verify_class(short flags)
	{
		return memory.flag_bit(flags, should_verify_class);
	}

	public static final short set_should_verify_class(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, should_verify_class, value);
	}

	public static final boolean is_contended(short flags)
	{
		return memory.flag_bit(flags, is_contended);
	}

	public static final short set_is_contended(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_contended, value);
	}

	public static final boolean has_nonstatic_concrete_methods(short flags)
	{
		return memory.flag_bit(flags, has_nonstatic_concrete_methods);
	}

	public static final short set_has_nonstatic_concrete_methods(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_nonstatic_concrete_methods, value);
	}

	public static final boolean declares_nonstatic_concrete_methods(short flags)
	{
		return memory.flag_bit(flags, declares_nonstatic_concrete_methods);
	}

	public static final short set_declares_nonstatic_concrete_methods(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, declares_nonstatic_concrete_methods, value);
	}

	public static final boolean shared_loading_failed(short flags)
	{
		return memory.flag_bit(flags, shared_loading_failed);
	}

	public static final short set_shared_loading_failed(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, shared_loading_failed, value);
	}

	public static final boolean is_shared_boot_class(short flags)
	{
		return memory.flag_bit(flags, is_shared_boot_class);
	}

	public static final short set_is_shared_boot_class(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_shared_boot_class, value);
	}

	public static final boolean is_shared_platform_class(short flags)
	{
		return memory.flag_bit(flags, is_shared_platform_class);
	}

	public static final short set_is_shared_platform_class(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_shared_platform_class, value);
	}

	public static final boolean is_shared_app_class(short flags)
	{
		return memory.flag_bit(flags, is_shared_app_class);
	}

	public static final short set_is_shared_app_class(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_shared_app_class, value);
	}

	public static final boolean has_contended_annotations(short flags)
	{
		return memory.flag_bit(flags, has_contended_annotations);
	}

	public static final short set_has_contended_annotations(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_contended_annotations, value);
	}

	public static final boolean has_localvariable_table(short flags)
	{
		return memory.flag_bit(flags, has_localvariable_table);
	}

	public static final short set_has_localvariable_table(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_localvariable_table, value);
	}

	public static final boolean has_miranda_methods(short flags)
	{
		return memory.flag_bit(flags, has_miranda_methods);
	}

	public static final short set_has_miranda_methods(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_miranda_methods, value);
	}

	public static final boolean has_vanilla_constructor(short flags)
	{
		return memory.flag_bit(flags, has_vanilla_constructor);
	}

	public static final short set_has_vanilla_constructor(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_vanilla_constructor, value);
	}

	public static final boolean has_final_method(short flags)
	{
		return memory.flag_bit(flags, has_final_method);
	}

	public static final short set_has_final_method(short flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_final_method, value);
	}

	// _status
	public static final boolean is_being_redefined(byte status)
	{
		return memory.flag_bit(status, is_being_redefined);
	}

	public static final byte set_is_being_redefined(byte status, boolean value)
	{
		return memory.set_flag_bit(status, is_being_redefined, value);
	}

	public static final boolean has_resolved_methods(byte status)
	{
		return memory.flag_bit(status, has_resolved_methods);
	}

	public static final byte set_has_resolved_methods(byte status, boolean value)
	{
		return memory.set_flag_bit(status, has_resolved_methods, value);
	}

	public static final boolean has_been_redefined(byte status)
	{
		return memory.flag_bit(status, has_been_redefined);
	}

	public static final byte set_has_been_redefined(byte status, boolean value)
	{
		return memory.set_flag_bit(status, has_been_redefined, value);
	}

	public static final boolean is_scratch_class(byte status)
	{
		return memory.flag_bit(status, is_scratch_class);
	}

	public static final byte set_is_scratch_class(byte status, boolean value)
	{
		return memory.set_flag_bit(status, is_scratch_class, value);
	}

	public static final boolean is_marked_dependent(byte status)
	{
		return memory.flag_bit(status, is_marked_dependent);
	}

	public static final byte set_is_marked_dependent(byte status, boolean value)
	{
		return memory.set_flag_bit(status, is_marked_dependent, value);
	}

	// 封装成员方法

	// _flags

	public boolean rewritten()
	{
		return rewritten(_flags());
	}

	public void set_rewritten(boolean value)
	{
		set_flags(set_rewritten(_flags(), value));
	}

	public boolean has_nonstatic_fields()
	{
		return has_nonstatic_fields(_flags());
	}

	public void set_has_nonstatic_fields(boolean value)
	{
		set_flags(set_has_nonstatic_fields(_flags(), value));
	}

	public boolean should_verify_class()
	{
		return should_verify_class(_flags());
	}

	public void set_should_verify_class(boolean value)
	{
		set_flags(set_should_verify_class(_flags(), value));
	}

	public boolean is_contended()
	{
		return is_contended(_flags());
	}

	public void set_is_contended(boolean value)
	{
		set_flags(set_is_contended(_flags(), value));
	}

	public boolean has_nonstatic_concrete_methods()
	{
		return has_nonstatic_concrete_methods(_flags());
	}

	public void set_has_nonstatic_concrete_methods(boolean value)
	{
		set_flags(set_has_nonstatic_concrete_methods(_flags(), value));
	}

	public boolean declares_nonstatic_concrete_methods()
	{
		return declares_nonstatic_concrete_methods(_flags());
	}

	public void set_declares_nonstatic_concrete_methods(boolean value)
	{
		set_flags(set_declares_nonstatic_concrete_methods(_flags(), value));
	}

	public boolean shared_loading_failed()
	{
		return shared_loading_failed(_flags());
	}

	public void set_shared_loading_failed(boolean value)
	{
		set_flags(set_shared_loading_failed(_flags(), value));
	}

	public boolean is_shared_boot_class()
	{
		return is_shared_boot_class(_flags());
	}

	public void set_is_shared_boot_class(boolean value)
	{
		set_flags(set_is_shared_boot_class(_flags(), value));
	}

	public boolean is_shared_platform_class()
	{
		return is_shared_platform_class(_flags());
	}

	public void set_is_shared_platform_class(boolean value)
	{
		set_flags(set_is_shared_platform_class(_flags(), value));
	}

	public boolean is_shared_app_class()
	{
		return is_shared_app_class(_flags());
	}

	public void set_is_shared_app_class(boolean value)
	{
		set_flags(set_is_shared_app_class(_flags(), value));
	}

	public boolean has_contended_annotations()
	{
		return has_contended_annotations(_flags());
	}

	public void set_has_contended_annotations(boolean value)
	{
		set_flags(set_has_contended_annotations(_flags(), value));
	}

	public boolean has_localvariable_table()
	{
		return has_localvariable_table(_flags());
	}

	public void set_has_localvariable_table(boolean value)
	{
		set_flags(set_has_localvariable_table(_flags(), value));
	}

	public boolean has_miranda_methods()
	{
		return has_miranda_methods(_flags());
	}

	public void set_has_miranda_methods(boolean value)
	{
		set_flags(set_has_miranda_methods(_flags(), value));
	}

	public boolean has_vanilla_constructor()
	{
		return has_vanilla_constructor(_flags());
	}

	public void set_has_vanilla_constructor(boolean value)
	{
		set_flags(set_has_vanilla_constructor(_flags(), value));
	}

	public boolean has_final_method()
	{
		return has_final_method(_flags());
	}

	public void set_has_final_method(boolean value)
	{
		set_flags(set_has_final_method(_flags(), value));
	}

	// _status
	public boolean is_being_redefined()
	{
		return is_being_redefined(_status());
	}

	public void set_is_being_redefined(boolean value)
	{
		set_status(set_is_being_redefined(_status(), value));
	}

	public boolean has_resolved_methods()
	{
		return has_resolved_methods(_status());
	}

	public void set_has_resolved_methods(boolean value)
	{
		set_status(set_has_resolved_methods(_status(), value));
	}

	public boolean has_been_redefined()
	{
		return has_been_redefined(_status());
	}

	public void set_has_been_redefined(boolean value)
	{
		set_status(set_has_been_redefined(_status(), value));
	}

	public boolean is_scratch_class()
	{
		return is_scratch_class(_status());
	}

	public void set_is_scratch_class(boolean value)
	{
		set_status(set_is_scratch_class(_status(), value));
	}

	public boolean is_marked_dependent()
	{
		return is_marked_dependent(_status());
	}

	public void set_is_marked_dependent(boolean value)
	{
		set_status(set_is_marked_dependent(_status(), value));
	}
}
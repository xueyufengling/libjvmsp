package jvmsp.hotspot.oops;

import jvmsp.memory;
import jvmsp.hotspot.vm_struct;
import jvmsp.type.cxx_type;

/**
 * 方法的运行时JVM标志
 */
public class MethodFlags extends vm_struct
{
	public static final String type_name = "MethodFlags";

	public static final cxx_type MethodFlags = cxx_type.define(type_name)
			.decl_field("_status", cxx_type.uint32_t)
			.resolve();

	public static final long size = MethodFlags.size();

	private static final long _status = MethodFlags.field("_status").offset();

	public static final int has_monitor_bytecodes = 1 << 0;
	public static final int has_jsrs = 1 << 1;
	public static final int is_old = 1 << 2;
	public static final int is_obsolete = 1 << 3;
	public static final int is_deleted = 1 << 4;
	public static final int is_prefixed_native = 1 << 5;
	public static final int monitor_matching = 1 << 6;
	public static final int queued_for_compilation = 1 << 7;
	public static final int is_not_c2_compilable = 1 << 8;
	public static final int is_not_c1_compilable = 1 << 9;
	public static final int is_not_c2_osr_compilable = 1 << 10;
	public static final int force_inline = 1 << 11;
	public static final int dont_inline = 1 << 12;
	public static final int has_loops_flag = 1 << 13;
	public static final int has_loops_flag_init = 1 << 14;
	public static final int on_stack_flag = 1 << 15;

	public MethodFlags(long address)
	{
		super(type_name, address);
	}

	public String toString()
	{
		return memory.bits_str(_status());
	}

	public int _status()
	{
		return super.read_int(_status);
	}

	public void set_status(int status)
	{
		super.write(_status, status);
	}

	public boolean is_has_monitor_bytecodes()
	{
		return memory.flag_bit(_status(), has_monitor_bytecodes);
	}

	public void set_has_monitor_bytecodes(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), has_monitor_bytecodes, value));
	}

	public boolean is_has_jsrs()
	{
		return memory.flag_bit(_status(), has_jsrs);
	}

	public void set_has_jsrs(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), has_jsrs, value));
	}

	public boolean is_old()
	{
		return memory.flag_bit(_status(), is_old);
	}

	public void set_is_old(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), is_old, value));
	}

	public boolean is_obsolete()
	{
		return memory.flag_bit(_status(), is_obsolete);
	}

	public void set_is_obsolete(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), is_obsolete, value));
	}

	public boolean is_deleted()
	{
		return memory.flag_bit(_status(), is_deleted);
	}

	public void set_is_deleted(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), is_deleted, value));
	}

	public boolean is_prefixed_native()
	{
		return memory.flag_bit(_status(), is_prefixed_native);
	}

	public void set_is_prefixed_native(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), is_prefixed_native, value));
	}

	public boolean is_monitor_matching()
	{
		return memory.flag_bit(_status(), monitor_matching);
	}

	public void set_monitor_matching(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), monitor_matching, value));
	}

	public boolean is_queued_for_compilation()
	{
		return memory.flag_bit(_status(), queued_for_compilation);
	}

	public void set_queued_for_compilation(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), queued_for_compilation, value));
	}

	public boolean is_not_c2_compilable()
	{
		return memory.flag_bit(_status(), is_not_c2_compilable);
	}

	public void set_is_not_c2_compilable(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), is_not_c2_compilable, value));
	}

	public boolean is_not_c1_compilable()
	{
		return memory.flag_bit(_status(), is_not_c1_compilable);
	}

	public void set_is_not_c1_compilable(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), is_not_c1_compilable, value));
	}

	public boolean is_not_c2_osr_compilable()
	{
		return memory.flag_bit(_status(), is_not_c2_osr_compilable);
	}

	public void set_is_not_c2_osr_compilable(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), is_not_c2_osr_compilable, value));
	}

	public boolean is_force_inline()
	{
		return memory.flag_bit(_status(), force_inline);
	}

	public void set_force_inline(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), force_inline, value));
	}

	public boolean is_dont_inline()
	{
		return memory.flag_bit(_status(), dont_inline);
	}

	public void set_dont_inline(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), dont_inline, value));
	}

	public boolean is_has_loops_flag()
	{
		return memory.flag_bit(_status(), has_loops_flag);
	}

	public void set_has_loops_flag(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), has_loops_flag, value));
	}

	public boolean is_has_loops_flag_init()
	{
		return memory.flag_bit(_status(), has_loops_flag_init);
	}

	public void set_has_loops_flag_init(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), has_loops_flag_init, value));
	}

	public boolean is_on_stack_flag()
	{
		return memory.flag_bit(_status(), on_stack_flag);
	}

	public void set_on_stack_flag(boolean value)
	{
		set_status(memory.set_flag_bit(_status(), on_stack_flag, value));
	}
}

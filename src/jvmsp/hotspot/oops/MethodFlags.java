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

	// 标志
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

	public static final boolean has_monitor_bytecodes(int flags)
	{
		return memory.flag_bit(flags, has_monitor_bytecodes);
	}

	public static final int set_has_monitor_bytecodes(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_monitor_bytecodes, value);
	}

	public static final boolean has_jsrs(int flags)
	{
		return memory.flag_bit(flags, has_jsrs);
	}

	public static final int set_has_jsrs(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_jsrs, value);
	}

	public static final boolean is_old(int flags)
	{
		return memory.flag_bit(flags, is_old);
	}

	public static final int set_is_old(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_old, value);
	}

	public static final boolean is_obsolete(int flags)
	{
		return memory.flag_bit(flags, is_obsolete);
	}

	public static final int set_is_obsolete(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_obsolete, value);
	}

	public static final boolean is_deleted(int flags)
	{
		return memory.flag_bit(flags, is_deleted);
	}

	public static final int set_is_deleted(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_deleted, value);
	}

	public static final boolean is_prefixed_native(int flags)
	{
		return memory.flag_bit(flags, is_prefixed_native);
	}

	public static final int set_is_prefixed_native(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_prefixed_native, value);
	}

	public static final boolean monitor_matching(int flags)
	{
		return memory.flag_bit(flags, monitor_matching);
	}

	public static final int set_monitor_matching(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, monitor_matching, value);
	}

	public static final boolean queued_for_compilation(int flags)
	{
		return memory.flag_bit(flags, queued_for_compilation);
	}

	public static final int set_queued_for_compilation(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, queued_for_compilation, value);
	}

	public static final boolean is_not_c2_compilable(int flags)
	{
		return memory.flag_bit(flags, is_not_c2_compilable);
	}

	public static final int set_is_not_c2_compilable(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_not_c2_compilable, value);
	}

	public static final boolean is_not_c1_compilable(int flags)
	{
		return memory.flag_bit(flags, is_not_c1_compilable);
	}

	public static final int set_is_not_c1_compilable(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_not_c1_compilable, value);
	}

	public static final boolean is_not_c2_osr_compilable(int flags)
	{
		return memory.flag_bit(flags, is_not_c2_osr_compilable);
	}

	public static final int set_is_not_c2_osr_compilable(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_not_c2_osr_compilable, value);
	}

	public static final boolean force_inline(int flags)
	{
		return memory.flag_bit(flags, force_inline);
	}

	public static final int set_force_inline(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, force_inline, value);
	}

	public static final boolean dont_inline(int flags)
	{
		return memory.flag_bit(flags, dont_inline);
	}

	public static final int set_dont_inline(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, dont_inline, value);
	}

	public static final boolean has_loops_flag(int flags)
	{
		return memory.flag_bit(flags, has_loops_flag);
	}

	public static final int set_has_loops_flag(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_loops_flag, value);
	}

	public static final boolean has_loops_flag_init(int flags)
	{
		return memory.flag_bit(flags, has_loops_flag_init);
	}

	public static final int set_has_loops_flag_init(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_loops_flag_init, value);
	}

	public static final boolean on_stack_flag(int flags)
	{
		return memory.flag_bit(flags, on_stack_flag);
	}

	public static final int set_on_stack_flag(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, on_stack_flag, value);
	}

	// 成员方法

	public boolean has_monitor_bytecodes()
	{
		return has_monitor_bytecodes(_status());
	}

	public void set_has_monitor_bytecodes(boolean value)
	{
		set_status(set_has_monitor_bytecodes(_status(), value));
	}

	public boolean has_jsrs()
	{
		return has_jsrs(_status());
	}

	public void set_has_jsrs(boolean value)
	{
		set_status(set_has_jsrs(_status(), value));
	}

	public boolean is_old()
	{
		return is_old(_status());
	}

	public void set_is_old(boolean value)
	{
		set_status(set_is_old(_status(), value));
	}

	public boolean is_obsolete()
	{
		return is_obsolete(_status());
	}

	public void set_is_obsolete(boolean value)
	{
		set_status(set_is_obsolete(_status(), value));
	}

	public boolean is_deleted()
	{
		return is_deleted(_status());
	}

	public void set_is_deleted(boolean value)
	{
		set_status(set_is_deleted(_status(), value));
	}

	public boolean is_prefixed_native()
	{
		return is_prefixed_native(_status());
	}

	public void set_is_prefixed_native(boolean value)
	{
		set_status(set_is_prefixed_native(_status(), value));
	}

	public boolean monitor_matching()
	{
		return monitor_matching(_status());
	}

	public void set_monitor_matching(boolean value)
	{
		set_status(set_monitor_matching(_status(), value));
	}

	public boolean queued_for_compilation()
	{
		return queued_for_compilation(_status());
	}

	public void set_queued_for_compilation(boolean value)
	{
		set_status(set_queued_for_compilation(_status(), value));
	}

	public boolean is_not_c2_compilable()
	{
		return is_not_c2_compilable(_status());
	}

	public void set_is_not_c2_compilable(boolean value)
	{
		set_status(set_is_not_c2_compilable(_status(), value));
	}

	public boolean is_not_c1_compilable()
	{
		return is_not_c1_compilable(_status());
	}

	public void set_is_not_c1_compilable(boolean value)
	{
		set_status(set_is_not_c1_compilable(_status(), value));
	}

	public boolean is_not_c2_osr_compilable()
	{
		return is_not_c2_osr_compilable(_status());
	}

	public void set_is_not_c2_osr_compilable(boolean value)
	{
		set_status(set_is_not_c2_osr_compilable(_status(), value));
	}

	public boolean force_inline()
	{
		return force_inline(_status());
	}

	public void set_force_inline(boolean value)
	{
		set_status(set_force_inline(_status(), value));
	}

	public boolean dont_inline()
	{
		return dont_inline(_status());
	}

	public void set_dont_inline(boolean value)
	{
		set_status(set_dont_inline(_status(), value));
	}

	public boolean has_loops_flag()
	{
		return has_loops_flag(_status());
	}

	public void set_has_loops_flag(boolean value)
	{
		set_status(set_has_loops_flag(_status(), value));
	}

	public boolean has_loops_flag_init()
	{
		return has_loops_flag_init(_status());
	}

	public void set_has_loops_flag_init(boolean value)
	{
		set_status(set_has_loops_flag_init(_status(), value));
	}

	public boolean on_stack_flag()
	{
		return on_stack_flag(_status());
	}

	public void set_on_stack_flag(boolean value)
	{
		set_status(set_on_stack_flag(_status(), value));
	}
}

package jvmsp.hotspot.runtime;

import jvmsp.hotspot.vm_struct;
import jvmsp.type.cxx_type;

public class StackOverflow extends vm_struct
{
	public static final String type_name = "StackOverflow";

	public static final cxx_type StackOverflow = cxx_type.define(type_name)
			.decl_field("_stack_guard_state", cxx_type._int)
			.decl_field("_stack_overflow_limit", cxx_type.pvoid)
			.decl_field("_reserved_stack_activation", cxx_type.pvoid)
			.decl_field("_shadow_zone_safe_limit", cxx_type.pvoid)
			.decl_field("_shadow_zone_growth_watermark", cxx_type.pvoid)
			.decl_field("_stack_base", cxx_type.pvoid)
			.decl_field("_stack_end", cxx_type.pvoid)
			.resolve();

	public static final long size = StackOverflow.size();

	private static final long _stack_guard_state = StackOverflow.field("_stack_guard_state").offset();
	public static final long _stack_overflow_limit = StackOverflow.field("_stack_overflow_limit").offset();
	private static final long _reserved_stack_activation = StackOverflow.field("_reserved_stack_activation").offset();
	private static final long _shadow_zone_safe_limit = StackOverflow.field("_shadow_zone_safe_limit").offset();
	private static final long _shadow_zone_growth_watermark = StackOverflow.field("_shadow_zone_growth_watermark").offset();
	private static final long _stack_base = StackOverflow.field("_stack_base").offset();
	private static final long _stack_end = StackOverflow.field("_stack_end").offset();

	public static abstract class StackGuardState
	{
		public static final int stack_guard_unused = 0;
		public static final int stack_guard_reserved_disabled = stack_guard_unused + 1;
		public static final int stack_guard_yellow_reserved_disabled = stack_guard_reserved_disabled + 1;
		public static final int stack_guard_enabled = stack_guard_yellow_reserved_disabled + 1;
	}

	public StackOverflow(long address)
	{
		super(type_name, address);
	}

	public int stack_guard_state()
	{
		return super.read_cint(_stack_guard_state);
	}

	public void set_stack_guard_state(int stack_guard_state)
	{
		super.write_cint(_stack_guard_state, stack_guard_state);
	}

	public long stack_overflow_limit()
	{
		return super.read_pointer(_stack_overflow_limit);
	}

	public void set_stack_overflow_limit(long stack_overflow_limit)
	{
		super.write_pointer(_stack_overflow_limit, stack_overflow_limit);
	}

	public long reserved_stack_activation()
	{
		return super.read_pointer(_reserved_stack_activation);
	}

	public void set_reserved_stack_activation(long reserved_stack_activation)
	{
		super.write_pointer(_reserved_stack_activation, reserved_stack_activation);
	}

	public long shadow_zone_safe_limit()
	{
		return super.read_pointer(_shadow_zone_safe_limit);
	}

	public void set_shadow_zone_safe_limit(long shadow_zone_safe_limit)
	{
		super.write_pointer(_shadow_zone_safe_limit, shadow_zone_safe_limit);
	}

	public long shadow_zone_growth_watermark()
	{
		return super.read_pointer(_shadow_zone_growth_watermark);
	}

	public void set_shadow_zone_growth_watermark(long shadow_zone_growth_watermark)
	{
		super.write_pointer(_shadow_zone_growth_watermark, shadow_zone_growth_watermark);
	}

	public long stack_base()
	{
		return super.read_pointer(_stack_base);
	}

	public void set_stack_base(long stack_base)
	{
		super.write_pointer(_stack_base, stack_base);
	}

	public long stack_end()
	{
		return super.read_pointer(_stack_end);
	}

	public void set_stack_end(long stack_end)
	{
		super.write_pointer(_stack_end, stack_end);
	}
}

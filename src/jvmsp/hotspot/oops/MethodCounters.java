package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;

public class MethodCounters extends Metadata
{
	private static final long _invoke_mask = vm_struct.entry.find("MethodCounters", "_invoke_mask").offset;
	private static final long _backedge_mask = vm_struct.entry.find("MethodCounters", "_backedge_mask").offset;
	private static final long _interpreter_throwout_count = vm_struct.entry.find("MethodCounters", "_interpreter_throwout_count").offset;
	private static final long _number_of_breakpoints = vm_struct.entry.find("MethodCounters", "_number_of_breakpoints").offset;
	private static final long _invocation_counter = vm_struct.entry.find("MethodCounters", "_invocation_counter").offset;
	private static final long _backedge_counter = vm_struct.entry.find("MethodCounters", "_backedge_counter").offset;

	public static final long size = sizeof("MethodCounters");
	
	public MethodCounters(long address)
	{
		super("MethodCounters", address);
	}

	@Override
	public final boolean is_methodCounters()
	{
		return true;
	}
}
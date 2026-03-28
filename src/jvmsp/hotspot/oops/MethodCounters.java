package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.MetaspaceObj;

public class MethodCounters extends Metadata
{
	public static final String type_name = "MethodCounters";
	public static final long size = sizeof(type_name);

	private static final long _invoke_mask = vm_struct.entry.find(type_name, "_invoke_mask").offset;
	private static final long _backedge_mask = vm_struct.entry.find(type_name, "_backedge_mask").offset;
	private static final long _interpreter_throwout_count = vm_struct.entry.find(type_name, "_interpreter_throwout_count").offset;
	private static final long _number_of_breakpoints = vm_struct.entry.find(type_name, "_number_of_breakpoints").offset;
	private static final long _invocation_counter = vm_struct.entry.find(type_name, "_invocation_counter").offset;
	private static final long _backedge_counter = vm_struct.entry.find(type_name, "_backedge_counter").offset;

	public MethodCounters(long address)
	{
		super(type_name, address);
	}

	@Override
	public final boolean is_methodCounters()
	{
		return true;
	}

	@Override
	public long size()
	{
		return size;
	}

	@Override
	public int type()
	{
		return MetaspaceObj.Type.MethodCountersType;
	}

	@Override
	public String internal_name()
	{
		return "{method counters}";
	}
}
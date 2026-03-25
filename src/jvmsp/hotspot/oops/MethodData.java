package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;

public class MethodData extends Metadata
{
	private static final long _size = vm_struct.entry.find("MethodData", "_size").offset;
	private static final long _method = vm_struct.entry.find("MethodData", "_method").offset;
	private static final long _data_size = vm_struct.entry.find("MethodData", "_data_size").offset;
	private static final long _data_0 = vm_struct.entry.find("MethodData", "_data[0]").offset;
	private static final long _parameters_type_data_di = vm_struct.entry.find("MethodData", "_parameters_type_data_di").offset;
	private static final long _compiler_counters_nof_decompiles = vm_struct.entry.find("MethodData", "_compiler_counters._nof_decompiles").offset;
	private static final long _compiler_counters_nof_overflow_recompiles = vm_struct.entry.find("MethodData", "_compiler_counters._nof_overflow_recompiles").offset;
	private static final long _compiler_counters_nof_overflow_traps = vm_struct.entry.find("MethodData", "_compiler_counters._nof_overflow_traps").offset;
	private static final long _compiler_counters_trap_hist_array_0 = vm_struct.entry.find("MethodData", "_compiler_counters._trap_hist._array[0]").offset;
	private static final long _eflags = vm_struct.entry.find("MethodData", "_eflags").offset;
	private static final long _arg_local = vm_struct.entry.find("MethodData", "_arg_local").offset;
	private static final long _arg_stack = vm_struct.entry.find("MethodData", "_arg_stack").offset;
	private static final long _arg_returned = vm_struct.entry.find("MethodData", "_arg_returned").offset;
	private static final long _tenure_traps = vm_struct.entry.find("MethodData", "_tenure_traps").offset;
	private static final long _invoke_mask = vm_struct.entry.find("MethodData", "_invoke_mask").offset;
	private static final long _backedge_mask = vm_struct.entry.find("MethodData", "_backedge_mask").offset;

	public MethodData(long address)
	{
		super("MethodData", address);
	}

	@Override
	public final boolean is_methodData()
	{
		return true;
	}

}
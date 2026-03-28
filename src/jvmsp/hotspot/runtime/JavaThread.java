package jvmsp.hotspot.runtime;

import jvmsp.hotspot.vm_struct;

public class JavaThread extends Thread
{
	public static final String type_name = "JavaThread";
	public static final long size = sizeof(type_name);

	private static final long _lock_stack = vm_struct.entry.find(type_name, "_lock_stack").offset;
	private static final long _threadObj = vm_struct.entry.find(type_name, "_threadObj").offset;
	private static final long _vthread = vm_struct.entry.find(type_name, "_vthread").offset;
	private static final long _jvmti_vthread = vm_struct.entry.find(type_name, "_jvmti_vthread").offset;
	private static final long _scopedValueCache = vm_struct.entry.find(type_name, "_scopedValueCache").offset;
	private static final long _anchor = vm_struct.entry.find(type_name, "_anchor").offset;
	private static final long _vm_result = vm_struct.entry.find(type_name, "_vm_result").offset;
	private static final long _vm_result_2 = vm_struct.entry.find(type_name, "_vm_result_2").offset;
	private static final long _current_pending_monitor = vm_struct.entry.find(type_name, "_current_pending_monitor").offset;
	private static final long _current_pending_monitor_is_from_java = vm_struct.entry.find(type_name, "_current_pending_monitor_is_from_java").offset;
	private static final long _current_waiting_monitor = vm_struct.entry.find(type_name, "_current_waiting_monitor").offset;
	private static final long _suspend_flags = vm_struct.entry.find(type_name, "_suspend_flags").offset;
	private static final long _exception_oop = vm_struct.entry.find(type_name, "_exception_oop").offset;
	private static final long _exception_pc = vm_struct.entry.find(type_name, "_exception_pc").offset;
	private static final long _is_method_handle_return = vm_struct.entry.find(type_name, "_is_method_handle_return").offset;
	private static final long _saved_exception_pc = vm_struct.entry.find(type_name, "_saved_exception_pc").offset;
	private static final long _thread_state = vm_struct.entry.find(type_name, "_thread_state").offset;
	private static final long _osthread = vm_struct.entry.find(type_name, "_osthread").offset;
	private static final long _stack_base = vm_struct.entry.find(type_name, "_stack_base").offset;
	private static final long _stack_size = vm_struct.entry.find(type_name, "_stack_size").offset;
	private static final long _vframe_array_head = vm_struct.entry.find(type_name, "_vframe_array_head").offset;
	private static final long _vframe_array_last = vm_struct.entry.find(type_name, "_vframe_array_last").offset;
	private static final long _active_handles = vm_struct.entry.find(type_name, "_active_handles").offset;
	private static final long _terminated = vm_struct.entry.find(type_name, "_terminated").offset;

	protected JavaThread(String name, long address)
	{
		super(name, address);
	}

	public JavaThread(long address)
	{
		super(type_name, address);
	}
}
package jvmsp.hotspot.runtime;

import jvmsp.memory;
import jvmsp.type.cxx_type;
import jvmsp.hotspot.vm_constant;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.oops.Metadata;
import jvmsp.hotspot.oops.OopHandle;

/**
 * Java线程
 */
public class JavaThread extends Thread
{
	public static final String type_name = "JavaThread";
	public static final long size = sizeof(type_name);

	private static final long _threadObj = vm_struct.entry.find(type_name, "_threadObj").offset; // 888
	private static final long _vthread = vm_struct.entry.find(type_name, "_vthread").offset; // 896
	private static final long _jvmti_vthread = vm_struct.entry.find(type_name, "_jvmti_vthread").offset; // 904
	private static final long _scopedValueCache = vm_struct.entry.find(type_name, "_scopedValueCache").offset; // 912
	private static final long _anchor = vm_struct.entry.find(type_name, "_anchor").offset; // 920
	private static final long _jni_environment = vm_struct.entry.find(type_name, "_jni_environment").offset; // 952
	private static final long _vframe_array_head = vm_struct.entry.find(type_name, "_vframe_array_head").offset; // 976
	private static final long _vframe_array_last = vm_struct.entry.find(type_name, "_vframe_array_last").offset; // 984
	private static final long _vm_result_oop = switch_address(// 1008
			() -> vm_struct.entry.find(type_name, "_vm_result").address, // JDK21
			() -> vm_struct.entry.find(type_name, "_vm_result_oop").address// JDK25
	);
	private static final long _vm_result_metadata = switch_address(// 1016
			() -> vm_struct.entry.find(type_name, "_vm_result_2").address, // JDK21
			() -> vm_struct.entry.find(type_name, "_vm_result_metadata").address// JDK25
	);
	private static final long _current_pending_monitor = vm_struct.entry.find(type_name, "_current_pending_monitor").offset; // 1040
	private static final long _current_pending_monitor_is_from_java = vm_struct.entry.find(type_name, "_current_pending_monitor_is_from_java").offset; // 1048
	private static final long _current_waiting_monitor = vm_struct.entry.find(type_name, "_current_waiting_monitor").offset; // 1056
	private static final long _active_handles = vm_struct.entry.find(type_name, "_active_handles").offset; // 1064
	private static final long _suspend_flags = vm_struct.entry.find(type_name, "_suspend_flags").offset; // 1096
	private static final long _thread_state = vm_struct.entry.find(type_name, "_thread_state").offset; // 1100

	private static final long _saved_exception_pc = vm_struct.entry.find(type_name, "_saved_exception_pc").offset; // 1128
	private static final long _terminated = vm_struct.entry.find(type_name, "_terminated").offset; // 1136
	private static final long _is_in_VTMS_transition = vm_struct.entry.find(type_name, "_is_in_VTMS_transition").offset; // 1147
	private static final long _doing_unsafe_access = vm_struct.entry.find(type_name, "_doing_unsafe_access").offset; // 1144
	private static final long _is_in_tmp_VTMS_transition = vm_struct.entry.find(type_name, "_is_in_tmp_VTMS_transition").offset; // 1148
	private static final long _pending_deoptimization = vm_struct.entry.find(type_name, "_pending_deoptimization").offset; // 1156
	private static final long _pending_transfer_to_interpreter = vm_struct.entry.find(type_name, "_pending_transfer_to_interpreter").offset; // 1161
	private static final long _pending_failed_speculation = vm_struct.entry.find(type_name, "_pending_failed_speculation").offset; // 1168
	private static final long _jvmci_counters = vm_struct.entry.find(type_name, "_jvmci_counters").offset; // 1192
	private static final long _jvmci_reserved0 = vm_struct.entry.find(type_name, "_jvmci_reserved0").offset; // 1200
	private static final long _jvmci_reserved1 = vm_struct.entry.find(type_name, "_jvmci_reserved1").offset; // 1208
	private static final long _jvmci_reserved_oop0 = vm_struct.entry.find(type_name, "_jvmci_reserved_oop0").offset; // 1216

	private static final long _stack_overflow_state_stack_overflow_limit = vm_struct.entry.find(type_name, "_stack_overflow_state._stack_overflow_limit").offset; // 1232
	private static final long _stack_overflow_state_reserved_stack_activation = vm_struct.entry.find(type_name, "_stack_overflow_state._reserved_stack_activation").offset; // 1240
	// StackOverflow _stack_overflow_state;
	private static final long _stack_overflow_state = _stack_overflow_state_stack_overflow_limit - StackOverflow._stack_overflow_limit;

	private static final long _exception_oop = vm_struct.entry.find(type_name, "_exception_oop").offset; // 1280
	private static final long _exception_pc = vm_struct.entry.find(type_name, "_exception_pc").offset; // 1288
	private static final long _exception_handler_pc = _exception_pc + cxx_type.pvoid.size();
	private static final long _is_method_handle_return = vm_struct.entry.find(type_name, "_is_method_handle_return").offset; // 1304
	private static final long _held_monitor_count = vm_struct.entry.find(type_name, "_held_monitor_count").offset; // 1360
	private static final long _should_post_on_exceptions_flag = vm_struct.entry.find(type_name, "_should_post_on_exceptions_flag").offset; // 1508
	private static final long _lock_stack = vm_struct.entry.find(type_name, "_lock_stack").offset; // 1560

	protected JavaThread(String name, long address)
	{
		super(name, address);
	}

	public JavaThread(long address)
	{
		super(type_name, address);
	}

	public OopHandle threadObj()
	{
		return super.read_memory_object_ptr(OopHandle.class, _threadObj);
	}

	public void set_threadObj(OopHandle threadObj)
	{
		super.write_memory_object_ptr(_threadObj, threadObj);
	}

	public OopHandle vthread()
	{
		return super.read_memory_object_ptr(OopHandle.class, _vthread);
	}

	public void set_vthread(OopHandle vthread)
	{
		super.write_memory_object_ptr(_vthread, vthread);
	}

	public OopHandle jvmti_vthread()
	{
		return super.read_memory_object_ptr(OopHandle.class, _jvmti_vthread);
	}

	public void set_jvmti_vthread(OopHandle jvmti_vthread)
	{
		super.write_memory_object_ptr(_jvmti_vthread, jvmti_vthread);
	}

	public OopHandle scopedValueCache()
	{
		return super.read_memory_object_ptr(OopHandle.class, _scopedValueCache);
	}

	public void set_scopedValueCache(OopHandle scopedValueCache)
	{
		super.write_memory_object_ptr(_scopedValueCache, scopedValueCache);
	}

	/**
	 * 获取当前线程执行的方法栈帧标记
	 * 
	 * @return
	 */
	public JavaFrameAnchor anchor()
	{
		return super.read_memory_object_ptr(JavaFrameAnchor.class, _anchor);
	}

	public void set_anchor(JavaFrameAnchor anchor)
	{
		super.write_memory_object_ptr(_anchor, anchor);
	}

	/**
	 * 获取JNIEnv，该结构体仅包含一个JNINativeInterface_*指针。
	 * 
	 * @return
	 */
	public long jni_environment()
	{
		return super.read_ptr(_jni_environment);
	}

	public void set_jni_environment(long jni_environment)
	{
		super.write_ptr(_jni_environment, jni_environment);
	}

	/**
	 * 当前线程启用的vframeArray*指针
	 * 
	 * @return
	 */
	public long _vframe_array_head()
	{
		return super.read_ptr(_vframe_array_head);
	}

	public void set_vframe_array_head(long vframe_array_head)
	{
		super.write_ptr(_vframe_array_head, vframe_array_head);
	}

	/**
	 * 当前线程持有的最后一个vframeArray*指针
	 * 
	 * @return
	 */
	public long _vframe_array_last()
	{
		return super.read_ptr(_vframe_array_last);
	}

	public void set_vframe_array_last(long vframe_array_last)
	{
		super.write_ptr(_vframe_array_last, vframe_array_last);
	}

	public int vm_result_oop()
	{
		return super.read_int(_vm_result_oop);
	}

	public void set_vm_result_oop(long vm_result_oop)
	{
		super.write_ptr(_vm_result_oop, vm_result_oop);
	}

	public long _vm_result_metadata()
	{
		return super.read_ptr(_vm_result_metadata);
	}

	public void set_vm_result_metadata(long vm_result_metadata)
	{
		super.write_ptr(_vm_result_metadata, vm_result_metadata);
	}

	public Metadata vm_result_metadata()
	{
		return super.read_memory_object_ptr(Metadata.class, _vm_result_metadata);
	}

	/**
	 * 本线程等待的锁
	 * 
	 * @return
	 */
	public ObjectMonitor current_pending_monitor()
	{
		return super.read_memory_object_ptr(ObjectMonitor.class, _current_pending_monitor);
	}

	public void set_current_pending_monitor(ObjectMonitor current_pending_monitor)
	{
		super.write_memory_object_ptr(_current_pending_monitor, current_pending_monitor);
	}

	public boolean current_pending_monitor_is_from_java()
	{
		return super.read_cbool(_current_pending_monitor_is_from_java);
	}

	public void set_current_pending_monitor_is_from_java(boolean current_pending_monitor_is_from_java)
	{
		super.write_cbool(_current_pending_monitor_is_from_java, current_pending_monitor_is_from_java);
	}

	/**
	 * 本线程调用Object.wait()的ObjectMonitor
	 * 
	 * @return
	 */
	public ObjectMonitor current_waiting_monitor()
	{
		return super.read_memory_object_ptr(ObjectMonitor.class, _current_waiting_monitor);
	}

	public void set_current_waiting_monitor(ObjectMonitor current_waiting_monitor)
	{
		super.write_memory_object_ptr(_current_waiting_monitor, current_waiting_monitor);
	}

	/**
	 * 链式存储的JNIHandleBlock链表
	 * 
	 * @return
	 */
	public JNIHandleBlock active_handles()
	{
		return super.read_memory_object_ptr(JNIHandleBlock.class, _active_handles);
	}

	public void set_active_handles(JNIHandleBlock active_handles)
	{
		super.write_memory_object_ptr(_active_handles, active_handles);
	}

	public static final int _obj_deopt = 0x00000008;

	/**
	 * 线程挂起相关的标志
	 * 
	 * @return
	 */
	public int suspend_flags()
	{
		return super.read_int(_suspend_flags);
	}

	public void set_suspend_flags(int suspend_flags)
	{
		super.write(_suspend_flags, suspend_flags);
	}

	public void set_obj_deopt_flag(boolean obj_deopt)
	{
		set_suspend_flags(memory.set_flag_bit(suspend_flags(), _obj_deopt, obj_deopt));
	}

	public void set_obj_deopt_flag()
	{
		set_obj_deopt_flag(true);
	}

	public void clear_obj_deopt_flag()
	{
		set_obj_deopt_flag(false);
	}

	public boolean is_obj_deopt_suspend()
	{
		return memory.flag_bit(suspend_flags(), _obj_deopt);
	}

	/**
	 * 线程当前状态，定义于JavaThreadState。<br>
	 * 
	 * @return
	 */
	public int thread_state()
	{
		return super.read_cint(_thread_state);
	}

	public void set_thread_state(int thread_state)
	{
		super.write_cint(_thread_state, thread_state);
	}

	/**
	 * 线程保存的异常PC值
	 * 
	 * @return
	 */
	public long saved_exception_pc()
	{
		return super.read_ptr(_saved_exception_pc);
	}

	public void set_saved_exception_pc(long saved_exception_pc)
	{
		super.write_ptr(_saved_exception_pc, saved_exception_pc);
	}

	public static abstract class TerminatedTypes
	{
		public static final int _not_terminated = vm_constant.find_int("JavaThread::_not_terminated");// 0xDEAD - 3;
		public static final int _thread_exiting = vm_constant.find_int("JavaThread::_thread_exiting");// _not_terminated + 1;
		public static final int _thread_gc_barrier_detached = _thread_exiting + 1;
		public static final int _thread_terminated = _thread_gc_barrier_detached + 1;
		public static final int _vm_exited = _thread_terminated + 1;
	}

	/**
	 * 线程结束的状态，返回TerminatedTypes中定义的值之一。<br>
	 * 
	 * @return
	 */
	public int terminated()
	{
		return super.read_cint(_terminated);
	}

	public void set_terminated(int terminated)
	{
		super.write_cint(_terminated, terminated);
	}

	/**
	 * 线程是否可能因为unsafe访问失败。<br>
	 * 
	 * @return
	 */
	public boolean doing_unsafe_access()
	{
		return super.read_cbool(_doing_unsafe_access);
	}

	public void set_doing_unsafe_access(boolean doing_unsafe_access)
	{
		super.write_cbool(_doing_unsafe_access, doing_unsafe_access);
	}

	// thread is in virtual thread mount state transition
	public boolean is_in_VTMS_transition()
	{
		return super.read_cbool(_is_in_VTMS_transition);
	}

	public void set_is_in_VTMS_transition(boolean is_in_VTMS_transition)
	{
		super.write_cbool(_is_in_VTMS_transition, is_in_VTMS_transition);
	}

	// JDK21独有，JDK25+移除
	public boolean is_in_tmp_VTMS_transition()
	{
		return super.read_cbool(_is_in_tmp_VTMS_transition);
	}

	public void set_is_in_tmp_VTMS_transition(boolean is_in_tmp_VTMS_transition)
	{
		super.write_cbool(_is_in_tmp_VTMS_transition, is_in_tmp_VTMS_transition);
	}

	// Communicates the DeoptReason and DeoptAction of the uncommon trap
	public int pending_deoptimization()
	{
		return super.read_cint(_pending_deoptimization);
	}

	public void set_pending_deoptimization(int pending_deoptimization)
	{
		super.write_cint(_pending_deoptimization, pending_deoptimization);
	}

	// Specifies if the DeoptReason for the last uncommon trap was Reason_transfer_to_interpreter
	public boolean pending_transfer_to_interpreter()
	{
		return super.read_cbool(_pending_transfer_to_interpreter);
	}

	public void set_pending_transfer_to_interpreter(boolean pending_transfer_to_interpreter)
	{
		super.write_cbool(_pending_transfer_to_interpreter, pending_transfer_to_interpreter);
	}

	// An id of a speculation that JVMCI compiled code can use to further describe and
	// uniquely identify the speculative optimization guarded by an uncommon trap.
	// See JVMCINMethodData::SPECULATION_LENGTH_BITS for further details.
	public long pending_failed_speculation()
	{
		return super.read_long(_pending_failed_speculation);
	}

	public void set_pending_failed_speculation(long pending_failed_speculation)
	{
		super.write(_pending_failed_speculation, pending_failed_speculation);
	}

	// Support for high precision, thread sensitive counters in JVMCI compiled code.
	// jlong* _jvmci_counters;
	public long jvmci_counters()
	{
		return super.read_ptr(_jvmci_counters);
	}

	public void set_jvmci_counters(long jvmci_counters)
	{
		super.write_ptr(_jvmci_counters, jvmci_counters);
	}

	public long jvmci_reserved0()
	{
		return super.read_long(_jvmci_reserved0);
	}

	public void set_jvmci_reserved0(long jvmci_reserved0)
	{
		super.write(_jvmci_reserved0, jvmci_reserved0);
	}

	public long jvmci_reserved1()
	{
		return super.read_long(_jvmci_reserved1);
	}

	public void set_jvmci_reserved1(long jvmci_reserved1)
	{
		super.write(_jvmci_reserved1, jvmci_reserved1);
	}

	public int jvmci_reserved_oop0()
	{
		return super.read_int(_jvmci_reserved_oop0);
	}

	public void set_jvmci_reserved_oop0(int jvmci_reserved_oop0)
	{
		super.write(_jvmci_reserved_oop0, jvmci_reserved_oop0);
	}

	// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/runtime/javaThread.hpp
	// StackOverflow _stack_overflow_state;
	public long stack_overflow_limit()
	{
		return super.read_ptr(_stack_overflow_state_stack_overflow_limit);
	}

	public void set_stack_overflow_limit(long stack_overflow_limit)
	{
		super.write_ptr(_stack_overflow_state_stack_overflow_limit, stack_overflow_limit);
	}

	public long reserved_stack_activation()
	{
		return super.read_ptr(_stack_overflow_state_reserved_stack_activation);
	}

	public void set_reserved_stack_activation(long reserved_stack_activation)
	{
		super.write_ptr(_stack_overflow_state_reserved_stack_activation, reserved_stack_activation);
	}

	public StackOverflow stack_overflow_state()
	{
		return super.read_memory_object(StackOverflow.class, _stack_overflow_state);
	}

	public int exception_oop()
	{
		return super.read_int(_exception_oop);
	}

	public void set_exception_oop(int exception_oop)
	{
		super.write(_exception_oop, exception_oop);
	}

	/**
	 * 异常发生的PC值
	 * 
	 * @return
	 */
	public long exception_pc()
	{
		return super.read_ptr(_exception_pc);
	}

	public void set_exception_pc(long exception_pc)
	{
		super.write_ptr(_exception_pc, exception_pc);
	}

	public long exception_handler_pc()
	{
		return super.read_ptr(_exception_handler_pc);
	}

	public void set_exception_handler_pc(long exception_handler_pc)
	{
		super.write_ptr(_exception_handler_pc, exception_handler_pc);
	}

	// volatile int _is_method_handle_return;
	// true (== 1) if the current exception PC is a MethodHandle call site.
	public boolean is_method_handle_return()
	{
		return super.read_cbool(_is_method_handle_return);
	}

	public void set_is_method_handle_return(boolean is_method_handle_return)
	{
		super.write_cbool(_is_method_handle_return, is_method_handle_return);
	}

	public long held_monitor_count()
	{
		return super.read_long(_held_monitor_count);
	}

	public void set_held_monitor_count(long held_monitor_count)
	{
		super.write(_held_monitor_count, held_monitor_count);
	}

	public int should_post_on_exceptions_flag()
	{
		return super.read_cint(_should_post_on_exceptions_flag);
	}

	public void set_should_post_on_exceptions_flag(int should_post_on_exceptions_flag)
	{
		super.write_cint(_should_post_on_exceptions_flag, should_post_on_exceptions_flag);
	}

	public LockStack lock_stack()
	{
		return super.read_memory_object(LockStack.class, _lock_stack);
	}
}
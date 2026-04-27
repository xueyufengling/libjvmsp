package jvmsp.hotspot.runtime;

import jvmsp.hotspot.vm_constant;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.enum_type;

/**
 * 操作系统线程基类。<br>
 * https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/runtime/osThreadBase.hpp<br>
 */
public abstract class OSThreadBase extends CHeapObj
{
	public static final String type_name = "OSThreadBase";
	public static final long size = sizeof(type_name);

	// _state实际上是OSThreadBase的字段
	private static final long _state = vm_struct.entry.find(OSThread.type_name, "_state").offset;

	protected OSThreadBase(String name, long address)
	{
		super(name, address);
	}

	public static abstract class ThreadState
	{
		public static final int ALLOCATED = vm_constant.find_int("ALLOCATED"); // 0 Memory has been allocated but not initialized
		public static final int INITIALIZED = vm_constant.find_int("INITIALIZED");// 1 The thread has been initialized but yet started
		public static final int RUNNABLE = vm_constant.find_int("RUNNABLE"); // 2 Has been started and is runnable, but not necessarily running
		public static final int MONITOR_WAIT = vm_constant.find_int("MONITOR_WAIT");// 3 Waiting on a contended monitor lock
		public static final int CONDVAR_WAIT = vm_constant.find_int("CONDVAR_WAIT");// 4 Waiting on a condition variable
		public static final int OBJECT_WAIT = vm_constant.find_int("OBJECT_WAIT");// 5 Waiting on an Object.wait() call
		public static final int BREAKPOINTED = vm_constant.find_int("BREAKPOINTED");// 6 Suspended at breakpoint
		public static final int SLEEPING = vm_constant.find_int("SLEEPING");// 7 Thread.sleep()
		public static final int ZOMBIE = vm_constant.find_int("ZOMBIE"); // 8 All done, but not reclaimed yet
	};

	/**
	 * 获取OS线程当前的状态，返回值必定是ThreadState中定义的值之一。<br>
	 * 
	 * @return
	 */
	public int state()
	{
		return super.read_cint(_state);
	}

	public void set_state(int state)
	{
		super.write_cint(_state, state);
	}
}
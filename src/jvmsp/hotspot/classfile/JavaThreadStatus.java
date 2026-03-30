package jvmsp.hotspot.classfile;

import jvmsp.hotspot.vm_constant;

public abstract class JavaThreadStatus
{
	public static final int JavaThreadStatus_NEW = vm_constant.find_int("JavaThreadStatus::NEW");
	public static final int JavaThreadStatus_RUNNABLE = vm_constant.find_int("JavaThreadStatus::RUNNABLE");
	public static final int JavaThreadStatus_SLEEPING = vm_constant.find_int("JavaThreadStatus::SLEEPING");
	public static final int JavaThreadStatus_IN_OBJECT_WAIT = vm_constant.find_int("JavaThreadStatus::IN_OBJECT_WAIT");
	public static final int JavaThreadStatus_IN_OBJECT_WAIT_TIMED = vm_constant.find_int("JavaThreadStatus::IN_OBJECT_WAIT_TIMED");
	public static final int JavaThreadStatus_PARKED = vm_constant.find_int("JavaThreadStatus::PARKED");
	public static final int JavaThreadStatus_PARKED_TIMED = vm_constant.find_int("JavaThreadStatus::PARKED_TIMED");
	public static final int JavaThreadStatus_BLOCKED_ON_MONITOR_ENTER = vm_constant.find_int("JavaThreadStatus::BLOCKED_ON_MONITOR_ENTER");
	public static final int JavaThreadStatus_TERMINATED = vm_constant.find_int("JavaThreadStatus::TERMINATED");
}

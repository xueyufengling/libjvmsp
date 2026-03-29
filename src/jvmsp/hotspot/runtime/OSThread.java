package jvmsp.hotspot.runtime;

import jvmsp.virtual_machine;
import jvmsp.hotspot.vm_struct;

/**
 * 操作系统线程，不同平台实现不同。<br>
 * https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/os/windows/osThread_windows.hpp<br>
 * https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/os/linux/osThread_linux.hpp<br>
 * https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/os/bsd/osThread_bsd.hpp<br>
 */
public class OSThread extends OSThreadBase
{
	public static final String type_name = "OSThread";
	public static final long size = sizeof(type_name);

	private static final long _thread_id = vm_struct.entry.find(type_name, "_thread_id").offset;
	private static final long _thread_handle = vm_struct.entry.find(type_name, "_thread_handle").offset;

	public OSThread(long address)
	{
		super(type_name, address);
	}

	/**
	 * 线程ID，不同操作系统的线程1ID大小不同。<br>
	 * macos是64位无符号整数。<br>
	 * linux、windows为32位整数。<br>
	 * 
	 * @return
	 */
	public long thread_id()
	{
		switch (virtual_machine.platform.host)
		{
		case linux:// pid_t
		case windows:// unsigned long
			return super.read_int(_thread_id);
		case macos:// bsd系统的一种，typedef struct thread *thread_t;
			return super.read_long(_thread_id);
		default:
			return 0;
		}
	}

	public void set_thread_id(long thread_id)
	{
		switch (virtual_machine.platform.host)
		{
		case linux:
		case windows:
			super.write(_thread_id, (int) thread_id);
		case macos:
			super.write(_thread_id, thread_id);
		}
	}

	/**
	 * 线程的句柄，一般是指针大小。
	 * 
	 * @return
	 */
	public long thread_handle()
	{
		return super.read_pointer(_thread_handle);
	}

	public void set_thread_handle(long thread_handle)
	{
		super.write_pointer(_thread_handle, thread_handle);
	}
}
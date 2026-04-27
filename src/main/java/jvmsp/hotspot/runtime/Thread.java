package jvmsp.hotspot.runtime;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.gc.shared.ThreadLocalAllocBuffer;
import jvmsp.hotspot.jfr.support.JfrThreadLocal;
import jvmsp.hotspot.memory.ResourceArea;
import jvmsp.hotspot.utilities.ThreadShadow;

public class Thread extends ThreadShadow
{
	public static final String type_name = "Thread";
	public static final long size = sizeof(type_name);

	// thread_local的eden空间
	private static final long _tlab = vm_struct.entry.find(type_name, "_tlab").offset;// 432
	private static final long _allocated_bytes = vm_struct.entry.find(type_name, "_allocated_bytes").offset;// 552
	// JFR only
	// #define DEFINE_THREAD_LOCAL_FIELD_JFR mutable JfrThreadLocal _jfr_thread_local
	private static final long _jfr_thread_local = vm_struct.entry.find(type_name, "_jfr_thread_local").offset;// 584
	private static final long _resource_area = vm_struct.entry.find(type_name, "_resource_area").offset;// 792

	// 从派生类中获取基类字段偏移量
	private static final long _osthread = vm_struct.entry.find(JavaThread.type_name, "_osthread").offset; // 784
	private static final long _stack_base = vm_struct.entry.find(type_name, "_stack_base").offset; // 816
	private static final long _stack_size = vm_struct.entry.find(type_name, "_stack_size").offset; // 824
	private static final long _poll_data = vm_struct.entry.find(type_name, "_poll_data").offset; // 1104

	protected Thread(String name, long address)
	{
		super(name, address);
	}

	public Thread(long address)
	{
		this(type_name, address);
	}

	public ThreadLocalAllocBuffer tlab()
	{
		return super.read_memory_object_ptr(ThreadLocalAllocBuffer.class, _tlab);
	}

	public void set_tlab(ThreadLocalAllocBuffer tlab)
	{
		super.write_memory_object_ptr(_tlab, tlab);
	}

	/**
	 * 已分配的字节数。<br>
	 * 注意，返回值是无符号uint64_t整数！
	 * 
	 * @return
	 */
	public long allocated_bytes()
	{
		return super.read_long(_allocated_bytes);
	}

	public void set_allocated_bytes(long allocated_bytes)
	{
		super.write(_allocated_bytes, allocated_bytes);
	}

	public JfrThreadLocal jfr_thread_local()
	{
		return super.read_memory_object(JfrThreadLocal.class, _jfr_thread_local);
	}

	public OSThread osthread()
	{
		return super.read_memory_object_ptr(OSThread.class, _osthread);
	}

	public void set_osthread(OSThread osthread)
	{
		super.write_memory_object_ptr(_osthread, osthread);
	}

	public ResourceArea resource_area()
	{
		return super.read_memory_object_ptr(ResourceArea.class, _resource_area);
	}

	public void set_tlab(ResourceArea resource_area)
	{
		super.write_memory_object_ptr(_resource_area, resource_area);
	}

	/**
	 * 线程栈的起始地址。<br>
	 * 
	 * @return
	 */
	public long stack_base()
	{
		return super.read_ptr(_stack_base);
	}

	public void set_stack_base(long stack_base)
	{
		super.write_ptr(_stack_base, stack_base);
	}

	/**
	 * 栈大小，返回值为size_t类型。<br>
	 * 
	 * @return
	 */
	public long stack_size()
	{
		return super.read_long(_stack_size);
	}

	public void set_stack_size(long stack_size)
	{
		super.write(_stack_size, stack_size);
	}

	/**
	 * 栈是反向生长的，地址越小的反而位置靠前。<br>
	 * 
	 * @return
	 */
	public long stack_end()
	{
		return stack_base() - stack_size();
	}

	public long poll_data()
	{
		return super.read_ptr(_poll_data);
	}

	public void set_poll_data(long poll_data)
	{
		super.write_ptr(_poll_data, poll_data);
	}
}
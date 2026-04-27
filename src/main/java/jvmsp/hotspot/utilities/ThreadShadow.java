package jvmsp.hotspot.utilities;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;

public class ThreadShadow extends CHeapObj
{
	public static final String type_name = "ThreadShadow";
	public static final long size = sizeof(type_name);

	private static final long _pending_exception = vm_struct.entry.find(type_name, "_pending_exception").offset;
	private static final long _exception_file = vm_struct.entry.find(type_name, "_exception_file").offset;
	private static final long _exception_line = vm_struct.entry.find(type_name, "_exception_line").offset;

	protected ThreadShadow(String name, long address)
	{
		super(name, address);
	}

	public ThreadShadow(long address)
	{
		super(type_name, address);
	}

	/**
	 * 线程GC操作，类型为oop
	 * 
	 * @return
	 */
	public int pending_exception()
	{
		return super.read_int(_pending_exception);
	}

	public void set_pending_exception(int pending_exception)
	{
		super.write_cint(_pending_exception, pending_exception);
	}

	public boolean has_pending_exception()
	{
		return pending_exception() != 0;
	}

	public void set_pending_exception(int pending_exception, String file, int line)
	{
		set_pending_exception(pending_exception);
		set_exception_file(file);
		set_exception_line(line);
	}

	void clear_pending_exception()
	{
		set_pending_exception(0, null, 0);
	}

	/**
	 * 异常的文件信息
	 * 
	 * @return
	 */
	public String exception_file()
	{
		return super.read_cstr(_exception_file);
	}

	public void set_exception_file(String exception_file)
	{
		super.write_cstr(_exception_file, exception_file);
	}

	/**
	 * 异常的行号信息
	 * 
	 * @return
	 */
	public int exception_line()
	{
		return super.read_cint(_exception_line);
	}

	public void set_exception_line(int exception_line)
	{
		super.write_cint(_exception_line, exception_line);
	}
}
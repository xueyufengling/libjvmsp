package jvmsp.hotspot.jfr.support;

import jvmsp.hotspot.vm_struct;

public class JfrThreadLocal extends vm_struct
{
	public static final String type_name = "JfrThreadLocal";
	public static final long size = sizeof(type_name);

	private static final long _vthread_id = vm_struct.entry.find(type_name, "_vthread_id").offset;// 88
	private static final long _vthread_epoch = vm_struct.entry.find(type_name, "_vthread_epoch").offset;// 184
	private static final long _vthread_excluded = vm_struct.entry.find(type_name, "_vthread_excluded").offset;// 186
	private static final long _vthread = vm_struct.entry.find(type_name, "_vthread").offset;// 188

	public JfrThreadLocal(long address)
	{
		super(type_name, address);
	}

	// typedef u8 traceid;
	// typedef int fio_fd;

	public byte vthread_id()
	{
		return super.read_byte(_vthread_id);
	}

	public void set_vthread_id(byte vthread_id)
	{
		super.write(_vthread_id, vthread_id);
	}

	// u2 _vthread_epoch;
	public int vthread_epoch()
	{
		return super.read_uint16_t(_vthread_epoch);
	}

	public void set_vthread_epoch(int vthread_epoch)
	{
		super.write_uint16_t(_vthread_epoch, vthread_epoch);
	}

	public boolean vthread_excluded()
	{
		return super.read_cbool(_vthread_excluded);
	}

	public void set_vthread_excluded(boolean vthread_excluded)
	{
		super.write_cbool(_vthread_excluded, vthread_excluded);
	}

	public boolean vthread()
	{
		return super.read_cbool(_vthread);
	}

	public void set_vthread(boolean vthread)
	{
		super.write_cbool(_vthread, vthread);
	}
}

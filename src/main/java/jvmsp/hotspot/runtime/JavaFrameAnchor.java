package jvmsp.hotspot.runtime;

import jvmsp.hotspot.vm_struct;

/**
 * Java栈帧标记点。<br>
 * https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/runtime/javaFrameAnchor.hpp
 */
public class JavaFrameAnchor extends vm_struct
{
	public static final String type_name = "JavaFrameAnchor";
	public static final long size = sizeof(type_name);

	private static final long _last_Java_sp = vm_struct.entry.find(type_name, "_last_Java_sp").offset;// 0
	private static final long _last_Java_pc = vm_struct.entry.find(type_name, "_last_Java_pc").offset;// 8
	private static final long _last_Java_fp = vm_struct.entry.find(type_name, "_last_Java_fp").offset;// 16

	public JavaFrameAnchor(long address)
	{
		super(type_name, address);
	}

	public long last_Java_sp()
	{
		return super.read_ptr(_last_Java_sp);
	}

	public void set_last_Java_sp(long last_Java_sp)
	{
		super.write_ptr(_last_Java_sp, last_Java_sp);
	}

	public long last_Java_pc()
	{
		return super.read_ptr(_last_Java_pc);
	}

	public void set_last_Java_pc(long last_Java_pc)
	{
		super.write_ptr(_last_Java_pc, last_Java_pc);
	}

	public long last_Java_fp()
	{
		return super.read_ptr(_last_Java_fp);
	}

	public void set_last_Java_fp(long last_Java_fp)
	{
		super.write_ptr(_last_Java_fp, last_Java_fp);
	}

	public boolean has_last_Java_frame()
	{
		return last_Java_sp() != 0;
	}

	// This is very dangerous unless sp == nullptr
	// Invalidate the anchor so that has_last_frame is false
	// and no one should look at the other fields.
	public void zap()
	{
		set_last_Java_sp(0);
	}
}

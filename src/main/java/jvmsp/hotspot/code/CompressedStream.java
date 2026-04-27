package jvmsp.hotspot.code;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.ResourceObj;

public class CompressedStream extends ResourceObj
{
	public static final String type_name = "CompressedStream";
	public static final long size = sizeof(type_name);

	private static final long _buffer = vm_struct.entry.find(type_name, "_buffer").offset;// 0
	private static final long _position = vm_struct.entry.find(type_name, "_position").offset;// 8

	protected CompressedStream(String name, long address)
	{
		super(name, address);
	}

	public CompressedStream(long address)
	{
		this(type_name, address);
	}

	public long buffer()
	{
		return super.read_byte(_buffer);
	}

	public void set_buffer(long buffer)
	{
		super.write(_buffer, buffer);
	}

	public int position()
	{
		return super.read_cint(_position);
	}

	public void set_position(int buffer)
	{
		super.write_cint(_position, buffer);
	}
}

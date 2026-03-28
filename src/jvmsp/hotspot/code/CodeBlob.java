package jvmsp.hotspot.code;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.compiler.ImmutableOopMapSet;

public class CodeBlob extends vm_struct
{
	public static final String type_name = "CodeBlob";
	public static final long size = sizeof(type_name);

	private static final long _code_begin = switch_offset(
			() -> vm_struct.entry.find(type_name, "_code_begin").offset// JDK21
	);
	private static final long _code_end = switch_offset(
			() -> vm_struct.entry.find(type_name, "_code_end").offset// JDK21
	);
	private static final long _content_begin = switch_offset(
			() -> vm_struct.entry.find(type_name, "_content_begin").offset// JDK21
	);
	private static final long _data_end = switch_offset(
			() -> vm_struct.entry.find(type_name, "_data_end").offset// JDK21
	);

	private static final long _oop_maps = vm_struct.entry.find(type_name, "_oop_maps").offset;
	private static final long _name = vm_struct.entry.find(type_name, "_name").offset;
	private static final long _size = vm_struct.entry.find(type_name, "_size").offset;
	private static final long _header_size = vm_struct.entry.find(type_name, "_header_size").offset;

	private static final long _frame_complete_offset = vm_struct.entry.find(type_name, "_frame_complete_offset").offset;
	private static final long _data_offset = vm_struct.entry.find(type_name, "_data_offset").offset;
	private static final long _frame_size = vm_struct.entry.find(type_name, "_frame_size").offset;

	protected CodeBlob(String name, long address)
	{
		super(name, address);
	}

	public CodeBlob(long address)
	{
		this(type_name, address);
	}

	public long _name()
	{
		return super.read_pointer(_name);
	}

	public String name()
	{
		return super.read_cstr(_name);
	}

	public void set_name(String name)
	{
		super.write_cstr(_name, name);
	}

	public ImmutableOopMapSet oop_maps()
	{
		return super.read_memory_object_ptr(ImmutableOopMapSet.class, _oop_maps);
	}

	public void set_oop_maps(ImmutableOopMapSet oop_maps)
	{
		super.write_memory_object_ptr(_oop_maps, oop_maps);
	}

	public int size()
	{
		return super.read_cint(_size);
	}

	public void set_size(int size)
	{
		super.write_cint(_size, size);
	}

	public int header_size()
	{
		return super.read_uint16_t(_header_size);
	}

	public void set_header_size(int header_size)
	{
		super.write_uint16_t(_header_size, header_size);
	}

	public short frame_complete_offset()
	{
		return super.read_short(_frame_complete_offset);
	}

	public void set_frame_complete_offset(short size)
	{
		super.write(_frame_complete_offset, size);
	}

	public int data_offset()
	{
		return super.read_cint(_data_offset);
	}

	public void set_data_offset(int data_offset)
	{
		super.write_cint(_data_offset, data_offset);
	}

	public int frame_size()
	{
		return super.read_cint(_frame_size);
	}

	public void set_frame_size(int frame_size)
	{
		super.write_cint(_frame_size, frame_size);
	}
}

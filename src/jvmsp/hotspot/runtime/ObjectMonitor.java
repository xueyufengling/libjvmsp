package jvmsp.hotspot.runtime;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;
import jvmsp.hotspot.oops.Metadata;

public class ObjectMonitor extends CHeapObj
{
	// JDK21: https://github.com/openjdk/jdk/blob/jdk-21%2B35/src/hotspot/share/runtime/objectMonitor.hpp
	// JDK25: https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/runtime/objectMonitor.hpp

	public static final String type_name = "ObjectMonitor";
	public static final long size = sizeof(type_name);

	// JDK21 markWord
	private static final long _header = vm_struct.entry.find(type_name, "_header").offset; // 0
	// JDK25 uintptr_t
	private static final long _metadata = vm_struct.entry.find(type_name, "_metadata").offset; // 0

	private static final long _object = vm_struct.entry.find(type_name, "_object").offset; // 8
	private static final long _owner = vm_struct.entry.find(type_name, "_owner").offset; // 64
	private static final long _next_om = vm_struct.entry.find(type_name, "_next_om").offset; // 128
	private static final long _recursions = vm_struct.entry.find(type_name, "_recursions").offset; // 136
	private static final long _EntryList = vm_struct.entry.find(type_name, "_EntryList").offset; // 144
	private static final long _cxq = vm_struct.entry.find(type_name, "_cxq").offset; // 152
	private static final long _succ = vm_struct.entry.find(type_name, "_succ").offset; // 160
	private static final long _contentions = vm_struct.entry.find(type_name, "_contentions").offset; // 184
	private static final long _waiters = vm_struct.entry.find(type_name, "_waiters").offset; // 200

	public ObjectMonitor(long address)
	{
		super(type_name, address);
	}

	public long header()
	{
		return super.read_pointer(_header);
	}

	public void set_header(long header)
	{
		super.write_pointer(_header, header);
	}

	public long _metadata()
	{
		return super.read_pointer(_metadata);
	}

	public Metadata metadata()
	{
		return super.read_memory_object_ptr(Metadata.class, _metadata);
	}

	public void set_metadata(long metadata)
	{
		super.write_pointer(_metadata, metadata);
	}

	public long object()
	{
		return super.read_pointer(_object);
	}

	public void set_object(long object)
	{
		super.write_pointer(_object, object);
	}

	public long owner()
	{
		return super.read_pointer(_owner);
	}

	public void set_owner(long owner)
	{
		super.write_pointer(_owner, owner);
	}

	public long next_om()
	{
		return super.read_pointer(_next_om);
	}

	public void set_next_om(long next_om)
	{
		super.write_pointer(_next_om, next_om);
	}

	public long recursions()
	{
		return super.read_pointer(_recursions);
	}

	public void set_recursions(long recursions)
	{
		super.write_pointer(_recursions, recursions);
	}

	public long EntryList()
	{
		return super.read_pointer(_EntryList);
	}

	public void set_EntryList(long EntryList)
	{
		super.write_pointer(_EntryList, EntryList);
	}

	public long cxq()
	{
		return super.read_pointer(_cxq);
	}

	public void set_cxq(long cxq)
	{
		super.write_pointer(_cxq, cxq);
	}

	public long succ()
	{
		return super.read_pointer(_succ);
	}

	public void set_succ(long succ)
	{
		super.write_pointer(_succ, succ);
	}

	public long contentions()
	{
		return super.read_pointer(_contentions);
	}

	public void set_contentions(long contentions)
	{
		super.write_pointer(_contentions, contentions);
	}

	public long waiters()
	{
		return super.read_pointer(_waiters);
	}

	public void set_waiters(long waiters)
	{
		super.write_pointer(_waiters, waiters);
	}
}

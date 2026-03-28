package jvmsp.hotspot.gc.shared;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;
import jvmsp.hotspot.memory.MemRegion;

public class CollectedHeap extends CHeapObj
{
	public static final String type_name = "CollectedHeap";
	public static final long size = sizeof(type_name);

	private static final long _lab_alignment_reserve = switch_address(
			null, // JDK21
			() -> vm_struct.entry.find(type_name, "_lab_alignment_reserve").address// JDK25
	);
	private static final long _reserved = vm_struct.entry.find(type_name, "_reserved").offset;
	private static final long _is_gc_active = switch_address(
			() -> vm_struct.entry.find(type_name, "_is_gc_active").offset, // JDK21
			() -> vm_struct.entry.find(type_name, "_is_stw_gc_active").offset// JDK25
	);
	private static final long _total_collections = vm_struct.entry.find(type_name, "_total_collections").offset;

	public CollectedHeap(long address)
	{
		super(type_name, address);
	}

	public static final long lab_alignment_reserve()
	{
		return unsafe.read_long(_lab_alignment_reserve);
	}

	public MemRegion reserved()
	{
		return super.read_memory_object(MemRegion.class, _reserved);
	}

	public boolean is_gc_active()
	{
		return super.read_cbool(_is_gc_active);
	}

	public void set_is_gc_active(boolean is_gc_active)
	{
		super.write_cbool(_is_gc_active, is_gc_active);
	}

	public int total_collections()
	{
		return super.read_int(_total_collections);
	}
}

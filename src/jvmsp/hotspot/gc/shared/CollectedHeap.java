package jvmsp.hotspot.gc.shared;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.MemRegion;

public class CollectedHeap extends vm_struct
{
	private static final long _lab_alignment_reserve = vm_struct.entry.find("CollectedHeap", "_lab_alignment_reserve").address;
	private static final long _reserved = vm_struct.entry.find("CollectedHeap", "_reserved").offset;
	private static final long _is_stw_gc_active = vm_struct.entry.find("CollectedHeap", "_is_stw_gc_active").offset;
	private static final long _total_collections = vm_struct.entry.find("CollectedHeap", "_total_collections").offset;

	protected CollectedHeap(long address)
	{
		super("CollectedHeap", address);
	}

	public static final long lab_alignment_reserve()
	{
		return unsafe.read_long(_lab_alignment_reserve);
	}

	public MemRegion reserved()
	{
		return super.read_memory_object(MemRegion.class, _reserved);
	}

	public boolean is_stw_gc_active()
	{
		return super.read_cbool(_is_stw_gc_active);
	}

	public void set_is_stw_gc_active(boolean is_stw_gc_active)
	{
		super.write_cbool(_is_stw_gc_active, is_stw_gc_active);
	}

	public int total_collections()
	{
		return super.read_int(_total_collections);
	}
}

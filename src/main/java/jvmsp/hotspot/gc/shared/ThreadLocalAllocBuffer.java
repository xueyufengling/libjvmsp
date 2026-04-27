package jvmsp.hotspot.gc.shared;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;

public class ThreadLocalAllocBuffer extends CHeapObj
{
	public static final String type_name = "ThreadLocalAllocBuffer";
	public static final long size = sizeof(type_name);

	// 成员字段
	private static final long _start = vm_struct.entry.find(type_name, "_start").offset; // 0
	private static final long _top = vm_struct.entry.find(type_name, "_top").offset; // 8
	private static final long _pf_top = vm_struct.entry.find(type_name, "_pf_top").offset; // 16
	private static final long _end = vm_struct.entry.find(type_name, "_end").offset; // 24
	private static final long _desired_size = vm_struct.entry.find(type_name, "_desired_size").offset; // 40
	private static final long _refill_waste_limit = vm_struct.entry.find(type_name, "_refill_waste_limit").offset; // 48
	private static final long _number_of_refills = vm_struct.entry.find(type_name, "_number_of_refills").offset; // 72
	private static final long _refill_waste = vm_struct.entry.find(type_name, "_refill_waste").offset; // 76
	private static final long _gc_waste = vm_struct.entry.find(type_name, "_gc_waste").offset; // 80
	private static final long _slow_allocations = vm_struct.entry.find(type_name, "_slow_allocations").offset; // 84

	// 静态字段
	private static final long _reserve_for_allocation_prefetch = vm_struct.entry.find(type_name, "_reserve_for_allocation_prefetch").address;
	private static final long _target_refills = vm_struct.entry.find(type_name, "_target_refills").address;

	public ThreadLocalAllocBuffer(long address)
	{
		super(type_name, address);
	}

	public long start()
	{
		return super.read_ptr(_start);
	}

	public void set_start(long start)
	{
		super.write_ptr(_start, start);
	}

	public long top()
	{
		return super.read_ptr(_top);
	}

	public void set_top(long top)
	{
		super.write_ptr(_top, top);
	}

	public long end()
	{
		return super.read_ptr(_end);
	}

	public void set_end(long end)
	{
		super.write_ptr(_end, end);
	}

	public long pf_top()
	{
		return super.read_ptr(_pf_top);
	}

	public void set_pf_top(long pf_top)
	{
		super.write_ptr(_pf_top, pf_top);
	}

	public long desired_size()
	{
		return super.read_long(_desired_size);
	}

	public void set_desired_size(long desired_size)
	{
		super.write(_desired_size, desired_size);
	}

	public long refill_waste_limit()
	{
		return super.read_long(_refill_waste_limit);
	}

	public void set_refill_waste_limit(long refill_waste_limit)
	{
		super.write(_refill_waste_limit, refill_waste_limit);
	}

	public long number_of_refills()
	{
		return super.read_cuint(_number_of_refills);
	}

	public void set_number_of_refills(long number_of_refills)
	{
		super.write_cuint(_number_of_refills, number_of_refills);
	}

	public long refill_waste()
	{
		return super.read_cuint(_refill_waste);
	}

	public void set_refill_waste(long refill_waste)
	{
		super.write_cuint(_refill_waste, refill_waste);
	}

	public long gc_waste()
	{
		return super.read_cuint(_gc_waste);
	}

	public void set_gc_waste(long gc_waste)
	{
		super.write_cuint(_gc_waste, gc_waste);
	}

	public long slow_allocations()
	{
		return super.read_cuint(_slow_allocations);
	}

	public void set_slow_allocations(long slow_allocations)
	{
		super.write_cuint(_slow_allocations, slow_allocations);
	}

	public static final long reserve_for_allocation_prefetch()
	{
		return unsafe.read_ptr(_reserve_for_allocation_prefetch);
	}

	public static final void set_reserve_for_allocation_prefetch(long reserve_for_allocation_prefetch)
	{
		unsafe.write_ptr(_reserve_for_allocation_prefetch, reserve_for_allocation_prefetch);
	}

	public static final long target_refills()
	{
		return unsafe.read_ptr(_target_refills);
	}

	public static final void set_target_refills(long target_refills)
	{
		unsafe.write_ptr(_target_refills, target_refills);
	}
}

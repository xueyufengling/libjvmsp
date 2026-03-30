package jvmsp.hotspot.gc.shared;

import java.util.Iterator;

import jvmsp.type.cxx_type;
import jvmsp.hotspot.vm_struct;

/**
 * 连续链式储存的内存空间块
 */
public class ContiguousSpace extends Space implements Iterable<ContiguousSpace>
{
	public static final String type_name = "ContiguousSpace";
	public static final long size = sizeof(type_name);

	private static final long _compaction_top = vm_struct.entry.find(type_name, "_compaction_top").offset;// 32
	private static final long _top = vm_struct.entry.find(type_name, "_top").offset;// 48
	private static final long _first_dead = vm_struct.entry.find(type_name, "_first_dead").offset;// 64
	private static final long _end_of_live = vm_struct.entry.find(type_name, "_end_of_live").offset;// 72

	private static final long _next_compaction_space = _compaction_top + cxx_type.pvoid.size();// 40

	public ContiguousSpace(long address)
	{
		super(type_name, address);
	}

	public long compaction_top()
	{
		return super.read_ptr(_compaction_top);
	}

	public void set_compaction_top(long compaction_top)
	{
		super.write_ptr(_compaction_top, compaction_top);
	}

	/**
	 * 链表下一块内存的地址
	 * 
	 * @return
	 */
	public long _next_compaction_space()
	{
		return super.read_ptr(_next_compaction_space);
	}

	public void set_next_compaction_space(long next_compaction_space)
	{
		super.write_ptr(_next_compaction_space, next_compaction_space);
	}

	public boolean has_next_compaction_space()
	{
		return _next_compaction_space() != 0;
	}

	public ContiguousSpace next_compaction_space()
	{
		return super.read_memory_object_ptr(ContiguousSpace.class, _next_compaction_space);
	}

	public long top()
	{
		return super.read_ptr(_top);
	}

	public void set_top(long top)
	{
		super.write_ptr(_top, top);
	}

	public long first_dead()
	{
		return super.read_ptr(_first_dead);
	}

	public void set_first_dead(long first_dead)
	{
		super.write_ptr(_first_dead, first_dead);
	}

	public long end_of_live()
	{
		return super.read_ptr(_end_of_live);
	}

	public void set_end_of_live(long end_of_live)
	{
		super.write_ptr(_end_of_live, end_of_live);
	}

	@Override
	public Iterator<ContiguousSpace> iterator()
	{
		return new Iterator<>()
		{
			@Override
			public boolean hasNext()
			{
				return has_next_compaction_space();
			}

			@Override
			public ContiguousSpace next()
			{
				return next_compaction_space();
			}
		};
	}
}

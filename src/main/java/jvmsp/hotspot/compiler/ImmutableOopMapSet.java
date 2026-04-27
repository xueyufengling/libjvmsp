package jvmsp.hotspot.compiler;

import jvmsp.hotspot.vm_struct;

public class ImmutableOopMapSet extends vm_struct
{
	public static final String type_name = "ImmutableOopMapSet";
	public static final long size = sizeof(type_name);

	private static final long _count = vm_struct.entry.find(type_name, "_count").offset;
	private static final long _size = vm_struct.entry.find(type_name, "_size").offset;

	public ImmutableOopMapSet(long address)
	{
		super(type_name, address);
	}

	/**
	 * 本Set中ImmutableOopMapPairs的个数
	 * 
	 * @return
	 */
	public int count()
	{
		return super.read_cint(_count);
	}

	/**
	 * 本Set包含数据的总共大小
	 * 
	 * @return
	 */
	public int nr_of_bytes()
	{
		return super.read_cint(_size);
	}

	/**
	 * 实际存放键值对数据的地址
	 * 
	 * @return
	 */
	public long pairs_address()
	{
		return address + size;
	}

	public ImmutableOopMapPair pair_at(int idx)
	{
		return super.read_memory_object_at(ImmutableOopMapPair.class, size, ImmutableOopMapPair.size, idx);
	}

	public ImmutableOopMapPair[] get_pairs()
	{
		return super.read_memory_object_arr(ImmutableOopMapPair.class, size, ImmutableOopMapPair.size, count());
	}

	public long data()
	{
		return address + size + ImmutableOopMapPair.size * count();
	}
}
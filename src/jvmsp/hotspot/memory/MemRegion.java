package jvmsp.hotspot.memory;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.utilities.globalDefinitions;

/**
 * 内存区间
 */
public class MemRegion extends vm_struct
{
	public static final String type_name = "MemRegion";
	public static final long size = sizeof(type_name);

	/**
	 * 堆的起始地址。<br>
	 * HeapWord*实质是void*，因为HeapWord只有声明没有定义。<br>
	 */
	private static final long _start = vm_struct.entry.find(type_name, "_start").offset;

	/**
	 * size_t _word_size，该内存块的长度
	 */
	private static final long _word_size = vm_struct.entry.find(type_name, "_word_size").offset;

	public MemRegion(long address)
	{
		super(type_name, address);
	}

	public long start()
	{
		return super.read_pointer(_start);
	}

	public long word_size()
	{
		return super.read_long(_word_size);
	}

	public long end()
	{
		return start() + word_size();
	}

	public long last()
	{
		return end() - 1;
	}

	public void set_start(long start)
	{
		super.write_pointer(_start, start);
	}

	public void set_word_size(long word_size)
	{
		super.write(_word_size, word_size);
	}

	public void set_end(long end)
	{
		set_word_size(end - start());
	}

	public boolean contains(MemRegion mr2)
	{
		return start() <= mr2.start() && end() >= mr2.end();
	}

	public boolean contains(long addr)
	{
		return addr >= start() && addr < end();
	}

	public long byte_size()
	{
		return _word_size * globalDefinitions.HeapWordSize;
	}

	public boolean is_empty()
	{
		return word_size() == 0;
	}
}
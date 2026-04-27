package jvmsp.hotspot.oops;

import jvmsp.type.cxx_type;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.utilities.align;
import jvmsp.hotspot.utilities.globalDefinitions;

public class OopMapBlock extends vm_struct
{
	public static final String type_name = "OopMapBlock";

	public static final cxx_type OopMapBlock = cxx_type.define(type_name)
			.decl_field("_offset", cxx_type._int)
			.decl_field("_count", cxx_type.unsigned_int)
			.resolve();

	public static final long size = OopMapBlock.size();

	private static final long _offset = OopMapBlock.field("_offset").offset();
	private static final long _count = OopMapBlock.field("_count").offset();

	public static final byte is_hidden_class = 1 << 0;
	public static final byte is_value_based_class = 1 << 1;
	public static final byte has_finalizer = 1 << 2;
	public static final byte is_cloneable_fast = 1 << 3;

	public OopMapBlock(long address)
	{
		super(type_name, address);
	}

	/**
	 * 获取偏移量
	 * 
	 * @return
	 */
	public int offset()
	{
		return super.read_cint(_offset);
	}

	/**
	 * 设置偏移量
	 * 
	 * @param offset
	 */
	public void set_offset(int offset)
	{
		super.write_cint(_offset, offset);
	}

	/**
	 * 获取oop数量
	 * 
	 * @return
	 */
	public long count()
	{
		return super.read_cuint(_count);
	}

	/**
	 * 设置oop数量
	 * 
	 * @param count
	 */
	public void set_count(long count)
	{
		super.write_cuint(_count, count);
	}

	/**
	 * 增加计数
	 * 
	 * @param diff
	 */
	public void increment_count(int diff)
	{
		set_count(count() + diff);
	}

	/**
	 * 偏移跨度 = count * heapOopSize
	 * 
	 * @return
	 */
	public long offset_span()
	{
		return count() * globalDefinitions.heapOopSize;
	}

	/**
	 * 结束偏移
	 * 
	 * @return
	 */
	public long end_offset()
	{
		return offset() + offset_span();
	}

	/**
	 * 内存是否连续
	 * 
	 * @param another_offset
	 * @return
	 */
	public boolean is_contiguous(int another_offset)
	{
		return another_offset == end_offset();
	}

	public static int size_in_words()
	{
		return (int) (align.align_up_word_size(size) >>> globalDefinitions.LogBytesPerWord);
	}

	public static int compare_offset(OopMapBlock a, OopMapBlock b)
	{
		return a.offset() - b.offset();
	}
}

package jvmsp.hotspot.utilities;

/**
 * 内存对齐工具函数。<br>
 * https://github.com/openjdk/jdk/blob/master/src/hotspot/share/utilities/align.hpp
 */
public class align
{
	public static final boolean is_power_of_2(long value)
	{
		return (value > 0) && ((value & (value - 1)) == 0);
	}

	public static final long alignment_mask(long alignment)
	{
		assert is_power_of_2(alignment) : "alignment must be a power of 2: " + alignment;
		return alignment - 1;
	}

	public static final boolean is_aligned(long size, long alignment)
	{
		return (size & alignment_mask(alignment)) == 0;
	}

	public static final long align_up(long size, long alignment)
	{
		return align_down(size + alignment_mask(alignment), alignment);
	}

	public static final long align_down(long size, long alignment)
	{
		return size & ~alignment_mask(alignment);
	}

	public static final long align_metadata_size(long size)
	{
		return align_up(size, 1);
	}

	public static final long align_object_size(long word_size)
	{
		return align_up(word_size, globalDefinitions.MinObjAlignment);
	}

	public static final boolean is_object_aligned(long word_size)
	{
		return is_aligned(word_size, globalDefinitions.MinObjAlignment);
	}

	public static final long align_object_offset(long offset)
	{
		return align_up(offset, globalDefinitions.HeapWordsPerLong);
	}

	public static final long align_up_word_size(long size)
	{
		return align_up(size, globalDefinitions.wordSize);
	}
}

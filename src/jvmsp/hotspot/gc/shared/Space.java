package jvmsp.hotspot.gc.shared;

import jvmsp.type.cxx_type;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;

/**
 * 内存空间块
 */
public class Space extends CHeapObj
{
	public static final String type_name = "Space";
	public static final long size = sizeof(type_name);

	private static final long _bottom = vm_struct.entry.find(type_name, "_bottom").offset;// 8
	private static final long _end = vm_struct.entry.find(type_name, "_end").offset;// 16

	private static final long _saved_mark_word = _end + cxx_type.pvoid.size();// 24

	protected Space(String name, long address)
	{
		super(name, address);
	}

	public Space(long address)
	{
		this(type_name, address);
	}

	/**
	 * 起始地址
	 * 
	 * @return
	 */
	public long bottom()
	{
		return super.read_pointer(_bottom);
	}

	public void set_bottom(long bottom)
	{
		super.write_pointer(_bottom, bottom);
	}

	/**
	 * 终止地址
	 * 
	 * @return
	 */
	public long end()
	{
		return super.read_pointer(_end);
	}

	public void set_end(long end)
	{
		super.write_pointer(_end, end);
	}

	public long saved_mark_word()
	{
		return super.read_pointer(_saved_mark_word);
	}

	public void set_saved_mark_word(long saved_mark_word)
	{
		super.write_pointer(_saved_mark_word, saved_mark_word);
	}
}

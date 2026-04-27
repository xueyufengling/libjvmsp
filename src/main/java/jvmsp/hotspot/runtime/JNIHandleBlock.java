package jvmsp.hotspot.runtime;

import java.util.Iterator;

import jvmsp.hotspot.vm_constant;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;

public class JNIHandleBlock extends CHeapObj implements Iterable<JNIHandleBlock>
{
	public static final String type_name = "JNIHandleBlock";
	public static final long size = sizeof(type_name);

	private static final long _handles = vm_struct.entry.find(type_name, "_handles").offset;
	private static final long _top = vm_struct.entry.find(type_name, "_top").offset;
	private static final long _next = vm_struct.entry.find(type_name, "_next").offset;

	public static final int block_size_in_oops = vm_constant.find_int("JNIHandleBlock::block_size_in_oops"); // Number of handles per handle block

	public JNIHandleBlock(long address)
	{
		super(type_name, address);
	}

	public long _handles()
	{
		return super.offset_addr(_handles);
	}

	public int top()
	{
		return super.read_cint(_top);
	}

	public void set_top(int top)
	{
		super.write_cint(_top, top);
	}

	public JNIHandleBlock next()
	{
		return super.read_memory_object_ptr(JNIHandleBlock.class, _next);
	}

	public long _next()
	{
		return super.read_ptr(_next);
	}

	public void set_next(JNIHandleBlock next)
	{
		super.write_memory_object_ptr(_next, next);
	}

	public boolean has_next()
	{
		return _next() != 0;
	}

	@Override
	public Iterator<JNIHandleBlock> iterator()
	{
		return new Iterator<>()
		{
			@Override
			public boolean hasNext()
			{
				return JNIHandleBlock.this.has_next();
			}

			@Override
			public JNIHandleBlock next()
			{
				return JNIHandleBlock.this.next();
			}
		};
	}
}

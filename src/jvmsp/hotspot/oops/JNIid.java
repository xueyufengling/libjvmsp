package jvmsp.hotspot.oops;

import java.util.Iterator;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;

/**
 * JNI id，链式储存。
 */
public class JNIid extends CHeapObj implements Iterable<JNIid>
{
	public static final String type_name = "JNIid";
	public static final long size = sizeof(type_name);

	private static final long _holder = vm_struct.entry.find(type_name, "_holder").offset;
	private static final long _next = vm_struct.entry.find(type_name, "_next").offset;
	private static final long _offset = vm_struct.entry.find(type_name, "_offset").offset;

	public JNIid(long address)
	{
		super(type_name, address);
	}

	public InstanceKlass holder()
	{
		return super.read_memory_object_ptr(InstanceKlass.class, _holder);
	}

	public void set_holder(InstanceKlass holder)
	{
		super.write_memory_object_ptr(_holder, holder);
	}

	public JNIid next()
	{
		return super.read_memory_object_ptr(JNIid.class, _next);
	}

	public long _next()
	{
		return super.read_pointer(_next);
	}

	public void set_next(JNIid next)
	{
		super.write_memory_object_ptr(_next, next);
	}

	public boolean has_next()
	{
		return _next() != 0;
	}

	public int offset()
	{
		return super.read_cint(_offset);
	}

	public void set_offset(int offset)
	{
		super.write_cint(_offset, offset);
	}

	@Override
	public Iterator<JNIid> iterator()
	{
		return new Iterator<>()
		{
			@Override
			public boolean hasNext()
			{
				return JNIid.this.has_next();
			}

			@Override
			public JNIid next()
			{
				return JNIid.this.next();
			}
		};
	}
}

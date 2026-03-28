package jvmsp.hotspot.classfile;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;
import jvmsp.hotspot.oops.Klass;
import jvmsp.hotspot.oops.OopHandle;

public class ClassLoaderData extends CHeapObj
{
	public static final String type_name = "ClassLoaderData";
	public static final long size = sizeof(type_name);

	private static final long _class_loader = vm_struct.entry.find(type_name, "_class_loader").offset;// 8
	private static final long _has_class_mirror_holder = vm_struct.entry.find(type_name, "_has_class_mirror_holder").offset;// 33
	private static final long _klasses = vm_struct.entry.find(type_name, "_klasses").offset;// 56
	private static final long _next = vm_struct.entry.find(type_name, "_next").offset;// 112

	public ClassLoaderData(long address)
	{
		super(type_name, address);
	}

	public OopHandle class_loader()
	{
		return super.read_memory_object(OopHandle.class, _class_loader);
	}

	public void set_class_loader(OopHandle class_loader)
	{
		super.write_memory_object(_class_loader, class_loader, OopHandle.size);
	}

	public boolean has_class_mirror_holder()
	{
		return super.read_cbool(_has_class_mirror_holder);
	}

	public void set_has_class_mirror_holder(boolean has_class_mirror_holder)
	{
		super.write_cbool(_has_class_mirror_holder, has_class_mirror_holder);
	}

	/**
	 * 返回链式存储的第一个Klass.<br>
	 * Klass可以使用for-each迭代。<br>
	 * 
	 * @return
	 */
	public Klass klasses()
	{
		return super.read_memory_object_ptr(Klass.class, _klasses);
	}

	public void set_klasses(Klass klasses)
	{
		super.write_memory_object_ptr(_klasses, klasses);
	}

	/**
	 * 获取下一个链式存储的ClassLoaderData。<br>
	 * 
	 * @return
	 */
	public ClassLoaderData next()
	{
		return super.read_memory_object_ptr(ClassLoaderData.class, _next);
	}

	public void set_next(ClassLoaderData next)
	{
		super.write_memory_object_ptr(_next, next);
	}
}

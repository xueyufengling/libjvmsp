package jvmsp.hotspot.classfile;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.oops.Klass;
import jvmsp.hotspot.oops.OopHandle;

public class ClassLoaderData extends vm_struct
{
	public static final String type_name = "ClassLoaderData";
	public static final long size = sizeof(type_name);

	private static final long _class_loader = vm_struct.entry.find(type_name, "_class_loader").offset;
	private static final long _next = vm_struct.entry.find(type_name, "_next").offset;
	private static final long _klasses = vm_struct.entry.find(type_name, "_klasses").offset;
	private static final long _has_class_mirror_holder = vm_struct.entry.find(type_name, "_has_class_mirror_holder").offset;

	public ClassLoaderData(long address)
	{
		super(type_name, address);
	}

	public OopHandle _class_loader()
	{
		return super.read_memory_object(OopHandle.class, _class_loader);
	}

	public ClassLoaderData _next()
	{
		return super.read_memory_object_ptr(ClassLoaderData.class, _next);
	}

	public Klass _klasses()
	{
		return super.read_memory_object_ptr(Klass.class, _klasses);
	}

	public boolean _has_class_mirror_holder()
	{
		return super.read_cbool(_has_class_mirror_holder);
	}

	public void set_has_class_mirror_holder(boolean has_class_mirror_holder)
	{
		super.write_cbool(_has_class_mirror_holder, has_class_mirror_holder);
	}
}

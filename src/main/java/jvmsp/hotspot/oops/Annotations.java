package jvmsp.hotspot.oops;

import jvmsp.hotspot.memory.MetaspaceObj;
import jvmsp.hotspot.oops.Array.Array_pArray_u1;
import jvmsp.hotspot.oops.Array.Array_u1;
import jvmsp.type.cxx_type;

public class Annotations extends MetaspaceObj
{
	public static final String type_name = "Annotations";

	public static final cxx_type Annotations = cxx_type.define(type_name)
			.decl_field("_class_annotations", cxx_type.pvoid)
			.decl_field("_fields_annotations", cxx_type.pvoid)
			.decl_field("_class_type_annotations", cxx_type.pvoid)
			.decl_field("_fields_type_annotations", cxx_type.pvoid)
			.resolve();

	public static final long size = Annotations.size();

	private static final long _class_annotations = Annotations.field("_class_annotations").offset();
	private static final long _fields_annotations = Annotations.field("_fields_annotations").offset();
	private static final long _class_type_annotations = Annotations.field("_class_type_annotations").offset();
	private static final long _fields_type_annotations = Annotations.field("_fields_type_annotations").offset();

	public Annotations(long address)
	{
		super(type_name, address);
	}

	public Array_u1 class_annotations()
	{
		return super.read_memory_object_ptr(Array_u1.class, _class_annotations);
	}

	public void set_class_annotations(Array_u1 class_annotations)
	{
		super.write_memory_object_ptr(_class_annotations, class_annotations);
	}

	public Array_pArray_u1 fields_annotations()
	{
		return super.read_memory_object_ptr(Array_pArray_u1.class, _fields_annotations);
	}

	public void set_fields_annotations(Array_pArray_u1 methods)
	{
		super.write_memory_object_ptr(_fields_annotations, methods);
	}

	public Array_u1 class_type_annotations()
	{
		return super.read_memory_object_ptr(Array_u1.class, _class_type_annotations);
	}

	public void set_class_type_annotations(Array_u1 class_type_annotations)
	{
		super.write_memory_object_ptr(_class_type_annotations, class_type_annotations);
	}

	public Array_pArray_u1 fields_type_annotations()
	{
		return super.read_memory_object_ptr(Array_pArray_u1.class, _fields_type_annotations);
	}

	public void set_fields_type_annotations(Array_pArray_u1 fields_type_annotations)
	{
		super.write_memory_object_ptr(_fields_type_annotations, fields_type_annotations);
	}

	@Override
	public int meta_type()
	{
		return Type.AnnotationsType;
	}

	public String internal_name()
	{
		return "{annotations}";
	}
}

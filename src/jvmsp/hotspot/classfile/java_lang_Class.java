package jvmsp.hotspot.classfile;

import jvmsp.type.java_type;
import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.AllStatic;
import jvmsp.hotspot.oops.CompressedKlassPointers;
import jvmsp.hotspot.oops.CompressedOops;
import jvmsp.hotspot.oops.InstanceKlass;
import jvmsp.hotspot.oops.Klass;
import jvmsp.hotspot.oops.oopDesc;

public class java_lang_Class extends AllStatic
{
	public static final String type_name = "java_lang_Class";

	private static final long _klass_offset = vm_struct.entry.find(type_name, "_klass_offset").address;
	private static final long _array_klass_offset = vm_struct.entry.find(type_name, "_array_klass_offset").address;
	private static final long _oop_size_offset = vm_struct.entry.find(type_name, "_oop_size_offset").address;
	private static final long _static_oop_field_count_offset = vm_struct.entry.find(type_name, "_static_oop_field_count_offset").address;

	private java_lang_Class()
	{
		super(type_name);
	}

	public static final int _klass_offset()
	{
		return unsafe.read_int(_klass_offset);
	}

	public static final int _array_klass_offset()
	{
		return unsafe.read_int(_array_klass_offset);
	}

	public static final int _oop_size_offset()
	{
		return unsafe.read_int(_oop_size_offset);
	}

	public static final int _static_oop_field_count_offset()
	{
		return unsafe.read_int(_static_oop_field_count_offset);
	}

	/**
	 * 从java.lang.Class所在的地址计算对应的Klass地址
	 * 
	 * @param java_clazz_address
	 * @return
	 */
	public static final long as_Klass(long java_clazz_address)
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/classfile/javaClasses.inline.hpp#L286
		return oopDesc.metadata_field(java_clazz_address, _klass_offset());
	}

	/**
	 * 获取本类型所对应的数值的ArrayKlass地址
	 * 
	 * @param java_clazz_address
	 * @return
	 */
	public static final long retrieve_ArrayKlass(long java_clazz_address)
	{
		return oopDesc.metadata_field(java_clazz_address, _array_klass_offset());
	}

	/**
	 * 获取指定Class对象的Klass地址
	 * 
	 * @param clazz
	 * @return
	 */
	public static final long klass_ptr(Class<?> clazz)
	{
		return as_Klass(CompressedOops.decode((int) java_type.oop_of(clazz)));
	}

	/**
	 * 获取OOP对象头中的Klass Word
	 * 
	 * @param clazz
	 * @return
	 */
	public static final int klass_word(Class<?> clazz)
	{
		return CompressedKlassPointers.encode(klass_ptr(clazz));
	}

	public static final Klass as_Klass(Class<?> clazz)
	{
		long klass_ptr = klass_ptr(clazz);
		short kind = Klass._kind(klass_ptr);
		switch (kind)
		{
		case Klass.InstanceKlassKind:
			return new InstanceKlass(klass_ptr);
		default:
			return null;
		}
	}

	public static final InstanceKlass as_InstanceKlass(Class<?> clazz)
	{
		return new InstanceKlass(klass_ptr(clazz));
	}
}

package jvmsp.hotspot.classfile;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.oops.CompressedKlassPointers;
import jvmsp.hotspot.oops.CompressedOops;
import jvmsp.hotspot.oops.InstanceKlass;
import jvmsp.hotspot.oops.Klass;
import jvmsp.hotspot.oops.oopDesc;
import jvmsp.type.java_type;

public class java_lang_Class
{
	private static final long _klass_offset = vm_struct.entry.find("java_lang_Class", "_klass_offset").address;
	private static final long _array_klass_offset = vm_struct.entry.find("java_lang_Class", "_array_klass_offset").address;
	private static final long _oop_size_offset = vm_struct.entry.find("java_lang_Class", "_oop_size_offset").address;
	private static final long _static_oop_field_count_offset = vm_struct.entry.find("java_lang_Class", "_static_oop_field_count_offset").address;

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
		return new Klass(klass_ptr(clazz));
	}

	public static final InstanceKlass as_InstanceKlass(Class<?> clazz)
	{
		InstanceKlass ik = new InstanceKlass(klass_ptr(clazz));
		return ik.is_instance_klass() ? ik : null;
	}
}

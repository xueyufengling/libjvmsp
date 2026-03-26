package jvmsp.hotspot.oops;

import jvmsp.type.cxx_type;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.classfile.java_lang_Class;
import jvmsp.hotspot.oops.Array.Array_pMethod;

public class InstanceKlass extends Klass
{
	private static final long _array_klasses = vm_struct.entry.find("InstanceKlass", "_array_klasses").offset;
	private static final long _methods = vm_struct.entry.find("InstanceKlass", "_methods").offset;
	private static final long _default_methods = vm_struct.entry.find("InstanceKlass", "_default_methods").offset;
	private static final long _local_interfaces = vm_struct.entry.find("InstanceKlass", "_local_interfaces").offset;
	private static final long _transitive_interfaces = vm_struct.entry.find("InstanceKlass", "_transitive_interfaces").offset;
	private static final long _fieldinfo_stream = vm_struct.entry.find("InstanceKlass", "_fieldinfo_stream").offset;
	private static final long _constants = vm_struct.entry.find("InstanceKlass", "_constants").offset;
	private static final long _source_debug_extension = vm_struct.entry.find("InstanceKlass", "_source_debug_extension").offset;
	private static final long _inner_classes = vm_struct.entry.find("InstanceKlass", "_inner_classes").offset;
	private static final long _nonstatic_field_size = vm_struct.entry.find("InstanceKlass", "_nonstatic_field_size").offset;
	private static final long _static_field_size = vm_struct.entry.find("InstanceKlass", "_static_field_size").offset;
	private static final long _static_oop_field_count = vm_struct.entry.find("InstanceKlass", "_static_oop_field_count").offset;
	private static final long _nonstatic_oop_map_size = vm_struct.entry.find("InstanceKlass", "_nonstatic_oop_map_size").offset;
	private static final long _init_state = vm_struct.entry.find("InstanceKlass", "_init_state").offset;
	private static final long _init_thread = vm_struct.entry.find("InstanceKlass", "_init_thread").offset;
	private static final long _itable_len = vm_struct.entry.find("InstanceKlass", "_itable_len").offset;
	private static final long _reference_type = vm_struct.entry.find("InstanceKlass", "_reference_type").offset;
	private static final long _oop_map_cache = vm_struct.entry.find("InstanceKlass", "_oop_map_cache").offset;
	private static final long _jni_ids = vm_struct.entry.find("InstanceKlass", "_jni_ids").offset;
	private static final long _osr_nmethods_head = vm_struct.entry.find("InstanceKlass", "_osr_nmethods_head").offset;
	private static final long _breakpoints = vm_struct.entry.find("InstanceKlass", "_breakpoints").offset;
	private static final long _methods_jmethod_ids = vm_struct.entry.find("InstanceKlass", "_methods_jmethod_ids").offset;
	private static final long _idnum_allocated_count = vm_struct.entry.find("InstanceKlass", "_idnum_allocated_count").offset;
	private static final long _annotations = vm_struct.entry.find("InstanceKlass", "_annotations").offset;
	private static final long _method_ordering = vm_struct.entry.find("InstanceKlass", "_method_ordering").offset;
	private static final long _default_vtable_indices = vm_struct.entry.find("InstanceKlass", "_default_vtable_indices").offset;

	private static final long _nest_members = _inner_classes + cxx_type.pvoid.size();
	private static final long _nest_host = _nest_members + cxx_type.pvoid.size();
	private static final long _permitted_subclasses = _nest_host + cxx_type.pvoid.size();
	private static final long _record_components = _permitted_subclasses + cxx_type.pvoid.size();

	public static final long size = sizeof("InstanceKlass");

	public InstanceKlass(long address)
	{
		super("InstanceKlass", address);
	}

	public Array_pMethod methods()
	{
		return super.read_memory_object_ptr(Array_pMethod.class, _methods);
	}

	/**
	 * 查找指定名称和签名的方法
	 * 
	 * @param methods
	 * @param name
	 * @param signature
	 * @return
	 */
	public static final Method lookup_method(Array_pMethod methods, String name, String signature)
	{
		int length = methods.length();
		for (int idx = 0; idx < length; ++idx)
		{
			Method m = methods.at(idx);
			if (name.equals(m.name().jstring()) && signature.equals(m.signature().jstring()))
			{
				return m;
			}
		}
		return null;
	}

	/**
	 * 查找本类定义的方法
	 * 
	 * @param name
	 * @param signature
	 * @return
	 */
	public Method lookup_method(String name, String signature)
	{
		return lookup_method(methods(), name, signature);
	}

	public static final Method lookup_method(Class<?> clazz, String name, String signature)
	{
		return java_lang_Class.as_InstanceKlass(clazz).lookup_method(name, signature);
	}
}
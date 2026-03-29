package jvmsp.hotspot.oops;

import jvmsp.type.cxx_type;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.classfile.java_lang_Class;
import jvmsp.hotspot.code.nmethod;
import jvmsp.hotspot.oops.Array.Array_int;
import jvmsp.hotspot.oops.Array.Array_pInstanceKlass;
import jvmsp.hotspot.oops.Array.Array_pMethod;
import jvmsp.hotspot.runtime.JavaThread;
import jvmsp.hotspot.utilities.AccessFlags;
import jvmsp.hotspot.utilities.align;
import jvmsp.hotspot.utilities.globalDefinitions;

public class InstanceKlass extends Klass
{
	public static final String type_name = "InstanceKlass";
	public static final long size = sizeof(type_name);

	private static final long _annotations = vm_struct.entry.find(type_name, "_annotations").offset; // 184
	private static final long _array_klasses = vm_struct.entry.find(type_name, "_array_klasses").offset; // 200
	private static final long _constants = vm_struct.entry.find(type_name, "_constants").offset; // 208
	private static final long _inner_classes = vm_struct.entry.find(type_name, "_inner_classes").offset; // 216
	private static final long _source_debug_extension = vm_struct.entry.find(type_name, "_source_debug_extension").offset; // 256
	private static final long _nonstatic_field_size = vm_struct.entry.find(type_name, "_nonstatic_field_size").offset; // 264
	private static final long _static_field_size = vm_struct.entry.find(type_name, "_static_field_size").offset; // 268
	private static final long _nonstatic_oop_map_size = vm_struct.entry.find(type_name, "_nonstatic_oop_map_size").offset; // 272
	private static final long _itable_len = vm_struct.entry.find(type_name, "_itable_len").offset; // 276
	private static final long _static_oop_field_count = vm_struct.entry.find(type_name, "_static_oop_field_count").offset; // 284
	private static final long _idnum_allocated_count = vm_struct.entry.find(type_name, "_idnum_allocated_count").offset; // 286
	private static final long _init_state = vm_struct.entry.find(type_name, "_init_state").offset; // 288
	private static final long _reference_type = vm_struct.entry.find(type_name, "_reference_type").offset; // 289
	private static final long _misc_flags_flags = vm_struct.entry.find(type_name, "_misc_flags._flags").offset; // 290
	private static final long _init_thread = vm_struct.entry.find(type_name, "_init_thread").offset; // 304
	private static final long _oop_map_cache = vm_struct.entry.find(type_name, "_oop_map_cache").offset; // 312
	private static final long _jni_ids = vm_struct.entry.find(type_name, "_jni_ids").offset; // 320
	private static final long _methods_jmethod_ids = vm_struct.entry.find(type_name, "_methods_jmethod_ids").offset; // 328
	private static final long _osr_nmethods_head = vm_struct.entry.find(type_name, "_osr_nmethods_head").offset; // 352
	private static final long _breakpoints = vm_struct.entry.find(type_name, "_breakpoints").offset; // 360
	private static final long _methods = vm_struct.entry.find(type_name, "_methods").offset; // 392
	private static final long _default_methods = vm_struct.entry.find(type_name, "_default_methods").offset; // 400
	private static final long _local_interfaces = vm_struct.entry.find(type_name, "_local_interfaces").offset; // 408
	private static final long _transitive_interfaces = vm_struct.entry.find(type_name, "_transitive_interfaces").offset; // 416
	private static final long _method_ordering = vm_struct.entry.find(type_name, "_method_ordering").offset; // 424
	private static final long _default_vtable_indices = vm_struct.entry.find(type_name, "_default_vtable_indices").offset; // 432
	private static final long _fieldinfo_stream = vm_struct.entry.find(type_name, "_fieldinfo_stream").offset; // 440

	private static final long _nest_members = _inner_classes + cxx_type.pvoid.size();
	private static final long _nest_host = _nest_members + cxx_type.pvoid.size();
	private static final long _permitted_subclasses = _nest_host + cxx_type.pvoid.size();
	private static final long _record_components = _permitted_subclasses + cxx_type.pvoid.size();

	public abstract class ClassState
	{
		/**
		 * 已分配内存，但未链接
		 */
		public static final byte allocated = 0;

		/**
		 * 已加载且插入了继承链，但未链接
		 */
		public static final byte loaded = 1;

		/**
		 * 链接、验证成功，但未初始化
		 */
		public static final byte linked = 2;

		/**
		 * 正在执行类初始化
		 */
		public static final byte being_initialized = 3;

		/**
		 * 初始化完成
		 */
		public static final byte fully_initialized = 4;

		/**
		 * 初始化出错
		 */
		public static final byte initialization_error = 5;
	}

	public InstanceKlass(long address)
	{
		super(type_name, address);
	}

	/**
	 * 额外的代码调试信息
	 * 
	 * @return
	 */
	public String source_debug_extension()
	{
		return super.read_cstr(_source_debug_extension);
	}

	public void set_source_debug_extension(String source_debug_extension)
	{
		super.write_cstr(_source_debug_extension, source_debug_extension);
	}

	public int nonstatic_field_size()
	{
		return super.read_cint(_nonstatic_field_size);
	}

	public void set_nonstatic_field_size(int nonstatic_field_size)
	{
		super.write_cint(_nonstatic_field_size, nonstatic_field_size);
	}

	public int static_field_size()
	{
		return super.read_cint(_static_field_size);
	}

	public void set_static_field_size(int static_field_size)
	{
		super.write_cint(_static_field_size, static_field_size);
	}

	public int nonstatic_oop_map_size()
	{
		return super.read_cint(_nonstatic_oop_map_size);
	}

	public void set_nonstatic_oop_map_size(int nonstatic_oop_map_size)
	{
		super.write_cint(_nonstatic_oop_map_size, nonstatic_oop_map_size);
	}

	public int static_oop_field_count()
	{
		return super.read_uint16_t(_static_oop_field_count);
	}

	public void set_static_oop_field_count(int static_oop_field_count)
	{
		super.write_uint16_t(_static_oop_field_count, static_oop_field_count);
	}

	public int idnum_allocated_count()
	{
		return super.read_uint16_t(_idnum_allocated_count);
	}

	public void set_initial_method_idnum(int idnum_allocated_count)
	{
		super.write_uint16_t(_idnum_allocated_count, idnum_allocated_count);
	}

	public int next_method_idnum()
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/instanceKlass.hpp#L1175
		int idnum_allocated_count = idnum_allocated_count();
		if (idnum_allocated_count == ConstMethod.MAX_IDNUM)
		{
			return ConstMethod.UNSET_IDNUM;
		}
		else
		{
			set_initial_method_idnum(idnum_allocated_count + 1);
			return idnum_allocated_count;
		}
	}

	/**
	 * 当前的初始化状态。<br>
	 * 返回值为ClassState中定义的值。<br>
	 * 
	 * @return
	 */
	public byte init_state()
	{
		return super.read_byte(_init_state);
	}

	public void set_init_state(byte init_state)
	{
		super.write(_init_state, init_state);
	}

	public boolean is_loaded()
	{
		return init_state() >= ClassState.loaded;
	}

	public boolean is_linked()
	{
		return init_state() >= ClassState.linked;
	}

	public boolean is_initialized()
	{
		return init_state() == ClassState.fully_initialized;
	}

	public boolean is_not_initialized()
	{
		return init_state() < ClassState.being_initialized;
	}

	public boolean is_being_initialized()
	{
		return init_state() == ClassState.being_initialized;
	}

	public boolean is_in_error_state()
	{
		return init_state() == ClassState.initialization_error;
	}

	/**
	 * 获取初始化本类的线程
	 * 
	 * @return
	 */
	public JavaThread init_thread()
	{
		return super.read_memory_object_ptr(JavaThread.class, _init_thread);
	}

	public void set_init_thread(JavaThread init_thread)
	{
		super.write_memory_object_ptr(_init_thread, init_thread);
	}

	public boolean is_reentrant_initialization(jvmsp.hotspot.runtime.Thread thread)
	{
		return init_thread().addr_equals(thread);
	}

	/**
	 * 当前类Klass的引用类型
	 * 返回值为ReferenceType中定义的值。<br>
	 * 
	 * @return
	 */
	public byte reference_type()
	{
		return super.read_byte(_reference_type);
	}

	public void set_reference_type(byte reference_type)
	{
		super.write(_reference_type, reference_type);
	}

	/**
	 * 获取注解
	 * 
	 * @return
	 */
	public Annotations annotations()
	{
		return super.read_memory_object_ptr(Annotations.class, _annotations);
	}

	public void set_annotations(Annotations annotations)
	{
		super.write_memory_object_ptr(_annotations, annotations);
	}

	/**
	 * 获取只由本类实现的接口
	 * 
	 * @return
	 */
	public Array_pInstanceKlass local_interfaces()
	{
		return super.read_memory_object_ptr(Array_pInstanceKlass.class, _local_interfaces);
	}

	public void set_local_interfaces(Array_pInstanceKlass local_interfaces)
	{
		super.write_memory_object_ptr(_local_interfaces, local_interfaces);
	}

	/**
	 * 获取由本类继承来的接口
	 * 
	 * @return
	 */
	public Array_pInstanceKlass transitive_interfaces()
	{
		return super.read_memory_object_ptr(Array_pInstanceKlass.class, _transitive_interfaces);
	}

	public void set_transitive_interfaces(Array_pInstanceKlass transitive_interfaces)
	{
		super.write_memory_object_ptr(_transitive_interfaces, transitive_interfaces);
	}

	/**
	 * 获取接口的默认方法
	 * 
	 * @return
	 */
	public Array_pMethod default_methods()
	{
		return super.read_memory_object_ptr(Array_pMethod.class, _default_methods);
	}

	public void set_default_methods(Array_pMethod default_methods)
	{
		super.write_memory_object_ptr(_default_methods, default_methods);
	}

	/**
	 * 获取class文件中定义的方法的原始顺序
	 * 
	 * @return
	 */
	public Array_int method_ordering()
	{
		return super.read_memory_object_ptr(Array_int.class, _method_ordering);
	}

	public void set_method_ordering(Array_int method_ordering)
	{
		super.write_memory_object_ptr(_method_ordering, method_ordering);
	}

	/**
	 * 方法的第一个断点。<br>
	 * 断点与断点之间是链式存储的。<br>
	 * 
	 * @return
	 */
	public BreakpointInfo breakpoints()
	{
		return super.read_memory_object_ptr(BreakpointInfo.class, _breakpoints);
	}

	public void set_breakpoints(BreakpointInfo breakpoints)
	{
		super.write_memory_object_ptr(_breakpoints, breakpoints);
	}

	/**
	 * 本类第一个静态字段的JNIid。<br>
	 * 链式存储。<br>
	 * 
	 * @return
	 */
	public JNIid jni_ids()
	{
		return super.read_memory_object_ptr(JNIid.class, _jni_ids);
	}

	public void set_jni_ids(JNIid jni_ids)
	{
		super.write_memory_object_ptr(_jni_ids, jni_ids);
	}

	/**
	 * 返回本类所有方法的JNI的jMethodId数组，每个jMethodId实际上是一个指针。<br>
	 * 返回数组长度为method_idnum。
	 * 
	 * @return
	 */
	public long methods_jmethod_ids()
	{
		return super.read_pointer(_methods_jmethod_ids);
	}

	public void set_methods_jmethod_ids(long methods_jmethod_ids)
	{
		super.write_pointer(_methods_jmethod_ids, methods_jmethod_ids);
	}

	public nmethod osr_nmethods_head()
	{
		return super.read_memory_object_ptr(nmethod.class, _osr_nmethods_head);
	}

	public void set_osr_nmethods_head(nmethod osr_nmethods_head)
	{
		super.write_memory_object_ptr(_osr_nmethods_head, osr_nmethods_head);
	}

	/**
	 * 获取本类定义的方法
	 * 
	 * @return
	 */
	public Array_pMethod methods()
	{
		return super.read_memory_object_ptr(Array_pMethod.class, _methods);
	}

	public void set_methods(Array_pMethod methods)
	{
		super.write_memory_object_ptr(_methods, methods);
	}

	public ObjArrayKlass array_klasses()
	{
		return super.read_memory_object_ptr(ObjArrayKlass.class, _array_klasses);
	}

	public void set_array_klasses(ObjArrayKlass array_klasses)
	{
		super.write_memory_object_ptr(_array_klasses, array_klasses);
	}

	public ConstantPool constants()
	{
		return super.read_memory_object_ptr(ConstantPool.class, _constants);
	}

	public void set_constants(ConstantPool cp)
	{
		super.write_memory_object_ptr(_constants, cp);
	}

	/**
	 * Klass对应的源文件名称
	 * 
	 * @return
	 */
	public Symbol source_file_name()
	{
		return constants().source_file_name();
	}

	public int source_file_name_index()
	{
		return constants().source_file_name_index();
	}

	public void set_source_file_name_index(int sourcefile_index)
	{
		constants().set_source_file_name_index(sourcefile_index);
	}

	public boolean try_set_source_file_name(Symbol source_file_name)
	{
		return constants().try_set_source_file_name(source_file_name);
	}

	public AccessFlags access_flags()
	{
		return super.access_flags();
	}

	public boolean is_public()
	{
		return access_flags().is_public();
	}

	public boolean is_final()
	{
		return access_flags().is_final();
	}

	public boolean is_interface()
	{
		return access_flags().is_interface();
	}

	public boolean is_abstract()
	{
		return access_flags().is_abstract();
	}

	public boolean is_super()
	{
		return access_flags().is_super();
	}

	public boolean is_synthetic()
	{
		return access_flags().is_synthetic();
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

	public int itable_length()
	{
		return super.read_cint(_itable_len);
	}

	public void set_itable_length(int itable_length)
	{
		super.write_cint(_itable_len, itable_length);
	}

	/**
	 * 获取类InstanceKlass的运行时JVM定义标志
	 * 
	 * @return
	 */
	public InstanceKlassFlags ik_misc_flags()
	{
		return super.read_memory_object(InstanceKlassFlags.class, _misc_flags_flags);
	}

	public static final int header_size()
	{
		return (int) (size / globalDefinitions.wordSize);
	}

	public static final int size(int vtable_length, int itable_length, int nonstatic_oop_map_size, boolean is_interface)
	{
		return (int) align.align_metadata_size(header_size() +
				vtable_length +
				itable_length +
				nonstatic_oop_map_size +
				(is_interface ? (int) cxx_type.pvoid.size() / globalDefinitions.wordSize : 0));
	}

	public static final int nonstatic_oop_map_size(long oop_map_count)
	{
		return (int) (oop_map_count * OopMapBlock.size_in_words());
	}

	public long nonstatic_oop_map_count()
	{
		return nonstatic_oop_map_size() / OopMapBlock.size_in_words();
	}

	@Override
	public long size()
	{
		return size(vtable_length(),
				itable_length(),
				nonstatic_oop_map_size(),
				is_interface());
	}
}
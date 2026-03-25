package jvmsp.hotspot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jvmsp.memory;
import jvmsp.memory.memory_operator;
import jvmsp.shared_object;
import jvmsp.structs.long_array;
import jvmsp.symbols;
import jvmsp.unsafe;
import jvmsp.versions;
import jvmsp.libso.libjvm;
import jvmsp.type.cxx_type;
import jvmsp.type.java_type;

import static jvmsp.versions.jdk_versions;

/**
 * hotspot虚拟机内部实现结构体访问
 */
@SuppressWarnings("unused")
public abstract class vm_struct extends memory_operator
{
	public static class entry
	{
		// VMStructs信息的起始地址
		private static final long gHotSpotVMStructs;
		// VMStructs数组的元素步长
		private static final long gHotSpotVMStructEntryArrayStride;

		private static final long gHotSpotVMStructEntryTypeNameOffset;
		private static final long gHotSpotVMStructEntryFieldNameOffset;
		private static final long gHotSpotVMStructEntryTypeStringOffset;
		private static final long gHotSpotVMStructEntryIsStaticOffset;
		private static final long gHotSpotVMStructEntryOffsetOffset;
		private static final long gHotSpotVMStructEntryAddressOffset;

		static
		{
			gHotSpotVMStructs = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMStructs"));
			gHotSpotVMStructEntryArrayStride = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMStructEntryArrayStride"));
			gHotSpotVMStructEntryTypeNameOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMStructEntryTypeNameOffset"));
			gHotSpotVMStructEntryFieldNameOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMStructEntryFieldNameOffset"));
			gHotSpotVMStructEntryTypeStringOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMStructEntryTypeStringOffset"));
			gHotSpotVMStructEntryIsStaticOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMStructEntryIsStaticOffset"));
			gHotSpotVMStructEntryOffsetOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMStructEntryOffsetOffset"));
			gHotSpotVMStructEntryAddressOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMStructEntryAddressOffset"));
		}

		public final String type_name;// 字段类型的名称
		public final String field_name;// 字段的名称
		public final String type_string;// 带引号的字段类型名称
		public final boolean is_static;
		public final long offset;// 如果是非静态的，则是成员字段偏移量
		public final long address;// 如果是静态的，则是静态字段的地址

		private entry(long struct_addr)
		{
			this.type_name = unsafe.read_cstr(struct_addr + gHotSpotVMStructEntryTypeNameOffset);
			this.field_name = unsafe.read_cstr(struct_addr + gHotSpotVMStructEntryFieldNameOffset);
			this.type_string = unsafe.read_cstr(struct_addr + gHotSpotVMStructEntryTypeStringOffset);
			this.is_static = unsafe.read_cbool(struct_addr + gHotSpotVMStructEntryIsStaticOffset);
			this.offset = unsafe.read_long(struct_addr + gHotSpotVMStructEntryOffsetOffset);
			this.address = unsafe.read_pointer(struct_addr + gHotSpotVMStructEntryAddressOffset);
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder().append("VMStructEntry [")
					.append("type_name = ").append(type_name)
					.append(", field_name = ").append(field_name)
					.append(", type_string = ").append(type_string)
					.append(", is_static = ").append(is_static)
					.append(", offset = ").append(offset)
					.append(", address = ").append(address)
					.append(']');
			return sb.toString();
		}

		/**
		 * 静态成员则获取其指针，非静态成员获取内存排布的偏移量。<br>
		 * 
		 * @return
		 */
		public final long address_offset()
		{
			return is_static ? address : offset;
		}

		private static final Map<String, Map<String, entry>> vm_struct_entries = new HashMap<>();

		static
		{
			for (int idx = 0;; ++idx)
			{
				entry entry = new entry(gHotSpotVMStructs + idx * gHotSpotVMStructEntryArrayStride);
				vm_struct_entries.computeIfAbsent(entry.type_name, (n) -> new HashMap<>()).put(entry.field_name, entry);
				if (entry.field_name == null)
				{
					break;// 最后一个vm struct为null
				}
			}
		}

		public static final entry find(String type_name, String field_name)
		{
			Map<String, entry> fields = vm_struct_entries.get(type_name);
			if (fields == null)
				return null;
			else
				return fields.get(field_name);
		}

		/**
		 * 针对不同JDK版本，同一个字段名称可能会变化，在此采用别名列表，查找时返回第一个匹配的条目。<br>
		 * 
		 * @param type_name
		 * @param field_name_alias
		 * @return
		 */
		public static final entry find_alias(String type_name, String... field_name_alias)
		{
			Map<String, entry> fields = vm_struct_entries.get(type_name);
			if (fields == null)
			{
				return null;
			}
			else
			{
				for (int idx = 0; idx < field_name_alias.length; ++idx)
				{
					entry e = fields.get(field_name_alias[idx]);
					if (e != null)
					{
						return e;
					}
				}
			}
			return null;
		}
	}

	/**
	 * 导出的VMType
	 */
	private final vm_type type;

	/**
	 * 该类型是否在libjvm.so中导出给Serviceability Agent
	 */
	private final boolean exported;

	protected vm_struct(String name, long address, long idx_base)
	{
		super(address, idx_base);
		this.type = vm_type.get(name);
		this.exported = this.type != null;
	}

	protected vm_struct(long address, long idx_base)
	{
		super(address, idx_base);
		this.type = null;
		this.exported = false;
	}

	protected vm_struct(String name, long address)
	{
		this(name, address, 0);
	}

	protected vm_struct(long address)
	{
		this(address, 0);
	}

	public final boolean is_exported()
	{
		return exported;
	}

	public final long size()
	{
		return exported ? 0 : type.size;
	}

	public final String type_name()
	{
		return exported ? null : type.type_name;
	}

	public final String super_class_name()
	{
		return exported ? null : type.super_class_name;
	}

	public final boolean is_oop_type()
	{
		return exported ? false : type.is_oop_type;
	}

	public final boolean is_integer_type()
	{
		return exported ? false : type.is_integer_type;
	}

	public final boolean is_unsigned()
	{
		return exported ? false : type.is_unsigned;
	}

	public static class ContiguousSpace
	{
		private static final long _end_of_live = vm_struct.entry.find("ContiguousSpace", "_end_of_live").offset;
		private static final long _top = vm_struct.entry.find("ContiguousSpace", "_top").offset;

		private static final long _saved_mark_word = vm_struct.entry.find("ContiguousSpace", "_saved_mark_word").offset;
	}

	public static class Generation
	{
		private static final long _reserved = vm_struct.entry.find("Generation", "_reserved").offset;
		private static final long _virtual_space = vm_struct.entry.find("Generation", "_virtual_space").offset;
		private static final long _stat_record = vm_struct.entry.find("Generation", "_stat_record").offset;
	}

	public static class Generation_StatRecord
	{
		private static final long invocations = vm_struct.entry.find("Generation::StatRecord", "invocations").offset;
		private static final long accumulated_time = vm_struct.entry.find("Generation::StatRecord", "accumulated_time").offset;
	}

	public static class GenerationSpec
	{
		private static final long _name = vm_struct.entry.find("GenerationSpec", "_name").offset;
		private static final long _init_size = vm_struct.entry.find("GenerationSpec", "_init_size").offset;
		private static final long _max_size = vm_struct.entry.find("GenerationSpec", "_max_size").offset;
	}

	public static class GenCollectedHeap
	{
		private static final long _young_gen = vm_struct.entry.find("GenCollectedHeap", "_young_gen").offset;
		private static final long _old_gen = vm_struct.entry.find("GenCollectedHeap", "_old_gen").offset;
		private static final long _young_gen_spec = vm_struct.entry.find("GenCollectedHeap", "_young_gen_spec").offset;
		private static final long _old_gen_spec = vm_struct.entry.find("GenCollectedHeap", "_old_gen_spec").offset;
	}

	public static class MemRegion
	{
		private static final long _start = vm_struct.entry.find("MemRegion", "_start").offset;
		private static final long _word_size = vm_struct.entry.find("MemRegion", "_word_size").offset;
	}

	public static class Space
	{
		private static final long _bottom = vm_struct.entry.find("Space", "_bottom").offset;
		private static final long _end = vm_struct.entry.find("Space", "_end").offset;
	}

	public static class oopDesc
	{
		private static final long _mark = vm_struct.entry.find("oopDesc", "_mark").offset;
		private static final long _metadata_klass = vm_struct.entry.find("oopDesc", "_metadata._klass").offset;
		private static final long _metadata_compressed_klass = vm_struct.entry.find("oopDesc", "_metadata._compressed_klass").offset;

		/**
		 * 计算oopDesc字段偏移量
		 * 
		 * @param oop
		 * @param offset
		 * @return
		 */
		public static final long field_addr(long oop, int offset)
		{
			// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/oop.inline.hpp#L239
			return oop + offset;
		}

		/**
		 * 获取指定偏移量储存的指针值
		 * 
		 * @param oop
		 * @param offset
		 * @return
		 */
		public static final long ptr_field(long oop, int offset)
		{
			return unsafe.read_pointer(field_addr(oop, offset));
		}

		/**
		 * 获取元数据指针
		 * 
		 * @param oop
		 * @param offset
		 * @return
		 */
		public static final long metadata_field(long oop, int offset)
		{
			// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/oop.cpp#L186
			return ptr_field(oop, offset);
		}
	}

	public static class BarrierSet
	{
		private static final long _barrier_set = vm_struct.entry.find("BarrierSet", "_barrier_set").address;
	}

	public static class ArrayKlass
	{
		private static final long _dimension = vm_struct.entry.find("ArrayKlass", "_dimension").offset;
		private static final long _higher_dimension = vm_struct.entry.find("ArrayKlass", "_higher_dimension").offset;
		private static final long _lower_dimension = vm_struct.entry.find("ArrayKlass", "_lower_dimension").offset;
	}

	public static class CompiledICHolder
	{
		private static final long _holder_metadata = vm_struct.entry.find("CompiledICHolder", "_holder_metadata").offset;
		private static final long _holder_klass = vm_struct.entry.find("CompiledICHolder", "_holder_klass").offset;
	}

	public static class ConstantPool
	{
		private static final long _tags = vm_struct.entry.find("ConstantPool", "_tags").offset;
		private static final long _cache = vm_struct.entry.find("ConstantPool", "_cache").offset;
		private static final long _pool_holder = vm_struct.entry.find("ConstantPool", "_pool_holder").offset;
		private static final long _operands = vm_struct.entry.find("ConstantPool", "_operands").offset;
		private static final long _resolved_klasses = vm_struct.entry.find("ConstantPool", "_resolved_klasses").offset;
		private static final long _length = vm_struct.entry.find("ConstantPool", "_length").offset;
		private static final long _minor_version = vm_struct.entry.find("ConstantPool", "_minor_version").offset;
		private static final long _major_version = vm_struct.entry.find("ConstantPool", "_major_version").offset;
		private static final long _generic_signature_index = vm_struct.entry.find("ConstantPool", "_generic_signature_index").offset;
		private static final long _source_file_name_index = vm_struct.entry.find("ConstantPool", "_source_file_name_index").offset;
	}

	public static class ConstantPoolCache
	{
		private static final long _resolved_references = vm_struct.entry.find("ConstantPoolCache", "_resolved_references").offset;
		private static final long _reference_map = vm_struct.entry.find("ConstantPoolCache", "_reference_map").offset;
		private static final long _length = vm_struct.entry.find("ConstantPoolCache", "_length").offset;
		private static final long _constant_pool = vm_struct.entry.find("ConstantPoolCache", "_constant_pool").offset;
		private static final long _resolved_indy_entries = vm_struct.entry.find("ConstantPoolCache", "_resolved_indy_entries").offset;
	}

	public static class ResolvedIndyEntry
	{
		private static final long _cpool_index = vm_struct.entry.find("ResolvedIndyEntry", "_cpool_index").offset;
	}

	public static class InstanceKlass extends Klass
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

		public InstanceKlass(long iklass_ptr)
		{
			super(iklass_ptr);
		}

		public Array_pMethod _methods()
		{
			return super.read_memory_operator_ptr(Array_pMethod.class, _methods);
		}
	}

	/**
	 * JVM运行时Klass内部使用标志。<br>
	 * JDK21尚不存在。<br>
	 */
	public static class KlassFlags extends vm_struct
	{
		public static final cxx_type KlassFlags = cxx_type.define("KlassFlags")
				.decl_field("_flags", cxx_type.uint8_t)
				.resolve();

		private static final long _flags = KlassFlags.field("_flags").offset();

		public static final byte is_hidden_class = 1 << 0;
		public static final byte is_value_based_class = 1 << 1;
		public static final byte has_finalizer = 1 << 2;
		public static final byte is_cloneable_fast = 1 << 3;

		public KlassFlags(long address)
		{
			super(address);
		}

		public String toString()
		{
			return memory.bits_str(_flags());
		}

		public byte _flags()
		{
			return super.read_byte(_flags);
		}

		public void set_flags(byte flags)
		{
			super.write(_flags, flags);
		}

		public boolean is_is_hidden_class()
		{
			return memory.flag_bit(_flags(), is_hidden_class);
		}

		public void set_is_hidden_class(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), is_hidden_class, value));
		}

		public boolean is_is_value_based_class()
		{
			return memory.flag_bit(_flags(), is_value_based_class);
		}

		public void set_is_value_based_class(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), is_value_based_class, value));
		}

		public boolean is_has_finalizer()
		{
			return memory.flag_bit(_flags(), has_finalizer);
		}

		public void set_has_finalizer(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), has_finalizer, value));
		}

		public boolean is_is_cloneable_fast()
		{
			return memory.flag_bit(_flags(), is_cloneable_fast);
		}

		public void set_is_cloneable_fast(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), is_cloneable_fast, value));
		}
	}

	public static class Klass extends MetaspaceObj
	{
		/**
		 * https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/instanceKlass.hpp#944
		 * 对象布局信息，包括对象的大小，此值将用于JVM分配对象。
		 */
		private static final long _layout_helper = vm_struct.entry.find("Klass", "_layout_helper").offset;// 8
		private static final long _super_check_offset = vm_struct.entry.find("Klass", "_super_check_offset").offset;// 20
		private static final long _name = vm_struct.entry.find("Klass", "_name").offset;// 24
		private static final long _secondary_super_cache = vm_struct.entry.find("Klass", "_secondary_super_cache").offset;// 32
		private static final long _secondary_supers = vm_struct.entry.find("Klass", "_secondary_supers").offset;// 40
		private static final long _primary_supers_0 = vm_struct.entry.find("Klass", "_primary_supers[0]").offset;// 48
		private static final long _java_mirror = vm_struct.entry.find("Klass", "_java_mirror").offset;// 112
		private static final long _super = vm_struct.entry.find("Klass", "_super").offset;// 120
		private static final long _subklass = vm_struct.entry.find("Klass", "_subklass").offset;// 128
		private static final long _next_sibling = vm_struct.entry.find("Klass", "_next_sibling").offset;// 136
		private static final long _next_link = vm_struct.entry.find("Klass", "_next_link").offset;// 144
		private static final long _class_loader_data = vm_struct.entry.find("Klass", "_class_loader_data").offset;// 152
		private static final long _vtable_len = vm_struct.entry.find("Klass", "_vtable_len").offset;// 160
		private static final long _access_flags = vm_struct.entry.find("Klass", "_access_flags").offset;// 164

		// 计算相对偏移量，4字节对齐
		/**
		 * 在JDK21中(https://github.com/openjdk/jdk/blob/jdk-21%2B35/src/hotspot/share/oops/klass.hpp#L126)，该字段还是
		 * // Processed access flags, for use by Class.getModifiers.
		 * jint _modifier_flags;
		 * 但在JDK25中(https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/klass.hpp#L126)，该字段已经改为
		 * // Some flags created by the JVM, not in the class file itself,
		 * // are in _misc_flags below.
		 * KlassFlags _misc_flags;
		 */
		private static final long _misc_flags = _super_check_offset - cxx_type._int.size();// 16

		private static final long _kind = _misc_flags - cxx_type._int.size();// 12

		// 继承链缓存的最大Klass数目
		public static final int _primary_super_limit = vm_constant.Klass_primary_super_limit;

		public static final short InstanceKlassKind = 0;
		public static final short InstanceRefKlassKind = 1;
		public static final short InstanceMirrorKlassKind = 2;
		public static final short InstanceClassLoaderKlassKind = 3;
		public static final short InstanceStackChunkKlassKind = 4;
		public static final short TypeArrayKlassKind = 5;
		public static final short ObjArrayKlassKind = 6;
		public static final short UnknownKlassKind = 7;

		public Klass(long klass_ptr)
		{
			super(klass_ptr);
		}

		public static final Klass java_lang_Object = java_lang_Class.as_Klass(Object.class);
		// java.lang.Object的对象布局
		public static final int java_lang_Object_layout_helper = java_lang_Object._layout_helper();

		/**
		 * 如果没有超类，则超类为空指针。<br>
		 * 此对象实际上是Object的超类。<br>
		 */
		public static final Klass nullptr = new Klass(0)
		{
			@Override
			public int _super_check_offset()
			{
				return java_lang_Object._super_check_offset();
			}

			@Override
			public Klass _secondary_super_cache()
			{
				return this;// 0
			}
		};

		public int _layout_helper()
		{
			return super.read_int(_layout_helper);
		}

		public void set_layout_helper(int layout_helper)
		{
			super.write(_layout_helper, layout_helper);
		}

		public static int layout_helper_to_size_helper(int layout_helper)
		{
			return 0;
			// return layout_helper >> LogBytesPerWord;
		}

		public int size_helper()
		{
			return layout_helper_to_size_helper(_layout_helper());
		}

		/**
		 * instanceof判断超类时使用，该值为当前的首要选项，即首先判断是否是该offset对应的Klass*。<br>
		 * 如果首要选项与_secondary_super_cache相同，则从_primary_supers按照继承关系依次查找。<br>
		 * 此偏移量实际对应_primary_supers中的元素偏移量.<br>
		 * 通常来说，此项数值必须是本Klass*所在的元素地址。<br>
		 * 
		 * @return
		 */
		public int _super_check_offset()
		{
			return super.read_int(_super_check_offset);
		}

		public void set_super_check_offset(int super_check_offset)
		{
			super.write(_super_check_offset, super_check_offset);
		}

		/**
		 * Object的klass返回0。<br>
		 * 
		 * @return
		 */
		public Klass _super()
		{
			return super.read_memory_operator_ptr(Klass.class, _super);
		}

		public void set_super(Klass super_klass)
		{
			super.write(_super, super_klass.address());
		}

		public Klass _secondary_super_cache()
		{
			return super.read_memory_operator_ptr(Klass.class, _secondary_super_cache);
		}

		public void set_secondary_super_cache(Klass secondary_super_cache)
		{
			super.write(_secondary_super_cache, secondary_super_cache.address());
		}

		private long _primary_supers_offset(int idx)
		{
			return _primary_supers_0 + idx * unsafe.address_size;
		}

		private long _primary_supers_ptr(int idx)
		{
			return super.read_pointer(_primary_supers_offset(idx));
		}

		/**
		 * 设置instanceof判断使用的超类继承链缓存值，继承链包含自己，即最后一个非nullptr的元素总是自己.<br>
		 * 只保留从本类起上面最多8个超类，且越顶级的超类索引越靠前。<br>
		 * 
		 * @param idx 缓存的Klass*索引，最多缓存8个。
		 * @return
		 */
		public Klass _primary_supers(int idx)
		{
			return super.read_memory_operator_ptr(Klass.class, _primary_supers_offset(idx));
		}

		/**
		 * 设置特定索引缓存的超类Klass*，如果缓存中找不到才重新决议继承链继续向上查找超类。
		 * 
		 * @param idx           索引
		 * @param primary_super 超类
		 */
		public void set_primary_supers(int idx, long primary_super)
		{
			super.write(_primary_supers_offset(idx), primary_super);
		}

		public void set_primary_supers(int idx, Klass primary_super)
		{
			this.set_primary_supers(idx, primary_super.address());
		}

		public int cached_self_klass_idx()
		{
			int direct_super_idx = _primary_super_limit - 1;// 从后往前查找
			for (int idx = direct_super_idx; idx >= 0; --idx)
			{
				if (_primary_supers_ptr(idx) == 0)
				{
					continue;
				}
				else
				{
					direct_super_idx = idx;
					break;
				}
			}
			return direct_super_idx;
		}

		public int cached_direct_super_klass_idx()
		{
			return cached_self_klass_idx() - 1;
		}

		/**
		 * 决议所有的超类，其中顶级超类的Klass* _super为nullptr。<br>
		 * 返回数组至少有一个元素，即该Klass*本身。<br>
		 * 不能使用Object[]数组，否则如果设置了Object的超类后调用此函数更新会引发数组存储类型不匹配。<br>
		 * 
		 * @return
		 */
		public long_array resolve_supers()
		{
			long_array klass_chain = new long_array();
			Klass _current = this;
			while (true)
			{
				klass_chain.add(_current.address);
				Klass _super = _current._super();
				if (_super.address == 0)
				{
					break;
				}
				else
				{
					_current = _super;
				}
			}
			return klass_chain;
		}

		/**
		 * 清空继承链超类缓存
		 */
		public void clear_primary_supers()
		{
			unsafe.memset(this.address + _primary_supers_0, _primary_super_limit * unsafe.address_size, (byte) 0);
		}

		/**
		 * 该类本身或其任意超类修改了Klass* _super字段后，需要使用此方法手动更新该类的继承链。<br>
		 */
		public void resolve_primary_supers()
		{
			long_array supers = resolve_supers();
			int num = Math.min(_primary_super_limit, supers.size());
			this.clear_primary_supers();
			for (int idx = 0; idx < num; ++idx)
			{
				this.set_primary_supers(idx, supers.get(supers.size() - 1 - idx));
			}
			this.set_super_check_offset((int) _primary_supers_offset(num - 1));// 设置为本Klass*所在偏移量。
		}

		/**
		 * 设置直接超类
		 * 
		 * @param super_klass
		 */
		public void set_super_klass(Klass super_klass)
		{
			this.set_super(super_klass);
			this.resolve_primary_supers();
		}

		public Symbol _name()
		{
			return super.read_memory_operator_ptr(Symbol.class, _name);
		}

		public void set_name(Symbol name)
		{
			super.write(_name, name.address());
		}

		public AccessFlags _access_flags()
		{
			return super.read_memory_operator(AccessFlags.class, _access_flags);
		}

		public short _kind()
		{
			return super.read_short(_kind);
		}

		public void set_kind(short kind)
		{
			super.write(_kind, kind);
		}

		public boolean is_instance_klass()
		{
			short kind = _kind();
			return kind >= Klass.InstanceKlassKind && kind < Klass.TypeArrayKlassKind;
		}

		public KlassFlags _misc_flags()
		{
			return super.read_memory_operator(KlassFlags.class, _misc_flags);
		}
	}

	/**
	 * 继承的超类虚方法表
	 */
	public static class klassVtable extends vm_struct
	{
		public klassVtable(long address)
		{
			super(address);
		}
	}

	/**
	 * 接口方法表
	 */
	public static class klassItable extends vm_struct
	{

		public static final cxx_type klassItable = cxx_type.define("klassItable")
				.decl_field("_klass", cxx_type.pvoid)
				.decl_field("_table_offset", cxx_type._int)
				.decl_field("_size_offset_table", cxx_type._int)
				.decl_field("_size_method_table", cxx_type._int)
				.resolve();

		public klassItable(long address)
		{
			super(address);
		}

	}

	public static class vtableEntry
	{
		private static final long _method = vm_struct.entry.find("vtableEntry", "_method").offset;
	}

	public static class MethodData
	{
		private static final long _size = vm_struct.entry.find("MethodData", "_size").offset;
		private static final long _method = vm_struct.entry.find("MethodData", "_method").offset;
		private static final long _data_size = vm_struct.entry.find("MethodData", "_data_size").offset;
		private static final long _data_0 = vm_struct.entry.find("MethodData", "_data[0]").offset;
		private static final long _parameters_type_data_di = vm_struct.entry.find("MethodData", "_parameters_type_data_di").offset;
		private static final long _compiler_counters_nof_decompiles = vm_struct.entry.find("MethodData", "_compiler_counters._nof_decompiles").offset;
		private static final long _compiler_counters_nof_overflow_recompiles = vm_struct.entry.find("MethodData", "_compiler_counters._nof_overflow_recompiles").offset;
		private static final long _compiler_counters_nof_overflow_traps = vm_struct.entry.find("MethodData", "_compiler_counters._nof_overflow_traps").offset;
		private static final long _compiler_counters_trap_hist_array_0 = vm_struct.entry.find("MethodData", "_compiler_counters._trap_hist._array[0]").offset;
		private static final long _eflags = vm_struct.entry.find("MethodData", "_eflags").offset;
		private static final long _arg_local = vm_struct.entry.find("MethodData", "_arg_local").offset;
		private static final long _arg_stack = vm_struct.entry.find("MethodData", "_arg_stack").offset;
		private static final long _arg_returned = vm_struct.entry.find("MethodData", "_arg_returned").offset;
		private static final long _tenure_traps = vm_struct.entry.find("MethodData", "_tenure_traps").offset;
		private static final long _invoke_mask = vm_struct.entry.find("MethodData", "_invoke_mask").offset;
		private static final long _backedge_mask = vm_struct.entry.find("MethodData", "_backedge_mask").offset;
	}

	public static class DataLayout
	{
		private static final long _header_struct_tag = vm_struct.entry.find("DataLayout", "_header._struct._tag").offset;
		private static final long _header_struct_flags = vm_struct.entry.find("DataLayout", "_header._struct._flags").offset;
		private static final long _header_struct_bci = vm_struct.entry.find("DataLayout", "_header._struct._bci").offset;
		private static final long _header_struct_traps = vm_struct.entry.find("DataLayout", "_header._struct._traps").offset;
		private static final long _cells_0 = vm_struct.entry.find("DataLayout", "_cells[0]").offset;
	}

	public static class MethodCounters
	{
		private static final long _invoke_mask = vm_struct.entry.find("MethodCounters", "_invoke_mask").offset;
		private static final long _backedge_mask = vm_struct.entry.find("MethodCounters", "_backedge_mask").offset;
		private static final long _interpreter_throwout_count = vm_struct.entry.find("MethodCounters", "_interpreter_throwout_count").offset;
		private static final long _number_of_breakpoints = vm_struct.entry.find("MethodCounters", "_number_of_breakpoints").offset;
		private static final long _invocation_counter = vm_struct.entry.find("MethodCounters", "_invocation_counter").offset;
		private static final long _backedge_counter = vm_struct.entry.find("MethodCounters", "_backedge_counter").offset;
	}

	/**
	 * 方法的运行时JVM标志
	 */
	public static class MethodFlags extends vm_struct
	{
		public static final cxx_type MethodFlags = cxx_type.define("MethodFlags")
				.decl_field("_status", cxx_type.uint32_t)
				.resolve();

		private static final long _status = MethodFlags.field("_status").offset();

		public static final int has_monitor_bytecodes = 1 << 0;
		public static final int has_jsrs = 1 << 1;
		public static final int is_old = 1 << 2;
		public static final int is_obsolete = 1 << 3;
		public static final int is_deleted = 1 << 4;
		public static final int is_prefixed_native = 1 << 5;
		public static final int monitor_matching = 1 << 6;
		public static final int queued_for_compilation = 1 << 7;
		public static final int is_not_c2_compilable = 1 << 8;
		public static final int is_not_c1_compilable = 1 << 9;
		public static final int is_not_c2_osr_compilable = 1 << 10;
		public static final int force_inline = 1 << 11;
		public static final int dont_inline = 1 << 12;
		public static final int has_loops_flag = 1 << 13;
		public static final int has_loops_flag_init = 1 << 14;
		public static final int on_stack_flag = 1 << 15;

		public MethodFlags(long address)
		{
			super(address);
		}

		public String toString()
		{
			return memory.bits_str(_status());
		}

		public int _status()
		{
			return super.read_int(_status);
		}

		public void set_status(int status)
		{
			super.write(_status, status);
		}

		public boolean is_has_monitor_bytecodes()
		{
			return memory.flag_bit(_status(), has_monitor_bytecodes);
		}

		public void set_has_monitor_bytecodes(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), has_monitor_bytecodes, value));
		}

		public boolean is_has_jsrs()
		{
			return memory.flag_bit(_status(), has_jsrs);
		}

		public void set_has_jsrs(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), has_jsrs, value));
		}

		public boolean is_old()
		{
			return memory.flag_bit(_status(), is_old);
		}

		public void set_is_old(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), is_old, value));
		}

		public boolean is_obsolete()
		{
			return memory.flag_bit(_status(), is_obsolete);
		}

		public void set_is_obsolete(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), is_obsolete, value));
		}

		public boolean is_deleted()
		{
			return memory.flag_bit(_status(), is_deleted);
		}

		public void set_is_deleted(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), is_deleted, value));
		}

		public boolean is_prefixed_native()
		{
			return memory.flag_bit(_status(), is_prefixed_native);
		}

		public void set_is_prefixed_native(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), is_prefixed_native, value));
		}

		public boolean is_monitor_matching()
		{
			return memory.flag_bit(_status(), monitor_matching);
		}

		public void set_monitor_matching(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), monitor_matching, value));
		}

		public boolean is_queued_for_compilation()
		{
			return memory.flag_bit(_status(), queued_for_compilation);
		}

		public void set_queued_for_compilation(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), queued_for_compilation, value));
		}

		public boolean is_not_c2_compilable()
		{
			return memory.flag_bit(_status(), is_not_c2_compilable);
		}

		public void set_is_not_c2_compilable(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), is_not_c2_compilable, value));
		}

		public boolean is_not_c1_compilable()
		{
			return memory.flag_bit(_status(), is_not_c1_compilable);
		}

		public void set_is_not_c1_compilable(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), is_not_c1_compilable, value));
		}

		public boolean is_not_c2_osr_compilable()
		{
			return memory.flag_bit(_status(), is_not_c2_osr_compilable);
		}

		public void set_is_not_c2_osr_compilable(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), is_not_c2_osr_compilable, value));
		}

		public boolean is_force_inline()
		{
			return memory.flag_bit(_status(), force_inline);
		}

		public void set_force_inline(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), force_inline, value));
		}

		public boolean is_dont_inline()
		{
			return memory.flag_bit(_status(), dont_inline);
		}

		public void set_dont_inline(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), dont_inline, value));
		}

		public boolean is_has_loops_flag()
		{
			return memory.flag_bit(_status(), has_loops_flag);
		}

		public void set_has_loops_flag(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), has_loops_flag, value));
		}

		public boolean is_has_loops_flag_init()
		{
			return memory.flag_bit(_status(), has_loops_flag_init);
		}

		public void set_has_loops_flag_init(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), has_loops_flag_init, value));
		}

		public boolean is_on_stack_flag()
		{
			return memory.flag_bit(_status(), on_stack_flag);
		}

		public void set_on_stack_flag(boolean value)
		{
			set_status(memory.set_flag_bit(_status(), on_stack_flag, value));
		}
	}

	/**
	 * 方法的运行时数据
	 */
	public static class Method extends vm_struct
	{
		private static final long _constMethod = vm_struct.entry.find("Method", "_constMethod").offset;
		private static final long _method_data = vm_struct.entry.find("Method", "_method_data").offset;
		private static final long _method_counters = vm_struct.entry.find("Method", "_method_counters").offset;
		private static final long _access_flags = vm_struct.entry.find("Method", "_access_flags").offset;
		private static final long _vtable_index = vm_struct.entry.find("Method", "_vtable_index").offset;
		private static final long _intrinsic_id = vm_struct.entry.find("Method", "_intrinsic_id").offset;
		private static final long _code = vm_struct.entry.find("Method", "_code").offset;
		private static final long _i2i_entry = vm_struct.entry.find("Method", "_i2i_entry").offset;
		private static final long _from_compiled_entry = vm_struct.entry.find("Method", "_from_compiled_entry").offset;
		private static final long _from_interpreted_entry = vm_struct.entry.find("Method", "_from_interpreted_entry").offset;

		// _flags字段位于u2 _intrinsic_id之前
		private static final long _flags = _intrinsic_id - MethodFlags.MethodFlags.size();

		public Method(long address)
		{
			super("Method", address);
		}

		public ConstMethod _constMethod()
		{
			return super.read_memory_operator_ptr(ConstMethod.class, _constMethod);
		}

		public AccessFlags _access_flags()
		{
			return super.read_memory_operator(AccessFlags.class, _access_flags);
		}

		public int _vtable_index()
		{
			return super.read_int(_vtable_index);
		}

		public void set_vtable_index(int vtidx)
		{
			super.write(_vtable_index, vtidx);
		}

		public MethodFlags _flags()
		{
			return super.read_memory_operator(MethodFlags.class, _flags);
		}
	}

	/**
	 * 方法的不可变数据
	 */
	public static class ConstMethod extends MetaspaceObj
	{
		private static final long _fingerprint = vm_struct.entry.find("ConstMethod", "_fingerprint").offset;
		private static final long _constants = vm_struct.entry.find("ConstMethod", "_constants").offset;
		private static final long _stackmap_data = vm_struct.entry.find("ConstMethod", "_stackmap_data").offset;
		private static final long _constMethod_size = vm_struct.entry.find("ConstMethod", "_constMethod_size").offset;
		private static final long _flags_flags = vm_struct.entry.find("ConstMethod", "_flags._flags").offset;
		private static final long _code_size = vm_struct.entry.find("ConstMethod", "_code_size").offset;
		private static final long _name_index = vm_struct.entry.find("ConstMethod", "_name_index").offset;
		private static final long _signature_index = vm_struct.entry.find("ConstMethod", "_signature_index").offset;
		private static final long _method_idnum = vm_struct.entry.find("ConstMethod", "_method_idnum").offset;
		private static final long _max_stack = vm_struct.entry.find("ConstMethod", "_max_stack").offset;
		private static final long _max_locals = vm_struct.entry.find("ConstMethod", "_max_locals").offset;
		private static final long _size_of_parameters = vm_struct.entry.find("ConstMethod", "_size_of_parameters").offset;
		private static final long _num_stack_arg_slots = vm_struct.entry.find("ConstMethod", "_num_stack_arg_slots").offset;

		public ConstMethod(long address)
		{
			super(address);
		}
	}

	public static class ObjArrayKlass
	{
		private static final long _element_klass = vm_struct.entry.find("ObjArrayKlass", "_element_klass").offset;
		private static final long _bottom_klass = vm_struct.entry.find("ObjArrayKlass", "_bottom_klass").offset;
	}

	public static class Symbol extends vm_struct
	{
		private static final long _hash_and_refcount = vm_struct.entry.find("Symbol", "_hash_and_refcount").offset;
		private static final long _length = vm_struct.entry.find("Symbol", "_length").offset;

		/**
		 * 符号的名称，UTF8字符数组。<br>
		 * 计算identity_hash也会使用。<br>
		 * GC不安全。<br>
		 * 尽管定义_body[]长度为2，但实际储存的长度大于2，具体长度为_length字段表明
		 */
		private static final long _body = vm_struct.entry.find("Symbol", "_body").offset;

		private static final long _body_0 = vm_struct.entry.find("Symbol", "_body[0]").offset;

		public Symbol(long address)
		{
			super("Symbol", address);
		}

		public int _hash_and_refcount()
		{
			return super.read_int(_hash_and_refcount);
		}

		public short _length()
		{
			return super.read_short(_length);
		}

		public byte get_body(int idx)
		{
			return super.read_byte(_body + idx);
		}

		/**
		 * 读取_body[]全部字节
		 * 
		 * @return
		 */
		public byte[] bytes()
		{
			byte[] b = new byte[_length()];
			unsafe.memcpy(base(), b, 0, b.length);
			return b;
		}

		public long base()
		{
			return this.address + _body;
		}

		/**
		 * 获取符号名称为C UTF-8字符串。<br>
		 * 
		 * @return
		 */
		public long cstr()
		{
			return base();
		}

		/**
		 * 获取符号名称为Java字符串。<br>
		 * 
		 * @return
		 */
		public String jstring()
		{
			return memory.string(cstr());
		}

		@Override
		public String toString()
		{
			return jstring();
		}
	}

	public static class TypeArrayKlass
	{
		private static final long _max_length = vm_struct.entry.find("TypeArrayKlass", "_max_length").offset;
	}

	public static class OopHandle
	{
		private static final long _obj = vm_struct.entry.find("OopHandle", "_obj").offset;
	}

	public static class ConstantPoolCacheEntry
	{
		private static final long _indices = vm_struct.entry.find("ConstantPoolCacheEntry", "_indices").offset;
		private static final long _f1 = vm_struct.entry.find("ConstantPoolCacheEntry", "_f1").offset;
		private static final long _f2 = vm_struct.entry.find("ConstantPoolCacheEntry", "_f2").offset;
		private static final long _flags = vm_struct.entry.find("ConstantPoolCacheEntry", "_flags").offset;
	}

	public static class CheckedExceptionElement
	{
		private static final long class_cp_index = vm_struct.entry.find("CheckedExceptionElement", "class_cp_index").offset;
	}

	public static class LocalVariableTableElement
	{
		private static final long start_bci = vm_struct.entry.find("LocalVariableTableElement", "start_bci").offset;
		private static final long length = vm_struct.entry.find("LocalVariableTableElement", "length").offset;
		private static final long name_cp_index = vm_struct.entry.find("LocalVariableTableElement", "name_cp_index").offset;
		private static final long descriptor_cp_index = vm_struct.entry.find("LocalVariableTableElement", "descriptor_cp_index").offset;
		private static final long signature_cp_index = vm_struct.entry.find("LocalVariableTableElement", "signature_cp_index").offset;
		private static final long slot = vm_struct.entry.find("LocalVariableTableElement", "slot").offset;
	}

	public static class ExceptionTableElement
	{
		private static final long start_pc = vm_struct.entry.find("ExceptionTableElement", "start_pc").offset;
		private static final long end_pc = vm_struct.entry.find("ExceptionTableElement", "end_pc").offset;
		private static final long handler_pc = vm_struct.entry.find("ExceptionTableElement", "handler_pc").offset;
		private static final long catch_type_index = vm_struct.entry.find("ExceptionTableElement", "catch_type_index").offset;
	}

	public static class BreakpointInfo
	{
		private static final long _orig_bytecode = vm_struct.entry.find("BreakpointInfo", "_orig_bytecode").offset;
		private static final long _bci = vm_struct.entry.find("BreakpointInfo", "_bci").offset;
		private static final long _name_index = vm_struct.entry.find("BreakpointInfo", "_name_index").offset;
		private static final long _signature_index = vm_struct.entry.find("BreakpointInfo", "_signature_index").offset;
		private static final long _next = vm_struct.entry.find("BreakpointInfo", "_next").offset;
	}

	public static class JNIid
	{
		private static final long _holder = vm_struct.entry.find("JNIid", "_holder").offset;
		private static final long _next = vm_struct.entry.find("JNIid", "_next").offset;
		private static final long _offset = vm_struct.entry.find("JNIid", "_offset").offset;
	}

	public static class Universe
	{
		private static final long _collectedHeap = vm_struct.entry.find("Universe", "_collectedHeap").address;// 静态，堆的基地址

		public static final long _collectedHeap()
		{
			return unsafe.read_pointer(_collectedHeap);
		}
	}

	public static class CompressedOops
	{
		private static final long _base = jdk_versions.switch_execute(
				() -> vm_struct.entry.find("CompressedOops", "_narrow_oop._base").address, // JDK21
				() -> vm_struct.entry.find("CompressedOops", "_base").address// JDK25
		);

		private static final long _shift = jdk_versions.switch_execute(
				() -> vm_struct.entry.find("CompressedOops", "_narrow_oop._shift").address, // JDK21
				() -> vm_struct.entry.find("CompressedOops", "_shift").address// JDK25
		);

		public static final long _narrow_oop_base()
		{
			return unsafe.read_pointer(_base);
		}

		public static final int _narrow_oop_shift()
		{
			return unsafe.read_int(_shift);
		}

		public static final int encode(long native_addr)
		{
			return (int) ((native_addr - _narrow_oop_base()) >> _narrow_oop_shift());
		}

		public static final long decode(int oop)
		{
			return _narrow_oop_base() + ((oop & cxx_type.uint32_t_mask) << _narrow_oop_shift());
		}
	}

	public static class CompressedKlassPointers
	{
		private static final long _base = jdk_versions.switch_execute(
				() -> vm_struct.entry.find("CompressedKlassPointers", "_narrow_klass._base").address, // JDK21
				() -> vm_struct.entry.find("CompressedKlassPointers", "_base").address// JDK25
		);

		private static final long _shift = jdk_versions.switch_execute(
				() -> vm_struct.entry.find("CompressedKlassPointers", "_narrow_klass._shift").address, // JDK21
				() -> vm_struct.entry.find("CompressedKlassPointers", "_shift").address// JDK25
		);

		public static final long _narrow_klass_base()
		{
			return unsafe.read_pointer(_base);
		}

		public static final int _narrow_klass_shift()
		{
			return unsafe.read_int(_shift);
		}

		public static final int encode(long klass_ptr)
		{
			// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/compressedKlass.inline.hpp#L39
			return (int) ((klass_ptr - _narrow_klass_base()) >> _narrow_klass_shift());
		}

		public static final long decode(int narrow_klass)
		{
			// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/compressedKlass.inline.hpp#L35
			return _narrow_klass_base() + ((narrow_klass & cxx_type.uint32_t_mask) << _narrow_klass_shift());
		}
	}

	public static class MetaspaceObj extends vm_struct
	{

		public static final long _shared_metaspace_base = vm_struct.entry.find("MetaspaceObj", "_shared_metaspace_base").address;
		public static final long _shared_metaspace_top = vm_struct.entry.find("MetaspaceObj", "_shared_metaspace_top").address;

		public MetaspaceObj(long address)
		{
			super("MetaspaceObj", address);
		}

		public static final long _shared_metaspace_base()
		{
			return unsafe.read_pointer(_shared_metaspace_base);
		}

		public static final long _shared_metaspace_top()
		{
			return unsafe.read_pointer(_shared_metaspace_top);
		}
	}

	public static class ThreadLocalAllocBuffer
	{
		private static final long _start = vm_struct.entry.find("ThreadLocalAllocBuffer", "_start").offset;
		private static final long _top = vm_struct.entry.find("ThreadLocalAllocBuffer", "_top").offset;
		private static final long _end = vm_struct.entry.find("ThreadLocalAllocBuffer", "_end").offset;
		private static final long _pf_top = vm_struct.entry.find("ThreadLocalAllocBuffer", "_pf_top").offset;
		private static final long _desired_size = vm_struct.entry.find("ThreadLocalAllocBuffer", "_desired_size").offset;
		private static final long _refill_waste_limit = vm_struct.entry.find("ThreadLocalAllocBuffer", "_refill_waste_limit").offset;
		private static final long _reserve_for_allocation_prefetch = vm_struct.entry.find("ThreadLocalAllocBuffer", "_reserve_for_allocation_prefetch").address;
		private static final long _target_refills = vm_struct.entry.find("ThreadLocalAllocBuffer", "_target_refills").address;
		private static final long _number_of_refills = vm_struct.entry.find("ThreadLocalAllocBuffer", "_number_of_refills").offset;
		private static final long _refill_waste = vm_struct.entry.find("ThreadLocalAllocBuffer", "_refill_waste").offset;
		private static final long _gc_waste = vm_struct.entry.find("ThreadLocalAllocBuffer", "_gc_waste").offset;
		private static final long _slow_allocations = vm_struct.entry.find("ThreadLocalAllocBuffer", "_slow_allocations").offset;
	}

	public static class VirtualSpace
	{
		private static final long _low_boundary = vm_struct.entry.find("VirtualSpace", "_low_boundary").offset;
		private static final long _high_boundary = vm_struct.entry.find("VirtualSpace", "_high_boundary").offset;
		private static final long _low = vm_struct.entry.find("VirtualSpace", "_low").offset;
		private static final long _high = vm_struct.entry.find("VirtualSpace", "_high").offset;
		private static final long _lower_high = vm_struct.entry.find("VirtualSpace", "_lower_high").offset;
		private static final long _middle_high = vm_struct.entry.find("VirtualSpace", "_middle_high").offset;
		private static final long _upper_high = vm_struct.entry.find("VirtualSpace", "_upper_high").offset;
	}

	public static class PerfDataPrologue
	{
		private static final long magic = vm_struct.entry.find("PerfDataPrologue", "magic").offset;
		private static final long byte_order = vm_struct.entry.find("PerfDataPrologue", "byte_order").offset;
		private static final long major_version = vm_struct.entry.find("PerfDataPrologue", "major_version").offset;
		private static final long minor_version = vm_struct.entry.find("PerfDataPrologue", "minor_version").offset;
		private static final long accessible = vm_struct.entry.find("PerfDataPrologue", "accessible").offset;
		private static final long used = vm_struct.entry.find("PerfDataPrologue", "used").offset;
		private static final long overflow = vm_struct.entry.find("PerfDataPrologue", "overflow").offset;
		private static final long mod_time_stamp = vm_struct.entry.find("PerfDataPrologue", "mod_time_stamp").offset;
		private static final long entry_offset = vm_struct.entry.find("PerfDataPrologue", "entry_offset").offset;
		private static final long num_entries = vm_struct.entry.find("PerfDataPrologue", "num_entries").offset;
	}

	public static class PerfDataEntry
	{
		private static final long entry_length = vm_struct.entry.find("PerfDataEntry", "entry_length").offset;
		private static final long name_offset = vm_struct.entry.find("PerfDataEntry", "name_offset").offset;
		private static final long vector_length = vm_struct.entry.find("PerfDataEntry", "vector_length").offset;
		private static final long data_type = vm_struct.entry.find("PerfDataEntry", "data_type").offset;
		private static final long flags = vm_struct.entry.find("PerfDataEntry", "flags").offset;
		private static final long data_units = vm_struct.entry.find("PerfDataEntry", "data_units").offset;
		private static final long data_variability = vm_struct.entry.find("PerfDataEntry", "data_variability").offset;
		private static final long data_offset = vm_struct.entry.find("PerfDataEntry", "data_offset").offset;
	}

	public static class PerfMemory
	{
		private static final long _start = vm_struct.entry.find("PerfMemory", "_start").address;
		private static final long _end = vm_struct.entry.find("PerfMemory", "_end").address;
		private static final long _top = vm_struct.entry.find("PerfMemory", "_top").address;
		private static final long _capacity = vm_struct.entry.find("PerfMemory", "_capacity").address;
		private static final long _prologue = vm_struct.entry.find("PerfMemory", "_prologue").address;
		private static final long _initialized = vm_struct.entry.find("PerfMemory", "_initialized").address;
	}

	public static class vmClasses
	{
		private static final long _klasses_Object_klass_knum = vm_struct.entry.find("vmClasses", "_klasses[static_cast<int>(vmClassID::Object_klass_knum)]").address;
		private static final long _klasses_String_klass_knum = vm_struct.entry.find("vmClasses", "_klasses[static_cast<int>(vmClassID::String_klass_knum)]").address;
		private static final long _klasses_Class_klass_knum = vm_struct.entry.find("vmClasses", "_klasses[static_cast<int>(vmClassID::Class_klass_knum)]").address;
		private static final long _klasses_ClassLoader_klass_knum = vm_struct.entry.find("vmClasses", "_klasses[static_cast<int>(vmClassID::ClassLoader_klass_knum)]").address;
		private static final long _klasses_System_klass_knum = vm_struct.entry.find("vmClasses", "_klasses[static_cast<int>(vmClassID::System_klass_knum)]").address;
		private static final long _klasses_Thread_klass_knum = vm_struct.entry.find("vmClasses", "_klasses[static_cast<int>(vmClassID::Thread_klass_knum)]").address;
		private static final long _klasses_Thread_FieldHolder_klass_knum = vm_struct.entry.find("vmClasses", "_klasses[static_cast<int>(vmClassID::Thread_FieldHolder_klass_knum)]").address;
		private static final long _klasses_ThreadGroup_klass_knum = vm_struct.entry.find("vmClasses", "_klasses[static_cast<int>(vmClassID::ThreadGroup_klass_knum)]").address;
		private static final long _klasses_MethodHandle_klass_knum = vm_struct.entry.find("vmClasses", "_klasses[static_cast<int>(vmClassID::MethodHandle_klass_knum)]").address;
	}

	public static class ClassLoaderData
	{
		private static final long _class_loader = vm_struct.entry.find("ClassLoaderData", "_class_loader").offset;
		private static final long _next = vm_struct.entry.find("ClassLoaderData", "_next").offset;
		private static final long _klasses = vm_struct.entry.find("ClassLoaderData", "_klasses").offset;
		private static final long _has_class_mirror_holder = vm_struct.entry.find("ClassLoaderData", "_has_class_mirror_holder").offset;
	}

	public static class ClassLoaderDataGraph
	{
		private static final long _head = vm_struct.entry.find("ClassLoaderDataGraph", "_head").address;
	}

	public static class Array_Klass_
	{
		private static final long _length = vm_struct.entry.find("Array<Klass*>", "_length").offset;
		private static final long _data_0 = vm_struct.entry.find("Array<Klass*>", "_data[0]").offset;
	}

	public static class Array_ResolvedIndyEntry_
	{
		private static final long _length = vm_struct.entry.find("Array<ResolvedIndyEntry>", "_length").offset;
		private static final long _data_0 = vm_struct.entry.find("Array<ResolvedIndyEntry>", "_data[0]").offset;
	}

	public static class GrowableArrayBase
	{
		private static final long _len = vm_struct.entry.find("GrowableArrayBase", "_len").offset;
		private static final long _capacity = vm_struct.entry.find("GrowableArrayBase", "_capacity").offset;
	}

	public static class GrowableArray_int_
	{
		private static final long _data = vm_struct.entry.find("GrowableArray<int>", "_data").offset;
	}

	public static class CodeCache
	{
		private static final long _heaps = vm_struct.entry.find("CodeCache", "_heaps").address;
		private static final long _low_bound = vm_struct.entry.find("CodeCache", "_low_bound").address;
		private static final long _high_bound = vm_struct.entry.find("CodeCache", "_high_bound").address;
	}

	public static class CodeHeap
	{
		private static final long _memory = vm_struct.entry.find("CodeHeap", "_memory").offset;
		private static final long _segmap = vm_struct.entry.find("CodeHeap", "_segmap").offset;
		private static final long _log2_segment_size = vm_struct.entry.find("CodeHeap", "_log2_segment_size").offset;
	}

	public static class HeapBlock
	{
		private static final long _header = vm_struct.entry.find("HeapBlock", "_header").offset;
	}

	public static class HeapBlock_Header
	{
		private static final long _length = vm_struct.entry.find("HeapBlock::Header", "_length").offset;
		private static final long _used = vm_struct.entry.find("HeapBlock::Header", "_used").offset;
	}

	public static class AbstractInterpreter
	{
		private static final long _code = vm_struct.entry.find("AbstractInterpreter", "_code").address;
	}

	public static class StubQueue
	{
		private static final long _stub_buffer = vm_struct.entry.find("StubQueue", "_stub_buffer").offset;
		private static final long _buffer_limit = vm_struct.entry.find("StubQueue", "_buffer_limit").offset;
		private static final long _queue_begin = vm_struct.entry.find("StubQueue", "_queue_begin").offset;
		private static final long _queue_end = vm_struct.entry.find("StubQueue", "_queue_end").offset;
		private static final long _number_of_stubs = vm_struct.entry.find("StubQueue", "_number_of_stubs").offset;
	}

	public static class InterpreterCodelet
	{
		private static final long _size = vm_struct.entry.find("InterpreterCodelet", "_size").offset;
		private static final long _description = vm_struct.entry.find("InterpreterCodelet", "_description").offset;
		private static final long _bytecode = vm_struct.entry.find("InterpreterCodelet", "_bytecode").offset;
	}

	public static class StubRoutines
	{
		private static final long _verify_oop_count = vm_struct.entry.find("StubRoutines", "_verify_oop_count").address;
		private static final long _call_stub_return_address = vm_struct.entry.find("StubRoutines", "_call_stub_return_address").address;
		private static final long _aescrypt_encryptBlock = vm_struct.entry.find("StubRoutines", "_aescrypt_encryptBlock").address;
		private static final long _aescrypt_decryptBlock = vm_struct.entry.find("StubRoutines", "_aescrypt_decryptBlock").address;
		private static final long _cipherBlockChaining_encryptAESCrypt = vm_struct.entry.find("StubRoutines", "_cipherBlockChaining_encryptAESCrypt").address;
		private static final long _cipherBlockChaining_decryptAESCrypt = vm_struct.entry.find("StubRoutines", "_cipherBlockChaining_decryptAESCrypt").address;
		private static final long _electronicCodeBook_encryptAESCrypt = vm_struct.entry.find("StubRoutines", "_electronicCodeBook_encryptAESCrypt").address;
		private static final long _electronicCodeBook_decryptAESCrypt = vm_struct.entry.find("StubRoutines", "_electronicCodeBook_decryptAESCrypt").address;
		private static final long _counterMode_AESCrypt = vm_struct.entry.find("StubRoutines", "_counterMode_AESCrypt").address;
		private static final long _galoisCounterMode_AESCrypt = vm_struct.entry.find("StubRoutines", "_galoisCounterMode_AESCrypt").address;
		private static final long _ghash_processBlocks = vm_struct.entry.find("StubRoutines", "_ghash_processBlocks").address;
		private static final long _chacha20Block = vm_struct.entry.find("StubRoutines", "_chacha20Block").address;
		private static final long _base64_encodeBlock = vm_struct.entry.find("StubRoutines", "_base64_encodeBlock").address;
		private static final long _base64_decodeBlock = vm_struct.entry.find("StubRoutines", "_base64_decodeBlock").address;
		private static final long _poly1305_processBlocks = vm_struct.entry.find("StubRoutines", "_poly1305_processBlocks").address;
		private static final long _updateBytesCRC32 = vm_struct.entry.find("StubRoutines", "_updateBytesCRC32").address;
		private static final long _crc_table_adr = vm_struct.entry.find("StubRoutines", "_crc_table_adr").address;
		private static final long _crc32c_table_addr = vm_struct.entry.find("StubRoutines", "_crc32c_table_addr").address;
		private static final long _updateBytesCRC32C = vm_struct.entry.find("StubRoutines", "_updateBytesCRC32C").address;
		private static final long _updateBytesAdler32 = vm_struct.entry.find("StubRoutines", "_updateBytesAdler32").address;
		private static final long _multiplyToLen = vm_struct.entry.find("StubRoutines", "_multiplyToLen").address;
		private static final long _squareToLen = vm_struct.entry.find("StubRoutines", "_squareToLen").address;
		private static final long _bigIntegerRightShiftWorker = vm_struct.entry.find("StubRoutines", "_bigIntegerRightShiftWorker").address;
		private static final long _bigIntegerLeftShiftWorker = vm_struct.entry.find("StubRoutines", "_bigIntegerLeftShiftWorker").address;
		private static final long _mulAdd = vm_struct.entry.find("StubRoutines", "_mulAdd").address;
		private static final long _dexp = vm_struct.entry.find("StubRoutines", "_dexp").address;
		private static final long _dlog = vm_struct.entry.find("StubRoutines", "_dlog").address;
		private static final long _dlog10 = vm_struct.entry.find("StubRoutines", "_dlog10").address;
		private static final long _dpow = vm_struct.entry.find("StubRoutines", "_dpow").address;
		private static final long _dsin = vm_struct.entry.find("StubRoutines", "_dsin").address;
		private static final long _dcos = vm_struct.entry.find("StubRoutines", "_dcos").address;
		private static final long _dtan = vm_struct.entry.find("StubRoutines", "_dtan").address;
		private static final long _vectorizedMismatch = vm_struct.entry.find("StubRoutines", "_vectorizedMismatch").address;
		private static final long _jbyte_arraycopy = vm_struct.entry.find("StubRoutines", "_jbyte_arraycopy").address;
		private static final long _jshort_arraycopy = vm_struct.entry.find("StubRoutines", "_jshort_arraycopy").address;
		private static final long _jint_arraycopy = vm_struct.entry.find("StubRoutines", "_jint_arraycopy").address;
		private static final long _jlong_arraycopy = vm_struct.entry.find("StubRoutines", "_jlong_arraycopy").address;
		private static final long _oop_arraycopy = vm_struct.entry.find("StubRoutines", "_oop_arraycopy").address;
		private static final long _oop_arraycopy_uninit = vm_struct.entry.find("StubRoutines", "_oop_arraycopy_uninit").address;
		private static final long _jbyte_disjoint_arraycopy = vm_struct.entry.find("StubRoutines", "_jbyte_disjoint_arraycopy").address;
		private static final long _jshort_disjoint_arraycopy = vm_struct.entry.find("StubRoutines", "_jshort_disjoint_arraycopy").address;
		private static final long _jint_disjoint_arraycopy = vm_struct.entry.find("StubRoutines", "_jint_disjoint_arraycopy").address;
		private static final long _jlong_disjoint_arraycopy = vm_struct.entry.find("StubRoutines", "_jlong_disjoint_arraycopy").address;
		private static final long _oop_disjoint_arraycopy = vm_struct.entry.find("StubRoutines", "_oop_disjoint_arraycopy").address;
		private static final long _oop_disjoint_arraycopy_uninit = vm_struct.entry.find("StubRoutines", "_oop_disjoint_arraycopy_uninit").address;
		private static final long _arrayof_jbyte_arraycopy = vm_struct.entry.find("StubRoutines", "_arrayof_jbyte_arraycopy").address;
		private static final long _arrayof_jshort_arraycopy = vm_struct.entry.find("StubRoutines", "_arrayof_jshort_arraycopy").address;
		private static final long _arrayof_jint_arraycopy = vm_struct.entry.find("StubRoutines", "_arrayof_jint_arraycopy").address;
		private static final long _arrayof_jlong_arraycopy = vm_struct.entry.find("StubRoutines", "_arrayof_jlong_arraycopy").address;
		private static final long _arrayof_oop_arraycopy = vm_struct.entry.find("StubRoutines", "_arrayof_oop_arraycopy").address;
		private static final long _arrayof_oop_arraycopy_uninit = vm_struct.entry.find("StubRoutines", "_arrayof_oop_arraycopy_uninit").address;
		private static final long _arrayof_jbyte_disjoint_arraycopy = vm_struct.entry.find("StubRoutines", "_arrayof_jbyte_disjoint_arraycopy").address;
		private static final long _arrayof_jshort_disjoint_arraycopy = vm_struct.entry.find("StubRoutines", "_arrayof_jshort_disjoint_arraycopy").address;
		private static final long _arrayof_jint_disjoint_arraycopy = vm_struct.entry.find("StubRoutines", "_arrayof_jint_disjoint_arraycopy").address;
		private static final long _arrayof_jlong_disjoint_arraycopy = vm_struct.entry.find("StubRoutines", "_arrayof_jlong_disjoint_arraycopy").address;
		private static final long _arrayof_oop_disjoint_arraycopy = vm_struct.entry.find("StubRoutines", "_arrayof_oop_disjoint_arraycopy").address;
		private static final long _arrayof_oop_disjoint_arraycopy_uninit = vm_struct.entry.find("StubRoutines", "_arrayof_oop_disjoint_arraycopy_uninit").address;
		private static final long _checkcast_arraycopy = vm_struct.entry.find("StubRoutines", "_checkcast_arraycopy").address;
		private static final long _checkcast_arraycopy_uninit = vm_struct.entry.find("StubRoutines", "_checkcast_arraycopy_uninit").address;
		private static final long _unsafe_arraycopy = vm_struct.entry.find("StubRoutines", "_unsafe_arraycopy").address;
		private static final long _generic_arraycopy = vm_struct.entry.find("StubRoutines", "_generic_arraycopy").address;

		public static final long _generic_arraycopy()
		{
			return unsafe.read_pointer(_generic_arraycopy);
		}
	}

	public static class SharedRuntime
	{
		private static final long _wrong_method_blob = vm_struct.entry.find("SharedRuntime", "_wrong_method_blob").address;
		private static final long _ic_miss_blob = vm_struct.entry.find("SharedRuntime", "_ic_miss_blob").address;
		private static final long _deopt_blob = vm_struct.entry.find("SharedRuntime", "_deopt_blob").address;
	}

	public static class PcDesc
	{
		private static final long _pc_offset = vm_struct.entry.find("PcDesc", "_pc_offset").offset;
		private static final long _scope_decode_offset = vm_struct.entry.find("PcDesc", "_scope_decode_offset").offset;
		private static final long _obj_decode_offset = vm_struct.entry.find("PcDesc", "_obj_decode_offset").offset;
		private static final long _flags = vm_struct.entry.find("PcDesc", "_flags").offset;
	}

	public static class CodeBlob
	{
		private static final long _name = vm_struct.entry.find("CodeBlob", "_name").offset;
		private static final long _size = vm_struct.entry.find("CodeBlob", "_size").offset;
		private static final long _header_size = vm_struct.entry.find("CodeBlob", "_header_size").offset;
		private static final long _frame_complete_offset = vm_struct.entry.find("CodeBlob", "_frame_complete_offset").offset;
		private static final long _data_offset = vm_struct.entry.find("CodeBlob", "_data_offset").offset;
		private static final long _frame_size = vm_struct.entry.find("CodeBlob", "_frame_size").offset;
		private static final long _oop_maps = vm_struct.entry.find("CodeBlob", "_oop_maps").offset;
		private static final long _code_begin = vm_struct.entry.find("CodeBlob", "_code_begin").offset;
		private static final long _code_end = vm_struct.entry.find("CodeBlob", "_code_end").offset;
		private static final long _content_begin = vm_struct.entry.find("CodeBlob", "_content_begin").offset;
		private static final long _data_end = vm_struct.entry.find("CodeBlob", "_data_end").offset;
	}

	public static class DeoptimizationBlob
	{
		private static final long _unpack_offset = vm_struct.entry.find("DeoptimizationBlob", "_unpack_offset").offset;
	}

	public static class RuntimeStub
	{
		private static final long _caller_must_gc_arguments = vm_struct.entry.find("RuntimeStub", "_caller_must_gc_arguments").offset;
	}

	public static class CompiledMethod
	{
		private static final long _method = vm_struct.entry.find("CompiledMethod", "_method").offset;
		private static final long _exception_cache = vm_struct.entry.find("CompiledMethod", "_exception_cache").offset;
		private static final long _scopes_data_begin = vm_struct.entry.find("CompiledMethod", "_scopes_data_begin").offset;
		private static final long _deopt_handler_begin = vm_struct.entry.find("CompiledMethod", "_deopt_handler_begin").offset;
		private static final long _deopt_mh_handler_begin = vm_struct.entry.find("CompiledMethod", "_deopt_mh_handler_begin").offset;
	}

	public static class nmethod
	{
		private static final long _entry_bci = vm_struct.entry.find("nmethod", "_entry_bci").offset;
		private static final long _osr_link = vm_struct.entry.find("nmethod", "_osr_link").offset;
		private static final long _state = vm_struct.entry.find("nmethod", "_state").offset;
		private static final long _exception_offset = vm_struct.entry.find("nmethod", "_exception_offset").offset;
		private static final long _orig_pc_offset = vm_struct.entry.find("nmethod", "_orig_pc_offset").offset;
		private static final long _stub_offset = vm_struct.entry.find("nmethod", "_stub_offset").offset;
		private static final long _consts_offset = vm_struct.entry.find("nmethod", "_consts_offset").offset;
		private static final long _oops_offset = vm_struct.entry.find("nmethod", "_oops_offset").offset;
		private static final long _metadata_offset = vm_struct.entry.find("nmethod", "_metadata_offset").offset;
		private static final long _scopes_pcs_offset = vm_struct.entry.find("nmethod", "_scopes_pcs_offset").offset;
		private static final long _dependencies_offset = vm_struct.entry.find("nmethod", "_dependencies_offset").offset;
		private static final long _handler_table_offset = vm_struct.entry.find("nmethod", "_handler_table_offset").offset;
		private static final long _nul_chk_table_offset = vm_struct.entry.find("nmethod", "_nul_chk_table_offset").offset;
		private static final long _nmethod_end_offset = vm_struct.entry.find("nmethod", "_nmethod_end_offset").offset;
		private static final long _entry_point = vm_struct.entry.find("nmethod", "_entry_point").offset;
		private static final long _verified_entry_point = vm_struct.entry.find("nmethod", "_verified_entry_point").offset;
		private static final long _osr_entry_point = vm_struct.entry.find("nmethod", "_osr_entry_point").offset;
		private static final long _compile_id = vm_struct.entry.find("nmethod", "_compile_id").offset;
		private static final long _comp_level = vm_struct.entry.find("nmethod", "_comp_level").offset;
	}

	public static class Deoptimization
	{
		private static final long _trap_reason_name = vm_struct.entry.find("Deoptimization", "_trap_reason_name").address;
	}

	public static class Deoptimization_UnrollBlock
	{
		private static final long _size_of_deoptimized_frame = vm_struct.entry.find("Deoptimization::UnrollBlock", "_size_of_deoptimized_frame").offset;
		private static final long _caller_adjustment = vm_struct.entry.find("Deoptimization::UnrollBlock", "_caller_adjustment").offset;
		private static final long _number_of_frames = vm_struct.entry.find("Deoptimization::UnrollBlock", "_number_of_frames").offset;
		private static final long _total_frame_sizes = vm_struct.entry.find("Deoptimization::UnrollBlock", "_total_frame_sizes").offset;
		private static final long _unpack_kind = vm_struct.entry.find("Deoptimization::UnrollBlock", "_unpack_kind").offset;
		private static final long _frame_sizes = vm_struct.entry.find("Deoptimization::UnrollBlock", "_frame_sizes").offset;
		private static final long _frame_pcs = vm_struct.entry.find("Deoptimization::UnrollBlock", "_frame_pcs").offset;
		private static final long _register_block = vm_struct.entry.find("Deoptimization::UnrollBlock", "_register_block").offset;
		private static final long _return_type = vm_struct.entry.find("Deoptimization::UnrollBlock", "_return_type").offset;
		private static final long _initial_info = vm_struct.entry.find("Deoptimization::UnrollBlock", "_initial_info").offset;
		private static final long _caller_actual_parameters = vm_struct.entry.find("Deoptimization::UnrollBlock", "_caller_actual_parameters").offset;
	}

	public static class JavaCallWrapper
	{
		private static final long _anchor = vm_struct.entry.find("JavaCallWrapper", "_anchor").offset;
	}

	public static class JavaFrameAnchor
	{
		private static final long _last_Java_sp = vm_struct.entry.find("JavaFrameAnchor", "_last_Java_sp").offset;
		private static final long _last_Java_pc = vm_struct.entry.find("JavaFrameAnchor", "_last_Java_pc").offset;
		private static final long _last_Java_fp = vm_struct.entry.find("JavaFrameAnchor", "_last_Java_fp").offset;
	}

	public static class Threads
	{
		private static final long _number_of_threads = vm_struct.entry.find("Threads", "_number_of_threads").address;
		private static final long _number_of_non_daemon_threads = vm_struct.entry.find("Threads", "_number_of_non_daemon_threads").address;
		private static final long _return_code = vm_struct.entry.find("Threads", "_return_code").address;
	}

	public static class ThreadsSMRSupport
	{
		private static final long _java_thread_list = vm_struct.entry.find("ThreadsSMRSupport", "_java_thread_list").address;
	}

	public static class ThreadsList
	{
		private static final long _length = vm_struct.entry.find("ThreadsList", "_length").offset;
		private static final long _threads = vm_struct.entry.find("ThreadsList", "_threads").offset;
	}

	public static class ThreadShadow
	{
		private static final long _pending_exception = vm_struct.entry.find("ThreadShadow", "_pending_exception").offset;
		private static final long _exception_file = vm_struct.entry.find("ThreadShadow", "_exception_file").offset;
		private static final long _exception_line = vm_struct.entry.find("ThreadShadow", "_exception_line").offset;
	}

	public static class Thread
	{
		private static final long _tlab = vm_struct.entry.find("Thread", "_tlab").offset;
		private static final long _allocated_bytes = vm_struct.entry.find("Thread", "_allocated_bytes").offset;
		private static final long _resource_area = vm_struct.entry.find("Thread", "_resource_area").offset;
	}

	public static class JavaThread
	{
		private static final long _lock_stack = vm_struct.entry.find("JavaThread", "_lock_stack").offset;
		private static final long _threadObj = vm_struct.entry.find("JavaThread", "_threadObj").offset;
		private static final long _vthread = vm_struct.entry.find("JavaThread", "_vthread").offset;
		private static final long _jvmti_vthread = vm_struct.entry.find("JavaThread", "_jvmti_vthread").offset;
		private static final long _scopedValueCache = vm_struct.entry.find("JavaThread", "_scopedValueCache").offset;
		private static final long _anchor = vm_struct.entry.find("JavaThread", "_anchor").offset;
		private static final long _vm_result = vm_struct.entry.find("JavaThread", "_vm_result").offset;
		private static final long _vm_result_2 = vm_struct.entry.find("JavaThread", "_vm_result_2").offset;
		private static final long _current_pending_monitor = vm_struct.entry.find("JavaThread", "_current_pending_monitor").offset;
		private static final long _current_pending_monitor_is_from_java = vm_struct.entry.find("JavaThread", "_current_pending_monitor_is_from_java").offset;
		private static final long _current_waiting_monitor = vm_struct.entry.find("JavaThread", "_current_waiting_monitor").offset;
		private static final long _suspend_flags = vm_struct.entry.find("JavaThread", "_suspend_flags").offset;
		private static final long _exception_oop = vm_struct.entry.find("JavaThread", "_exception_oop").offset;
		private static final long _exception_pc = vm_struct.entry.find("JavaThread", "_exception_pc").offset;
		private static final long _is_method_handle_return = vm_struct.entry.find("JavaThread", "_is_method_handle_return").offset;
		private static final long _saved_exception_pc = vm_struct.entry.find("JavaThread", "_saved_exception_pc").offset;
		private static final long _thread_state = vm_struct.entry.find("JavaThread", "_thread_state").offset;
		private static final long _osthread = vm_struct.entry.find("JavaThread", "_osthread").offset;
		private static final long _stack_base = vm_struct.entry.find("JavaThread", "_stack_base").offset;
		private static final long _stack_size = vm_struct.entry.find("JavaThread", "_stack_size").offset;
		private static final long _vframe_array_head = vm_struct.entry.find("JavaThread", "_vframe_array_head").offset;
		private static final long _vframe_array_last = vm_struct.entry.find("JavaThread", "_vframe_array_last").offset;
		private static final long _active_handles = vm_struct.entry.find("JavaThread", "_active_handles").offset;
		private static final long _terminated = vm_struct.entry.find("JavaThread", "_terminated").offset;
	}

	public static class LockStack
	{
		private static final long _top = vm_struct.entry.find("LockStack", "_top").offset;
		private static final long _base_0 = vm_struct.entry.find("LockStack", "_base[0]").offset;
	}

	public static class NamedThread
	{
		private static final long _name = vm_struct.entry.find("NamedThread", "_name").offset;
		private static final long _processed_thread = vm_struct.entry.find("NamedThread", "_processed_thread").offset;
	}

	public static class CompilerThread
	{
		private static final long _env = vm_struct.entry.find("CompilerThread", "_env").offset;
	}

	public static class OSThread
	{
		private static final long _state = vm_struct.entry.find("OSThread", "_state").offset;
		private static final long _thread_id = vm_struct.entry.find("OSThread", "_thread_id").offset;
		private static final long _thread_handle = vm_struct.entry.find("OSThread", "_thread_handle").offset;
	}

	public static class ImmutableOopMapSet
	{
		private static final long _count = vm_struct.entry.find("ImmutableOopMapSet", "_count").offset;
		private static final long _size = vm_struct.entry.find("ImmutableOopMapSet", "_size").offset;
	}

	public static class ImmutableOopMapPair
	{
		private static final long _pc_offset = vm_struct.entry.find("ImmutableOopMapPair", "_pc_offset").offset;
		private static final long _oopmap_offset = vm_struct.entry.find("ImmutableOopMapPair", "_oopmap_offset").offset;
	}

	public static class ImmutableOopMap
	{
		private static final long _count = vm_struct.entry.find("ImmutableOopMap", "_count").offset;
	}

	public static class JNIHandles
	{
		private static final long _global_handles = vm_struct.entry.find("JNIHandles", "_global_handles").address;
		private static final long _weak_global_handles = vm_struct.entry.find("JNIHandles", "_weak_global_handles").address;
	}

	public static class JNIHandleBlock
	{
		private static final long _handles = vm_struct.entry.find("JNIHandleBlock", "_handles").offset;
		private static final long _top = vm_struct.entry.find("JNIHandleBlock", "_top").offset;
		private static final long _next = vm_struct.entry.find("JNIHandleBlock", "_next").offset;
	}

	public static class CompressedStream
	{
		private static final long _buffer = vm_struct.entry.find("CompressedStream", "_buffer").offset;
		private static final long _position = vm_struct.entry.find("CompressedStream", "_position").offset;
	}

	public static class VMRegImpl
	{
		private static final long regName_0 = vm_struct.entry.find("VMRegImpl", "regName[0]").address;
		private static final long stack0 = vm_struct.entry.find("VMRegImpl", "stack0").address;
	}

	public static class Runtime1
	{
		private static final long _blobs = vm_struct.entry.find("Runtime1", "_blobs").address;
	}

	public static class ciEnv
	{
		private static final long _compiler_data = vm_struct.entry.find("ciEnv", "_compiler_data").offset;
		private static final long _failure_reason = vm_struct.entry.find("ciEnv", "_failure_reason").offset;
		private static final long _factory = vm_struct.entry.find("ciEnv", "_factory").offset;
		private static final long _dependencies = vm_struct.entry.find("ciEnv", "_dependencies").offset;
		private static final long _task = vm_struct.entry.find("ciEnv", "_task").offset;
		private static final long _arena = vm_struct.entry.find("ciEnv", "_arena").offset;
	}

	public static class ciBaseObject
	{
		private static final long _ident = vm_struct.entry.find("ciBaseObject", "_ident").offset;
	}

	public static class ciObject
	{
		private static final long _handle = vm_struct.entry.find("ciObject", "_handle").offset;
		private static final long _klass = vm_struct.entry.find("ciObject", "_klass").offset;
	}

	public static class ciMetadata
	{
		private static final long _metadata = vm_struct.entry.find("ciMetadata", "_metadata").offset;
	}

	public static class ciSymbol
	{
		private static final long _symbol = vm_struct.entry.find("ciSymbol", "_symbol").offset;
	}

	public static class ciType
	{
		private static final long _basic_type = vm_struct.entry.find("ciType", "_basic_type").offset;
	}

	public static class ciKlass
	{
		private static final long _name = vm_struct.entry.find("ciKlass", "_name").offset;
	}

	public static class ciArrayKlass
	{
		private static final long _dimension = vm_struct.entry.find("ciArrayKlass", "_dimension").offset;
	}

	public static class ciObjArrayKlass
	{
		private static final long _element_klass = vm_struct.entry.find("ciObjArrayKlass", "_element_klass").offset;
		private static final long _base_element_klass = vm_struct.entry.find("ciObjArrayKlass", "_base_element_klass").offset;
	}

	public static class ciInstanceKlass
	{
		private static final long _init_state = vm_struct.entry.find("ciInstanceKlass", "_init_state").offset;
		private static final long _is_shared = vm_struct.entry.find("ciInstanceKlass", "_is_shared").offset;
	}

	public static class ciMethod
	{
		private static final long _interpreter_invocation_count = vm_struct.entry.find("ciMethod", "_interpreter_invocation_count").offset;
		private static final long _interpreter_throwout_count = vm_struct.entry.find("ciMethod", "_interpreter_throwout_count").offset;
		private static final long _inline_instructions_size = vm_struct.entry.find("ciMethod", "_inline_instructions_size").offset;
	}

	public static class ciMethodData
	{
		private static final long _data_size = vm_struct.entry.find("ciMethodData", "_data_size").offset;
		private static final long _state = vm_struct.entry.find("ciMethodData", "_state").offset;
		private static final long _extra_data_size = vm_struct.entry.find("ciMethodData", "_extra_data_size").offset;
		private static final long _data = vm_struct.entry.find("ciMethodData", "_data").offset;
		private static final long _hint_di = vm_struct.entry.find("ciMethodData", "_hint_di").offset;
		private static final long _eflags = vm_struct.entry.find("ciMethodData", "_eflags").offset;
		private static final long _arg_local = vm_struct.entry.find("ciMethodData", "_arg_local").offset;
		private static final long _arg_stack = vm_struct.entry.find("ciMethodData", "_arg_stack").offset;
		private static final long _arg_returned = vm_struct.entry.find("ciMethodData", "_arg_returned").offset;
		private static final long _orig = vm_struct.entry.find("ciMethodData", "_orig").offset;
	}

	public static class ciField
	{
		private static final long _holder = vm_struct.entry.find("ciField", "_holder").offset;
		private static final long _name = vm_struct.entry.find("ciField", "_name").offset;
		private static final long _signature = vm_struct.entry.find("ciField", "_signature").offset;
		private static final long _offset = vm_struct.entry.find("ciField", "_offset").offset;
		private static final long _is_constant = vm_struct.entry.find("ciField", "_is_constant").offset;
		private static final long _constant_value = vm_struct.entry.find("ciField", "_constant_value").offset;
	}

	public static class ciObjectFactory
	{
		private static final long _ci_metadata = vm_struct.entry.find("ciObjectFactory", "_ci_metadata").offset;
		private static final long _symbols = vm_struct.entry.find("ciObjectFactory", "_symbols").offset;
	}

	public static class ciConstant
	{
		private static final long _type = vm_struct.entry.find("ciConstant", "_type").offset;
		private static final long _value_int = vm_struct.entry.find("ciConstant", "_value._int").offset;
		private static final long _value_long = vm_struct.entry.find("ciConstant", "_value._long").offset;
		private static final long _value_float = vm_struct.entry.find("ciConstant", "_value._float").offset;
		private static final long _value_double = vm_struct.entry.find("ciConstant", "_value._double").offset;
		private static final long _value_object = vm_struct.entry.find("ciConstant", "_value._object").offset;
	}

	public static class ObjectMonitor
	{
		private static final long _header = vm_struct.entry.find("ObjectMonitor", "_header").offset;
		private static final long _object = vm_struct.entry.find("ObjectMonitor", "_object").offset;
		private static final long _owner = vm_struct.entry.find("ObjectMonitor", "_owner").offset;
		private static final long _next_om = vm_struct.entry.find("ObjectMonitor", "_next_om").offset;
		private static final long _contentions = vm_struct.entry.find("ObjectMonitor", "_contentions").offset;
		private static final long _waiters = vm_struct.entry.find("ObjectMonitor", "_waiters").offset;
		private static final long _recursions = vm_struct.entry.find("ObjectMonitor", "_recursions").offset;
	}

	public static class BasicLock
	{
		private static final long _displaced_header = vm_struct.entry.find("BasicLock", "_displaced_header").offset;
	}

	public static class BasicObjectLock
	{
		private static final long _lock = vm_struct.entry.find("BasicObjectLock", "_lock").offset;
		private static final long _obj = vm_struct.entry.find("BasicObjectLock", "_obj").offset;
	}

	public static class ObjectSynchronizer
	{
		private static final long _in_use_list = vm_struct.entry.find("ObjectSynchronizer", "_in_use_list").address;
	}

	public static class MonitorList
	{
		private static final long _head = vm_struct.entry.find("MonitorList", "_head").offset;
	}

	public static class Matcher
	{
		private static final long _regEncode = vm_struct.entry.find("Matcher", "_regEncode").address;
	}

	public static class Node
	{
		private static final long _in = vm_struct.entry.find("Node", "_in").offset;
		private static final long _out = vm_struct.entry.find("Node", "_out").offset;
		private static final long _cnt = vm_struct.entry.find("Node", "_cnt").offset;
		private static final long _max = vm_struct.entry.find("Node", "_max").offset;
		private static final long _outcnt = vm_struct.entry.find("Node", "_outcnt").offset;
		private static final long _outmax = vm_struct.entry.find("Node", "_outmax").offset;
		private static final long _idx = vm_struct.entry.find("Node", "_idx").offset;
		private static final long _class_id = vm_struct.entry.find("Node", "_class_id").offset;
		private static final long _flags = vm_struct.entry.find("Node", "_flags").offset;
	}

	public static class Compile
	{
		private static final long _root = vm_struct.entry.find("Compile", "_root").offset;
		private static final long _unique = vm_struct.entry.find("Compile", "_unique").offset;
		private static final long _entry_bci = vm_struct.entry.find("Compile", "_entry_bci").offset;
		private static final long _top = vm_struct.entry.find("Compile", "_top").offset;
		private static final long _cfg = vm_struct.entry.find("Compile", "_cfg").offset;
		private static final long _regalloc = vm_struct.entry.find("Compile", "_regalloc").offset;
		private static final long _method = vm_struct.entry.find("Compile", "_method").offset;
		private static final long _compile_id = vm_struct.entry.find("Compile", "_compile_id").offset;
		private static final long _options = vm_struct.entry.find("Compile", "_options").offset;
		private static final long _ilt = vm_struct.entry.find("Compile", "_ilt").offset;
	}

	public static class Options
	{
		private static final long _subsume_loads = vm_struct.entry.find("Options", "_subsume_loads").offset;
		private static final long _do_escape_analysis = vm_struct.entry.find("Options", "_do_escape_analysis").offset;
		private static final long _eliminate_boxing = vm_struct.entry.find("Options", "_eliminate_boxing").offset;
		private static final long _do_locks_coarsening = vm_struct.entry.find("Options", "_do_locks_coarsening").offset;
		private static final long _install_code = vm_struct.entry.find("Options", "_install_code").offset;
	}

	public static class InlineTree
	{
		private static final long _caller_jvms = vm_struct.entry.find("InlineTree", "_caller_jvms").offset;
		private static final long _method = vm_struct.entry.find("InlineTree", "_method").offset;
		private static final long _caller_tree = vm_struct.entry.find("InlineTree", "_caller_tree").offset;
		private static final long _subtrees = vm_struct.entry.find("InlineTree", "_subtrees").offset;
	}

	public static class OptoRegPair
	{
		private static final long _first = vm_struct.entry.find("OptoRegPair", "_first").offset;
		private static final long _second = vm_struct.entry.find("OptoRegPair", "_second").offset;
	}

	public static class JVMState
	{
		private static final long _caller = vm_struct.entry.find("JVMState", "_caller").offset;
		private static final long _depth = vm_struct.entry.find("JVMState", "_depth").offset;
		private static final long _locoff = vm_struct.entry.find("JVMState", "_locoff").offset;
		private static final long _stkoff = vm_struct.entry.find("JVMState", "_stkoff").offset;
		private static final long _monoff = vm_struct.entry.find("JVMState", "_monoff").offset;
		private static final long _scloff = vm_struct.entry.find("JVMState", "_scloff").offset;
		private static final long _endoff = vm_struct.entry.find("JVMState", "_endoff").offset;
		private static final long _sp = vm_struct.entry.find("JVMState", "_sp").offset;
		private static final long _bci = vm_struct.entry.find("JVMState", "_bci").offset;
		private static final long _method = vm_struct.entry.find("JVMState", "_method").offset;
		private static final long _map = vm_struct.entry.find("JVMState", "_map").offset;
	}

	public static class SafePointNode
	{
		private static final long _jvms = vm_struct.entry.find("SafePointNode", "_jvms").offset;
	}

	public static class MachSafePointNode
	{
		private static final long _jvms = vm_struct.entry.find("MachSafePointNode", "_jvms").offset;
		private static final long _jvmadj = vm_struct.entry.find("MachSafePointNode", "_jvmadj").offset;
	}

	public static class MachIfNode
	{
		private static final long _prob = vm_struct.entry.find("MachIfNode", "_prob").offset;
		private static final long _fcnt = vm_struct.entry.find("MachIfNode", "_fcnt").offset;
	}

	public static class MachJumpNode
	{
		private static final long _probs = vm_struct.entry.find("MachJumpNode", "_probs").offset;
	}

	public static class CallNode
	{
		private static final long _entry_point = vm_struct.entry.find("CallNode", "_entry_point").offset;
	}

	public static class CallJavaNode
	{
		private static final long _method = vm_struct.entry.find("CallJavaNode", "_method").offset;
	}

	public static class CallRuntimeNode
	{
		private static final long _name = vm_struct.entry.find("CallRuntimeNode", "_name").offset;
	}

	public static class CallStaticJavaNode
	{
		private static final long _name = vm_struct.entry.find("CallStaticJavaNode", "_name").offset;
	}

	public static class MachCallJavaNode
	{
		private static final long _method = vm_struct.entry.find("MachCallJavaNode", "_method").offset;
	}

	public static class MachCallStaticJavaNode
	{
		private static final long _name = vm_struct.entry.find("MachCallStaticJavaNode", "_name").offset;
	}

	public static class MachCallRuntimeNode
	{
		private static final long _name = vm_struct.entry.find("MachCallRuntimeNode", "_name").offset;
	}

	public static class PhaseCFG
	{
		private static final long _number_of_blocks = vm_struct.entry.find("PhaseCFG", "_number_of_blocks").offset;
		private static final long _blocks = vm_struct.entry.find("PhaseCFG", "_blocks").offset;
		private static final long _node_to_block_mapping = vm_struct.entry.find("PhaseCFG", "_node_to_block_mapping").offset;
		private static final long _root_block = vm_struct.entry.find("PhaseCFG", "_root_block").offset;
	}

	public static class PhaseRegAlloc
	{
		private static final long _node_regs = vm_struct.entry.find("PhaseRegAlloc", "_node_regs").offset;
		private static final long _node_regs_max_index = vm_struct.entry.find("PhaseRegAlloc", "_node_regs_max_index").offset;
		private static final long _framesize = vm_struct.entry.find("PhaseRegAlloc", "_framesize").offset;
		private static final long _max_reg = vm_struct.entry.find("PhaseRegAlloc", "_max_reg").offset;
	}

	public static class PhaseChaitin
	{
		private static final long _trip_cnt = vm_struct.entry.find("PhaseChaitin", "_trip_cnt").offset;
		private static final long _alternate = vm_struct.entry.find("PhaseChaitin", "_alternate").offset;
		private static final long _lo_degree = vm_struct.entry.find("PhaseChaitin", "_lo_degree").offset;
		private static final long _lo_stk_degree = vm_struct.entry.find("PhaseChaitin", "_lo_stk_degree").offset;
		private static final long _hi_degree = vm_struct.entry.find("PhaseChaitin", "_hi_degree").offset;
		private static final long _simplified = vm_struct.entry.find("PhaseChaitin", "_simplified").offset;
	}

	public static class Block
	{
		private static final long _nodes = vm_struct.entry.find("Block", "_nodes").offset;
		private static final long _succs = vm_struct.entry.find("Block", "_succs").offset;
		private static final long _num_succs = vm_struct.entry.find("Block", "_num_succs").offset;
		private static final long _pre_order = vm_struct.entry.find("Block", "_pre_order").offset;
		private static final long _dom_depth = vm_struct.entry.find("Block", "_dom_depth").offset;
		private static final long _idom = vm_struct.entry.find("Block", "_idom").offset;
		private static final long _freq = vm_struct.entry.find("Block", "_freq").offset;
	}

	public static class CFGElement
	{
		private static final long _freq = vm_struct.entry.find("CFGElement", "_freq").offset;
	}

	public static class Block_List
	{
		private static final long _cnt = vm_struct.entry.find("Block_List", "_cnt").offset;
	}

	public static class Block_Array
	{
		private static final long _size = vm_struct.entry.find("Block_Array", "_size").offset;
		private static final long _blocks = vm_struct.entry.find("Block_Array", "_blocks").offset;
		private static final long _arena = vm_struct.entry.find("Block_Array", "_arena").offset;
	}

	public static class Node_List
	{
		private static final long _cnt = vm_struct.entry.find("Node_List", "_cnt").offset;
	}

	public static class Node_Array
	{
		private static final long _max = vm_struct.entry.find("Node_Array", "_max").offset;
		private static final long _nodes = vm_struct.entry.find("Node_Array", "_nodes").offset;
		private static final long _a = vm_struct.entry.find("Node_Array", "_a").offset;
	}

	public static class JVMFlag
	{
		private static final long _type = vm_struct.entry.find("JVMFlag", "_type").offset;
		private static final long _name = vm_struct.entry.find("JVMFlag", "_name").offset;
		private static final long _addr = vm_struct.entry.find("JVMFlag", "_addr").offset;
		private static final long _flags = vm_struct.entry.find("JVMFlag", "_flags").offset;
		private static final long flags = vm_struct.entry.find("JVMFlag", "flags").address;
		private static final long numFlags = vm_struct.entry.find("JVMFlag", "numFlags").address;
	}

	public static class Abstract_VM_Version
	{
		private static final long _s_vm_release = vm_struct.entry.find("Abstract_VM_Version", "_s_vm_release").address;
		private static final long _s_internal_vm_info_string = vm_struct.entry.find("Abstract_VM_Version", "_s_internal_vm_info_string").address;
		private static final long _features = vm_struct.entry.find("Abstract_VM_Version", "_features").address;
		private static final long _features_string = vm_struct.entry.find("Abstract_VM_Version", "_features_string").address;
		private static final long _vm_major_version = vm_struct.entry.find("Abstract_VM_Version", "_vm_major_version").address;
		private static final long _vm_minor_version = vm_struct.entry.find("Abstract_VM_Version", "_vm_minor_version").address;
		private static final long _vm_security_version = vm_struct.entry.find("Abstract_VM_Version", "_vm_security_version").address;
		private static final long _vm_build_number = vm_struct.entry.find("Abstract_VM_Version", "_vm_build_number").address;
	}

	public static class JDK_Version
	{
		private static final long _current = vm_struct.entry.find("JDK_Version", "_current").address;
		private static final long _major = vm_struct.entry.find("JDK_Version", "_major").offset;
	}

	public static class JvmtiExport
	{
		private static final long _can_access_local_variables = vm_struct.entry.find("JvmtiExport", "_can_access_local_variables").address;
		private static final long _can_hotswap_or_post_breakpoint = vm_struct.entry.find("JvmtiExport", "_can_hotswap_or_post_breakpoint").address;
		private static final long _can_post_on_exceptions = vm_struct.entry.find("JvmtiExport", "_can_post_on_exceptions").address;
		private static final long _can_walk_any_space = vm_struct.entry.find("JvmtiExport", "_can_walk_any_space").address;
	}

	public static class Arguments
	{
		private static final long _jvm_flags_array = vm_struct.entry.find("Arguments", "_jvm_flags_array").address;
		private static final long _num_jvm_flags = vm_struct.entry.find("Arguments", "_num_jvm_flags").address;
		private static final long _jvm_args_array = vm_struct.entry.find("Arguments", "_jvm_args_array").address;
		private static final long _num_jvm_args = vm_struct.entry.find("Arguments", "_num_jvm_args").address;
		private static final long _java_command = vm_struct.entry.find("Arguments", "_java_command").address;
	}

	public static class Array extends vm_struct
	{
		long length_offset;

		protected Array(String name, long length_offset, long data_offset, long address)
		{
			super(name, address, data_offset);
			this.length_offset = length_offset;
		}

		public int length()
		{
			return super.read_int(length_offset);
		}
	}

	public static class Array_int extends Array
	{
		private static final long _length = vm_struct.entry.find("Array<int>", "_length").offset;
		private static final long _data = vm_struct.entry.find("Array<int>", "_data").offset;

		public Array_int(long address)
		{
			super("Array<int>", _length, _data, address);
		}

		public int get(int idx)
		{
			return super.read_int_idx(idx);
		}

		public void set(int idx, int value)
		{
			super.write_idx(idx, value);
		}
	}

	public static class Array_u1 extends Array
	{
		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find("Array<u1>", "_data").offset;

		public Array_u1(long address)
		{
			super("Array<u1>", _length, _data, address);
		}

		public byte get(int idx)
		{
			return super.read_byte_idx(idx);
		}

		public void set(int idx, byte value)
		{
			super.write_idx(idx, value);
		}
	}

	public static class Array_u2 extends Array
	{
		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find("Array<u2>", "_data").offset;

		public Array_u2(long address)
		{
			super("Array<u2>", _length, _data, address);
		}

		public short get(int idx)
		{
			return super.read_short_idx(idx);
		}

		public void set(int idx, short value)
		{
			super.write_idx(idx, value);
		}
	}

	public static class Array_pMethod extends Array
	{
		private static final long _length = Array_int._length;
		private static final long _data = vm_struct.entry.find("Array<Method*>", "_data").offset;

		public Array_pMethod(long address)
		{
			super("Array<Method*>", _length, _data, address);
		}

		public Method get(int idx)
		{
			return super.read_memory_operator_ptr_idx(Method.class, idx);
		}

		public void set(int idx, Method value)
		{
			super.write_idx(idx, value);
		}
	}

	public static class java_lang_Class
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

	public static class FileMapInfo
	{
		private static final long _header = vm_struct.entry.find("FileMapInfo", "_header").offset;
		private static final long _current_info = vm_struct.entry.find("FileMapInfo", "_current_info").address;
	}

	public static class FileMapHeader
	{
		private static final long _regions_0 = vm_struct.entry.find("FileMapHeader", "_regions[0]").offset;
		private static final long _cloned_vtables_offset = vm_struct.entry.find("FileMapHeader", "_cloned_vtables_offset").offset;
		private static final long _mapped_base_address = vm_struct.entry.find("FileMapHeader", "_mapped_base_address").offset;
	}

	public static class CDSFileMapRegion
	{
		private static final long _mapped_base = vm_struct.entry.find("CDSFileMapRegion", "_mapped_base").offset;
		private static final long _used = vm_struct.entry.find("CDSFileMapRegion", "_used").offset;
	}

	public static class VMError
	{
		private static final long _thread = vm_struct.entry.find("VMError", "_thread").address;
	}

	public static class CompileTask
	{
		private static final long _method = vm_struct.entry.find("CompileTask", "_method").offset;
		private static final long _osr_bci = vm_struct.entry.find("CompileTask", "_osr_bci").offset;
		private static final long _comp_level = vm_struct.entry.find("CompileTask", "_comp_level").offset;
		private static final long _compile_id = vm_struct.entry.find("CompileTask", "_compile_id").offset;
		private static final long _num_inlined_bytecodes = vm_struct.entry.find("CompileTask", "_num_inlined_bytecodes").offset;
		private static final long _next = vm_struct.entry.find("CompileTask", "_next").offset;
		private static final long _prev = vm_struct.entry.find("CompileTask", "_prev").offset;
	}

	public static class vframeArray
	{
		private static final long _original = vm_struct.entry.find("vframeArray", "_original").offset;
		private static final long _caller = vm_struct.entry.find("vframeArray", "_caller").offset;
		private static final long _frames = vm_struct.entry.find("vframeArray", "_frames").offset;
	}

	public static class vframeArrayElement
	{
		private static final long _frame = vm_struct.entry.find("vframeArrayElement", "_frame").offset;
		private static final long _bci = vm_struct.entry.find("vframeArrayElement", "_bci").offset;
		private static final long _method = vm_struct.entry.find("vframeArrayElement", "_method").offset;
	}

	public static class AccessFlags extends vm_struct
	{
		private static final long _flags = vm_struct.entry.find("AccessFlags", "_flags").offset;

		// 类、字段、方法通用标记

		/**
		 * 是否是public修饰的
		 */
		public static final short JVM_ACC_PUBLIC = 0x0001;

		/**
		 * 是否是private修饰的
		 */
		public static final short JVM_ACC_PRIVATE = 0x0002;

		/**
		 * 是否是protected修饰的
		 */
		public static final short JVM_ACC_PROTECTED = 0x0004;

		/**
		 * 是否是final修饰的
		 */
		public static final short JVM_ACC_FINAL = 0x0010;

		/**
		 * 是否是编译器自动生成的
		 */
		public static final short JVM_ACC_SYNTHETIC = 0x1000;

		// 类、字段通用标记

		/**
		 * 是否是枚举
		 */
		public static final short JVM_ACC_ENUM = 0x4000;

		// 类、方法通用标记

		/**
		 * 是否是abstract修饰的
		 */
		public static final short JVM_ACC_ABSTRACT = 0x0400;

		// 字段、方法通用标记

		/**
		 * 是否是static修饰的
		 */
		public static final short JVM_ACC_STATIC = 0x0008;

		// 类专用标记

		/**
		 * 类专用，invokespecial使用
		 */
		public static final short JVM_ACC_SUPER = 0x0020;

		/**
		 * 类专用，是否是接口
		 */
		public static final short JVM_ACC_INTERFACE = 0x0200;

		/**
		 * 类专用，是否是注解
		 */
		public static final short JVM_ACC_ANNOTATION = 0x2000;

		// 字段专用标记

		/**
		 * 字段专用，是否是transient修饰的，序列化与反序列化使用
		 */
		public static final short JVM_ACC_TRANSIENT = 0x0080;

		/**
		 * 字段专用，是否是volatile修饰的
		 */
		public static final short JVM_ACC_VOLATILE = 0x0040;

		// 方法专用标记

		/**
		 * 方法专用，是否加锁
		 */
		public static final short JVM_ACC_SYNCHRONIZED = 0x0020;

		/**
		 * 方法专用，是否是泛型编译生成的桥接方法
		 */
		public static final short JVM_ACC_BRIDGE = 0x0040;

		/**
		 * 方法专用，是否是变长参数
		 */
		public static final short JVM_ACC_VARARGS = 0x0080;

		/**
		 * 方法专用，是否是native修饰的
		 */
		public static final short JVM_ACC_NATIVE = 0x0100;

		/**
		 * 方法专用，strictfp即严格浮点运算
		 */
		public static final short JVM_ACC_STRICT = 0x0800;

		/**
		 * JVM识别类的标记掩码
		 */
		public static final short JVM_RECOGNIZED_CLASS_MODIFIERS = (JVM_ACC_PUBLIC |
				JVM_ACC_FINAL |
				JVM_ACC_SUPER |
				JVM_ACC_INTERFACE |
				JVM_ACC_ABSTRACT |
				JVM_ACC_ANNOTATION |
				JVM_ACC_ENUM |
				JVM_ACC_SYNTHETIC);

		/**
		 * JVM识别字段的标记掩码
		 */
		public static final short JVM_RECOGNIZED_FIELD_MODIFIERS = (JVM_ACC_PUBLIC |
				JVM_ACC_PRIVATE |
				JVM_ACC_PROTECTED |
				JVM_ACC_STATIC |
				JVM_ACC_FINAL |
				JVM_ACC_VOLATILE |
				JVM_ACC_TRANSIENT |
				JVM_ACC_ENUM |
				JVM_ACC_SYNTHETIC);

		/**
		 * JVM识别方法的标记掩码
		 */
		public static final short JVM_RECOGNIZED_METHOD_MODIFIERS = (JVM_ACC_PUBLIC |
				JVM_ACC_PRIVATE |
				JVM_ACC_PROTECTED |
				JVM_ACC_STATIC |
				JVM_ACC_FINAL |
				JVM_ACC_SYNCHRONIZED |
				JVM_ACC_BRIDGE |
				JVM_ACC_VARARGS |
				JVM_ACC_NATIVE |
				JVM_ACC_ABSTRACT |
				JVM_ACC_STRICT |
				JVM_ACC_SYNTHETIC);

		public AccessFlags(long address)
		{
			super("AccessFlags", address);
		}

		public String toString()
		{
			return memory.bits_str(_flags());
		}

		public short _flags()
		{
			return super.read_short(_flags);
		}

		public void set_flags(short flags)
		{
			super.write(_flags, flags);
		}

		public boolean is_public()
		{
			return memory.flag_bit(_flags(), JVM_ACC_PUBLIC);
		}

		public boolean is_private()
		{
			return memory.flag_bit(_flags(), JVM_ACC_PRIVATE);
		}

		public boolean is_protected()
		{
			return memory.flag_bit(_flags(), JVM_ACC_PROTECTED);
		}

		public boolean is_static()
		{
			return memory.flag_bit(_flags(), JVM_ACC_STATIC);
		}

		public boolean is_final()
		{
			return memory.flag_bit(_flags(), JVM_ACC_FINAL);
		}

		public boolean is_synchronized()
		{
			return memory.flag_bit(_flags(), JVM_ACC_SYNCHRONIZED);
		}

		public boolean is_super()
		{
			return memory.flag_bit(_flags(), JVM_ACC_SUPER);
		}

		public boolean is_volatile()
		{
			return memory.flag_bit(_flags(), JVM_ACC_VOLATILE);
		}

		public boolean is_transient()
		{
			return memory.flag_bit(_flags(), JVM_ACC_TRANSIENT);
		}

		public boolean is_native()
		{
			return memory.flag_bit(_flags(), JVM_ACC_NATIVE);
		}

		public boolean is_interface()
		{
			return memory.flag_bit(_flags(), JVM_ACC_INTERFACE);
		}

		public boolean is_abstract()
		{
			return memory.flag_bit(_flags(), JVM_ACC_ABSTRACT);
		}

		public boolean is_synthetic()
		{
			return memory.flag_bit(_flags(), JVM_ACC_SYNTHETIC);
		}

		public void set_public(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_PUBLIC, value));
		}

		public void set_private(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_PRIVATE, value));
		}

		public void set_protected(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_PROTECTED, value));
		}

		public void set_static(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_STATIC, value));
		}

		public void set_final(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_FINAL, value));
		}

		public void set_synchronized(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_SYNCHRONIZED, value));
		}

		public void set_super(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_SUPER, value));
		}

		public void set_volatile(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_VOLATILE, value));
		}

		public void set_bridge(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_BRIDGE, value));
		}

		public void set_transient(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_TRANSIENT, value));
		}

		public void set_varargs(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_VARARGS, value));
		}

		public void set_native(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_NATIVE, value));
		}

		public void set_interface(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_INTERFACE, value));
		}

		public void set_abstract(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_ABSTRACT, value));
		}

		public void set_strict(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_STRICT, value));
		}

		public void set_synthetic(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_SYNTHETIC, value));
		}

		public void set_annotation(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_ANNOTATION, value));
		}

		public void set_enum(boolean value)
		{
			set_flags(memory.set_flag_bit(_flags(), JVM_ACC_ENUM, value));
		}
	}

	public static class elapsedTimer
	{
		private static final long _counter = vm_struct.entry.find("elapsedTimer", "_counter").offset;
		private static final long _active = vm_struct.entry.find("elapsedTimer", "_active").offset;
	}

	public static class InvocationCounter
	{
		private static final long _counter = vm_struct.entry.find("InvocationCounter", "_counter").offset;
	}
}
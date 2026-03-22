package jvmsp.hotspot;

import java.util.HashMap;
import java.util.Map;

import jvmsp.memory;
import jvmsp.shared_object;
import jvmsp.unsafe;
import jvmsp.libso.libjvm;

/**
 * hotspot虚拟机内部实现结构体访问
 */
@SuppressWarnings("unused")
public class vm_struct
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
			this.type_name = memory.string(unsafe.read_pointer(struct_addr + gHotSpotVMStructEntryTypeNameOffset));
			this.field_name = memory.string(unsafe.read_pointer(struct_addr + gHotSpotVMStructEntryFieldNameOffset));
			this.type_string = memory.string(unsafe.read_pointer(struct_addr + gHotSpotVMStructEntryTypeStringOffset));
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

		public static final entry get(String type_name, String field_name)
		{
			Map<String, entry> fields = vm_struct_entries.get(type_name);
			if (fields == null)
				return null;
			else
				return fields.get(field_name);
		}
	}

	public static class ContiguousSpace
	{
		private static final long _end_of_live = vm_struct.entry.get("ContiguousSpace", "_end_of_live").offset;
		private static final long _top = vm_struct.entry.get("ContiguousSpace", "_top").offset;

		private static final long _saved_mark_word = vm_struct.entry.get("ContiguousSpace", "_saved_mark_word").offset;
	}

	public static class Generation
	{
		private static final long _reserved = vm_struct.entry.get("Generation", "_reserved").offset;
		private static final long _virtual_space = vm_struct.entry.get("Generation", "_virtual_space").offset;
		private static final long _stat_record = vm_struct.entry.get("Generation", "_stat_record").offset;
	}

	public static class Generation_StatRecord
	{
		private static final long invocations = vm_struct.entry.get("Generation::StatRecord", "invocations").offset;
		private static final long accumulated_time = vm_struct.entry.get("Generation::StatRecord", "accumulated_time").offset;
	}

	public static class GenerationSpec
	{
		private static final long _name = vm_struct.entry.get("GenerationSpec", "_name").offset;
		private static final long _init_size = vm_struct.entry.get("GenerationSpec", "_init_size").offset;
		private static final long _max_size = vm_struct.entry.get("GenerationSpec", "_max_size").offset;
	}

	public static class GenCollectedHeap
	{
		private static final long _young_gen = vm_struct.entry.get("GenCollectedHeap", "_young_gen").offset;
		private static final long _old_gen = vm_struct.entry.get("GenCollectedHeap", "_old_gen").offset;
		private static final long _young_gen_spec = vm_struct.entry.get("GenCollectedHeap", "_young_gen_spec").offset;
		private static final long _old_gen_spec = vm_struct.entry.get("GenCollectedHeap", "_old_gen_spec").offset;
	}

	public static class MemRegion
	{
		private static final long _start = vm_struct.entry.get("MemRegion", "_start").offset;
		private static final long _word_size = vm_struct.entry.get("MemRegion", "_word_size").offset;
	}

	public static class Space
	{
		private static final long _bottom = vm_struct.entry.get("Space", "_bottom").offset;
		private static final long _end = vm_struct.entry.get("Space", "_end").offset;
	}

	public static class oopDesc
	{
		private static final long _mark = vm_struct.entry.get("oopDesc", "_mark").offset;
		private static final long _metadata__klass = vm_struct.entry.get("oopDesc", "_metadata._klass").offset;
		private static final long _metadata__compressed_klass = vm_struct.entry.get("oopDesc", "_metadata._compressed_klass").offset;
	}

	public static class BarrierSet
	{
		private static final long _barrier_set = vm_struct.entry.get("BarrierSet", "_barrier_set").address;
	}

	public static class ArrayKlass
	{
		private static final long _dimension = vm_struct.entry.get("ArrayKlass", "_dimension").offset;
		private static final long _higher_dimension = vm_struct.entry.get("ArrayKlass", "_higher_dimension").offset;
		private static final long _lower_dimension = vm_struct.entry.get("ArrayKlass", "_lower_dimension").offset;
	}

	public static class CompiledICHolder
	{
		private static final long _holder_metadata = vm_struct.entry.get("CompiledICHolder", "_holder_metadata").offset;
		private static final long _holder_klass = vm_struct.entry.get("CompiledICHolder", "_holder_klass").offset;
	}

	public static class ConstantPool
	{
		private static final long _tags = vm_struct.entry.get("ConstantPool", "_tags").offset;
		private static final long _cache = vm_struct.entry.get("ConstantPool", "_cache").offset;
		private static final long _pool_holder = vm_struct.entry.get("ConstantPool", "_pool_holder").offset;
		private static final long _operands = vm_struct.entry.get("ConstantPool", "_operands").offset;
		private static final long _resolved_klasses = vm_struct.entry.get("ConstantPool", "_resolved_klasses").offset;
		private static final long _length = vm_struct.entry.get("ConstantPool", "_length").offset;
		private static final long _minor_version = vm_struct.entry.get("ConstantPool", "_minor_version").offset;
		private static final long _major_version = vm_struct.entry.get("ConstantPool", "_major_version").offset;
		private static final long _generic_signature_index = vm_struct.entry.get("ConstantPool", "_generic_signature_index").offset;
		private static final long _source_file_name_index = vm_struct.entry.get("ConstantPool", "_source_file_name_index").offset;
	}

	public static class ConstantPoolCache
	{
		private static final long _resolved_references = vm_struct.entry.get("ConstantPoolCache", "_resolved_references").offset;
		private static final long _reference_map = vm_struct.entry.get("ConstantPoolCache", "_reference_map").offset;
		private static final long _length = vm_struct.entry.get("ConstantPoolCache", "_length").offset;
		private static final long _constant_pool = vm_struct.entry.get("ConstantPoolCache", "_constant_pool").offset;
		private static final long _resolved_indy_entries = vm_struct.entry.get("ConstantPoolCache", "_resolved_indy_entries").offset;
	}

	public static class ResolvedIndyEntry
	{
		private static final long _cpool_index = vm_struct.entry.get("ResolvedIndyEntry", "_cpool_index").offset;
	}

	public static class InstanceKlass
	{
		private static final long _array_klasses = vm_struct.entry.get("InstanceKlass", "_array_klasses").offset;
		private static final long _methods = vm_struct.entry.get("InstanceKlass", "_methods").offset;
		private static final long _default_methods = vm_struct.entry.get("InstanceKlass", "_default_methods").offset;
		private static final long _local_interfaces = vm_struct.entry.get("InstanceKlass", "_local_interfaces").offset;
		private static final long _transitive_interfaces = vm_struct.entry.get("InstanceKlass", "_transitive_interfaces").offset;
		private static final long _fieldinfo_stream = vm_struct.entry.get("InstanceKlass", "_fieldinfo_stream").offset;
		private static final long _constants = vm_struct.entry.get("InstanceKlass", "_constants").offset;
		private static final long _source_debug_extension = vm_struct.entry.get("InstanceKlass", "_source_debug_extension").offset;
		private static final long _inner_classes = vm_struct.entry.get("InstanceKlass", "_inner_classes").offset;
		private static final long _nonstatic_field_size = vm_struct.entry.get("InstanceKlass", "_nonstatic_field_size").offset;
		private static final long _static_field_size = vm_struct.entry.get("InstanceKlass", "_static_field_size").offset;
		private static final long _static_oop_field_count = vm_struct.entry.get("InstanceKlass", "_static_oop_field_count").offset;
		private static final long _nonstatic_oop_map_size = vm_struct.entry.get("InstanceKlass", "_nonstatic_oop_map_size").offset;
		private static final long _init_state = vm_struct.entry.get("InstanceKlass", "_init_state").offset;
		private static final long _init_thread = vm_struct.entry.get("InstanceKlass", "_init_thread").offset;
		private static final long _itable_len = vm_struct.entry.get("InstanceKlass", "_itable_len").offset;
		private static final long _reference_type = vm_struct.entry.get("InstanceKlass", "_reference_type").offset;
		private static final long _oop_map_cache = vm_struct.entry.get("InstanceKlass", "_oop_map_cache").offset;
		private static final long _jni_ids = vm_struct.entry.get("InstanceKlass", "_jni_ids").offset;
		private static final long _osr_nmethods_head = vm_struct.entry.get("InstanceKlass", "_osr_nmethods_head").offset;
		private static final long _breakpoints = vm_struct.entry.get("InstanceKlass", "_breakpoints").offset;
		private static final long _methods_jmethod_ids = vm_struct.entry.get("InstanceKlass", "_methods_jmethod_ids").offset;
		private static final long _idnum_allocated_count = vm_struct.entry.get("InstanceKlass", "_idnum_allocated_count").offset;
		private static final long _annotations = vm_struct.entry.get("InstanceKlass", "_annotations").offset;
		private static final long _method_ordering = vm_struct.entry.get("InstanceKlass", "_method_ordering").offset;
		private static final long _default_vtable_indices = vm_struct.entry.get("InstanceKlass", "_default_vtable_indices").offset;
	}

	public static class Klass
	{
		private static final long _super_check_offset = vm_struct.entry.get("Klass", "_super_check_offset").offset;
		private static final long _secondary_super_cache = vm_struct.entry.get("Klass", "_secondary_super_cache").offset;
		private static final long _secondary_supers = vm_struct.entry.get("Klass", "_secondary_supers").offset;
		private static final long _primary_supers_0 = vm_struct.entry.get("Klass", "_primary_supers[0]").offset;
		private static final long _java_mirror = vm_struct.entry.get("Klass", "_java_mirror").offset;
		private static final long _modifier_flags = vm_struct.entry.get("Klass", "_modifier_flags").offset;
		private static final long _super = vm_struct.entry.get("Klass", "_super").offset;
		private static final long _subklass = vm_struct.entry.get("Klass", "_subklass").offset;
		private static final long _layout_helper = vm_struct.entry.get("Klass", "_layout_helper").offset;
		private static final long _name = vm_struct.entry.get("Klass", "_name").offset;
		private static final long _access_flags = vm_struct.entry.get("Klass", "_access_flags").offset;
		private static final long _next_sibling = vm_struct.entry.get("Klass", "_next_sibling").offset;
		private static final long _next_link = vm_struct.entry.get("Klass", "_next_link").offset;
		private static final long _vtable_len = vm_struct.entry.get("Klass", "_vtable_len").offset;
		private static final long _class_loader_data = vm_struct.entry.get("Klass", "_class_loader_data").offset;
	}

	public static class vtableEntry
	{
		private static final long _method = vm_struct.entry.get("vtableEntry", "_method").offset;
	}

	public static class MethodData
	{
		private static final long _size = vm_struct.entry.get("MethodData", "_size").offset;
		private static final long _method = vm_struct.entry.get("MethodData", "_method").offset;
		private static final long _data_size = vm_struct.entry.get("MethodData", "_data_size").offset;
		private static final long _data_0 = vm_struct.entry.get("MethodData", "_data[0]").offset;
		private static final long _parameters_type_data_di = vm_struct.entry.get("MethodData", "_parameters_type_data_di").offset;
		private static final long _compiler_counters__nof_decompiles = vm_struct.entry.get("MethodData", "_compiler_counters._nof_decompiles").offset;
		private static final long _compiler_counters__nof_overflow_recompiles = vm_struct.entry.get("MethodData", "_compiler_counters._nof_overflow_recompiles").offset;
		private static final long _compiler_counters__nof_overflow_traps = vm_struct.entry.get("MethodData", "_compiler_counters._nof_overflow_traps").offset;
		private static final long _compiler_counters__trap_hist__array_0 = vm_struct.entry.get("MethodData", "_compiler_counters._trap_hist._array[0]").offset;
		private static final long _eflags = vm_struct.entry.get("MethodData", "_eflags").offset;
		private static final long _arg_local = vm_struct.entry.get("MethodData", "_arg_local").offset;
		private static final long _arg_stack = vm_struct.entry.get("MethodData", "_arg_stack").offset;
		private static final long _arg_returned = vm_struct.entry.get("MethodData", "_arg_returned").offset;
		private static final long _tenure_traps = vm_struct.entry.get("MethodData", "_tenure_traps").offset;
		private static final long _invoke_mask = vm_struct.entry.get("MethodData", "_invoke_mask").offset;
		private static final long _backedge_mask = vm_struct.entry.get("MethodData", "_backedge_mask").offset;
	}

	public static class DataLayout
	{
		private static final long _header__struct__tag = vm_struct.entry.get("DataLayout", "_header._struct._tag").offset;
		private static final long _header__struct__flags = vm_struct.entry.get("DataLayout", "_header._struct._flags").offset;
		private static final long _header__struct__bci = vm_struct.entry.get("DataLayout", "_header._struct._bci").offset;
		private static final long _header__struct__traps = vm_struct.entry.get("DataLayout", "_header._struct._traps").offset;
		private static final long _cells_0 = vm_struct.entry.get("DataLayout", "_cells[0]").offset;
	}

	public static class MethodCounters
	{
		private static final long _invoke_mask = vm_struct.entry.get("MethodCounters", "_invoke_mask").offset;
		private static final long _backedge_mask = vm_struct.entry.get("MethodCounters", "_backedge_mask").offset;
		private static final long _interpreter_throwout_count = vm_struct.entry.get("MethodCounters", "_interpreter_throwout_count").offset;
		private static final long _number_of_breakpoints = vm_struct.entry.get("MethodCounters", "_number_of_breakpoints").offset;
		private static final long _invocation_counter = vm_struct.entry.get("MethodCounters", "_invocation_counter").offset;
		private static final long _backedge_counter = vm_struct.entry.get("MethodCounters", "_backedge_counter").offset;
	}

	public static class Method
	{
		private static final long _constMethod = vm_struct.entry.get("Method", "_constMethod").offset;
		private static final long _method_data = vm_struct.entry.get("Method", "_method_data").offset;
		private static final long _method_counters = vm_struct.entry.get("Method", "_method_counters").offset;
		private static final long _access_flags = vm_struct.entry.get("Method", "_access_flags").offset;
		private static final long _vtable_index = vm_struct.entry.get("Method", "_vtable_index").offset;
		private static final long _intrinsic_id = vm_struct.entry.get("Method", "_intrinsic_id").offset;
		private static final long _code = vm_struct.entry.get("Method", "_code").offset;
		private static final long _i2i_entry = vm_struct.entry.get("Method", "_i2i_entry").offset;
		private static final long _from_compiled_entry = vm_struct.entry.get("Method", "_from_compiled_entry").offset;
		private static final long _from_interpreted_entry = vm_struct.entry.get("Method", "_from_interpreted_entry").offset;
	}

	public static class ConstMethod
	{
		private static final long _fingerprint = vm_struct.entry.get("ConstMethod", "_fingerprint").offset;
		private static final long _constants = vm_struct.entry.get("ConstMethod", "_constants").offset;
		private static final long _stackmap_data = vm_struct.entry.get("ConstMethod", "_stackmap_data").offset;
		private static final long _constMethod_size = vm_struct.entry.get("ConstMethod", "_constMethod_size").offset;
		private static final long _flags__flags = vm_struct.entry.get("ConstMethod", "_flags._flags").offset;
		private static final long _code_size = vm_struct.entry.get("ConstMethod", "_code_size").offset;
		private static final long _name_index = vm_struct.entry.get("ConstMethod", "_name_index").offset;
		private static final long _signature_index = vm_struct.entry.get("ConstMethod", "_signature_index").offset;
		private static final long _method_idnum = vm_struct.entry.get("ConstMethod", "_method_idnum").offset;
		private static final long _max_stack = vm_struct.entry.get("ConstMethod", "_max_stack").offset;
		private static final long _max_locals = vm_struct.entry.get("ConstMethod", "_max_locals").offset;
		private static final long _size_of_parameters = vm_struct.entry.get("ConstMethod", "_size_of_parameters").offset;
		private static final long _num_stack_arg_slots = vm_struct.entry.get("ConstMethod", "_num_stack_arg_slots").offset;
	}

	public static class ObjArrayKlass
	{
		private static final long _element_klass = vm_struct.entry.get("ObjArrayKlass", "_element_klass").offset;
		private static final long _bottom_klass = vm_struct.entry.get("ObjArrayKlass", "_bottom_klass").offset;
	}

	public static class Symbol
	{
		private static final long _hash_and_refcount = vm_struct.entry.get("Symbol", "_hash_and_refcount").offset;
		private static final long _length = vm_struct.entry.get("Symbol", "_length").offset;
		private static final long _body = vm_struct.entry.get("Symbol", "_body").offset;
		private static final long _body_0 = vm_struct.entry.get("Symbol", "_body[0]").offset;
		private static final long _vm_symbols_0 = vm_struct.entry.get("Symbol", "_vm_symbols[0]").address;
	}

	public static class TypeArrayKlass
	{
		private static final long _max_length = vm_struct.entry.get("TypeArrayKlass", "_max_length").offset;
	}

	public static class OopHandle
	{
		private static final long _obj = vm_struct.entry.get("OopHandle", "_obj").offset;
	}

	public static class ConstantPoolCacheEntry
	{
		private static final long _indices = vm_struct.entry.get("ConstantPoolCacheEntry", "_indices").offset;
		private static final long _f1 = vm_struct.entry.get("ConstantPoolCacheEntry", "_f1").offset;
		private static final long _f2 = vm_struct.entry.get("ConstantPoolCacheEntry", "_f2").offset;
		private static final long _flags = vm_struct.entry.get("ConstantPoolCacheEntry", "_flags").offset;
	}

	public static class CheckedExceptionElement
	{
		private static final long class_cp_index = vm_struct.entry.get("CheckedExceptionElement", "class_cp_index").offset;
	}

	public static class LocalVariableTableElement
	{
		private static final long start_bci = vm_struct.entry.get("LocalVariableTableElement", "start_bci").offset;
		private static final long length = vm_struct.entry.get("LocalVariableTableElement", "length").offset;
		private static final long name_cp_index = vm_struct.entry.get("LocalVariableTableElement", "name_cp_index").offset;
		private static final long descriptor_cp_index = vm_struct.entry.get("LocalVariableTableElement", "descriptor_cp_index").offset;
		private static final long signature_cp_index = vm_struct.entry.get("LocalVariableTableElement", "signature_cp_index").offset;
		private static final long slot = vm_struct.entry.get("LocalVariableTableElement", "slot").offset;
	}

	public static class ExceptionTableElement
	{
		private static final long start_pc = vm_struct.entry.get("ExceptionTableElement", "start_pc").offset;
		private static final long end_pc = vm_struct.entry.get("ExceptionTableElement", "end_pc").offset;
		private static final long handler_pc = vm_struct.entry.get("ExceptionTableElement", "handler_pc").offset;
		private static final long catch_type_index = vm_struct.entry.get("ExceptionTableElement", "catch_type_index").offset;
	}

	public static class BreakpointInfo
	{
		private static final long _orig_bytecode = vm_struct.entry.get("BreakpointInfo", "_orig_bytecode").offset;
		private static final long _bci = vm_struct.entry.get("BreakpointInfo", "_bci").offset;
		private static final long _name_index = vm_struct.entry.get("BreakpointInfo", "_name_index").offset;
		private static final long _signature_index = vm_struct.entry.get("BreakpointInfo", "_signature_index").offset;
		private static final long _next = vm_struct.entry.get("BreakpointInfo", "_next").offset;
	}

	public static class JNIid
	{
		private static final long _holder = vm_struct.entry.get("JNIid", "_holder").offset;
		private static final long _next = vm_struct.entry.get("JNIid", "_next").offset;
		private static final long _offset = vm_struct.entry.get("JNIid", "_offset").offset;
	}

	public static class Universe
	{
		private static final long _collectedHeap = vm_struct.entry.get("Universe", "_collectedHeap").address;
	}

	public static class CompressedOops
	{
		private static final long _narrow_oop__base = vm_struct.entry.get("CompressedOops", "_narrow_oop._base").address;
		private static final long _narrow_oop__shift = vm_struct.entry.get("CompressedOops", "_narrow_oop._shift").address;
		private static final long _narrow_oop__use_implicit_null_checks = vm_struct.entry.get("CompressedOops", "_narrow_oop._use_implicit_null_checks").address;
	}

	public static class CompressedKlassPointers
	{
		private static final long _narrow_klass__base = vm_struct.entry.get("CompressedKlassPointers", "_narrow_klass._base").address;
		private static final long _narrow_klass__shift = vm_struct.entry.get("CompressedKlassPointers", "_narrow_klass._shift").address;
	}

	public static class MetaspaceObj
	{
		private static final long _shared_metaspace_base = vm_struct.entry.get("MetaspaceObj", "_shared_metaspace_base").address;
		private static final long _shared_metaspace_top = vm_struct.entry.get("MetaspaceObj", "_shared_metaspace_top").address;
	}

	public static class ThreadLocalAllocBuffer
	{
		private static final long _start = vm_struct.entry.get("ThreadLocalAllocBuffer", "_start").offset;
		private static final long _top = vm_struct.entry.get("ThreadLocalAllocBuffer", "_top").offset;
		private static final long _end = vm_struct.entry.get("ThreadLocalAllocBuffer", "_end").offset;
		private static final long _pf_top = vm_struct.entry.get("ThreadLocalAllocBuffer", "_pf_top").offset;
		private static final long _desired_size = vm_struct.entry.get("ThreadLocalAllocBuffer", "_desired_size").offset;
		private static final long _refill_waste_limit = vm_struct.entry.get("ThreadLocalAllocBuffer", "_refill_waste_limit").offset;
		private static final long _reserve_for_allocation_prefetch = vm_struct.entry.get("ThreadLocalAllocBuffer", "_reserve_for_allocation_prefetch").address;
		private static final long _target_refills = vm_struct.entry.get("ThreadLocalAllocBuffer", "_target_refills").address;
		private static final long _number_of_refills = vm_struct.entry.get("ThreadLocalAllocBuffer", "_number_of_refills").offset;
		private static final long _refill_waste = vm_struct.entry.get("ThreadLocalAllocBuffer", "_refill_waste").offset;
		private static final long _gc_waste = vm_struct.entry.get("ThreadLocalAllocBuffer", "_gc_waste").offset;
		private static final long _slow_allocations = vm_struct.entry.get("ThreadLocalAllocBuffer", "_slow_allocations").offset;
	}

	public static class VirtualSpace
	{
		private static final long _low_boundary = vm_struct.entry.get("VirtualSpace", "_low_boundary").offset;
		private static final long _high_boundary = vm_struct.entry.get("VirtualSpace", "_high_boundary").offset;
		private static final long _low = vm_struct.entry.get("VirtualSpace", "_low").offset;
		private static final long _high = vm_struct.entry.get("VirtualSpace", "_high").offset;
		private static final long _lower_high = vm_struct.entry.get("VirtualSpace", "_lower_high").offset;
		private static final long _middle_high = vm_struct.entry.get("VirtualSpace", "_middle_high").offset;
		private static final long _upper_high = vm_struct.entry.get("VirtualSpace", "_upper_high").offset;
	}

	public static class PerfDataPrologue
	{
		private static final long magic = vm_struct.entry.get("PerfDataPrologue", "magic").offset;
		private static final long byte_order = vm_struct.entry.get("PerfDataPrologue", "byte_order").offset;
		private static final long major_version = vm_struct.entry.get("PerfDataPrologue", "major_version").offset;
		private static final long minor_version = vm_struct.entry.get("PerfDataPrologue", "minor_version").offset;
		private static final long accessible = vm_struct.entry.get("PerfDataPrologue", "accessible").offset;
		private static final long used = vm_struct.entry.get("PerfDataPrologue", "used").offset;
		private static final long overflow = vm_struct.entry.get("PerfDataPrologue", "overflow").offset;
		private static final long mod_time_stamp = vm_struct.entry.get("PerfDataPrologue", "mod_time_stamp").offset;
		private static final long entry_offset = vm_struct.entry.get("PerfDataPrologue", "entry_offset").offset;
		private static final long num_entries = vm_struct.entry.get("PerfDataPrologue", "num_entries").offset;
	}

	public static class PerfDataEntry
	{
		private static final long entry_length = vm_struct.entry.get("PerfDataEntry", "entry_length").offset;
		private static final long name_offset = vm_struct.entry.get("PerfDataEntry", "name_offset").offset;
		private static final long vector_length = vm_struct.entry.get("PerfDataEntry", "vector_length").offset;
		private static final long data_type = vm_struct.entry.get("PerfDataEntry", "data_type").offset;
		private static final long flags = vm_struct.entry.get("PerfDataEntry", "flags").offset;
		private static final long data_units = vm_struct.entry.get("PerfDataEntry", "data_units").offset;
		private static final long data_variability = vm_struct.entry.get("PerfDataEntry", "data_variability").offset;
		private static final long data_offset = vm_struct.entry.get("PerfDataEntry", "data_offset").offset;
	}

	public static class PerfMemory
	{
		private static final long _start = vm_struct.entry.get("PerfMemory", "_start").address;
		private static final long _end = vm_struct.entry.get("PerfMemory", "_end").address;
		private static final long _top = vm_struct.entry.get("PerfMemory", "_top").address;
		private static final long _capacity = vm_struct.entry.get("PerfMemory", "_capacity").address;
		private static final long _prologue = vm_struct.entry.get("PerfMemory", "_prologue").address;
		private static final long _initialized = vm_struct.entry.get("PerfMemory", "_initialized").address;
	}

	public static class vmClasses
	{
		private static final long _klasses_Object_klass_knum = vm_struct.entry.get("vmClasses", "_klasses[static_cast<int>(vmClassID::Object_klass_knum)]").address;
		private static final long _klasses_String_klass_knum = vm_struct.entry.get("vmClasses", "_klasses[static_cast<int>(vmClassID::String_klass_knum)]").address;
		private static final long _klasses_Class_klass_knum = vm_struct.entry.get("vmClasses", "_klasses[static_cast<int>(vmClassID::Class_klass_knum)]").address;
		private static final long _klasses_ClassLoader_klass_knum = vm_struct.entry.get("vmClasses", "_klasses[static_cast<int>(vmClassID::ClassLoader_klass_knum)]").address;
		private static final long _klasses_System_klass_knum = vm_struct.entry.get("vmClasses", "_klasses[static_cast<int>(vmClassID::System_klass_knum)]").address;
		private static final long _klasses_Thread_klass_knum = vm_struct.entry.get("vmClasses", "_klasses[static_cast<int>(vmClassID::Thread_klass_knum)]").address;
		private static final long _klasses_Thread_FieldHolder_klass_knum = vm_struct.entry.get("vmClasses", "_klasses[static_cast<int>(vmClassID::Thread_FieldHolder_klass_knum)]").address;
		private static final long _klasses_ThreadGroup_klass_knum = vm_struct.entry.get("vmClasses", "_klasses[static_cast<int>(vmClassID::ThreadGroup_klass_knum)]").address;
		private static final long _klasses_MethodHandle_klass_knum = vm_struct.entry.get("vmClasses", "_klasses[static_cast<int>(vmClassID::MethodHandle_klass_knum)]").address;
	}

	public static class ClassLoaderData
	{
		private static final long _class_loader = vm_struct.entry.get("ClassLoaderData", "_class_loader").offset;
		private static final long _next = vm_struct.entry.get("ClassLoaderData", "_next").offset;
		private static final long _klasses = vm_struct.entry.get("ClassLoaderData", "_klasses").offset;
		private static final long _has_class_mirror_holder = vm_struct.entry.get("ClassLoaderData", "_has_class_mirror_holder").offset;
	}

	public static class ClassLoaderDataGraph
	{
		private static final long _head = vm_struct.entry.get("ClassLoaderDataGraph", "_head").address;
	}

	public static class Array_Klass_
	{
		private static final long _length = vm_struct.entry.get("Array<Klass*>", "_length").offset;
		private static final long _data_0 = vm_struct.entry.get("Array<Klass*>", "_data[0]").offset;
	}

	public static class Array_ResolvedIndyEntry_
	{
		private static final long _length = vm_struct.entry.get("Array<ResolvedIndyEntry>", "_length").offset;
		private static final long _data_0 = vm_struct.entry.get("Array<ResolvedIndyEntry>", "_data[0]").offset;
	}

	public static class GrowableArrayBase
	{
		private static final long _len = vm_struct.entry.get("GrowableArrayBase", "_len").offset;
		private static final long _capacity = vm_struct.entry.get("GrowableArrayBase", "_capacity").offset;
	}

	public static class GrowableArray_int_
	{
		private static final long _data = vm_struct.entry.get("GrowableArray<int>", "_data").offset;
	}

	public static class CodeCache
	{
		private static final long _heaps = vm_struct.entry.get("CodeCache", "_heaps").address;
		private static final long _low_bound = vm_struct.entry.get("CodeCache", "_low_bound").address;
		private static final long _high_bound = vm_struct.entry.get("CodeCache", "_high_bound").address;
	}

	public static class CodeHeap
	{
		private static final long _memory = vm_struct.entry.get("CodeHeap", "_memory").offset;
		private static final long _segmap = vm_struct.entry.get("CodeHeap", "_segmap").offset;
		private static final long _log2_segment_size = vm_struct.entry.get("CodeHeap", "_log2_segment_size").offset;
	}

	public static class HeapBlock
	{
		private static final long _header = vm_struct.entry.get("HeapBlock", "_header").offset;
	}

	public static class HeapBlock_Header
	{
		private static final long _length = vm_struct.entry.get("HeapBlock::Header", "_length").offset;
		private static final long _used = vm_struct.entry.get("HeapBlock::Header", "_used").offset;
	}

	public static class AbstractInterpreter
	{
		private static final long _code = vm_struct.entry.get("AbstractInterpreter", "_code").address;
	}

	public static class StubQueue
	{
		private static final long _stub_buffer = vm_struct.entry.get("StubQueue", "_stub_buffer").offset;
		private static final long _buffer_limit = vm_struct.entry.get("StubQueue", "_buffer_limit").offset;
		private static final long _queue_begin = vm_struct.entry.get("StubQueue", "_queue_begin").offset;
		private static final long _queue_end = vm_struct.entry.get("StubQueue", "_queue_end").offset;
		private static final long _number_of_stubs = vm_struct.entry.get("StubQueue", "_number_of_stubs").offset;
	}

	public static class InterpreterCodelet
	{
		private static final long _size = vm_struct.entry.get("InterpreterCodelet", "_size").offset;
		private static final long _description = vm_struct.entry.get("InterpreterCodelet", "_description").offset;
		private static final long _bytecode = vm_struct.entry.get("InterpreterCodelet", "_bytecode").offset;
	}

	public static class StubRoutines
	{
		private static final long _verify_oop_count = vm_struct.entry.get("StubRoutines", "_verify_oop_count").address;
		private static final long _call_stub_return_address = vm_struct.entry.get("StubRoutines", "_call_stub_return_address").address;
		private static final long _aescrypt_encryptBlock = vm_struct.entry.get("StubRoutines", "_aescrypt_encryptBlock").address;
		private static final long _aescrypt_decryptBlock = vm_struct.entry.get("StubRoutines", "_aescrypt_decryptBlock").address;
		private static final long _cipherBlockChaining_encryptAESCrypt = vm_struct.entry.get("StubRoutines", "_cipherBlockChaining_encryptAESCrypt").address;
		private static final long _cipherBlockChaining_decryptAESCrypt = vm_struct.entry.get("StubRoutines", "_cipherBlockChaining_decryptAESCrypt").address;
		private static final long _electronicCodeBook_encryptAESCrypt = vm_struct.entry.get("StubRoutines", "_electronicCodeBook_encryptAESCrypt").address;
		private static final long _electronicCodeBook_decryptAESCrypt = vm_struct.entry.get("StubRoutines", "_electronicCodeBook_decryptAESCrypt").address;
		private static final long _counterMode_AESCrypt = vm_struct.entry.get("StubRoutines", "_counterMode_AESCrypt").address;
		private static final long _galoisCounterMode_AESCrypt = vm_struct.entry.get("StubRoutines", "_galoisCounterMode_AESCrypt").address;
		private static final long _ghash_processBlocks = vm_struct.entry.get("StubRoutines", "_ghash_processBlocks").address;
		private static final long _chacha20Block = vm_struct.entry.get("StubRoutines", "_chacha20Block").address;
		private static final long _base64_encodeBlock = vm_struct.entry.get("StubRoutines", "_base64_encodeBlock").address;
		private static final long _base64_decodeBlock = vm_struct.entry.get("StubRoutines", "_base64_decodeBlock").address;
		private static final long _poly1305_processBlocks = vm_struct.entry.get("StubRoutines", "_poly1305_processBlocks").address;
		private static final long _updateBytesCRC32 = vm_struct.entry.get("StubRoutines", "_updateBytesCRC32").address;
		private static final long _crc_table_adr = vm_struct.entry.get("StubRoutines", "_crc_table_adr").address;
		private static final long _crc32c_table_addr = vm_struct.entry.get("StubRoutines", "_crc32c_table_addr").address;
		private static final long _updateBytesCRC32C = vm_struct.entry.get("StubRoutines", "_updateBytesCRC32C").address;
		private static final long _updateBytesAdler32 = vm_struct.entry.get("StubRoutines", "_updateBytesAdler32").address;
		private static final long _multiplyToLen = vm_struct.entry.get("StubRoutines", "_multiplyToLen").address;
		private static final long _squareToLen = vm_struct.entry.get("StubRoutines", "_squareToLen").address;
		private static final long _bigIntegerRightShiftWorker = vm_struct.entry.get("StubRoutines", "_bigIntegerRightShiftWorker").address;
		private static final long _bigIntegerLeftShiftWorker = vm_struct.entry.get("StubRoutines", "_bigIntegerLeftShiftWorker").address;
		private static final long _mulAdd = vm_struct.entry.get("StubRoutines", "_mulAdd").address;
		private static final long _dexp = vm_struct.entry.get("StubRoutines", "_dexp").address;
		private static final long _dlog = vm_struct.entry.get("StubRoutines", "_dlog").address;
		private static final long _dlog10 = vm_struct.entry.get("StubRoutines", "_dlog10").address;
		private static final long _dpow = vm_struct.entry.get("StubRoutines", "_dpow").address;
		private static final long _dsin = vm_struct.entry.get("StubRoutines", "_dsin").address;
		private static final long _dcos = vm_struct.entry.get("StubRoutines", "_dcos").address;
		private static final long _dtan = vm_struct.entry.get("StubRoutines", "_dtan").address;
		private static final long _vectorizedMismatch = vm_struct.entry.get("StubRoutines", "_vectorizedMismatch").address;
		private static final long _jbyte_arraycopy = vm_struct.entry.get("StubRoutines", "_jbyte_arraycopy").address;
		private static final long _jshort_arraycopy = vm_struct.entry.get("StubRoutines", "_jshort_arraycopy").address;
		private static final long _jint_arraycopy = vm_struct.entry.get("StubRoutines", "_jint_arraycopy").address;
		private static final long _jlong_arraycopy = vm_struct.entry.get("StubRoutines", "_jlong_arraycopy").address;
		private static final long _oop_arraycopy = vm_struct.entry.get("StubRoutines", "_oop_arraycopy").address;
		private static final long _oop_arraycopy_uninit = vm_struct.entry.get("StubRoutines", "_oop_arraycopy_uninit").address;
		private static final long _jbyte_disjoint_arraycopy = vm_struct.entry.get("StubRoutines", "_jbyte_disjoint_arraycopy").address;
		private static final long _jshort_disjoint_arraycopy = vm_struct.entry.get("StubRoutines", "_jshort_disjoint_arraycopy").address;
		private static final long _jint_disjoint_arraycopy = vm_struct.entry.get("StubRoutines", "_jint_disjoint_arraycopy").address;
		private static final long _jlong_disjoint_arraycopy = vm_struct.entry.get("StubRoutines", "_jlong_disjoint_arraycopy").address;
		private static final long _oop_disjoint_arraycopy = vm_struct.entry.get("StubRoutines", "_oop_disjoint_arraycopy").address;
		private static final long _oop_disjoint_arraycopy_uninit = vm_struct.entry.get("StubRoutines", "_oop_disjoint_arraycopy_uninit").address;
		private static final long _arrayof_jbyte_arraycopy = vm_struct.entry.get("StubRoutines", "_arrayof_jbyte_arraycopy").address;
		private static final long _arrayof_jshort_arraycopy = vm_struct.entry.get("StubRoutines", "_arrayof_jshort_arraycopy").address;
		private static final long _arrayof_jint_arraycopy = vm_struct.entry.get("StubRoutines", "_arrayof_jint_arraycopy").address;
		private static final long _arrayof_jlong_arraycopy = vm_struct.entry.get("StubRoutines", "_arrayof_jlong_arraycopy").address;
		private static final long _arrayof_oop_arraycopy = vm_struct.entry.get("StubRoutines", "_arrayof_oop_arraycopy").address;
		private static final long _arrayof_oop_arraycopy_uninit = vm_struct.entry.get("StubRoutines", "_arrayof_oop_arraycopy_uninit").address;
		private static final long _arrayof_jbyte_disjoint_arraycopy = vm_struct.entry.get("StubRoutines", "_arrayof_jbyte_disjoint_arraycopy").address;
		private static final long _arrayof_jshort_disjoint_arraycopy = vm_struct.entry.get("StubRoutines", "_arrayof_jshort_disjoint_arraycopy").address;
		private static final long _arrayof_jint_disjoint_arraycopy = vm_struct.entry.get("StubRoutines", "_arrayof_jint_disjoint_arraycopy").address;
		private static final long _arrayof_jlong_disjoint_arraycopy = vm_struct.entry.get("StubRoutines", "_arrayof_jlong_disjoint_arraycopy").address;
		private static final long _arrayof_oop_disjoint_arraycopy = vm_struct.entry.get("StubRoutines", "_arrayof_oop_disjoint_arraycopy").address;
		private static final long _arrayof_oop_disjoint_arraycopy_uninit = vm_struct.entry.get("StubRoutines", "_arrayof_oop_disjoint_arraycopy_uninit").address;
		private static final long _checkcast_arraycopy = vm_struct.entry.get("StubRoutines", "_checkcast_arraycopy").address;
		private static final long _checkcast_arraycopy_uninit = vm_struct.entry.get("StubRoutines", "_checkcast_arraycopy_uninit").address;
		private static final long _unsafe_arraycopy = vm_struct.entry.get("StubRoutines", "_unsafe_arraycopy").address;
		private static final long _generic_arraycopy = vm_struct.entry.get("StubRoutines", "_generic_arraycopy").address;
	}

	public static class SharedRuntime
	{
		private static final long _wrong_method_blob = vm_struct.entry.get("SharedRuntime", "_wrong_method_blob").address;
		private static final long _ic_miss_blob = vm_struct.entry.get("SharedRuntime", "_ic_miss_blob").address;
		private static final long _deopt_blob = vm_struct.entry.get("SharedRuntime", "_deopt_blob").address;
	}

	public static class PcDesc
	{
		private static final long _pc_offset = vm_struct.entry.get("PcDesc", "_pc_offset").offset;
		private static final long _scope_decode_offset = vm_struct.entry.get("PcDesc", "_scope_decode_offset").offset;
		private static final long _obj_decode_offset = vm_struct.entry.get("PcDesc", "_obj_decode_offset").offset;
		private static final long _flags = vm_struct.entry.get("PcDesc", "_flags").offset;
	}

	public static class CodeBlob
	{
		private static final long _name = vm_struct.entry.get("CodeBlob", "_name").offset;
		private static final long _size = vm_struct.entry.get("CodeBlob", "_size").offset;
		private static final long _header_size = vm_struct.entry.get("CodeBlob", "_header_size").offset;
		private static final long _frame_complete_offset = vm_struct.entry.get("CodeBlob", "_frame_complete_offset").offset;
		private static final long _data_offset = vm_struct.entry.get("CodeBlob", "_data_offset").offset;
		private static final long _frame_size = vm_struct.entry.get("CodeBlob", "_frame_size").offset;
		private static final long _oop_maps = vm_struct.entry.get("CodeBlob", "_oop_maps").offset;
		private static final long _code_begin = vm_struct.entry.get("CodeBlob", "_code_begin").offset;
		private static final long _code_end = vm_struct.entry.get("CodeBlob", "_code_end").offset;
		private static final long _content_begin = vm_struct.entry.get("CodeBlob", "_content_begin").offset;
		private static final long _data_end = vm_struct.entry.get("CodeBlob", "_data_end").offset;
	}

	public static class DeoptimizationBlob
	{
		private static final long _unpack_offset = vm_struct.entry.get("DeoptimizationBlob", "_unpack_offset").offset;
	}

	public static class RuntimeStub
	{
		private static final long _caller_must_gc_arguments = vm_struct.entry.get("RuntimeStub", "_caller_must_gc_arguments").offset;
	}

	public static class CompiledMethod
	{
		private static final long _method = vm_struct.entry.get("CompiledMethod", "_method").offset;
		private static final long _exception_cache = vm_struct.entry.get("CompiledMethod", "_exception_cache").offset;
		private static final long _scopes_data_begin = vm_struct.entry.get("CompiledMethod", "_scopes_data_begin").offset;
		private static final long _deopt_handler_begin = vm_struct.entry.get("CompiledMethod", "_deopt_handler_begin").offset;
		private static final long _deopt_mh_handler_begin = vm_struct.entry.get("CompiledMethod", "_deopt_mh_handler_begin").offset;
	}

	public static class nmethod
	{
		private static final long _entry_bci = vm_struct.entry.get("nmethod", "_entry_bci").offset;
		private static final long _osr_link = vm_struct.entry.get("nmethod", "_osr_link").offset;
		private static final long _state = vm_struct.entry.get("nmethod", "_state").offset;
		private static final long _exception_offset = vm_struct.entry.get("nmethod", "_exception_offset").offset;
		private static final long _orig_pc_offset = vm_struct.entry.get("nmethod", "_orig_pc_offset").offset;
		private static final long _stub_offset = vm_struct.entry.get("nmethod", "_stub_offset").offset;
		private static final long _consts_offset = vm_struct.entry.get("nmethod", "_consts_offset").offset;
		private static final long _oops_offset = vm_struct.entry.get("nmethod", "_oops_offset").offset;
		private static final long _metadata_offset = vm_struct.entry.get("nmethod", "_metadata_offset").offset;
		private static final long _scopes_pcs_offset = vm_struct.entry.get("nmethod", "_scopes_pcs_offset").offset;
		private static final long _dependencies_offset = vm_struct.entry.get("nmethod", "_dependencies_offset").offset;
		private static final long _handler_table_offset = vm_struct.entry.get("nmethod", "_handler_table_offset").offset;
		private static final long _nul_chk_table_offset = vm_struct.entry.get("nmethod", "_nul_chk_table_offset").offset;
		private static final long _nmethod_end_offset = vm_struct.entry.get("nmethod", "_nmethod_end_offset").offset;
		private static final long _entry_point = vm_struct.entry.get("nmethod", "_entry_point").offset;
		private static final long _verified_entry_point = vm_struct.entry.get("nmethod", "_verified_entry_point").offset;
		private static final long _osr_entry_point = vm_struct.entry.get("nmethod", "_osr_entry_point").offset;
		private static final long _compile_id = vm_struct.entry.get("nmethod", "_compile_id").offset;
		private static final long _comp_level = vm_struct.entry.get("nmethod", "_comp_level").offset;
	}

	public static class Deoptimization
	{
		private static final long _trap_reason_name = vm_struct.entry.get("Deoptimization", "_trap_reason_name").address;
	}

	public static class Deoptimization_UnrollBlock
	{
		private static final long _size_of_deoptimized_frame = vm_struct.entry.get("Deoptimization::UnrollBlock", "_size_of_deoptimized_frame").offset;
		private static final long _caller_adjustment = vm_struct.entry.get("Deoptimization::UnrollBlock", "_caller_adjustment").offset;
		private static final long _number_of_frames = vm_struct.entry.get("Deoptimization::UnrollBlock", "_number_of_frames").offset;
		private static final long _total_frame_sizes = vm_struct.entry.get("Deoptimization::UnrollBlock", "_total_frame_sizes").offset;
		private static final long _unpack_kind = vm_struct.entry.get("Deoptimization::UnrollBlock", "_unpack_kind").offset;
		private static final long _frame_sizes = vm_struct.entry.get("Deoptimization::UnrollBlock", "_frame_sizes").offset;
		private static final long _frame_pcs = vm_struct.entry.get("Deoptimization::UnrollBlock", "_frame_pcs").offset;
		private static final long _register_block = vm_struct.entry.get("Deoptimization::UnrollBlock", "_register_block").offset;
		private static final long _return_type = vm_struct.entry.get("Deoptimization::UnrollBlock", "_return_type").offset;
		private static final long _initial_info = vm_struct.entry.get("Deoptimization::UnrollBlock", "_initial_info").offset;
		private static final long _caller_actual_parameters = vm_struct.entry.get("Deoptimization::UnrollBlock", "_caller_actual_parameters").offset;
	}

	public static class JavaCallWrapper
	{
		private static final long _anchor = vm_struct.entry.get("JavaCallWrapper", "_anchor").offset;
	}

	public static class JavaFrameAnchor
	{
		private static final long _last_Java_sp = vm_struct.entry.get("JavaFrameAnchor", "_last_Java_sp").offset;
		private static final long _last_Java_pc = vm_struct.entry.get("JavaFrameAnchor", "_last_Java_pc").offset;
		private static final long _last_Java_fp = vm_struct.entry.get("JavaFrameAnchor", "_last_Java_fp").offset;
	}

	public static class Threads
	{
		private static final long _number_of_threads = vm_struct.entry.get("Threads", "_number_of_threads").address;
		private static final long _number_of_non_daemon_threads = vm_struct.entry.get("Threads", "_number_of_non_daemon_threads").address;
		private static final long _return_code = vm_struct.entry.get("Threads", "_return_code").address;
	}

	public static class ThreadsSMRSupport
	{
		private static final long _java_thread_list = vm_struct.entry.get("ThreadsSMRSupport", "_java_thread_list").address;
	}

	public static class ThreadsList
	{
		private static final long _length = vm_struct.entry.get("ThreadsList", "_length").offset;
		private static final long _threads = vm_struct.entry.get("ThreadsList", "_threads").offset;
	}

	public static class ThreadShadow
	{
		private static final long _pending_exception = vm_struct.entry.get("ThreadShadow", "_pending_exception").offset;
		private static final long _exception_file = vm_struct.entry.get("ThreadShadow", "_exception_file").offset;
		private static final long _exception_line = vm_struct.entry.get("ThreadShadow", "_exception_line").offset;
	}

	public static class Thread
	{
		private static final long _tlab = vm_struct.entry.get("Thread", "_tlab").offset;
		private static final long _allocated_bytes = vm_struct.entry.get("Thread", "_allocated_bytes").offset;
		private static final long _resource_area = vm_struct.entry.get("Thread", "_resource_area").offset;
	}

	public static class JavaThread
	{
		private static final long _lock_stack = vm_struct.entry.get("JavaThread", "_lock_stack").offset;
		private static final long _threadObj = vm_struct.entry.get("JavaThread", "_threadObj").offset;
		private static final long _vthread = vm_struct.entry.get("JavaThread", "_vthread").offset;
		private static final long _jvmti_vthread = vm_struct.entry.get("JavaThread", "_jvmti_vthread").offset;
		private static final long _scopedValueCache = vm_struct.entry.get("JavaThread", "_scopedValueCache").offset;
		private static final long _anchor = vm_struct.entry.get("JavaThread", "_anchor").offset;
		private static final long _vm_result = vm_struct.entry.get("JavaThread", "_vm_result").offset;
		private static final long _vm_result_2 = vm_struct.entry.get("JavaThread", "_vm_result_2").offset;
		private static final long _current_pending_monitor = vm_struct.entry.get("JavaThread", "_current_pending_monitor").offset;
		private static final long _current_pending_monitor_is_from_java = vm_struct.entry.get("JavaThread", "_current_pending_monitor_is_from_java").offset;
		private static final long _current_waiting_monitor = vm_struct.entry.get("JavaThread", "_current_waiting_monitor").offset;
		private static final long _suspend_flags = vm_struct.entry.get("JavaThread", "_suspend_flags").offset;
		private static final long _exception_oop = vm_struct.entry.get("JavaThread", "_exception_oop").offset;
		private static final long _exception_pc = vm_struct.entry.get("JavaThread", "_exception_pc").offset;
		private static final long _is_method_handle_return = vm_struct.entry.get("JavaThread", "_is_method_handle_return").offset;
		private static final long _saved_exception_pc = vm_struct.entry.get("JavaThread", "_saved_exception_pc").offset;
		private static final long _thread_state = vm_struct.entry.get("JavaThread", "_thread_state").offset;
		private static final long _osthread = vm_struct.entry.get("JavaThread", "_osthread").offset;
		private static final long _stack_base = vm_struct.entry.get("JavaThread", "_stack_base").offset;
		private static final long _stack_size = vm_struct.entry.get("JavaThread", "_stack_size").offset;
		private static final long _vframe_array_head = vm_struct.entry.get("JavaThread", "_vframe_array_head").offset;
		private static final long _vframe_array_last = vm_struct.entry.get("JavaThread", "_vframe_array_last").offset;
		private static final long _active_handles = vm_struct.entry.get("JavaThread", "_active_handles").offset;
		private static final long _terminated = vm_struct.entry.get("JavaThread", "_terminated").offset;
	}

	public static class LockStack
	{
		private static final long _top = vm_struct.entry.get("LockStack", "_top").offset;
		private static final long _base_0 = vm_struct.entry.get("LockStack", "_base[0]").offset;
	}

	public static class NamedThread
	{
		private static final long _name = vm_struct.entry.get("NamedThread", "_name").offset;
		private static final long _processed_thread = vm_struct.entry.get("NamedThread", "_processed_thread").offset;
	}

	public static class CompilerThread
	{
		private static final long _env = vm_struct.entry.get("CompilerThread", "_env").offset;
	}

	public static class OSThread
	{
		private static final long _state = vm_struct.entry.get("OSThread", "_state").offset;
		private static final long _thread_id = vm_struct.entry.get("OSThread", "_thread_id").offset;
		private static final long _thread_handle = vm_struct.entry.get("OSThread", "_thread_handle").offset;
	}

	public static class ImmutableOopMapSet
	{
		private static final long _count = vm_struct.entry.get("ImmutableOopMapSet", "_count").offset;
		private static final long _size = vm_struct.entry.get("ImmutableOopMapSet", "_size").offset;
	}

	public static class ImmutableOopMapPair
	{
		private static final long _pc_offset = vm_struct.entry.get("ImmutableOopMapPair", "_pc_offset").offset;
		private static final long _oopmap_offset = vm_struct.entry.get("ImmutableOopMapPair", "_oopmap_offset").offset;
	}

	public static class ImmutableOopMap
	{
		private static final long _count = vm_struct.entry.get("ImmutableOopMap", "_count").offset;
	}

	public static class JNIHandles
	{
		private static final long _global_handles = vm_struct.entry.get("JNIHandles", "_global_handles").address;
		private static final long _weak_global_handles = vm_struct.entry.get("JNIHandles", "_weak_global_handles").address;
	}

	public static class JNIHandleBlock
	{
		private static final long _handles = vm_struct.entry.get("JNIHandleBlock", "_handles").offset;
		private static final long _top = vm_struct.entry.get("JNIHandleBlock", "_top").offset;
		private static final long _next = vm_struct.entry.get("JNIHandleBlock", "_next").offset;
	}

	public static class CompressedStream
	{
		private static final long _buffer = vm_struct.entry.get("CompressedStream", "_buffer").offset;
		private static final long _position = vm_struct.entry.get("CompressedStream", "_position").offset;
	}

	public static class VMRegImpl
	{
		private static final long regName_0 = vm_struct.entry.get("VMRegImpl", "regName[0]").address;
		private static final long stack0 = vm_struct.entry.get("VMRegImpl", "stack0").address;
	}

	public static class Runtime1
	{
		private static final long _blobs = vm_struct.entry.get("Runtime1", "_blobs").address;
	}

	public static class ciEnv
	{
		private static final long _compiler_data = vm_struct.entry.get("ciEnv", "_compiler_data").offset;
		private static final long _failure_reason = vm_struct.entry.get("ciEnv", "_failure_reason").offset;
		private static final long _factory = vm_struct.entry.get("ciEnv", "_factory").offset;
		private static final long _dependencies = vm_struct.entry.get("ciEnv", "_dependencies").offset;
		private static final long _task = vm_struct.entry.get("ciEnv", "_task").offset;
		private static final long _arena = vm_struct.entry.get("ciEnv", "_arena").offset;
	}

	public static class ciBaseObject
	{
		private static final long _ident = vm_struct.entry.get("ciBaseObject", "_ident").offset;
	}

	public static class ciObject
	{
		private static final long _handle = vm_struct.entry.get("ciObject", "_handle").offset;
		private static final long _klass = vm_struct.entry.get("ciObject", "_klass").offset;
	}

	public static class ciMetadata
	{
		private static final long _metadata = vm_struct.entry.get("ciMetadata", "_metadata").offset;
	}

	public static class ciSymbol
	{
		private static final long _symbol = vm_struct.entry.get("ciSymbol", "_symbol").offset;
	}

	public static class ciType
	{
		private static final long _basic_type = vm_struct.entry.get("ciType", "_basic_type").offset;
	}

	public static class ciKlass
	{
		private static final long _name = vm_struct.entry.get("ciKlass", "_name").offset;
	}

	public static class ciArrayKlass
	{
		private static final long _dimension = vm_struct.entry.get("ciArrayKlass", "_dimension").offset;
	}

	public static class ciObjArrayKlass
	{
		private static final long _element_klass = vm_struct.entry.get("ciObjArrayKlass", "_element_klass").offset;
		private static final long _base_element_klass = vm_struct.entry.get("ciObjArrayKlass", "_base_element_klass").offset;
	}

	public static class ciInstanceKlass
	{
		private static final long _init_state = vm_struct.entry.get("ciInstanceKlass", "_init_state").offset;
		private static final long _is_shared = vm_struct.entry.get("ciInstanceKlass", "_is_shared").offset;
	}

	public static class ciMethod
	{
		private static final long _interpreter_invocation_count = vm_struct.entry.get("ciMethod", "_interpreter_invocation_count").offset;
		private static final long _interpreter_throwout_count = vm_struct.entry.get("ciMethod", "_interpreter_throwout_count").offset;
		private static final long _inline_instructions_size = vm_struct.entry.get("ciMethod", "_inline_instructions_size").offset;
	}

	public static class ciMethodData
	{
		private static final long _data_size = vm_struct.entry.get("ciMethodData", "_data_size").offset;
		private static final long _state = vm_struct.entry.get("ciMethodData", "_state").offset;
		private static final long _extra_data_size = vm_struct.entry.get("ciMethodData", "_extra_data_size").offset;
		private static final long _data = vm_struct.entry.get("ciMethodData", "_data").offset;
		private static final long _hint_di = vm_struct.entry.get("ciMethodData", "_hint_di").offset;
		private static final long _eflags = vm_struct.entry.get("ciMethodData", "_eflags").offset;
		private static final long _arg_local = vm_struct.entry.get("ciMethodData", "_arg_local").offset;
		private static final long _arg_stack = vm_struct.entry.get("ciMethodData", "_arg_stack").offset;
		private static final long _arg_returned = vm_struct.entry.get("ciMethodData", "_arg_returned").offset;
		private static final long _orig = vm_struct.entry.get("ciMethodData", "_orig").offset;
	}

	public static class ciField
	{
		private static final long _holder = vm_struct.entry.get("ciField", "_holder").offset;
		private static final long _name = vm_struct.entry.get("ciField", "_name").offset;
		private static final long _signature = vm_struct.entry.get("ciField", "_signature").offset;
		private static final long _offset = vm_struct.entry.get("ciField", "_offset").offset;
		private static final long _is_constant = vm_struct.entry.get("ciField", "_is_constant").offset;
		private static final long _constant_value = vm_struct.entry.get("ciField", "_constant_value").offset;
	}

	public static class ciObjectFactory
	{
		private static final long _ci_metadata = vm_struct.entry.get("ciObjectFactory", "_ci_metadata").offset;
		private static final long _symbols = vm_struct.entry.get("ciObjectFactory", "_symbols").offset;
	}

	public static class ciConstant
	{
		private static final long _type = vm_struct.entry.get("ciConstant", "_type").offset;
		private static final long _value__int = vm_struct.entry.get("ciConstant", "_value._int").offset;
		private static final long _value__long = vm_struct.entry.get("ciConstant", "_value._long").offset;
		private static final long _value__float = vm_struct.entry.get("ciConstant", "_value._float").offset;
		private static final long _value__double = vm_struct.entry.get("ciConstant", "_value._double").offset;
		private static final long _value__object = vm_struct.entry.get("ciConstant", "_value._object").offset;
	}

	public static class ObjectMonitor
	{
		private static final long _header = vm_struct.entry.get("ObjectMonitor", "_header").offset;
		private static final long _object = vm_struct.entry.get("ObjectMonitor", "_object").offset;
		private static final long _owner = vm_struct.entry.get("ObjectMonitor", "_owner").offset;
		private static final long _next_om = vm_struct.entry.get("ObjectMonitor", "_next_om").offset;
		private static final long _contentions = vm_struct.entry.get("ObjectMonitor", "_contentions").offset;
		private static final long _waiters = vm_struct.entry.get("ObjectMonitor", "_waiters").offset;
		private static final long _recursions = vm_struct.entry.get("ObjectMonitor", "_recursions").offset;
	}

	public static class BasicLock
	{
		private static final long _displaced_header = vm_struct.entry.get("BasicLock", "_displaced_header").offset;
	}

	public static class BasicObjectLock
	{
		private static final long _lock = vm_struct.entry.get("BasicObjectLock", "_lock").offset;
		private static final long _obj = vm_struct.entry.get("BasicObjectLock", "_obj").offset;
	}

	public static class ObjectSynchronizer
	{
		private static final long _in_use_list = vm_struct.entry.get("ObjectSynchronizer", "_in_use_list").address;
	}

	public static class MonitorList
	{
		private static final long _head = vm_struct.entry.get("MonitorList", "_head").offset;
	}

	public static class Matcher
	{
		private static final long _regEncode = vm_struct.entry.get("Matcher", "_regEncode").address;
	}

	public static class Node
	{
		private static final long _in = vm_struct.entry.get("Node", "_in").offset;
		private static final long _out = vm_struct.entry.get("Node", "_out").offset;
		private static final long _cnt = vm_struct.entry.get("Node", "_cnt").offset;
		private static final long _max = vm_struct.entry.get("Node", "_max").offset;
		private static final long _outcnt = vm_struct.entry.get("Node", "_outcnt").offset;
		private static final long _outmax = vm_struct.entry.get("Node", "_outmax").offset;
		private static final long _idx = vm_struct.entry.get("Node", "_idx").offset;
		private static final long _class_id = vm_struct.entry.get("Node", "_class_id").offset;
		private static final long _flags = vm_struct.entry.get("Node", "_flags").offset;
	}

	public static class Compile
	{
		private static final long _root = vm_struct.entry.get("Compile", "_root").offset;
		private static final long _unique = vm_struct.entry.get("Compile", "_unique").offset;
		private static final long _entry_bci = vm_struct.entry.get("Compile", "_entry_bci").offset;
		private static final long _top = vm_struct.entry.get("Compile", "_top").offset;
		private static final long _cfg = vm_struct.entry.get("Compile", "_cfg").offset;
		private static final long _regalloc = vm_struct.entry.get("Compile", "_regalloc").offset;
		private static final long _method = vm_struct.entry.get("Compile", "_method").offset;
		private static final long _compile_id = vm_struct.entry.get("Compile", "_compile_id").offset;
		private static final long _options = vm_struct.entry.get("Compile", "_options").offset;
		private static final long _ilt = vm_struct.entry.get("Compile", "_ilt").offset;
	}

	public static class Options
	{
		private static final long _subsume_loads = vm_struct.entry.get("Options", "_subsume_loads").offset;
		private static final long _do_escape_analysis = vm_struct.entry.get("Options", "_do_escape_analysis").offset;
		private static final long _eliminate_boxing = vm_struct.entry.get("Options", "_eliminate_boxing").offset;
		private static final long _do_locks_coarsening = vm_struct.entry.get("Options", "_do_locks_coarsening").offset;
		private static final long _install_code = vm_struct.entry.get("Options", "_install_code").offset;
	}

	public static class InlineTree
	{
		private static final long _caller_jvms = vm_struct.entry.get("InlineTree", "_caller_jvms").offset;
		private static final long _method = vm_struct.entry.get("InlineTree", "_method").offset;
		private static final long _caller_tree = vm_struct.entry.get("InlineTree", "_caller_tree").offset;
		private static final long _subtrees = vm_struct.entry.get("InlineTree", "_subtrees").offset;
	}

	public static class OptoRegPair
	{
		private static final long _first = vm_struct.entry.get("OptoRegPair", "_first").offset;
		private static final long _second = vm_struct.entry.get("OptoRegPair", "_second").offset;
	}

	public static class JVMState
	{
		private static final long _caller = vm_struct.entry.get("JVMState", "_caller").offset;
		private static final long _depth = vm_struct.entry.get("JVMState", "_depth").offset;
		private static final long _locoff = vm_struct.entry.get("JVMState", "_locoff").offset;
		private static final long _stkoff = vm_struct.entry.get("JVMState", "_stkoff").offset;
		private static final long _monoff = vm_struct.entry.get("JVMState", "_monoff").offset;
		private static final long _scloff = vm_struct.entry.get("JVMState", "_scloff").offset;
		private static final long _endoff = vm_struct.entry.get("JVMState", "_endoff").offset;
		private static final long _sp = vm_struct.entry.get("JVMState", "_sp").offset;
		private static final long _bci = vm_struct.entry.get("JVMState", "_bci").offset;
		private static final long _method = vm_struct.entry.get("JVMState", "_method").offset;
		private static final long _map = vm_struct.entry.get("JVMState", "_map").offset;
	}

	public static class SafePointNode
	{
		private static final long _jvms = vm_struct.entry.get("SafePointNode", "_jvms").offset;
	}

	public static class MachSafePointNode
	{
		private static final long _jvms = vm_struct.entry.get("MachSafePointNode", "_jvms").offset;
		private static final long _jvmadj = vm_struct.entry.get("MachSafePointNode", "_jvmadj").offset;
	}

	public static class MachIfNode
	{
		private static final long _prob = vm_struct.entry.get("MachIfNode", "_prob").offset;
		private static final long _fcnt = vm_struct.entry.get("MachIfNode", "_fcnt").offset;
	}

	public static class MachJumpNode
	{
		private static final long _probs = vm_struct.entry.get("MachJumpNode", "_probs").offset;
	}

	public static class CallNode
	{
		private static final long _entry_point = vm_struct.entry.get("CallNode", "_entry_point").offset;
	}

	public static class CallJavaNode
	{
		private static final long _method = vm_struct.entry.get("CallJavaNode", "_method").offset;
	}

	public static class CallRuntimeNode
	{
		private static final long _name = vm_struct.entry.get("CallRuntimeNode", "_name").offset;
	}

	public static class CallStaticJavaNode
	{
		private static final long _name = vm_struct.entry.get("CallStaticJavaNode", "_name").offset;
	}

	public static class MachCallJavaNode
	{
		private static final long _method = vm_struct.entry.get("MachCallJavaNode", "_method").offset;
	}

	public static class MachCallStaticJavaNode
	{
		private static final long _name = vm_struct.entry.get("MachCallStaticJavaNode", "_name").offset;
	}

	public static class MachCallRuntimeNode
	{
		private static final long _name = vm_struct.entry.get("MachCallRuntimeNode", "_name").offset;
	}

	public static class PhaseCFG
	{
		private static final long _number_of_blocks = vm_struct.entry.get("PhaseCFG", "_number_of_blocks").offset;
		private static final long _blocks = vm_struct.entry.get("PhaseCFG", "_blocks").offset;
		private static final long _node_to_block_mapping = vm_struct.entry.get("PhaseCFG", "_node_to_block_mapping").offset;
		private static final long _root_block = vm_struct.entry.get("PhaseCFG", "_root_block").offset;
	}

	public static class PhaseRegAlloc
	{
		private static final long _node_regs = vm_struct.entry.get("PhaseRegAlloc", "_node_regs").offset;
		private static final long _node_regs_max_index = vm_struct.entry.get("PhaseRegAlloc", "_node_regs_max_index").offset;
		private static final long _framesize = vm_struct.entry.get("PhaseRegAlloc", "_framesize").offset;
		private static final long _max_reg = vm_struct.entry.get("PhaseRegAlloc", "_max_reg").offset;
	}

	public static class PhaseChaitin
	{
		private static final long _trip_cnt = vm_struct.entry.get("PhaseChaitin", "_trip_cnt").offset;
		private static final long _alternate = vm_struct.entry.get("PhaseChaitin", "_alternate").offset;
		private static final long _lo_degree = vm_struct.entry.get("PhaseChaitin", "_lo_degree").offset;
		private static final long _lo_stk_degree = vm_struct.entry.get("PhaseChaitin", "_lo_stk_degree").offset;
		private static final long _hi_degree = vm_struct.entry.get("PhaseChaitin", "_hi_degree").offset;
		private static final long _simplified = vm_struct.entry.get("PhaseChaitin", "_simplified").offset;
	}

	public static class Block
	{
		private static final long _nodes = vm_struct.entry.get("Block", "_nodes").offset;
		private static final long _succs = vm_struct.entry.get("Block", "_succs").offset;
		private static final long _num_succs = vm_struct.entry.get("Block", "_num_succs").offset;
		private static final long _pre_order = vm_struct.entry.get("Block", "_pre_order").offset;
		private static final long _dom_depth = vm_struct.entry.get("Block", "_dom_depth").offset;
		private static final long _idom = vm_struct.entry.get("Block", "_idom").offset;
		private static final long _freq = vm_struct.entry.get("Block", "_freq").offset;
	}

	public static class CFGElement
	{
		private static final long _freq = vm_struct.entry.get("CFGElement", "_freq").offset;
	}

	public static class Block_List
	{
		private static final long _cnt = vm_struct.entry.get("Block_List", "_cnt").offset;
	}

	public static class Block_Array
	{
		private static final long _size = vm_struct.entry.get("Block_Array", "_size").offset;
		private static final long _blocks = vm_struct.entry.get("Block_Array", "_blocks").offset;
		private static final long _arena = vm_struct.entry.get("Block_Array", "_arena").offset;
	}

	public static class Node_List
	{
		private static final long _cnt = vm_struct.entry.get("Node_List", "_cnt").offset;
	}

	public static class Node_Array
	{
		private static final long _max = vm_struct.entry.get("Node_Array", "_max").offset;
		private static final long _nodes = vm_struct.entry.get("Node_Array", "_nodes").offset;
		private static final long _a = vm_struct.entry.get("Node_Array", "_a").offset;
	}

	public static class JVMFlag
	{
		private static final long _type = vm_struct.entry.get("JVMFlag", "_type").offset;
		private static final long _name = vm_struct.entry.get("JVMFlag", "_name").offset;
		private static final long _addr = vm_struct.entry.get("JVMFlag", "_addr").offset;
		private static final long _flags = vm_struct.entry.get("JVMFlag", "_flags").offset;
		private static final long flags = vm_struct.entry.get("JVMFlag", "flags").address;
		private static final long numFlags = vm_struct.entry.get("JVMFlag", "numFlags").address;
	}

	public static class Abstract_VM_Version
	{
		private static final long _s_vm_release = vm_struct.entry.get("Abstract_VM_Version", "_s_vm_release").address;
		private static final long _s_internal_vm_info_string = vm_struct.entry.get("Abstract_VM_Version", "_s_internal_vm_info_string").address;
		private static final long _features = vm_struct.entry.get("Abstract_VM_Version", "_features").address;
		private static final long _features_string = vm_struct.entry.get("Abstract_VM_Version", "_features_string").address;
		private static final long _vm_major_version = vm_struct.entry.get("Abstract_VM_Version", "_vm_major_version").address;
		private static final long _vm_minor_version = vm_struct.entry.get("Abstract_VM_Version", "_vm_minor_version").address;
		private static final long _vm_security_version = vm_struct.entry.get("Abstract_VM_Version", "_vm_security_version").address;
		private static final long _vm_build_number = vm_struct.entry.get("Abstract_VM_Version", "_vm_build_number").address;
	}

	public static class JDK_Version
	{
		private static final long _current = vm_struct.entry.get("JDK_Version", "_current").address;
		private static final long _major = vm_struct.entry.get("JDK_Version", "_major").offset;
	}

	public static class JvmtiExport
	{
		private static final long _can_access_local_variables = vm_struct.entry.get("JvmtiExport", "_can_access_local_variables").address;
		private static final long _can_hotswap_or_post_breakpoint = vm_struct.entry.get("JvmtiExport", "_can_hotswap_or_post_breakpoint").address;
		private static final long _can_post_on_exceptions = vm_struct.entry.get("JvmtiExport", "_can_post_on_exceptions").address;
		private static final long _can_walk_any_space = vm_struct.entry.get("JvmtiExport", "_can_walk_any_space").address;
	}

	public static class Arguments
	{
		private static final long _jvm_flags_array = vm_struct.entry.get("Arguments", "_jvm_flags_array").address;
		private static final long _num_jvm_flags = vm_struct.entry.get("Arguments", "_num_jvm_flags").address;
		private static final long _jvm_args_array = vm_struct.entry.get("Arguments", "_jvm_args_array").address;
		private static final long _num_jvm_args = vm_struct.entry.get("Arguments", "_num_jvm_args").address;
		private static final long _java_command = vm_struct.entry.get("Arguments", "_java_command").address;
	}

	public static class Array_int_
	{
		private static final long _length = vm_struct.entry.get("Array<int>", "_length").offset;
		private static final long _data = vm_struct.entry.get("Array<int>", "_data").offset;
	}

	public static class Array_u1_
	{
		private static final long _data = vm_struct.entry.get("Array<u1>", "_data").offset;
	}

	public static class Array_u2_
	{
		private static final long _data = vm_struct.entry.get("Array<u2>", "_data").offset;
	}

	public static class Array_Method_
	{
		private static final long _data = vm_struct.entry.get("Array<Method*>", "_data").offset;
	}

	public static class java_lang_Class
	{
		private static final long _klass_offset = vm_struct.entry.get("java_lang_Class", "_klass_offset").address;
		private static final long _array_klass_offset = vm_struct.entry.get("java_lang_Class", "_array_klass_offset").address;
		private static final long _oop_size_offset = vm_struct.entry.get("java_lang_Class", "_oop_size_offset").address;
		private static final long _static_oop_field_count_offset = vm_struct.entry.get("java_lang_Class", "_static_oop_field_count_offset").address;
	}

	public static class FileMapInfo
	{
		private static final long _header = vm_struct.entry.get("FileMapInfo", "_header").offset;
		private static final long _current_info = vm_struct.entry.get("FileMapInfo", "_current_info").address;
	}

	public static class FileMapHeader
	{
		private static final long _regions_0 = vm_struct.entry.get("FileMapHeader", "_regions[0]").offset;
		private static final long _cloned_vtables_offset = vm_struct.entry.get("FileMapHeader", "_cloned_vtables_offset").offset;
		private static final long _mapped_base_address = vm_struct.entry.get("FileMapHeader", "_mapped_base_address").offset;
	}

	public static class CDSFileMapRegion
	{
		private static final long _mapped_base = vm_struct.entry.get("CDSFileMapRegion", "_mapped_base").offset;
		private static final long _used = vm_struct.entry.get("CDSFileMapRegion", "_used").offset;
	}

	public static class VMError
	{
		private static final long _thread = vm_struct.entry.get("VMError", "_thread").address;
	}

	public static class CompileTask
	{
		private static final long _method = vm_struct.entry.get("CompileTask", "_method").offset;
		private static final long _osr_bci = vm_struct.entry.get("CompileTask", "_osr_bci").offset;
		private static final long _comp_level = vm_struct.entry.get("CompileTask", "_comp_level").offset;
		private static final long _compile_id = vm_struct.entry.get("CompileTask", "_compile_id").offset;
		private static final long _num_inlined_bytecodes = vm_struct.entry.get("CompileTask", "_num_inlined_bytecodes").offset;
		private static final long _next = vm_struct.entry.get("CompileTask", "_next").offset;
		private static final long _prev = vm_struct.entry.get("CompileTask", "_prev").offset;
	}

	public static class vframeArray
	{
		private static final long _original = vm_struct.entry.get("vframeArray", "_original").offset;
		private static final long _caller = vm_struct.entry.get("vframeArray", "_caller").offset;
		private static final long _frames = vm_struct.entry.get("vframeArray", "_frames").offset;
	}

	public static class vframeArrayElement
	{
		private static final long _frame = vm_struct.entry.get("vframeArrayElement", "_frame").offset;
		private static final long _bci = vm_struct.entry.get("vframeArrayElement", "_bci").offset;
		private static final long _method = vm_struct.entry.get("vframeArrayElement", "_method").offset;
	}

	public static class AccessFlags
	{
		private static final long _flags = vm_struct.entry.get("AccessFlags", "_flags").offset;
	}

	public static class elapsedTimer
	{
		private static final long _counter = vm_struct.entry.get("elapsedTimer", "_counter").offset;
		private static final long _active = vm_struct.entry.get("elapsedTimer", "_active").offset;
	}

	public static class InvocationCounter
	{
		private static final long _counter = vm_struct.entry.get("InvocationCounter", "_counter").offset;
	}
}
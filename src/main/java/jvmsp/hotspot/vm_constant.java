package jvmsp.hotspot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jvmsp.shared_object;
import jvmsp.unsafe;
import jvmsp.libso.libjvm;

/**
 * JVM共享常量
 */
public class vm_constant
{
	public static class int_entry
	{
		private static final long gHotSpotVMIntConstants;
		private static final long jvmciHotSpotVMIntConstants;
		private static final long gHotSpotVMIntConstantEntryArrayStride;

		private static final long gHotSpotVMIntConstantEntryNameOffset;
		private static final long gHotSpotVMIntConstantEntryValueOffset;

		static
		{
			gHotSpotVMIntConstants = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMIntConstants"));
			jvmciHotSpotVMIntConstants = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "jvmciHotSpotVMIntConstants"));
			gHotSpotVMIntConstantEntryArrayStride = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMIntConstantEntryArrayStride"));
			gHotSpotVMIntConstantEntryNameOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMIntConstantEntryNameOffset"));
			gHotSpotVMIntConstantEntryValueOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMIntConstantEntryValueOffset"));
		}

		public final String name;// 常量的名称
		public final int value;// 常量值

		private int_entry(long entry_addr)
		{
			this.name = unsafe.read_cstr(entry_addr + gHotSpotVMIntConstantEntryNameOffset);
			this.value = unsafe.read_int(entry_addr + gHotSpotVMIntConstantEntryValueOffset);
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder().append("VMIntConstantEntry [")
					.append("name = ").append(name)
					.append(", value = ").append(value)
					.append(']');
			return sb.toString();
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null)
				return false;
			if (o instanceof int_entry other)
			{
				return value == other.value &&
						Objects.equals(name, other.name);
			}
			else
			{
				return false;
			}
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(
					name,
					value);
		}

		private static final Map<String, int_entry> vm_int_constant_entries = new HashMap<>();

		private static final void collect_entries(Map<String, int_entry> vm_int_constant_entries, long vm_int_constants)
		{
			for (int idx = 0;; ++idx)
			{
				int_entry entry = new int_entry(vm_int_constants + idx * gHotSpotVMIntConstantEntryArrayStride);
				if (entry.name == null)
				{
					break;
				}
				int_entry existed = vm_int_constant_entries.get(entry.name);
				if (existed != null && !entry.equals(existed))
				{
					throw new java.lang.InternalError("conflict VMIntConstantEntry '" + entry + "' and '" + existed + "'");
				}
				else
				{
					vm_int_constant_entries.put(entry.name, entry);
				}
			}
		}

		static
		{
			collect_entries(vm_int_constant_entries, jvmciHotSpotVMIntConstants);
			collect_entries(vm_int_constant_entries, gHotSpotVMIntConstants);
		}

		public static final int_entry find(String name)
		{
			return vm_int_constant_entries.get(name);
		}
	}

	public static class long_entry
	{
		private static final long gHotSpotVMLongConstants;
		private static final long jvmciHotSpotVMLongConstants;
		private static final long gHotSpotVMLongConstantEntryArrayStride;

		private static final long gHotSpotVMLongConstantEntryNameOffset;
		private static final long gHotSpotVMLongConstantEntryValueOffset;

		static
		{
			gHotSpotVMLongConstants = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMLongConstants"));
			jvmciHotSpotVMLongConstants = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "jvmciHotSpotVMLongConstants"));
			gHotSpotVMLongConstantEntryArrayStride = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMLongConstantEntryArrayStride"));
			gHotSpotVMLongConstantEntryNameOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMLongConstantEntryNameOffset"));
			gHotSpotVMLongConstantEntryValueOffset = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "gHotSpotVMLongConstantEntryValueOffset"));
		}

		public final String name;// 常量的名称
		public final long value;// 常量值

		private long_entry(long entry_addr)
		{
			this.name = unsafe.read_cstr(entry_addr + gHotSpotVMLongConstantEntryNameOffset);
			this.value = unsafe.read_long(entry_addr + gHotSpotVMLongConstantEntryValueOffset);
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder().append("VMLongConstantEntry [")
					.append("name = ").append(name)
					.append(", value = ").append(value)
					.append(']');
			return sb.toString();
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null)
				return false;
			if (o instanceof long_entry other)
			{
				return value == other.value &&
						Objects.equals(name, other.name);
			}
			else
			{
				return false;
			}
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(
					name,
					value);
		}

		private static final Map<String, long_entry> vm_long_constant_entries = new HashMap<>();

		private static final void collect_entries(Map<String, long_entry> vm_long_constant_entries, long vm_long_constants)
		{
			for (int idx = 0;; ++idx)
			{
				long_entry entry = new long_entry(vm_long_constants + idx * gHotSpotVMLongConstantEntryArrayStride);
				if (entry.name == null)
				{
					break;
				}
				long_entry existed = vm_long_constant_entries.get(entry.name);
				if (existed != null && !entry.equals(existed))
				{
					throw new java.lang.InternalError("conflict VMLongConstantEntry '" + entry + "' and '" + existed + "'");
				}
				else
				{
					vm_long_constant_entries.put(entry.name, entry);
				}
			}
		}

		static
		{
			collect_entries(vm_long_constant_entries, jvmciHotSpotVMLongConstants);
			collect_entries(vm_long_constant_entries, gHotSpotVMLongConstants);
		}

		public static final long_entry find(String name)
		{
			return vm_long_constant_entries.get(name);
		}
	}

	public static int find_int(String name)
	{
		return int_entry.find(name).value;
	}

	public static int find_int_or(String name, int default_value)
	{
		int_entry _int = int_entry.find(name);
		if (_int != null)
			return _int.value;
		else
			return default_value;
	}

	public static long find_long(String name)
	{
		return long_entry.find(name).value;
	}

	public static long find_long_or(String name, long default_value)
	{
		long_entry _long = long_entry.find(name);
		if (_long != null)
			return _long.value;
		else
			return default_value;
	}

	public static long find(String name)
	{
		int_entry _int = int_entry.find(name);
		if (_int != null)
			return _int.value;
		else
			return find_long(name);
	}

	public static void print_value(String name)
	{
		int_entry _int = int_entry.find(name);
		if (_int != null)
			System.out.println(_int.value);
		long_entry _long = long_entry.find(name);
		if (_long != null)
			System.out.println(_long.value);
	}

	// int类型常量，其中包含部分VMStruct使用的枚举值

	public static final class JDK25
	{
		public static final int G1HeapRegionType_FreeTag = find_int("G1HeapRegionType::FreeTag");
		public static final int G1HeapRegionType_YoungMask = find_int("G1HeapRegionType::YoungMask");
		public static final int G1HeapRegionType_EdenTag = find_int("G1HeapRegionType::EdenTag");
		public static final int G1HeapRegionType_SurvTag = find_int("G1HeapRegionType::SurvTag");
		public static final int G1HeapRegionType_HumongousMask = find_int("G1HeapRegionType::HumongousMask");
		public static final int G1HeapRegionType_StartsHumongousTag = find_int("G1HeapRegionType::StartsHumongousTag");
		public static final int G1HeapRegionType_ContinuesHumongousTag = find_int("G1HeapRegionType::ContinuesHumongousTag");
		public static final int G1HeapRegionType_OldMask = find_int("G1HeapRegionType::OldMask");
		public static final int BarrierSet_G1BarrierSet = find_int("BarrierSet::G1BarrierSet");
		public static final int G1CardTable_g1_young_gen = find_int("G1CardTable::g1_young_gen");
		public static final int Generation_LogOfGenGrain = find_int("Generation::LogOfGenGrain");
		public static final int Generation_GenGrain = find_int("Generation::GenGrain");
		public static final int ZPageType_small = find_int("ZPageType::small");
		public static final int ZPageType_medium = find_int("ZPageType::medium");
		public static final int ZPageType_large = find_int("ZPageType::large");
		public static final int ZPageSizeSmallShift = find_int("ZPageSizeSmallShift");
		public static final int ZPageSizeMediumMaxShift = find_int("ZPageSizeMediumMaxShift");
		public static final int ZObjectAlignmentMediumShift = find_int("ZObjectAlignmentMediumShift");
		public static final int ZObjectAlignmentLargeShift = find_int("ZObjectAlignmentLargeShift");
		public static final int AgeTable_table_size = find_int("AgeTable::table_size");
		public static final int BarrierSet_ModRef = find_int("BarrierSet::ModRef");
		public static final int BarrierSet_CardTableBarrierSet = find_int("BarrierSet::CardTableBarrierSet");
		public static final int BOTConstants_LogBase = find_int("BOTConstants::LogBase");
		public static final int BOTConstants_Base = find_int("BOTConstants::Base");
		public static final int BOTConstants_N_powers = find_int("BOTConstants::N_powers");
		public static final int CardTable_clean_card = find_int("CardTable::clean_card");
		public static final int CardTable_dirty_card = find_int("CardTable::dirty_card");

		public static final int COMPILER2 = find_int("COMPILER2");

		public static final int Deoptimization_Reason_many = find_int("Deoptimization::Reason_many");
		public static final int Deoptimization_Reason_none = find_int("Deoptimization::Reason_none");
		public static final int Deoptimization_Reason_null_check = find_int("Deoptimization::Reason_null_check");
		public static final int Deoptimization_Reason_null_assert = find_int("Deoptimization::Reason_null_assert");
		public static final int Deoptimization_Reason_range_check = find_int("Deoptimization::Reason_range_check");
		public static final int Deoptimization_Reason_class_check = find_int("Deoptimization::Reason_class_check");
		public static final int Deoptimization_Reason_array_check = find_int("Deoptimization::Reason_array_check");
		public static final int Deoptimization_Reason_intrinsic = find_int("Deoptimization::Reason_intrinsic");
		public static final int Deoptimization_Reason_bimorphic = find_int("Deoptimization::Reason_bimorphic");
		public static final int Deoptimization_Reason_profile_predicate = find_int("Deoptimization::Reason_profile_predicate");
		public static final int Deoptimization_Reason_unloaded = find_int("Deoptimization::Reason_unloaded");
		public static final int Deoptimization_Reason_uninitialized = find_int("Deoptimization::Reason_uninitialized");
		public static final int Deoptimization_Reason_initialized = find_int("Deoptimization::Reason_initialized");
		public static final int Deoptimization_Reason_unreached = find_int("Deoptimization::Reason_unreached");
		public static final int Deoptimization_Reason_unhandled = find_int("Deoptimization::Reason_unhandled");
		public static final int Deoptimization_Reason_constraint = find_int("Deoptimization::Reason_constraint");
		public static final int Deoptimization_Reason_div0_check = find_int("Deoptimization::Reason_div0_check");
		public static final int Deoptimization_Reason_age = find_int("Deoptimization::Reason_age");
		public static final int Deoptimization_Reason_predicate = find_int("Deoptimization::Reason_predicate");
		public static final int Deoptimization_Reason_loop_limit_check = find_int("Deoptimization::Reason_loop_limit_check");
		public static final int Deoptimization_Reason_auto_vectorization_check = find_int("Deoptimization::Reason_auto_vectorization_check");
		public static final int Deoptimization_Reason_speculate_class_check = find_int("Deoptimization::Reason_speculate_class_check");
		public static final int Deoptimization_Reason_speculate_null_check = find_int("Deoptimization::Reason_speculate_null_check");
		public static final int Deoptimization_Reason_speculate_null_assert = find_int("Deoptimization::Reason_speculate_null_assert");
		public static final int Deoptimization_Reason_unstable_if = find_int("Deoptimization::Reason_unstable_if");
		public static final int Deoptimization_Reason_unstable_fused_if = find_int("Deoptimization::Reason_unstable_fused_if");
		public static final int Deoptimization_Reason_receiver_constraint = find_int("Deoptimization::Reason_receiver_constraint");
		public static final int Deoptimization_Reason_aliasing = find_int("Deoptimization::Reason_aliasing");
		public static final int Deoptimization_Reason_transfer_to_interpreter = find_int("Deoptimization::Reason_transfer_to_interpreter");
		public static final int Deoptimization_Reason_not_compiled_exception_handler = find_int("Deoptimization::Reason_not_compiled_exception_handler");
		public static final int Deoptimization_Reason_unresolved = find_int("Deoptimization::Reason_unresolved");
		public static final int Deoptimization_Reason_jsr_mismatch = find_int("Deoptimization::Reason_jsr_mismatch");
		public static final int Deoptimization_Reason_tenured = find_int("Deoptimization::Reason_tenured");
		public static final int Deoptimization_Reason_LIMIT = find_int("Deoptimization::Reason_LIMIT");
		public static final int Deoptimization_Reason_RECORDED_LIMIT = find_int("Deoptimization::Reason_RECORDED_LIMIT");
		public static final int Deoptimization_Action_none = find_int("Deoptimization::Action_none");
		public static final int Deoptimization_Action_maybe_recompile = find_int("Deoptimization::Action_maybe_recompile");
		public static final int Deoptimization_Action_reinterpret = find_int("Deoptimization::Action_reinterpret");
		public static final int Deoptimization_Action_make_not_entrant = find_int("Deoptimization::Action_make_not_entrant");
		public static final int Deoptimization_Action_make_not_compilable = find_int("Deoptimization::Action_make_not_compilable");
		public static final int Deoptimization_Action_LIMIT = find_int("Deoptimization::Action_LIMIT");
		public static final int Deoptimization_Unpack_deopt = find_int("Deoptimization::Unpack_deopt");
		public static final int Deoptimization_Unpack_exception = find_int("Deoptimization::Unpack_exception");
		public static final int Deoptimization_Unpack_uncommon_trap = find_int("Deoptimization::Unpack_uncommon_trap");
		public static final int Deoptimization_Unpack_reexecute = find_int("Deoptimization::Unpack_reexecute");
		public static final int Deoptimization_action_bits = find_int("Deoptimization::_action_bits");
		public static final int Deoptimization_reason_bits = find_int("Deoptimization::_reason_bits");
		public static final int Deoptimization_debug_id_bits = find_int("Deoptimization::_debug_id_bits");
		public static final int Deoptimization_action_shift = find_int("Deoptimization::_action_shift");
		public static final int Deoptimization_reason_shift = find_int("Deoptimization::_reason_shift");
		public static final int Deoptimization_debug_id_shift = find_int("Deoptimization::_debug_id_shift");

		public static final int CodeBlobKind_Nmethod = find_int("CodeBlobKind::Nmethod");
		public static final int CodeBlobKind_Buffer = find_int("CodeBlobKind::Buffer");
		public static final int CodeBlobKind_Adapter = find_int("CodeBlobKind::Adapter");
		public static final int CodeBlobKind_Vtable = find_int("CodeBlobKind::Vtable");
		public static final int CodeBlobKind_MHAdapter = find_int("CodeBlobKind::MHAdapter");
		public static final int CodeBlobKind_RuntimeStub = find_int("CodeBlobKind::RuntimeStub");
		public static final int CodeBlobKind_Deoptimization = find_int("CodeBlobKind::Deoptimization");
		public static final int CodeBlobKind_Safepoint = find_int("CodeBlobKind::Safepoint");
		public static final int CodeBlobKind_Exception = find_int("CodeBlobKind::Exception");
		public static final int CodeBlobKind_UncommonTrap = find_int("CodeBlobKind::UncommonTrap");
		public static final int CodeBlobKind_Upcall = find_int("CodeBlobKind::Upcall");
		public static final int CodeBlobKind_Number_Of_Kinds = find_int("CodeBlobKind::Number_Of_Kinds");
		public static final int OopMapValue_type_bits = find_int("OopMapValue::type_bits");
		public static final int OopMapValue_register_bits = find_int("OopMapValue::register_bits");
		public static final int OopMapValue_type_shift = find_int("OopMapValue::type_shift");
		public static final int OopMapValue_register_shift = find_int("OopMapValue::register_shift");
		public static final int OopMapValue_type_mask = find_int("OopMapValue::type_mask");
		public static final int OopMapValue_type_mask_in_place = find_int("OopMapValue::type_mask_in_place");
		public static final int OopMapValue_register_mask = find_int("OopMapValue::register_mask");
		public static final int OopMapValue_register_mask_in_place = find_int("OopMapValue::register_mask_in_place");
		public static final int OopMapValue_unused_value = find_int("OopMapValue::unused_value");
		public static final int OopMapValue_oop_value = find_int("OopMapValue::oop_value");
		public static final int OopMapValue_narrowoop_value = find_int("OopMapValue::narrowoop_value");
		public static final int OopMapValue_callee_saved_value = find_int("OopMapValue::callee_saved_value");
		public static final int OopMapValue_derived_oop_value = find_int("OopMapValue::derived_oop_value");
		public static final int PcDesc_PCDESC_reexecute = find_int("PcDesc::PCDESC_reexecute");
		public static final int PcDesc_PCDESC_is_method_handle_invoke = find_int("PcDesc::PCDESC_is_method_handle_invoke");
		public static final int PcDesc_PCDESC_return_oop = find_int("PcDesc::PCDESC_return_oop");
		public static final int frame_pc_return_offset = find_int("frame::pc_return_offset");
		public static final int vmSymbols_FIRST_SID = find_int("vmSymbols::FIRST_SID");
		public static final int vmSymbols_SID_LIMIT = find_int("vmSymbols::SID_LIMIT");
	}

	public static final int PERFDATA_MAJOR_VERSION = find_int("PERFDATA_MAJOR_VERSION");
	public static final int PERFDATA_MINOR_VERSION = find_int("PERFDATA_MINOR_VERSION");
	public static final int PERFDATA_BIG_ENDIAN = find_int("PERFDATA_BIG_ENDIAN");
	public static final int PERFDATA_LITTLE_ENDIAN = find_int("PERFDATA_LITTLE_ENDIAN");

	public static final int ConstMethodFlags_misc_has_linenumber_table = find_int("ConstMethodFlags::_misc_has_linenumber_table");
	public static final int ConstMethodFlags_misc_has_checked_exceptions = find_int("ConstMethodFlags::_misc_has_checked_exceptions");
	public static final int ConstMethodFlags_misc_has_localvariable_table = find_int("ConstMethodFlags::_misc_has_localvariable_table");
	public static final int ConstMethodFlags_misc_has_exception_table = find_int("ConstMethodFlags::_misc_has_exception_table");
	public static final int ConstMethodFlags_misc_has_generic_signature = find_int("ConstMethodFlags::_misc_has_generic_signature");
	public static final int ConstMethodFlags_misc_has_method_parameters = find_int("ConstMethodFlags::_misc_has_method_parameters");
	public static final int ConstMethodFlags_misc_has_method_annotations = find_int("ConstMethodFlags::_misc_has_method_annotations");
	public static final int ConstMethodFlags_misc_has_parameter_annotations = find_int("ConstMethodFlags::_misc_has_parameter_annotations");
	public static final int ConstMethodFlags_misc_has_default_annotations = find_int("ConstMethodFlags::_misc_has_default_annotations");
	public static final int ConstMethodFlags_misc_has_type_annotations = find_int("ConstMethodFlags::_misc_has_type_annotations");
	public static final int DataLayout_cell_size = find_int("DataLayout::cell_size");
	public static final int DataLayout_no_tag = find_int("DataLayout::no_tag");
	public static final int DataLayout_bit_data_tag = find_int("DataLayout::bit_data_tag");
	public static final int DataLayout_counter_data_tag = find_int("DataLayout::counter_data_tag");
	public static final int DataLayout_jump_data_tag = find_int("DataLayout::jump_data_tag");
	public static final int DataLayout_receiver_type_data_tag = find_int("DataLayout::receiver_type_data_tag");
	public static final int DataLayout_virtual_call_data_tag = find_int("DataLayout::virtual_call_data_tag");
	public static final int DataLayout_ret_data_tag = find_int("DataLayout::ret_data_tag");
	public static final int DataLayout_branch_data_tag = find_int("DataLayout::branch_data_tag");
	public static final int DataLayout_multi_branch_data_tag = find_int("DataLayout::multi_branch_data_tag");
	public static final int DataLayout_arg_info_data_tag = find_int("DataLayout::arg_info_data_tag");
	public static final int DataLayout_call_type_data_tag = find_int("DataLayout::call_type_data_tag");
	public static final int DataLayout_virtual_call_type_data_tag = find_int("DataLayout::virtual_call_type_data_tag");
	public static final int DataLayout_parameters_type_data_tag = find_int("DataLayout::parameters_type_data_tag");
	public static final int DataLayout_speculative_trap_data_tag = find_int("DataLayout::speculative_trap_data_tag");

	public static final int ConstantPool_indy_bsm_offset = find_int("ConstantPool::_indy_bsm_offset");
	public static final int ConstantPool_indy_argc_offset = find_int("ConstantPool::_indy_argc_offset");
	public static final int ConstantPool_indy_argv_offset = find_int("ConstantPool::_indy_argv_offset");

	public static final int FieldInfo_FieldFlags_ff_initialized = find_int("FieldInfo::FieldFlags::_ff_initialized");
	public static final int FieldInfo_FieldFlags_ff_injected = find_int("FieldInfo::FieldFlags::_ff_injected");
	public static final int FieldInfo_FieldFlags_ff_generic = find_int("FieldInfo::FieldFlags::_ff_generic");
	public static final int FieldInfo_FieldFlags_ff_stable = find_int("FieldInfo::FieldFlags::_ff_stable");
	public static final int FieldInfo_FieldFlags_ff_contended = find_int("FieldInfo::FieldFlags::_ff_contended");
	public static final int Location_OFFSET_MASK = find_int("Location::OFFSET_MASK");
	public static final int Location_OFFSET_SHIFT = find_int("Location::OFFSET_SHIFT");
	public static final int Location_TYPE_MASK = find_int("Location::TYPE_MASK");
	public static final int Location_TYPE_SHIFT = find_int("Location::TYPE_SHIFT");
	public static final int Location_WHERE_MASK = find_int("Location::WHERE_MASK");
	public static final int Location_WHERE_SHIFT = find_int("Location::WHERE_SHIFT");
	public static final int Location_normal = find_int("Location::normal");
	public static final int Location_oop = find_int("Location::oop");
	public static final int Location_narrowoop = find_int("Location::narrowoop");
	public static final int Location_int_in_long = find_int("Location::int_in_long");
	public static final int Location_lng = find_int("Location::lng");
	public static final int Location_float_in_dbl = find_int("Location::float_in_dbl");
	public static final int Location_dbl = find_int("Location::dbl");
	public static final int Location_addr = find_int("Location::addr");
	public static final int Location_invalid = find_int("Location::invalid");
	public static final int Location_on_stack = find_int("Location::on_stack");
	public static final int Location_in_register = find_int("Location::in_register");

	public static final int LM_MONITOR = find_int("LM_MONITOR");
	public static final int LM_LEGACY = find_int("LM_LEGACY");
	public static final int LM_LIGHTWEIGHT = find_int("LM_LIGHTWEIGHT");
	public static final int InvocationEntryBci = find_int("InvocationEntryBci");
	public static final int CompLevel_any = find_int("CompLevel_any");
	public static final int CompLevel_all = find_int("CompLevel_all");
	public static final int CompLevel_none = find_int("CompLevel_none");
	public static final int CompLevel_simple = find_int("CompLevel_simple");
	public static final int CompLevel_limited_profile = find_int("CompLevel_limited_profile");
	public static final int CompLevel_full_profile = find_int("CompLevel_full_profile");
	public static final int CompLevel_full_optimization = find_int("CompLevel_full_optimization");

	public static final int ConcreteRegisterImpl_number_of_registers = find_int("ConcreteRegisterImpl::number_of_registers");
	public static final int REG_COUNT = find_int("REG_COUNT");
	public static final int SAVED_ON_ENTRY_REG_COUNT = find_int("SAVED_ON_ENTRY_REG_COUNT");
	public static final int C_SAVED_ON_ENTRY_REG_COUNT = find_int("C_SAVED_ON_ENTRY_REG_COUNT");
	public static final int PerfData_U_None = find_int("PerfData::U_None");
	public static final int PerfData_U_Bytes = find_int("PerfData::U_Bytes");
	public static final int PerfData_U_Ticks = find_int("PerfData::U_Ticks");
	public static final int PerfData_U_Events = find_int("PerfData::U_Events");
	public static final int PerfData_U_String = find_int("PerfData::U_String");
	public static final int PerfData_U_Hertz = find_int("PerfData::U_Hertz");
	public static final int INCLUDE_JVMCI = find_int("INCLUDE_JVMCI");
	public static final int VMRegImpl_stack_slot_size = find_int("VMRegImpl::stack_slot_size");
	public static final int JVMFlagOrigin_DEFAULT = find_int("JVMFlagOrigin::DEFAULT");
	public static final int JVMFlagOrigin_COMMAND_LINE = find_int("JVMFlagOrigin::COMMAND_LINE");
	public static final int JVMFlagOrigin_ENVIRON_VAR = find_int("JVMFlagOrigin::ENVIRON_VAR");
	public static final int JVMFlagOrigin_CONFIG_FILE = find_int("JVMFlagOrigin::CONFIG_FILE");
	public static final int JVMFlagOrigin_MANAGEMENT = find_int("JVMFlagOrigin::MANAGEMENT");
	public static final int JVMFlagOrigin_ERGONOMIC = find_int("JVMFlagOrigin::ERGONOMIC");
	public static final int JVMFlagOrigin_ATTACH_ON_DEMAND = find_int("JVMFlagOrigin::ATTACH_ON_DEMAND");
	public static final int JVMFlagOrigin_INTERNAL = find_int("JVMFlagOrigin::INTERNAL");
	public static final int JVMFlagOrigin_JIMAGE_RESOURCE = find_int("JVMFlagOrigin::JIMAGE_RESOURCE");
	public static final int JVMFlag_VALUE_ORIGIN_MASK = find_int("JVMFlag::VALUE_ORIGIN_MASK");
	public static final int JVMFlag_WAS_SET_ON_COMMAND_LINE = find_int("JVMFlag::WAS_SET_ON_COMMAND_LINE");
	public static final int frame_arg_reg_save_area_bytes = find_int("frame::arg_reg_save_area_bytes");
	public static final int frame_interpreter_frame_sender_sp_offset = find_int("frame::interpreter_frame_sender_sp_offset");
	public static final int frame_interpreter_frame_last_sp_offset = find_int("frame::interpreter_frame_last_sp_offset");
	public static final int frame_entry_frame_call_wrapper_offset = find_int("frame::entry_frame_call_wrapper_offset");

	// long类型常量
	public static final long ZGranuleSizeShift = find_long("ZGranuleSizeShift");
	public static final long ZAddressOffsetShift = find_long("ZAddressOffsetShift");
	public static final long ZAddressOffsetBits = find_long("ZAddressOffsetBits");
	public static final long ZAddressOffsetMask = find_long("ZAddressOffsetMask");
	public static final long ZAddressOffsetMax = find_long("ZAddressOffsetMax");
	public static final long InvocationCounter_count_increment = find_long("InvocationCounter::count_increment");
	public static final long InvocationCounter_count_shift = find_long("InvocationCounter::count_shift");
	public static final long VM_Version_CPU_CX8 = find_long("VM_Version::CPU_CX8");
	public static final long VM_Version_CPU_CMOV = find_long("VM_Version::CPU_CMOV");
	public static final long VM_Version_CPU_FXSR = find_long("VM_Version::CPU_FXSR");
	public static final long VM_Version_CPU_HT = find_long("VM_Version::CPU_HT");
	public static final long VM_Version_CPU_MMX = find_long("VM_Version::CPU_MMX");
	public static final long VM_Version_CPU_3DNOW_PREFETCH = find_long("VM_Version::CPU_3DNOW_PREFETCH");
	public static final long VM_Version_CPU_SSE = find_long("VM_Version::CPU_SSE");
	public static final long VM_Version_CPU_SSE2 = find_long("VM_Version::CPU_SSE2");
	public static final long VM_Version_CPU_SSE3 = find_long("VM_Version::CPU_SSE3");
	public static final long VM_Version_CPU_SSSE3 = find_long("VM_Version::CPU_SSSE3");
	public static final long VM_Version_CPU_SSE4A = find_long("VM_Version::CPU_SSE4A");
	public static final long VM_Version_CPU_SSE4_1 = find_long("VM_Version::CPU_SSE4_1");
	public static final long VM_Version_CPU_SSE4_2 = find_long("VM_Version::CPU_SSE4_2");
	public static final long VM_Version_CPU_POPCNT = find_long("VM_Version::CPU_POPCNT");
	public static final long VM_Version_CPU_LZCNT = find_long("VM_Version::CPU_LZCNT");
	public static final long VM_Version_CPU_TSC = find_long("VM_Version::CPU_TSC");
	public static final long VM_Version_CPU_TSCINV_BIT = find_long("VM_Version::CPU_TSCINV_BIT");
	public static final long VM_Version_CPU_TSCINV = find_long("VM_Version::CPU_TSCINV");
	public static final long VM_Version_CPU_AVX = find_long("VM_Version::CPU_AVX");
	public static final long VM_Version_CPU_AVX2 = find_long("VM_Version::CPU_AVX2");
	public static final long VM_Version_CPU_AES = find_long("VM_Version::CPU_AES");
	public static final long VM_Version_CPU_ERMS = find_long("VM_Version::CPU_ERMS");
	public static final long VM_Version_CPU_CLMUL = find_long("VM_Version::CPU_CLMUL");
	public static final long VM_Version_CPU_BMI1 = find_long("VM_Version::CPU_BMI1");
	public static final long VM_Version_CPU_BMI2 = find_long("VM_Version::CPU_BMI2");
	public static final long VM_Version_CPU_RTM = find_long("VM_Version::CPU_RTM");
	public static final long VM_Version_CPU_ADX = find_long("VM_Version::CPU_ADX");
	public static final long VM_Version_CPU_AVX512F = find_long("VM_Version::CPU_AVX512F");
	public static final long VM_Version_CPU_AVX512DQ = find_long("VM_Version::CPU_AVX512DQ");
	public static final long VM_Version_CPU_AVX512PF = find_long("VM_Version::CPU_AVX512PF");
	public static final long VM_Version_CPU_AVX512ER = find_long("VM_Version::CPU_AVX512ER");
	public static final long VM_Version_CPU_AVX512CD = find_long("VM_Version::CPU_AVX512CD");
	public static final long VM_Version_CPU_AVX512BW = find_long("VM_Version::CPU_AVX512BW");
	public static final long VM_Version_CPU_AVX512VL = find_long("VM_Version::CPU_AVX512VL");
	public static final long VM_Version_CPU_SHA = find_long("VM_Version::CPU_SHA");
	public static final long VM_Version_CPU_FMA = find_long("VM_Version::CPU_FMA");
	public static final long VM_Version_CPU_VZEROUPPER = find_long("VM_Version::CPU_VZEROUPPER");
	public static final long VM_Version_CPU_AVX512_VPOPCNTDQ = find_long("VM_Version::CPU_AVX512_VPOPCNTDQ");
	public static final long VM_Version_CPU_AVX512_VPCLMULQDQ = find_long("VM_Version::CPU_AVX512_VPCLMULQDQ");
	public static final long VM_Version_CPU_AVX512_VAES = find_long("VM_Version::CPU_AVX512_VAES");
	public static final long VM_Version_CPU_AVX512_VNNI = find_long("VM_Version::CPU_AVX512_VNNI");
	public static final long VM_Version_CPU_FLUSH = find_long("VM_Version::CPU_FLUSH");
	public static final long VM_Version_CPU_FLUSHOPT = find_long("VM_Version::CPU_FLUSHOPT");
	public static final long VM_Version_CPU_CLWB = find_long("VM_Version::CPU_CLWB");
	public static final long VM_Version_CPU_AVX512_VBMI2 = find_long("VM_Version::CPU_AVX512_VBMI2");
	public static final long VM_Version_CPU_AVX512_VBMI = find_long("VM_Version::CPU_AVX512_VBMI");
	public static final long VM_Version_CPU_HV = find_long("VM_Version::CPU_HV");
	public static final long VM_Version_CPU_SERIALIZE = find_long("VM_Version::CPU_SERIALIZE");
	public static final long VM_Version_CPU_RDTSCP = find_long("VM_Version::CPU_RDTSCP");
	public static final long VM_Version_CPU_RDPID = find_long("VM_Version::CPU_RDPID");
	public static final long VM_Version_CPU_FSRM = find_long("VM_Version::CPU_FSRM");
	public static final long VM_Version_CPU_GFNI = find_long("VM_Version::CPU_GFNI");
	public static final long VM_Version_CPU_AVX512_BITALG = find_long("VM_Version::CPU_AVX512_BITALG");
	public static final long VM_Version_CPU_F16C = find_long("VM_Version::CPU_F16C");
	public static final long VM_Version_CPU_PKU = find_long("VM_Version::CPU_PKU");
	public static final long VM_Version_CPU_OSPKE = find_long("VM_Version::CPU_OSPKE");
	public static final long VM_Version_CPU_CET_IBT = find_long("VM_Version::CPU_CET_IBT");
	public static final long VM_Version_CPU_CET_SS = find_long("VM_Version::CPU_CET_SS");
	public static final long VM_Version_CPU_AVX512_IFMA = find_long("VM_Version::CPU_AVX512_IFMA");

}

package jvmsp.hotspot.oops;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.MetaspaceObj;
import jvmsp.hotspot.oops.Array.Array_u1;
import jvmsp.hotspot.utilities.globalDefinitions;

/**
 * 方法的不可变数据
 */
public class ConstMethod extends MetaspaceObj
{
	public static final String type_name = "ConstMethod";
	public static final long size = sizeof(type_name);

	private static final long _fingerprint = vm_struct.entry.find(type_name, "_fingerprint").offset;
	private static final long _constants = vm_struct.entry.find(type_name, "_constants").offset;
	private static final long _stackmap_data = vm_struct.entry.find(type_name, "_stackmap_data").offset;
	private static final long _constMethod_size = vm_struct.entry.find(type_name, "_constMethod_size").offset;
	private static final long _flags_flags = vm_struct.entry.find(type_name, "_flags._flags").offset;
	private static final long _code_size = vm_struct.entry.find(type_name, "_code_size").offset;
	private static final long _name_index = vm_struct.entry.find(type_name, "_name_index").offset;
	private static final long _signature_index = vm_struct.entry.find(type_name, "_signature_index").offset;
	private static final long _method_idnum = vm_struct.entry.find(type_name, "_method_idnum").offset;
	private static final long _max_stack = vm_struct.entry.find(type_name, "_max_stack").offset;
	private static final long _max_locals = vm_struct.entry.find(type_name, "_max_locals").offset;
	private static final long _size_of_parameters = vm_struct.entry.find(type_name, "_size_of_parameters").offset;
	private static final long _num_stack_arg_slots = vm_struct.entry.find(type_name, "_num_stack_arg_slots").offset;

	// ConstMethod对象中储存字节码的偏移量
	public static final long codes_offset = size;

	public ConstMethod(long address)
	{
		super(type_name, address);
	}

	public long _fingerprint()
	{
		return super.read_long(_fingerprint);
	}

	public void _write_fingerprint(long fingerprint)
	{
		super.write(_fingerprint, fingerprint);
	}

	public ConstantPool constants()
	{
		return super.read_memory_object_ptr(ConstantPool.class, _constants);
	}

	public void set_constants(ConstantPool cp)
	{
		super.write_memory_object_ptr(_constants, cp);
	}

	public ConstMethodFlags flags()
	{
		return super.read_memory_object(ConstMethodFlags.class, _flags_flags);
	}

	public int name_index()
	{
		return super.read_uint16_t(_name_index);
	}

	public void set_name_index(int name_index)
	{
		super.write_uint16_t(_name_index, name_index);
	}

	public int signature_index()
	{
		return super.read_uint16_t(_signature_index);
	}

	public void set_signature_index(int signature_index)
	{
		super.write_uint16_t(_signature_index, signature_index);
	}

	public Array_u1 stackmap_data()
	{
		return super.read_memory_object_ptr(Array_u1.class, _stackmap_data);
	}

	public void set_stackmap_data(Array_u1 sd)
	{
		super.write_memory_object_ptr(_stackmap_data, sd);
	}

	/**
	 * 包含字节码的总大小
	 * 
	 * @return
	 */
	public long size()
	{
		return super.read_cint(_constMethod_size);
	}

	public void set_constMethod_size(int size)
	{
		super.write_cint(_constMethod_size, size);
	}

	public long constMethod_end()
	{
		return address + size();
	}

	/**
	 * 字节码的大小。<br>
	 * native方法没有字节码，返回值始终是0.<br>
	 * 
	 * @return
	 */
	public int code_size()
	{
		return super.read_uint16_t(_code_size);
	}

	public void set_code_size(int code_size)
	{
		assert (0 <= code_size && code_size <= globalDefinitions.max_method_code_size) : "invalid code size";
		super.write_uint16_t(_code_size, code_size);
	}

	/**
	 * 拷贝指定地址的字节码。<br>
	 * 字节码的大小与原先相同。<br>
	 * 如果字节码有对应的源码行号，则行号紧随字节码的结尾。<br>
	 * 
	 * @param code_addr
	 */
	public void set_code(long code_addr)
	{
		if (code_size() > 0)
		{
			unsafe.memcpy(code_base(), code_addr, code_size());
		}
	}

	public void set_code(long code_addr, int num)
	{
		if (num > 0)
		{
			set_code_size(num);
			unsafe.memcpy(code_base(), code_addr, num);
		}
	}

	public void set_code(ConstMethod cm)
	{
		int num = cm.code_size();
		set_code_size(num);
		unsafe.memcpy(code_base(), cm.code_base(), num);
	}

	/**
	 * 设置字节码。<br>
	 * 拷贝的字节码大小为bytecode的前code_size()个字节。<br>
	 * 
	 * @param bytecode
	 */
	public void set_code(byte[] bytecode)
	{
		int code_size = code_size();
		if (code_size > 0)
		{
			set_code_size(bytecode.length);
			unsafe.memcpy(code_base(), bytecode, 0, code_size);
		}
	}

	/**
	 * 字节码的起始地址
	 * 
	 * @return
	 */
	public long code_base()
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/constMethod.hpp#L420
		// 原定义为address code_base() const { return (address) (this+1); } 即对象起始地址+对象。
		return address + codes_offset;
	}

	/**
	 * 字节码的结束地址
	 * 
	 * @return
	 */
	public long code_end()
	{
		return code_base() + code_size();
	}

	/**
	 * 特定位置是否在字节码区。<br>
	 * BCP为以ConstMethod地址计算的偏移量，BCI为以字节码区起始地址code_base()起算的偏移量。<br.
	 * 
	 * @param bcp
	 * @return
	 */
	public boolean contains(long bcp)
	{
		return code_base() <= bcp && bcp < code_end();
	}

	/**
	 * 读取字节码。<br>
	 * 该字节码中使用的常量池索引为ConstantPoolCache()中的索引，不是ConstantPool中的索引！<br>
	 * 见解释器逻辑 https://github.com/openjdk/jdk/blob/jdk-21%2B35/src/hotspot/cpu/zero/zeroInterpreter_zero.cpp#L699。<br>
	 * 
	 * @return
	 */
	public byte[] code()
	{
		byte[] bytecode = new byte[code_size()];
		if (bytecode.length > 0)
		{
			unsafe.memcpy(bytecode, 0, code_base(), bytecode.length);
		}
		return bytecode;
	}

	public int resolve_cp_cache_idx(byte bytecode, int cp_cache_idx)
	{
		return constants().resolve_cp_cache_idx(bytecode, cp_cache_idx);
	}

	public int method_idnum()
	{
		return super.read_uint16_t(_method_idnum);
	}

	public void set_method_idnum(int method_idnum)
	{
		super.write_uint16_t(_method_idnum, method_idnum);
	}

	public int max_stack()
	{
		return super.read_uint16_t(_max_stack);
	}

	public void set_max_stack(int max_stack)
	{
		super.write_uint16_t(_max_stack, max_stack);
	}

	public int max_locals()
	{
		return super.read_uint16_t(_max_locals);
	}

	public void set_max_locals(int max_locals)
	{
		super.write_uint16_t(_max_locals, max_locals);
	}

	public int size_of_parameters()
	{
		return super.read_uint16_t(_size_of_parameters);
	}

	public void set_size_of_parameters(int size_of_parameters)
	{
		super.write_uint16_t(_size_of_parameters, size_of_parameters);
	}

	public int num_stack_arg_slots()
	{
		return super.read_uint16_t(_num_stack_arg_slots);
	}

	public void set_num_stack_arg_slots(int num_stack_arg_slots)
	{
		super.write_uint16_t(_num_stack_arg_slots, num_stack_arg_slots);
	}

	public boolean has_linenumber_table()
	{
		return flags().has_linenumber_table();
	}

	public void set_has_linenumber_table(boolean value)
	{
		flags().set_has_linenumber_table(value);
	}

	public boolean has_checked_exceptions()
	{
		return flags().has_checked_exceptions();
	}

	public void set_has_checked_exceptions(boolean value)
	{
		flags().set_has_checked_exceptions(value);
	}

	public boolean has_localvariable_table()
	{
		return flags().has_localvariable_table();
	}

	public void set_has_localvariable_table(boolean value)
	{
		flags().set_has_localvariable_table(value);
	}

	public boolean has_exception_table()
	{
		return flags().has_exception_table();
	}

	public void set_has_exception_table(boolean value)
	{
		flags().set_has_exception_table(value);
	}

	public boolean has_generic_signature()
	{
		return flags().has_generic_signature();
	}

	public void set_has_generic_signature(boolean value)
	{
		flags().set_has_generic_signature(value);
	}

	public boolean has_method_parameters()
	{
		return flags().has_method_parameters();
	}

	public void set_has_method_parameters(boolean value)
	{
		flags().set_has_method_parameters(value);
	}

	public boolean is_overpass()
	{
		return flags().is_overpass();
	}

	public void set_is_overpass(boolean value)
	{
		flags().set_is_overpass(value);
	}

	public boolean has_method_annotations()
	{
		return flags().has_method_annotations();
	}

	public void set_has_method_annotations(boolean value)
	{
		flags().set_has_method_annotations(value);
	}

	public boolean has_parameter_annotations()
	{
		return flags().has_parameter_annotations();
	}

	public void set_has_parameter_annotations(boolean value)
	{
		flags().set_has_parameter_annotations(value);
	}

	public boolean has_type_annotations()
	{
		return flags().has_type_annotations();
	}

	public void set_has_type_annotations(boolean value)
	{
		flags().set_has_type_annotations(value);
	}

	public boolean has_default_annotations()
	{
		return flags().has_default_annotations();
	}

	public void set_has_default_annotations(boolean value)
	{
		flags().set_has_default_annotations(value);
	}

	public boolean caller_sensitive()
	{
		return flags().caller_sensitive();
	}

	public void set_caller_sensitive(boolean value)
	{
		flags().set_caller_sensitive(value);
	}

	public boolean is_hidden()
	{
		return flags().is_hidden();
	}

	public void set_is_hidden(boolean value)
	{
		flags().set_is_hidden(value);
	}

	public boolean has_injected_profile()
	{
		return flags().has_injected_profile();
	}

	public void set_has_injected_profile(boolean value)
	{
		flags().set_has_injected_profile(value);
	}

	public boolean intrinsic_candidate()
	{
		return flags().intrinsic_candidate();
	}

	public void set_intrinsic_candidate(boolean value)
	{
		flags().set_intrinsic_candidate(value);
	}

	public boolean reserved_stack_access()
	{
		return flags().reserved_stack_access();
	}

	public void set_reserved_stack_access(boolean value)
	{
		flags().set_reserved_stack_access(value);
	}

	public boolean is_scoped()
	{
		return flags().is_scoped();
	}

	public void set_is_scoped(boolean value)
	{
		flags().set_is_scoped(value);
	}

	public boolean changes_current_thread()
	{
		return flags().changes_current_thread();
	}

	public void set_changes_current_thread(boolean value)
	{
		flags().set_changes_current_thread(value);
	}

	public boolean jvmti_mount_transition()
	{
		return flags().jvmti_mount_transition();
	}

	public void set_jvmti_mount_transition(boolean value)
	{
		flags().set_jvmti_mount_transition(value);
	}

	public boolean deprecated()
	{
		return flags().deprecated();
	}

	public void set_deprecated(boolean value)
	{
		flags().set_deprecated(value);
	}

	public boolean deprecated_for_removal()
	{
		return flags().deprecated_for_removal();
	}

	public void set_deprecated_for_removal(boolean value)
	{
		flags().set_deprecated_for_removal(value);
	}

	public boolean jvmti_hide_events()
	{
		return flags().jvmti_hide_events();
	}

	public void set_jvmti_hide_events(boolean value)
	{
		flags().set_jvmti_hide_events(value);
	}

	/**
	 * 获取压缩后的行号。<br>
	 * 行号就在字节码结尾。<br>
	 * 
	 * @return
	 */
	public long compressed_linenumber_table()
	{
		assert has_linenumber_table() : "called only if table is present";
		return code_end();
	}

	@Override
	public int type()
	{
		return MetaspaceObj.Type.ConstMethodType;
	}

	public String internal_name()
	{
		return "{constMethod}";
	}
}
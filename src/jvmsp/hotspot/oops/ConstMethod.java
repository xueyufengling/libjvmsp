package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.MetaspaceObj;

/**
 * 方法的不可变数据
 */
public class ConstMethod extends MetaspaceObj
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
		super("ConstMethod", address);
	}

	public ConstantPool constants()
	{
		return super.read_memory_object_ptr(ConstantPool.class, _constants);
	}
}
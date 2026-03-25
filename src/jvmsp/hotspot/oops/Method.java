package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.utilities.AccessFlags;

/**
 * 方法的运行时数据
 */
public class Method extends Metadata
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

	@Override
	public final boolean is_method()
	{
		return true;
	}

	public ConstMethod constMethod()
	{
		return super.read_memory_object_ptr(ConstMethod.class, _constMethod);
	}

	public void set_constMethod(ConstMethod xconst)
	{
		super.write_pointer(_constMethod, xconst);
	}

	public ConstantPool constants()
	{
		return constMethod().constants();
	}

	public AccessFlags access_flags()
	{
		return super.read_memory_object(AccessFlags.class, _access_flags);
	}

	public int vtable_index()
	{
		return super.read_int(_vtable_index);
	}

	public void set_vtable_index(int vtidx)
	{
		super.write(_vtable_index, vtidx);
	}

	public MethodFlags flags()
	{
		return super.read_memory_object(MethodFlags.class, _flags);
	}
}
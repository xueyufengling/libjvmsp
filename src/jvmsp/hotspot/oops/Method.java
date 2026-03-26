package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.code.nmethod;
import jvmsp.hotspot.utilities.AccessFlags;

/**
 * 方法的运行时数据
 */
public class Method extends Metadata
{
	private static final long _constMethod = vm_struct.entry.find("Method", "_constMethod").offset;
	private static final long _method_data = vm_struct.entry.find("Method", "_method_data").offset;
	private static final long _method_counters = vm_struct.entry.find("Method", "_method_counters").offset;
	private static final long _vtable_index = vm_struct.entry.find("Method", "_vtable_index").offset;
	private static final long _access_flags = vm_struct.entry.find("Method", "_access_flags").offset;
	private static final long _intrinsic_id = vm_struct.entry.find("Method", "_intrinsic_id").offset;
	private static final long _i2i_entry = vm_struct.entry.find("Method", "_i2i_entry").offset;

	private static final long _from_compiled_entry = vm_struct.entry.find("Method", "_from_compiled_entry").offset;

	// JDK21为CompiledMethod*，JDK25为nmethod*
	private static final long _code = vm_struct.entry.find("Method", "_code").offset;
	private static final long _from_interpreted_entry = vm_struct.entry.find("Method", "_from_interpreted_entry").offset;

	// _flags字段位于u2 _intrinsic_id之前
	private static final long _flags = _intrinsic_id - MethodFlags.MethodFlags.size();

	public static final long size = sizeof("Method");

	public Method(long address)
	{
		super("Method", address);
	}

	@Override
	public final String toString()
	{
		StringBuilder sb = new StringBuilder()
				.append(method_holder().toString())
				.append(name().jstring())
				.append(signature().jstring());
		return sb.toString();
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

	public MethodData method_data()
	{
		return super.read_memory_object_ptr(MethodData.class, _method_data);
	}

	public void set_method_data(MethodData method_data)
	{
		super.write_pointer(_method_data, method_data);
	}

	public MethodCounters method_counters()
	{
		return super.read_memory_object_ptr(MethodCounters.class, _method_counters);
	}

	public void set_method_counters(MethodCounters method_counters)
	{
		super.write_pointer(_method_counters, method_counters);
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

	/**
	 * 本方法的JVM内部标识，用于标记JVM内部方法，例如标识是否是lambda、是否是MethodHandle等。<br>
	 * 所有标识见 https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/classfile/vmIntrinsics.hpp#L108<br>
	 * 对于用户自定义的方法，内部ID始终是0，即不被视为JVM本身的内部方法。<br>
	 * 
	 * @return
	 */
	public short intrinsic_id()
	{
		return super.read_short(_intrinsic_id);
	}

	public void set_intrinsic_id(short intrinsic_id)
	{
		super.write(_intrinsic_id, intrinsic_id);
	}

	public long _from_interpreted_entry()
	{
		return super.read_pointer(_from_interpreted_entry);
	}

	public long interpreter_entry()
	{
		return super.read_pointer(_i2i_entry);
	}

	public void set_interpreter_entry(long interpreter_entry)
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/method.hpp#L429
		super.write_pointer(_i2i_entry, interpreter_entry);
		if (_from_interpreted_entry() != interpreter_entry)
		{
			super.write_pointer(_from_interpreted_entry, interpreter_entry);
		}
	}

	public long _from_compiled_entry()
	{
		return super.read_pointer(_from_compiled_entry);
	}

	public void set_from_compiled_entry(long from_compiled_entry)
	{
		super.write(_from_compiled_entry, from_compiled_entry);
	}

	public MethodFlags flags()
	{
		return super.read_memory_object(MethodFlags.class, _flags);
	}

	// JDK25+
	public nmethod code_nmethod()
	{
		return super.read_memory_object_ptr(nmethod.class, _code);
	}

	public void set_code(nmethod code)
	{
		super.write_pointer(_code, code);
	}

	public int name_index()
	{
		return constMethod().name_index();
	}

	public void set_name_index(int idx)
	{
		constMethod().set_name_index(idx);
	}

	public Symbol name()
	{
		return constants().symbol_at(name_index());
	}

	public int signature_index()
	{
		return constMethod().signature_index();
	}

	public void set_signature_index(int idx)
	{
		constMethod().set_signature_index(idx);
	}

	public Symbol signature()
	{
		return constants().symbol_at(signature_index());
	}

	// 拓展方法
	public InstanceKlass method_holder()
	{
		return constants().pool_holder();
	}

}
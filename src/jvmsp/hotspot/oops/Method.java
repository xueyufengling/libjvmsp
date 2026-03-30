package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_constant;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.code.nmethod;
import jvmsp.hotspot.interpreter.Bytecodes;
import jvmsp.hotspot.utilities.AccessFlags;
import jvmsp.hotspot.utilities.align;
import jvmsp.hotspot.utilities.globalDefinitions;

/**
 * 方法的运行时数据
 */
public class Method extends Metadata
{
	public static final String type_name = "Method";
	public static final long size = sizeof(type_name);

	private static final long _constMethod = vm_struct.entry.find(type_name, "_constMethod").offset;
	private static final long _method_data = vm_struct.entry.find(type_name, "_method_data").offset;
	private static final long _method_counters = vm_struct.entry.find(type_name, "_method_counters").offset;
	private static final long _vtable_index = vm_struct.entry.find(type_name, "_vtable_index").offset;
	private static final long _access_flags = vm_struct.entry.find(type_name, "_access_flags").offset;
	// _flags字段位于u2 _intrinsic_id之前
	private static final long _flags = vm_struct.entry.find(type_name, "_flags._status").offset;
	private static final long _intrinsic_id = vm_struct.entry.find(type_name, "_intrinsic_id").offset;
	private static final long _i2i_entry = vm_struct.entry.find(type_name, "_i2i_entry").offset;

	private static final long _from_compiled_entry = vm_struct.entry.find(type_name, "_from_compiled_entry").offset;

	// JDK21为CompiledMethod*，JDK25为nmethod*
	private static final long _code = vm_struct.entry.find(type_name, "_code").offset;
	private static final long _from_interpreted_entry = vm_struct.entry.find(type_name, "_from_interpreted_entry").offset;

	public static abstract class VtableIndexFlag
	{
		public static final int itable_index_max = -10; // first itable index, growing downward
		public static final int pending_itable_index = -9; // itable index will be assigned
		public static final int invalid_vtable_index = vm_constant.find_int("Method::invalid_vtable_index");// -4; // distinct from any valid vtable index
		public static final int garbage_vtable_index = -3; // not yet linked; no vtable layout yet
		public static final int nonvirtual_vtable_index = vm_constant.find_int("Method::nonvirtual_vtable_index");// -2
	}

	public static final int extra_stack_entries_for_jsr292 = vm_constant.find_int("Method::extra_stack_entries_for_jsr292");// 1

	public Method(long address)
	{
		super(type_name, address);
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

	public void print_code()
	{
		System.out.println(method_holder().toString() + name() + signature());
		constMethod().print_code();
	}

	public void set_constMethod(ConstMethod xconst)
	{
		super.write_memory_object_ptr(_constMethod, xconst);
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
		super.write_memory_object_ptr(_method_data, method_data);
	}

	public MethodCounters method_counters()
	{
		return super.read_memory_object_ptr(MethodCounters.class, _method_counters);
	}

	public void set_method_counters(MethodCounters method_counters)
	{
		super.write_memory_object_ptr(_method_counters, method_counters);
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

	public boolean is_native()
	{
		return access_flags().is_native();
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
		super.write_memory_object_ptr(_code, code);
	}

	/**
	 * 该方法的断点BCI->对应字节码
	 * 
	 * @param bci
	 * @return
	 */
	public byte orig_bytecode_at(int bci)
	{
		BreakpointInfo bp = method_holder().breakpoints();
		for (; bp != null; bp = bp.next())
		{
			if (bp.match(this, bci))
			{
				return bp.orig_bytecode();
			}
		}
		return Bytecodes.Code._shouldnotreachhere;// 该断点没有对应本方法的任何字节码
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

	public void set_name(Symbol name)
	{
		constants().symbol_at_put(name_index(), name);
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

	public void set_signature(Symbol signature)
	{
		constants().symbol_at_put(signature_index(), signature);
	}

	/**
	 * native方法的函数地址
	 * 
	 * @return
	 */
	public long native_function_addr()
	{
		assert is_native() : "must be native";
		return address + size;
	}

	/**
	 * native方法的签名处理
	 * 
	 * @return
	 */
	public long signature_handler_addr()
	{
		return native_function_addr() + globalDefinitions.BytesPerWord;
	}

	// 拓展方法
	public InstanceKlass method_holder()
	{
		return constants().pool_holder();
	}

	public int method_size()
	{
		return (int) (size / globalDefinitions.BytesPerWord + (is_native() ? 2 : 0));
	}

	public static final int header_size()
	{
		return (int) (align.align_up((int) size, globalDefinitions.BytesPerWord) / globalDefinitions.BytesPerWord);
	}

	@Override
	public long size()
	{
		return method_size();
	}

	@Override
	public String internal_name()
	{
		return "{method}";
	}

	@Override
	public int meta_type()
	{
		return Type.MethodType;
	}

}
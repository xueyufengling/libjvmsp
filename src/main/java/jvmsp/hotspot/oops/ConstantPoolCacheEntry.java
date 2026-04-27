package jvmsp.hotspot.oops;

import jvmsp.memory;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.interpreter.Bytecodes.Code;
import jvmsp.hotspot.utilities.globalDefinitions;

@Deprecated(since = "jdk-25", forRemoval = true) // 本库不会移除，此标记仅仅是表明JDK已经移除
public class ConstantPoolCacheEntry extends vm_struct
{
	public static final String type_name = "ConstantPoolCacheEntry";
	public static final long size = sizeof(type_name);

	private static final long _indices = vm_struct.entry.find("ConstantPoolCacheEntry", "_indices").offset;// constant pool index & rewrite bytecodes
	private static final long _f1 = vm_struct.entry.find("ConstantPoolCacheEntry", "_f1").offset;// entry specific metadata field
	private static final long _f2 = vm_struct.entry.find("ConstantPoolCacheEntry", "_f2").offset; // entry specific int/metadata field
	private static final long _flags = vm_struct.entry.find("ConstantPoolCacheEntry", "_flags").offset;

	// https://github.com/openjdk/jdk/blob/jdk-21%2B35/src/hotspot/share/oops/cpCache.hpp#L176

	// high order bits are the TosState corresponding to field type or method return type
	public static final int tos_state_bits = 4;
	public static final long tos_state_mask = memory.right_bits_mask(tos_state_bits);
	public static final int tos_state_shift = globalDefinitions.BitsPerInt - tos_state_bits; // see verify_tos_state_shift below
	// misc. option bits; can be any bit position in [16..27]

	public static final int is_field_entry_shift = 26; // (F) is it a field or a method?
	public static final int has_local_signature_shift = 25; // (S) does the call site have a per-site signature (sig-poly methods)?
	public static final int has_appendix_shift = 24; // (A) does the call site have an appendix argument?
	public static final int is_forced_virtual_shift = 23; // (I) is the interface reference forced to virtual mode?
	public static final int is_final_shift = 22; // (f) is the field or method final?
	public static final int is_volatile_shift = 21; // (v) is the field volatile?
	public static final int is_vfinal_shift = 20; // (vf) did the call resolve to a final method?
	public static final int indy_resolution_failed_shift = 19; // (indy_rf) did call site specifier resolution fail ?
	// low order bits give field index (for FieldInfo) or method parameter size:
	public static final int field_index_bits = 16;
	public static final long field_index_mask = memory.right_bits_mask(field_index_bits);

	public static final int parameter_size_bits = 8; // subset of field_index_mask, range is 0..255
	public static final long parameter_size_mask = memory.right_bits_mask(parameter_size_bits);

	public static final long option_bits_mask = ~(((~0) << tos_state_shift) | (field_index_mask | parameter_size_mask));

	public static final int cp_index_bits = 2 * globalDefinitions.BitsPerByte;
	public static final long cp_index_mask = memory.right_bits_mask(cp_index_bits);// 右侧2字节位cp_index
	public static final int bytecode_1_shift = cp_index_bits;
	public static final long bytecode_1_mask = memory.right_bits_mask(globalDefinitions.BitsPerByte); // == (u1)0xFF
	public static final int bytecode_2_shift = cp_index_bits + globalDefinitions.BitsPerByte;
	public static final long bytecode_2_mask = memory.right_bits_mask(globalDefinitions.BitsPerByte); // == (u1)0xFF

	public ConstantPoolCacheEntry(long address)
	{
		super(type_name, address);
	}

	public long indices()
	{
		return super.read_ptr(_indices);
	}

	public void set_indices(long indices)
	{
		super.write_ptr(_indices, indices);
	}

	public long _f1()
	{
		return super.read_ptr(_f1);
	}

	public Metadata f1()
	{
		return super.read_memory_object_ptr(Metadata.class, _f1);
	}

	public void set_f1(long f1)
	{
		super.write_ptr(_f1, f1);
	}

	public long f2()
	{
		return super.read_ptr(_f2);
	}

	public void set_f2(long f2)
	{
		super.write_ptr(_f2, f2);
	}

	public long flags()
	{
		return super.read_ptr(_flags);
	}

	public void set_flags(long flags)
	{
		super.write_ptr(_flags, flags);
	}

	public boolean is_f1_null()
	{
		return _f1() == 0;
	}

	public boolean has_appendix()
	{
		return !is_f1_null() && memory.flag_bit(flags(), 1 << has_appendix_shift);
	}

	public boolean has_local_signature()
	{
		return !is_f1_null() && memory.flag_bit(flags(), 1 << has_local_signature_shift);
	}

	/**
	 * 获取该条目在ConstantPool中的索引，即使未决议也能获取。<br>
	 * 
	 * @return
	 */
	public int constant_pool_index()
	{
		return (int) (indices() & cp_index_mask);
	}

	public byte bytecode_1()
	{
		return (byte) ((indices() >> bytecode_1_shift) & bytecode_1_mask);
	}

	public byte bytecode_2()
	{
		return (byte) ((indices() >> bytecode_2_shift) & bytecode_2_mask);
	}

	public static int bytecode_number(byte code)
	{
		switch (code)
		{
		case Code._getstatic:
		case Code._getfield:
		case Code._invokespecial:
		case Code._invokestatic:
		case Code._invokehandle:
		case Code._invokedynamic:
		case Code._invokeinterface:
			return 1;
		case Code._putstatic:
		case Code._putfield:
		case Code._invokevirtual:
			return 2;
		default:
			return -1;
		}
	}

	/**
	 * 该条目是否已经被决议。<br>
	 * 只有决议后的条目bytecode_1()和bytecode_2()才有值。<br>
	 * 调用目标方法可以触发解释器对其决议。<br>
	 * 
	 * @param code
	 * @return
	 */
	public boolean is_resolved(byte code)
	{
		switch (bytecode_number(code))
		{
		case 1:
			return (bytecode_1() == code);
		case 2:
			return (bytecode_2() == code);
		default:
			return false;
		}
	}

}
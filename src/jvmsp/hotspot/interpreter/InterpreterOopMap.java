package jvmsp.hotspot.interpreter;

import jvmsp.type.cxx_type;
import jvmsp.hotspot.memory.ResourceObj;
import jvmsp.hotspot.utilities.globalDefinitions;

public class InterpreterOopMap extends ResourceObj
{
	public static final int N = 4; // the number of words reserved
	// for inlined mask storage
	public static final int small_mask_limit = N * globalDefinitions.BitsPerWord; // the maximum number of bits
	// available for small masks,
	// small_mask_limit can be set to 0
	// for testing bit_mask allocation

	public static final int bits_per_entry = 2;
	public static final int dead_bit_number = 1;
	public static final int oop_bit_number = 0;

	public static final String type_name = "InterpreterOopMap";

	public static final cxx_type InterpreterOopMap = cxx_type.define(type_name)
			.decl_field("_method", cxx_type.pvoid)
			.decl_field("_mask_size", cxx_type._int)
			.decl_field("_expression_stack_size", cxx_type._int)
			.decl_field("_bci", cxx_type.unsigned_short)
			.decl_field("_num_oops", cxx_type._int)
			.decl_field("_bit_mask", cxx_type.intptr_t.array(N))
			.resolve();

	public static final long size = InterpreterOopMap.size();

	private static final long _method = InterpreterOopMap.field("_method").offset();
	private static final long _mask_size = InterpreterOopMap.field("_mask_size").offset();
	private static final long _expression_stack_size = InterpreterOopMap.field("_expression_stack_size").offset();
	private static final long _bci = InterpreterOopMap.field("_bci").offset();
	private static final long _num_oops = InterpreterOopMap.field("_num_oops").offset();
	private static final long _bit_mask = InterpreterOopMap.field("_bit_mask").offset();

	public InterpreterOopMap(String name, long address)
	{
		super(name, address);
	}

	public InterpreterOopMap(long address)
	{
		this(type_name, address);
	}
}

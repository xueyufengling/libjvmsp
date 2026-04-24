package jvmsp;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;

import jvmsp.type.cxx_type.function_signature;
import jvmsp.type.cxx_type.pointer;
import jvmsp.libso.libllvmmci;

/**
 * 汇编操作
 */
public class asm
{
	/**
	 * 汇编成的.o
	 */
	public static class o
	{
		private long o;

		o(long o)
		{
			this.o = o;
		}

		public final void free()
		{
			libllvmmci.free_array(o);
			this.o = 0;
		}
	}

	public static class assembler
	{
		private final ArrayList<pointer> src_cache = new ArrayList<>();

		private long assembler;

		private assembler(long assembler)
		{
			this.assembler = assembler;
		}

		public final void add_src(String src)
		{
			src_cache.add(libllvmmci.assembler_add_src(assembler, src));
		}

		public final o assemble(boolean PIC, boolean LargeCodeModel, int syntax)
		{
			return new o(libllvmmci.assemble_unit(assembler, PIC, LargeCodeModel, syntax));
		}

		public o assemble()
		{
			return assemble(true, false, libllvmmci.assembly_syntax.ASM_SYNTAX_ATT);
		}

		public final void new_unit()
		{
			for (pointer src : src_cache)
			{
				src.delete();// 释放源码缓存
			}
			src_cache.clear();
			libllvmmci.assembler_clear_unit(assembler);
		}
	}

	public static final assembler host_assembler = new assembler(libllvmmci.host_assembler);

	public static class dynamic_lib_target
	{
		private long target;
		private String lib_name;

		dynamic_lib_target(long target, String lib_name)
		{
			this.target = target;
			this.lib_name = lib_name;
		}

		public final String lib_name()
		{
			return lib_name;
		}

		public final void free()
		{
			libllvmmci.free_dynamic_lib(target);
			this.target = 0;
		}

		public final void add_o(o o)
		{
			libllvmmci.dynamic_link_o(target, o.o);
		}

		public final long lookup(String sym_name)
		{
			return libllvmmci.dynamic_symbol_lookup(target, sym_name);
		}

		public final MethodHandle lookup(abi cabi, function_signature signature)
		{
			return libllvmmci.dynamic_symbol_lookup(target, cabi, signature);
		}

		public final MethodHandle lookup(function_signature signature)
		{
			return lookup(abi.host, signature);
		}
	}

	public static class dynamic_linker
	{
		private long linker;

		private dynamic_linker(long linker)
		{
			this.linker = linker;
		}

		public final dynamic_lib_target link_target(String lib_name)
		{
			return new dynamic_lib_target(libllvmmci.dynamic_link_target(linker, lib_name), lib_name);
		}
	}

	public static final dynamic_linker global_dynamic_linker = new dynamic_linker(libllvmmci.global_dynamic_linker);
}

package jvmsp.hotspot.oops;

import jvmsp.memory;
import jvmsp.hotspot.vm_struct;
import jvmsp.type.cxx_type;

public class ConstMethodFlags extends vm_struct
{
	public static final String type_name = "ConstMethodFlags";

	public static final cxx_type ConstMethodFlags = cxx_type.define(type_name)
			.decl_field("_flags", cxx_type.uint32_t)
			.resolve();

	public static final long size = ConstMethodFlags.size();

	private static final long _flags = ConstMethodFlags.field("_flags").offset();

	// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/constMethodFlags.hpp

	public static final int has_linenumber_table = 1 << 0;// 是否有源代码行号，在打印堆栈时有行号则会打印行号
	public static final int has_checked_exceptions = 1 << 1;// 是否声明了throws抛出异常
	public static final int has_localvariable_table = 1 << 2;// 是否有局部变量表
	public static final int has_exception_table = 1 << 3;// 是否有异常捕获列表
	public static final int has_generic_signature = 1 << 4;// 方法签名中是否有泛型
	public static final int has_method_parameters = 1 << 5;
	public static final int is_overpass = 1 << 6;
	public static final int has_method_annotations = 1 << 7;// 方法本体是否有注解
	public static final int has_parameter_annotations = 1 << 8;// 方法参数是否有注解
	public static final int has_type_annotations = 1 << 9;
	public static final int has_default_annotations = 1 << 10;
	public static final int caller_sensitive = 1 << 11;// @CallerSensitive注解标记
	public static final int is_hidden = 1 << 12;
	public static final int has_injected_profile = 1 << 13;
	public static final int intrinsic_candidate = 1 << 14;// @IntrinsicCandidate注解标记
	public static final int reserved_stack_access = 1 << 15;
	public static final int is_scoped = 1 << 16;
	public static final int changes_current_thread = 1 << 17;
	public static final int jvmti_mount_transition = 1 << 18;
	public static final int deprecated = 1 << 19;
	public static final int deprecated_for_removal = 1 << 20;
	public static final int jvmti_hide_events = 1 << 21;

	public ConstMethodFlags(long address)
	{
		super(type_name, address);
	}

	public String toString()
	{
		return memory.bits_str(_flags());
	}

	public int _flags()
	{
		return super.read_int(_flags);
	}

	public void set_flags(int flags)
	{
		super.write(_flags, flags);
	}

	public boolean has_linenumber_table()
	{
		return memory.flag_bit(_flags(), has_linenumber_table);
	}

	public void set_has_linenumber_table(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_linenumber_table, value));
	}

	public boolean has_checked_exceptions()
	{
		return memory.flag_bit(_flags(), has_checked_exceptions);
	}

	public void set_has_checked_exceptions(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_checked_exceptions, value));
	}

	public boolean has_localvariable_table()
	{
		return memory.flag_bit(_flags(), has_localvariable_table);
	}

	public void set_has_localvariable_table(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_localvariable_table, value));
	}

	public boolean has_exception_table()
	{
		return memory.flag_bit(_flags(), has_exception_table);
	}

	public void set_has_exception_table(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_exception_table, value));
	}

	public boolean has_generic_signature()
	{
		return memory.flag_bit(_flags(), has_generic_signature);
	}

	public void set_has_generic_signature(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_generic_signature, value));
	}

	public boolean has_method_parameters()
	{
		return memory.flag_bit(_flags(), has_method_parameters);
	}

	public void set_has_method_parameters(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_method_parameters, value));
	}

	public boolean is_overpass()
	{
		return memory.flag_bit(_flags(), is_overpass);
	}

	public void set_is_overpass(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), is_overpass, value));
	}

	public boolean has_method_annotations()
	{
		return memory.flag_bit(_flags(), has_method_annotations);
	}

	public void set_has_method_annotations(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_method_annotations, value));
	}

	public boolean has_parameter_annotations()
	{
		return memory.flag_bit(_flags(), has_parameter_annotations);
	}

	public void set_has_parameter_annotations(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_parameter_annotations, value));
	}

	public boolean has_type_annotations()
	{
		return memory.flag_bit(_flags(), has_type_annotations);
	}

	public void set_has_type_annotations(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_type_annotations, value));
	}

	public boolean has_default_annotations()
	{
		return memory.flag_bit(_flags(), has_default_annotations);
	}

	public void set_has_default_annotations(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_default_annotations, value));
	}

	public boolean caller_sensitive()
	{
		return memory.flag_bit(_flags(), caller_sensitive);
	}

	public void set_caller_sensitive(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), caller_sensitive, value));
	}

	public boolean is_hidden()
	{
		return memory.flag_bit(_flags(), is_hidden);
	}

	public void set_is_hidden(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), is_hidden, value));
	}

	public boolean has_injected_profile()
	{
		return memory.flag_bit(_flags(), has_injected_profile);
	}

	public void set_has_injected_profile(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), has_injected_profile, value));
	}

	public boolean intrinsic_candidate()
	{
		return memory.flag_bit(_flags(), intrinsic_candidate);
	}

	public void set_intrinsic_candidate(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), intrinsic_candidate, value));
	}

	public boolean reserved_stack_access()
	{
		return memory.flag_bit(_flags(), reserved_stack_access);
	}

	public void set_reserved_stack_access(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), reserved_stack_access, value));
	}

	public boolean is_scoped()
	{
		return memory.flag_bit(_flags(), is_scoped);
	}

	public void set_is_scoped(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), is_scoped, value));
	}

	public boolean changes_current_thread()
	{
		return memory.flag_bit(_flags(), changes_current_thread);
	}

	public void set_changes_current_thread(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), changes_current_thread, value));
	}

	public boolean jvmti_mount_transition()
	{
		return memory.flag_bit(_flags(), jvmti_mount_transition);
	}

	public void set_jvmti_mount_transition(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), jvmti_mount_transition, value));
	}

	public boolean deprecated()
	{
		return memory.flag_bit(_flags(), deprecated);
	}

	public void set_deprecated(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), deprecated, value));
	}

	public boolean deprecated_for_removal()
	{
		return memory.flag_bit(_flags(), deprecated_for_removal);
	}

	public void set_deprecated_for_removal(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), deprecated_for_removal, value));
	}

	public boolean jvmti_hide_events()
	{
		return memory.flag_bit(_flags(), jvmti_hide_events);
	}

	public void set_jvmti_hide_events(boolean value)
	{
		set_flags(memory.set_flag_bit(_flags(), jvmti_hide_events, value));
	}
}

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

	// 静态标志方法

	public static final boolean has_linenumber_table(int flags)
	{
		return memory.flag_bit(flags, has_linenumber_table);
	}

	public static final int set_has_linenumber_table(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_linenumber_table, value);
	}

	public static final boolean has_checked_exceptions(int flags)
	{
		return memory.flag_bit(flags, has_checked_exceptions);
	}

	public static final int set_has_checked_exceptions(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_checked_exceptions, value);
	}

	public static final boolean has_localvariable_table(int flags)
	{
		return memory.flag_bit(flags, has_localvariable_table);
	}

	public static final int set_has_localvariable_table(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_localvariable_table, value);
	}

	public static final boolean has_exception_table(int flags)
	{
		return memory.flag_bit(flags, has_exception_table);
	}

	public static final int set_has_exception_table(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_exception_table, value);
	}

	public static final boolean has_generic_signature(int flags)
	{
		return memory.flag_bit(flags, has_generic_signature);
	}

	public static final int set_has_generic_signature(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_generic_signature, value);
	}

	public static final boolean has_method_parameters(int flags)
	{
		return memory.flag_bit(flags, has_method_parameters);
	}

	public static final int set_has_method_parameters(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_method_parameters, value);
	}

	public static final boolean is_overpass(int flags)
	{
		return memory.flag_bit(flags, is_overpass);
	}

	public static final int set_is_overpass(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_overpass, value);
	}

	public static final boolean has_method_annotations(int flags)
	{
		return memory.flag_bit(flags, has_method_annotations);
	}

	public static final int set_has_method_annotations(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_method_annotations, value);
	}

	public static final boolean has_parameter_annotations(int flags)
	{
		return memory.flag_bit(flags, has_parameter_annotations);
	}

	public static final int set_has_parameter_annotations(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_parameter_annotations, value);
	}

	public static final boolean has_type_annotations(int flags)
	{
		return memory.flag_bit(flags, has_type_annotations);
	}

	public static final int set_has_type_annotations(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_type_annotations, value);
	}

	public static final boolean has_default_annotations(int flags)
	{
		return memory.flag_bit(flags, has_default_annotations);
	}

	public static final int set_has_default_annotations(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_default_annotations, value);
	}

	public static final boolean caller_sensitive(int flags)
	{
		return memory.flag_bit(flags, caller_sensitive);
	}

	public static final int set_caller_sensitive(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, caller_sensitive, value);
	}

	public static final boolean is_hidden(int flags)
	{
		return memory.flag_bit(flags, is_hidden);
	}

	public static final int set_is_hidden(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_hidden, value);
	}

	public static final boolean has_injected_profile(int flags)
	{
		return memory.flag_bit(flags, has_injected_profile);
	}

	public static final int set_has_injected_profile(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, has_injected_profile, value);
	}

	public static final boolean intrinsic_candidate(int flags)
	{
		return memory.flag_bit(flags, intrinsic_candidate);
	}

	public static final int set_intrinsic_candidate(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, intrinsic_candidate, value);
	}

	public static final boolean reserved_stack_access(int flags)
	{
		return memory.flag_bit(flags, reserved_stack_access);
	}

	public static final int set_reserved_stack_access(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, reserved_stack_access, value);
	}

	public static final boolean is_scoped(int flags)
	{
		return memory.flag_bit(flags, is_scoped);
	}

	public static final int set_is_scoped(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, is_scoped, value);
	}

	public static final boolean changes_current_thread(int flags)
	{
		return memory.flag_bit(flags, changes_current_thread);
	}

	public static final int set_changes_current_thread(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, changes_current_thread, value);
	}

	public static final boolean jvmti_mount_transition(int flags)
	{
		return memory.flag_bit(flags, jvmti_mount_transition);
	}

	public static final int set_jvmti_mount_transition(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, jvmti_mount_transition, value);
	}

	public static final boolean deprecated(int flags)
	{
		return memory.flag_bit(flags, deprecated);
	}

	public static final int set_deprecated(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, deprecated, value);
	}

	public static final boolean deprecated_for_removal(int flags)
	{
		return memory.flag_bit(flags, deprecated_for_removal);
	}

	public static final int set_deprecated_for_removal(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, deprecated_for_removal, value);
	}

	public static final boolean jvmti_hide_events(int flags)
	{
		return memory.flag_bit(flags, jvmti_hide_events);
	}

	public static final int set_jvmti_hide_events(int flags, boolean value)
	{
		return memory.set_flag_bit(flags, jvmti_hide_events, value);
	}

	// 成员方法

	public boolean has_linenumber_table()
	{
		return has_linenumber_table(_flags());
	}

	public void set_has_linenumber_table(boolean value)
	{
		set_flags(set_has_linenumber_table(_flags(), value));
	}

	public boolean has_checked_exceptions()
	{
		return has_checked_exceptions(_flags());
	}

	public void set_has_checked_exceptions(boolean value)
	{
		set_flags(set_has_checked_exceptions(_flags(), value));
	}

	public boolean has_localvariable_table()
	{
		return has_localvariable_table(_flags());
	}

	public void set_has_localvariable_table(boolean value)
	{
		set_flags(set_has_localvariable_table(_flags(), value));
	}

	public boolean has_exception_table()
	{
		return has_exception_table(_flags());
	}

	public void set_has_exception_table(boolean value)
	{
		set_flags(set_has_exception_table(_flags(), value));
	}

	public boolean has_generic_signature()
	{
		return has_generic_signature(_flags());
	}

	public void set_has_generic_signature(boolean value)
	{
		set_flags(set_has_generic_signature(_flags(), value));
	}

	public boolean has_method_parameters()
	{
		return has_method_parameters(_flags());
	}

	public void set_has_method_parameters(boolean value)
	{
		set_flags(set_has_method_parameters(_flags(), value));
	}

	public boolean is_overpass()
	{
		return is_overpass(_flags());
	}

	public void set_is_overpass(boolean value)
	{
		set_flags(set_is_overpass(_flags(), value));
	}

	public boolean has_method_annotations()
	{
		return has_method_annotations(_flags());
	}

	public void set_has_method_annotations(boolean value)
	{
		set_flags(set_has_method_annotations(_flags(), value));
	}

	public boolean has_parameter_annotations()
	{
		return has_parameter_annotations(_flags());
	}

	public void set_has_parameter_annotations(boolean value)
	{
		set_flags(set_has_parameter_annotations(_flags(), value));
	}

	public boolean has_type_annotations()
	{
		return has_type_annotations(_flags());
	}

	public void set_has_type_annotations(boolean value)
	{
		set_flags(set_has_type_annotations(_flags(), value));
	}

	public boolean has_default_annotations()
	{
		return has_default_annotations(_flags());
	}

	public void set_has_default_annotations(boolean value)
	{
		set_flags(set_has_default_annotations(_flags(), value));
	}

	public boolean caller_sensitive()
	{
		return caller_sensitive(_flags());
	}

	public void set_caller_sensitive(boolean value)
	{
		set_flags(set_caller_sensitive(_flags(), value));
	}

	public boolean is_hidden()
	{
		return is_hidden(_flags());
	}

	public void set_is_hidden(boolean value)
	{
		set_flags(set_is_hidden(_flags(), value));
	}

	public boolean has_injected_profile()
	{
		return has_injected_profile(_flags());
	}

	public void set_has_injected_profile(boolean value)
	{
		set_flags(set_has_injected_profile(_flags(), value));
	}

	public boolean intrinsic_candidate()
	{
		return intrinsic_candidate(_flags());
	}

	public void set_intrinsic_candidate(boolean value)
	{
		set_flags(set_intrinsic_candidate(_flags(), value));
	}

	public boolean reserved_stack_access()
	{
		return reserved_stack_access(_flags());
	}

	public void set_reserved_stack_access(boolean value)
	{
		set_flags(set_reserved_stack_access(_flags(), value));
	}

	public boolean is_scoped()
	{
		return is_scoped(_flags());
	}

	public void set_is_scoped(boolean value)
	{
		set_flags(set_is_scoped(_flags(), value));
	}

	public boolean changes_current_thread()
	{
		return changes_current_thread(_flags());
	}

	public void set_changes_current_thread(boolean value)
	{
		set_flags(set_changes_current_thread(_flags(), value));
	}

	public boolean jvmti_mount_transition()
	{
		return jvmti_mount_transition(_flags());
	}

	public void set_jvmti_mount_transition(boolean value)
	{
		set_flags(set_jvmti_mount_transition(_flags(), value));
	}

	public boolean deprecated()
	{
		return deprecated(_flags());
	}

	public void set_deprecated(boolean value)
	{
		set_flags(set_deprecated(_flags(), value));
	}

	public boolean deprecated_for_removal()
	{
		return deprecated_for_removal(_flags());
	}

	public void set_deprecated_for_removal(boolean value)
	{
		set_flags(set_deprecated_for_removal(_flags(), value));
	}

	public boolean jvmti_hide_events()
	{
		return jvmti_hide_events(_flags());
	}

	public void set_jvmti_hide_events(boolean value)
	{
		set_flags(set_jvmti_hide_events(_flags(), value));
	}
}

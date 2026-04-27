package jvmsp.hotspot.utilities;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot._native.include.classfile_constants;
import jvmsp.hotspot.utilities.globalDefinitions.BasicType;
import jvmsp.type.cxx_type;

public class constantTag extends vm_struct
{
	public static final String type_name = "constantTag";

	public static final cxx_type constantTag = cxx_type.define(type_name)
			.decl_field("_tag", cxx_type.uint8_t)
			.resolve();

	public static final long size = constantTag.size();

	private static final long _tag = constantTag.field("_tag").offset();

	public constantTag(long address)
	{
		super(type_name, address);
	}

	public byte tag()
	{
		return super.read_byte(_tag);
	}

	public void set_tag(byte tag)
	{
		super.write(_tag, tag);
	}

	public static boolean is_klass(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_Class;
	}

	public static boolean is_field(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_Fieldref;
	}

	public static boolean is_method(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_Methodref;
	}

	public static boolean is_interface_method(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_InterfaceMethodref;
	}

	public static boolean is_string(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_String;
	}

	public static boolean is_int(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_Integer;
	}

	public static boolean is_float(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_Float;
	}

	public static boolean is_long(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_Long;
	}

	public static boolean is_double(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_Double;
	}

	public static boolean is_name_and_type(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_NameAndType;
	}

	public static boolean is_utf8(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_Utf8;
	}

	public static boolean is_invalid(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_Invalid;
	}

	public static boolean is_unresolved_klass(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_UnresolvedClass
				|| tag == classfile_constants.JVM_CONSTANT_UnresolvedClassInError;
	}

	public static boolean is_unresolved_klass_in_error(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_UnresolvedClassInError;
	}

	public static boolean is_method_handle_in_error(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_MethodHandleInError;
	}

	public static boolean is_method_type_in_error(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_MethodTypeInError;
	}

	public static boolean is_dynamic_constant_in_error(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_DynamicInError;
	}

	public static boolean is_in_error(byte tag)
	{
		return is_unresolved_klass_in_error(tag)
				|| is_method_handle_in_error(tag)
				|| is_method_type_in_error(tag)
				|| is_dynamic_constant_in_error(tag);
	}

	public static boolean is_klass_index(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_ClassIndex;
	}

	public static boolean is_string_index(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_StringIndex;
	}

	public static boolean is_klass_reference(byte tag)
	{
		return is_klass_index(tag) || is_unresolved_klass(tag);
	}

	public static boolean is_klass_or_reference(byte tag)
	{
		return is_klass(tag) || is_klass_reference(tag);
	}

	public static boolean is_field_or_method(byte tag)
	{
		return is_field(tag) || is_method(tag) || is_interface_method(tag);
	}

	public static boolean is_symbol(byte tag)
	{
		return is_utf8(tag);
	}

	public static boolean is_method_type(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_MethodType;
	}

	public static boolean is_method_handle(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_MethodHandle;
	}

	public static boolean is_dynamic_constant(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_Dynamic;
	}

	public static boolean is_invoke_dynamic(byte tag)
	{
		return tag == classfile_constants.JVM_CONSTANT_InvokeDynamic;
	}

	public static boolean has_bootstrap(byte tag)
	{
		return (tag == classfile_constants.JVM_CONSTANT_Dynamic
				|| tag == classfile_constants.JVM_CONSTANT_DynamicInError
				|| tag == classfile_constants.JVM_CONSTANT_InvokeDynamic);
	}

	public static boolean is_loadable_constant(byte tag)
	{
		return ((tag >= classfile_constants.JVM_CONSTANT_Integer && tag <= classfile_constants.JVM_CONSTANT_String)
				|| is_method_type(tag)
				|| is_method_handle(tag)
				|| is_dynamic_constant(tag)
				|| is_unresolved_klass(tag));
	}

	public static byte type2tag(byte bt)
	{
		if (is_subword_type(bt))
		{
			bt = BasicType.T_INT;
		}
		if (bt == BasicType.T_ARRAY)
		{
			bt = BasicType.T_OBJECT;
		}
		if (bt == BasicType.T_INT)
			return classfile_constants.JVM_CONSTANT_Integer;
		else if (bt == BasicType.T_LONG)
			return classfile_constants.JVM_CONSTANT_Long;
		else if (bt == BasicType.T_FLOAT)
			return classfile_constants.JVM_CONSTANT_Float;
		else if (bt == BasicType.T_DOUBLE)
			return classfile_constants.JVM_CONSTANT_Double;
		else if (bt == BasicType.T_OBJECT)
			return classfile_constants.JVM_CONSTANT_String;
		else
			throw new java.lang.InternalError("invalid BasicType '" + bt + "'");
	}

	// 成员方法封装
	public boolean is_klass()
	{
		return is_klass(tag());
	}

	public boolean is_field()
	{
		return is_field(tag());
	}

	public boolean is_method()
	{
		return is_method(tag());
	}

	public boolean is_interface_method()
	{
		return is_interface_method(tag());
	}

	public boolean is_string()
	{
		return is_string(tag());
	}

	public boolean is_int()
	{
		return is_int(tag());
	}

	public boolean is_float()
	{
		return is_float(tag());
	}

	public boolean is_long()
	{
		return is_long(tag());
	}

	public boolean is_double()
	{
		return is_double(tag());
	}

	public boolean is_name_and_type()
	{
		return is_name_and_type(tag());
	}

	public boolean is_utf8()
	{
		return is_utf8(tag());
	}

	public boolean is_invalid()
	{
		return is_invalid(tag());
	}

	public boolean is_unresolved_klass()
	{
		return is_unresolved_klass(tag());
	}

	public boolean is_unresolved_klass_in_error()
	{
		return is_unresolved_klass_in_error(tag());
	}

	public boolean is_method_handle_in_error()
	{
		return is_method_handle_in_error(tag());
	}

	public boolean is_method_type_in_error()
	{
		return is_method_type_in_error(tag());
	}

	public boolean is_dynamic_constant_in_error()
	{
		return is_dynamic_constant_in_error(tag());
	}

	public boolean is_in_error()
	{
		return is_in_error(tag());
	}

	public boolean is_klass_index()
	{
		return is_klass_index(tag());
	}

	public boolean is_string_index()
	{
		return is_string_index(tag());
	}

	public boolean is_klass_reference()
	{
		return is_klass_reference(tag());
	}

	public boolean is_klass_or_reference()
	{
		return is_klass_or_reference(tag());
	}

	public boolean is_field_or_method()
	{
		return is_field_or_method(tag());
	}

	public boolean is_symbol()
	{
		return is_symbol(tag());
	}

	public boolean is_method_type()
	{
		return is_method_type(tag());
	}

	public boolean is_method_handle()
	{
		return is_method_handle(tag());
	}

	public boolean is_dynamic_constant()
	{
		return is_dynamic_constant(tag());
	}

	public boolean is_invoke_dynamic()
	{
		return is_invoke_dynamic(tag());
	}

	public boolean has_bootstrap()
	{
		return has_bootstrap(tag());
	}

	public boolean is_loadable_constant()
	{
		return is_loadable_constant(tag());
	}

	private static boolean is_subword_type(byte bt)
	{
		return bt == BasicType.T_BOOLEAN
				|| bt == BasicType.T_CHAR
				|| bt == BasicType.T_BYTE
				|| bt == BasicType.T_SHORT;
	}
}

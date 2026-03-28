package jvmsp.hotspot.oops;

import jvmsp.type.cxx_type;
import jvmsp.unsafe;
import jvmsp.hotspot.vm_constant;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.interpreter.Bytecodes;
import jvmsp.hotspot.memory.MetaspaceObj;
import jvmsp.hotspot.oops.Array.Array_pKlass;
import jvmsp.hotspot.oops.Array.Array_u1;
import jvmsp.hotspot.oops.Array.Array_u2;
import jvmsp.hotspot.utilities.align;

/**
 * 常量池。<br>
 */
public class ConstantPool extends Metadata
{
	public static final String type_name = "ConstantPool";
	public static final long size = sizeof(type_name);

	private static final long _tags = vm_struct.entry.find(type_name, "_tags").offset;// 8
	private static final long _cache = vm_struct.entry.find(type_name, "_cache").offset;// 16
	private static final long _pool_holder = vm_struct.entry.find(type_name, "_pool_holder").offset;// 24

	// InvokeDynamic节点使用，通常为空。JDK25存在，但JDK26已移除，并改为_bsm_entries
	private static final long _operands = switch_offset(// 32
			() -> vm_struct.entry.find(type_name, "_operands").offset, // JDK21
			() -> vm_struct.entry.find(type_name, "_operands").offset // JDK25
	);

	private static final long _resolved_klasses = vm_struct.entry.find(type_name, "_resolved_klasses").offset;// 40
	private static final long _major_version = vm_struct.entry.find(type_name, "_major_version").offset;// 48
	private static final long _minor_version = vm_struct.entry.find(type_name, "_minor_version").offset;// 50
	private static final long _generic_signature_index = vm_struct.entry.find(type_name, "_generic_signature_index").offset;// 52
	private static final long _source_file_name_index = vm_struct.entry.find(type_name, "_source_file_name_index").offset;// 54
	private static final long _length = vm_struct.entry.find(type_name, "_length").offset;// 60

	public ConstantPool(long address)
	{
		super(type_name, address);
	}

	@Override
	public final boolean is_constantPool()
	{
		return true;
	}

	public int length()
	{
		return super.read_int(_length);
	}

	public void set_length(int length)
	{
		super.write(_length, length);
	}

	public Array_u1 tags()
	{
		return super.read_memory_object_ptr(Array_u1.class, _tags);
	}

	public void set_tags(Array_u1 tags)
	{
		super.write_memory_object_ptr(_tags, tags);
	}

	public ConstantPoolCache cache()
	{
		return super.read_memory_object_ptr(ConstantPoolCache.class, _cache);
	}

	public void set_cache(ConstantPoolCache cache)
	{
		super.write_memory_object_ptr(_cache, cache);
	}

	public long pool_holder_addr()
	{
		return address + _pool_holder;
	}

	/**
	 * 持有该常量池的类
	 * 
	 * @return
	 */
	public InstanceKlass pool_holder()
	{
		return super.read_memory_object_ptr(InstanceKlass.class, _pool_holder);
	}

	public void set_pool_holder(InstanceKlass ik)
	{
		super.write_memory_object_ptr(_pool_holder, ik);
	}

	public Array_pKlass resolved_klasses()
	{
		return super.read_memory_object_ptr(Array_pKlass.class, _resolved_klasses);
	}

	public void set_resolved_klasses(Array_pKlass resolved_klasses)
	{
		super.write_memory_object_ptr(_resolved_klasses, resolved_klasses);
	}

	public Array_u2 operands()
	{
		return _operands < 0 ? null : super.read_memory_object_ptr(Array_u2.class, _operands);// JDK21、JDK25
	}

	public void set_operands(Array_u2 operands)
	{
		super.write_memory_object_ptr(_operands, operands);
	}

	public int major_version()
	{
		return super.read_uint16_t(_major_version);
	}

	public void set_major_version(int major_version)
	{
		super.write_uint16_t(_major_version, major_version);
	}

	public int minor_version()
	{
		return super.read_uint16_t(_minor_version);
	}

	public void set_minor_version(int minor_version)
	{
		super.write_uint16_t(_minor_version, minor_version);
	}

	public int generic_signature_index()
	{
		return super.read_uint16_t(_generic_signature_index);
	}

	public void set_generic_signature_index(int generic_signature_index)
	{
		super.write_uint16_t(_generic_signature_index, generic_signature_index);
	}

	/**
	 * 泛型签名，没有则返回null
	 * 
	 * @return
	 */
	public Symbol generic_signature()
	{
		int generic_signature_index = generic_signature_index();
		return (generic_signature_index == 0) ? null : symbol_at(generic_signature_index);
	}

	public int source_file_name_index()
	{
		return super.read_uint16_t(_source_file_name_index);
	}

	public void set_source_file_name_index(int source_file_name_index)
	{
		super.write_uint16_t(_source_file_name_index, source_file_name_index);
	}

	/**
	 * 获取源文件名称
	 * 
	 * @return
	 */
	public Symbol source_file_name()
	{
		int source_file_name_index = source_file_name_index();
		return (source_file_name_index == 0) ? null : symbol_at(source_file_name_index);
	}

	/**
	 * 如果有源文件名称则设置源文件名称
	 * 
	 * @return
	 */
	public void try_set_source_file_name(Symbol source_file_name)
	{
		int source_file_name_index = source_file_name_index();
		if (source_file_name_index != 0)
		{
			symbol_at_put(source_file_name_index, source_file_name);
		}
	}

	// 拓展方法
	public boolean is_within_bounds(int idx)
	{
		return 0 <= idx && idx < length();
	}

	public long base()
	{
		return address + size;
	}

	public void tag_at_put(int cp_index, byte t)
	{
		tags().at_put(cp_index, t);
	}

	/**
	 * 获取符号的地址
	 * 
	 * @param cp_index
	 * @return
	 */
	public long symbol_at_addr(int cp_index)
	{
		return base() + cp_index * unsafe.address_size;
	}

	public long obj_at_addr(int cp_index)
	{
		return base() + cp_index * unsafe.address_size;
	}

	public long int_at_addr(int cp_index)
	{
		return base() + cp_index * cxx_type.jint.size();
	}

	public long long_at_addr(int cp_index)
	{
		return base() + cp_index * cxx_type.jlong.size();
	}

	public long float_at_addr(int cp_index)
	{
		return base() + cp_index * cxx_type.jfloat.size();
	}

	public long double_at_addr(int cp_index)
	{
		return base() + cp_index * cxx_type.jdouble.size();
	}

	/**
	 * 获取指定索引的Symbol。<br>
	 * 需要注意传入的索引必须对应有效的Symbol*而非其他类型的常量值，否则使用非Symbol常量构建的Symbol对象会因为野指针崩溃。<br>
	 * 
	 * @param cp_index
	 * @return
	 */
	public Symbol symbol_at(int cp_index)
	{
		return as_memory_object_ptr(Symbol.class, symbol_at_addr(cp_index));
	}

	public void symbol_at_put(int cp_index, Symbol s)
	{
		tag_at_put(cp_index, vm_constant.JVM_CONSTANT_Utf8);
		unsafe.write_pointer(symbol_at_addr(cp_index), s);
	}

	public void symbol_at_put(int cp_index, String s)
	{
		symbol_at_put(cp_index, Symbol.permanent(s));
	}

	public void klass_index_at_put(int cp_index, int name_index)
	{
		tag_at_put(cp_index, vm_constant.JVM_CONSTANT_ClassIndex);
		unsafe.write(int_at_addr(cp_index), name_index);
	}

	public void method_handle_index_at_put(int cp_index, int ref_kind, int ref_index)
	{
		tag_at_put(cp_index, vm_constant.JVM_CONSTANT_MethodHandle);
		unsafe.write(int_at_addr(cp_index), ref_index << 16 | ref_kind);
	}

	public void method_type_index_at_put(int cp_index, int ref_index)
	{
		tag_at_put(cp_index, vm_constant.JVM_CONSTANT_MethodType);
		unsafe.write(int_at_addr(cp_index), ref_index);
	}

	public void dynamic_constant_at_put(int cp_index, int bsms_attribute_index, int name_and_type_index)
	{
		tag_at_put(cp_index, vm_constant.JVM_CONSTANT_Dynamic);
		unsafe.write(int_at_addr(cp_index), name_and_type_index << 16 | bsms_attribute_index);
	}

	public void invoke_dynamic_at_put(int cp_index, int bsms_attribute_index, int name_and_type_index)
	{
		tag_at_put(cp_index, vm_constant.JVM_CONSTANT_InvokeDynamic);
		unsafe.write(int_at_addr(cp_index), name_and_type_index << 16 | bsms_attribute_index);
	}

	public void unresolved_string_at_put(int cp_index, Symbol s)
	{
		tag_at_put(cp_index, vm_constant.JVM_CONSTANT_String);
		unsafe.write(symbol_at_addr(cp_index), s.address());
	}

	public void int_at_put(int cp_index, int i)
	{
		tag_at_put(cp_index, vm_constant.JVM_CONSTANT_Integer);
		unsafe.write(int_at_addr(cp_index), i);
	}

	public void long_at_put(int cp_index, long l)
	{
		tag_at_put(cp_index, vm_constant.JVM_CONSTANT_Long);
		unsafe.write(long_at_addr(cp_index), l);
	}

	public void float_at_put(int cp_index, float f)
	{
		tag_at_put(cp_index, vm_constant.JVM_CONSTANT_Float);
		unsafe.write(float_at_addr(cp_index), f);
	}

	public void double_at_put(int cp_index, double d)
	{
		tag_at_put(cp_index, vm_constant.JVM_CONSTANT_Double);
		unsafe.write(double_at_addr(cp_index), d);
	}

	public int resolve_cp_cache_idx(byte bytecode, int cp_cache_idx)
	{
		switch (bytecode)
		{
		case Bytecodes.Code._invokedynamic:
			return cache().resolved_indy_entry_at(cp_cache_idx).constant_pool_index();
		case Bytecodes.Code._getfield:
		case Bytecodes.Code._getstatic:
		case Bytecodes.Code._putfield:
		case Bytecodes.Code._putstatic:
			return cache().resolved_field_entry_at(cp_cache_idx).constant_pool_index();
		case Bytecodes.Code._invokeinterface:
		case Bytecodes.Code._invokehandle:
		case Bytecodes.Code._invokespecial:
		case Bytecodes.Code._invokestatic:
		case Bytecodes.Code._invokevirtual:
			return cache().resolved_method_entry_at(cp_cache_idx).constant_pool_index();
		default:
			throw new java.lang.InternalError("invalid bytecode '" + bytecode + "' with cp cache index '" + cp_cache_idx + "'");
		}
	}

	/**
	 * 以WORD为单位的本结构体WORD对齐后的大小
	 * 
	 * @return
	 */
	public static final int header_size()
	{
		return (int) (align.align_up(size, vm_constant.BytesPerWord) / vm_constant.BytesPerWord);
	}

	public static final int size(int length)
	{
		return (int) align.align_metadata_size(header_size() + length);
	}

	@Override
	public long size()
	{
		return size(length());
	}

	@Override
	public String internal_name()
	{
		return "{constant pool}";
	}

	@Override
	public int type()
	{
		return MetaspaceObj.Type.ConstantPoolType;
	}
}

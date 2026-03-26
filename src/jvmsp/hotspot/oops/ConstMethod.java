package jvmsp.hotspot.oops;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.MetaspaceObj;
import jvmsp.hotspot.oops.Array.Array_u1;

/**
 * 方法的不可变数据
 */
public class ConstMethod extends MetaspaceObj
{
	private static final long _fingerprint = vm_struct.entry.find("ConstMethod", "_fingerprint").offset;
	private static final long _constants = vm_struct.entry.find("ConstMethod", "_constants").offset;
	private static final long _stackmap_data = vm_struct.entry.find("ConstMethod", "_stackmap_data").offset;
	private static final long _constMethod_size = vm_struct.entry.find("ConstMethod", "_constMethod_size").offset;
	private static final long _flags_flags = vm_struct.entry.find("ConstMethod", "_flags._flags").offset;
	private static final long _code_size = vm_struct.entry.find("ConstMethod", "_code_size").offset;
	private static final long _name_index = vm_struct.entry.find("ConstMethod", "_name_index").offset;
	private static final long _signature_index = vm_struct.entry.find("ConstMethod", "_signature_index").offset;
	private static final long _method_idnum = vm_struct.entry.find("ConstMethod", "_method_idnum").offset;
	private static final long _max_stack = vm_struct.entry.find("ConstMethod", "_max_stack").offset;
	private static final long _max_locals = vm_struct.entry.find("ConstMethod", "_max_locals").offset;
	private static final long _size_of_parameters = vm_struct.entry.find("ConstMethod", "_size_of_parameters").offset;
	private static final long _num_stack_arg_slots = vm_struct.entry.find("ConstMethod", "_num_stack_arg_slots").offset;

	public static final long size = sizeof("ConstMethod");

	// ConstMethod对象中储存字节码的偏移量
	public static final long codes_offset = size;

	public ConstMethod(long address)
	{
		super("ConstMethod", address);
	}

	public long _fingerprint()
	{
		return super.read_long(_fingerprint);
	}

	public void _write_fingerprint(long fingerprint)
	{
		super.write(_fingerprint, fingerprint);
	}

	public ConstantPool constants()
	{
		return super.read_memory_object_ptr(ConstantPool.class, _constants);
	}

	public ConstMethodFlags flags()
	{
		return super.read_memory_object(ConstMethodFlags.class, _flags_flags);
	}

	public int name_index()
	{
		return super.read_uint16_t(_name_index);
	}

	public void set_name_index(int name_index)
	{
		super.write_uint16_t(_name_index, name_index);
	}

	public int signature_index()
	{
		return super.read_uint16_t(_signature_index);
	}

	public void set_signature_index(int signature_index)
	{
		super.write_uint16_t(_signature_index, signature_index);
	}

	public Array_u1 stackmap_data()
	{
		return super.read_memory_object_ptr(Array_u1.class, _stackmap_data);
	}

	public void set_stackmap_data(Array_u1 sd)
	{
		super.write_pointer(_stackmap_data, sd);
	}

	/**
	 * 包含字节码的总大小
	 * 
	 * @return
	 */
	public int size()
	{
		return super.read_cint(_constMethod_size);
	}

	public void set_constMethod_size(int size)
	{
		super.write_cint(_constMethod_size, size);
	}

	public long constMethod_end()
	{
		return address + size();
	}

	/**
	 * 字节码的大小。<br>
	 * native方法没有字节码，返回值始终是0.<br>
	 * 
	 * @return
	 */
	public int code_size()
	{
		return super.read_uint16_t(_code_size);
	}

	public void set_code_size(int code_size)
	{
		super.write_uint16_t(_code_size, code_size);
	}

	/**
	 * 拷贝指定地址的字节码
	 * 
	 * @param code_addr
	 */
	public void set_code(long code_addr)
	{
		if (code_size() > 0)
		{
			unsafe.memcpy(code_base(), code_addr, code_size());
		}
	}

	public void set_code(long code_addr, int num)
	{
		if (num > 0)
		{
			set_code_size(num);
			unsafe.memcpy(code_base(), code_addr, num);
		}
	}

	public void set_code(ConstMethod cm)
	{
		int num = cm.code_size();
		set_code_size(num);
		unsafe.memcpy(code_base(), cm.code_base(), num);
	}

	/**
	 * 设置字节码
	 * 
	 * @param bytecode
	 */
	public void set_code(byte[] bytecode)
	{
		if (bytecode.length > 0)
		{
			set_code_size(bytecode.length);
			unsafe.memcpy(code_base(), bytecode, 0, bytecode.length);
		}
		else
		{
			set_code_size(0);
		}
	}

	/**
	 * 字节码的起始地址
	 * 
	 * @return
	 */
	public long code_base()
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/constMethod.hpp#L420
		// 原定义为address code_base() const { return (address) (this+1); } 即对象起始地址+对象。
		return address + codes_offset;
	}

	/**
	 * 字节码的结束地址
	 * 
	 * @return
	 */
	public long code_end()
	{
		return code_base() + code_size();
	}

	public boolean contains(long bcp)
	{
		return code_base() <= bcp && bcp < code_end();
	}

	/**
	 * 读取字节码
	 * 
	 * @return
	 */
	public byte[] code()
	{
		byte[] bytecode = new byte[code_size()];
		if (bytecode.length > 0)
		{
			unsafe.memcpy(bytecode, 0, code_base(), bytecode.length);
		}
		return bytecode;
	}
}
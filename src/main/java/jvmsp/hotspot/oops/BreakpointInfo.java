package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;

/**
 * 断点信息
 */
public class BreakpointInfo extends CHeapObj
{
	public static final String type_name = "BreakpointInfo";
	public static final long size = sizeof(type_name);

	private static final long _orig_bytecode = vm_struct.entry.find(type_name, "_orig_bytecode").offset;
	private static final long _bci = vm_struct.entry.find(type_name, "_bci").offset;
	private static final long _name_index = vm_struct.entry.find(type_name, "_name_index").offset;
	private static final long _signature_index = vm_struct.entry.find(type_name, "_signature_index").offset;
	private static final long _next = vm_struct.entry.find(type_name, "_next").offset;

	public BreakpointInfo(long address)
	{
		super(type_name, address);
	}

	/**
	 * 该断点对应的字节码
	 * 
	 * @return
	 */
	public byte orig_bytecode()
	{
		return super.read_byte(_orig_bytecode);
	}

	public void set_orig_bytecode(byte orig_bytecode)
	{
		super.write(_orig_bytecode, orig_bytecode);
	}

	/**
	 * 相对于方法体起始地址字节码的偏移量
	 * 
	 * @return
	 */
	public int bci()
	{
		return super.read_cint(_bci);
	}

	public void set_bci(int bci)
	{
		super.write(_bci, bci);
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

	/**
	 * 下一个断点
	 * 
	 * @return
	 */
	public BreakpointInfo next()
	{
		return super.read_memory_object_ptr(BreakpointInfo.class, _next);
	}

	public void set_next(BreakpointInfo next)
	{
		super.write_memory_object_ptr(_next, next);
	}

	/**
	 * 判断该断点是否属于某个方法的对应的字节码
	 * 
	 * @param m
	 * @return
	 */
	public boolean match(Method m, int bci)
	{
		return bci == _bci && match(m);
	}

	/**
	 * 判断该断点是否属于某个方法
	 * 
	 * @param m
	 * @return
	 */
	public boolean match(Method m)
	{
		return name_index() == m.name_index() && signature_index() == m.signature_index();
	}
}
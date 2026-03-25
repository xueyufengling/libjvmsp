package jvmsp.hotspot.oops;

import jvmsp.memory;
import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;

public class Symbol extends vm_struct
{
	private static final long _hash_and_refcount = vm_struct.entry.find("Symbol", "_hash_and_refcount").offset;
	private static final long _length = vm_struct.entry.find("Symbol", "_length").offset;

	/**
	 * 符号的名称，UTF8字符数组。<br>
	 * 计算identity_hash也会使用。<br>
	 * GC不安全。<br>
	 * 尽管定义_body[]长度为2，但实际储存的长度大于2，具体长度为_length字段表明
	 */
	private static final long _body = vm_struct.entry.find("Symbol", "_body").offset;

	private static final long _body_0 = vm_struct.entry.find("Symbol", "_body[0]").offset;

	public Symbol(long address)
	{
		super("Symbol", address);
	}

	public int _hash_and_refcount()
	{
		return super.read_int(_hash_and_refcount);
	}

	public short _length()
	{
		return super.read_short(_length);
	}

	public byte char_at(int idx)
	{
		return super.read_byte(_body + idx);
	}

	/**
	 * 读取_body[]全部字节
	 * 
	 * @return
	 */
	public byte[] bytes()
	{
		byte[] b = new byte[_length()];
		unsafe.memcpy(base(), b, 0, b.length);
		return b;
	}

	public long base()
	{
		return this.address + _body_0;
	}

	/**
	 * 获取符号名称为C UTF-8字符串。<br>
	 * 
	 * @return
	 */
	public long as_C_string()
	{
		return base();
	}

	/**
	 * 获取符号名称为Java字符串。<br>
	 * 
	 * @return
	 */
	public String jstring()
	{
		return memory.string(as_C_string());
	}

	@Override
	public String toString()
	{
		return jstring();
	}
}
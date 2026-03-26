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

	public static final long size = sizeof("Symbol");

	public Symbol(long address)
	{
		super("Symbol", address);
	}

	public Symbol(int _hash_and_refcount, byte[] bytes)
	{
		super("Symbol", unsafe.malloc(size + bytes.length));
		set_hash_and_refcount(_hash_and_refcount);
		set_data(bytes);
	}

	public Symbol(int _hash_and_refcount, String sym)
	{
		this(_hash_and_refcount, sym.getBytes());
	}

	public int _hash_and_refcount()
	{
		return super.read_int(_hash_and_refcount);
	}

	public void set_hash_and_refcount(int hash_and_refcount)
	{
		super.write(_hash_and_refcount, hash_and_refcount);
	}

	public int length()
	{
		return super.read_uint16_t(_length);
	}

	public void set_data(byte[] bytes)
	{
		super.write_uint16_t(_length, bytes.length);
		unsafe.memcpy(base(), bytes, 0, bytes.length);
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
	public byte[] data()
	{
		byte[] b = new byte[length()];
		unsafe.memcpy(b, 0, base(), b.length);
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
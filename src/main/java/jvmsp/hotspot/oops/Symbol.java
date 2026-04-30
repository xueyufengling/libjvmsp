package jvmsp.hotspot.oops;

import java.nio.charset.Charset;

import jvmsp.memory;
import jvmsp.unsafe;
import jvmsp.hotspot.vm_constant;
import jvmsp.hotspot.vm_struct;

public class Symbol extends vm_struct
{

	public static final String type_name = "Symbol";
	public static final long size = sizeof(type_name);

	private static final long _hash_and_refcount = vm_struct.entry.find(type_name, "_hash_and_refcount").offset;
	private static final long _length = vm_struct.entry.find(type_name, "_length").offset;

	/**
	 * 符号的名称，UTF8字符数组。<br>
	 * 计算identity_hash也会使用。<br>
	 * GC不安全。<br>
	 * 尽管定义_body[]长度为2，但实际储存的长度大于2，具体长度为_length字段表明
	 */
	private static final long _body = vm_struct.entry.find(type_name, "_body").offset;

	private static final long _body_0 = vm_struct.entry.find(type_name, "_body[0]").offset;

	public static final int max_symbol_length = vm_constant.find_int("Symbol::max_symbol_length");

	public Symbol(long address)
	{
		super(type_name, address);
	}

	public Symbol(short hash, int refcount, byte[] bytes)
	{
		super(type_name, unsafe.malloc(size + bytes.length - 1));// _body[2]本身包含2个字节，末尾字符串结尾'\0'，因此需要再减1
		set_hash_and_refcount(hash, refcount);
		set_str_data(bytes);
	}

	public Symbol(short hash, int refcount, String sym)
	{
		this(hash, refcount, sym.getBytes(Charset.defaultCharset()));// 必须是UTF-8编码
	}

	/**
	 * 提取符号的hash值
	 * 
	 * @param hash_and_refcount
	 * @return
	 */
	public static final short extract_hash(int hash_and_refcount)
	{
		return (short) (hash_and_refcount >> 16);
	}

	/**
	 * 该回收计数表示该符号永远不会被回收
	 */
	public static final int PERM_REFCOUNT = 0xffff;

	/**
	 * 提取符号的引用计数
	 * 
	 * @param hash_and_refcount
	 * @return
	 */
	public static final int extract_refcount(int hash_and_refcount)
	{
		return hash_and_refcount & 0xffff;
	}

	public static final int pack_hash_and_refcount(short hash, int refcount)
	{
		return (hash << 16) | refcount;
	}

	public static final Symbol permanent(String name)
	{
		return new Symbol((short) 0, PERM_REFCOUNT, name);
	}

	public int _hash_and_refcount()
	{
		return super.read_int(_hash_and_refcount);
	}

	public void set_hash_and_refcount(int hash_and_refcount)
	{
		super.write(_hash_and_refcount, hash_and_refcount);
	}

	public void set_hash_and_refcount(short hash, int refcount)
	{
		set_hash_and_refcount(pack_hash_and_refcount(hash, refcount));
	}

	public short hash()
	{
		return extract_hash(_hash_and_refcount());
	}

	/**
	 * 设置hash值
	 * 
	 * @param hash
	 */
	public void set_hash(short hash)
	{
		set_hash_and_refcount(pack_hash_and_refcount(hash, refcount()));
	}

	public int refcount()
	{
		return extract_refcount(_hash_and_refcount());
	}

	/**
	 * 设置引用计数
	 * 
	 * @param hash
	 */
	public void set_refcount(int refcount)
	{
		set_hash_and_refcount(pack_hash_and_refcount(hash(), refcount));
	}

	/**
	 * 该符号是否是永久的
	 * 
	 * @return
	 */
	public boolean is_permanent()
	{
		return (refcount() == PERM_REFCOUNT);
	}

	/**
	 * 设置符号为永久的
	 */
	public void set_permanent()
	{
		set_refcount(PERM_REFCOUNT);
	}

	/**
	 * 符号名称的长度
	 * 
	 * @return
	 */
	public int length()
	{
		return super.read_uint16_t(_length);
	}

	/**
	 * 设置数据
	 * 
	 * @param bytes
	 */
	public void set_data(byte[] bytes)
	{
		super.write_uint16_t(_length, bytes.length);
		unsafe.memcpy(base(), bytes, 0, bytes.length);
	}

	/**
	 * 设置数据，其中bytes中不包含结尾的'\0'，由此方法负责增加。<br>
	 * 
	 * @param bytes
	 */
	public void set_str_data(byte[] bytes)
	{
		set_data(bytes);
		unsafe.write(base() + bytes.length, (byte) 0);
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
		return memory.string(as_C_string(), length());
	}

	@Override
	public String toString()
	{
		return jstring();
	}
}
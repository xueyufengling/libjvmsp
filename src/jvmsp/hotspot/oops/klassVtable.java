package jvmsp.hotspot.oops;

import jvmsp.hotspot.vm_struct;

/**
 * 继承的超类虚方法表
 */
public class klassVtable extends vm_struct
{
	private Klass _klass;
	/**
	 * 条目起始偏移量
	 */
	int _tableOffset;
	private int _length;

	public klassVtable(Klass klass, long base, int length)
	{
		super("klassVtable", base);
		this._klass = klass;
		_tableOffset = (int) (base - klass.address());
		this._length = length;
	}

	public long table()
	{
		return address;
	}

	public long table_offset()
	{
		return _tableOffset;
	}

	public Klass klass()
	{
		return _klass;
	}

	public int length()
	{
		return _length;
	}
}
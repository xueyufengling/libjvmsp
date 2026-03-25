package jvmsp.hotspot.oops;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;

public class oopDesc
{
	private static final long _mark = vm_struct.entry.find("oopDesc", "_mark").offset;
	private static final long _metadata_klass = vm_struct.entry.find("oopDesc", "_metadata._klass").offset;
	private static final long _metadata_compressed_klass = vm_struct.entry.find("oopDesc", "_metadata._compressed_klass").offset;

	/**
	 * 计算oopDesc字段偏移量
	 * 
	 * @param oop
	 * @param offset
	 * @return
	 */
	public static final long field_addr(long oop, int offset)
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/oop.inline.hpp#L239
		return oop + offset;
	}

	/**
	 * 获取指定偏移量储存的指针值
	 * 
	 * @param oop
	 * @param offset
	 * @return
	 */
	public static final long ptr_field(long oop, int offset)
	{
		return unsafe.read_pointer(field_addr(oop, offset));
	}

	/**
	 * 获取元数据指针
	 * 
	 * @param oop
	 * @param offset
	 * @return
	 */
	public static final long metadata_field(long oop, int offset)
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/oop.cpp#L186
		return ptr_field(oop, offset);
	}
}

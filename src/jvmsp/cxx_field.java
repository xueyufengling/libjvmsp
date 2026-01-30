package jvmsp;

public class cxx_field implements Cloneable {

	/**
	 * 克隆字段，当分析继承结构需要修改offset时使用
	 */
	@Override
	public cxx_field clone() {
		try {
			return (cxx_field) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return type.toString() + ' ' + name;
	}

	private String name;

	/**
	 * 字段偏移量，由cxx_type类计算
	 */
	long offset = 0;

	/**
	 * 该字段的类型
	 */
	private cxx_type type;

	/**
	 * 声明该字段的类
	 */
	cxx_type decl_type;

	/**
	 * 字段偏移量
	 * 
	 * @return
	 */
	public long offset() {
		return offset;
	}

	/**
	 * 字段名称
	 * 
	 * @return
	 */
	public String name() {
		return name;
	}

	/**
	 * 字段类型
	 * 
	 * @return
	 */
	public cxx_type type() {
		return type;
	}

	/**
	 * 该字段属于哪个类
	 * 
	 * @return
	 */
	public cxx_type decl_type() {
		return decl_type;
	}

	private cxx_field(String name, cxx_type type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * 定义字段
	 * 
	 * @param name
	 * @param type
	 * @return
	 */
	public static final cxx_field define(String name, cxx_type type) {
		return new cxx_field(name, type);
	}

	/**
	 * 访问一个基本类型字段
	 * 
	 * @param native_addr
	 * @return
	 */
	final Object access(long native_addr) {
		long access_addr = native_addr + offset;
		if (type == cxx_stdtypes._char || type == cxx_stdtypes.int8_t)
			return unsafe.read_byte(null, access_addr);
		else if (type == cxx_stdtypes.unsigned_char || type == cxx_stdtypes.uint8_t)
			return cxx_stdtypes.uint8_t(unsafe.read_byte(null, access_addr));
		else if (type == cxx_stdtypes._short || type == cxx_stdtypes.int16_t)
			return unsafe.read_short(null, access_addr);
		else if (type == cxx_stdtypes.unsigned_short || type == cxx_stdtypes.uint16_t)
			return cxx_stdtypes.uint16_t(unsafe.read_short(null, access_addr));
		else if (type == cxx_stdtypes._int || type == cxx_stdtypes.int32_t)
			return unsafe.read_int(null, access_addr);
		else if (type == cxx_stdtypes.unsigned_int || type == cxx_stdtypes.uint32_t || type == cxx_stdtypes.bool)
			return cxx_stdtypes.uint32_t(unsafe.read_int(null, access_addr));
		else if (type == cxx_stdtypes._long_long || type == cxx_stdtypes.int64_t || type == cxx_stdtypes.unsigned_long_long || type == cxx_stdtypes.uint64_t)// 很遗憾，Java没有比64位无符号整数还大的基本类型，因此不论有无符号均储存在Java的有符号long类型
			return unsafe.read_long(null, access_addr);
		else if (type == cxx_stdtypes._float)
			return unsafe.read_float(null, access_addr);
		else if (type == cxx_stdtypes._double)
			return unsafe.read_double(null, access_addr);
		else if (type == cxx_stdtypes.WORD || type == cxx_stdtypes.pointer || type == cxx_stdtypes.uintptr_t) {
			if (cxx_type.sizeof(type) == 4)
				return cxx_stdtypes.uint32_t(unsafe.read_int(null, access_addr));
			else if (cxx_type.sizeof(type) == 8)
				return unsafe.read_long(null, access_addr);
		} else
			return new cxx_object(access_addr, type);
		return 0;
	}
}

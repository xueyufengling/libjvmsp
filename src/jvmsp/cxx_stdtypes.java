package jvmsp;

/**
 * 仅支持32位和64位机器，16位的不支持。JVM有16位吗？
 */
public class cxx_stdtypes {
	public static final cxx_type _char = cxx_type.define_primitive("char", 1);
	public static final cxx_type unsigned_char = cxx_type.define_primitive("unsigned char", cxx_type.sizeof(_char));
	public static final cxx_type _short = cxx_type.define_primitive("short", 2);
	public static final cxx_type unsigned_short = cxx_type.define_primitive("unsigned short", cxx_type.sizeof(_short));
	public static final cxx_type _int = cxx_type.define_primitive("int", 4);
	public static final cxx_type unsigned_int = cxx_type.define_primitive("unsigned int", cxx_type.sizeof(_int));
	public static final cxx_type bool = cxx_type.define_primitive("bool", cxx_type.sizeof(_int));
	public static final cxx_type _long_long = cxx_type.define_primitive("long long", 8);
	public static final cxx_type unsigned_long_long = cxx_type.define_primitive("unsigned long long", cxx_type.sizeof(_long_long));
	public static final cxx_type _float = cxx_type.define_primitive("float", cxx_type.sizeof(_int));
	public static final cxx_type _double = cxx_type.define_primitive("double", cxx_type.sizeof(_long_long));
	public static final cxx_type _void = cxx_type.define_primitive("void", 0);

	/**
	 * 无符号机器数据字
	 */
	public static final cxx_type WORD;

	static {
		if (VmBase.ON_64_BIT_JVM)
			WORD = cxx_type.define_primitive("WORD", 8);
		else
			WORD = cxx_type.define_primitive("WORD", 4);
	}

	public static final cxx_type pointer = cxx_type.define_primitive("void*", cxx_type.sizeof(WORD));

	public static final cxx_type pointer(String type) {
		return cxx_type.define_primitive(type + '*', cxx_type.sizeof(WORD));
	}

	public static final cxx_type pointer(cxx_type type) {
		return cxx_type.define_primitive(type.name() + '*', cxx_type.sizeof(WORD));
	}

	public static final cxx_type uintptr_t = cxx_type.define_primitive("uintptr_t", cxx_type.sizeof(pointer));

	public static final cxx_type uintptr_t(String type) {
		return cxx_type.define_primitive(type + '*', cxx_type.sizeof(WORD));
	}

	public static final cxx_type uintptr_t(cxx_type type) {
		return cxx_type.define_primitive(type.name() + '*', cxx_type.sizeof(WORD));
	}

	public static final cxx_type int8_t = cxx_type.define_primitive("int8_t", 1);
	public static final cxx_type uint8_t = cxx_type.define_primitive("uint8_t", cxx_type.sizeof(int8_t));
	public static final cxx_type int16_t = cxx_type.define_primitive("int16_t", 2);
	public static final cxx_type uint16_t = cxx_type.define_primitive("uint16_t", cxx_type.sizeof(int16_t));
	public static final cxx_type int32_t = cxx_type.define_primitive("int32_t", 4);
	public static final cxx_type uint32_t = cxx_type.define_primitive("uint32_t", cxx_type.sizeof(int32_t));
	public static final cxx_type int64_t = cxx_type.define_primitive("int64_t", 8);
	public static final cxx_type uint64_t = cxx_type.define_primitive("uint64_t", cxx_type.sizeof(int64_t));

	/**
	 * 用于将signed int类型储存的unsigned int值转换为unsigned long值。<br>
	 * 用法：{@code uint64_t addr = (int32_t) & UINT32_T_MASK;}
	 */
	public static final long UINT32_T_MASK = 0xFFFFFFFFL;

	public static final long uint_ptr(int oop_addr) {
		return oop_addr & UINT32_T_MASK;
	}

	public static final long UINT16_T_MASK = 0xFFFFL;

	public static final long uint_ptr(short s) {
		return s & UINT16_T_MASK;
	}

	public static final long UINT8_T_MASK = 0xFFL;

	public static final long uint_ptr(byte b) {
		return b & UINT8_T_MASK;
	}

	public static final long uint_ptr(char c) {
		return c & UINT8_T_MASK;
	}

	public static final int UINT8_T_MASK_I = 0xFF;

	public static final int uint8_t(byte b) {
		return b & UINT8_T_MASK_I;
	}

	public static final int uint8_t(char c) {
		return c & UINT8_T_MASK_I;
	}

	public static final int UINT16_T_MASK_I = 0xFFFF;

	public static final int uint16_t(short s) {
		return s & UINT16_T_MASK_I;
	}

	public static final long uint32_t(int i) {
		return i & UINT32_T_MASK;
	}
}

package jvmsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * C++对象内存布局计算
 */
public class cxx_type {

	public static class field implements Cloneable {

		/**
		 * 克隆字段，当分析继承结构需要修改offset时使用
		 */
		@Override
		public field clone() {
			try {
				return (field) super.clone();
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

		private field(String name, cxx_type type) {
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
		public static final field define(String name, cxx_type type) {
			return new field(name, type);
		}

		/**
		 * 访问一个基本类型字段
		 * 
		 * @param native_addr
		 * @return
		 */
		final Object access(long native_addr) {
			long access_addr = native_addr + offset;
			if (type == _char || type == int8_t)
				return unsafe.read_byte(null, access_addr);
			else if (type == unsigned_char || type == uint8_t)
				return uint8_t(unsafe.read_byte(null, access_addr));
			else if (type == _short || type == int16_t)
				return unsafe.read_short(null, access_addr);
			else if (type == unsigned_short || type == uint16_t)
				return uint16_t(unsafe.read_short(null, access_addr));
			else if (type == _int || type == int32_t)
				return unsafe.read_int(null, access_addr);
			else if (type == unsigned_int || type == uint32_t || type == bool)
				return uint32_t(unsafe.read_int(null, access_addr));
			else if (type == _long_long || type == int64_t || type == unsigned_long_long || type == uint64_t)// 很遗憾，Java没有比64位无符号整数还大的基本类型，因此不论有无符号均储存在Java的有符号long类型
				return unsafe.read_long(null, access_addr);
			else if (type == _float)
				return unsafe.read_float(null, access_addr);
			else if (type == _double)
				return unsafe.read_double(null, access_addr);
			else if (type == WORD || type == pointer || type == uintptr_t) {
				if (cxx_type.sizeof(type) == 4)
					return uint32_t(unsafe.read_int(null, access_addr));
				else if (cxx_type.sizeof(type) == 8)
					return unsafe.read_long(null, access_addr);
			} else
				return type.new object(access_addr);
			return 0;
		}
	}

	/**
	 * 所有定义的类型
	 */
	private static final HashMap<String, cxx_type> definedTypes = new HashMap<>();

	@Override
	public cxx_type clone() {
		try {
			return (cxx_type) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		return (obj instanceof cxx_type type) && this.name.equals(type.name);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name) ^ Objects.hashCode(size) ^ Objects.hashCode(base_types) ^ Objects.hashCode(fields);
	}

	@Override
	public String toString() {
		return name;
	}

	private String name;
	/**
	 * 类型的长度，不含padding
	 */
	private long size;

	/**
	 * 父类
	 */
	private cxx_type[] base_types;

	/**
	 * 所有直接或间接基类的字段全部展开存放的数组
	 */
	private ArrayList<field> base_fields;

	/**
	 * 派生类，即本类的对象顶，是本类第一个字段的偏移量
	 */
	private long derived_top;

	/**
	 * 默认的对齐尺寸，是该类型包含的最大的基本类型的大小
	 */
	private long default_align_size;

	public long default_align_size() {
		return resolve().default_align_size;
	}

	/**
	 * pragma pack指定的对齐
	 */
	private long pragma_pack;

	public long pragma_pack() {
		return pragma_pack;
	}

	/**
	 * 最终的结构体整体对齐大小，结构体的size必定为此数的整数倍。实际上是{@code min(align_size, pragma_pack)}
	 */
	private long align_size;

	public long align_size() {
		return resolve().align_size;
	}

	/**
	 * 解析派生类的内存布局时，基类使用自己的pragma_pack
	 * 
	 * @param override_pragma_pack
	 * @return
	 */
	private long override_align_size(cxx_type override_pragma_pack) {
		return Math.min(align_size(), override_pragma_pack.pragma_pack);
	}

	/**
	 * 计算基类字段偏移
	 * 
	 * @param arr            字段数组
	 * @param current_offset 计算开始前的偏移量
	 * @return 计算完成后的末尾偏移量
	 */
	private long resolve_base_fields(ArrayList<field> arr, long current_offset) {
		for (int i = 0; i < base_types.length; ++i)// 基类字段放在最前方
			current_offset = base_types[i].resolve_base_fields(arr, current_offset);
		for (int i = 0; i < fields.size(); ++i) {
			field current_field = fields.get(i).clone();
			cxx_type current_field_type = current_field.type();
			long as = Math.min(current_field_type.pragma_pack(), current_field_type.override_align_size(this));// 字段对齐数
			if (current_offset % as != 0)
				current_offset = (current_offset / as + 1) * as;// 字节对齐
			current_field.offset = current_offset;
			current_offset += sizeof(current_field_type);
			arr.add(current_field);
		}
		return current_offset;
	}

	/**
	 * 本身的字段
	 */
	private ArrayList<field> fields;

	/**
	 * 更新size的标记
	 */
	boolean dirty_flag;

	/**
	 * 定义一个类型，禁止递归继承，且基类必须已经解析完成，即基类的size和字段不再变化。
	 * 
	 * @param type
	 * @param pragma_pack 对齐字节数
	 * @param base_types
	 * @return
	 */
	public static final cxx_type define(String type, long pragma_pack, cxx_type... base_types) {
		return definedTypes.computeIfAbsent(type, (String t) -> new cxx_type(type, pragma_pack, base_types));
	}

	public static final cxx_type define(String type, cxx_type... base_types) {
		return define(type, Long.MAX_VALUE, base_types);
	}

	/**
	 * 仅用于定义基本类型
	 * 
	 * @param name
	 * @param size
	 */
	private cxx_type(String name, long size) {
		this.name = name;
		this.size = size;
		this.align_size = size;
		this.default_align_size = size;
		this.pragma_pack = size;
		this.dirty_flag = false;
	}

	private cxx_type(String name, long pragma_pack, cxx_type... base_types) {
		this.name = name;
		this.size = 0;
		this.pragma_pack = pragma_pack;
		this.fields = new ArrayList<>();
		this.base_types = base_types;
		this.base_fields = new ArrayList<>();
		this.derived_top = resolve_base_fields(base_fields, 0);
		this.dirty_flag = true;
	}

	/**
	 * 定义一个原生类型的长度，原生类型没有字段
	 * 
	 * @param type
	 * @return
	 */
	private static final cxx_type define_primitive(String type, long size) {
		cxx_type new_type = new cxx_type(type, size);
		definedTypes.put(type, new_type);
		return new_type;
	}

	/**
	 * 是否是基本类型
	 * 
	 * @return
	 */
	public final boolean is_primitive() {
		return fields == null;
	}

	/**
	 * 添加字段，禁止添加类本身。
	 * 
	 * @param field
	 * @return
	 */
	public field decl_field(field field) {
		if (is_primitive() || field.type().equals(this))// 禁止给原生类型添加字段，或添加类本身作为字段
			throw new RuntimeException("Cannot append field \"" + field + "\" to " + this + ". append fields to primitive types or append self as a field are not allowed.");
		fields.add(field);
		field.decl_type = this;
		this.dirty_flag = true;// 标记size更新
		return field;
	}

	public field decl_field(String name, cxx_type type) {
		return decl_field(field.define(name, type));
	}

	/**
	 * 更新字段偏移量和本类型的大小
	 * 
	 * @return
	 */
	private long resolve_offset_and_size() {
		if (base_types.length == 0 && fields.isEmpty()) {// 空结构体
			size = 1;
			align_size = 1;
			default_align_size = 1;
			pragma_pack = 1;
		} else {
			long current_offset = derived_top;
			for (int idx = 0; idx < fields.size(); ++idx) {
				field f = fields.get(idx);
				cxx_type f_type = f.type();// 字段f的类型
				long as = Math.min(pragma_pack, f_type.align_size());// 字段对齐数
				if (as > align_size)
					align_size = as;// 更新本结构体的整体对齐字节数
				if (current_offset % as != 0)
					current_offset = (current_offset / as + 1) * as;// 字节对齐
				f.offset = current_offset;
				current_offset += sizeof(f_type);
			}
			size = current_offset;
		}
		return size;
	}

	/**
	 * 更新数据，包括对齐大小、偏移量、占用内存尺寸
	 * 
	 * @return
	 */
	private cxx_type resolve() {
		if (is_primitive() || !dirty_flag) {
			return this;
		} else {
			dirty_flag = false;
			resolve_offset_and_size();
			return this;
		}
	}

	/**
	 * 获取目标类型的大小
	 * 
	 * @param t
	 * @return
	 */
	public static final long sizeof(cxx_type t) {
		return t.resolve().size;
	}

	/**
	 * 获取类型名称
	 * 
	 * @return
	 */
	public String name() {
		return name;
	}

	/**
	 * 获取本类声明的指定索引字段，不包含基类字段
	 * 
	 * @param idx
	 * @return
	 */
	public field declared_field_at(int idx) {
		return is_primitive() ? null : fields.get(idx);
	}

	/**
	 * 获取本类声明的指定索引字段，包含基类字段
	 * 
	 * @param idx
	 * @return
	 */
	public field field_at(int idx) {
		if (is_primitive() || idx < 0)
			return null;
		int base_f_num = base_fields.size();
		int self_f_idx = idx - base_f_num;
		if (self_f_idx < 0)
			return base_fields.get(idx);
		else if (self_f_idx < fields.size())
			return fields.get(self_f_idx);
		else
			return null;
	}

	public int declared_field_index(String field_name) {
		if (!is_primitive())
			for (int idx = 0; idx < fields.size(); ++idx)// 优先使用派生类的字段
				if (fields.get(idx).name().equals(field_name))
					return idx;
		return -1;
	}

	/**
	 * 获取本类声明的指定名称的字段索引
	 * 
	 * @param field_name
	 * @return
	 */
	public int field_index(String field_name) {
		if (!is_primitive()) {
			for (int idx = 0; idx < fields.size(); ++idx)// 优先使用派生类的字段
				if (fields.get(idx).name().equals(field_name))
					return idx;
			for (int idx = 0; idx < base_fields.size(); ++idx)
				if (base_fields.get(idx).name().equals(field_name))// 派生类没有该字段则查找基类字段
					return idx;
		}
		return -1;
	}

	public field declared_field(String field_name) {
		if (!is_primitive()) {
			for (int idx = 0; idx < fields.size(); ++idx) {// 优先使用派生类的字段
				field f = fields.get(idx);
				if (f.name().equals(field_name))
					return f;
			}
		}
		return null;
	}

	/**
	 * 获取本类声明的指定名称的字段
	 * 
	 * @param field_name
	 * @return
	 */
	public field field(String field_name) {
		if (!is_primitive()) {
			for (int idx = 0; idx < fields.size(); ++idx) {// 优先使用派生类的字段
				field f = fields.get(idx);
				if (f.name().equals(field_name))
					return f;
			}
			for (int idx = 0; idx < base_fields.size(); ++idx) {
				field f = base_fields.get(idx);
				if (f.name().equals(field_name))// 派生类没有该字段则查找基类字段
					return f;
			}
		}
		return null;
	}

	/**
	 * C++对象内存布局打印工具，<br>
	 * 当打印字符不对齐时请修改tab_size的值，并且使用带参数的打印函数。<br>
	 */
	private static final class mem_layout_printer {

		public static int type_element_size_in_tab = 3;
		public static int number_element_size_in_tab = 2;

		private static final int line_size_in_char(int type_element_size_in_tab, int number_element_size_in_tab) {
			return (1 + type_element_size_in_tab * 2 + number_element_size_in_tab * 2) * tab_size;
		}

		private static final int line_size_in_tab(int type_element_size_in_tab, int number_element_size_in_tab) {
			return (1 + type_element_size_in_tab * 2 + number_element_size_in_tab * 2);
		}

		private static final String print_mem_layout_split_line(int type_element_size_in_tab, int number_element_size_in_tab) {
			return "├" + "─".repeat(line_size_in_char(type_element_size_in_tab, number_element_size_in_tab) - 1) + "┤";
		}

		private static final String print_mem_layout_start_line(int type_element_size_in_tab, int number_element_size_in_tab) {
			return "┌" + "─".repeat(line_size_in_char(type_element_size_in_tab, number_element_size_in_tab) - 1) + "┐";
		}

		private static final String print_mem_layout_end_line(int type_element_size_in_tab, int number_element_size_in_tab) {
			return "└" + "─".repeat(line_size_in_char(type_element_size_in_tab, number_element_size_in_tab) - 1) + "┘";
		}

		private static final String header_text(String text, int type_element_size_in_tab, int number_element_size_in_tab) {
			int line_start_tab = type_element_size_in_tab + 1;
			return "│" + "\t".repeat(line_start_tab) + layout_info_element(text, line_size_in_tab(type_element_size_in_tab, number_element_size_in_tab) - line_start_tab) + "│";
		}

		/**
		 * @param print_idx_as_from 显示打印字段从哪个索引开始
		 * @param fields
		 */
		private static void print_fields_layout(long print_idx_as_from, ArrayList<field> fields, int type_element_size_in_tab, int number_element_size_in_tab) {
			for (int i = 0; i < fields.size(); ++i) {
				field f = fields.get(i);
				System.out.println("│ " + layout_info_element("" + (print_idx_as_from + i), 1) + layout_info_element("in: " + f.decl_type.toString(), type_element_size_in_tab) + layout_info_element(f.toString(), type_element_size_in_tab) + layout_info_element("offset: " + f.offset + ",", number_element_size_in_tab) + layout_info_element("size: " + sizeof(f.type()), number_element_size_in_tab) + "│");
			}
		}

		public static int tab_size = 8;

		/**
		 * 制表符对齐
		 * 
		 * @param text
		 * @param size_in_tab_num
		 * @return
		 */
		private static String layout_info_element(String text, int size_in_tab_num) {
			int append_size = size_in_tab_num * tab_size - text.length();
			if (append_size <= 0)
				return text;
			int append_tab_num = append_size / tab_size;
			if (append_size % tab_size != 0)
				++append_tab_num;
			return text + "\t".repeat(append_tab_num);
		}

		public static void print_mem_layout(cxx_type t) {
			print_mem_layout(t, type_element_size_in_tab, number_element_size_in_tab);
		}

		/**
		 * 打印内存布局相关信息
		 * 
		 * @param t                          要打印的目标类型
		 * @param type_element_size_in_tab   声明的类、字段描述占几个tab长度
		 * @param number_element_size_in_tab 偏移量、字段大小占几个tab长度
		 */
		public static void print_mem_layout(cxx_type t, int type_element_size_in_tab, int number_element_size_in_tab) {
			System.out.println(print_mem_layout_start_line(type_element_size_in_tab, number_element_size_in_tab));
			System.out.println("│" + "\t".repeat(type_element_size_in_tab + 1) + layout_info_element(t.toString(), 3) + layout_info_element("alignment: " + t.align_size() + ",", 2) + layout_info_element("size: " + sizeof(t), 2) + "│");
			if (!t.is_primitive()) {
				if (t.base_types.length != 0) {
					System.out.print("│" + "\t".repeat(type_element_size_in_tab + 1));
					String inherit_info = "Inherited: ";
					for (int i = 0; i < t.base_types.length; ++i) {
						inherit_info += t.base_types[i];
						if (i != t.base_types.length - 1)
							inherit_info += ", ";
					}
					System.out.print(layout_info_element(inherit_info, 7) + "│\n");
				}
				if (!t.base_fields.isEmpty()) {
					System.out.println(print_mem_layout_split_line(type_element_size_in_tab, number_element_size_in_tab) + "\n" + header_text("** Base Fields **", type_element_size_in_tab, number_element_size_in_tab) + "\n" + print_mem_layout_split_line(type_element_size_in_tab, number_element_size_in_tab));
					print_fields_layout(0, t.base_fields, type_element_size_in_tab, number_element_size_in_tab);
				}
				if (!t.fields.isEmpty()) {
					System.out.println(print_mem_layout_split_line(type_element_size_in_tab, number_element_size_in_tab) + "\n" + header_text("** Declared Fields **", type_element_size_in_tab, number_element_size_in_tab) + "\n" + print_mem_layout_split_line(type_element_size_in_tab, number_element_size_in_tab));
					print_fields_layout(t.base_fields.size(), t.fields, type_element_size_in_tab, number_element_size_in_tab);
				}
			}
			System.out.println(print_mem_layout_end_line(type_element_size_in_tab, number_element_size_in_tab));
		}
	}

	public void print_mem_layout(int type_element_size_in_tab, int number_element_size_in_tab) {
		mem_layout_printer.print_mem_layout(this, type_element_size_in_tab, number_element_size_in_tab);
	}

	public void print_mem_layout() {
		mem_layout_printer.print_mem_layout(this);
	}

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
		if (virtual_machine.ON_64_BIT_JVM)
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

	/**
	 * C++对象操作
	 */
	public class object {
		final pointer ptr;

		object(pointer ptr) {
			this.ptr = ptr.copy().cast(cxx_type.this);
		}

		object(long addr) {
			this.ptr = jvmsp.pointer.at(addr);
		}

		public final Object access(String field_name) {
			return cxx_type.this.field(field_name).access(ptr.addr);
		}

		public final Object access(field f) {
			return f.access(ptr.addr);
		}
	}

	public static class jtypes {
		public static final cxx_type oop = define_primitive("oop", sizeof(unsigned_int));
	}
}

package jvmsp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * C++对象内存布局计算
 */
public class cxx_type {
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
	private ArrayList<cxx_field> base_fields;

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
	private long resolve_base_fields(ArrayList<cxx_field> arr, long current_offset) {
		for (int i = 0; i < base_types.length; ++i)// 基类字段放在最前方
			current_offset = base_types[i].resolve_base_fields(arr, current_offset);
		for (int i = 0; i < fields.size(); ++i) {
			cxx_field current_field = fields.get(i).clone();
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
	private ArrayList<cxx_field> fields;

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
	static final cxx_type define_primitive(String type, long size) {
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
	public cxx_field decl_field(cxx_field field) {
		if (is_primitive() || field.type().equals(this))// 禁止给原生类型添加字段，或添加类本身作为字段
			throw new RuntimeException("Cannot append field \"" + field + "\" to " + this + ". append fields to primitive types or append self as a field are not allowed.");
		fields.add(field);
		field.decl_type = this;
		this.dirty_flag = true;// 标记size更新
		return field;
	}

	public cxx_field decl_field(String name, cxx_type type) {
		return decl_field(cxx_field.define(name, type));
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
				cxx_field f = fields.get(idx);
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
	public cxx_field declared_field_at(int idx) {
		return is_primitive() ? null : fields.get(idx);
	}

	/**
	 * 获取本类声明的指定索引字段，包含基类字段
	 * 
	 * @param idx
	 * @return
	 */
	public cxx_field field_at(int idx) {
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

	public cxx_field declared_field(String field_name) {
		if (!is_primitive()) {
			for (int idx = 0; idx < fields.size(); ++idx) {// 优先使用派生类的字段
				cxx_field f = fields.get(idx);
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
	public cxx_field field(String field_name) {
		if (!is_primitive()) {
			for (int idx = 0; idx < fields.size(); ++idx) {// 优先使用派生类的字段
				cxx_field f = fields.get(idx);
				if (f.name().equals(field_name))
					return f;
			}
			for (int idx = 0; idx < base_fields.size(); ++idx) {
				cxx_field f = base_fields.get(idx);
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
		private static void print_fields_layout(long print_idx_as_from, ArrayList<cxx_field> fields, int type_element_size_in_tab, int number_element_size_in_tab) {
			for (int i = 0; i < fields.size(); ++i) {
				cxx_field f = fields.get(i);
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
}

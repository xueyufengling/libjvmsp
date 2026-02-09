package jvmsp;

import java.lang.annotation.Annotation;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import jvmsp.type.cxx_type.pointer;

public abstract class type
{
	public static enum lang
	{
		CXX, JAVA, UNKNOWN
	}

	/**
	 * 该类型所属的语言，关系对象模型
	 * 
	 * @return
	 */
	public lang lang()
	{
		return lang.UNKNOWN;
	}

	private boolean is_signed;

	/**
	 * 该类型是否是有符号的
	 * 
	 * @return
	 */
	public final boolean signed()
	{
		return is_signed;
	}

	protected long size;

	/**
	 * 更新本类型的标记
	 */
	private boolean dirty = false;

	/**
	 * 设置类型需要更新
	 */
	protected void mark_dirty()
	{
		dirty = true;
	}

	protected void resolve_type_info()
	{

	}

	/**
	 * 对本类型的成员信息进行更新，如果是primitive类型则不更新
	 * 
	 * @return
	 */
	public final type resolve()
	{
		if (!is_primitive && dirty)
		{
			dirty = false;
			resolve_type_info();
		}
		return this;
	}

	/**
	 * 该类型的完整大小，不含padding
	 * 
	 * @return
	 */
	public final long size()
	{
		resolve();
		return size;
	}

	/**
	 * 获取目标类型的大小
	 * 
	 * @param t
	 * @return
	 */
	public static final long sizeof(type t)
	{
		return t.size();
	}

	public static final long sizeof(type... ts)
	{
		long total_size = 0;
		for (type t : ts)
		{
			total_size += sizeof(t);
		}
		return total_size;
	}

	private String name;

	/**
	 * 获取类型名称
	 * 
	 * @return
	 */
	public String name()
	{
		return name;
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(name) ^ Objects.hashCode(size);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		return (obj instanceof type type) && this.name.equals(type.name);
	}

	private boolean is_primitive;

	/**
	 * 是否是基本类型
	 * 
	 * @return
	 */
	public final boolean is_primitive()
	{
		return is_primitive;
	}

	protected MemoryLayout memory_layout;

	public final MemoryLayout memory_layout()
	{
		resolve();
		return memory_layout;
	}

	public static enum memory_layout_type
	{
		PRIMITIVE_INT, PRIMITIVE_FLOAT, STRUCT;

		public static final MemoryLayout of(long size, memory_layout_type layout_type)
		{
			switch (layout_type)
			{
			case PRIMITIVE_INT:
				switch ((int) size)
				{
				case 1:
					return ValueLayout.JAVA_BYTE;
				case 2:
					return ValueLayout.JAVA_SHORT_UNALIGNED;
				case 4:
					return ValueLayout.JAVA_INT_UNALIGNED;
				case 8:
					return ValueLayout.JAVA_LONG_UNALIGNED;
				}
			case PRIMITIVE_FLOAT:
				switch ((int) size)
				{
				case 4:
					return ValueLayout.JAVA_FLOAT_UNALIGNED;
				case 8:
					return ValueLayout.JAVA_DOUBLE_UNALIGNED;
				}
			case STRUCT:
				return MemoryLayout.structLayout(MemoryLayout.sequenceLayout(size, ValueLayout.JAVA_BYTE)).withByteAlignment(1);
			}
			throw new java.lang.InternalError("memory layout type '" + layout_type + "' size cannot be " + size);
		}
	}

	protected type(String name, boolean is_primitive, boolean is_signed, long size, MemoryLayout memory_layout)
	{
		this.name = name;
		this.is_primitive = is_primitive;
		this.memory_layout = memory_layout;
		this.is_signed = is_signed;
		this.size = size;
	}

	/**
	 * 仅用于定义基本类型
	 * 
	 * @param name
	 * @param is_signed 只有基本类型有is_signed
	 * @param size
	 */
	protected type(String name, boolean is_signed, long size, MemoryLayout memory_layout)
	{
		this(name, true, is_signed, size, memory_layout);
	}

	protected type(String name, boolean is_signed, long size, memory_layout_type layout_type)
	{
		this(name, is_signed, size, memory_layout_type.of(size, layout_type));
	}

	/**
	 * 仅用于定义非基本类型
	 * 
	 * @param name
	 * @param size
	 */
	protected type(String name, long size)
	{
		this(name, false, false, size, null);
	}

	/**
	 * C++对象内存布局计算
	 */
	public static class cxx_type extends type implements Cloneable
	{
		public static final Object __access(long native_addr, cxx_type type)
		{
			if (type == jobject || type == jclass)
				return unsafe.read_reference(native_addr);
			switch ((int) type.size())
			{
			case 1:
				return unsafe.read_byte(native_addr);
			case 2:
				return unsafe.read_short(native_addr);
			case 4:
				if (type == _float || type == jfloat)
					return unsafe.read_float(native_addr);
				else
					return unsafe.read_int(native_addr);
			case 8:
				if (type == _double || type == jdouble)
					return unsafe.read_double(native_addr);
				else
					return unsafe.read_long(native_addr);
			default:
				return type.new object(native_addr);
			}
		}

		public static final class field implements Cloneable
		{
			/**
			 * 克隆字段，当分析继承结构需要修改offset时使用
			 */
			@Override
			public field clone()
			{
				try
				{
					return (field) super.clone();
				}
				catch (CloneNotSupportedException e)
				{
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public String toString()
			{
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
			public long offset()
			{
				return offset;
			}

			/**
			 * 字段名称
			 * 
			 * @return
			 */
			public String name()
			{
				return name;
			}

			/**
			 * 字段类型
			 * 
			 * @return
			 */
			public cxx_type type()
			{
				return type;
			}

			/**
			 * 该字段属于哪个类
			 * 
			 * @return
			 */
			public cxx_type decl_type()
			{
				return decl_type;
			}

			private field(String name, cxx_type type)
			{
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
			public static final field define(String name, cxx_type type)
			{
				return new field(name, type);
			}

			/**
			 * 访问一个基本类型字段并返回Java值
			 * 
			 * @param native_addr
			 * @return
			 */
			final Object access(long native_addr)
			{
				return __access(native_addr + offset, type);
			}
		}

		/**
		 * 所有定义的类型
		 */
		private static final HashMap<String, cxx_type> defined_types = new HashMap<>();

		/**
		 * 获取已定义的指定名称的C++类型
		 * 
		 * @param type_name
		 * @return
		 */
		public static cxx_type of(String type_name)
		{
			return defined_types.get(type_name);
		}

		public static cxx_type typedef(cxx_type type_alias, String type_name)
		{
			defined_types.put(type_name, type_alias);
			return type_alias;
		}

		@Override
		public cxx_type clone()
		{
			try
			{
				return (cxx_type) super.clone();
			}
			catch (CloneNotSupportedException e)
			{
				e.printStackTrace();
			}
			return null;
		}

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

		public long default_align_size()
		{
			resolve();
			return default_align_size;
		}

		/**
		 * pragma pack指定的对齐
		 */
		private long pragma_pack;

		public long pragma_pack()
		{
			resolve();
			return pragma_pack;
		}

		/**
		 * 最终的结构体整体对齐大小，结构体的size必定为此数的整数倍。实际上是{@code min(align_size, pragma_pack)}
		 */
		private long align_size;

		public long align_size()
		{
			resolve();
			return align_size;
		}

		/**
		 * 解析派生类的内存布局时，基类使用自己的pragma_pack
		 * 
		 * @param override_pragma_pack
		 * @return
		 */
		private long override_align_size(cxx_type override_pragma_pack)
		{
			return Math.min(align_size(), override_pragma_pack.pragma_pack);
		}

		/**
		 * 计算基类字段偏移
		 * 
		 * @param arr            字段数组
		 * @param current_offset 计算开始前的偏移量
		 * @return 计算完成后的末尾偏移量
		 */
		private long resolve_base_fields(ArrayList<field> arr, long current_offset)
		{
			for (int i = 0; i < base_types.length; ++i)// 基类字段放在最前方
				current_offset = base_types[i].resolve_base_fields(arr, current_offset);
			for (int i = 0; i < fields.size(); ++i)
			{
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
		 * 定义一个类型，禁止递归继承，且基类必须已经解析完成，即基类的size和字段不再变化。
		 * 
		 * @param type_name
		 * @param pragma_pack 对齐字节数
		 * @param base_types
		 * @return
		 */
		public static final cxx_type define(String type_name, long pragma_pack, cxx_type... base_types)
		{
			return new cxx_type(type_name, pragma_pack, base_types);
		}

		public static final cxx_type define(String type_name, cxx_type... base_types)
		{
			return define(type_name, Long.MAX_VALUE, base_types);
		}

		/**
		 * 仅用于定义基本类型
		 * 
		 * @param name
		 * @param size
		 */
		private cxx_type(String name, boolean is_signed, long size, MemoryLayout memory_layout)
		{
			super(name, is_signed, size, memory_layout);
			this.align_size = size;
			this.default_align_size = size;
			this.pragma_pack = size;
			defined_types.computeIfAbsent(name, (String t) -> this);
		}

		private cxx_type(String name, boolean is_signed, long size, memory_layout_type layout_type)
		{
			this(name, is_signed, size, memory_layout_type.of(size, layout_type));
		}

		private cxx_type(String name, long pragma_pack, cxx_type... base_types)
		{
			super(name, 0);
			this.pragma_pack = pragma_pack;
			this.fields = new ArrayList<>();
			this.base_types = base_types;
			this.base_fields = new ArrayList<>();
			this.derived_top = resolve_base_fields(base_fields, 0);
			mark_dirty();
			defined_types.computeIfAbsent(name, (String t) -> this);
		}

		/**
		 * 定义一个Java原生类型的长度，原生类型没有字段
		 * 
		 * @param type_name
		 * @param is_signed
		 * @param size
		 * @return
		 */
		private static final cxx_type define_primitive(String type_name, boolean is_signed, long size, MemoryLayout memory_layout)
		{
			return new cxx_type(type_name, is_signed, size, memory_layout);
		}

		/**
		 * 定义一个C/C++原生类型的长度，原生类型没有字段
		 * 
		 * @param type_name
		 * @param is_signed
		 * @param size
		 * @return
		 */
		private static final cxx_type define_primitive(String type_name, boolean is_signed, long size, memory_layout_type layout_type)
		{
			return new cxx_type(type_name, is_signed, size, layout_type);
		}

		/**
		 * 无符号基本类型
		 * 
		 * @param type_name
		 * @param size
		 * @return
		 */
		private static final cxx_type define_primitive(String type_name, long size, MemoryLayout memory_layout)
		{
			return define_primitive(type_name, false, size, memory_layout);
		}

		/**
		 * 添加字段，禁止添加类本身。
		 * 
		 * @param field
		 * @return
		 */
		public field decl_field(field field)
		{
			if (is_primitive() || field.type().equals(this))// 禁止给原生类型添加字段，或添加类本身作为字段
				throw new java.lang.InternalError("cannot append field \"" + field + "\" to " + this + ". append fields to primitive types or append self as a field are not allowed.");
			fields.add(field);
			field.decl_type = this;
			mark_dirty();// 标记size更新
			return field;
		}

		public field decl_field(String name, cxx_type type)
		{
			return decl_field(field.define(name, type));
		}

		/**
		 * 更新字段偏移量和本类型的大小
		 * 
		 * @return
		 */
		private void resolve_offset_and_size()
		{
			if (base_types.length == 0 && fields.isEmpty())
			{
				// 空结构体
				size = 1;
				align_size = 1;
				default_align_size = 1;
				pragma_pack = 1;
			}
			else
			{
				long current_offset = derived_top;
				for (int idx = 0; idx < fields.size(); ++idx)
				{
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
		}

		private void resolve_memory_layout()
		{
			memory_layout = MemoryLayout.sequenceLayout(size(), ValueLayout.JAVA_BYTE);// 对于C++类型均使用以字节为单位的SequenceLayout
		}

		/**
		 * 更新数据，包括对齐大小、偏移量、占用内存尺寸
		 * 
		 * @return
		 */
		@Override
		protected void resolve_type_info()
		{
			resolve_offset_and_size();
			resolve_memory_layout();// 生成Java FFI API使用的MemoryLayout
		}

		/**
		 * 获取本类声明的指定索引字段，不包含基类字段
		 * 
		 * @param idx
		 * @return
		 */
		public field declared_field_at(int idx)
		{
			return is_primitive() ? null : fields.get(idx);
		}

		/**
		 * 获取本类声明的指定索引字段，包含基类字段
		 * 
		 * @param idx
		 * @return
		 */
		public field field_at(int idx)
		{
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

		public int declared_field_index(String field_name)
		{
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
		public int field_index(String field_name)
		{
			if (!is_primitive())
			{
				for (int idx = 0; idx < fields.size(); ++idx)// 优先使用派生类的字段
					if (fields.get(idx).name().equals(field_name))
						return idx;
				for (int idx = 0; idx < base_fields.size(); ++idx)
					if (base_fields.get(idx).name().equals(field_name))// 派生类没有该字段则查找基类字段
						return idx;
			}
			return -1;
		}

		public field declared_field(String field_name)
		{
			if (!is_primitive())
			{
				for (int idx = 0; idx < fields.size(); ++idx)
				{
					// 优先使用派生类的字段
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
		public field field(String field_name)
		{
			if (!is_primitive())
			{
				for (int idx = 0; idx < fields.size(); ++idx)
				{// 优先使用派生类的字段
					field f = fields.get(idx);
					if (f.name().equals(field_name))
						return f;
				}
				for (int idx = 0; idx < base_fields.size(); ++idx)
				{
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
		private static final class mem_layout_printer
		{

			public static final int type_element_size_in_tab = 3;
			public static final int number_element_size_in_tab = 2;

			private static final int line_size_in_char(int type_element_size_in_tab, int number_element_size_in_tab)
			{
				return (1 + type_element_size_in_tab * 2 + number_element_size_in_tab * 2) * tab_size;
			}

			private static final int line_size_in_tab(int type_element_size_in_tab, int number_element_size_in_tab)
			{
				return (1 + type_element_size_in_tab * 2 + number_element_size_in_tab * 2);
			}

			private static final String print_mem_layout_split_line(int type_element_size_in_tab, int number_element_size_in_tab)
			{
				return "├" + "─".repeat(line_size_in_char(type_element_size_in_tab, number_element_size_in_tab) - 1) + "┤";
			}

			private static final String print_mem_layout_start_line(int type_element_size_in_tab, int number_element_size_in_tab)
			{
				return "┌" + "─".repeat(line_size_in_char(type_element_size_in_tab, number_element_size_in_tab) - 1) + "┐";
			}

			private static final String print_mem_layout_end_line(int type_element_size_in_tab, int number_element_size_in_tab)
			{
				return "└" + "─".repeat(line_size_in_char(type_element_size_in_tab, number_element_size_in_tab) - 1) + "┘";
			}

			private static final String header_text(String text, int type_element_size_in_tab, int number_element_size_in_tab)
			{
				int line_start_tab = type_element_size_in_tab + 1;
				return "│" + "\t".repeat(line_start_tab) + layout_info_element(text, line_size_in_tab(type_element_size_in_tab, number_element_size_in_tab) - line_start_tab) + "│";
			}

			/**
			 * @param print_idx_as_from 显示打印字段从哪个索引开始
			 * @param fields
			 */
			private static void print_fields_layout(long print_idx_as_from, ArrayList<field> fields, int type_element_size_in_tab, int number_element_size_in_tab)
			{
				for (int i = 0; i < fields.size(); ++i)
				{
					field f = fields.get(i);
					System.out.println("│ " + layout_info_element("" + (print_idx_as_from + i), 1) + layout_info_element("in: " + f.decl_type.toString(), type_element_size_in_tab) + layout_info_element(f.toString(), type_element_size_in_tab) + layout_info_element("offset: " + f.offset + ",", number_element_size_in_tab) + layout_info_element("size: " + sizeof(f.type()), number_element_size_in_tab) + "│");
				}
			}

			public static final int tab_size = 8;

			/**
			 * 制表符对齐
			 * 
			 * @param text
			 * @param size_in_tab_num
			 * @return
			 */
			private static String layout_info_element(String text, int size_in_tab_num)
			{
				int append_size = size_in_tab_num * tab_size - text.length();
				if (append_size <= 0)
					return text;
				int append_tab_num = append_size / tab_size;
				if (append_size % tab_size != 0)
					++append_tab_num;
				return text + "\t".repeat(append_tab_num);
			}

			public static final void print_mem_layout(cxx_type t)
			{
				print_mem_layout(t, type_element_size_in_tab, number_element_size_in_tab);
			}

			/**
			 * 打印内存布局相关信息
			 * 
			 * @param t                          要打印的目标类型
			 * @param type_element_size_in_tab   声明的类、字段描述占几个tab长度
			 * @param number_element_size_in_tab 偏移量、字段大小占几个tab长度
			 */
			public static final void print_mem_layout(cxx_type t, int type_element_size_in_tab, int number_element_size_in_tab)
			{
				System.out.println(print_mem_layout_start_line(type_element_size_in_tab, number_element_size_in_tab));
				System.out.println("│" + "\t".repeat(type_element_size_in_tab + 1) + layout_info_element(t.toString(), 3) + layout_info_element("alignment: " + t.align_size() + ",", 2) + layout_info_element("size: " + sizeof(t), 2) + "│");
				if (!t.is_primitive())
				{
					if (t.base_types.length != 0)
					{
						System.out.print("│" + "\t".repeat(type_element_size_in_tab + 1));
						String inherit_info = "Inherited: ";
						for (int i = 0; i < t.base_types.length; ++i)
						{
							inherit_info += t.base_types[i];
							if (i != t.base_types.length - 1)
								inherit_info += ", ";
						}
						System.out.print(layout_info_element(inherit_info, 7) + "│\n");
					}
					if (!t.base_fields.isEmpty())
					{
						System.out.println(print_mem_layout_split_line(type_element_size_in_tab, number_element_size_in_tab) + "\n" + header_text("** Base Fields **", type_element_size_in_tab, number_element_size_in_tab) + "\n" + print_mem_layout_split_line(type_element_size_in_tab, number_element_size_in_tab));
						print_fields_layout(0, t.base_fields, type_element_size_in_tab, number_element_size_in_tab);
					}
					if (!t.fields.isEmpty())
					{
						System.out.println(print_mem_layout_split_line(type_element_size_in_tab, number_element_size_in_tab) + "\n" + header_text("** Declared Fields **", type_element_size_in_tab, number_element_size_in_tab) + "\n" + print_mem_layout_split_line(type_element_size_in_tab, number_element_size_in_tab));
						print_fields_layout(t.base_fields.size(), t.fields, type_element_size_in_tab, number_element_size_in_tab);
					}
				}
				System.out.println(print_mem_layout_end_line(type_element_size_in_tab, number_element_size_in_tab));
			}
		}

		public void print_mem_layout(int type_element_size_in_tab, int number_element_size_in_tab)
		{
			mem_layout_printer.print_mem_layout(this, type_element_size_in_tab, number_element_size_in_tab);
		}

		public void print_mem_layout()
		{
			mem_layout_printer.print_mem_layout(this);
		}

		public static final cxx_type _char = cxx_type.define_primitive("char", true, 1, memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type unsigned_char = cxx_type.define_primitive("unsigned char", false, type.sizeof(_char), memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type _short = cxx_type.define_primitive("short", true, 2, memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type unsigned_short = cxx_type.define_primitive("unsigned short", false, type.sizeof(_short), memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type _int = cxx_type.define_primitive("int", true, 4, memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type unsigned_int = cxx_type.define_primitive("unsigned int", false, type.sizeof(_int), memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type bool = cxx_type.define_primitive("bool", false, type.sizeof(_int), memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type _long_long = cxx_type.define_primitive("long long", true, 8, memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type unsigned_long_long = cxx_type.define_primitive("unsigned long long", false, type.sizeof(_long_long), memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type _float = cxx_type.define_primitive("float", true, type.sizeof(_int), memory_layout_type.PRIMITIVE_FLOAT);
		public static final cxx_type _double = cxx_type.define_primitive("double", true, type.sizeof(_long_long), memory_layout_type.PRIMITIVE_FLOAT);
		public static final cxx_type _void = cxx_type.define_primitive("void", false, 0, (MemoryLayout) null);

		/**
		 * 无符号机器数据字
		 */
		public static final cxx_type WORD;

		static
		{
			if (virtual_machine.ON_64_BIT_JVM)
				WORD = cxx_type.define_primitive("WORD", false, 8, memory_layout_type.PRIMITIVE_INT);
			else
				WORD = cxx_type.define_primitive("WORD", false, 4, memory_layout_type.PRIMITIVE_INT);
		}

		public static final cxx_type int8_t = cxx_type.define_primitive("int8_t", true, 1, memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type uint8_t = cxx_type.define_primitive("uint8_t", false, type.sizeof(int8_t), memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type int16_t = cxx_type.define_primitive("int16_t", true, 2, memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type uint16_t = cxx_type.define_primitive("uint16_t", false, type.sizeof(int16_t), memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type int32_t = cxx_type.define_primitive("int32_t", true, 4, memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type uint32_t = cxx_type.define_primitive("uint32_t", false, type.sizeof(int32_t), memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type int64_t = cxx_type.define_primitive("int64_t", true, 8, memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type uint64_t = cxx_type.define_primitive("uint64_t", false, type.sizeof(int64_t), memory_layout_type.PRIMITIVE_INT);
		public static final cxx_type size_t = cxx_type.define_primitive("size_t", false, type.sizeof(uint64_t), memory_layout_type.PRIMITIVE_INT);

		public static class pointer_type extends cxx_type
		{
			/**
			 * 指针的类型
			 */
			cxx_type type;

			private pointer_type(cxx_type type)
			{
				super(type.name() + "*", false, WORD.size(), ValueLayout.ADDRESS);
				this.type = type;
			}

			private pointer_type(String type_name)
			{
				this(cxx_type.of(type_name));
			}

			public final cxx_type type()
			{
				return type;
			}

			public static final pointer_type of(String type_name)
			{
				return new pointer_type(type_name);
			}

			public static final pointer_type of(cxx_type type)
			{
				return new pointer_type(type);
			}
		}

		public static final pointer_type pvoid = pointer_type.of(_void);

		public static final cxx_type uintptr_t = cxx_type.define_primitive("uintptr_t", type.sizeof(pvoid), ValueLayout.ADDRESS);

		/**
		 * 用于将signed int类型储存的unsigned int值转换为unsigned long值。<br>
		 * 用法：{@code uint64_t addr = (int32_t) & UINT32_T_MASK;}
		 */
		public static final long UINT32_T_MASK = 0xFFFFFFFFL;

		public static final long uint_ptr(int oop_addr)
		{
			return oop_addr & UINT32_T_MASK;
		}

		public static final long UINT16_T_MASK = 0xFFFFL;

		public static final long uint_ptr(short s)
		{
			return s & UINT16_T_MASK;
		}

		public static final long UINT8_T_MASK = 0xFFL;

		public static final long uint_ptr(byte b)
		{
			return b & UINT8_T_MASK;
		}

		public static final long uint_ptr(char c)
		{
			return c & UINT8_T_MASK;
		}

		public static final int UINT8_T_MASK_I = 0xFF;

		public static final int uint8_t(byte b)
		{
			return b & UINT8_T_MASK_I;
		}

		public static final int uint8_t(char c)
		{
			return c & UINT8_T_MASK_I;
		}

		public static final int UINT16_T_MASK_I = 0xFFFF;

		public static final int uint16_t(short s)
		{
			return s & UINT16_T_MASK_I;
		}

		public static final long uint32_t(int i)
		{
			return i & UINT32_T_MASK;
		}

		/**
		 * C++对象操作
		 */
		public class object
		{
			final pointer ptr;

			object(pointer ptr)
			{
				this.ptr = ptr.copy().cast(cxx_type.this);
			}

			object(long addr)
			{
				this.ptr = pointer.at(addr);
			}

			public final Object access(String field_name)
			{
				return access(cxx_type.this.field(field_name));
			}

			public final Object access(field f)
			{
				return f.access(ptr.addr);
			}
		}

		public static final cxx_type oop = define_primitive("oop", sizeof(unsigned_int), ValueLayout.ADDRESS);
		public static final cxx_type jobject = define_primitive("jobject", sizeof(oop), ValueLayout.ADDRESS);
		public static final cxx_type jclass = define_primitive("jclass", sizeof(oop), ValueLayout.ADDRESS);
		public static final cxx_type jboolean = define_primitive("jboolean", false, sizeof(uint8_t), ValueLayout.JAVA_BOOLEAN);
		public static final cxx_type jbyte = define_primitive("jbyte", true, sizeof(int8_t), ValueLayout.JAVA_BYTE);
		public static final cxx_type jchar = define_primitive("jchar", false, sizeof(uint16_t), ValueLayout.JAVA_CHAR);
		public static final cxx_type jshort = define_primitive("jshort", true, sizeof(int16_t), ValueLayout.JAVA_SHORT);
		public static final cxx_type jint = define_primitive("jint", true, sizeof(int32_t), ValueLayout.JAVA_INT);
		public static final cxx_type jsize = define_primitive("jsize", true, sizeof(jint), ValueLayout.JAVA_INT);
		public static final cxx_type jlong = define_primitive("jlong", true, sizeof(int64_t), ValueLayout.JAVA_LONG);
		public static final cxx_type jfloat = define_primitive("jfloat", true, sizeof(_float), ValueLayout.JAVA_FLOAT);
		public static final cxx_type jdouble = define_primitive("jdouble", true, sizeof(_double), ValueLayout.JAVA_DOUBLE);

		public static final cxx_type of(Class<?> jtype)
		{
			if (jtype == byte.class)
				return jbyte;
			else if (jtype == char.class)
				return jchar;
			else if (jtype == boolean.class)
				return jboolean;
			else if (jtype == short.class)
				return jshort;
			else if (jtype == int.class)
				return jint;
			else if (jtype == float.class)
				return jfloat;
			else if (jtype == long.class)
				return jlong;
			else if (jtype == double.class)
				return jdouble;
			else if (jtype == void.class)
				return _void;
			else
				return jobject;
		}

		/**
		 * C++指针，使用机器的绝对内存地址<br>
		 * 不要用于取对象地址，短时间内可能不会出问题，但对象会随着GC过程移动，原先的地址会失效。<br>
		 * 主要配合memory使用，对分配的固定地址内存进行操作。
		 */
		public static class pointer
		{
			/**
			 * C++层的指针转换为(void*)(uint64_t)addr
			 */
			long addr;

			/**
			 * 指针所指向的C++类型
			 */
			cxx_type type;

			/**
			 * 指针算术运算的步长，与类型有关，以byte为单位
			 */
			long stride;

			/**
			 * 仅指针算术运算使用！
			 * 
			 * @param addr
			 * @param type
			 * @param stride
			 */
			private pointer(long addr, cxx_type type, long stride)
			{
				this.addr = addr;
				this.type = type;
				this.stride = stride;
			}

			/**
			 * C++对象指针
			 * 
			 * @param addr
			 * @param type
			 */
			private pointer(long addr, cxx_type type)
			{
				this(addr, type, type.size());
			}

			/**
			 * 仅拷贝构造指针使用！
			 * 
			 * @param addr
			 * @param java_type
			 * @param stride
			 * @param ptr_type_klass_word
			 */
			private pointer(pointer ptr)
			{
				this.addr = ptr.addr;
				this.type = ptr.type;
				this.stride = ptr.stride;
			}

			private pointer(long addr)
			{
				this(addr, _void);
			}

			public static long parse_address(String hex)
			{
				return Long.decode(hex.strip().toLowerCase());
			}

			private pointer(String hex)
			{
				this(parse_address(hex));
			}

			public static final pointer nullptr = pointer.at(address_of_jobject(null));

			public long address()
			{
				return addr;
			}

			public cxx_type type()
			{
				return type;
			}

			public cxx_type pointer_type()
			{
				return pointer_type.of(type);
			}

			public boolean is_nullptr()
			{
				return addr == 0;
			}

			/**
			 * 十六进制地址
			 */
			@Override
			public String toString()
			{
				return "0x" + ((addr >> 32 == 0) ? String.format("%08x", addr) : String.format("%016x", addr));
			}

			/**
			 * 将C/C++数组转换为Java数组
			 * 
			 * @param num
			 * @param clazz
			 * @return
			 */
			public final Object to_jarray(int num, Class<?> clazz)
			{
				if (clazz == void.class)
					throw new java.lang.InstantiationError("array type cannot be void");
				Object arr = Array.newInstance(clazz, num);
				if (clazz == byte.class)
				{
					byte[] a = ((byte[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (byte) this.add(idx).dereference();
					}
				}
				else if (clazz == boolean.class)
				{
					boolean[] a = ((boolean[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (boolean) this.add(idx).dereference();
					}
				}
				else if (clazz == short.class)
				{
					short[] a = ((short[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (short) this.add(idx).dereference();
					}
				}
				else if (clazz == char.class)
				{
					char[] a = ((char[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (char) this.add(idx).dereference();
					}
				}
				else if (clazz == int.class)
				{
					int[] a = ((int[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (int) this.add(idx).dereference();
					}
				}
				else if (clazz == float.class)
				{
					float[] a = ((float[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (float) this.add(idx).dereference();
					}
				}
				else if (clazz == long.class)
				{
					long[] a = ((long[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (long) this.add(idx).dereference();
					}
				}
				else if (clazz == double.class)
				{
					double[] a = ((double[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (double) this.add(idx).dereference();
					}
				}
				else
				{
					Object[] a = ((Object[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = this.add(idx).dereference();
					}
				}
				return arr;
			}

			/**
			 * 判断两个指针是否相同，只比较地址不比较类型。
			 */
			@Override
			public boolean equals(Object obj)
			{
				if (obj == this)
					return true;
				return (obj instanceof pointer ptr) && this.addr == ptr.addr;
			}

			@Override
			public int hashCode()
			{
				return Objects.hashCode(addr) ^ Objects.hashCode(type);
			}

			/**
			 * 把给定的地址和类型包装为指针
			 * 
			 * @param addr
			 * @param type
			 * @return
			 */
			public static final pointer at(long addr, cxx_type type)
			{
				return new pointer(addr, type);
			}

			public static final pointer at(long addr)
			{
				return new pointer(addr);
			}

			public static final pointer at(int _32bit_addr)
			{
				return new pointer(cxx_type.uint_ptr(_32bit_addr));
			}

			/**
			 * 将给定的十六进制地址和类型包装为指针
			 * 
			 * @param hex
			 * @param type
			 * @return
			 */
			public static final pointer at(String hex)
			{
				return new pointer(hex);
			}

			public pointer copy()
			{
				return new pointer(this);
			}

			/**
			 * 强制转换指针
			 * 
			 * @param dest_type
			 * @return
			 */
			public pointer cast(cxx_type dest_type)
			{
				this.type = dest_type;
				return this;
			}

			/**
			 * 指针赋值，只赋地址不赋类型。类型依然是原指针的类型
			 */
			public pointer assign(pointer ptr)
			{
				this.addr = ptr.addr;
				return this;
			}

			public pointer assign(long addr)
			{
				this.addr = addr;
				return this;
			}

			public pointer assign(String hex)
			{
				this.addr = parse_address(hex);
				return this;
			}

			/**
			 * 指针加法，返回一个新指针
			 * 
			 * @param step
			 * @return
			 */
			public pointer add(long step)
			{
				return new pointer(addr + stride * step, type, stride);
			}

			/**
			 * 指针的自增运算，改变的是指针自身的地址，并不返回新的指针拷贝
			 * 
			 * @param step
			 * @return
			 */
			public pointer inc(long step)
			{
				this.addr += stride * step;
				return this;
			}

			public pointer inc()
			{
				this.addr += stride;
				return this;
			}

			public pointer sub(long step)
			{
				return new pointer(addr - stride * step, type, stride);
			}

			public pointer dec(long step)
			{
				this.addr -= stride * step;
				return this;
			}

			public pointer dec()
			{
				this.addr -= stride;
				return this;
			}

			/**
			 * 获取对象的地址，返回long<br>
			 * 利用Object[]的元素为oop指针的事实来间接取地址，该地址为JVM内部相对地址，不一定是实际的绝对地址。该地址可直接用于InternalUnsafe的相关方法<br>
			 * 在32位和未启用UseCompressedOops的64位JVM上，取的地址直接就是绝对地址。<br>
			 * 在开启UseCompressedOops的64位JVM上，取的地址是相对偏移量，需要乘以字节对齐量（字节对齐默认为8）或者左移（3位）+相对偏移量为0的基地址（即nullptr的绝对地址）才是绝对地址。
			 * 
			 * @param _jobject
			 * @return
			 */
			static final long address_of_jobject(Object _jobject)
			{
				return unsafe.native_address_of(new Object[]
				{ _jobject }, unsafe.ARRAY_OBJECT_BASE_OFFSET);
			}

			/**
			 * 取对象地址，即andress_of_object()。基本类型都是by value传递参数入栈，取地址没意义，因此只能取对象的地址。<br>
			 * 如果要取对象的字段（可能是基本类型）的地址，使用本方法的其他重载方法。
			 * 
			 * @param jobject
			 * @return
			 */
			public static final pointer address_of(Object _jobject)
			{
				return pointer.at(address_of_jobject(_jobject), jobject);
			}

			public static final pointer address_of(cxx_type.object cxx_obj)
			{
				return cxx_obj.ptr.copy();
			}

			/**
			 * 因为基本类型都是by value传递参数入栈，取地址没意义，因此只能取类的字段地址
			 * 
			 * @param jobject
			 * @param field
			 * @return
			 */
			public static final pointer address_of(Object jobject, Field field)
			{
				if (Modifier.isStatic(field.getModifiers()))// 静态字段
					return pointer.at(address_of_jobject(unsafe.static_field_base(field)) + unsafe.static_field_offset(field), cxx_type.of(field.getType()));
				else
					return pointer.at(address_of_jobject(jobject) + unsafe.object_field_offset(field), cxx_type.of(field.getType()));
			}

			public static final pointer address_of(Object jobject, String field)
			{
				return address_of(jobject, reflection.find_field(jobject, field));
			}

			/**
			 * 对一个对象指针取引用
			 * 
			 * @param addr
			 * @return
			 */
			static final Object dereference_object(long addr)
			{
				Object[] __ref_fetch = new Object[1];
				unsafe.store_native_address(__ref_fetch, unsafe.ARRAY_OBJECT_BASE_OFFSET, addr);
				return __ref_fetch[0];
			}

			/**
			 * 取引用值
			 * 
			 * @return
			 */
			public Object dereference()
			{
				// 不可对void*类型的指针取值
				if (type == _void)
					throw new java.lang.IllegalAccessError("cannot dereference a void* pointer at " + this.toString());
				else
				{
					return __access(addr, type);
				}
			}

			public final void print_memory(long size)
			{
				pointer indicator = this.copy().cast(uint8_t);
				for (int i = 0; i < size; ++i, indicator.inc())
				{
					System.out.print(String.format("%02x", cxx_type.uint_ptr((byte) indicator.dereference())) + " ");
				}
			}
		}

		/**
		 * C++风格的引用，无需担心GC移动对象导致指针失效。
		 */
		public static class jobject_reference
		{
			Object base;
			long offset;

			Class<?> type;// 可以是基本类型
			long ref_type_klass_word;

			private jobject_reference(Object _jobject, long offset, Class<?> ref_type)
			{
				this.base = _jobject;
				this.offset = offset;
				this.type = ref_type;
			}

			long address_of_reference()
			{
				return pointer.address_of_jobject(base) + offset;
			}

			public Object value()
			{
				if (type == byte.class)
					return unsafe.read_byte(offset);
				else if (type == char.class)
					return unsafe.read_char(offset);
				else if (type == boolean.class)
					return unsafe.read_bool(offset);
				else if (type == short.class)
					return unsafe.read_short(offset);
				else if (type == int.class)
					return unsafe.read_int(offset);
				else if (type == float.class)
					return unsafe.read_float(offset);
				else if (type == long.class)
					return unsafe.read_long(offset);
				else if (type == double.class)
					return unsafe.read_double(offset);
				else
				{
					Object deref_obj = pointer.dereference_object(address_of_reference());
					java_type.set_klass_word(deref_obj, ref_type_klass_word);
					return deref_obj;
				}
			}

			public Class<?> type()
			{
				return type;
			}

			@Override
			public boolean equals(Object obj)
			{
				if (obj == this)
					return true;
				return (obj instanceof jobject_reference ref) && (this.base == ref.base) && (this.offset == ref.offset);
			}

			@Override
			public int hashCode()
			{
				return Objects.hashCode(base) ^ Objects.hashCode(offset) ^ Objects.hashCode(type);
			}

			/**
			 * 强制转引用类型
			 * 
			 * @param dest_type
			 * @return
			 */
			public jobject_reference cast(Class<?> dest_type)
			{
				this.type = dest_type;
				if (!java_type.is_primitive(dest_type))
				{
					// 每次cast()的时候更新目标对象的类型
					ref_type_klass_word = java_type.get_klass_word(dest_type);
				}
				return this;
			}

			/**
			 * 获取对象的引用，不能对ref_base取引用。
			 * 
			 * @param _jobject
			 * @return
			 */
			public static final jobject_reference reference_of(Object _jobject)
			{
				return new jobject_reference(_jobject, 0, _jobject.getClass());
			}

			/**
			 * 获取字段的引用，包括静态字段的引用
			 * 
			 * @param _jobject
			 * @param field
			 * @return
			 */
			public static final jobject_reference reference_of(Object _jobject, Field field)
			{
				if (Modifier.isStatic(field.getModifiers()))
					return new jobject_reference(unsafe.static_field_base(field), unsafe.static_field_offset(field), field.getType());
				else
					return new jobject_reference(_jobject, unsafe.object_field_offset(field), field.getType());
			}

			public static final jobject_reference reference_of(Object jobject, String field)
			{
				return reference_of(jobject, reflection.find_field(jobject, field));
			}

			/**
			 * 为引用的对象赋值，如果目标为对象，则只赋值其字段，不改变对象头。
			 * 
			 * @param v
			 * @return
			 */
			public jobject_reference assign(Object v)
			{
				if (type == byte.class)
					unsafe.write(base, offset, java_type.byte_value(v));
				else if (type == char.class)
					unsafe.write(base, offset, java_type.char_value(v));
				else if (type == boolean.class)
					unsafe.write(base, offset, java_type.boolean_value(v));
				else if (type == short.class)
					unsafe.write(base, offset, java_type.short_value(v));
				else if (type == int.class)
					unsafe.write(base, offset, java_type.int_value(v));
				else if (type == float.class)
					unsafe.write(base, offset, java_type.float_value(v));
				else if (type == long.class)
					unsafe.write(base, offset, java_type.long_value(v));
				else if (type == double.class)
					unsafe.write(base, offset, java_type.double_value(v));
				else
					unsafe.__memcpy(v, java_type.HEADER_BYTE_LENGTH, base, java_type.HEADER_BYTE_LENGTH, java_type.sizeof_object(v.getClass()) - java_type.HEADER_BYTE_LENGTH);// 只拷贝字段，不覆盖对象头
				return this;
			}
		}

		@Override
		public lang lang()
		{
			return lang.CXX;
		}
	}

	/**
	 * Java类型所占字节数
	 */
	public static abstract class java_type
	{
		/**
		 * 在32位JVM或64位JVM中UseCompressedOops开启的情况下，对象引用占4字节
		 */
		public static final long object_reference_size;

		static
		{
			object_reference_size = unsafe.OOP_SIZE;
		}

		/**
		 * 是否是基本类型
		 * 
		 * @param type
		 * @return
		 */
		public static final boolean is_primitive(Class<?> type)
		{
			return type.isPrimitive();
		}

		/**
		 * 是否是基本类型的Boxing Type
		 * 
		 * @param type
		 * @return
		 */
		public static final boolean is_primitive_boxing(Class<?> type)
		{
			return type == Integer.class || type == Long.class || type == Boolean.class || type == Double.class || type == Float.class || type == Byte.class || type == Short.class || type == Character.class || type == Void.class;
		}

		public static final boolean is_primitive_boxing(Object obj)
		{
			return is_primitive_boxing(obj.getClass());
		}

		/**
		 * 计算类型的大小，任何非基本类型均为引用类型。<br>
		 * 引用类型相当于指针，指向对象的实际内存。引用的值会因为GC而移动。
		 * 
		 * @param type
		 * @return
		 */
		public static final long sizeof(Class<?> type)
		{
			if (type == void.class)
				return 0;
			else if (type == byte.class || type == boolean.class)
				return 1;
			else if (type == short.class || type == char.class)
				return 2;
			else if (type == int.class || type == float.class)
				return 4;
			else if (type == long.class || type == double.class)
				return 8;
			else
				return object_reference_size;
		}

		/**
		 * 基本类型包装类的拆箱
		 * 
		 * @param b
		 * @return
		 */
		public static final byte byte_value(Object b)
		{
			return ((Number) b).byteValue();
		}

		public static final char char_value(Object c)
		{
			return ((Character) c).charValue();
		}

		public static final boolean boolean_value(Object bool)
		{
			return ((Boolean) bool).booleanValue();
		}

		public static final short short_value(Object s)
		{
			return ((Number) s).shortValue();
		}

		public static final int int_value(Object i)
		{
			return ((Number) i).intValue();
		}

		public static final float float_value(Object f)
		{
			return ((Number) f).floatValue();
		}

		public static final long long_value(Object l)
		{
			return ((Number) l).longValue();
		}

		public static final double double_value(Object d)
		{
			return ((Number) d).doubleValue();
		}

		private static final HashMap<Class<?>, Long> cached_size = new HashMap<>();

		/**
		 * Java对象所占用内存的大小，无对齐大小。每个Class<?>计算一次后将缓存。
		 * 
		 * @param type
		 * @return
		 */
		public static final long sizeof_object(Class<?> jtype)
		{
			return cached_size.computeIfAbsent(jtype, (Class<?> type) ->
			{
				long max_offset = 0;
				Class<?> max_offset_field_type = null;
				Field[] fields = reflection.find_declared_fields(type);
				for (Field f : fields)
				{
					if (!Modifier.isStatic(f.getModifiers()))
					{
						Class<?> field_type = f.getType();
						long current_field_offset = unsafe.object_field_offset(f);
						if (max_offset < current_field_offset)
						{
							max_offset = current_field_offset;
							max_offset_field_type = field_type;
						}
					}
				}
				return max_offset + sizeof(max_offset_field_type);
			});
		}

		/**
		 * 对齐的大小
		 * 
		 * @param size
		 * @return
		 */
		public static final long padding_size(int size)
		{
			if (size % 8 != 0)// 对象所占字节数必须是8的整数倍，如果不到则需要padding
				size = (size / 8 + 1) * 8;
			return size;
		}

		/**
		 * 在已实例化的对象上再次调用构造函数，不会设置对象头，仅初始化字段。父类的构造函数也会被调用。
		 * 
		 * @param jobject
		 * @param arg_types
		 * @param args
		 * @return
		 */
		public static final <T> T placement_new(T jobject, Class<?>[] arg_types, Object... args)
		{
			Class<?> target_type = jobject.getClass();
			MethodHandle constructor = symbols.constructor_method(target_type, arg_types);
			try
			{
				constructor.invoke(memory.cat(jobject, args));
			}
			catch (Throwable ex)
			{
				throw new java.lang.InternalError("placement new for " + target_type + " failed", ex);
			}
			return jobject;
		}

		@SuppressWarnings("unchecked")
		public static final <T> T copy(T jobject)
		{
			Class<T> clazz = (Class<T>) jobject.getClass();
			T o = unsafe.allocate(clazz);
			unsafe.__memcpy(jobject, HEADER_BYTE_LENGTH, o, HEADER_BYTE_LENGTH, java_type.sizeof_object(clazz) - HEADER_BYTE_LENGTH);// 只拷贝字段，不覆盖对象头
			return o;
		}

		/**
		 * OOP相关操作 https://github.com/openjdk/jdk/blob/9586817cea3f1cad8a49d43e9106e25dafa04765/src/hotspot/share/oops/compressedOops.cpp#L49<br>
		 * oop压缩相关常量。是否会运行时动态变更未知。<br>
		 * oop压缩指将绝对地址取相对于堆base的偏移量并位移构成一个32位的oop。<br>
		 * 对象头压缩/Klass压缩是指将对象头的Klass Word从64位压缩到32位的narrowKlass。<br>
		 * 开启UseCompressedOops后，默认开启Klass压缩，但oop是否压缩取决于分配的堆内存大小。
		 */

		/**
		 * 压缩模式
		 */
		public static enum oops_mode
		{
			UnscaledNarrowOop, // 无压缩
			ZeroBasedNarrowOop, // 压缩，基地址为0
			DisjointBaseNarrowOop, //
			HeapBasedNarrowOop;// 压缩，基地址非0
		};

		oops_mode mode;

		/**
		 * 最大堆内存大小
		 */
		public static final long max_heap_size;

		/**
		 * 堆内存末尾在内存中的绝对地址
		 */
		public static final long heap_space_end;

		/**
		 * 堆内存的起始地址
		 */
		public static final long base;

		/**
		 * 压缩oop时的位移
		 */
		public static final long shift;

		/**
		 * 堆内存相对地址范围
		 */
		public static final long heap_address_range;

		static
		{
			max_heap_size = virtual_machine.max_heap_size();
			heap_space_end = virtual_machine.HeapBaseMinAddress + max_heap_size;// 这是最大的范围，实际范围可能只是其中一段区间，这种方法或许并不准确。
			if (heap_space_end > virtual_machine.UnscaledOopHeapMax)
			{// 实际堆内存大小大于不压缩oop时支持的最大地址，则需要压缩oop，哪怕没启用UseCompressedOops也会自动开启压缩。
				shift = virtual_machine.OOP_ENCODE_ADDRESS_SHIFT;
			}
			else if (virtual_machine.UseCompressedOops)// 指定了UseCompressedOops后则必定压缩。
				shift = virtual_machine.OOP_ENCODE_ADDRESS_SHIFT;
			else// 堆内存的末尾绝对地址小于4GB就不压缩
				shift = 0;
			if (heap_space_end <= virtual_machine.OopEncodingHeapMax)
			{
				base = 0;
			}
			else
			{
				base = pointer.nullptr.address();// 这对吗？
			}
			heap_address_range = heap_space_end - base;
		}

		/**
		 * 编码压缩oop<br>
		 * oop.encode_heap_oop_not_null
		 * 
		 * @param native_addr
		 * @return
		 */
		public static final int encode_oop(long native_addr)
		{
			return (int) ((native_addr - base) >> shift);
		}

		public static final long pointer_delta(long native_addr)
		{
			return native_addr - base;
		}

		/**
		 * 解码压缩oop，位移可能为0，此时表示未压缩的相对于堆起始位置的相对地址.
		 * 
		 * @param oop_addr
		 * @return
		 */
		public static final long decode_oop(int oop_addr)
		{
			return ((oop_addr & cxx_type.UINT32_T_MASK) << shift) + base;
		}

		/**
		 * markWord
		 */

		private static abstract class __obj_header_base
		{
			// public static final void
		}

	// @formatter:off
	/**
	* 对象头的结构<br>
	* Object Header由Mark Word和Klass Word组成<br>
	* ObjectHeader 32-bit JVM<br>
	* |----------------------------------------------------------------------------------------|--------------------|<br>
	* |                                    Object Header (64 bits)                             |        State       |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |                  Mark Word (32 bits)                  |      Klass Word (32 bits)      |                    |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* | identity_hashcode:25 | age:4 | biased_lock:1 | lock:2 |      OOP to metadata object    |       Normal       |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |  thread:23 | epoch:2 | age:4 | biased_lock:1 | lock:2 |      OOP to metadata object    |       Biased       |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |               ptr_to_lock_record:30          | lock:2 |      OOP to metadata object    | Lightweight Locked |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |               ptr_to_heavyweight_monitor:30  | lock:2 |      OOP to metadata object    | Heavyweight Locked |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	* |                                              | lock:2 |      OOP to metadata object    |    Marked for GC   |<br>
	* |-------------------------------------------------------|--------------------------------|--------------------|<br>
	*/
	// @formatter:on
		@SuppressWarnings("unused")
		private static final class __32_bit extends __obj_header_base
		{
			// 32位JVM无OOP指针压缩
			public static final int HEADER_OFFSET = 0;
			public static final int HEADER_LENGTH = 64;

			public static final int MARKWORD_OFFSET = HEADER_OFFSET;
			public static final int MARKWORD_LENGTH = 32;
			public static final int KLASS_OFFSET = MARKWORD_OFFSET + MARKWORD_LENGTH;
			public static final int KLASS_LENGTH = 32;

			public static final int IDENTITY_HASHCODE_OFFSET = MARKWORD_OFFSET;
			public static final int IDENTITY_HASHCODE_LENGTH = 25;
			public static final int AGE_OFFSET = IDENTITY_HASHCODE_OFFSET + IDENTITY_HASHCODE_LENGTH;
			public static final int AGE_LENGTH = 4;
			public static final int BIASED_LOCK_OFFSET = AGE_OFFSET + AGE_LENGTH;
			public static final int BIASED_LOCK_LENGTH = 1;

			public static final int LOCK_OFFSET = BIASED_LOCK_OFFSET + BIASED_LOCK_LENGTH;
			public static final int LOCK_LENGTH = 2;

			public static final int THREAD_OFFSET = MARKWORD_OFFSET;
			public static final int THREAD_LENGTH = 23;
			public static final int EPOCH_OFFSET = THREAD_OFFSET + THREAD_LENGTH;
			public static final int EPOCH_LENGTH = 2;

			public static final int PTR_TO_LOCK_RECORD_OFFSET = MARKWORD_OFFSET;
			public static final int PTR_TO_LOCK_RECORD_LENGTH = 30;

			public static final int PTR_TO_HEAVYWEIGHT_MONITOR_OFFSET = MARKWORD_OFFSET;
			public static final int PTR_TO_HEAVYWEIGHT_MONITOR_LENGTH = 30;
		}

	// @formatter:off
	/**
	* ObjectHeader 64-bit JVM<br>
	* |------------------------------------------------------------------------------------------------------------|--------------------|<br>
	* |                                            Object Header (128 bits)                                        |        State       |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                                  Mark Word (64 bits)                         |    Klass Word (64 bits)     |                    |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* | unused:25 | identity_hashcode:31 | unused:1 | age:4 | biased_lock:1 | lock:2 |    OOP to metadata object   |       Normal       |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* | thread:54 |       epoch:2        | unused:1 | age:4 | biased_lock:1 | lock:2 |    OOP to metadata object   |       Biased       |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                       ptr_to_lock_record:62                         | lock:2 |    OOP to metadata object   | Lightweight Locked |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                     ptr_to_heavyweight_monitor:62                   | lock:2 |    OOP to metadata object   | Heavyweight Locked |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                                                                     | lock:2 |    OOP to metadata object   |    Marked for GC   |<br>
	* |------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	*/
	// @formatter:on
		@SuppressWarnings("unused")
		private static final class __64_bit_no_UseCompressedOops extends __obj_header_base
		{

			// 64位JVM无OOP指针压缩
			public static final int HEADER_OFFSET = 0;
			public static final int HEADER_LENGTH = 128;

			public static final int MARKWORD_OFFSET = HEADER_OFFSET;
			public static final int MARKWORD_LENGTH = 64;
			public static final int KLASS_OFFSET = MARKWORD_OFFSET + MARKWORD_LENGTH;
			public static final int KLASS_LENGTH = 64;

			public static final int UNUSED_1_NORMAL_OFFSET = MARKWORD_OFFSET;
			public static final int UNUSED_1_NORMAL_LENGTH = 25;
			public static final int IDENTITY_HASHCODE_OFFSET = UNUSED_1_NORMAL_OFFSET + UNUSED_1_NORMAL_LENGTH;
			public static final int IDENTITY_HASHCODE_LENGTH = 31;
			public static final int UNUSED_2_NORMAL_OFFSET = IDENTITY_HASHCODE_OFFSET + IDENTITY_HASHCODE_LENGTH;
			public static final int UNUSED_2_NORMAL_LENGTH = 1;
			public static final int AGE_OFFSET = UNUSED_2_NORMAL_OFFSET + UNUSED_2_NORMAL_LENGTH;
			public static final int AGE_LENGTH = 4;
			public static final int BIASED_LOCK_OFFSET = AGE_OFFSET + AGE_LENGTH;
			public static final int BIASED_LOCK_LENGTH = 1;
			public static final int LOCK_OFFSET = BIASED_LOCK_OFFSET + BIASED_LOCK_LENGTH;
			public static final int LOCK_LENGTH = 2;

			public static final int THREAD_OFFSET = MARKWORD_OFFSET;
			public static final int THREAD_LENGTH = 54;
			public static final int EPOCH_OFFSET = THREAD_OFFSET + THREAD_LENGTH;
			public static final int EPOCH_LENGTH = 2;
			public static final int UNUSED_1_BIASED_OFFSET = EPOCH_OFFSET + EPOCH_LENGTH;
			public static final int UNUSED_1_BIASED_LENGTH = 1;

			public static final int PTR_TO_LOCK_RECORD_OFFSET = MARKWORD_OFFSET;
			public static final int PTR_TO_LOCK_RECORD_LENGTH = 62;

			public static final int PTR_TO_HEAVYWEIGHT_MONITOR_OFFSET = MARKWORD_OFFSET;
			public static final int PTR_TO_HEAVYWEIGHT_MONITOR_LENGTH = 62;
		}

	// @formatter:off
	/** <br>
	* ObjectHeader 64-bit JVM UseCompressedOops=true<br>
	* |--------------------------------------------------------------------------------------------------------------|--------------------|<br>
	* |                                            Object Header (96 bits)                                           |        State       |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                                  Mark Word (64 bits)                           |    Klass Word (32 bits)     |                    |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* | unused:25 | identity_hashcode:31 | cms_free:1 | age:4 | biased_lock:1 | lock:2 |    OOP to metadata object   |       Normal       |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* | thread:54 |       epoch:2        | cms_free:1 | age:4 | biased_lock:1 | lock:2 |    OOP to metadata object   |       Biased       |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                         ptr_to_lock_record                            | lock:2 |    OOP to metadata object   | Lightweight Locked |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                     ptr_to_heavyweight_monitor                        | lock:2 |    OOP to metadata object   | Heavyweight Locked |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	* |                                                                       | lock:2 |    OOP to metadata object   |    Marked for GC   |<br>
	* |--------------------------------------------------------------------------------|-----------------------------|--------------------|<br>
	 */
	// @formatter:on
		@SuppressWarnings("unused")
		private static final class __64_bit_with_UseCompressedOops extends __obj_header_base
		{
			// 64位JVM开启OOP指针压缩，JVM默认是开启的
			public static final int HEADER_OFFSET = 0;
			public static final int HEADER_LENGTH = 96;

			public static final int MARKWORD_OFFSET = HEADER_OFFSET;
			public static final int MARKWORD_LENGTH = 64;
			public static final int KLASS_OFFSET = MARKWORD_OFFSET + MARKWORD_LENGTH;
			public static final int KLASS_LENGTH = 32;

			public static final int UNUSED_1_NORMAL_OFFSET = MARKWORD_OFFSET;
			public static final int UNUSED_1_NORMAL_LENGTH = 25;
			public static final int IDENTITY_HASHCODE_OFFSET = UNUSED_1_NORMAL_OFFSET + UNUSED_1_NORMAL_LENGTH;
			public static final int IDENTITY_HASHCODE_LENGTH = 31;
			public static final int CMS_FREE_OFFSET = IDENTITY_HASHCODE_OFFSET + IDENTITY_HASHCODE_LENGTH;
			public static final int CMS_FREE_LENGTH = 1;
			public static final int AGE_OFFSET = CMS_FREE_OFFSET + CMS_FREE_LENGTH;
			public static final int AGE_LENGTH = 4;
			public static final int BIASED_LOCK_OFFSET = AGE_OFFSET + AGE_LENGTH;
			public static final int BIASED_LOCK_LENGTH = 1;
			public static final int LOCK_OFFSET = BIASED_LOCK_OFFSET + BIASED_LOCK_LENGTH;
			public static final int LOCK_LENGTH = 2;

			public static final int THREAD_OFFSET = MARKWORD_OFFSET;
			public static final int THREAD_LENGTH = 54;
			public static final int EPOCH_OFFSET = THREAD_OFFSET + THREAD_LENGTH;
			public static final int EPOCH_LENGTH = 2;

			public static final int PTR_TO_LOCK_RECORD_OFFSET = MARKWORD_OFFSET;
			public static final int PTR_TO_LOCK_RECORD_LENGTH = 62;

			public static final int PTR_TO_HEAVYWEIGHT_MONITOR_OFFSET = MARKWORD_OFFSET;
			public static final int PTR_TO_HEAVYWEIGHT_MONITOR_LENGTH = 62;

		}

		public static final int INVALID_OFFSET = -1;
		public static final int INVALID_LENGTH = -1;

		/**
		 * Mark Word的长度，单位bit
		 */
		public static final int MARKWORD_LENGTH;

		/**
		 * Klass Word的偏移量，单位bit
		 */
		public static final int KLASS_WORD_OFFSET;

		/**
		 * Klass Word的长度，单位bit
		 */
		public static final int KLASS_WORD_LENGTH;

		public static final int HEADER_LENGTH;

		/**
		 * Mark Word的长度，单位byte
		 */
		public static final int MARKWORD_BYTE_LENGTH;

		/**
		 * Klass Word的偏移量，单位byte
		 */
		public static final int KLASS_WORD_BYTE_OFFSET;

		/**
		 * Klass Word的长度，单位byte
		 */
		public static final int KLASS_WORD_BYTE_LENGTH;

		/**
		 * header总长度
		 */
		public static final int HEADER_BYTE_LENGTH;

		static
		{
			if (virtual_machine.NATIVE_JVM_BIT_VERSION == 32)
			{
				MARKWORD_LENGTH = __32_bit.MARKWORD_LENGTH;
				KLASS_WORD_OFFSET = __32_bit.KLASS_OFFSET;
				KLASS_WORD_LENGTH = __32_bit.KLASS_LENGTH;
				HEADER_LENGTH = __32_bit.HEADER_LENGTH;
			}
			else if (virtual_machine.NATIVE_JVM_BIT_VERSION == 64)
			{
				if (virtual_machine.UseCompressedOops)
				{
					MARKWORD_LENGTH = __64_bit_with_UseCompressedOops.MARKWORD_LENGTH;
					KLASS_WORD_OFFSET = __64_bit_with_UseCompressedOops.KLASS_OFFSET;
					KLASS_WORD_LENGTH = __64_bit_with_UseCompressedOops.KLASS_LENGTH;
					HEADER_LENGTH = __64_bit_with_UseCompressedOops.HEADER_LENGTH;
				}
				else
				{
					MARKWORD_LENGTH = __64_bit_no_UseCompressedOops.MARKWORD_LENGTH;
					KLASS_WORD_OFFSET = __64_bit_no_UseCompressedOops.KLASS_OFFSET;
					KLASS_WORD_LENGTH = __64_bit_no_UseCompressedOops.KLASS_LENGTH;
					HEADER_LENGTH = __64_bit_no_UseCompressedOops.HEADER_LENGTH;
				}
			}
			else
			{
				throw new java.lang.InternalError("unknown native jvm bit-version '" + virtual_machine.NATIVE_JVM_BIT_VERSION + "'");
			}
			MARKWORD_BYTE_LENGTH = MARKWORD_LENGTH / 8;
			KLASS_WORD_BYTE_OFFSET = KLASS_WORD_OFFSET / 8;
			KLASS_WORD_BYTE_LENGTH = KLASS_WORD_LENGTH / 8;
			HEADER_BYTE_LENGTH = HEADER_LENGTH / 8;
		}

		public static final long get_klass_word(Class<?> c)
		{
			return get_klass_word(unsafe.allocate(c));
		}

		/**
		 * 获取对象头
		 * 
		 * @param obj
		 * @return
		 */
		public static final long get_klass_word(Object obj)
		{
			if (KLASS_WORD_LENGTH == 32)
				return unsafe.read_int(obj, KLASS_WORD_BYTE_OFFSET);
			else if (KLASS_WORD_LENGTH == 64)
				return unsafe.read_long(obj, KLASS_WORD_BYTE_OFFSET);
			else
				throw new java.lang.InternalError("get klass word of '" + obj + "' failed");
		}

		/**
		 * 强制改写对象头
		 * 
		 * @param obj
		 * @param klass_word
		 * @return
		 */
		public static final void set_klass_word(Object obj, long klass_word)
		{
			if (KLASS_WORD_LENGTH == 32)
			{
				unsafe.write(obj, KLASS_WORD_BYTE_OFFSET, (int) klass_word);
			}
			else if (KLASS_WORD_LENGTH == 64)
			{
				unsafe.write(obj, KLASS_WORD_BYTE_OFFSET, klass_word);
			}
			else
				throw new java.lang.InternalError("set klass word of '" + obj + "' failed");
		}

		public static final void set_klass_word(long obj_base, long klass_word)
		{
			if (KLASS_WORD_LENGTH == 32)
			{
				unsafe.write(null, obj_base + KLASS_WORD_BYTE_OFFSET, (int) klass_word);
			}
			else if (KLASS_WORD_LENGTH == 64)
			{
				unsafe.write(null, obj_base + KLASS_WORD_BYTE_OFFSET, klass_word);
			}
			else
				throw new java.lang.InternalError("set klass word of '" + obj_base + "' to '" + klass_word + "' failed");
		}

		public static final Object cast(Object obj, long cast_type_klass_word)
		{
			set_klass_word(obj, cast_type_klass_word);
			return obj;
		}

		public static final Object cast(Object obj, Object cast_type_obj)
		{
			return cast(obj, get_klass_word(cast_type_obj));
		}

		public static final Object cast(Object obj, Class<?> cast_type)
		{
			return cast(obj, get_klass_word(cast_type));
		}

		public static final Object cast(Object obj, String cast_type)
		{
			return cast(obj, get_klass_word(cast_type));
		}

		@SuppressWarnings("unchecked")
		public static final <_T> _T safe_cast(Object obj, _T cast_type_obj)
		{
			return safe_cast(obj, (Class<_T>) cast_type_obj.getClass());
		}

		/**
		 * 安全的强制转换，没有继承关系的独立类的转换会抛出Exception。<br>
		 * 主要用于Mixin。<br>
		 * 
		 * @param obj
		 * @param cast_type
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static final <_T> _T safe_cast(Object obj, Class<_T> cast_type)
		{
			return (_T) (Object) obj;
		}

		@SuppressWarnings("unchecked")
		public static final <T> T undefined(long cast_type_klass_word)
		{
			return (T) java_type.cast(new Object(), cast_type_klass_word);
		}

		/**
		 * 用于作为Object类型的static final变量初始值，防止变量字面值或null值被内联
		 * 
		 * @param <T>
		 * @param klass
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static final <T> T undefined(T dest_type_obj)
		{
			return (T) java_type.cast(new Object(), dest_type_obj);
		}

		@SuppressWarnings("unchecked")
		public static final <T> T undefined(Object obj, Class<?> cast_type)
		{
			return (T) java_type.cast(new Object(), cast_type);
		}

		@SuppressWarnings("unchecked")
		public static final <T> T undefined(Object obj, String cast_type)
		{
			return (T) java_type.cast(new Object(), cast_type);
		}

		/**
		 * 防止Object类型的static final变量初始null字面值被内联<br>
		 * 当跨类修改目标类字段，且static final Object被初始化为null字面值时：如果不在修改之前在本类使用这个变量，那么这个值的修改就不会成功（会被内联）。
		 * 
		 * @param var
		 * @return
		 */
		public static final void not_inlined(Object var)
		{

		}

		/**
		 * 任何枚举类型的占位符
		 */
		public static enum enum_placeholder
		{
			Null;

			/**
			 * 将该占位符转换为实际的枚举类型值
			 * 
			 * @param <T>
			 * @param target_class
			 * @return
			 */
			@SuppressWarnings("unchecked")
			public final <T extends Enum<T>> T as(Class<T> target_class)
			{
				return (T) cast(this, target_class);
			}

			/**
			 * 将一个枚举类型值包装为占位符
			 * 
			 * @param <T>
			 * @param enumeration
			 * @return
			 */
			public static final <T extends Enum<T>> enum_placeholder pack(T enumeration)
			{
				return (enum_placeholder) cast(enumeration, enum_placeholder.class);
			}
		}

		/**
		 * 用于lambda表达式内更改表达式外的局部变量值
		 * 
		 * @param <T>
		 */
		public static final class wrapper<T>
		{
			public T value;

			public wrapper(T value)
			{
				this.value = value;
			}

			public static final <T> wrapper<T> wrap(T value)
			{
				return new wrapper<T>(value);
			}

			@SuppressWarnings(
			{ "rawtypes", "unchecked" })
			public static final wrapper wrap()
			{
				return new wrapper(null);
			}
		}

		/**
		 * 父类实现此接口，即可在父类中使用子类Class<_Derived>对象
		 * 
		 * @param <_Derived>
		 */
		public static interface _crtp<_Derived>
		{
			@SuppressWarnings("unchecked")
			public default Class<_Derived> derived_class()
			{
				return (Class<_Derived>) this.getClass();
			}
		}

		/**
		 * 通过定义的枚举的构造函数类型获取实际的构造函数类型。<br>
		 * 这是因为编译器会在枚举类的构造函数声明的构造函数参数最前方自动添加两个额外参数，如果不加入这两个额外参数，就找不到枚举的构造函数。<br>
		 * 自动添加的两个参数是String name：枚举值的字符串名称、int ordinal枚举值的序号，从0开始计数。
		 * 
		 * @param ctor_arg_types
		 * @return
		 */
		private static Class<?>[] _enum_constructor_arg_types(Class<?>... ctor_arg_types)
		{
			return memory.cat(String.class, int.class, ctor_arg_types);
		}

		/**
		 * 获取enum内部定义的枚举值数组，此为原始数组，对该数组的任何修改都会反映到Enum的相关方法
		 * 
		 * @param <_T>
		 * @param target_enum
		 * @return
		 */
		public static <_T extends Enum<_T>> VarHandle __enum_values(Class<_T> target_enum)
		{
			return symbols.find_static_var(target_enum, "ENUM$VALUES", target_enum.arrayType());
		}

		/**
		 * 为目标枚举设置values()
		 * 
		 * @param <_T>
		 * @param target_enum
		 * @param values
		 */
		@SuppressWarnings("unchecked")
		public static <_T extends Enum<_T>> void set_enum_values(Class<_T> target_enum, _T... values)
		{
			__enum_values(target_enum).set(null, values);
		}

		public static <_T extends Enum<_T>> _T[] get_enum_values(Class<_T> target_enum)
		{
			return (_T[]) __enum_values(target_enum).get(null);
		}

		/**
		 * 修改枚举、创建新的枚举值实例
		 * 
		 * @param <_T>
		 */
		public static interface mutable_enum<_T extends Enum<_T>> extends _crtp<_T>
		{
			public default _T of(Class<?>[] arg_types, Object... args)
			{
				return of("$tmp", -1, arg_types, args);
			}

			public default _T of(String name, int ordinal, Class<?>[] arg_types, Object... args)
			{
				return of(this.derived_class(), name, ordinal, arg_types, args);
			}

			/**
			 * 新建一个枚举值实例，该枚举值为自由对象，没有被添加到枚举类的values()中。
			 * 
			 * @param <_T>
			 * @param target_enum
			 * @param arg_types
			 * @param args
			 * @return
			 */
			@SuppressWarnings("unchecked")
			public static <_T extends Enum<_T>> _T of(Class<_T> target_enum, String name, int ordinal, Class<?>[] arg_types, Object... args)
			{
				MethodHandle constructor = symbols.find_constructor(target_enum, _enum_constructor_arg_types(arg_types));
				try
				{
					return (_T) constructor.invokeWithArguments(memory.cat(name, ordinal, args));
				}
				catch (Throwable ex)
				{
					ex.printStackTrace();
				}
				return null;
			}

			public static <_T extends Enum<_T>> _T of(Class<_T> target_enum, Class<?>[] arg_types, Object... args)
			{
				return of(target_enum, "$tmp", -1, arg_types, args);
			}
		}

		/**
		 * 多重继承解决方案<br>
		 * 将所有接口所需字段定义到接口的内部类，这个内部类实例可以绑定到一个接口对象上，接口通过definition()方法获取字段内部类实例。
		 * 
		 * @param <_Def>
		 */
		public static interface base<_Def extends base.definition<? extends base<?>>>
		{
			/**
			 * 接口内部类都是隐式public static final嵌套类<br>
			 * 接口字段定义父类
			 * 
			 * @param <_Derived>
			 */
			abstract class definition<_Derived extends base<? extends definition<?>>>
			{
				/**
				 * (derived_obj, base_type)->base_type_definition
				 */
				private static final HashMap<base<?>, HashMap<Class<?>, Object>> definitions = new HashMap<>();

				public final _Derived this_;

				private static Field this_field;

				/**
				 * 初始化对象时使用
				 */
				private static final HashMap<definition<?>, base<?>> this_preinit_refs = new HashMap<>();

				static
				{
					this_field = reflection.find_declared_field(definition.class, "this_");
				}

				@SuppressWarnings("unchecked")
				protected definition()
				{
					this_ = (_Derived) this_preinit_refs.remove(this);// 在执行子类构造函数之前先设置好子类的this_引用
				}

				/**
				 * 将本实例绑定在一个base对象上
				 * 
				 * @param obj
				 * @return
				 */
				@SuppressWarnings(
				{ "rawtypes" })
				public final definition move(_Derived obj)
				{
					Class<?> base_type = this.getClass();
					if (this_ != null)
					{
						HashMap<Class<?>, Object> orig_base_defs = definition.definitions.computeIfAbsent(this_, (base) -> new HashMap<>());
						orig_base_defs.remove(base_type);// 从原绑定对象移除该实例
					}
					reflection.write(this, this_field, obj);
					HashMap<Class<?>, Object> new_base_defs = definition.definitions.computeIfAbsent(obj, (base) -> new HashMap<>());
					new_base_defs.put(base_type, this);// 将指定基类定义加入Map
					return this;
				}
			}

			default _Def construct(Class<_Def> base_type, Class<?>[] arg_types, Object... args)
			{
				_Def def = unsafe.allocate(base_type);// 先分配对象内存
				definition.this_preinit_refs.put(def, this);// 为目标对象指定this_引用
				try
				{
					def = placement_new(def, arg_types, args);// 调用目标构造函数
				}
				catch (Throwable ex)
				{
					ex.printStackTrace();
				}
				definition.definitions.computeIfAbsent(this, (base) -> new HashMap<>()).put(base_type, def);
				return def;
			}

			@SuppressWarnings("unchecked")
			default _Def construct(Object base_type, Class<?>[] arg_types, Object... args)
			{
				return construct((Class<_Def>) base_type, arg_types, args);
			}

			/**
			 * 子类需要有正确的Definition对象
			 * 
			 * @return
			 */
			@SuppressWarnings("unchecked")
			default _Def definition(Class<?> type)
			{
				return (_Def) definition.definitions.get(this).get(type);
			}

			@SuppressWarnings("unchecked")
			default _Def definition()
			{
				HashMap<Class<?>, Object> base_defs = definition.definitions.get(this);
				if (base_defs.size() == 1)
					return (_Def) base_defs.values().iterator().next();// 只继承了一个base则直接返回其字段定义实例
				else
					throw new IllegalArgumentException("Class " + this.getClass() + " have multipe base class, specify the target base class type.");
			}
		}

		/**
		 * 遍历类的工具，无视访问修饰符和反射过滤<br>
		 * 提供原始Field、Method、Constructor等，对其进行修改会导致反射获取到的所有副本都被修改
		 */

		@FunctionalInterface
		public static interface field_operation<_F>
		{
			/**
			 * 遍历每个字段，处理的是root原对象，即反射缓存的对象。
			 * 
			 * @param f
			 * @param is_static 目标字段是否是静态的
			 * @param value     字段值，无效则为null
			 * @return 是否继续迭代，返回true代表继续迭代，false则终止迭代
			 */
			public boolean operate(Field f, boolean is_static, _F value);

			@FunctionalInterface
			public static interface simple<_F>
			{
				/**
				 * 遍历每个字段，处理的是root原对象，即反射缓存的对象。
				 * 
				 * @param f
				 * @param is_static 目标字段是否是静态的
				 * @param value     字段值，无效则为null
				 */
				public boolean operate(String field_name, Class<?> field_type, boolean is_static, _F value);
			}

			@FunctionalInterface
			public static interface annotated<_F, _T extends Annotation>
			{
				/**
				 * 遍历每个具有某注解的字段
				 * 
				 * @param f
				 * @param is_static 目标字段是否是静态的
				 * @param value     字段值，无效则为null
				 */
				public boolean operate(Field f, boolean is_static, _F value, _T annotation);

				@FunctionalInterface
				public static interface simple<_F, _T extends Annotation>
				{
					/**
					 * 遍历每个具有某注解的字段
					 * 
					 * @param f
					 * @param is_static 目标字段是否是静态的
					 * @param value     字段值，无效则为null
					 */
					public boolean operate(String field_name, Class<?> field_type, boolean is_static, _F value, _T annotation);
				}
			}

			@FunctionalInterface
			public static interface generic<_F>
			{
				/**
				 * 遍历具有单个泛型参数的字段
				 * 
				 * @param f
				 * @param is_static 目标字段是否是静态的
				 * @param value     字段值，无效则为null
				 * @return 是否继续迭代，返回true代表继续迭代，false则终止迭代
				 */
				public boolean operate(Field f, boolean is_static, Class<?> genericType, _F value);
			}
		}

		/**
		 * op()中形参value为字段值<br>
		 * 字段如果是静态的，则传入值；如果是非静态字段则传入target的该字段值（若target为Class<?>则表示无对象，传入null）
		 * 
		 * @param obj
		 * @param op
		 */
		@SuppressWarnings(
		{ "unchecked", "rawtypes" })
		public static final <_F> void walk_fields(Object target, field_operation<_F> op)
		{
			Class<?> clazz;
			Object obj;
			if (target instanceof Class c)
			{
				clazz = c;
				obj = null;
			}
			else
			{
				clazz = target.getClass();
				obj = target;
			}
			Field[] fields = reflection.find_declared_fields(clazz);
			field_operation rop = (field_operation) op;
			for (Field f : fields)
			{
				boolean is_static = Modifier.isStatic(f.getModifiers());
				if (!rop.operate(f, is_static, is_static ? reflection.read(clazz, f) : (obj == null ? null : reflection.read(obj, f))))
					return;
			}
		}

		@SuppressWarnings(
		{ "rawtypes", "unchecked" })
		public static final <_F> void walk_fields(Object target, field_operation.simple<_F> op)
		{
			field_operation.simple rop = (field_operation.simple) op;
			walk_fields(target, (Field f, boolean is_static, Object value) ->
			{
				return rop.operate(f.getName(), f.getType(), is_static, value);
			});
		}

		/**
		 * 遍历含有某个注解的全部字段
		 * 
		 * @param clazz
		 * @param annotation
		 * @param op
		 */
		@SuppressWarnings(
		{ "unchecked", "rawtypes" })
		public static final <_F, _T extends Annotation> void walk_fields(Object target, Class<_T> annotation_clazz, field_operation.annotated<_F, _T> op)
		{
			field_operation.annotated rop = (field_operation.annotated) op;
			walk_fields(target, (Field f, boolean is_static, Object value) ->
			{
				_T annotation = f.getAnnotation(annotation_clazz);
				if (annotation != null)
					return rop.operate(f, is_static, value, annotation);
				return true;
			});
		}

		@SuppressWarnings(
		{ "unchecked", "rawtypes" })
		public static final <_F, _T extends Annotation> void walk_fields(Object target, Class<_T> annotation_clazz, field_operation.annotated.simple<_F, _T> op)
		{
			field_operation.annotated.simple rop = (field_operation.annotated.simple) op;
			walk_fields(target, annotation_clazz, (Field f, boolean is_static, Object value, _T annotation) ->
			{
				return rop.operate(f.getName(), f.getType(), is_static, value, annotation);
			});
		}

		/**
		 * 遍历指定类的目标类型或其子类的字段
		 * 
		 * @param <_T>
		 * @param clazz
		 * @param targetType
		 * @param op
		 */
		@SuppressWarnings("unchecked")
		public static final <_T> void walk_fields(Object target, Class<_T> targetType, field_operation<_T> op)
		{
			walk_fields(target, (Field f, boolean is_static, Object value) ->
			{
				if (reflection.is(f, targetType))
					return op.operate(f, is_static, (_T) value);
				return true;
			});
		}

		@SuppressWarnings(
		{ "rawtypes", "unchecked" })
		public static final <_F> void walk_fields(Object target, Class<_F> field_type, field_operation.generic<_F> op)
		{
			field_operation.generic rop = (field_operation.generic) op;
			walk_fields(target, field_type, (Field f, boolean is_static, _F value) ->
			{
				return rop.operate(f, is_static, reflection.first_generic_class(f), value);
			});
		}

		/**
		 * 遍历target中全部第一个泛型参数为single_generic_type的field_type类型的字段
		 * 
		 * @param <_F>
		 * @param target
		 * @param field_type
		 * @param single_generic_type
		 * @param op
		 */
		public static final <_F, _G> void walk_fields(Object target, Class<_F> field_type, Class<_G> single_generic_type, field_operation<_F> op)
		{
			walk_fields(target, field_type, (Field f, boolean is_static, Class<?> genericType, _F value) ->
			{
				if (reflection.is(genericType, single_generic_type))
				{
					return op.operate(f, is_static, (_F) value);
				}
				return true;
			});
		}

		@FunctionalInterface
		public static interface method_operation<_M>
		{
			/**
			 * 遍历每个方法，处理的是root原对象，即反射缓存的对象。
			 * 
			 * @param m
			 * @param is_static 目标字段是否是静态的
			 * @param value     方法所属对象实例，静态方法则为null
			 */
			public boolean operate(Method m, boolean is_static, _M obj);

			@FunctionalInterface
			public static interface annotated<_M, _T extends Annotation>
			{
				/**
				 * 遍历每个具有某注解的方法
				 * 
				 * @param m
				 * @param is_static 目标字段是否是静态的
				 * @param value     方法所属对象实例，静态方法则为null
				 */
				public boolean operate(Method m, boolean is_static, _M obj, _T annotation);
			}
		}

		@SuppressWarnings(
		{ "unchecked", "rawtypes" })
		public static final <_M> void walk_methods(Object target, method_operation<_M> op)
		{
			Class<?> clazz;
			Object obj;
			if (target instanceof Class c)
			{
				clazz = c;
				obj = null;
			}
			else
			{
				clazz = target.getClass();
				obj = target;
			}
			Method[] methods = reflection.find_declared_methods(clazz);
			method_operation rop = (method_operation) op;
			for (Method m : methods)
			{
				boolean is_static = Modifier.isStatic(m.getModifiers());
				if (!rop.operate(m, is_static, obj))
					return;
			}
		}

		@SuppressWarnings(
		{ "unchecked", "rawtypes" })
		public static final <_M, _T extends Annotation> void walk_methods(Object target, Class<_T> annotation_clazz, method_operation.annotated<_M, _T> op)
		{
			method_operation.annotated rop = (method_operation.annotated) op;
			walk_methods(target, (Method m, boolean is_static, Object obj) ->
			{
				_T annotation = m.getAnnotation(annotation_clazz);
				if (annotation != null)
					return rop.operate(m, is_static, obj, annotation);
				return true;
			});
		}

		@FunctionalInterface
		public static interface constructor_operation<_C>
		{
			/**
			 * 遍历每个构造函数，处理的是root原对象，即反射缓存的对象。
			 * 
			 * @param c
			 */
			public boolean operate(Constructor<_C> c);

			@FunctionalInterface
			public static interface annotated<_C, _T extends Annotation>
			{
				/**
				 * 遍历每个具有某注解的构造函数
				 * 
				 * @param c
				 * @param annotation
				 */
				public boolean operate(Constructor<_C> c, _T annotation);
			}
		}

		public static final <_C> void walk_constructors(Class<_C> clazz, constructor_operation<_C> op)
		{
			Constructor<_C>[] constructors = reflection.__find_declared_constructors(clazz);
			for (Constructor<_C> c : constructors)
			{
				if (!op.operate(c))
					return;
			}
		}

		public static final <_C, _T extends Annotation> void walk_constructors(Class<_C> clazz, Class<_T> annotation_clazz, constructor_operation.annotated<_C, _T> op)
		{
			walk_constructors(clazz, (Constructor<_C> c) ->
			{
				_T annotation = c.getAnnotation(annotation_clazz);
				if (annotation != null)
					return op.operate(c, annotation);
				return true;
			});
		}

		@FunctionalInterface
		public static interface executable_operation<_E>
		{
			/**
			 * 遍历每个方法或构造函数，处理的是root原对象，即反射缓存的对象。
			 * 
			 * @param e
			 * @param is_static 目标字段是否是静态的
			 * @param value     字段值，无效则为null
			 */
			public boolean operate(Executable e, boolean is_static, _E obj);
		}

		@SuppressWarnings(
		{ "rawtypes", "unchecked" })
		public static final <_E> void walk_executables(Object target, executable_operation<_E> op)
		{
			Class<?> clazz;
			Object obj;
			if (target instanceof Class c)
			{
				clazz = c;
				obj = null;
			}
			else
			{
				clazz = target.getClass();
				obj = target;
			}
			Method[] methods = reflection.find_declared_methods(clazz);
			Constructor<?>[] constructors = reflection.__find_declared_constructors(clazz);
			executable_operation rop = (executable_operation) op;
			for (Constructor<?> c : constructors)
			{
				if (!rop.operate(c, Modifier.isStatic(c.getModifiers()), obj))
					return;
			}
			for (Method m : methods)
			{
				if (!rop.operate(m, Modifier.isStatic(m.getModifiers()), obj))
					return;
			}
		}

		@FunctionalInterface
		public static interface accessible_object_operation<_A>
		{
			/**
			 * 遍历每个字段，处理的是root原对象，即反射缓存的对象。
			 * 
			 * @param ao
			 * @param is_static 目标字段是否是静态的
			 * @param value     字段值，无效则为null
			 */
			public boolean operate(AccessibleObject ao, boolean is_static, _A obj);
		}

		@SuppressWarnings(
		{ "rawtypes", "unchecked" })
		public static final <_A> void walk_accessible_objects(Object target, accessible_object_operation<_A> op)
		{
			Class<?> clazz;
			Object obj;
			if (target instanceof Class c)
			{
				clazz = c;
				obj = null;
			}
			else
			{
				clazz = target.getClass();
				obj = target;
			}
			Field[] fields = reflection.find_declared_fields(clazz);
			Method[] methods = reflection.find_declared_methods(clazz);
			Constructor<?>[] constructors = reflection.__find_declared_constructors(clazz);
			accessible_object_operation rop = (accessible_object_operation) op;
			for (Field f : fields)
			{
				if (!rop.operate(f, Modifier.isStatic(f.getModifiers()), obj))
					return;
			}
			for (Constructor<?> c : constructors)
			{
				if (!rop.operate(c, Modifier.isStatic(c.getModifiers()), obj))
					return;
			}
			for (Method m : methods)
			{
				if (!rop.operate(m, Modifier.isStatic(m.getModifiers()), obj))
					return;
			}
		}

		/**
		 * 扫描过滤
		 */
		@FunctionalInterface
		public static interface filter
		{
			/**
			 * 过滤AnnotatedElement的条件。
			 * 
			 * @param scanned_clazz
			 * @return 返回为true才收集该元素
			 */
			public boolean condition(AnnotatedElement scanned_ae);

			public static final filter RESERVE_ALL = (AnnotatedElement scanned_ae) -> true;

			@FunctionalInterface
			public static interface _class
			{
				/**
				 * 过滤扫描到的类，只有返回true的类才被保留。
				 * 
				 * @param scanned_clazz
				 * @return
				 */
				public boolean condition(Class<?> scanned_clazz);

				public static final _class RESERVE_ALL = (Class<?> scanned_clazz) -> true;
			}
		}

		/**
		 * 扫描所有被注解的元素，包括Class，Field，Method，Constructor等<br>
		 * 
		 * @param loader
		 * @param filter
		 * @return
		 */
		public static final ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader, filter filter)
		{
			ArrayList<AnnotatedElement> annotated = new ArrayList<>();
			ArrayList<Class<?>> classes = class_loader.loaded_classes_copy(loader);
			for (Class<?> clazz : classes)
			{
				if (clazz.getAnnotations().length > 0)
				{
					if (filter.condition(clazz))
						annotated.add(clazz);
				}
				walk_accessible_objects(clazz, (AccessibleObject ao, boolean isStatic, Object obj) ->
				{
					if (ao.getAnnotations().length > 0)
					{
						if (filter.condition(ao))
							annotated.add(ao);
					}
					return true;
				});
			}
			return annotated;
		}

		public static final ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader)
		{
			return scan_annotated_elements(loader, filter.RESERVE_ALL);
		}

		/**
		 * 扫描指定ClassLoader的指定注解的元素
		 * 
		 * @param <_T>
		 * @param loader           要扫描的ClassLoader
		 * @param annotation_clazz 注解类，不要求必须是Annotation子类，可以是任何类型
		 * @param filter           过滤条件
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static final <_T> ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader, Class<_T> annotation_clazz, filter filter)
		{
			ArrayList<AnnotatedElement> annotated = new ArrayList<>();
			ArrayList<Class<?>> classes = class_loader.loaded_classes_copy(loader);
			for (Class<?> clazz : classes)
			{
				if (clazz.isAnnotationPresent((Class<? extends Annotation>) annotation_clazz))
				{
					if (filter.condition(clazz))// 不满足条件的AnnotatedElement不放入结果
						annotated.add(clazz);
				}
				walk_accessible_objects(clazz, (AccessibleObject ao, boolean isStatic, Object obj) ->
				{
					if (ao.isAnnotationPresent((Class<? extends Annotation>) annotation_clazz))
					{
						if (filter.condition(ao))
							annotated.add(ao);
					}
					return true;
				});
			}
			return annotated;
		}

		public static final <_T> ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader, Class<_T> annotation_clazz)
		{
			return scan_annotated_elements(loader, annotation_clazz, filter.RESERVE_ALL);
		}

		@SuppressWarnings("unchecked")
		public static final <_T> ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader, Class<_T> annotation_clazz, filter._class filter)
		{
			ArrayList<AnnotatedElement> annotated = new ArrayList<>();
			ArrayList<Class<?>> classes = class_loader.loaded_classes_copy(loader);
			for (Class<?> clazz : classes)
			{
				// 不满足条件的类直接略过
				if (!filter.condition(clazz))
					continue;
				if (clazz.isAnnotationPresent((Class<? extends Annotation>) annotation_clazz))
					annotated.add(clazz);
				walk_accessible_objects(clazz, (AccessibleObject ao, boolean isStatic, Object obj) ->
				{
					if (ao.isAnnotationPresent((Class<? extends Annotation>) annotation_clazz))
						annotated.add(ao);
					return true;
				});
			}
			return annotated;
		}

		public static final <_T> ArrayList<AnnotatedElement> scan_annotated_classes(ClassLoader loader, Class<_T> annotation_clazz)
		{
			return scan_annotated_elements(loader, annotation_clazz, filter._class.RESERVE_ALL);
		}
	}

}
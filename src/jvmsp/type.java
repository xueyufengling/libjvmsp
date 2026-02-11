package jvmsp;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public abstract class type<_T>
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
	@SuppressWarnings("unchecked")
	public final _T resolve()
	{
		if (!is_primitive && dirty)
		{
			dirty = false;
			resolve_type_info();
		}
		return (_T) this;
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
	@SuppressWarnings("rawtypes")
	public static final long sizeof(type t)
	{
		return t.size();
	}

	@SuppressWarnings("rawtypes")
	public static final long sizeof(type... ts)
	{
		long total_size = 0;
		for (type t : ts)
		{
			total_size += sizeof(t);
		}
		return total_size;
	}

	private String typename;

	/**
	 * 获取类型名称
	 * 
	 * @return
	 */
	public String typename()
	{
		return typename;
	}

	@Override
	public String toString()
	{
		return typename;
	}

	/**
	 * 转换为字符串"types[0], types[2], types[3]"此类格式
	 * 
	 * @param types
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static final String to_string(type... types)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < types.length; i++)
		{
			sb.append(types[i].typename());
			if (i != types.length - 1)
			{
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	public static final String func_to_string(type ret_type, type... arg_types)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(ret_type.typename());
		sb.append('(');
		sb.append(to_string(arg_types));
		sb.append(')');
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	public static final String func_to_string(String func_name, type ret_type, type... arg_types)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(ret_type.typename());
		sb.append(' ');
		sb.append(func_name);
		sb.append('(');
		sb.append(to_string(arg_types));
		sb.append(')');
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	public static final String func_ptr_to_string(type ret_type, type... arg_types)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(ret_type.typename());
		sb.append("(*)(");
		sb.append(to_string(arg_types));
		sb.append(')');
		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(typename) ^ Objects.hashCode(size);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		return (obj instanceof type type) && this.typename.equals(type.typename);
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
		this.typename = name;
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
	public static class cxx_type extends type<cxx_type> implements Cloneable
	{
		public static final Object __access(long native_addr, cxx_type type)
		{
			if (type == _void)
				throw new java.lang.IllegalAccessError("error: dereferencing 'void*' pointer " + pointer.to_hex(native_addr));
			if (type == jobject)
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
				return decl_type.toString() + ' ' + name;
			}

			private String name;

			/**
			 * 字段偏移量，由cxx_type类计算
			 */
			long offset = 0;

			/**
			 * 该字段的类型
			 */
			private cxx_type decl_type;

			/**
			 * 声明该字段的类
			 */
			cxx_type struct_type;

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
			public cxx_type decl_type()
			{
				return decl_type;
			}

			/**
			 * 该字段属于哪个类
			 * 
			 * @return
			 */
			public cxx_type struct_type()
			{
				return struct_type;
			}

			private field(String name, cxx_type type)
			{
				this.name = name;
				this.decl_type = type;
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
			 * @param base_addr
			 * @return
			 */
			public final Object read(long base_addr)
			{
				return __access(base_addr + offset, decl_type);
			}

			public final void write(long base_addr, Object x)
			{
				unsafe.write(base_addr + offset, x);
			}

			public final void write(long base_addr, byte x)
			{
				unsafe.write(base_addr + offset, x);
			}

			public final void write(long base_addr, boolean x)
			{
				unsafe.write(base_addr + offset, x);
			}

			public final void write(long base_addr, char x)
			{
				unsafe.write(base_addr + offset, x);
			}

			public final void write(long base_addr, short x)
			{
				unsafe.write(base_addr + offset, x);
			}

			public final void write(long base_addr, int x)
			{
				unsafe.write(base_addr + offset, x);
			}

			public final void write(long base_addr, long x)
			{
				unsafe.write(base_addr + offset, x);
			}

			public final void write(long base_addr, float x)
			{
				unsafe.write(base_addr + offset, x);
			}

			public final void write(long base_addr, double x)
			{
				unsafe.write(base_addr + offset, x);
			}

			public final void write(long base_addr, pointer x)
			{
				unsafe.write(base_addr + offset, x.address());
			}

			/**
			 * 将该字段的值解释为函数指针并返回可调用的MethodHandle。<br>
			 * decl_type必须是function_pointer_type，否则抛出异常。
			 * 
			 * @param base_addr
			 * @return
			 */
			public final MethodHandle callable(long base_addr)
			{
				return symbols.bind(shared_object.stub_function((cxx_type.function_pointer_type) decl_type), 0, (long) read(base_addr));
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
				cxx_type current_field_type = current_field.decl_type();
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
			defined_types.put(name, this);
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
			defined_types.put(name, this);
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

		private static final cxx_type define_primitive(String type_name, cxx_type t)
		{
			return define_primitive(type_name, t.size(), t.memory_layout());
		}

		/**
		 * 添加字段，禁止添加类本身。
		 * 
		 * @param field
		 * @return
		 */
		public cxx_type decl_field(field field)
		{
			if (is_primitive() || field.decl_type().equals(this))// 禁止给原生类型添加字段，或添加类本身作为字段
				throw new java.lang.InternalError("cannot append field \"" + field + "\" to " + this + ". append fields to primitive types or append self as a field are not allowed.");
			fields.add(field);
			field.struct_type = this;
			mark_dirty();// 标记size更新
			return this;
		}

		public cxx_type decl_field(String name, cxx_type type)
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
					cxx_type f_type = f.decl_type();// 字段f的类型
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
				throw new java.lang.NoSuchFieldError("field index '" + idx + "' doesn't exists in '" + typename() + "'");
		}

		public int declared_field_index(String field_name)
		{
			if (!is_primitive())
				for (int idx = 0; idx < fields.size(); ++idx)// 优先使用派生类的字段
					if (fields.get(idx).name().equals(field_name))
						return idx;
			throw new java.lang.NoSuchFieldError("declared field '" + field_name + "' doesn't exists in '" + typename() + "'");
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
			throw new java.lang.NoSuchFieldError("declared field '" + field_name + "' doesn't exists in '" + typename() + "'");
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
			throw new java.lang.NoSuchFieldError("declared field '" + field_name + "' doesn't exists in '" + typename() + "'");
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
			throw new java.lang.NoSuchFieldError("field '" + field_name + "' doesn't exists in '" + typename() + "'");
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
					System.out.println("│ " + layout_info_element("" + (print_idx_as_from + i), 1) + layout_info_element("in: " + f.struct_type.toString(), type_element_size_in_tab) + layout_info_element(f.toString(), type_element_size_in_tab) + layout_info_element("offset: " + f.offset + ",", number_element_size_in_tab) + layout_info_element("size: " + sizeof(f.decl_type()), number_element_size_in_tab) + "│");
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

		public static final cxx_type _enum = typedef(_int, "_enum");

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

		/**
		 * 函数类型
		 */
		public static class function_type extends cxx_type
		{
			private final cxx_type ret_type;
			private final cxx_type[] arg_types;

			protected function_type(String func_type_name, cxx_type ret_type, cxx_type[] arg_types)
			{
				super(func_type_name, false, 0, (MemoryLayout) null);// 函数类型不同于变量类型，本身不可被声明，仅储存了返回值和参数类型信息
				this.ret_type = ret_type;
				this.arg_types = arg_types;
			}

			protected function_type(cxx_type ret_type, cxx_type[] arg_types)
			{
				this(type.func_to_string(ret_type, arg_types), ret_type, arg_types);
			}

			public final cxx_type return_type()
			{
				return ret_type;
			}

			public final cxx_type[] argument_types()
			{
				return arg_types;
			}

			public static final function_type of(cxx_type ret_type, cxx_type... arg_types)
			{
				return new function_type(ret_type, arg_types);
			}

			/**
			 * 将函数类型转换为FunctionDescriptor，供FFI API使用
			 * 
			 * @return
			 */
			public final FunctionDescriptor to_function_descriptor()
			{
				MemoryLayout[] arg_layouts = new MemoryLayout[arg_types.length];
				for (int idx = 0; idx < arg_types.length; ++idx)
					arg_layouts[idx] = arg_types[idx].memory_layout();
				if (ret_type == cxx_type._void)
					return FunctionDescriptor.ofVoid(arg_layouts);
				else
					return FunctionDescriptor.of(ret_type.memory_layout(), arg_layouts);
			}
		}

		public static class pointer_type extends cxx_type
		{
			/**
			 * 指针的类型
			 */
			protected cxx_type pointed_to_type;
			protected String pointed_to_type_name;// 指针类型创建时，指向的类型有可能还未声明，此时pointed_to_type为null，需要保留指向类型的名称后期动态查找

			protected pointer_type(String ptr_type_name, cxx_type pointed_to_type)
			{
				super(ptr_type_name, false, WORD.size(), ValueLayout.ADDRESS);
				this.pointed_to_type = pointed_to_type;
			}

			protected pointer_type(cxx_type type)
			{
				this(type.typename() + "*", type);
			}

			protected pointer_type(String ptr_type_name, String pointed_to_type_name)
			{
				super(ptr_type_name, false, WORD.size(), ValueLayout.ADDRESS);
				this.pointed_to_type_name = pointed_to_type_name;
				this.pointed_to_type = cxx_type.of(pointed_to_type_name);
			}

			protected pointer_type(String pointed_to_type_name)
			{
				this(pointed_to_type_name + "*", pointed_to_type_name);
			}

			public cxx_type pointed_to_type()
			{
				if (pointed_to_type == null)
				{
					this.pointed_to_type = cxx_type.of(pointed_to_type_name);
				}
				return pointed_to_type;
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

		public static class function_pointer_type extends pointer_type
		{
			protected function_pointer_type(cxx_type.function_type func_type)
			{
				super(type.func_ptr_to_string(func_type.return_type(), func_type.argument_types()), func_type);
			}

			protected function_pointer_type(cxx_type ret_type, cxx_type... arg_types)
			{
				this(cxx_type.function_type.of(ret_type, arg_types));
			}

			@Override
			public final cxx_type.function_type pointed_to_type()
			{
				return ((cxx_type.function_type) pointed_to_type);
			}

			public final FunctionDescriptor to_function_descriptor()
			{
				return pointed_to_type().to_function_descriptor();
			}

			public static final function_pointer_type of(cxx_type.function_type func_type)
			{
				return new function_pointer_type(func_type);
			}

			public static final function_pointer_type of(cxx_type ret_type, cxx_type... arg_types)
			{
				return new function_pointer_type(ret_type, arg_types);
			}
		}

		/**
		 * 函数签名
		 */
		public static class function_signature
		{
			public String function_name;

			public cxx_type.function_type func_type;

			public String toString()
			{
				return type.func_to_string(function_name, func_type.return_type(), func_type.argument_types());
			}

			public function_signature(String function_name, cxx_type.function_type func_type)
			{
				this.function_name = function_name;
				this.func_type = func_type;
			}

			public function_signature(String function_name, cxx_type return_type, cxx_type... arg_types)
			{
				this(function_name, cxx_type.function_type.of(return_type, arg_types));
			}

			public function_signature(cxx_type return_type, cxx_type... arg_types)
			{
				this(null, return_type, arg_types);
			}

			public static final function_signature of(String function_name, cxx_type return_type, cxx_type... arg_types)
			{
				return new function_signature(function_name, return_type, arg_types);
			}

			public static final function_signature of(cxx_type return_type, cxx_type... arg_types)
			{
				return new function_signature(return_type, arg_types);
			}
		}

		public static final pointer_type pvoid = pointer_type.of(_void);

		public static final pointer_type pchar = pointer_type.of(_char);
		public static final pointer_type punsigned_char = pointer_type.of(unsigned_char);
		public static final pointer_type pshort = pointer_type.of(_short);
		public static final pointer_type punsigned_short = pointer_type.of(unsigned_short);
		public static final pointer_type pint = pointer_type.of(_int);
		public static final pointer_type punsigned_int = pointer_type.of(unsigned_int);
		public static final pointer_type pbool = pointer_type.of(bool);
		public static final pointer_type plong_long = pointer_type.of(_long_long);
		public static final pointer_type punsigned_long_long = pointer_type.of(unsigned_long_long);
		public static final pointer_type pfloat = pointer_type.of(_float);
		public static final pointer_type pdouble = pointer_type.of(_double);
		public static final pointer_type pint8_t = pointer_type.of(int8_t);
		public static final pointer_type puint8_t = pointer_type.of(uint8_t);
		public static final pointer_type pint16_t = pointer_type.of(int16_t);
		public static final pointer_type puint16_t = pointer_type.of(uint16_t);
		public static final pointer_type pint32_t = pointer_type.of(int32_t);
		public static final pointer_type puint32_t = pointer_type.of(uint32_t);
		public static final pointer_type pint64_t = pointer_type.of(int64_t);
		public static final pointer_type puint64_t = pointer_type.of(uint64_t);
		public static final pointer_type psize_t = pointer_type.of(size_t);

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
			private final long addr;

			public object(pointer ptr)
			{
				this.addr = ptr.address();
			}

			public object(long addr)
			{
				this.addr = addr;
			}

			/**
			 * 所属类型
			 * 
			 * @return
			 */
			public final cxx_type type()
			{
				return cxx_type.this;
			}

			public final long address()
			{
				return addr;
			}

			public final field get_field(String field_name)
			{
				return type().field(field_name);
			}

			public final Object read(field f)
			{
				return f.read(addr);
			}

			public final Object read(String field_name)
			{
				return read(get_field(field_name));
			}

			public final void write(field f, Object x)
			{
				f.write(addr, x);
			}

			public final void write(field f, byte x)
			{
				f.write(addr, x);
			}

			public final void write(field f, boolean x)
			{
				f.write(addr, x);
			}

			public final void write(field f, char x)
			{
				f.write(addr, x);
			}

			public final void write(field f, short x)
			{
				f.write(addr, x);
			}

			public final void write(field f, int x)
			{
				f.write(addr, x);
			}

			public final void write(field f, long x)
			{
				f.write(addr, x);
			}

			public final void write(field f, float x)
			{
				f.write(addr, x);
			}

			public final void write(field f, double x)
			{
				f.write(addr, x);
			}

			public final void write(field f, pointer x)
			{
				f.write(addr, x);
			}

			public final void write(String field_name, Object x)
			{
				write(get_field(field_name), x);
			}

			public final void write(String field_name, byte x)
			{
				write(get_field(field_name), x);
			}

			public final void write(String field_name, boolean x)
			{
				write(get_field(field_name), x);
			}

			public final void write(String field_name, char x)
			{
				write(get_field(field_name), x);
			}

			public final void write(String field_name, short x)
			{
				write(get_field(field_name), x);
			}

			public final void write(String field_name, int x)
			{
				write(get_field(field_name), x);
			}

			public final void write(String field_name, long x)
			{
				write(get_field(field_name), x);
			}

			public final void write(String field_name, float x)
			{
				write(get_field(field_name), x);
			}

			public final void write(String field_name, double x)
			{
				write(get_field(field_name), x);
			}

			public final void write(String field_name, pointer x)
			{
				write(get_field(field_name), x);
			}

			public final MethodHandle callable(field f)
			{
				return f.callable(addr);
			}

			public final MethodHandle callable(String field_name)
			{
				return callable(type().field(field_name));
			}
		}

		public static final cxx_type va_list = typedef(pchar, "va_list");

		/**
		 * JNI中的Java基本类型
		 */
		public static final cxx_type oop = define_primitive("oop", unsigned_int);// oop为32位

		public static final cxx_type jobject = define_primitive("jobject", sizeof(pvoid), ValueLayout.ADDRESS);
		public static final cxx_type jweak = typedef(jobject, "jweak");
		public static final cxx_type jclass = typedef(jobject, "jclass");
		public static final cxx_type jstring = typedef(jobject, "jstring");
		public static final cxx_type jthrowable = typedef(jobject, "jthrowable");
		public static final cxx_type jfieldID = define_primitive("jfieldID ", sizeof(pvoid), ValueLayout.ADDRESS);
		public static final cxx_type jmethodID = define_primitive("jmethodID ", sizeof(pvoid), ValueLayout.ADDRESS);
		public static final cxx_type jarray = define_primitive("jarray", sizeof(pvoid), ValueLayout.ADDRESS);
		public static final cxx_type jbooleanArray = typedef(jarray, "jbooleanArray");
		public static final cxx_type jbyteArray = typedef(jarray, "jbyteArray");
		public static final cxx_type jcharArray = typedef(jarray, "jcharArray");
		public static final cxx_type jshortArray = typedef(jarray, "jshortArray");
		public static final cxx_type jintArray = typedef(jarray, "jintArray");
		public static final cxx_type jlongArray = typedef(jarray, "jlongArray");
		public static final cxx_type jfloatArray = typedef(jarray, "jfloatArray");
		public static final cxx_type jdoubleArray = typedef(jarray, "jdoubleArray");
		public static final cxx_type jobjectArray = typedef(jarray, "jobjectArray");

		public static final cxx_type jboolean = define_primitive("jboolean", false, sizeof(uint8_t), ValueLayout.JAVA_BOOLEAN);
		public static final cxx_type jbyte = define_primitive("jbyte", true, sizeof(int8_t), ValueLayout.JAVA_BYTE);
		public static final cxx_type jchar = define_primitive("jchar", false, sizeof(uint16_t), ValueLayout.JAVA_CHAR);
		public static final cxx_type jshort = define_primitive("jshort", true, sizeof(int16_t), ValueLayout.JAVA_SHORT);
		public static final cxx_type jint = define_primitive("jint", true, sizeof(int32_t), ValueLayout.JAVA_INT);
		public static final cxx_type jsize = typedef(jint, "jsize");
		public static final cxx_type jlong = define_primitive("jlong", true, sizeof(int64_t), ValueLayout.JAVA_LONG);
		public static final cxx_type jfloat = define_primitive("jfloat", true, sizeof(_float), ValueLayout.JAVA_FLOAT);
		public static final cxx_type jdouble = define_primitive("jdouble", true, sizeof(_double), ValueLayout.JAVA_DOUBLE);

		public static final cxx_type jobjectRefType = typedef(_enum, "jobjectRefType");

		private static final MemoryLayout _jvalue_layout = MemoryLayout.unionLayout(
				ValueLayout.JAVA_BOOLEAN.withName("z"),
				ValueLayout.JAVA_BYTE.withName("b"),
				ValueLayout.JAVA_CHAR.withName("c"),
				ValueLayout.JAVA_SHORT.withName("s"),
				ValueLayout.JAVA_INT.withName("i"),
				ValueLayout.JAVA_LONG.withName("j"),
				ValueLayout.JAVA_FLOAT.withName("f"),
				ValueLayout.JAVA_DOUBLE.withName("d"),
				ValueLayout.ADDRESS.withName("l"));
		public static final cxx_type jvalue = define_primitive("jvalue", true, _jvalue_layout.byteSize(), _jvalue_layout);

		public static final pointer_type pjboolean = pointer_type.of(jboolean);
		public static final pointer_type pjbyte = pointer_type.of(jbyte);
		public static final pointer_type pjchar = pointer_type.of(jchar);
		public static final pointer_type pjshort = pointer_type.of(jshort);
		public static final pointer_type pjint = pointer_type.of(jint);
		public static final pointer_type pjsize = pointer_type.of(jsize);
		public static final pointer_type pjlong = pointer_type.of(jlong);
		public static final pointer_type pjfloat = pointer_type.of(jfloat);
		public static final pointer_type pjdouble = pointer_type.of(jdouble);

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
		public static class pointer implements AutoCloseable
		{
			/**
			 * C++层的指针转换为(void*)(uint64_t)addr
			 */
			long addr;

			/**
			 * 指针所指向的C++类型
			 */
			cxx_type pointed_to_type;

			/**
			 * 指针算术运算的步长，与类型有关，以byte为单位
			 */
			long stride;

			/**
			 * 仅指针算术运算使用！
			 * 
			 * @param addr
			 * @param pointed_to_type
			 * @param stride
			 */
			private pointer(long addr, cxx_type pointed_to_type, long stride)
			{
				this.addr = addr;
				this.pointed_to_type = pointed_to_type;
				this.stride = stride;
			}

			/**
			 * C++对象指针
			 * 
			 * @param addr
			 * @param pointed_to_type
			 */
			private pointer(long addr, cxx_type pointed_to_type)
			{
				this(addr, pointed_to_type, pointed_to_type.size());
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
				this.pointed_to_type = ptr.pointed_to_type;
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

			/**
			 * C++的nullptr一定是0
			 */
			public static final pointer nullptr = pointer.to(0);

			/**
			 * Java的null地址，对应堆内存的基地址0偏移处。堆内存起始地址并不一定是0.
			 */
			public static final pointer jnull = pointer.to(java_type.oop_of(null));

			public long address()
			{
				return addr;
			}

			public cxx_type pointed_to_type()
			{
				return pointed_to_type;
			}

			public cxx_type pointer_type()
			{
				return pointer_type.of(pointed_to_type);
			}

			public boolean is_nullptr()
			{
				return addr == 0;
			}

			public static final String to_hex(long addr)
			{
				return "0x" + ((addr >> 32 == 0) ? String.format("%08x", addr) : String.format("%016x", addr));
			}

			/**
			 * 十六进制地址
			 */
			@Override
			public String toString()
			{
				return to_hex(addr);
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
						a[idx] = (byte) this.at(idx);
					}
				}
				else if (clazz == boolean.class)
				{
					boolean[] a = ((boolean[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (boolean) this.at(idx);
					}
				}
				else if (clazz == short.class)
				{
					short[] a = ((short[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (short) this.at(idx);
					}
				}
				else if (clazz == char.class)
				{
					char[] a = ((char[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (char) this.at(idx);
					}
				}
				else if (clazz == int.class)
				{
					int[] a = ((int[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (int) this.at(idx);
					}
				}
				else if (clazz == float.class)
				{
					float[] a = ((float[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (float) this.at(idx);
					}
				}
				else if (clazz == long.class)
				{
					long[] a = ((long[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (long) this.at(idx);
					}
				}
				else if (clazz == double.class)
				{
					double[] a = ((double[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = (double) this.at(idx);
					}
				}
				else
				{
					Object[] a = ((Object[]) arr);
					for (int idx = 0; idx < num; ++idx)
					{
						a[idx] = this.at(idx);
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
				return Objects.hashCode(addr) ^ Objects.hashCode(pointed_to_type);
			}

			/**
			 * 把给定的地址和类型包装为指针
			 * 
			 * @param addr
			 * @param pointed_to_type
			 * @return
			 */
			public static final pointer to(long addr, cxx_type pointed_to_type)
			{
				return new pointer(addr, pointed_to_type);
			}

			public static final pointer to(long addr)
			{
				return new pointer(addr);
			}

			public static final pointer to(int _32bit_addr)
			{
				return new pointer(cxx_type.uint_ptr(_32bit_addr));
			}

			/**
			 * 将给定的十六进制地址和类型包装为指针
			 * 
			 * @param hex
			 * @param pointed_to_type
			 * @return
			 */
			public static final pointer to(String hex)
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
			 * @param dest_pointed_to_type
			 * @return
			 */
			public pointer cast(cxx_type dest_pointed_to_type)
			{
				this.pointed_to_type = dest_pointed_to_type;
				return this;
			}

			/**
			 * 将指针视作C数组并使用索引读值
			 * 
			 * @param dest_pointed_to_type
			 * @param offset
			 * @param stride
			 * @return
			 */
			public Object at(cxx_type dest_pointed_to_type, long offset, long stride)
			{
				return __access(addr + offset * stride, pointed_to_type);
			}

			public Object at(cxx_type dest_pointed_to_type, long offset)
			{
				return at(dest_pointed_to_type, offset, dest_pointed_to_type.size());
			}

			public Object at(long offset)
			{
				return at(pointed_to_type, offset, pointed_to_type.size());
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
			 * 解引用后赋值，即向地址addr写入新值
			 * 
			 * @param x
			 * @return
			 */
			public final Object dereference_assign(Object x)
			{
				unsafe.write(addr, x);
				return x;
			}

			public final byte dereference_assign(byte x)
			{
				unsafe.write(addr, x);
				return x;
			}

			public final boolean dereference_assign(boolean x)
			{
				unsafe.write(addr, x);
				return x;
			}

			public final char dereference_assign(char x)
			{
				unsafe.write(addr, x);
				return x;
			}

			public final short dereference_assign(short x)
			{
				unsafe.write(addr, x);
				return x;
			}

			public final int dereference_assign(int x)
			{
				unsafe.write(addr, x);
				return x;
			}

			public final long dereference_assign(long x)
			{
				unsafe.write(addr, x);
				return x;
			}

			public final float dereference_assign(float x)
			{
				unsafe.write(addr, x);
				return x;
			}

			public final double dereference_assign(double x)
			{
				unsafe.write(addr, x);
				return x;
			}

			/**
			 * 指针加法，返回一个新指针
			 * 
			 * @param step
			 * @return
			 */
			public pointer add(long step)
			{
				return new pointer(addr + stride * step, pointed_to_type, stride);
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
				return new pointer(addr - stride * step, pointed_to_type, stride);
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

			public static final pointer address_of(Object _jobject)
			{
				return pointer.to(java_type.uncompressed_oop_of(_jobject), jobject);
			}

			public static final pointer address_of(cxx_type.object cxx_obj)
			{
				return pointer.to(cxx_obj.addr);
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
					return pointer.to(java_type.uncompressed_oop_of(unsafe.static_field_base(field)) + unsafe.static_field_offset(field), cxx_type.of(field.getType()));
				else
					return pointer.to(java_type.uncompressed_oop_of(jobject) + unsafe.object_field_offset(field), cxx_type.of(field.getType()));
			}

			public static final pointer address_of(Object jobject, String field)
			{
				return address_of(jobject, reflection.find_field(jobject, field));
			}

			/**
			 * 解引用值
			 * 
			 * @return
			 */
			public Object dereference()
			{
				return __access(addr, pointed_to_type);
			}

			/**
			 * 创建一个新指针，地址和本指针相同，但托管在try-with-resources代码中自动释放指针
			 * 
			 * @return
			 */
			public final pointer auto()
			{
				final class auto_pointer extends pointer
				{
					private auto_pointer(pointer ptr)
					{
						super(ptr.addr, ptr.pointed_to_type, ptr.stride);
					}

					/**
					 * try-with-resources代码结束时自动释放指针
					 * 
					 */
					@Override
					public void close() throws Exception
					{
						memory.free(this);
					}
				}
				return new auto_pointer(this);
			}

			public final void print_memory(long size)
			{
				pointer indicator = this.copy().cast(uint8_t);
				for (int i = 0; i < size; ++i, indicator.inc())
				{
					System.out.print(String.format("%02x", cxx_type.uint_ptr((byte) indicator.dereference())) + " ");
				}
			}

			@Override
			public void close() throws Exception
			{
				// 无操作，仅为让pointer声明通过编译器的AutoCloseable检查
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

			public final long address()
			{
				return java_type.uncompressed_oop_of(base) + offset;
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
					Object deref_obj = java_type.object_from_oop(address());
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
					unsafe.memcpy(v, java_type.HEADER_BYTE_LENGTH, base, java_type.HEADER_BYTE_LENGTH, java_type.sizeof_object(v.getClass()) - java_type.HEADER_BYTE_LENGTH);// 只拷贝字段，不覆盖对象头
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
	 * Java类型相关信息和操作
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
		 * @param x
		 * @return
		 */
		public static final byte byte_value(Object x)
		{
			return ((Number) x).byteValue();
		}

		public static final char char_value(Object x)
		{
			return ((Character) x).charValue();
		}

		public static final boolean boolean_value(Object x)
		{
			return ((Boolean) x).booleanValue();
		}

		public static final short short_value(Object x)
		{
			return ((Number) x).shortValue();
		}

		public static final int int_value(Object x)
		{
			return ((Number) x).intValue();
		}

		public static final float float_value(Object x)
		{
			return ((Number) x).floatValue();
		}

		public static final long long_value(Object x)
		{
			return ((Number) x).longValue();
		}

		public static final double double_value(Object x)
		{
			return ((Number) x).doubleValue();
		}

		private static final HashMap<Class<?>, Long> cached_size = new HashMap<>();

		/**
		 * Java对象所占用内存的大小，无对齐大小。每个Class<?>计算一次后将缓存。
		 * 
		 * @param jtype
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
		 * @param object
		 * @param arg_types
		 * @param args
		 * @return
		 */
		public static final <T> T placement_new(T object, Class<?>[] arg_types, Object... args)
		{
			Class<?> target_type = object.getClass();
			MethodHandle constructor = symbols.constructor_method(target_type, arg_types);
			try
			{
				constructor.invoke(memory.cat(object, args));
			}
			catch (Throwable ex)
			{
				throw new java.lang.InternalError("placement new for " + target_type + " failed", ex);
			}
			return object;
		}

		@SuppressWarnings("unchecked")
		public static final <T> T copy(T object)
		{
			Class<T> clazz = (Class<T>) object.getClass();
			T o = unsafe.allocate(clazz);
			unsafe.memcpy(object, HEADER_BYTE_LENGTH, o, HEADER_BYTE_LENGTH, java_type.sizeof_object(clazz) - HEADER_BYTE_LENGTH);// 只拷贝字段，不覆盖对象头
			return o;
		}

		/**
		 * 获取对象（压缩后的）的oop，返回long<br>
		 * 利用Object[]的元素为oop指针的事实来间接取oop。<br>
		 * 在32位和未启用UseCompressedOops的64位JVM上，取的地址直接就是未压缩的oop，直接指向Java对象本身的内存。<br>
		 * 在开启UseCompressedOops的64位JVM上，取的oop是压缩后的，需要乘以字节对齐量（字节对齐默认为8）或者左移（3位）+堆的基地址（即Java中null的绝对地址）才是绝对地址。
		 * 
		 * @param object
		 * @return
		 */
		public static final long oop_of(Object object)
		{
			return unsafe.pointed_to_address(new Object[]
			{ object }, unsafe.ARRAY_OBJECT_BASE_OFFSET);
		}

		public static final long oop_of(long native_addr)
		{
			return virtual_machine.encode_oop(native_addr);
		}

		/**
		 * 从（压缩后的）oop获取Java对象。<br>
		 * 如果开启了oop压缩，则必须传入压缩后的oop。<br>
		 * 
		 * @param addr
		 * @return
		 */
		public static final Object object_from_oop(long addr)
		{
			Object[] _jobjects = new Object[1];
			unsafe.store_address(_jobjects, unsafe.ARRAY_OBJECT_BASE_OFFSET, addr);
			return _jobjects[0];
		}

		/**
		 * 为oop创建一个local的handle，等价于JNIHandles::make_local(oop)。<br>
		 * local引用本质上是个指向oop的指针。
		 * 
		 * @param oop 要创建local引用的oop
		 * @return oop对应的handle，不需要的时候必须通过free()销毁
		 */
		public static final long local_handle(long oop)
		{
			long handle = unsafe.allocate(cxx_type.pvoid.size());
			unsafe.write(handle, oop);
			return handle;
		}

		public static final long local_handle(Object object)
		{
			return local_handle(oop_of(object));
		}

		/**
		 * 获取未压缩的oop，该地址为Java对象的实际内存地址
		 * 
		 * @param object
		 * @return
		 */
		public static final long uncompressed_oop_of(Object object)
		{
			return virtual_machine.decode_oop((int) oop_of(object));
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
			if (virtual_machine.JVM_BIT_VERSION == 32)
			{
				MARKWORD_LENGTH = __32_bit.MARKWORD_LENGTH;
				KLASS_WORD_OFFSET = __32_bit.KLASS_OFFSET;
				KLASS_WORD_LENGTH = __32_bit.KLASS_LENGTH;
				HEADER_LENGTH = __32_bit.HEADER_LENGTH;
			}
			else if (virtual_machine.JVM_BIT_VERSION == 64)
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
				throw new java.lang.InternalError("unknown native jvm bit-version '" + virtual_machine.JVM_BIT_VERSION + "'");
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

		/**
		 * 对象layout类型
		 */
		public static enum object_layout
		{
			/**
			 * JDK 24+<br>
			 * 开启对象头压缩，包含压缩klass pointer，即+UseCompressedClassPointers<br>
			 * +UseCompactObjectHeaders
			 */
			Compact,
			/**
			 * 压缩klass pointer，但不压缩对象头<br>
			 * +UseCompressedClassPointers -UseCompactObjectHeaders
			 */
			Compressed,
			/**
			 * 未压缩klass pointer，也未压缩对象头<br>
			 * -UseCompressedClassPointers -UseCompactObjectHeaders
			 */
			Uncompressed,
			/**
			 * 未定义
			 */
			Undefined
		}

		public static final object_layout _KLASS_MODE;
		public static final long _OBJECT_HEADER_BASE_OFFSET;
		public static final boolean _OOP_HAS_KLASS_GAP;

		static
		{
			if (virtual_machine.UseCompactObjectHeaders)
			{
				_KLASS_MODE = object_layout.Compact;
				_OOP_HAS_KLASS_GAP = false;
			}
			else
			{
				if (virtual_machine.UseCompressedClassPointers)
				{
					_KLASS_MODE = object_layout.Compressed;
					_OOP_HAS_KLASS_GAP = true;
				}
				else
				{
					_KLASS_MODE = object_layout.Uncompressed;
					_OOP_HAS_KLASS_GAP = false;
				}
			}
			_OBJECT_HEADER_BASE_OFFSET = java_type.HEADER_BYTE_LENGTH;
		}

		/**
		 * 其他常用操作
		 * 
		 * @param <T>
		 * @param cast_type_klass_word
		 * @return
		 */

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
	}
}
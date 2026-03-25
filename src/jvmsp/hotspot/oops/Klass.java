package jvmsp.hotspot.oops;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_constant;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.classfile.ClassLoaderData;
import jvmsp.hotspot.classfile.java_lang_Class;
import jvmsp.hotspot.utilities.AccessFlags;
import jvmsp.structs.long_array;
import jvmsp.type.cxx_type;

public class Klass extends Metadata
{
	/**
	 * https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/instanceKlass.hpp#944
	 * 对象布局信息，包括对象的大小，此值将用于JVM分配对象。
	 */
	private static final long _layout_helper = vm_struct.entry.find("Klass", "_layout_helper").offset;// 8
	private static final long _super_check_offset = vm_struct.entry.find("Klass", "_super_check_offset").offset;// 20
	private static final long _name = vm_struct.entry.find("Klass", "_name").offset;// 24
	private static final long _secondary_super_cache = vm_struct.entry.find("Klass", "_secondary_super_cache").offset;// 32
	private static final long _secondary_supers = vm_struct.entry.find("Klass", "_secondary_supers").offset;// 40
	private static final long _primary_supers_0 = vm_struct.entry.find("Klass", "_primary_supers[0]").offset;// 48
	private static final long _java_mirror = vm_struct.entry.find("Klass", "_java_mirror").offset;// 112
	private static final long _super = vm_struct.entry.find("Klass", "_super").offset;// 120
	private static final long _subklass = vm_struct.entry.find("Klass", "_subklass").offset;// 128
	private static final long _next_sibling = vm_struct.entry.find("Klass", "_next_sibling").offset;// 136
	private static final long _next_link = vm_struct.entry.find("Klass", "_next_link").offset;// 144
	private static final long _class_loader_data = vm_struct.entry.find("Klass", "_class_loader_data").offset;// 152
	private static final long _vtable_len = vm_struct.entry.find("Klass", "_vtable_len").offset;// 160
	private static final long _access_flags = vm_struct.entry.find("Klass", "_access_flags").offset;// 164

	// 计算相对偏移量，4字节对齐
	/**
	 * 在JDK21中(https://github.com/openjdk/jdk/blob/jdk-21%2B35/src/hotspot/share/oops/klass.hpp#L126)，该字段还是
	 * // Processed access flags, for use by Class.getModifiers.
	 * jint _modifier_flags;
	 * 但在JDK25中(https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/klass.hpp#L126)，该字段已经改为
	 * // Some flags created by the JVM, not in the class file itself,
	 * // are in _misc_flags below.
	 * KlassFlags _misc_flags;
	 */
	private static final long _misc_flags = _super_check_offset - cxx_type._int.size();// 16

	private static final long _kind = _misc_flags - cxx_type._int.size();// 12

	// 继承链缓存的最大Klass数目
	public static final int _primary_super_limit = vm_constant.Klass_primary_super_limit;

	public static final short InstanceKlassKind = 0;
	public static final short InstanceRefKlassKind = 1;
	public static final short InstanceMirrorKlassKind = 2;
	public static final short InstanceClassLoaderKlassKind = 3;
	public static final short InstanceStackChunkKlassKind = 4;
	public static final short TypeArrayKlassKind = 5;
	public static final short ObjArrayKlassKind = 6;
	public static final short UnknownKlassKind = 7;

	protected Klass(String name, long address)
	{
		super(name, address);
	}

	public Klass(long klass_ptr)
	{
		this("Klass", klass_ptr);
	}

	@Override
	public final boolean is_klass()
	{
		return true;
	}

	public static final Klass java_lang_Object = java_lang_Class.as_Klass(Object.class);
	// java.lang.Object的对象布局
	public static final int java_lang_Object_layout_helper = java_lang_Object._layout_helper();

	/**
	 * 如果没有超类，则超类为空指针。<br>
	 * 此对象实际上是Object的超类。<br>
	 */
	public static final Klass nullptr = new Klass(0)
	{
		@Override
		public int _super_check_offset()
		{
			return java_lang_Object._super_check_offset();
		}

		@Override
		public Klass _secondary_super_cache()
		{
			return this;// 0
		}
	};

	public int _layout_helper()
	{
		return super.read_int(_layout_helper);
	}

	public void set_layout_helper(int layout_helper)
	{
		super.write(_layout_helper, layout_helper);
	}

	public static int layout_helper_to_size_helper(int layout_helper)
	{
		return layout_helper >> vm_constant.LogBytesPerWord;
	}

	public int size_helper()
	{
		return layout_helper_to_size_helper(_layout_helper());
	}

	/**
	 * instanceof判断超类时使用，该值为当前的首要选项，即首先判断是否是该offset对应的Klass*。<br>
	 * 如果首要选项与_secondary_super_cache相同，则从_primary_supers按照继承关系依次查找。<br>
	 * 此偏移量实际对应_primary_supers中的元素偏移量.<br>
	 * 通常来说，此项数值必须是本Klass*所在的元素地址。<br>
	 * 
	 * @return
	 */
	public int _super_check_offset()
	{
		return super.read_int(_super_check_offset);
	}

	public void set_super_check_offset(int super_check_offset)
	{
		super.write(_super_check_offset, super_check_offset);
	}

	/**
	 * Object的klass返回0。<br>
	 * 
	 * @return
	 */
	public Klass _super()
	{
		return super.read_memory_object_ptr(Klass.class, _super);
	}

	public void set_super(Klass super_klass)
	{
		super.write(_super, super_klass.address());
	}

	public Klass _secondary_super_cache()
	{
		return super.read_memory_object_ptr(Klass.class, _secondary_super_cache);
	}

	public void set_secondary_super_cache(Klass secondary_super_cache)
	{
		super.write(_secondary_super_cache, secondary_super_cache.address());
	}

	private long _primary_supers_offset(int idx)
	{
		return _primary_supers_0 + idx * unsafe.address_size;
	}

	private long _primary_supers_ptr(int idx)
	{
		return super.read_pointer(_primary_supers_offset(idx));
	}

	/**
	 * 设置instanceof判断使用的超类继承链缓存值，继承链包含自己，即最后一个非nullptr的元素总是自己.<br>
	 * 只保留从本类起上面最多8个超类，且越顶级的超类索引越靠前。<br>
	 * 
	 * @param idx 缓存的Klass*索引，最多缓存8个。
	 * @return
	 */
	public Klass _primary_supers(int idx)
	{
		return super.read_memory_object_ptr(Klass.class, _primary_supers_offset(idx));
	}

	/**
	 * 设置特定索引缓存的超类Klass*，如果缓存中找不到才重新决议继承链继续向上查找超类。
	 * 
	 * @param idx           索引
	 * @param primary_super 超类
	 */
	public void set_primary_supers(int idx, long primary_super)
	{
		super.write(_primary_supers_offset(idx), primary_super);
	}

	public void set_primary_supers(int idx, Klass primary_super)
	{
		this.set_primary_supers(idx, primary_super.address());
	}

	public int cached_self_klass_idx()
	{
		int direct_super_idx = _primary_super_limit - 1;// 从后往前查找
		for (int idx = direct_super_idx; idx >= 0; --idx)
		{
			if (_primary_supers_ptr(idx) == 0)
			{
				continue;
			}
			else
			{
				direct_super_idx = idx;
				break;
			}
		}
		return direct_super_idx;
	}

	public int cached_direct_super_klass_idx()
	{
		return cached_self_klass_idx() - 1;
	}

	/**
	 * 决议所有的超类，其中顶级超类的Klass* _super为nullptr。<br>
	 * 返回数组至少有一个元素，即该Klass*本身。<br>
	 * 不能使用Object[]数组，否则如果设置了Object的超类后调用此函数更新会引发数组存储类型不匹配。<br>
	 * 
	 * @return
	 */
	public long_array resolve_supers()
	{
		long_array klass_chain = new long_array();
		Klass _current = this;
		while (true)
		{
			klass_chain.add(_current.address);
			Klass _super = _current._super();
			if (_super.address == 0)
			{
				break;
			}
			else
			{
				_current = _super;
			}
		}
		return klass_chain;
	}

	/**
	 * 清空继承链超类缓存
	 */
	public void clear_primary_supers()
	{
		unsafe.memset(this.address + _primary_supers_0, _primary_super_limit * unsafe.address_size, (byte) 0);
	}

	/**
	 * 该类本身或其任意超类修改了Klass* _super字段后，需要使用此方法手动更新该类的继承链。<br>
	 */
	public void resolve_primary_supers()
	{
		long_array supers = resolve_supers();
		int num = Math.min(_primary_super_limit, supers.size());
		this.clear_primary_supers();
		for (int idx = 0; idx < num; ++idx)
		{
			this.set_primary_supers(idx, supers.get(supers.size() - 1 - idx));
		}
		this.set_super_check_offset((int) _primary_supers_offset(num - 1));// 设置为本Klass*所在偏移量。
	}

	/**
	 * 设置直接超类
	 * 
	 * @param super_klass
	 */
	public void set_super_klass(Klass super_klass)
	{
		this.set_super(super_klass);
		this.resolve_primary_supers();
	}

	public Symbol _name()
	{
		return super.read_memory_object_ptr(Symbol.class, _name);
	}

	public void set_name(Symbol name)
	{
		super.write(_name, name.address());
	}

	public AccessFlags _access_flags()
	{
		return super.read_memory_object(AccessFlags.class, _access_flags);
	}

	public short _kind()
	{
		return super.read_short(_kind);
	}

	public void set_kind(short kind)
	{
		super.write(_kind, kind);
	}

	public boolean is_instance_klass()
	{
		short kind = _kind();
		return kind >= Klass.InstanceKlassKind && kind < Klass.TypeArrayKlassKind;
	}

	public KlassFlags _misc_flags()
	{
		return super.read_memory_object(KlassFlags.class, _misc_flags);
	}

	/**
	 * 第一个子类
	 * 
	 * @return
	 */
	public Klass _subklass()
	{
		return super.read_memory_object_ptr(Klass.class, _subklass);
	}

	/**
	 * _subklass()._next_sibling()链式访问所有子类
	 * 
	 * @return
	 */

	public Klass _next_sibling()
	{
		return super.read_memory_object_ptr(Klass.class, _next_sibling);
	}

	/**
	 * 链式访问加载本类的ClassLoader的加载的全部类
	 * 
	 * @return
	 */
	public Klass _next_link()
	{
		return super.read_memory_object_ptr(Klass.class, _next_link);
	}

	/**
	 * ClassLoader的数据
	 * 
	 * @return
	 */
	ClassLoaderData _class_loader_data()
	{
		return super.read_memory_object_ptr(ClassLoaderData.class, _class_loader_data);
	}
}
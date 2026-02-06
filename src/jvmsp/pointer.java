package jvmsp;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * C++指针，使用机器的绝对内存地址<br>
 * 不要用于取对象地址，短时间内可能不会出问题，但对象会随着GC过程移动，原先的地址会失效。<br>
 * 主要配合memory使用，对分配的固定地址内存进行操作。
 */
public class pointer
{
	/**
	 * C++层的指针转换为(void*)(uint64_t)addr
	 */
	long addr;

	/**
	 * Java类型
	 */
	Class<?> ptr_jtype;

	/**
	 * C++类型
	 */
	cxx_type ptr_cxx_type;

	/**
	 * 指针算术运算的步长，与类型有关，以byte为单位
	 */
	long stride;

	/**
	 * 缓存指针类型的klass word，每次cast()的时候更新值
	 */
	long ptr_type_klass_word;

	static final Class<?> void_ptr_type = void.class;

	/**
	 * 仅指针算术运算使用！
	 * 
	 * @param addr
	 * @param type
	 * @param stride
	 * @param ptr_type_klass_word
	 */
	private pointer(long addr, Class<?> type, long stride, long ptr_type_klass_word)
	{
		this.addr = addr;
		this.ptr_jtype = type;
		this.stride = stride;
		this.ptr_type_klass_word = ptr_type_klass_word;
	}

	/**
	 * C++对象指针
	 * 
	 * @param addr
	 * @param type
	 */
	private pointer(long addr, cxx_type type)
	{
		this.addr = addr;
		this.ptr_cxx_type = type;
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
		this.ptr_jtype = ptr.ptr_jtype;
		this.ptr_cxx_type = ptr.ptr_cxx_type;
		this.stride = ptr.stride;
		this.ptr_type_klass_word = ptr.ptr_type_klass_word;
	}

	private pointer(long addr, Class<?> type)
	{
		this.addr = addr;
		cast(type);
	}

	private pointer(long addr)
	{
		this(addr, void_ptr_type);
	}

	private pointer(String hex, Class<?> type)
	{
		this.addr = Long.decode(hex.strip().toLowerCase());
		cast(type);
	}

	private pointer(String hex)
	{
		this(hex, void_ptr_type);
	}

	public static final pointer nullptr;

	public long address()
	{
		return addr;
	}

	public Class<?> type()
	{
		return ptr_jtype;
	}

	public boolean is_nullptr()
	{
		return addr == 0;
	}

	public boolean is_void_ptr_type()
	{
		return ptr_jtype == void_ptr_type || ptr_cxx_type.equals(cxx_type.pointer);
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
		return Objects.hashCode(addr) ^ Objects.hashCode(ptr_jtype) ^ Objects.hashCode(ptr_jtype);
	}

	/**
	 * 把给定的地址和类型包装为指针
	 * 
	 * @param addr
	 * @param type
	 * @return
	 */
	public static final pointer at(long addr, Class<?> type)
	{
		return new pointer(addr, type);
	}

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
	public static final pointer at(String hex, Class<?> type)
	{
		return new pointer(hex, type);
	}

	public static final pointer at(String hex)
	{
		return pointer.at(0);
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
	public pointer cast(Class<?> dest_type)
	{
		this.ptr_jtype = dest_type;
		this.ptr_cxx_type = null;
		this.stride = java_type.sizeof(dest_type);
		if (!java_type.is_primitive(dest_type))
		{
			// 每次cast()的时候更新目标对象的类型
			ptr_type_klass_word = java_type.get_klass_word(dest_type);
		}
		return this;
	}

	public pointer cast(cxx_type dest_type)
	{
		this.ptr_jtype = null;
		this.ptr_cxx_type = dest_type;
		return null;
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
		this.addr = Long.decode(hex.strip().toLowerCase());
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
		return new pointer(addr + stride * step, ptr_jtype, stride, ptr_type_klass_word);
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
		return new pointer(addr - stride * step, ptr_jtype, stride, ptr_type_klass_word);
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

	static
	{
		nullptr = pointer.at(address_of_object(null), void_ptr_type);
	}

	/**
	 * 获取对象的地址，返回long<br>
	 * 利用Object[]的元素为oop指针的事实来间接取地址，该地址为JVM内部相对地址，不一定是实际的绝对地址。该地址可直接用于InternalUnsafe的相关方法<br>
	 * 在32位和未启用UseCompressedOops的64位JVM上，取的地址直接就是绝对地址。<br>
	 * 在开启UseCompressedOops的64位JVM上，取的地址是相对偏移量，需要乘以字节对齐量（字节对齐默认为8）或者左移（3位）+相对偏移量为0的基地址（即nullptr的绝对地址）才是绝对地址。
	 * 
	 * @param jobject
	 * @return
	 */
	static final long address_of_object(Object jobject)
	{
		return unsafe.native_address_of(new Object[]
		{ jobject }, unsafe.ARRAY_OBJECT_BASE_OFFSET);
	}

	/**
	 * 取对象地址，即andress_of_object()。基本类型都是by value传递参数入栈，取地址没意义，因此只能取对象的地址。<br>
	 * 如果要取对象的字段（可能是基本类型）的地址，使用本方法的其他重载方法。
	 * 
	 * @param jobject
	 * @return
	 */
	public static final pointer address_of(Object jobject)
	{
		return pointer.at(address_of_object(jobject), jobject == null ? void_ptr_type : jobject.getClass());
	}

	/**
	 * 将引用转换为指针
	 * 
	 * @param ref
	 * @return
	 */
	public static final pointer address_of(reference ref)
	{
		return pointer.at(ref.address_of_reference(), ref.ref_type);
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
			return pointer.at(address_of_object(unsafe.static_field_base(field)) + unsafe.static_field_offset(field), field.getType());
		else
			return pointer.at(address_of_object(jobject) + unsafe.object_field_offset(field), field.getType());
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
		if (is_void_ptr_type())
			throw new RuntimeException("Cannot dereference a void* pointer at " + this.toString());
		else if (ptr_cxx_type != null)
			return ptr_cxx_type.new object(this);
		else if (ptr_jtype == byte.class)
			return unsafe.read_byte(null, addr);
		else if (ptr_jtype == char.class)
			return unsafe.read_char(null, addr);
		else if (ptr_jtype == boolean.class)
			return unsafe.read_bool(null, addr);
		else if (ptr_jtype == short.class)
			return unsafe.read_short(null, addr);
		else if (ptr_jtype == int.class)
			return unsafe.read_int(null, addr);
		else if (ptr_jtype == float.class)
			return unsafe.read_float(null, addr);
		else if (ptr_jtype == long.class)
			return unsafe.read_long(null, addr);
		else if (ptr_jtype == double.class)
			return unsafe.read_double(null, addr);
		else
		{
			Object deref_obj = dereference_object(addr);
			java_type.set_klass_word(deref_obj, ptr_type_klass_word);
			return deref_obj;
		}
	}

	/**
	 * 设置指针指向的地址的值，如果是对象则拷贝对象的字段，对象头保持不动。
	 * 
	 * @param v
	 * @return 返回指针本身
	 */
	public pointer dereference_assign(Object v)
	{
		// 不可对void*类型的指针取值
		if (is_void_ptr_type())
			throw new RuntimeException("Cannot dereference a void* pointer at " + this.toString());

		else if (ptr_jtype == byte.class)
			unsafe.write(null, addr, java_type.byte_value(v));
		else if (ptr_jtype == char.class)
			unsafe.write(null, addr, java_type.char_value(v));
		else if (ptr_jtype == boolean.class)
			unsafe.write(null, addr, java_type.boolean_value(v));
		else if (ptr_jtype == short.class)
			unsafe.write(null, addr, java_type.short_value(v));
		else if (ptr_jtype == int.class)
			unsafe.write(null, addr, java_type.int_value(v));
		else if (ptr_jtype == float.class)
			unsafe.write(null, addr, java_type.float_value(v));
		else if (ptr_jtype == long.class)
			unsafe.write(null, addr, java_type.long_value(v));
		else if (ptr_jtype == double.class)
			unsafe.write(null, addr, java_type.double_value(v));
		else
			unsafe.__memcpy(v, java_type.HEADER_BYTE_LENGTH, dereference(), java_type.HEADER_BYTE_LENGTH, java_type.sizeof_object(v.getClass()) - java_type.HEADER_BYTE_LENGTH);// 只拷贝字段，不覆盖对象头
		return this;
	}

	public final void print_memory(long size)
	{
		pointer indicator = this.copy().cast(byte.class);
		for (int i = 0; i < size; ++i, indicator.inc())
		{
			System.out.print(String.format("%02x", cxx_type.uint_ptr((byte) indicator.dereference())) + " ");
		}
	}
}

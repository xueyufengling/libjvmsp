package jvmsp;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;

/**
 * 修改枚举、创建新的枚举值实例
 * 
 * @param <_T>
 */
public interface mutable_enum<_T extends Enum<_T>> extends _crtp<_T> {

	public default _T of(Class<?>[] arg_types, Object... args) {
		return of("$tmp", -1, arg_types, args);
	}

	public default _T of(String name, int ordinal, Class<?>[] arg_types, Object... args) {
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
	public static <_T extends Enum<_T>> _T of(Class<_T> target_enum, String name, int ordinal, Class<?>[] arg_types, Object... args) {
		MethodHandle constructor = symbols.find_constructor(target_enum, _enum_constructor_arg_types(arg_types));
		try {
			return (_T) constructor.invokeWithArguments(arrays.cat(name, ordinal, args));
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static <_T extends Enum<_T>> _T of(Class<_T> target_enum, Class<?>[] arg_types, Object... args) {
		return of(target_enum, "$tmp", -1, arg_types, args);
	}

	/**
	 * 通过定义的枚举的构造函数类型获取实际的构造函数类型。<br>
	 * 这是因为编译器会在枚举类的构造函数声明的构造函数参数最前方自动添加两个额外参数，如果不加入这两个额外参数，就找不到枚举的构造函数。<br>
	 * 自动添加的两个参数是String name：枚举值的字符串名称、int ordinal枚举值的序号，从0开始计数。
	 * 
	 * @param ctor_arg_types
	 * @return
	 */
	private static Class<?>[] _enum_constructor_arg_types(Class<?>... ctor_arg_types) {
		return arrays.cat(String.class, int.class, ctor_arg_types);
	}

	/**
	 * 为目标枚举设置values()
	 * 
	 * @param <_T>
	 * @param target_enum
	 * @param values
	 */
	@SuppressWarnings("unchecked")
	public static <_T extends Enum<_T>> void set(Class<_T> target_enum, _T... values) {
		VarHandle __ENUM$VALUES = symbols.find_static_var(target_enum, "ENUM$VALUES", target_enum.arrayType());
		__ENUM$VALUES.set(values);
	}
}

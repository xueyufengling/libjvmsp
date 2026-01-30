package jvmsp;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 反射工具，大部分功能可以直接使用Manipulator调用
 */
public abstract class reflection {
	private static Class<?> class_jdk_internal_reflect_Reflection = null;

	/**
	 * 反射的过滤字段表，位于该map的字段无法被反射获取
	 */
	private static VarHandle Reflection_fieldFilterMap;

	/**
	 * 反射的过滤方法表，位于该map的方法无法被反射获取
	 */
	private static VarHandle Reflection_methodFilterMap;

	/**
	 * 64位JVM的offset从12开始为数据段，此处为java.lang.reflect.AccessibleObject的boolean override成员，将该成员覆写为true可以无视权限调用Method、Field、Constructor
	 */
	private static VarHandle java_lang_reflect_AccessibleObject_override;

	static {
		java_lang_reflect_AccessibleObject_override = symbols.find_var(AccessibleObject.class, "override", boolean.class);
		try {
			class_jdk_internal_reflect_Reflection = Class.forName("jdk.internal.reflect.Reflection");
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		Reflection_fieldFilterMap = symbols.find_static_var(class_jdk_internal_reflect_Reflection, "fieldFilterMap", Map.class);
		Reflection_methodFilterMap = symbols.find_static_var(class_jdk_internal_reflect_Reflection, "methodFilterMap", Map.class);
	}

	/**
	 * 初始化一个类
	 * 
	 * @param cls
	 * @throws ClassNotFoundException
	 */
	public static final void initialize_class(Class<?> cls) throws ClassNotFoundException {
		Class.forName(cls.getName(), true, cls.getClassLoader());
	}

	/**
	 * 无视权限设置是否可访问
	 * 
	 * @param <AO>
	 * @param accessible_obj
	 * @param accessible
	 * @return
	 */
	public static <AO extends AccessibleObject> AO set_accessible(AO accessible_obj, boolean accessible) {
		java_lang_reflect_AccessibleObject_override.set(accessible_obj, accessible);
		return accessible_obj;
	}

	public static <AO extends AccessibleObject> AO set_accessible(AO accessible_obj) {
		return set_accessible(accessible_obj, true);
	}

	/**
	 * 获取反射过滤的字段
	 * 
	 * @return
	 */
	public static Map<Class<?>, Set<String>> get_field_filter() {
		return (Map<Class<?>, Set<String>>) Reflection_fieldFilterMap.get();
	}

	/**
	 * 获取反射过滤的方法
	 * 
	 * @return
	 */
	public static Map<Class<?>, Set<String>> get_method_filter() {
		return (Map<Class<?>, Set<String>>) Reflection_methodFilterMap.get();
	}

	/**
	 * 设置字段反射过滤，Java设置了一些非常核心的类无法通过反射获取即设置反射过滤，此操作将会替换原有的过滤限制。危险操作。
	 */
	public static void set_field_filter(Map<Class<?>, Set<String>> filter_map) {
		Reflection_fieldFilterMap.set(filter_map);
	}

	/**
	 * 设置方法反射过滤，Java设置了一些非常核心的类无法通过反射获取即设置反射过滤，此操作将会替换原有的过滤限制。危险操作。
	 */
	public static void set_method_filter(Map<Class<?>, Set<String>> filter_map) {
		Reflection_methodFilterMap.set(filter_map);
	}

	/**
	 * 移除反射过滤，使得全部字段均可通过反射获取，Java设置了一些非常核心的类无法通过反射获取即设置反射过滤，此操作将会移除该限制。危险操作。
	 */
	public static void remove_field_filter() {
		set_field_filter(new HashMap<Class<?>, Set<String>>());
	}

	/**
	 * 移除反射过滤，使得全部方法均可通过反射获取，Java设置了一些非常核心的类无法通过反射获取即设置反射过滤，此操作将会移除该限制。危险操作。
	 */
	public static void remove_method_filter() {
		set_method_filter(new HashMap<Class<?>, Set<String>>());
	}

	/**
	 * 在没有反射字段过滤器的环境下操作
	 * 
	 * @param op
	 */
	public static final void no_field_filter(Runnable op) {
		Map<Class<?>, Set<String>> filter_map = get_field_filter();
		remove_field_filter();
		op.run();
		set_field_filter(filter_map);
	}

	/**
	 * 不经过反射过滤获取字段
	 * 
	 * @param cls
	 * @param field_name
	 * @return
	 */
	public static final Field no_field_filter_find(Class<?> cls, String field_name) {
		Field f = null;
		Map<Class<?>, Set<String>> filterMap = get_field_filter();
		remove_field_filter();
		f = find_field(cls, field_name);
		set_field_filter(filterMap);
		return f;
	}

	private static MethodHandle Class_getDeclaredFields0;// Class.getDeclaredFields0无视反射访问权限获取字段
	private static MethodHandle Class_privateGetDeclaredFields;
	private static MethodHandle Class_getDeclaredMethods0;
	private static MethodHandle Class_privateGetDeclaredMethods;
	private static MethodHandle Class_getDeclaredConstructors0;
	private static MethodHandle Class_searchFields;
	private static MethodHandle Class_searchMethods;
	private static MethodHandle Class_getConstructor0;
	private static MethodHandle Class_forName0;
	private static MethodHandle Reflection_isCallerSensitive;

	static {
		try {
			Class_getDeclaredFields0 = symbols.find_special_method(Class.class, Class.class, "getDeclaredFields0", Field[].class, boolean.class);
			Class_privateGetDeclaredFields = symbols.find_special_method(Class.class, Class.class, "privateGetDeclaredFields", Field[].class, boolean.class);
			Class_getDeclaredMethods0 = symbols.find_special_method(Class.class, Class.class, "getDeclaredMethods0", Method[].class, boolean.class);
			Class_privateGetDeclaredMethods = symbols.find_special_method(Class.class, Class.class, "privateGetDeclaredMethods", Method[].class, boolean.class);
			Class_getDeclaredConstructors0 = symbols.find_special_method(Class.class, Class.class, "getDeclaredConstructors0", Constructor[].class, boolean.class);
			Class_searchFields = symbols.find_static_method(Class.class, "searchFields", Field.class, Field[].class, String.class);
			Class_searchMethods = symbols.find_static_method(Class.class, "searchMethods", Method.class, Method[].class, String.class, Class[].class);
			Class_getConstructor0 = symbols.find_special_method(Class.class, Class.class, "getConstructor0", Constructor.class, Class[].class, int.class);
			Class_forName0 = symbols.find_static_method(Class.class, "forName0", Class.class, String.class, boolean.class, ClassLoader.class, Class.class);
			Reflection_isCallerSensitive = symbols.find_static_method(class_jdk_internal_reflect_Reflection, "isCallerSensitive", boolean.class, Method.class);
		} catch (SecurityException | IllegalArgumentException ex) {
			ex.printStackTrace();
		}
	}

	public static Field set_accessible(Class<?> cls, String field_name, boolean accessible) {
		Field f = reflection.find_field(cls, field_name);
		set_accessible(f, accessible);
		return f;
	}

	/**
	 * 获取对象定义的字段原root对象，无视反射过滤和访问权限，直接调用JVM内部的native方法获取全部字段。<br>
	 * 注意：本方法没有拷贝对象，因此对返回字段的任何修改都将反应在反射系统获取的所有的复制对象中
	 * 
	 * @param clazz 要获取的类
	 * @return 字段列表
	 */
	public static Field[] __get_declared_fields(Class<?> clazz, boolean public_only) {
		try {
			return (Field[]) Class_getDeclaredFields0.invokeExact(clazz, public_only);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static Field[] __get_declared_fields(Class<?> clazz) {
		return __get_declared_fields(clazz, false);
	}

	public static Field __get_declared_field(Class<?> clazz, String field_name) {
		try {
			return (Field) Class_searchFields.invokeExact(__get_declared_fields(clazz), field_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static Field[] get_declared_fields(Class<?> clazz, boolean public_only) {
		try {
			return (Field[]) Class_privateGetDeclaredFields.invokeExact(clazz, public_only);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static Field[] get_declared_fields(Class<?> clazz) {
		return get_declared_fields(clazz, false);
	}

	public static Field get_declared_field(Class<?> clazz, String field_name) {
		try {
			return (Field) Class_searchFields.invokeExact(get_declared_fields(clazz), field_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取对象定义的方法原root对象，无视反射过滤和访问权限，直接调用JVM内部的native方法获取全部方法
	 * 
	 * @param clazz 要获取的类
	 * @return 字段列表
	 */
	public static Method[] __get_declared_methods(Class<?> clazz, boolean public_only) {
		try {
			return (Method[]) Class_getDeclaredMethods0.invokeExact(clazz, false);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static Method[] __get_declared_methods(Class<?> clazz) {
		return __get_declared_methods(clazz, false);
	}

	public static Method __get_declared_method(Class<?> clazz, String method_name, Class<?>... arg_types) {
		try {
			return (Method) Class_searchMethods.invokeExact(__get_declared_methods(clazz), method_name, arg_types);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取Class对象缓存的root对象，除非类被重载否则不再改变。
	 * 
	 * @param clazz
	 * @param public_only
	 * @return
	 */
	public static Method[] get_declared_methods(Class<?> clazz, boolean public_only) {
		try {
			return (Method[]) Class_privateGetDeclaredMethods.invokeExact(clazz, false);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static Method[] get_declared_methods(Class<?> clazz) {
		return get_declared_methods(clazz, false);
	}

	public static Method get_declared_method(Class<?> clazz, String method_name, Class<?>... arg_types) {
		try {
			return (Method) Class_searchMethods.invokeExact(get_declared_methods(clazz), method_name, arg_types);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 目标方法是否是静态的。该方法主要用于MethodHandle查找。
	 * 
	 * @param clazz
	 * @param method_name
	 * @param arg_types
	 * @return
	 */
	public static boolean is_static(Class<?> clazz, String method_name, Class<?>... arg_types) {
		Method m = reflection.get_declared_method(clazz, method_name, arg_types);
		return Modifier.isStatic(m.getModifiers());
	}

	/**
	 * 查找构造函数的原始对象
	 * 
	 * @param <T>
	 * @param clazz
	 * @param public_only
	 * @return
	 */
	public static <T> Constructor<T>[] __get_declared_constructors(Class<?> clazz, boolean public_only) {
		try {
			return (Constructor<T>[]) Class_getDeclaredConstructors0.invokeExact(clazz, public_only);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static <T> Constructor<T>[] __get_declared_constructors(Class<?> clazz) {
		return __get_declared_constructors(clazz, false);
	}

	/**
	 * 获取root构造函数
	 * 
	 * @param <T>
	 * @param clazz
	 * @param which    java.lang.reflect.Member接口中的访问类型，Member.DECLARED为全部定义的构造函数，Member.PUBLIC为public的构造函数
	 * @param argTypes 构造函数的参数类型
	 * @return
	 */
	public static <T> Constructor<T> __get_declared_constructor(Class<T> clazz, int which, Class<?>... argTypes) {
		try {
			return (Constructor<T>) Class_getConstructor0.invokeExact(clazz, argTypes, which);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static <T> Constructor<T> __get_declared_constructor(Class<T> clazz, Class<?>... argTypes) {
		return __get_declared_constructor(clazz, Member.DECLARED, argTypes);
	}

	/**
	 * 查找类
	 * 
	 * @param name
	 * @param initialize
	 * @param loader
	 * @param caller
	 * @return
	 */
	public static Class<?> find_class(String name, boolean initialize, ClassLoader loader, Class<?> caller) {
		try {
			return (Class<?>) Class_forName0.invokeExact(name, initialize, loader, caller);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@CallerSensitive
	public static Class<?> find_class(String name, boolean initialize) {
		Class<?> caller = JavaLang.caller_class();
		return find_class(name, initialize, caller.getClassLoader(), caller);
	}

	@CallerSensitive
	public static Class<?> find_class(String name) {
		Class<?> caller = JavaLang.caller_class();
		return find_class(name, true, caller.getClassLoader(), caller);
	}

	public static Class<?> find_sys_class(String name, boolean initialize) {
		return find_class(name, initialize, null, Class.class);
	}

	public static Class<?> find_sys_class(String name) {
		return find_sys_class(name, true);
	}

	/**
	 * 获取类名
	 * 
	 * @param full_name 带有包的完整类名
	 * @return
	 */
	public static String class_name(String full_name) {
		return full_name.substring(full_name.lastIndexOf('.') + 1);
	}

	public static String class_name(Object obj) {
		return class_name(obj.getClass().getName());
	}

	public static String package_name(String full_name) {
		return full_name.substring(0, full_name.lastIndexOf('.'));
	}

	/**
	 * 查询类成员，如果该类没有则递归查找父类
	 */
	public static Field find_field(Object obj, String name) {
		Class<?> cls;
		if (obj instanceof Class<?> c)
			cls = c;
		else
			cls = obj.getClass();
		Field result = get_declared_field(cls, name);
		if (result == null) {
			Class<?> supercls = cls.getSuperclass();
			if (supercls == null) {
				throw new IllegalArgumentException("cannot find field " + name + " in " + obj);
			} else
				return find_field(supercls, name);
		}
		return result;
	}

	public static Object read(Object obj, Field field) {
		if (obj == null || field == null)
			return null;
		try {
			set_accessible(field, true);
			return field.get(obj);
		} catch (IllegalAccessException ex) {
			System.err.println("IllegalAccessException thrown when reading field " + field + " in " + obj);
			ex.printStackTrace();
		}
		return null;
	}

	public static Object read(Object obj, String field) {
		return read(obj, find_field(obj, field));
	}

	public static boolean write(Object obj, Field field, Object value) {
		if (obj == null || field == null)
			return false;
		try {
			set_accessible(field, true);
			field.set(obj, value);
		} catch (IllegalAccessException ex) {
			System.err.println("IllegalAccessException thrown when writing field " + field + " with value " + value + " in " + obj);
			ex.printStackTrace();
			;
			return false;
		}
		return true;
	}

	public static boolean write(Object obj, String field, Object value) {
		return write(obj, find_field(obj, field), value);
	}

	public static String method_description(String name, Class<?>... arg_types) {
		String method_description = name + '(';
		if (arg_types != null)
			for (int a = 0; a < arg_types.length; ++a) {
				method_description += arg_types[a].getName();
				if (a != arg_types.length - 1)
					method_description += ", ";
			}
		method_description += ')';
		return method_description;
	}

	/**
	 * 只搜寻该类自己的方法
	 * 
	 * @param clazz
	 * @param name
	 * @param arg_types
	 * @return
	 */
	public static Method find_method_self(Class<?> clazz, String name, Class<?>... arg_types) {
		return get_declared_method(clazz, name, arg_types == null ? (new Class<?>[] {}) : arg_types);
	}

	/**
	 * 只搜寻该类及其父类、实现接口的方法
	 * 
	 * @param clazz
	 * @param name
	 * @param arg_types
	 * @return
	 */
	public static Method find_method_inherited(Class<?> clazz, String name, Class<?>... arg_types) {
		Method result = get_declared_method(clazz, name, arg_types == null ? (new Class<?>[] {}) : arg_types);
		if (result == null) {
			Class<?> supercls = clazz.getSuperclass();
			Class<?>[] interfaces = clazz.getInterfaces();
			if (supercls == null && interfaces.length == 0) {
				System.err.println("cannot find method " + name + " in neither super class nor implemented interfaces");
				return null;
			} else {
				Method method = find_method_self(supercls, name, arg_types);
				if (method != null)// 如果父类有方法则优先返回父类的方法
					return method;
				else {// 从接口中搜寻方法
					for (Class<?> i : interfaces)
						method = find_method_self(i, name, arg_types);
				}
				return method;
			}
		}
		return result;
	}

	/**
	 * 从子类开始按照继承链依次查找并返回查找到的第一个结果
	 * 
	 * @param obj
	 * @param name
	 * @param arg_types
	 * @return
	 */
	public static Method find_method(Object obj, String name, Class<?>... arg_types) {
		Class<?> cls;
		if (obj instanceof Class<?> c)
			cls = c;
		else
			cls = obj.getClass();
		Method method = null;
		ArrayList<ArrayList<Class<?>>> chain = resolve_inherit_implament_chain(cls);
		FOUND: for (int depth = 0; depth < chain.size(); ++depth) {
			ArrayList<Class<?>> equal_depth_classes = chain.get(depth);
			for (int i = 0; i < equal_depth_classes.size(); ++i)
				if ((method = find_method_self(equal_depth_classes.get(i), name, arg_types)) != null)
					break FOUND;
		}
		if (method == null) {
			System.err.println("method " + method_description(name, arg_types) + " not found in class " + cls.getName() + " or its parents");
		}
		return method;
	}

	private static void _resolve_inherit_chain(Class<?> clazz, ArrayList<Class<?>> chain) {
		chain.add(clazz);
		Class<?> supercls = clazz.getSuperclass();
		if (supercls != null)
			_resolve_inherit_chain(supercls, chain);
	}

	public static Class<?>[] resolve_inherit_chain(Class<?> clazz) {
		ArrayList<Class<?>> chain = new ArrayList<>();
		_resolve_inherit_chain(clazz, chain);
		return chain.toArray(new Class<?>[chain.size()]);
	}

	private static ArrayList<ArrayList<Class<?>>> _resolve_inherit_implament_chain(Class<?> self, int current_depth, ArrayList<ArrayList<Class<?>>> chain) {
		ArrayList<Class<?>> current_depth_classes = null;
		while (current_depth_classes == null)
			try {
				current_depth_classes = chain.get(current_depth);
			} catch (IndexOutOfBoundsException ex) {
				chain.add(new ArrayList<>());
			}
		current_depth_classes.add(self);
		Class<?> supercls = self.getSuperclass();
		if (supercls != null)
			_resolve_inherit_implament_chain(supercls, current_depth + 1, chain);
		Class<?>[] interfaces = self.getInterfaces();
		for (Class<?> i : interfaces)
			_resolve_inherit_implament_chain(i, current_depth + 1, chain);
		return chain;
	}

	public static ArrayList<ArrayList<Class<?>>> resolve_inherit_implament_chain(Class<?> clazz) {
		ArrayList<ArrayList<Class<?>>> chain = new ArrayList<>();
		return _resolve_inherit_implament_chain(clazz, 0, chain);
	}

	/**
	 * 推断每个参数的类型，每个参数的类型均是一个数组，为该类型的继承链
	 * 
	 * @param args 要推断的参数列表
	 * @return
	 */
	public static Class<?>[][] resolve_arg_types_chain(Object... args) {
		Class<?>[][] arg_types = new Class<?>[args.length][];
		for (int idx = 0; idx < args.length; ++idx)
			arg_types[idx] = resolve_inherit_chain(args[idx].getClass());
		return arg_types;
	}

	/**
	 * 推断每个参数的类型，每个参数的类型均是传入参数本类型，不包括其父类继承链
	 * 
	 * @param args 要推断的参数列表
	 * @return
	 */
	public static Class<?>[] resolve_arg_types(Object... args) {
		Class<?>[] arg_types = new Class<?>[args.length];
		for (int idx = 0; idx < args.length; ++idx)
			arg_types[idx] = args[idx].getClass();
		return arg_types;
	}

	public static Object invoke(Object obj, String method_name, Class<?>[] arg_types, Object... args) {
		Method method = find_method(obj, method_name, arg_types);
		try {
			set_accessible(method, true);
			return method.invoke(obj, args);
		} catch (IllegalAccessException | InvocationTargetException ex) {
			System.err.println(ex.getClass().getSimpleName() + " thrown invoking method " + method_name + " with arguments " + args + " in object " + obj.toString());
			ex.printStackTrace();
			return null;
		}
	}

	public static Constructor<?> find_constructor(Object obj, Class<?>... arg_types) {
		Class<?> cls;
		if (obj instanceof Class<?> c)
			cls = c;
		else
			cls = obj.getClass();
		Constructor<?> result = __get_declared_constructor(cls, arg_types == null ? (new Class<?>[] {}) : arg_types);
		if (result == null) {
			Class<?> supercls = cls.getSuperclass();
			return supercls == null ? null : find_constructor(supercls, arg_types);
		}
		return result;
	}

	/**
	 * 利用反射调用构造函数
	 * 
	 * @param obj  目标类型的对象实例或Class<T>
	 * @param args
	 * @return
	 */
	public static Object construct(Object obj, Class<?>[] arg_types, Object... args) {
		Constructor<?> constructor = find_constructor(obj, arg_types);
		try {
			set_accessible(constructor, true);
			return constructor.newInstance(args);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			System.err.println("Reflection throws exception contructing " + obj.toString() + " with arguments " + args);
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 判断某个类是否具有指定超类，支持向上递归查找超类
	 * 
	 * @param clazz       要判断是否有超类的类
	 * @param super_class 超类
	 * @return clazz具有超类super_class则返回true，否则返回false
	 */
	public static boolean has_super(Class<?> clazz, Class<?> super_class) {
		Class<?> supercls = clazz.getSuperclass();
		if (supercls == super_class)
			return true;
		return supercls == null ? false : has_super(supercls, super_class);
	}

	/**
	 * f所声明的类型是否是type或者其子类
	 * 
	 * @param f
	 * @param type
	 * @return
	 */
	public static boolean is(Field f, Class<?> type) {
		return type.isAssignableFrom(f.getType());
	}

	/**
	 * 判断一个类是否是另一个类的子类
	 * 
	 * @param son
	 * @param parent
	 * @return
	 */
	public static boolean is(Class<?> son, Class<?> parent) {
		return parent.isAssignableFrom(son);
	}

	/**
	 * jdk.internal.reflect.Reflection.isCallerSensitive()
	 * 
	 * @param m
	 * @return
	 */
	public static boolean is_caller_sensitive(Method m) {
		try {
			return (boolean) Reflection_isCallerSensitive.invokeExact(m);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return false;
	}

	/**
	 * 内部类相关
	 */

	/**
	 * 获取指定类型的外部类引用
	 * 
	 * @param <T>
	 * @param target 要获取的外部类类型
	 * @param obj    要获取外部类引用的对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T enclosing_class_instance(Class<T> target, Object obj) {
		if (target == null || obj == null) {
			return null;
		}
		Class<?> cls = obj.getClass();
		Class<?> outerCls = cls.getEnclosingClass();
		if (outerCls == null) {
			return null;// obj没有外部类
		}
		T outerObj = null;
		Field[] fields = reflection.get_declared_fields(cls);
		for (Field field : fields) {
			if (field.getType() == outerCls && field.getName().startsWith("this$")) {// 找到上一层外部类的实例引用字段
				outerObj = (T) ObjectManipulator.access(obj, field);
				if (outerCls == target)
					return outerObj;
				else
					return enclosing_class_instance(target, outerObj);
			}
		}
		return null;
	}

	/**
	 * 获取当前对象上一层的外部类引用
	 * 
	 * @param obj
	 * @return
	 */
	public static Object enclosing_class_instance(Object obj) {
		return obj == null ? null : enclosing_class_instance(obj.getClass().getEnclosingClass(), obj);
	}

	/**
	 * 包相关
	 */
	public static List<String> class_names_in_package(Class<?> any_class_in_package, file_system.uri.resolver resolver, String package_name, boolean include_subpackage) {
		String path = file_system.classpath(any_class_in_package, resolver);
		if (path.endsWith(file_system.JAR_EXTENSION_NAME))
			return file_system.class_names_in_jar(any_class_in_package, resolver, package_name, include_subpackage);
		else
			return file_system.class_names_local(any_class_in_package, resolver, package_name, include_subpackage);
	}

	public static List<String> class_names_in_package(Class<?> any_class_in_package, String package_name, boolean include_subpackage) {
		return class_names_in_package(any_class_in_package, file_system.uri.resolver.DEFAULT, package_name, include_subpackage);
	}

	public static List<String> class_names_in_package(String package_name, boolean include_subpackage) {
		Class<?> caller = JavaLang.caller_class();
		return class_names_in_package(caller, package_name, include_subpackage);// 获取调用该方法的类
	}

	public static List<String> class_names_in_package(Class<?> any_class_in_package, String package_name) {
		return class_names_in_package(any_class_in_package, package_name, false);
	}

	public static List<String> class_names_in_package(String package_name) {
		Class<?> caller = JavaLang.caller_class();
		return class_names_in_package(caller, package_name);// 获取调用该方法的类
	}

	/**
	 * 注解相关
	 */
	private static VarHandle AnnotationData_annotations;
	private static VarHandle AnnotationData_declaredAnnotations;
	private static MethodHandle Class_annotationData;
	private static MethodHandle Field_declaredAnnotations;
	private static MethodHandle Executable_declaredAnnotations;

	static {
		Class<?> class_AnnotationData = reflection.find_class("java.lang.Class$AnnotationData");
		Class_annotationData = symbols.find_special_method(Class.class, Class.class, "annotationData", class_AnnotationData);
		AnnotationData_annotations = symbols.find_var(class_AnnotationData, "annotations", Map.class);
		AnnotationData_declaredAnnotations = symbols.find_var(class_AnnotationData, "declaredAnnotations", Map.class);
		Field_declaredAnnotations = symbols.find_special_method(Field.class, "declaredAnnotations", Map.class);
		Executable_declaredAnnotations = symbols.find_special_method(Executable.class, Executable.class, "declaredAnnotations", Map.class);
	}

	/**
	 * 获取类缓存的注解数据，Class.getAnnotation()获取的注解都是此处缓存的注解数据
	 * 
	 * @param cls
	 * @return
	 */
	public static Map<Class<? extends Annotation>, Annotation> cached_annotations(Class<?> cls) {
		return (Map<Class<? extends Annotation>, Annotation>) AnnotationData_annotations.get(cls);
	}

	/**
	 * 获取声明注解Map，对于Class而言，使用的是缓存注解而非该声明注解。
	 * 
	 * @param e
	 * @return
	 */
	public static Map<Class<? extends Annotation>, Annotation> declared_annotations(AnnotatedElement ae) {
		try {
			if (ae instanceof Class cls)
				return (Map<Class<? extends Annotation>, Annotation>) AnnotationData_declaredAnnotations.get(Class_annotationData.invokeExact(cls));
			else if (ae instanceof Field f)
				return (Map<Class<? extends Annotation>, Annotation>) Field_declaredAnnotations.invokeExact(f);
			else if (ae instanceof Executable e)
				return (Map<Class<? extends Annotation>, Annotation>) Executable_declaredAnnotations.invokeExact(e);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 运行时实际被使用的注解数据
	 * 
	 * @param ae
	 * @return
	 */
	public static Map<Class<? extends Annotation>, Annotation> actual_used_annotations(AnnotatedElement ae) {
		if (ae instanceof Class cls)
			return cached_annotations(cls);
		else
			return declared_annotations(ae);
	}

	/**
	 * 获取被注解的元素所在的类
	 * 
	 * @param ae
	 * @return
	 */
	public static Class<?> declaring_class(AnnotatedElement ae) {
		if (ae instanceof Class cls)
			return cls;
		else if (ae instanceof Field f)
			return f.getDeclaringClass();
		else if (ae instanceof Executable e)
			return e.getDeclaringClass();
		return null;
	}

	@SuppressWarnings("unchecked")
	public static void cast(AnnotatedElement ae, Class<? extends Annotation> target_annotation_cls, Class<?> dest_annotation_cls, Annotation dest_annotation) {
		// 判断一个AnnotatedElement是否有某个注解，实际是判断缓存的注解map是否存在指定注解Class<?>的key
		Map<Class<? extends Annotation>, Annotation> annoMap = actual_used_annotations(ae);
		Annotation mirror_annotation = annoMap.remove(target_annotation_cls);// 移除镜像注解的key
		Mirror.cast(mirror_annotation, dest_annotation);
		annoMap.put((Class<? extends Annotation>) dest_annotation_cls, mirror_annotation);// 填入目标注解
	}

	public static void cast(AnnotatedElement ae, Class<? extends Annotation> target_annotation_cls, Annotation dest_annotation) {
		cast(ae, target_annotation_cls, dest_annotation.annotationType(), dest_annotation);
	}

	/**
	 * 如果目标注解的类型和实际获取的对象annotationType()类型不一致，那么需要手动传入目标注解类型。<br>
	 * 
	 * @param ae
	 * @param target_annotation_cls
	 * @param dest_annotation_cls
	 * @param dest_annotation
	 */
	@SuppressWarnings("unchecked")
	public static void replace(AnnotatedElement ae, Class<? extends Annotation> target_annotation_cls, Class<?> dest_annotation_cls, Annotation dest_annotation) {
		// 判断一个AnnotatedElement是否有某个注解，实际是判断缓存的注解map是否存在指定注解Class<?>的key
		Map<Class<? extends Annotation>, Annotation> annoMap = actual_used_annotations(ae);
		annoMap.remove(target_annotation_cls);// 移除镜像注解的key
		annoMap.put((Class<? extends Annotation>) dest_annotation_cls, dest_annotation);// 填入目标注解
	}

	public static void replace(AnnotatedElement ae, Class<? extends Annotation> target_annotation_cls, Annotation dest_annotation) {
		replace(ae, target_annotation_cls, dest_annotation.annotationType(), dest_annotation);
	}

	/**
	 * 泛型相关
	 */
	public static enum entry_type {
		INTERFACE, CLASS, RAW_TYPE, UPPER_BOUNDS, LOWER_BOUNDS
	}

	/**
	 * 单个类或上界类数组、下界类数组
	 */
	public static class generic_entry {
		public final entry_type type;
		private Class<?>[] result;

		generic_entry(entry_type type, Class<?>... result) {
			this.type = type;
			this.result = result;
		}

		public Class<?> single_type() {
			if (type == entry_type.INTERFACE | type == entry_type.CLASS || type == entry_type.RAW_TYPE)
				return result[0];
			else
				return null;
		}

		public Class<?> type() {
			return result[0];
		}

		public Class<?> type(int idx) {
			return result[idx];
		}

		public Class<?>[] upper_bounds() {
			if (type == entry_type.UPPER_BOUNDS)
				return result;
			else
				return null;
		}

		public Class<?>[] lower_bounds() {
			if (type == entry_type.LOWER_BOUNDS)
				return result;
			else
				return null;
		}

		/**
		 * 判断是否result中存在任意一个Class<?>严格地是cls类
		 * 
		 * @param cls
		 * @return
		 */
		public boolean equals_any(Class<?> cls) {
			for (Class<?> c : result)
				if (cls == c)
					return true;
			return false;
		}

		/**
		 * 判断是否result中存在任意一个Class<?>是cls或其子类
		 * 
		 * @param cls
		 * @return
		 */
		public boolean is_any(Class<?> cls) {
			for (Class<?> c : result)
				if (reflection.is(c, cls))
					return true;
			return false;
		}
	}

	/**
	 * 字段的泛型参数是否是某些类型
	 * 
	 * @param f
	 * @param types
	 * @return
	 */
	public static boolean is(Field f, int[] indices, Class<?>... types) {
		generic_entry[] classes = generic_classes(f, indices);
		if (classes.length != types.length)
			return false;
		for (int idx = 0; idx < types.length; ++idx) {
			if (!classes[idx].is_any(types[idx]))
				return false;
		}
		return true;
	}

	/**
	 * 返回第一层的泛型参数是否匹配给定类列表
	 * 
	 * @param f
	 * @param types
	 * @return
	 */
	public static boolean is(Field f, Class<?>... types) {
		return is(f, new int[] {}, types);
	}

	/**
	 * 匹配前N个泛型参数是否和types一致
	 * 
	 * @param f
	 * @param types
	 * @return
	 */
	public static boolean generic_start_with(Field f, int[] indices, Class<?>... types) {
		generic_entry[] classes = generic_classes(f, indices);
		for (int idx = 0; idx < types.length; ++idx) {
			if (!classes[idx].is_any(types[idx]))
				return false;
		}
		return true;
	}

	public static boolean generic_start_with(Field f, Class<?>... types) {
		return generic_start_with(f, new int[] {}, types);
	}

	public static generic_entry[] generic_classes(Field f, int... indices) {
		return generic_classes(f.getGenericType(), indices);
	}

	/**
	 * 获取指定嵌套深度索引的泛型参数
	 * 
	 * @param currentType
	 * @param indices
	 * @return
	 */
	public static Type generic_type(Type currentType, int... indices) {
		Type[] actualTypeArguments = null;
		for (int nest_depth = 0; nest_depth < indices.length; ++nest_depth) {
			// 没有泛型参数则直接返回
			if (currentType instanceof ParameterizedType currentParameterizedType) {
				int nest_idx = indices[nest_depth];
				actualTypeArguments = currentParameterizedType.getActualTypeArguments();
				// 索引超出该深度的泛型参数个数
				if (nest_idx < 0 || nest_idx >= actualTypeArguments.length) {
					return null;
				}
				// 除非是最后一层，否则继续向下查找
				currentType = actualTypeArguments[nest_idx];
			} else
				return null;
		}
		return currentType;
	}

	/**
	 * 获取指定字段的指定嵌套深度的泛型参数的Class<?>
	 * 
	 * @param currentType 当前的类型
	 * @param indices     从最外层开始，向内的索引
	 * @return
	 */
	public static generic_entry[] generic_classes(Type currentType, int... indices) {
		Type[] actualTypeArguments = null;
		currentType = generic_type(currentType, indices);
		// 获取最终深度的特定索引的全部泛型参数
		if (currentType instanceof ParameterizedType pt)
			actualTypeArguments = pt.getActualTypeArguments();
		else
			actualTypeArguments = new Type[] { currentType };
		generic_entry[] entries = new generic_entry[actualTypeArguments.length];
		for (int idx = 0; idx < actualTypeArguments.length; ++idx) {
			currentType = actualTypeArguments[idx];
			if (currentType instanceof Class cls) {
				entries[idx] = new generic_entry(entry_type.CLASS, cls);
				continue;
			}
			// 如果参数还是泛型类，就直接getRawType()
			else if (currentType instanceof ParameterizedType parameterizedType) {
				Type rawType = parameterizedType.getRawType();
				if (rawType instanceof Class cls) {
					entries[idx] = new generic_entry(entry_type.RAW_TYPE, cls);
					continue;
				}
			} else if (currentType instanceof WildcardType wildcardType) {
				Type[] upper_bounds = wildcardType.getUpperBounds();
				Type[] lower_bounds = wildcardType.getLowerBounds();
				if (upper_bounds.length != 0) {
					Class<?>[] upper_bounds_clsarr = new Class[upper_bounds.length];
					for (int i = 0; i < upper_bounds_clsarr.length; ++i) {
						upper_bounds_clsarr[idx] = _resolve_type_class(upper_bounds[i]);
					}
					entries[idx] = new generic_entry(entry_type.UPPER_BOUNDS, upper_bounds_clsarr);
				} else if (lower_bounds.length != 0) {
					Class<?>[] lower_bounds_clsarr = new Class[lower_bounds.length];
					for (int i = 0; i < lower_bounds_clsarr.length; ++i) {
						lower_bounds_clsarr[idx] = _resolve_type_class(lower_bounds[i]);
					}
					entries[idx] = new generic_entry(entry_type.LOWER_BOUNDS, lower_bounds_clsarr);
				}
				continue;
			} else
				entries[idx] = null;
		}
		return entries;
	}

	/**
	 * 如果currentType是Class<?>则直接返回class，如果是带泛型参数的class，则返回rawType
	 * 
	 * @param currentType
	 * @return
	 */
	public static Class<?> _resolve_type_class(Type currentType) {
		if (currentType instanceof Class cls) {
			return cls;
		}
		// 如果参数还是泛型类，就直接getRawType()
		else if (currentType instanceof ParameterizedType parameterizedType) {
			Type rawType = parameterizedType.getRawType();
			if (rawType instanceof Class cls) {
				return cls;
			}
		}
		return null;
	}

	/**
	 * 获取最外层的第一个泛型参数
	 * 
	 * @param registryKeyField
	 * @return
	 */
	public static Class<?> first_generic_class(Field f) {
		return generic_classes(f)[0].type();
	}

	public static Class<?> first_generic_class(Type t) {
		return generic_classes(t)[0].type();
	}
}

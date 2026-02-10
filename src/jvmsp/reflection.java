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

import jvmsp.type.java_type;
import sun.reflect.ReflectionFactory;

/**
 * 反射工具，大部分功能可以直接使用Manipulator调用
 */
public abstract class reflection
{
	/**
	 * sun.reflect.ReflectionFactory相关方法，分配TRUSTED_LOOKUP的基本入口
	 */
	public static class reflection_factory
	{
		public static final ReflectionFactory instance_ReflectionFactory;

		static
		{
			instance_ReflectionFactory = ReflectionFactory.getReflectionFactory();
		}

		/**
		 * 使用反序列化时调用目标构造函数构造新实例，ReflectionFactory具有调用所有构造函数的权限，因此可以构建任何类的实例。
		 * 
		 * @param target
		 * @param target_constructor
		 * @param args
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public static final <_T> _T construct(Class<_T> target, Constructor<?> target_constructor, Object... args)
		{
			try
			{
				Constructor<?> ctor = (Constructor<?>) instance_ReflectionFactory.newConstructorForSerialization(target, target_constructor);
				return (_T) ctor.newInstance(args);// 通过该方法拿到的构造函数默认可以直接调用，如果再手动setAccessible(true)则会报错无权限，需要开放模块。
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex)
			{
				ex.printStackTrace();
			}
			return null;
		}

		/**
		 * 使用反序列化创建一个新对象，该对象未执行任何构造函数，仅分配了内存。
		 * 
		 * @param target
		 * @return
		 */
		public static final <T> T allocate(Class<T> target)
		{
			try
			{
				return construct(target, Object.class.getConstructor());
			}
			catch (IllegalArgumentException | SecurityException | NoSuchMethodException ex)
			{
				ex.printStackTrace();
			}
			return null;
		}
	}

	public static final StackWalker stack_walker;

	static
	{
		stack_walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);// 最常用，最先初始化
	}

	private static Class<?> jdk_internal_reflect_Reflection = null;

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

	static
	{
		java_lang_reflect_AccessibleObject_override = symbols.find_var(AccessibleObject.class, "override", boolean.class);
		try
		{
			jdk_internal_reflect_Reflection = Class.forName("jdk.internal.reflect.Reflection");
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		Reflection_fieldFilterMap = symbols.find_static_var(jdk_internal_reflect_Reflection, "fieldFilterMap", Map.class);
		Reflection_methodFilterMap = symbols.find_static_var(jdk_internal_reflect_Reflection, "methodFilterMap", Map.class);
	}

	/**
	 * 初始化一个类
	 * 
	 * @param clazz
	 * @throws ClassNotFoundException
	 */
	public static final void initialize_class(Class<?> clazz) throws ClassNotFoundException
	{
		Class.forName(clazz.getName(), true, clazz.getClassLoader());
	}

	/**
	 * 无视权限设置是否可访问。 注意：如果access_obj为null，JVM将直接崩溃。
	 * 
	 * @param <_AccessObj>
	 * @param accessible_obj
	 * @param accessible
	 * @return
	 */
	public static final <_AccessObj extends AccessibleObject> _AccessObj set_accessible(_AccessObj accessible_obj, boolean accessible)
	{
		java_lang_reflect_AccessibleObject_override.set(accessible_obj, accessible);
		return accessible_obj;
	}

	public static final <_AccessObj extends AccessibleObject> _AccessObj set_accessible(_AccessObj accessible_obj)
	{
		return set_accessible(accessible_obj, true);
	}

	/**
	 * 获取反射过滤的字段
	 * 
	 * @return
	 */
	public static final Map<Class<?>, Set<String>> get_field_filter()
	{
		return (Map<Class<?>, Set<String>>) Reflection_fieldFilterMap.get();
	}

	/**
	 * 获取反射过滤的方法
	 * 
	 * @return
	 */
	public static final Map<Class<?>, Set<String>> get_method_filter()
	{
		return (Map<Class<?>, Set<String>>) Reflection_methodFilterMap.get();
	}

	/**
	 * 设置字段反射过滤，Java设置了一些非常核心的类无法通过反射获取即设置反射过滤，此操作将会替换原有的过滤限制。危险操作。
	 */
	public static final void set_field_filter(Map<Class<?>, Set<String>> filter_map)
	{
		Reflection_fieldFilterMap.set(filter_map);
	}

	/**
	 * 设置方法反射过滤，Java设置了一些非常核心的类无法通过反射获取即设置反射过滤，此操作将会替换原有的过滤限制。危险操作。
	 */
	public static final void set_method_filter(Map<Class<?>, Set<String>> filter_map)
	{
		Reflection_methodFilterMap.set(filter_map);
	}

	/**
	 * 移除反射过滤，使得全部字段均可通过反射获取，Java设置了一些非常核心的类无法通过反射获取即设置反射过滤，此操作将会移除该限制。危险操作。
	 */
	public static final void remove_field_filter()
	{
		set_field_filter(new HashMap<Class<?>, Set<String>>());
	}

	/**
	 * 移除反射过滤，使得全部方法均可通过反射获取，Java设置了一些非常核心的类无法通过反射获取即设置反射过滤，此操作将会移除该限制。危险操作。
	 */
	public static final void remove_method_filter()
	{
		set_method_filter(new HashMap<Class<?>, Set<String>>());
	}

	/**
	 * 在没有反射字段过滤器的环境下操作
	 * 
	 * @param op
	 */
	public static final void no_field_filter(Runnable op)
	{
		Map<Class<?>, Set<String>> filter_map = get_field_filter();
		remove_field_filter();
		op.run();
		set_field_filter(filter_map);
	}

	/**
	 * 不经过反射过滤获取字段
	 * 
	 * @param clazz
	 * @param field_name
	 * @return
	 */
	public static final Field no_field_filter_find(Class<?> clazz, String field_name)
	{
		Field f = null;
		Map<Class<?>, Set<String>> filter_map = get_field_filter();
		remove_field_filter();
		f = find_field(clazz, field_name);
		set_field_filter(filter_map);
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

	static
	{
		try
		{
			Class_getDeclaredFields0 = symbols.find_special_method(Class.class, Class.class, "getDeclaredFields0", Field[].class, boolean.class);
			Class_privateGetDeclaredFields = symbols.find_special_method(Class.class, Class.class, "privateGetDeclaredFields", Field[].class, boolean.class);
			Class_getDeclaredMethods0 = symbols.find_special_method(Class.class, Class.class, "getDeclaredMethods0", Method[].class, boolean.class);
			Class_privateGetDeclaredMethods = symbols.find_special_method(Class.class, Class.class, "privateGetDeclaredMethods", Method[].class, boolean.class);
			Class_getDeclaredConstructors0 = symbols.find_special_method(Class.class, Class.class, "getDeclaredConstructors0", Constructor[].class, boolean.class);
			Class_searchFields = symbols.find_static_method(Class.class, "searchFields", Field.class, Field[].class, String.class);
			Class_searchMethods = symbols.find_static_method(Class.class, "searchMethods", Method.class, Method[].class, String.class, Class[].class);
			Class_getConstructor0 = symbols.find_special_method(Class.class, Class.class, "getConstructor0", Constructor.class, Class[].class, int.class);
			Class_forName0 = symbols.find_static_method(Class.class, "forName0", Class.class, String.class, boolean.class, ClassLoader.class, Class.class);
			Reflection_isCallerSensitive = symbols.find_static_method(jdk_internal_reflect_Reflection, "isCallerSensitive", boolean.class, Method.class);
		}
		catch (SecurityException | IllegalArgumentException ex)
		{
			ex.printStackTrace();
		}
	}

	public static final Field set_accessible(Class<?> clazz, String field_name, boolean accessible)
	{
		Field f = find_field(clazz, field_name);
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
	public static final Field[] __find_declared_fields(Class<?> clazz, boolean public_only)
	{
		try
		{
			return (Field[]) Class_getDeclaredFields0.invokeExact(clazz, public_only);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("find declared fields of '" + clazz + "' failed", ex);
		}
	}

	public static final Field[] __find_declared_fields(Class<?> clazz)
	{
		return __find_declared_fields(clazz, false);
	}

	public static final Field __find_declared_field(Class<?> clazz, String field_name)
	{
		try
		{
			return (Field) Class_searchFields.invokeExact(__find_declared_fields(clazz), field_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("find declared field '" + field_name + "' of '" + clazz + "' failed", ex);
		}
	}

	public static final Field[] find_declared_fields(Class<?> clazz, boolean public_only)
	{
		try
		{
			return (Field[]) Class_privateGetDeclaredFields.invokeExact(clazz, public_only);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("find declared fields of '" + clazz + "' failed", ex);
		}
	}

	public static final Field[] find_declared_fields(Class<?> clazz)
	{
		return find_declared_fields(clazz, false);
	}

	/**
	 * 查找本类声明的字段
	 * 
	 * @param clazz
	 * @param field_name
	 * @return
	 */
	public static final Field find_declared_field(Class<?> clazz, String field_name)
	{
		try
		{
			return (Field) Class_searchFields.invokeExact(find_declared_fields(clazz), field_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("find declared field '" + field_name + "' of '" + clazz + "' failed", ex);
		}
	}

	public static final Field find_declared_field(Object obj, String field_name)
	{
		return find_declared_field(obj.getClass(), field_name);
	}

	/**
	 * 获取对象定义的方法原root对象，无视反射过滤和访问权限，直接调用JVM内部的native方法获取全部方法
	 * 
	 * @param clazz 要获取的类
	 * @return 字段列表
	 */
	public static final Method[] __find_declared_methods(Class<?> clazz, boolean public_only)
	{
		try
		{
			return (Method[]) Class_getDeclaredMethods0.invokeExact(clazz, false);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("find declared methods of '" + clazz + "' failed", ex);
		}
	}

	public static final Method[] __find_declared_methods(Class<?> clazz)
	{
		return __find_declared_methods(clazz, false);
	}

	public static final Method __find_declared_method(Class<?> clazz, String method_name, Class<?>... arg_types)
	{
		try
		{
			return (Method) Class_searchMethods.invokeExact(__find_declared_methods(clazz), method_name, arg_types);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("find declared method '" + method_name + "' of '" + clazz + "' failed", ex);
		}
	}

	/**
	 * 获取Class对象缓存的root对象，除非类被重载否则不再改变。
	 * 
	 * @param clazz
	 * @param public_only
	 * @return
	 */
	public static final Method[] find_declared_methods(Class<?> clazz, boolean public_only)
	{
		try
		{
			return (Method[]) Class_privateGetDeclaredMethods.invokeExact(clazz, false);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("find declared methods of '" + clazz + "' failed", ex);
		}
	}

	public static final Method[] find_declared_methods(Class<?> clazz)
	{
		return find_declared_methods(clazz, false);
	}

	public static final Method find_declared_method(Class<?> clazz, String method_name, Class<?>... arg_types)
	{
		try
		{
			return (Method) Class_searchMethods.invokeExact(find_declared_methods(clazz), method_name, arg_types);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("find declared method '" + method_name + "' of '" + clazz + "' failed", ex);
		}
	}

	/**
	 * 目标方法是否是静态的。该方法主要用于MethodHandle查找。
	 * 
	 * @param clazz
	 * @param method_name
	 * @param arg_types
	 * @return
	 */
	public static final boolean is_static(Class<?> clazz, String method_name, Class<?>... arg_types)
	{
		Method m = find_declared_method(clazz, method_name, arg_types);
		return Modifier.isStatic(m.getModifiers());
	}

	/**
	 * 查找构造函数的原始对象
	 * 
	 * @param <_T>
	 * @param clazz
	 * @param public_only
	 * @return
	 */
	public static final <_T> Constructor<_T>[] __find_declared_constructors(Class<?> clazz, boolean public_only)
	{
		try
		{
			return (Constructor<_T>[]) Class_getDeclaredConstructors0.invokeExact(clazz, public_only);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("find declared constructors of '" + clazz + "' failed", ex);
		}
	}

	public static final <_T> Constructor<_T>[] __find_declared_constructors(Class<?> clazz)
	{
		return __find_declared_constructors(clazz, false);
	}

	/**
	 * 获取root构造函数
	 * 
	 * @param <_T>
	 * @param clazz
	 * @param which    java.lang.reflect.Member接口中的访问类型，Member.DECLARED为全部定义的构造函数，Member.PUBLIC为public的构造函数
	 * @param argTypes 构造函数的参数类型
	 * @return
	 */
	public static final <_T> Constructor<_T> __find_declared_constructor(Class<_T> clazz, int which, Class<?>... argTypes)
	{
		try
		{
			return (Constructor<_T>) Class_getConstructor0.invokeExact(clazz, argTypes, which);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("find declared constructor of '" + clazz + "' failed", ex);
		}
	}

	public static final <_T> Constructor<_T> __find_declared_constructor(Class<_T> clazz, Class<?>... argTypes)
	{
		return __find_declared_constructor(clazz, Member.DECLARED, argTypes);
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
	public static final Class<?> find_class(String name, boolean initialize, ClassLoader loader, Class<?> caller)
	{
		try
		{
			return (Class<?>) Class_forName0.invokeExact(name, initialize, loader, caller);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("find class '" + name + "' failed", ex);
		}
	}

	public static final Class<?> find_class(String name, boolean initialize, ClassLoader loader)
	{
		Class<?> caller = caller_class();
		return find_class(name, initialize, loader, caller);
	}

	public static final Class<?> find_class(String name, boolean initialize)
	{
		Class<?> caller = caller_class();
		return find_class(name, initialize, caller.getClassLoader(), caller);
	}

	public static final Class<?> find_class(String name)
	{
		Class<?> caller = caller_class();
		return find_class(name, true, caller.getClassLoader(), caller);
	}

	public static final Class<?> find_sys_class(String name, boolean initialize)
	{
		return find_class(name, initialize, null, Class.class);
	}

	public static final Class<?> find_sys_class(String name)
	{
		return find_sys_class(name, true);
	}

	/**
	 * 获取类名
	 * 
	 * @param full_name 带有包的完整类名
	 * @return
	 */
	public static final String class_name(String full_name)
	{
		return full_name.substring(full_name.lastIndexOf('.') + 1);
	}

	public static final String class_name(Object obj)
	{
		return class_name(obj.getClass().getName());
	}

	public static final String package_name(String full_name)
	{
		return full_name.substring(0, full_name.lastIndexOf('.'));
	}

	/**
	 * 查询类成员，如果该类没有则递归查找父类
	 */
	public static final Field find_field(Class<?> clazz, String name)
	{
		return _find_field(clazz, clazz, name);
	}

	public static final Field find_field(Object obj, String name)
	{
		return find_field(obj.getClass(), name);
	}

	private static Field _find_field(Class<?> start_clazz, Class<?> current_clazz, String name)
	{
		Field result = find_declared_field(current_clazz, name);
		if (result == null)
		{
			Class<?> super_clazz = current_clazz.getSuperclass();
			if (super_clazz == null)
			{
				throw new IllegalArgumentException("cannot find field " + name + " in " + start_clazz);
			}
			else
				return _find_field(start_clazz, super_clazz, name);
		}
		return result;
	}

	/**
	 * 读本类字段。使用反射无视权限访问成员，如果是静态成员则传入null，非静态成员则传入对象本身。<br>
	 * jdk.internal.reflect.Reflection会对反射获取的字段进行过滤，因此这些字段不能访问。如需访问使用VarHandle的方法进行
	 * 
	 * @param obj        非静态成员所属对象本身或静态成员对应的Class<?>
	 * @param field_name 要访问的字段
	 * @return 成员的值
	 */
	public static final Object read(Object obj, Field field)
	{
		try
		{
			return set_accessible(field, true).get(obj);
		}
		catch (IllegalAccessException ex)
		{
			throw new java.lang.InternalError("reading field '" + field + "' in '" + obj + "' faield", ex);
		}
	}

	public static final Object read(Object obj, String field)
	{
		return read(obj, find_declared_field(obj.getClass(), field));
	}

	public static final Object read(Class<?> clazz, String field)
	{
		return read(null, find_declared_field(clazz, field));
	}

	/**
	 * 访问值，若目标字段不存在则返回默认值
	 * 
	 * @param obj
	 * @param field
	 * @param default_value
	 * @return
	 */
	public static final Object read_or_default(Object obj, Field field, Object default_value)
	{
		try
		{
			return set_accessible(field, true).get(obj);
		}
		catch (Throwable ex)
		{
			return default_value;
		}
	}

	public static final Object read_or_default(Object obj, String field_name, Object default_value)
	{
		try
		{
			return set_accessible(find_declared_field(obj.getClass(), field_name), true).get(obj);
		}
		catch (Throwable ex)
		{
			return default_value;
		}
	}

	public static final Object read_or_default(Class<?> clazz, String field_name, Object default_value)
	{
		try
		{
			return set_accessible(find_declared_field(clazz, field_name), true).get(null);
		}
		catch (Throwable ex)
		{
			return default_value;
		}
	}

	/**
	 * 写本类字段，如果是静态字段则obj将忽略（可以传入null），非静态字段需要传入待修改的对象
	 * 
	 * @param obj
	 * @param field
	 * @param value
	 * @return
	 */
	public static final boolean write(Object obj, Field field, Object value)
	{
		try
		{
			set_accessible(field, true).set(obj, value);
			return true;
		}
		catch (IllegalAccessException ex)
		{
			throw new java.lang.InternalError("writing field '" + field + "' with value '" + value + "' in '" + obj + "' faield", ex);
		}
	}

	public static final boolean write(Object obj, String field, Object value)
	{
		return write(obj, find_declared_field(obj.getClass(), field), value);
	}

	public static final Object write(Class<?> clazz, String field, Object value)
	{
		return write(null, find_declared_field(clazz, field), value);
	}

	/**
	 * 使用反射无视权限调用方法，如果是静态方法则传入Class<?>，非静态方法则传入对象本身。jdk.internal.reflect.Reflection会对反射获取的方法进行过滤，因此这些方法不能访问。如需访问使用Handle的方法进行
	 * 
	 * @param obj
	 * @param method_name
	 * @param arg_types
	 * @param args
	 */
	public static final Object call(Object obj, String method_name, Class<?>[] arg_types, Object... args)
	{
		try
		{
			return set_accessible(find_method(obj, method_name, arg_types)).invoke(obj, args);
		}
		catch (IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException ex)
		{
			throw new java.lang.InternalError("call " + obj.toString() + "." + method_name + "() failed", ex);
		}
	}

	public static final Object call(Object obj, Method method, Object... args)
	{
		try
		{
			return set_accessible(method).invoke(obj, args);
		}
		catch (IllegalArgumentException | IllegalAccessException | SecurityException | InvocationTargetException ex)
		{
			throw new java.lang.InternalError("call " + obj.toString() + "." + method.getName() + "() failed", ex);
		}
	}

	public static final String method_description(String name, Class<?>... arg_types)
	{
		String method_description = name + '(';
		if (arg_types != null)
			for (int a = 0; a < arg_types.length; ++a)
			{
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
	public static final Method find_method_self(Class<?> clazz, String name, Class<?>... arg_types)
	{
		return find_declared_method(clazz, name, arg_types == null ? (new Class<?>[] {}) : arg_types);
	}

	/**
	 * 只搜寻该类及其父类、实现接口的方法
	 * 
	 * @param clazz
	 * @param name
	 * @param arg_types
	 * @return
	 */
	public static final Method find_method_inherited(Class<?> clazz, String name, Class<?>... arg_types)
	{
		Method result = find_declared_method(clazz, name, arg_types == null ? (new Class<?>[] {}) : arg_types);
		if (result == null)
		{
			Class<?> super_clazz = clazz.getSuperclass();
			Class<?>[] interfaces = clazz.getInterfaces();
			if (super_clazz == null && interfaces.length == 0)
			{
				throw new java.lang.InternalError("cannot find '" + clazz + "' method '" + name + "' in neither super class nor implemented interfaces");
			}
			else
			{
				Method method = find_method_self(super_clazz, name, arg_types);
				if (method != null)// 如果父类有方法则优先返回父类的方法
					return method;
				else
				{// 从接口中搜寻方法
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
	public static final Method find_method(Object obj, String name, Class<?>... arg_types)
	{
		Class<?> clazz;
		if (obj instanceof Class<?> c)
			clazz = c;
		else
			clazz = obj.getClass();
		Method method = null;
		ArrayList<ArrayList<Class<?>>> chain = resolve_inherit_implament_chain(clazz);
		FOUND: for (int depth = 0; depth < chain.size(); ++depth)
		{
			ArrayList<Class<?>> equal_depth_classes = chain.get(depth);
			for (int i = 0; i < equal_depth_classes.size(); ++i)
				if ((method = find_method_self(equal_depth_classes.get(i), name, arg_types)) != null)
					break FOUND;
		}
		if (method == null)
		{
			throw new java.lang.InternalError("method " + method_description(name, arg_types) + " not found in class " + clazz.getName() + " or its parents");
		}
		return method;
	}

	private static void _resolve_inherit_chain(Class<?> clazz, ArrayList<Class<?>> chain)
	{
		chain.add(clazz);
		Class<?> super_clazz = clazz.getSuperclass();
		if (super_clazz != null)
			_resolve_inherit_chain(super_clazz, chain);
	}

	public static final Class<?>[] resolve_inherit_chain(Class<?> clazz)
	{
		ArrayList<Class<?>> chain = new ArrayList<>();
		_resolve_inherit_chain(clazz, chain);
		return chain.toArray(new Class<?>[chain.size()]);
	}

	private static ArrayList<ArrayList<Class<?>>> _resolve_inherit_implament_chain(Class<?> self, int current_depth, ArrayList<ArrayList<Class<?>>> chain)
	{
		ArrayList<Class<?>> current_depth_classes = null;
		while (current_depth_classes == null)
			try
			{
				current_depth_classes = chain.get(current_depth);
			}
			catch (IndexOutOfBoundsException ex)
			{
				chain.add(new ArrayList<>());
			}
		current_depth_classes.add(self);
		Class<?> super_clazz = self.getSuperclass();
		if (super_clazz != null)
			_resolve_inherit_implament_chain(super_clazz, current_depth + 1, chain);
		Class<?>[] interfaces = self.getInterfaces();
		for (Class<?> i : interfaces)
			_resolve_inherit_implament_chain(i, current_depth + 1, chain);
		return chain;
	}

	public static final ArrayList<ArrayList<Class<?>>> resolve_inherit_implament_chain(Class<?> clazz)
	{
		ArrayList<ArrayList<Class<?>>> chain = new ArrayList<>();
		return _resolve_inherit_implament_chain(clazz, 0, chain);
	}

	/**
	 * 推断每个参数的类型，每个参数的类型均是一个数组，为该类型的继承链
	 * 
	 * @param args 要推断的参数列表
	 * @return
	 */
	public static final Class<?>[][] resolve_arg_types_chain(Object... args)
	{
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
	public static final Class<?>[] resolve_arg_types(Object... args)
	{
		Class<?>[] arg_types = new Class<?>[args.length];
		for (int idx = 0; idx < args.length; ++idx)
			arg_types[idx] = args[idx].getClass();
		return arg_types;
	}

	public static final Object invoke(Object obj, String method_name, Class<?>[] arg_types, Object... args)
	{
		Method method = find_method(obj, method_name, arg_types);
		try
		{
			set_accessible(method, true);
			return method.invoke(obj, args);
		}
		catch (IllegalAccessException | InvocationTargetException ex)
		{
			throw new java.lang.InternalError("call " + method_name + "() with arguments '" + args + "' in object '" + obj.toString() + "' failed", ex);
		}
	}

	public static final Constructor<?> find_constructor(Object obj, Class<?>... arg_types)
	{
		Class<?> clazz;
		if (obj instanceof Class<?> c)
			clazz = c;
		else
			clazz = obj.getClass();
		Constructor<?> result = __find_declared_constructor(clazz, arg_types == null ? (new Class<?>[] {}) : arg_types);
		if (result == null)
		{
			Class<?> super_clazz = clazz.getSuperclass();
			return super_clazz == null ? null : find_constructor(super_clazz, arg_types);
		}
		return result;
	}

	/**
	 * 利用反射调用构造函数
	 * 
	 * @param obj  目标类型的对象实例或Class<_T>
	 * @param args
	 * @return
	 */
	public static final Object construct(Object obj, Class<?>[] arg_types, Object... args)
	{
		Constructor<?> constructor = find_constructor(obj, arg_types);
		try
		{
			set_accessible(constructor, true);
			return constructor.newInstance(args);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
		{
			throw new java.lang.InternalError("construct '" + obj.toString() + "' with arguments '" + args + "' faield", ex);
		}
	}

	/**
	 * 判断某个类是否具有指定超类，支持向上递归查找超类
	 * 
	 * @param clazz       要判断是否有超类的类
	 * @param super_class 超类
	 * @return clazz具有超类super_class则返回true，否则返回false
	 */
	public static final boolean has_super(Class<?> clazz, Class<?> super_class)
	{
		Class<?> super_clazz = clazz.getSuperclass();
		if (super_clazz == super_class)
			return true;
		return super_clazz == null ? false : has_super(super_clazz, super_class);
	}

	/**
	 * f所声明的类型是否是type或者其子类
	 * 
	 * @param f
	 * @param type
	 * @return
	 */
	public static final boolean is(Field f, Class<?> type)
	{
		return type.isAssignableFrom(f.getType());
	}

	/**
	 * 判断一个类是否是另一个类的子类
	 * 
	 * @param son
	 * @param parent
	 * @return
	 */
	public static final boolean is(Class<?> son, Class<?> parent)
	{
		return parent.isAssignableFrom(son);
	}

	/**
	 * jdk.internal.reflect.Reflection.isCallerSensitive()
	 * 
	 * @param m
	 * @return
	 */
	public static final boolean is_caller_sensitive(Method m)
	{
		try
		{
			return (boolean) Reflection_isCallerSensitive.invokeExact(m);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("method '" + m.toString() + "' check is caller sensitive faield", ex);
		}
	}

	/**
	 * 内部类相关
	 */

	/**
	 * 获取指定类型的外部类引用
	 * 
	 * @param <_T>
	 * @param target 要获取的外部类类型
	 * @param obj    要获取外部类引用的对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <_T> _T enclosing_class_instance(Class<_T> target, Object obj)
	{
		if (target == null || obj == null)
		{
			return null;
		}
		Class<?> clazz = obj.getClass();
		Class<?> outerCls = clazz.getEnclosingClass();
		if (outerCls == null)
		{
			return null;// obj没有外部类
		}
		_T outerObj = null;
		Field[] fields = find_declared_fields(clazz);
		for (Field field : fields)
		{
			if (field.getType() == outerCls && field.getName().startsWith("this$"))
			{// 找到上一层外部类的实例引用字段
				outerObj = (_T) read(obj, field);
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
	public static final Object enclosing_class_instance(Object obj)
	{
		return obj == null ? null : enclosing_class_instance(obj.getClass().getEnclosingClass(), obj);
	}

	/**
	 * 包相关
	 */
	public static final List<String> class_names_in_package(Class<?> any_class_in_package, file_system.uri.resolver resolver, String package_name, boolean include_subpackage)
	{
		String path = file_system.classpath(any_class_in_package, resolver);
		if (path.endsWith(file_system.JAR_EXTENSION_NAME))
			return file_system.class_names_in_jar(any_class_in_package, resolver, package_name, include_subpackage);
		else
			return file_system.class_names_local(any_class_in_package, resolver, package_name, include_subpackage);
	}

	public static final List<String> class_names_in_package(Class<?> any_class_in_package, String package_name, boolean include_subpackage)
	{
		return class_names_in_package(any_class_in_package, file_system.uri.resolver.DEFAULT, package_name, include_subpackage);
	}

	public static final List<String> class_names_in_package(String package_name, boolean include_subpackage)
	{
		Class<?> caller = caller_class();
		return class_names_in_package(caller, package_name, include_subpackage);// 获取调用该方法的类
	}

	public static final List<String> class_names_in_package(Class<?> any_class_in_package, String package_name)
	{
		return class_names_in_package(any_class_in_package, package_name, false);
	}

	public static final List<String> class_names_in_package(String package_name)
	{
		Class<?> caller = caller_class();
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

	static
	{
		Class<?> class_AnnotationData = find_class("java.lang.Class$AnnotationData");
		Class_annotationData = symbols.find_special_method(Class.class, Class.class, "annotationData", class_AnnotationData);
		AnnotationData_annotations = symbols.find_var(class_AnnotationData, "annotations", Map.class);
		AnnotationData_declaredAnnotations = symbols.find_var(class_AnnotationData, "declaredAnnotations", Map.class);
		Field_declaredAnnotations = symbols.find_special_method(Field.class, "declaredAnnotations", Map.class);
		Executable_declaredAnnotations = symbols.find_special_method(Executable.class, Executable.class, "declaredAnnotations", Map.class);
	}

	/**
	 * 获取类缓存的注解数据，Class.getAnnotation()获取的注解都是此处缓存的注解数据
	 * 
	 * @param clazz
	 * @return
	 */
	public static final Map<Class<?>, ?> cached_annotations(Class<?> clazz)
	{
		return (Map<Class<?>, ?>) AnnotationData_annotations.get(clazz);
	}

	/**
	 * 获取声明注解Map，对于Class而言，使用的是缓存注解而非该声明注解。
	 * 
	 * @param e
	 * @return
	 */
	public static final Map<Class<?>, ?> declared_annotations(AnnotatedElement ae)
	{
		try
		{
			if (ae instanceof Class clazz)
				return (Map<Class<?>, ?>) AnnotationData_declaredAnnotations.get(Class_annotationData.invokeExact(clazz));
			else if (ae instanceof Field f)
				return (Map<Class<?>, ?>) Field_declaredAnnotations.invokeExact(f);
			else if (ae instanceof Executable e)
				return (Map<Class<?>, ?>) Executable_declaredAnnotations.invokeExact(e);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get raw declared annotations of '" + ae.toString() + "' faield", ex);
		}
		return null;
	}

	/**
	 * 运行时实际被使用的注解数据
	 * 
	 * @param ae
	 * @return
	 */
	public static final Map<Class<?>, ?> runtime_annotations(AnnotatedElement ae)
	{
		if (ae instanceof Class clazz)
			return cached_annotations(clazz);
		else
			return declared_annotations(ae);
	}

	/**
	 * 获取被注解的元素所在的类
	 * 
	 * @param ae
	 * @return
	 */
	public static final Class<?> declaring_class(AnnotatedElement ae)
	{
		if (ae instanceof Class clazz)
			return clazz;
		else if (ae instanceof Field f)
			return f.getDeclaringClass();
		else if (ae instanceof Executable e)
			return e.getDeclaringClass();
		return null;
	}

	@FunctionalInterface
	public static interface annotation_replace_operation
	{
		/**
		 * 在转换之前需要进行的操作
		 * 
		 * @param ae
		 */
		public void operate(AnnotatedElement ae);

		public static annotation_replace_operation NONE = (ae) ->
		{
		};
	}

	/**
	 * 替换目标元素的注解 如果目标注解的类型和实际获取的对象annotationType()类型不一致，那么需要手动传入目标注解类型。<br>
	 * 
	 * @param ae
	 * @param target_annotation_clazz 将要被替换的目标注解类型
	 * @param new_annotation_clazz    要替换的新注解类型，该类型可以不是Annotation而是普通类型
	 * @param new_annotation
	 * @param op                      替换完成后执行的操作
	 */
	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public static final void replace(AnnotatedElement ae, Class<?> target_annotation_clazz, Class<?> new_annotation_clazz, Object new_annotation, annotation_replace_operation op)
	{
		// 判断一个AnnotatedElement是否有某个注解，实际是判断缓存的注解map是否存在指定注解Class<?>的key
		Map anno_map = runtime_annotations(ae);
		anno_map.remove(target_annotation_clazz);// 移除镜像注解的key
		anno_map.put(new_annotation_clazz, new_annotation);// 填入目标注解
		op.operate(ae);
	}

	public static final void replace(AnnotatedElement ae, Class<?> target_annotation_clazz, Annotation new_annotation, annotation_replace_operation op)
	{
		replace(ae, target_annotation_clazz, new_annotation.annotationType(), new_annotation, op);
	}

	/**
	 * 强制替换注解，将任何类型的new_annotation都修改klass word强制cast到new_annotation_clazz类型
	 * 
	 * @param ae
	 * @param target_annotation_clazz
	 * @param new_annotation_clazz
	 * @param new_annotation
	 * @param op
	 */
	public static final void force_replace(AnnotatedElement ae, Class<?> target_annotation_clazz, Class<?> new_annotation_clazz, Object new_annotation, annotation_replace_operation op)
	{
		replace(ae, target_annotation_clazz, new_annotation_clazz, java_type.cast(new_annotation, new_annotation_clazz), op);
	}

	/**
	 * 将类加载器loader所加载的类的所有目标注解替换成新注解
	 * 
	 * @param loader
	 * @param target_annotation_clazz
	 * @param new_annotation_clazz
	 * @param new_annotation
	 * @param is_system               目标注解是否是系统级注解，如果是，那么必须将包含注解的类设置为系统类
	 * @param op
	 */
	public static final void force_replace(ClassLoader loader, Class<?> target_annotation_clazz, Class<?> new_annotation_clazz, Object new_annotation, boolean is_system, annotation_replace_operation op)
	{
		ArrayList<AnnotatedElement> annotated = class_operation.scan_annotated_elements(loader, target_annotation_clazz);
		for (AnnotatedElement ae : annotated)
		{
			// 如果是系统注解，那么包含该注解的类也必须是BootstrapLoader加载的类
			if (is_system)
			{
				Class<?> decl_class = declaring_class(ae);
				class_loader.as_bootstrap(decl_class);
			}
			force_replace(ae, target_annotation_clazz, new_annotation_clazz, new_annotation, op);
		}
	}

	/**
	 * 泛型相关
	 */
	public static enum entry_type
	{
		INTERFACE, CLASS, RAW_TYPE, UPPER_BOUNDS, LOWER_BOUNDS
	}

	/**
	 * 单个类或上界类数组、下界类数组
	 */
	public static final class generic_entry
	{
		public final entry_type type;
		private Class<?>[] result;

		generic_entry(entry_type type, Class<?>... result)
		{
			this.type = type;
			this.result = result;
		}

		public Class<?> single_type()
		{
			if (type == entry_type.INTERFACE | type == entry_type.CLASS || type == entry_type.RAW_TYPE)
				return result[0];
			else
				return null;
		}

		public Class<?> type()
		{
			return result[0];
		}

		public Class<?> type(int idx)
		{
			return result[idx];
		}

		public Class<?>[] upper_bounds()
		{
			if (type == entry_type.UPPER_BOUNDS)
				return result;
			else
				return null;
		}

		public Class<?>[] lower_bounds()
		{
			if (type == entry_type.LOWER_BOUNDS)
				return result;
			else
				return null;
		}

		/**
		 * 判断是否result中存在任意一个Class<?>严格地是clazz类
		 * 
		 * @param clazz
		 * @return
		 */
		public boolean equals_any(Class<?> clazz)
		{
			for (Class<?> c : result)
				if (clazz == c)
					return true;
			return false;
		}

		/**
		 * 判断是否result中存在任意一个Class<?>是clazz或其子类
		 * 
		 * @param clazz
		 * @return
		 */
		public boolean is_any(Class<?> clazz)
		{
			for (Class<?> c : result)
				if (is(c, clazz))
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
	public static final boolean is(Field f, int[] indices, Class<?>... types)
	{
		generic_entry[] classes = generic_classes(f, indices);
		if (classes.length != types.length)
			return false;
		for (int idx = 0; idx < types.length; ++idx)
		{
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
	public static final boolean is(Field f, Class<?>... types)
	{
		return is(f, new int[] {}, types);
	}

	/**
	 * 匹配前N个泛型参数是否和types一致
	 * 
	 * @param f
	 * @param types
	 * @return
	 */
	public static final boolean generic_start_with(Field f, int[] indices, Class<?>... types)
	{
		generic_entry[] classes = generic_classes(f, indices);
		for (int idx = 0; idx < types.length; ++idx)
		{
			if (!classes[idx].is_any(types[idx]))
				return false;
		}
		return true;
	}

	public static final boolean generic_start_with(Field f, Class<?>... types)
	{
		return generic_start_with(f, new int[] {}, types);
	}

	public static final generic_entry[] generic_classes(Field f, int... indices)
	{
		return generic_classes(f.getGenericType(), indices);
	}

	/**
	 * 获取指定嵌套深度索引的泛型参数
	 * 
	 * @param current_type
	 * @param indices
	 * @return
	 */
	public static final Type generic_type(Type current_type, int... indices)
	{
		Type[] actual_type_arguments = null;
		for (int nest_depth = 0; nest_depth < indices.length; ++nest_depth)
		{
			// 没有泛型参数则直接返回
			if (current_type instanceof ParameterizedType current_parameterized_type)
			{
				int nest_idx = indices[nest_depth];
				actual_type_arguments = current_parameterized_type.getActualTypeArguments();
				// 索引超出该深度的泛型参数个数
				if (nest_idx < 0 || nest_idx >= actual_type_arguments.length)
				{
					return null;
				}
				// 除非是最后一层，否则继续向下查找
				current_type = actual_type_arguments[nest_idx];
			}
			else
				return null;
		}
		return current_type;
	}

	/**
	 * 获取指定字段的指定嵌套深度的泛型参数的Class<?>
	 * 
	 * @param current_type 当前的类型
	 * @param indices      从最外层开始，向内的索引
	 * @return
	 */
	public static final generic_entry[] generic_classes(Type current_type, int... indices)
	{
		Type[] actual_type_arguments = null;
		current_type = generic_type(current_type, indices);
		// 获取最终深度的特定索引的全部泛型参数
		if (current_type instanceof ParameterizedType pt)
			actual_type_arguments = pt.getActualTypeArguments();
		else
			actual_type_arguments = new Type[]
			{ current_type };
		generic_entry[] entries = new generic_entry[actual_type_arguments.length];
		for (int idx = 0; idx < actual_type_arguments.length; ++idx)
		{
			current_type = actual_type_arguments[idx];
			if (current_type instanceof Class clazz)
			{
				entries[idx] = new generic_entry(entry_type.CLASS, clazz);
				continue;
			}
			// 如果参数还是泛型类，就直接getRawType()
			else if (current_type instanceof ParameterizedType parameterized_type)
			{
				Type rawType = parameterized_type.getRawType();
				if (rawType instanceof Class clazz)
				{
					entries[idx] = new generic_entry(entry_type.RAW_TYPE, clazz);
					continue;
				}
			}
			else if (current_type instanceof WildcardType wildcard_type)
			{
				Type[] upper_bounds = wildcard_type.getUpperBounds();
				Type[] lower_bounds = wildcard_type.getLowerBounds();
				if (upper_bounds.length != 0)
				{
					Class<?>[] upper_bounds_clazzarr = new Class[upper_bounds.length];
					for (int i = 0; i < upper_bounds_clazzarr.length; ++i)
					{
						upper_bounds_clazzarr[idx] = _resolve_type_class(upper_bounds[i]);
					}
					entries[idx] = new generic_entry(entry_type.UPPER_BOUNDS, upper_bounds_clazzarr);
				}
				else if (lower_bounds.length != 0)
				{
					Class<?>[] lower_bounds_clazzarr = new Class[lower_bounds.length];
					for (int i = 0; i < lower_bounds_clazzarr.length; ++i)
					{
						lower_bounds_clazzarr[idx] = _resolve_type_class(lower_bounds[i]);
					}
					entries[idx] = new generic_entry(entry_type.LOWER_BOUNDS, lower_bounds_clazzarr);
				}
				continue;
			}
			else
				entries[idx] = null;
		}
		return entries;
	}

	/**
	 * 如果current_type是Class<?>则直接返回class，如果是带泛型参数的class，则返回rawType
	 * 
	 * @param current_type
	 * @return
	 */
	public static final Class<?> _resolve_type_class(Type current_type)
	{
		if (current_type instanceof Class clazz)
		{
			return clazz;
		}
		// 如果参数还是泛型类，就直接getRawType()
		else if (current_type instanceof ParameterizedType parameterized_type)
		{
			Type rawType = parameterized_type.getRawType();
			if (rawType instanceof Class clazz)
			{
				return clazz;
			}
		}
		return null;
	}

	/**
	 * 获取最外层的第一个泛型参数
	 * 
	 * @param f
	 * @return
	 */
	public static final Class<?> first_generic_class(Field f)
	{
		return generic_classes(f)[0].type();
	}

	public static final Class<?> first_generic_class(Type t)
	{
		return generic_classes(t)[0].type();
	}

	/**
	 * 栈帧回溯
	 */

	/**
	 * 栈追踪时执行的操作
	 */
	@FunctionalInterface
	public interface unwind_operation
	{
		public void operate(StackWalker.StackFrame stack_frame);
	}

	/**
	 * 栈追踪设置
	 */
	public enum unwind_option
	{
		SKIP_COUNT_BY_FRAME, SKIP_COUNT_BY_CLASS
	}

	/**
	 * 栈追踪<br>
	 * 本方法对应的栈帧始终是0，<br>
	 * skip 1 会返回直接调用本方法unwind(int skip_frame_count)的栈帧，即调用者方法本身栈帧<br>
	 * skip 2 会返回调用该调用者的方法栈帧
	 * 
	 * @param skip_frame_count
	 * @return
	 */
	public static final StackWalker.StackFrame unwind(int skip_frame_count)
	{
		return stack_walker.walk(stack -> stack.skip(skip_frame_count).findFirst().get());
	}

	public static final void unwind(int skip_frame_count, unwind_operation op)
	{
		op.operate(stack_walker.walk(stack -> stack.skip(skip_frame_count).findFirst().get()));
	}

	public static final Class<?> unwind_class(int skip_frame_count)
	{
		return stack_walker.walk(stack -> stack.skip(skip_frame_count).findFirst().get().getDeclaringClass());
	}

	/**
	 * 追踪函数调用栈帧，并获取调用的类<br>
	 * StackTrackOption为SKIP_COUNT_BY_FRAME时，行为同unwind_class(int skip_frame_count)一致<br>
	 * StackTrackOption为SKIP_COUNT_BY_CLASS时，追踪函数调用栈帧，并且返回第skip_class_count个不同的类<br>
	 * skip_class_count只表明跳过几个不同的类，对于连续同一个类调用栈帧将直接全部跳过
	 * 
	 * @param skip_count
	 * @param option
	 * @return
	 * @since Java 9
	 */
	public static final Class<?> unwind_class(int skip_count, unwind_option option)
	{
		switch (option)
		{
		case SKIP_COUNT_BY_FRAME:
			return unwind_class(skip_count);
		case SKIP_COUNT_BY_CLASS:
		{
			int skipped_class_count = 0;
			int skip_frame = 1;// 以JavaLang作为起点
			Class<?> caller_record = unwind_class(skip_frame);
			Class<?> stack_frame_class = null;
			if (skip_count > 0)
			{
				for (;;)
				{
					stack_frame_class = unwind_class(++skip_frame);
					if (caller_record == stack_frame_class)
						continue;
					else
					{
						caller_record = stack_frame_class;// 将下一个与当前caller_record不同的类记录作为追踪结果
						if (++skipped_class_count >= skip_count)
							break;
					}
				}
			}
			return caller_record;
		}
		}
		return null;
	}

	/**
	 * 获取直接调用该方法的类<br>
	 * 例如A()调用B()，B()调用context_class()，那么返回B()栈帧
	 * 
	 * @return
	 * @since Java 9
	 * @CallerSensitive
	 */
	public static final Class<?> context_class()
	{
		return unwind_class(2);
	}

	/**
	 * 获取一次间接调用该方法的类<br>
	 * 例如A()调用B()，B()调用caller_class()，那么返回A()栈帧
	 * 
	 * @return
	 * @since Java 9
	 * @CallerSensitive
	 */
	public static final Class<?> caller_class()
	{
		return unwind_class(3);
	}

	public static final Class<?> caller_class_as_param()
	{
		return unwind_class(4);
	}
}

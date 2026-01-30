package jvmsp;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 核心的类成员修改、访问和方法调用类，支持修改final、record成员变量。<br>
 * 该类的方法破坏了Java的安全性，请谨慎使用。
 */
public abstract class ObjectManipulator {
	/**
	 * 本类最核心的方法，移除AccessibleObject的访问安全检查限制，使得对象可以被访问。<br>
	 * 注意：如果access_obj为null，JVM将直接崩溃。
	 * 
	 * @param <AO>
	 * @param access_obj 要移除访问安全检查的对象
	 * @return
	 */
	public static <AO extends AccessibleObject> AO removeAccessCheck(AO access_obj) {
		return ReflectionBase.setAccessible(access_obj, true);
	}

	/**
	 * 恢复AccessibleObject的访问安全检查限制，使得对象访问遵循Java规则
	 * 
	 * @param <AO>
	 * @param access_obj 要恢复访问安全检查的对象
	 * @return
	 */
	public static <AO extends AccessibleObject> AO recoveryAccessCheck(AO access_obj) {
		return ReflectionBase.setAccessible(access_obj, false);
	}

	/**
	 * 使用反射无视权限访问成员，如果是静态成员则传入Class<?>，非静态成员则传入对象本身，jdk.internal.reflect.Reflection会对反射获取的字段进行过滤，因此这些字段不能访问。如需访问使用Handle的方法进行
	 * 
	 * @param obj        非静态成员所属对象本身或静态成员对应的Class<?>
	 * @param field_name 要访问的字段
	 * @return 成员的值
	 */
	public static Object access(Object obj, String field_name) {
		try {
			Field field = ObjectManipulator.removeAccessCheck(reflection.find_field(obj, field_name));
			return field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException ex) {
			System.err.println("access failed. obj=" + obj.toString() + ", field_name=" + field_name);
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 访问值，若目标字段不存在则返回默认值
	 * 
	 * @param obj
	 * @param field_name
	 * @param defaultValue
	 * @return
	 */
	public static Object accessOrDefault(Object obj, String field_name, Object defaultValue) {
		try {
			Field field = ObjectManipulator.removeAccessCheck(reflection.find_field(obj, field_name));
			return field.get(obj);
		} catch (Throwable ex) {
			return defaultValue;
		}
	}

	public static Object access(Object obj, Field field) {
		try {
			return ObjectManipulator.removeAccessCheck(field).get(obj);
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			System.err.println("access failed. obj=" + obj.toString() + ", field_name=" + field.getName());
			ex.printStackTrace();
		}
		return null;
	}

	public static Object accessOrDefault(Object obj, Field field, Object defaultValue) {
		try {
			return ObjectManipulator.removeAccessCheck(field).get(obj);
		} catch (Throwable ex) {
			return defaultValue;
		}
	}

	/**
	 * 使用反射无视权限调用方法，如果是静态方法则传入Class<?>，非静态方法则传入对象本身。jdk.internal.reflect.Reflection会对反射获取的方法进行过滤，因此这些方法不能访问。如需访问使用Handle的方法进行
	 * 
	 * @param obj
	 * @param method_name
	 * @param arg_types
	 * @param args
	 */
	public static Object invoke(Object obj, String method_name, Class<?>[] arg_types, Object... args) {
		try {
			Method method = ObjectManipulator.removeAccessCheck(reflection.find_method(obj, method_name, arg_types));
			return method.invoke(obj, args);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException ex) {
			System.err.println("invoke failed. obj=" + obj.toString() + ", method_name=" + method_name);
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			System.err.println("invoke method throws exception. obj=" + obj.toString() + ", method_name=" + method_name);
			ex.getCause().printStackTrace();
		}
		return null;
	}

	public static Object invoke(Object obj, Method method, Object... args) {
		try {
			return ObjectManipulator.removeAccessCheck(method).invoke(obj, args);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException ex) {
			System.err.println("invoke failed. obj=" + obj.toString() + ", method_name=" + method.getName());
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			System.err.println("invoke method throws exception. obj=" + obj.toString() + ", method_name=" + method.getName());
			ex.getCause().printStackTrace();
		}
		return null;
	}

	/**
	 * 无视访问权限和修饰符修改Object值，如果是静态成员忽略obj参数.此方法对于HiddenClass和record同样有效
	 * 
	 * @param obj   要修改值的对象
	 * @param field 要修改的Field
	 * @param value 要修改的值
	 */
	public static void setObject(Object obj, Field field, Object value) {
		unsafe.write(obj, field, value);
	}

	public static void setObject(Object obj, String field, Object value) {
		unsafe.write(obj, field, value);
	}

	public static void setDeclaredMemberObject(Object obj, String field, Object value) {
		unsafe.write_member(obj, field, value);
	}

	public static void setStaticObject(Class<?> cls, String field, Object value) {
		unsafe.write_static(cls, field, value);
	}

	public static Object getObject(Object obj, Field field) {
		return unsafe.read_reference(obj, field);
	}

	public static Object getObject(Object obj, String field) {
		return unsafe.read_reference(obj, field);
	}

	public static Object getDeclaredMemberObject(Object obj, String field) {
		return unsafe.read_member_reference(obj, field);
	}

	public static Object getStaticObject(Class<?> cls, String field) {
		return unsafe.read_static_reference(cls, field);
	}

	public static void setLong(Object obj, Field field, long value) {
		unsafe.write(obj, field, value);
	}

	public static void setLong(Object obj, String field, long value) {
		unsafe.write(obj, field, value);
	}

	public static Object getLong(Object obj, Field field) {
		return unsafe.read_long(obj, field);
	}

	public static Object getLong(Object obj, String field) {
		return unsafe.read_long(obj, field);
	}

	public static long getDeclaredMemberLong(Object obj, String field) {
		return unsafe.read_member_long(obj, field);
	}

	public static void setDeclaredMemberLong(Object obj, String field, long value) {
		unsafe.write_member_long(obj, field, value);
	}

	public static void setBoolean(Object obj, Field field, boolean value) {
		unsafe.write(obj, field, value);
	}

	public static void setBoolean(Object obj, String field, boolean value) {
		unsafe.write(obj, field, value);
	}

	public static void setDeclaredMemberBoolean(Object obj, String field, boolean value) {
		unsafe.write_member(obj, field, value);
	}

	public static void setStaticBoolean(Class<?> cls, String field, boolean value) {
		unsafe.write_static(cls, field, value);
	}

	public static boolean getDeclaredMemberBoolean(Object obj, String field) {
		return unsafe.read_member_bool(obj, field);
	}

	public static boolean getStaticBoolean(Class<?> cls, String field) {
		return unsafe.read_static_bool(cls, field);
	}

	public static void setInt(Object obj, Field field, int value) {
		unsafe.write(obj, field, value);
	}

	public static void setInt(Object obj, String field, int value) {
		unsafe.write(obj, field, value);
	}

	public static int getDeclaredMemberInt(Object obj, String field) {
		return unsafe.read_member_int(obj, field);
	}

	public static void setDeclaredMemberInt(Object obj, String field, int value) {
		unsafe.write_member(obj, field, value);
	}

	public static void setStaticInt(Class<?> cls, String field, int value) {
		unsafe.write_static(cls, field, value);
	}

	public static Object getInt(Object obj, Field field) {
		return unsafe.read_int(obj, field);
	}

	public static Object getInt(Object obj, String field) {
		return unsafe.read_int(obj, field);
	}

	public static void setDouble(Object obj, Field field, double value) {
		unsafe.write(obj, field, value);
	}

	public static void setDouble(Object obj, String field, double value) {
		unsafe.write(obj, field, value);
	}

	public static void setFloat(Object obj, Field field, float value) {
		unsafe.write(obj, field, value);
	}

	public static void setFloat(Object obj, String field, float value) {
		unsafe.write(obj, field, value);
	}

	public static final Object cast(Object obj, long castTypeKlassWord) {
		markWord.set_klass_word(obj, castTypeKlassWord);
		return obj;
	}

	public static final Object cast(Object obj, Object castTypeObj) {
		return cast(obj, markWord.get_klass_word(castTypeObj));
	}

	public static final Object cast(Object obj, Class<?> castType) {
		return cast(obj, markWord.get_klass_word(castType));
	}

	public static final Object cast(Object obj, String castType) {
		return cast(obj, markWord.get_klass_word(castType));
	}

	@SuppressWarnings("unchecked")
	public static final <T> T safeCast(Object obj, T castTypeObj) {
		return safeCast(obj, (Class<T>) castTypeObj.getClass());
	}

	/**
	 * 安全的强制转换，没有继承关系的独立类的转换会抛出Exception。<br>
	 * 主要用于Mixin。<br>
	 * 
	 * @param obj
	 * @param castType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T safeCast(Object obj, Class<T> castType) {
		return (T) (Object) obj;
	}

}

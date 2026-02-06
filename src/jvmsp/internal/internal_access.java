package jvmsp.internal;

import java.lang.invoke.MethodHandle;

import jvmsp.reflection;
import jvmsp.symbols;

/**
 * 修改反射安全限制等
 */
public class internal_access
{

	private static Class<?> class_jdk_internal_access_SharedSecrets;// jdk.internal.access.SharedSecrets
	private static Object instance_JavaLangAccess;// jdk.internal.access.JavaLangAccess;
	private static MethodHandle getConstantPool;// 获取指定类的类常量池The ConstantPool，其中包含静态成员、方法列表等

	static
	{
		try
		{
			class_jdk_internal_access_SharedSecrets = Class.forName("jdk.internal.access.SharedSecrets");
			Class<?> class_ConstantPool = Class.forName("jdk.internal.reflect.ConstantPool");
			instance_JavaLangAccess = access("JavaLangAccess");
			getConstantPool = symbols.find_special_method(instance_JavaLangAccess.getClass(), "getConstantPool", class_ConstantPool, Class.class);
		}
		catch (ClassNotFoundException | SecurityException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 获取jdk.internal.access.SharedSecrets中的访问对象。<br>
	 * 无法确定返回类型，因此只能使用反射，不能使用MethodHandle。<br>
	 * 
	 * @param access_name 访问对象的类名，不包含包名
	 * @return 访问对象
	 */
	public static final Object access(String access_name)
	{
		return reflection.call(class_jdk_internal_access_SharedSecrets, "get" + access_name, null);
	}

	/**
	 * 获取指定类的常量池
	 * 
	 * @param clazz
	 * @return
	 */
	public static final Object constant_pool(Class<?> clazz)
	{
		try
		{
			return getConstantPool.invokeExact(instance_JavaLangAccess, clazz);
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

}

package jvmsp.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import sun.reflect.ReflectionFactory;

public class reflection_factory {
	public static final ReflectionFactory instance_ReflectionFactory;

	static {
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
	public static final <_T> _T construct(Class<_T> target, Constructor<?> target_constructor, Object... args) {
		try {
			Constructor<?> ctor = (Constructor<?>) instance_ReflectionFactory.newConstructorForSerialization(target, target_constructor);
			return (_T) ctor.newInstance(args);// 通过该方法拿到的构造函数默认可以直接调用，如果再手动setAccessible(true)则会报错无权限，需要开放模块。
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException ex) {
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
	public static final <T> T allocate(Class<T> target) {
		try {
			return construct(target, Object.class.getConstructor());
		} catch (IllegalArgumentException | SecurityException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
		return null;
	}
}

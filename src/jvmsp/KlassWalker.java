package jvmsp;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 遍历类的工具，无视访问修饰符和反射过滤<br>
 * 提供原始Field、Method、Constructor等，对其进行修改会导致反射获取到的所有副本都被修改
 */
public class KlassWalker {
	@FunctionalInterface
	public static interface FieldOperation<F> {
		/**
		 * 遍历每个字段，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param f
		 * @param isStatic 目标字段是否是静态的
		 * @param value    字段值，无效则为null
		 * @return 是否继续迭代，返回true代表继续迭代，false则终止迭代
		 */
		public boolean operate(Field f, boolean isStatic, F value);
	}

	@FunctionalInterface
	public static interface SimpleFieldOperation<F> {
		/**
		 * 遍历每个字段，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param f
		 * @param isStatic 目标字段是否是静态的
		 * @param value    字段值，无效则为null
		 */
		public boolean operate(String field_name, Class<?> field_type, boolean isStatic, F value);
	}

	/**
	 * op()中形参value为字段值<br>
	 * 字段如果是静态的，则传入值；如果是非静态字段则传入target的该字段值（若target为Class<?>则表示无对象，传入null）
	 * 
	 * @param obj
	 * @param op
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <F> void walkFields(Object target, FieldOperation<F> op) {
		Class<?> cls;
		Object obj;
		if (target instanceof Class c) {
			cls = c;
			obj = null;
		} else {
			cls = target.getClass();
			obj = target;
		}
		Field[] fields = reflection.get_declared_fields(cls);
		FieldOperation rop = (FieldOperation) op;
		for (Field f : fields) {
			boolean isStatic = Modifier.isStatic(f.getModifiers());
			if (!rop.operate(f, isStatic, isStatic ? ObjectManipulator.access(cls, f) : (obj == null ? null : ObjectManipulator.access(obj, f))))
				return;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <F> void walkFields(Object target, SimpleFieldOperation<F> op) {
		SimpleFieldOperation rop = (SimpleFieldOperation) op;
		walkFields(target, (Field f, boolean isStatic, Object value) -> {
			return rop.operate(f.getName(), f.getType(), isStatic, value);
		});
	}

	@FunctionalInterface
	public static interface AnnotatedFieldOperation<F, T extends Annotation> {
		/**
		 * 遍历每个具有某注解的字段
		 * 
		 * @param f
		 * @param isStatic 目标字段是否是静态的
		 * @param value    字段值，无效则为null
		 */
		public boolean operate(Field f, boolean isStatic, F value, T annotation);
	}

	@FunctionalInterface
	public static interface SimpleAnnotatedFieldOperation<F, T extends Annotation> {
		/**
		 * 遍历每个具有某注解的字段
		 * 
		 * @param f
		 * @param isStatic 目标字段是否是静态的
		 * @param value    字段值，无效则为null
		 */
		public boolean operate(String field_name, Class<?> field_type, boolean isStatic, F value, T annotation);
	}

	/**
	 * 遍历含有某个注解的全部字段
	 * 
	 * @param cls
	 * @param annotation
	 * @param op
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <F, T extends Annotation> void walkAnnotatedFields(Object target, Class<T> annotationCls, AnnotatedFieldOperation<F, T> op) {
		AnnotatedFieldOperation rop = (AnnotatedFieldOperation) op;
		walkFields(target, (Field f, boolean isStatic, Object value) -> {
			T annotation = f.getAnnotation(annotationCls);
			if (annotation != null)
				return rop.operate(f, isStatic, value, annotation);
			return true;
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <F, T extends Annotation> void walkAnnotatedFields(Object target, Class<T> annotationCls, SimpleAnnotatedFieldOperation<F, T> op) {
		SimpleAnnotatedFieldOperation rop = (SimpleAnnotatedFieldOperation) op;
		walkAnnotatedFields(target, annotationCls, (Field f, boolean isStatic, Object value, T annotation) -> {
			return rop.operate(f.getName(), f.getType(), isStatic, value, annotation);
		});
	}

	/**
	 * 遍历指定类的目标类型或其子类的字段
	 * 
	 * @param <T>
	 * @param cls
	 * @param targetType
	 * @param op
	 */
	@SuppressWarnings("unchecked")
	public static <T> void walkTypeFields(Object target, Class<T> targetType, FieldOperation<T> op) {
		walkFields(target, (Field f, boolean isStatic, Object value) -> {
			if (reflection.is(f, targetType))
				return op.operate(f, isStatic, (T) value);
			return true;
		});
	}

	@FunctionalInterface
	public static interface GenericFieldOperation<F> {
		/**
		 * 遍历具有单个泛型参数的字段
		 * 
		 * @param f
		 * @param isStatic 目标字段是否是静态的
		 * @param value    字段值，无效则为null
		 * @return 是否继续迭代，返回true代表继续迭代，false则终止迭代
		 */
		public boolean operate(Field f, boolean isStatic, Class<?> genericType, F value);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final <F> void walkGenericFields(Object target, Class<F> fieldType, GenericFieldOperation<F> op) {
		GenericFieldOperation rop = (GenericFieldOperation) op;
		KlassWalker.walkTypeFields(target, fieldType, (Field f, boolean isStatic, F value) -> {
			return rop.operate(f, isStatic, GenericTypes.first_generic_class(f), value);
		});
	}

	/**
	 * 遍历target中全部第一个泛型参数为singleGenericType的fieldType类型的字段
	 * 
	 * @param <F>
	 * @param target
	 * @param fieldType
	 * @param singleGenericType
	 * @param op
	 */
	public static final <F, G> void walkGenericFields(Object target, Class<F> fieldType, Class<G> singleGenericType, FieldOperation<F> op) {
		KlassWalker.walkGenericFields(target, fieldType, (Field f, boolean isStatic, Class<?> genericType, F value) -> {
			if (reflection.is(genericType, singleGenericType)) {
				return op.operate(f, isStatic, (F) value);
			}
			return true;
		});
	}

	@FunctionalInterface
	public static interface MethodOperation<M> {
		/**
		 * 遍历每个方法，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param m
		 * @param isStatic 目标字段是否是静态的
		 * @param value    方法所属对象实例，静态方法则为null
		 */
		public boolean operate(Method m, boolean isStatic, M obj);
	}

	@FunctionalInterface
	public static interface AnnotatedMethodOperation<M, T extends Annotation> {
		/**
		 * 遍历每个具有某注解的方法
		 * 
		 * @param m
		 * @param isStatic 目标字段是否是静态的
		 * @param value    方法所属对象实例，静态方法则为null
		 */
		public boolean operate(Method m, boolean isStatic, M obj, T annotation);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <M> void walkMethods(Object target, MethodOperation<M> op) {
		Class<?> cls;
		Object obj;
		if (target instanceof Class c) {
			cls = c;
			obj = null;
		} else {
			cls = target.getClass();
			obj = target;
		}
		Method[] methods = reflection.get_declared_methods(cls);
		MethodOperation rop = (MethodOperation) op;
		for (Method m : methods) {
			boolean isStatic = Modifier.isStatic(m.getModifiers());
			if (!rop.operate(m, isStatic, obj))
				return;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <M, T extends Annotation> void walkAnnotatedMethods(Object target, Class<T> annotationCls, AnnotatedMethodOperation<M, T> op) {
		AnnotatedMethodOperation rop = (AnnotatedMethodOperation) op;
		walkMethods(target, (Method m, boolean isStatic, Object obj) -> {
			T annotation = m.getAnnotation(annotationCls);
			if (annotation != null)
				return rop.operate(m, isStatic, obj, annotation);
			return true;
		});
	}

	@FunctionalInterface
	public static interface ConstructorOperation<C> {
		/**
		 * 遍历每个构造函数，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param c
		 */
		public boolean operate(Constructor<C> c);
	}

	@FunctionalInterface
	public static interface AnnotatedConstructorOperation<C, T extends Annotation> {
		/**
		 * 遍历每个具有某注解的构造函数
		 * 
		 * @param c
		 * @param annotation
		 */
		public boolean operate(Constructor<C> c, T annotation);
	}

	public static <C> void walkConstructors(Class<C> cls, ConstructorOperation<C> op) {
		Constructor<C>[] constructors = reflection.__get_declared_constructors(cls);
		for (Constructor<C> c : constructors) {
			if (!op.operate(c))
				return;
		}
	}

	public static <C, T extends Annotation> void walkAnnotatedConstructors(Class<C> cls, Class<T> annotationCls, AnnotatedConstructorOperation<C, T> op) {
		walkConstructors(cls, (Constructor<C> c) -> {
			T annotation = c.getAnnotation(annotationCls);
			if (annotation != null)
				return op.operate(c, annotation);
			return true;
		});
	}

	@FunctionalInterface
	public static interface ExecutableOperation<E> {
		/**
		 * 遍历每个方法或构造函数，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param e
		 * @param isStatic 目标字段是否是静态的
		 * @param value    字段值，无效则为null
		 */
		public boolean operate(Executable e, boolean isStatic, E obj);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <E> void walkExecutables(Object target, ExecutableOperation<E> op) {
		Class<?> cls;
		Object obj;
		if (target instanceof Class c) {
			cls = c;
			obj = null;
		} else {
			cls = target.getClass();
			obj = target;
		}
		Method[] methods = reflection.get_declared_methods(cls);
		Constructor<?>[] constructors = reflection.__get_declared_constructors(cls);
		ExecutableOperation rop = (ExecutableOperation) op;
		for (Constructor<?> c : constructors) {
			if (!rop.operate(c, Modifier.isStatic(c.getModifiers()), obj))
				return;
		}
		for (Method m : methods) {
			if (!rop.operate(m, Modifier.isStatic(m.getModifiers()), obj))
				return;
		}
	}

	@FunctionalInterface
	public static interface AccessibleObjectOperation<A> {
		/**
		 * 遍历每个字段，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param ao
		 * @param isStatic 目标字段是否是静态的
		 * @param value    字段值，无效则为null
		 */
		public boolean operate(AccessibleObject ao, boolean isStatic, A obj);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <A> void walkAccessibleObjects(Object target, AccessibleObjectOperation<A> op) {
		Class<?> cls;
		Object obj;
		if (target instanceof Class c) {
			cls = c;
			obj = null;
		} else {
			cls = target.getClass();
			obj = target;
		}
		Field[] fields = reflection.get_declared_fields(cls);
		Method[] methods = reflection.get_declared_methods(cls);
		Constructor<?>[] constructors = reflection.__get_declared_constructors(cls);
		AccessibleObjectOperation rop = (AccessibleObjectOperation) op;
		for (Field f : fields) {
			if (!rop.operate(f, Modifier.isStatic(f.getModifiers()), obj))
				return;
		}
		for (Constructor<?> c : constructors) {
			if (!rop.operate(c, Modifier.isStatic(c.getModifiers()), obj))
				return;
		}
		for (Method m : methods) {
			if (!rop.operate(m, Modifier.isStatic(m.getModifiers()), obj))
				return;
		}
	}

}

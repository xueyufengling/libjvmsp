package jvmsp;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import jvmsp.KlassScanner.filter;
import jvmsp.KlassScanner.filter._class;

/**
 * 遍历类的工具，无视访问修饰符和反射过滤<br>
 * 提供原始Field、Method、Constructor等，对其进行修改会导致反射获取到的所有副本都被修改
 */
public class class_info {
	@FunctionalInterface
	public static interface field_operation<_F> {
		/**
		 * 遍历每个字段，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param f
		 * @param is_static 目标字段是否是静态的
		 * @param value     字段值，无效则为null
		 * @return 是否继续迭代，返回true代表继续迭代，false则终止迭代
		 */
		public boolean operate(Field f, boolean is_static, _F value);

		@FunctionalInterface
		public static interface simple<_F> {
			/**
			 * 遍历每个字段，处理的是root原对象，即反射缓存的对象。
			 * 
			 * @param f
			 * @param is_static 目标字段是否是静态的
			 * @param value     字段值，无效则为null
			 */
			public boolean operate(String field_name, Class<?> field_type, boolean is_static, _F value);
		}

		@FunctionalInterface
		public static interface annotated<_F, _T extends Annotation> {
			/**
			 * 遍历每个具有某注解的字段
			 * 
			 * @param f
			 * @param is_static 目标字段是否是静态的
			 * @param value     字段值，无效则为null
			 */
			public boolean operate(Field f, boolean is_static, _F value, _T annotation);

			@FunctionalInterface
			public static interface simple<_F, _T extends Annotation> {
				/**
				 * 遍历每个具有某注解的字段
				 * 
				 * @param f
				 * @param is_static 目标字段是否是静态的
				 * @param value     字段值，无效则为null
				 */
				public boolean operate(String field_name, Class<?> field_type, boolean is_static, _F value, _T annotation);
			}
		}

		@FunctionalInterface
		public static interface generic<_F> {
			/**
			 * 遍历具有单个泛型参数的字段
			 * 
			 * @param f
			 * @param is_static 目标字段是否是静态的
			 * @param value     字段值，无效则为null
			 * @return 是否继续迭代，返回true代表继续迭代，false则终止迭代
			 */
			public boolean operate(Field f, boolean is_static, Class<?> genericType, _F value);
		}
	}

	/**
	 * op()中形参value为字段值<br>
	 * 字段如果是静态的，则传入值；如果是非静态字段则传入target的该字段值（若target为Class<?>则表示无对象，传入null）
	 * 
	 * @param obj
	 * @param op
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <_F> void walk_fields(Object target, field_operation<_F> op) {
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
		field_operation rop = (field_operation) op;
		for (Field f : fields) {
			boolean is_static = Modifier.isStatic(f.getModifiers());
			if (!rop.operate(f, is_static, is_static ? reflection.read(cls, f) : (obj == null ? null : reflection.read(obj, f))))
				return;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <_F> void walk_fields(Object target, field_operation.simple<_F> op) {
		field_operation.simple rop = (field_operation.simple) op;
		walk_fields(target, (Field f, boolean is_static, Object value) -> {
			return rop.operate(f.getName(), f.getType(), is_static, value);
		});
	}

	/**
	 * 遍历含有某个注解的全部字段
	 * 
	 * @param cls
	 * @param annotation
	 * @param op
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <_F, _T extends Annotation> void walk_fields(Object target, Class<_T> annotation_cls, field_operation.annotated<_F, _T> op) {
		field_operation.annotated rop = (field_operation.annotated) op;
		walk_fields(target, (Field f, boolean is_static, Object value) -> {
			_T annotation = f.getAnnotation(annotation_cls);
			if (annotation != null)
				return rop.operate(f, is_static, value, annotation);
			return true;
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <_F, _T extends Annotation> void walk_fields(Object target, Class<_T> annotation_cls, field_operation.annotated.simple<_F, _T> op) {
		field_operation.annotated.simple rop = (field_operation.annotated.simple) op;
		walk_fields(target, annotation_cls, (Field f, boolean is_static, Object value, _T annotation) -> {
			return rop.operate(f.getName(), f.getType(), is_static, value, annotation);
		});
	}

	/**
	 * 遍历指定类的目标类型或其子类的字段
	 * 
	 * @param <_T>
	 * @param cls
	 * @param targetType
	 * @param op
	 */
	@SuppressWarnings("unchecked")
	public static <_T> void walk_fields(Object target, Class<_T> targetType, field_operation<_T> op) {
		walk_fields(target, (Field f, boolean is_static, Object value) -> {
			if (reflection.is(f, targetType))
				return op.operate(f, is_static, (_T) value);
			return true;
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final <_F> void walk_fields(Object target, Class<_F> field_type, field_operation.generic<_F> op) {
		field_operation.generic rop = (field_operation.generic) op;
		class_info.walk_fields(target, field_type, (Field f, boolean is_static, _F value) -> {
			return rop.operate(f, is_static, reflection.first_generic_class(f), value);
		});
	}

	/**
	 * 遍历target中全部第一个泛型参数为single_generic_type的field_type类型的字段
	 * 
	 * @param <_F>
	 * @param target
	 * @param field_type
	 * @param single_generic_type
	 * @param op
	 */
	public static final <_F, _G> void walk_fields(Object target, Class<_F> field_type, Class<_G> single_generic_type, field_operation<_F> op) {
		class_info.walk_fields(target, field_type, (Field f, boolean is_static, Class<?> genericType, _F value) -> {
			if (reflection.is(genericType, single_generic_type)) {
				return op.operate(f, is_static, (_F) value);
			}
			return true;
		});
	}

	@FunctionalInterface
	public static interface method_operation<_M> {
		/**
		 * 遍历每个方法，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param m
		 * @param is_static 目标字段是否是静态的
		 * @param value     方法所属对象实例，静态方法则为null
		 */
		public boolean operate(Method m, boolean is_static, _M obj);

		@FunctionalInterface
		public static interface annotated<_M, _T extends Annotation> {
			/**
			 * 遍历每个具有某注解的方法
			 * 
			 * @param m
			 * @param is_static 目标字段是否是静态的
			 * @param value     方法所属对象实例，静态方法则为null
			 */
			public boolean operate(Method m, boolean is_static, _M obj, _T annotation);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <_M> void walk_methods(Object target, method_operation<_M> op) {
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
		method_operation rop = (method_operation) op;
		for (Method m : methods) {
			boolean is_static = Modifier.isStatic(m.getModifiers());
			if (!rop.operate(m, is_static, obj))
				return;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <_M, _T extends Annotation> void walk_methods(Object target, Class<_T> annotation_cls, method_operation.annotated<_M, _T> op) {
		method_operation.annotated rop = (method_operation.annotated) op;
		walk_methods(target, (Method m, boolean is_static, Object obj) -> {
			_T annotation = m.getAnnotation(annotation_cls);
			if (annotation != null)
				return rop.operate(m, is_static, obj, annotation);
			return true;
		});
	}

	@FunctionalInterface
	public static interface constructor_operation<_C> {
		/**
		 * 遍历每个构造函数，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param c
		 */
		public boolean operate(Constructor<_C> c);

		@FunctionalInterface
		public static interface annotated<_C, _T extends Annotation> {
			/**
			 * 遍历每个具有某注解的构造函数
			 * 
			 * @param c
			 * @param annotation
			 */
			public boolean operate(Constructor<_C> c, _T annotation);
		}
	}

	public static <_C> void walk_constructors(Class<_C> cls, constructor_operation<_C> op) {
		Constructor<_C>[] constructors = reflection.__get_declared_constructors(cls);
		for (Constructor<_C> c : constructors) {
			if (!op.operate(c))
				return;
		}
	}

	public static <_C, _T extends Annotation> void walk_constructors(Class<_C> cls, Class<_T> annotation_cls, constructor_operation.annotated<_C, _T> op) {
		walk_constructors(cls, (Constructor<_C> c) -> {
			_T annotation = c.getAnnotation(annotation_cls);
			if (annotation != null)
				return op.operate(c, annotation);
			return true;
		});
	}

	@FunctionalInterface
	public static interface executable_operation<_E> {
		/**
		 * 遍历每个方法或构造函数，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param e
		 * @param is_static 目标字段是否是静态的
		 * @param value     字段值，无效则为null
		 */
		public boolean operate(Executable e, boolean is_static, _E obj);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <_E> void walk_executables(Object target, executable_operation<_E> op) {
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
		executable_operation rop = (executable_operation) op;
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
	public static interface accessible_object_operation<_A> {
		/**
		 * 遍历每个字段，处理的是root原对象，即反射缓存的对象。
		 * 
		 * @param ao
		 * @param is_static 目标字段是否是静态的
		 * @param value     字段值，无效则为null
		 */
		public boolean operate(AccessibleObject ao, boolean is_static, _A obj);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <_A> void walk_accessible_objects(Object target, accessible_object_operation<_A> op) {
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
		accessible_object_operation rop = (accessible_object_operation) op;
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

	/**
	 * 扫描过滤
	 */
	@FunctionalInterface
	public static interface filter {
		/**
		 * 过滤AnnotatedElement的条件。
		 * 
		 * @param scanned_cls
		 * @return 返回为true才收集该元素
		 */
		public boolean condition(AnnotatedElement scanned_ae);

		public static final filter RESERVE_ALL = (AnnotatedElement scanned_ae) -> true;

		@FunctionalInterface
		public static interface _class {
			/**
			 * 过滤扫描到的类，只有返回true的类才被保留。
			 * 
			 * @param scanned_cls
			 * @return
			 */
			public boolean condition(Class<?> scanned_cls);

			public static final _class RESERVE_ALL = (Class<?> scanned_cls) -> true;
		}
	}

	/**
	 * 扫描所有被注解的元素，包括Class，Field，Method，Constructor等<br>
	 * 
	 * @param loader
	 * @param filter
	 * @return
	 */
	public static ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader, filter filter) {
		ArrayList<AnnotatedElement> annotated = new ArrayList<>();
		ArrayList<Class<?>> classes = class_loader.loaded_classes_copy(loader);
		for (Class<?> cls : classes) {
			if (cls.getAnnotations().length > 0) {
				if (filter.condition(cls))
					annotated.add(cls);
			}
			class_info.walk_accessible_objects(cls, (AccessibleObject ao, boolean isStatic, Object obj) -> {
				if (ao.getAnnotations().length > 0) {
					if (filter.condition(ao))
						annotated.add(ao);
				}
				return true;
			});
		}
		return annotated;
	}

	public static ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader) {
		return scan_annotated_elements(loader, filter.RESERVE_ALL);
	}

	/**
	 * 扫描指定ClassLoader的指定注解的元素
	 * 
	 * @param <_T>
	 * @param loader         要扫描的ClassLoader
	 * @param annotation_cls 注解类
	 * @param filter         过滤条件
	 * @return
	 */
	public static <_T extends Annotation> ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader, Class<_T> annotation_cls, filter filter) {
		ArrayList<AnnotatedElement> annotated = new ArrayList<>();
		ArrayList<Class<?>> classes = class_loader.loaded_classes_copy(loader);
		for (Class<?> cls : classes) {
			if (cls.isAnnotationPresent(annotation_cls)) {
				if (filter.condition(cls))// 不满足条件的AnnotatedElement不放入结果
					annotated.add(cls);
			}
			class_info.walk_accessible_objects(cls, (AccessibleObject ao, boolean isStatic, Object obj) -> {
				if (ao.isAnnotationPresent(annotation_cls)) {
					if (filter.condition(ao))
						annotated.add(ao);
				}
				return true;
			});
		}
		return annotated;
	}

	public static <_T extends Annotation> ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader, Class<_T> annotation_cls) {
		return scan_annotated_elements(loader, annotation_cls, filter.RESERVE_ALL);
	}

	public static <_T extends Annotation> ArrayList<AnnotatedElement> scan_annotated_elements(ClassLoader loader, Class<_T> annotation_cls, filter._class filter) {
		ArrayList<AnnotatedElement> annotated = new ArrayList<>();
		ArrayList<Class<?>> classes = class_loader.loaded_classes_copy(loader);
		for (Class<?> cls : classes) {
			// 不满足条件的类直接略过
			if (!filter.condition(cls))
				continue;
			if (cls.isAnnotationPresent(annotation_cls))
				annotated.add(cls);
			class_info.walk_accessible_objects(cls, (AccessibleObject ao, boolean isStatic, Object obj) -> {
				if (ao.isAnnotationPresent(annotation_cls))
					annotated.add(ao);
				return true;
			});
		}
		return annotated;
	}

	public static <_T extends Annotation> ArrayList<AnnotatedElement> scan_annotated_classes(ClassLoader loader, Class<_T> annotation_cls) {
		return scan_annotated_elements(loader, annotation_cls, filter._class.RESERVE_ALL);
	}
}

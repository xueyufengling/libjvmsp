package jvmsp;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;

public class KlassScanner {
	@FunctionalInterface
	public static interface Filter {
		/**
		 * 过滤AnnotatedElement的条件。
		 * 
		 * @param scanned_cls
		 * @return 返回为true才收集该元素
		 */
		public boolean condition(AnnotatedElement scanned_ae);

		public static final Filter RESERVE_ALL = (AnnotatedElement scanned_ae) -> true;

		@FunctionalInterface
		public static interface KlassFilter {
			/**
			 * 过滤扫描到的类，只有返回true的类才被保留。
			 * 
			 * @param scanned_cls
			 * @return
			 */
			public boolean condition(Class<?> scanned_cls);

			public static final KlassFilter RESERVE_ALL = (Class<?> scanned_cls) -> true;
		}
	}

	/**
	 * 扫描所有被注解的元素，包括Class，Field，Method，Constructor等<br>
	 * 
	 * @param loader
	 * @param filter
	 * @return
	 */
	public static ArrayList<AnnotatedElement> scanAnnotatedElements(ClassLoader loader, Filter filter) {
		ArrayList<AnnotatedElement> annotated = new ArrayList<>();
		ArrayList<Class<?>> classes = class_loader.loadedClassesCopy(loader);
		for (Class<?> cls : classes) {
			if (cls.getAnnotations().length > 0) {
				if (filter.condition(cls))
					annotated.add(cls);
			}
			KlassWalker.walkAccessibleObjects(cls, (AccessibleObject ao, boolean isStatic, Object obj) -> {
				if (ao.getAnnotations().length > 0) {
					if (filter.condition(ao))
						annotated.add(ao);
				}
				return true;
			});
		}
		return annotated;
	}

	public static ArrayList<AnnotatedElement> scanAnnotatedElements(ClassLoader loader) {
		return scanAnnotatedElements(loader, Filter.RESERVE_ALL);
	}

	/**
	 * 扫描指定ClassLoader的指定注解的元素
	 * 
	 * @param <T>
	 * @param loader         要扫描的ClassLoader
	 * @param annotation_cls 注解类
	 * @param filter         过滤条件
	 * @return
	 */
	public static <T extends Annotation> ArrayList<AnnotatedElement> scanAnnotatedElements(ClassLoader loader, Class<T> annotation_cls, Filter filter) {
		ArrayList<AnnotatedElement> annotated = new ArrayList<>();
		ArrayList<Class<?>> classes = class_loader.loadedClassesCopy(loader);
		for (Class<?> cls : classes) {
			if (cls.isAnnotationPresent(annotation_cls)) {
				if (filter.condition(cls))// 不满足条件的AnnotatedElement不放入结果
					annotated.add(cls);
			}
			KlassWalker.walkAccessibleObjects(cls, (AccessibleObject ao, boolean isStatic, Object obj) -> {
				if (ao.isAnnotationPresent(annotation_cls)) {
					if (filter.condition(ao))
						annotated.add(ao);
				}
				return true;
			});
		}
		return annotated;
	}

	public static <T extends Annotation> ArrayList<AnnotatedElement> scanAnnotatedElements(ClassLoader loader, Class<T> annotation_cls) {
		return scanAnnotatedElements(loader, annotation_cls, Filter.RESERVE_ALL);
	}

	public static <T extends Annotation> ArrayList<AnnotatedElement> scanAnnotatedElements(ClassLoader loader, Class<T> annotation_cls, Filter.KlassFilter filter) {
		ArrayList<AnnotatedElement> annotated = new ArrayList<>();
		ArrayList<Class<?>> classes = class_loader.loadedClassesCopy(loader);
		for (Class<?> cls : classes) {
			// 不满足条件的类直接略过
			if (!filter.condition(cls))
				continue;
			if (cls.isAnnotationPresent(annotation_cls))
				annotated.add(cls);
			KlassWalker.walkAccessibleObjects(cls, (AccessibleObject ao, boolean isStatic, Object obj) -> {
				if (ao.isAnnotationPresent(annotation_cls))
					annotated.add(ao);
				return true;
			});
		}
		return annotated;
	}

	public static <T extends Annotation> ArrayList<AnnotatedElement> scanAnnotatedElementsFilterClass(ClassLoader loader, Class<T> annotation_cls) {
		return scanAnnotatedElements(loader, annotation_cls, Filter.KlassFilter.RESERVE_ALL);
	}
}

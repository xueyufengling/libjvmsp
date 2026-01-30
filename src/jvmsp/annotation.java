package jvmsp;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;

/**
 * Java的注解不能实现接口，因此只能镜像注解内部类实现该接口。
 * 镜像注解内部类中必须存在一个静态的MirrorAnnotation对象，此时可以通过reflect_all()来替换注解。<br>
 * 内部类中必须包含一个本类型的名为mirrorInstance的静态字段。详情参考{@link jvmsp.CallerSensitive}
 * 
 * @param <_MirrorAnno> 镜像注解
 * @param <MC>          镜像注解的内部类
 * @param <_ReflAnno>   目标类
 */
public interface annotation<_MirrorAnno extends Annotation, MC, _ReflAnno extends Annotation> extends mirror<_MirrorAnno, _ReflAnno> {
	public static final String STATIC_MIRROR_INSTANCE_FIELD = "mirrorInstance";
	/**
	 * 该镜像注解的目标注解是否是系统注解，默认为true。<br>
	 * 该接口的存在目的就是为了伪造并转换成系统注解。
	 */
	public static boolean DEFAULT_IS_DEST_SYSTEM_ANNOTATION = true;

	/**
	 * 提供目标注解的一个实例
	 * 
	 * @return
	 */
	public abstract _ReflAnno reflect();

	/**
	 * 目标注解的类型，当获取的目标注解实例的annotationType()类型和其声明的类型不一致时需要手动指定目标注解的Class<?>
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default Class<_ReflAnno> reflect_class() {
		return (Class<_ReflAnno>) reflect().annotationType();
	}

	/**
	 * 镜像注解的Class对象
	 * 
	 * @return
	 */
	public abstract Class<_MirrorAnno> mirror_class();

	@Override
	public default Class<_MirrorAnno> derived_class() {
		return mirror_class();
	}

	public default boolean isDestSystemAnnotation() {
		return DEFAULT_IS_DEST_SYSTEM_ANNOTATION;
	}

	@Override
	@SuppressWarnings("unchecked")
	public default _ReflAnno cast(_MirrorAnno mirrorAnnotation) {
		return (_ReflAnno) oops.cast(mirrorAnnotation, reflect());
	}

	/**
	 * 在转换之前需要进行的操作
	 * 
	 * @param ae
	 */
	public default void operate(AnnotatedElement ae) {

	}

	/**
	 * 将所有镜像注解替换成目标注解
	 * 
	 * @param isDestSystemAnnotation 目标注解是否是系统级注解，如果是，那么必须将包含注解的类设置为系统类
	 */
	public default void reflect_all(ClassLoader loader, Class<_MirrorAnno> mirrorAnnotationClass, Class<? extends Annotation> targetAnnotationCls, Annotation destAnnotation, boolean isDestSystemAnnotation) {
		ArrayList<AnnotatedElement> annotated = class_info.scan_annotated_elements(loader, mirrorAnnotationClass);
		for (AnnotatedElement ae : annotated) {
			operate(ae);
			// 如果是系统注解，那么包含该注解的类也必须是BootstrapLoader加载的类
			if (isDestSystemAnnotation) {
				Class<?> includeCls = reflection.declaring_class(ae);
				class_loader.as_bootstrap(includeCls);
			}
			reflection.cast(ae, mirrorAnnotationClass, targetAnnotationCls, destAnnotation);
		}
	}

	public default void reflect_all(ClassLoader loader, Annotation destAnnotation, boolean isDestSystemAnnotation) {
		reflect_all(loader, this.derived_class(), this.reflect_class(), destAnnotation, isDestSystemAnnotation);
	}

	public default void reflect_all(Annotation destAnnotation, boolean isDestSystemAnnotation) {
		Class<_MirrorAnno> mirrorAnnotationClass = this.derived_class();
		reflect_all(mirrorAnnotationClass.getClassLoader(), mirrorAnnotationClass, this.reflect_class(), destAnnotation, isDestSystemAnnotation);
	}

	public default void reflect_all() {
		reflect_all(this.reflect(), this.isDestSystemAnnotation());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void reflect_all(String staticMirrorInstanceField, Class<? extends annotation>... mirror_annotation_impls) {
		for (Class<?> cls : mirror_annotation_impls) {
			reflection.find_class(cls.getName(), true);
			annotation mirrorInstance = (annotation) reflection.read(cls, staticMirrorInstanceField);
			mirrorInstance.reflect_all();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void reflect_all(Class<? extends annotation>... mirror_annotation_impls) {
		reflect_all(annotation.STATIC_MIRROR_INSTANCE_FIELD, mirror_annotation_impls);
	}
}

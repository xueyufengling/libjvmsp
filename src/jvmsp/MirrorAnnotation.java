package jvmsp;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;

/**
 * Java的注解不能实现接口，因此只能镜像注解内部类实现该接口。
 * 镜像注解内部类中必须存在一个静态的MirrorAnnotation对象，此时可以通过castAllAnnotations()来替换注解。<br>
 * 内部类中必须包含一个本类型的名为mirrorInstance的静态字段。详情参考{@link jvmsp.CallerSensitive}
 * 
 * @param <MA> 镜像注解
 * @param <MC> 镜像注解的内部类
 * @param <D>  目标类
 */
public interface MirrorAnnotation<MA extends Annotation, MC, D extends Annotation> extends Mirror<MA, D> {
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
	public abstract D destAnnotationInstance();

	/**
	 * 目标注解的类型，当获取的目标注解实例的annotationType()类型和其声明的类型不一致时需要手动指定目标注解的Class<?>
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default Class<D> destAnnotationClass() {
		return (Class<D>) destAnnotationInstance().annotationType();
	}

	/**
	 * 镜像注解的Class对象
	 * 
	 * @return
	 */
	public abstract Class<MA> mirrorAnnotationClass();

	@Override
	public default Class<MA> getDerivedClass() {
		return mirrorAnnotationClass();
	}

	public default boolean isDestSystemAnnotation() {
		return DEFAULT_IS_DEST_SYSTEM_ANNOTATION;
	}

	@Override
	@SuppressWarnings("unchecked")
	public default D cast(MA mirrorAnnotation) {
		return (D) ObjectManipulator.cast(mirrorAnnotation, destAnnotationInstance());
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
	public default void castAllAnnotations(ClassLoader loader, Class<MA> mirrorAnnotationClass, Class<? extends Annotation> targetAnnotationCls, Annotation destAnnotation, boolean isDestSystemAnnotation) {
		ArrayList<AnnotatedElement> annotated = KlassScanner.scanAnnotatedElements(loader, mirrorAnnotationClass);
		for (AnnotatedElement ae : annotated) {
			operate(ae);
			// 如果是系统注解，那么包含该注解的类也必须是BootstrapLoader加载的类
			if (isDestSystemAnnotation) {
				Class<?> includeCls = annotations.declaring_class(ae);
				Klass.as_bootstrap(includeCls);
			}
			annotations.cast(ae, mirrorAnnotationClass, targetAnnotationCls, destAnnotation);
		}
	}

	public default void castAllAnnotations(ClassLoader loader, Annotation destAnnotation, boolean isDestSystemAnnotation) {
		castAllAnnotations(loader, this.getDerivedClass(), this.destAnnotationClass(), destAnnotation, isDestSystemAnnotation);
	}

	public default void castAllAnnotations(Annotation destAnnotation, boolean isDestSystemAnnotation) {
		Class<MA> mirrorAnnotationClass = this.getDerivedClass();
		castAllAnnotations(mirrorAnnotationClass.getClassLoader(), mirrorAnnotationClass, this.destAnnotationClass(), destAnnotation, isDestSystemAnnotation);
	}

	public default void castAllAnnotations() {
		castAllAnnotations(this.destAnnotationInstance(), this.isDestSystemAnnotation());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void enableMirrorAnnotations(String staticMirrorInstanceField, Class<? extends MirrorAnnotation>... mirrorAnnotationImplClasses) {
		for (Class<?> cls : mirrorAnnotationImplClasses) {
			reflection.find_class(cls.getName(), true);
			MirrorAnnotation mirrorInstance = (MirrorAnnotation) ObjectManipulator.access(cls, staticMirrorInstanceField);
			mirrorInstance.castAllAnnotations();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void enableMirrorAnnotations(Class<? extends MirrorAnnotation>... mirrorAnnotationImplClasses) {
		enableMirrorAnnotations(MirrorAnnotation.STATIC_MIRROR_INSTANCE_FIELD, mirrorAnnotationImplClasses);
	}
}

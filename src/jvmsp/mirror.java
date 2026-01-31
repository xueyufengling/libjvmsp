package jvmsp;

import java.lang.reflect.AnnotatedElement;

/**
 * 镜像类，使用无法访问的类时将镜像类用做占位符。镜像类对象可以转换成目标类对象。<br>
 * 需要自己保证镜像类和目标类的内存布局相同。
 * 考虑到目标类有可能没有其访问权限不能编译，故不将其用作模板参数，而是在reflect_class()中提供
 * 
 * @param <_Mirror> 镜像类
 */
public interface mirror<_Mirror> extends jtype._crtp<_Mirror> {
	/**
	 * 目标类
	 * 
	 * @return
	 */
	public abstract Class<?> reflect_class();

	/**
	 * 镜像类，即实现本接口的类
	 * 
	 * @return
	 */
	public abstract Class<_Mirror> unreflect_class();

	/**
	 * 将本对象转换为目标对象
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default <_T> _T reflect() {
		return (_T) jtype.cast(this, reflect_class());
	}

	/**
	 * 将本对象还原为镜像对象
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default _Mirror unreflect() {
		return (_Mirror) jtype.cast(this, unreflect_class());
	}

	/**
	 * Java的注解不能实现接口，因此只能镜像注解内部类实现该接口。
	 * 镜像注解内部类中必须存在一个静态的MirrorAnnotation对象，此时可以通过reflect_all()来替换注解。<br>
	 * 内部类中必须包含一个本类型的名为mirror_instance的静态字段。详情参考{@link jvmsp.DummyCallerSensitive}
	 * 
	 * @param <_MirrorAnno> 镜像注解
	 * @param <_MirrorImpl> 镜像注解的内部类,是实现类
	 */
	public interface annotation<_MirrorAnno, _MirrorImpl> extends mirror<_MirrorAnno> {
		/**
		 * 该镜像注解的目标注解是否是系统注解，默认为true。<br>
		 * 该接口的存在目的就是为了伪造并转换成系统注解。
		 */
		public static boolean DEFAULT_IS_DEST_SYSTEM_ANNOTATION = true;

		public default boolean is_system() {
			return DEFAULT_IS_DEST_SYSTEM_ANNOTATION;
		}

		/**
		 * 在转换之前需要进行的操作
		 * 
		 * @param ae
		 */
		public default void operate(AnnotatedElement ae) {

		}

		public default void reflect_all(ClassLoader loader, boolean is_dest_system) {
			reflection.force_replace(loader, this.reflect_class(), this.reflect_class(), this, is_dest_system, (ae) -> {
				this.operate(ae);
			});
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static void reflect_all(annotation mirror_instance, Class<? extends annotation>... mirror_annotation_impls) {
			for (Class<?> cls : mirror_annotation_impls) {
				reflection.find_class(cls.getName(), true);
				mirror_instance.reflect_all(cls.getClassLoader(), false);
			}
		}

		/*
		 * 镜像注解示例：该注解实际无用，CallerSensitive在Java层的标志仅仅是缓存，JVM不读取该缓存值而是读取C++层面的标记
		 * 
		 * 运行时的该注解将转为jdk.internal.reflect.CallerSensitive。<br>
		 * 检测逻辑具体实现在jdk.internal.reflect.Reflection.isCallerSensitive(Method m)。<br>
		 * 在OpenJDK中该注解与{@code @Hidden}注解均属于特殊处理注解，在classFileParse就已经处理保存，存在于C++层的constMethod中，Reflection.getCallerClass()是直接在C++中检查调用栈的constMethod的is_caller_sensitive()标志位。
		 * 
		 * @Retention(RetentionPolicy.RUNTIME)
		 * 
		 * @Target({ ElementType.METHOD })
		 * public @interface DummyCallerSensitive {
		 * 
		 * @SuppressWarnings({ "unchecked", "rawtypes" })
		 * static class CallerSensitiveMirrorImpl implements mirror.annotation<DummyCallerSensitive, CallerSensitiveMirrorImpl> {
		 * 
		 * 未缓存CallerSensitive标志位，将调用jdk.internal.reflect.Reflection.isCallerSensitive(Method m)获取标志位
		 * static final byte not_initialized = 0;
		 * 
		 * 已初始化标志位，该方法是CallerSensitive的
		 * /*
		 * static final byte initialized_cs = 1;
		 * 
		 * 已初始化标志位，该方法不是CallerSensitive的
		 * /*
		 * static final byte initialized_not_cs = -1;
		 * 
		 * 标志位缓存状态<br>
		 * 该字段被标记为{@code @Stable}，其行为是改变一次值后和final等价，运行时JIT编译字节码时会优化内联，编译后修改该值将不再生效。<br>
		 * 该字段如果已经被初始化，将导致字段值内联，Method.isCallerSensitive()方法可能也会被内联，再修改字段可能也无法变更方法的callerSensitive状态，即该方法没有被JVM视为CallerSensitive。<br>
		 * /*
		 * private static final VarHandle Method_callerSensitive;
		 * 
		 * static final Class class_jdk_internal_reflect_CallerSensitive = reflection.find_class("jdk.internal.reflect.CallerSensitive");
		 * 
		 * CallerSensitive的实例，的实际类型为class com.sun.proxy.jdk.proxy1.$Proxy51
		 * 该实例为annotation.reflect_all()使用的参数
		 * /*
		 * public static final Annotation instance_CallerSensitive;
		 * 
		 * static {
		 * Method_callerSensitive = symbols.find_var(Method.class, "callerSensitive", byte.class);
		 * Method m = reflection.get_declared_method(Class.class, "forName", String.class);
		 * instance_CallerSensitive = m.getAnnotation(class_jdk_internal_reflect_CallerSensitive);
		 * }
		 * 
		 * private final void set_caller_sensitive_flag(Method m, byte flag) {
		 * Method_callerSensitive.set(m, flag);
		 * }
		 * 
		 * @Override
		 * public void operate(AnnotatedElement ae) {
		 * set_caller_sensitive_flag((Method) ae, initialized_cs);
		 * }
		 * 
		 * @Override
		 * public Class<?> reflect_class() {
		 * return class_jdk_internal_reflect_CallerSensitive;
		 * }
		 * 
		 * @Override
		 * public Class<DummyCallerSensitive> unreflect_class() {
		 * return DummyCallerSensitive.class;
		 * }
		 * }
		 * }
		 */
	}

}

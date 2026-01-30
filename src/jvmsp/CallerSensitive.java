package jvmsp;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * 运行时的该注解将转为jdk.internal.reflect.CallerSensitive。<br>
 * 检测逻辑具体实现在jdk.internal.reflect.Reflection.isCallerSensitive(Method m)。<br>
 * 在OpenJDK中该注解与{@code @Hidden}注解均属于特殊处理注解，在classFileParse就已经处理保存，存在于C++层的constMethod中，Reflection.getCallerClass()是直接在C++中检查调用栈的constMethod的is_caller_sensitive()标志位。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface CallerSensitive {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static class CallerSensitiveMirrorImpl implements MirrorAnnotation<CallerSensitive, CallerSensitiveMirrorImpl, Annotation> {
		/**
		 * 未缓存CallerSensitive标志位，将调用jdk.internal.reflect.Reflection.isCallerSensitive(Method m)获取标志位
		 */
		static final byte not_initialized = 0;

		/**
		 * 已初始化标志位，该方法是CallerSensitive的
		 */
		static final byte initialized_cs = 1;

		/**
		 * 已初始化标志位，该方法不是CallerSensitive的
		 */
		static final byte initialized_not_cs = -1;

		/**
		 * 标志位缓存状态<br>
		 * 该字段被标记为{@code @Stable}，其行为是改变一次值后和final等价，运行时JIT编译字节码时会优化内联，编译后修改该值将不再生效。<br>
		 * 该字段如果已经被初始化，将导致字段值内联，Method.isCallerSensitive()方法可能也会被内联，再修改字段可能也无法变更方法的callerSensitive状态，即该方法没有被JVM视为CallerSensitive。<br>
		 */
		private static final VarHandle Method_callerSensitive;

		static final Class CallerSensitiveClass = reflection.find_class("jdk.internal.reflect.CallerSensitive");

		/**
		 * CallerSensitiveInstance的实际类型为class com.sun.proxy.jdk.proxy1.$Proxy51
		 */
		static final Annotation CallerSensitiveInstance;

		static final CallerSensitiveMirrorImpl mirrorInstance;

		static {
			Method_callerSensitive = symbols.find_var(Method.class, "callerSensitive", byte.class);
			Method m = reflection.get_declared_method(Class.class, "forName", String.class);
			CallerSensitiveInstance = m.getAnnotation(CallerSensitiveClass);
			mirrorInstance = new CallerSensitiveMirrorImpl();
		}

		/**
		 * 擦除缓存标志位
		 * 
		 * @param m
		 */
		private final void setCallerSensitiveFlag(Method m, byte flag) {
			Method_callerSensitive.set(m, flag);
			reflection.methodIsCallerSensitive(m);
		}

		@Override
		public void operate(AnnotatedElement ae) {
			setCallerSensitiveFlag((Method) ae, initialized_cs);
		}

		@Override
		public Annotation destAnnotationInstance() {
			return CallerSensitiveInstance;
		}

		@Override
		public Class<CallerSensitive> mirrorAnnotationClass() {
			return CallerSensitive.class;
		}
	}
}

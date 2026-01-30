package jvmsp;

import java.lang.invoke.MethodHandle;

/**
 * 修改反射安全限制等
 */
public class JavaLang {

	/**
	 * 栈追踪时执行的操作
	 */
	@FunctionalInterface
	public interface unwind_operation {
		public void operate(StackWalker.StackFrame stack_frame);
	}

	/**
	 * 栈追踪设置
	 */
	public enum unwind_option {
		SKIP_COUNT_BY_FRAME, SKIP_COUNT_BY_CLASS
	}

	private static Class<?> SharedSecrets;// jdk.internal.access.SharedSecrets
	private static Object JavaLangAccess;// jdk.internal.access.JavaLangAccess;
	private static MethodHandle getConstantPool;// 获取指定类的类常量池The ConstantPool，其中包含静态成员、方法列表等

	public static final StackWalker stack_walker;

	static {
		stack_walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);// 最常用，最先初始化
		try {
			SharedSecrets = Class.forName("jdk.internal.access.SharedSecrets");
			Class<?> ConstantPool = Class.forName("jdk.internal.reflect.ConstantPool");
			JavaLangAccess = getAccess("JavaLangAccess");
			getConstantPool = symbols.find_special_method(JavaLangAccess.getClass(), "getConstantPool", ConstantPool, Class.class);
		} catch (ClassNotFoundException | SecurityException ex) {
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
	public static Object getAccess(String access_name) {
		return ObjectManipulator.invoke(SharedSecrets, "get" + access_name, null);
	}

	/**
	 * 获取指定类的常量池
	 * 
	 * @param clazz
	 * @return
	 */
	public static Object getConstantPool(Class<?> clazz) {
		try {
			return getConstantPool.invokeExact(JavaLangAccess, clazz);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 栈追踪<br>
	 * 本方法对应的栈帧始终是0，<br>
	 * skip 1 会返回直接调用本方法trackStackFrame(int skip_frame_count)的栈帧，即getCallerClass()方法本身栈帧<br>
	 * skip 2 会返回调用getCallerClass()的方法栈帧
	 * 
	 * @param skip_frame_count
	 * @return
	 */
	public static StackWalker.StackFrame unwind(int skip_frame_count) {
		return stack_walker.walk(stack -> stack.skip(skip_frame_count).findFirst().get());
	}

	public static void unwind(int skip_frame_count, unwind_operation op) {
		op.operate(stack_walker.walk(stack -> stack.skip(skip_frame_count).findFirst().get()));
	}

	public static Class<?> unwind_class(int skip_frame_count) {
		return stack_walker.walk(stack -> stack.skip(skip_frame_count).findFirst().get().getDeclaringClass());
	}

	/**
	 * 追踪函数调用栈帧，并获取调用的类<br>
	 * StackTrackOption为SKIP_COUNT_BY_FRAME时，行为同unwind_class(int skip_frame_count)一致<br>
	 * StackTrackOption为SKIP_COUNT_BY_CLASS时，追踪函数调用栈帧，并且返回第skip_class_count个不同的类<br>
	 * skip_class_count只表明跳过几个不同的类，对于连续同一个类调用栈帧将直接全部跳过
	 * 
	 * @param skip_count
	 * @param option
	 * @return
	 * @since Java 9
	 */
	public static Class<?> unwind_class(int skip_count, unwind_option option) {
		switch (option) {
		case SKIP_COUNT_BY_FRAME:
			return unwind_class(skip_count);
		case SKIP_COUNT_BY_CLASS: {
			int skipped_class_count = 0;
			int skip_frame = 1;// 以JavaLang作为起点
			Class<?> caller_record = unwind_class(skip_frame);
			Class<?> stack_frame_class = null;
			if (skip_count > 0) {
				for (;;) {
					stack_frame_class = unwind_class(++skip_frame);
					if (caller_record == stack_frame_class)
						continue;
					else {
						caller_record = stack_frame_class;// 将下一个与当前caller_record不同的类记录作为追踪结果
						if (++skipped_class_count >= skip_count)
							break;
					}
				}
			}
			return caller_record;
		}
		}
		return null;
	}

	/**
	 * 获取直接调用该方法的类<br>
	 * 例如A()调用B()，B()调用get_context_class()，那么返回B()栈帧
	 * 
	 * @return
	 * @since Java 9
	 * @CallerSensitive
	 */
	public static Class<?> context_class() {
		return unwind_class(2);
	}

	/**
	 * 获取一次间接调用该方法的类<br>
	 * 例如A()调用B()，B()调用getOuterCallerClass()，那么返回A()栈帧
	 * 
	 * @return
	 * @since Java 9
	 * @CallerSensitive
	 */
	public static Class<?> caller_class() {
		return unwind_class(3);
	}

	public static Class<?> caller_class_as_param() {
		return unwind_class(4);
	}
}

package jvmsp;

import java.lang.invoke.MethodHandle;

/**
 * 方法包装对象，可以修改方法调用相关参数和标志。
 */
public class callable {
	private int flags;
	private Class<?> targetClass;
	private MethodHandle warppedMethod;
	private Object memberName;
	private byte invokeBytecode;

	private callable(Class<?> targetClass, String targetMethodName, Class<?>... arg_types) {
		this.targetClass = targetClass;
		MethodHandle m = null;
		if (targetMethodName.equals(MemberName.CONSTRUCTOR_NAME))
			m = invokeVirtualConstructor(targetClass, arg_types);// 目标函数是构造函数则将构造函数转换为可当作方法调用的构造函数
		else
			m = symbols.find_method(targetClass, targetMethodName, arg_types);
		this.memberName = MemberName.member_name(m);
		this.flags = MemberName.member_name_flags(memberName);
		this.invokeBytecode = MemberName.reference_kind(memberName);
	}

	/**
	 * 获取一个可以被当作实例方法调用的构造函数。
	 * 
	 * @param targetClass
	 * @param arg_types
	 * @return
	 */
	public static MethodHandle invokeVirtualConstructor(Class<?> targetClass, Class<?>... arg_types) {
		Object memberName = MemberName.member_name(HandleBase.findConstructor(targetClass, arg_types));
		int flags = MemberName.member_name_flags(memberName);
		flags = MemberName.set_flag(flags, MemberName.IS_CONSTRUCTOR, false);// 取消构造函数标志
		flags = MemberName.set_flag(flags, MemberName.IS_METHOD, true);// 添加普通方法标志
		MemberName.set_member_name_flags(memberName, flags);
		return MemberName.direct_method(MethodHandleNatives.constants.REF_invokeVirtual, targetClass, memberName);
	}

	/**
	 * 从一个可调用对象绑定字节码。
	 * 
	 * @param cls
	 * @param targetMethodName
	 * @param arg_types
	 * @return
	 */
	public static final callable bind(Class<?> cls, String targetMethodName, Class<?>... arg_types) {
		return new callable(cls, targetMethodName, arg_types);
	}

	/**
	 * 设置标志
	 * 
	 * @param flag
	 * @param mark
	 * @return
	 */
	public callable setFlag(int flag, boolean mark) {
		this.flags = MemberName.set_flag(this.flags, flag, mark);
		return this;
	}

	/**
	 * 目标方法是否是CallerSensitive的，由于检查是在C++层的constMethod，因此为MethodHandle设置该标志无效。
	 * 
	 * @param mark
	 * @return
	 */
	public boolean isCallerSensitive(boolean mark) {
		return MemberName.is_caller_sensitive(memberName);
	}

	/**
	 * 设置标志并包装MemberName为MethodHandle
	 */
	public callable warp() {
		MemberName.set_member_name_flags(memberName, flags);
		warppedMethod = MemberName.direct_method(invokeBytecode, targetClass, memberName);
		return this;
	}

	/**
	 * 返回包装好的可调用对象的MethodHandle
	 * 
	 * @return
	 */
	public MethodHandle unwarp() {
		return warppedMethod;
	}

	/**
	 * 调用该可调用对象
	 * 
	 * @param args
	 * @return
	 */
	public Object call(Object... args) {
		try {
			return warppedMethod.invokeWithArguments(args);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return HandleBase.UNREACHABLE_REFERENCE;
	}
}

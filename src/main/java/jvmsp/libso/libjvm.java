package jvmsp.libso;

import java.lang.invoke.MethodHandle;

import jvmsp.memory;
import jvmsp.shared_object;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.function_signature;
import jvmsp.type.cxx_type.pointer;
import jvmsp.unsafe;
import jvmsp.hotspot.classfile.java_lang_Class;
import jvmsp.hotspot.oops.CompressedOops;
import jvmsp.hotspot.oops.InstanceKlass;

/**
 * libjvm.so库中的函数封装
 * JNIEnv -> JNINativeInterface_*
 * JavaVM -> JNIInvokeInterface_*
 */
public abstract class libjvm
{
	public static final cxx_type JNINativeMethod = cxx_type.define("JNINativeMethod")
			.decl_field("name", cxx_type.pchar)
			.decl_field("signature", cxx_type.pchar)
			.decl_field("fnPtr", cxx_type.pvoid)
			.resolve();

	public static final cxx_type pJNINativeMethod = cxx_type.pointer_type.of(JNINativeMethod);

	public static final cxx_type JavaVM = cxx_type.typedef(cxx_type.pvoid, "JavaVM");

	public static final cxx_type pJNIEnv = cxx_type.pointer_type.of("JNINativeInterface_");

	/**
	 * 引用类型
	 */
	public static enum ref_type
	{
		JNIInvalidRefType(0),
		JNILocalRefType(1), // 线程本地引用
		JNIGlobalRefType(2), // 全局引用
		JNIWeakGlobalRefType(3);// 弱全局引用

		public final int type;

		private ref_type(int type)
		{
			this.type = type;
		}
	}

	public static final int JNI_OK = 0;
	public static final int JNI_ERR = -1;
	public static final int JNI_EDETACHED = -2;
	public static final int JNI_EVERSION = -3;
	public static final int JNI_ENOMEM = -4;
	public static final int JNI_EEXIST = -5;
	public static final int JNI_EINVAL = -6;

	public static final int JNI_COMMIT = 1;
	public static final int JNI_ABORT = 2;

	static final void _check_jni_call_ret(int ret)
	{
		switch (ret)
		{
		case JNI_OK:
			return;
		case JNI_ERR:
			throw new java.lang.Error("call JNI failed: unknown error");
		case JNI_EDETACHED:
			throw new java.lang.Error("call JNI failed: thread detached from the VM");
		case JNI_EVERSION:
			throw new java.lang.Error("call JNI failed: JNI version error");
		case JNI_ENOMEM:
			throw new java.lang.Error("call JNI failed: not enough memory");
		case JNI_EEXIST:
			throw new java.lang.Error("call JNI failed: VM already created");
		case JNI_EINVAL:
			throw new java.lang.Error("call JNI failed: invalid arguments");
		default:
			throw new java.lang.Error("call JNI failed: unknown error code " + ret);
		}
	}

	public static final int JNI_VERSION_1_1 = 0x00010001;
	public static final int JNI_VERSION_1_2 = 0x00010002;
	public static final int JNI_VERSION_1_4 = 0x00010004;
	public static final int JNI_VERSION_1_6 = 0x00010006;
	public static final int JNI_VERSION_1_8 = 0x00010008;
	public static final int JNI_VERSION_9 = 0x00090000;
	public static final int JNI_VERSION_10 = 0x000a0000;
	public static final int JNI_VERSION_19 = 0x00130000;
	public static final int JNI_VERSION_20 = 0x00140000;
	public static final int JNI_VERSION_21 = 0x00150000;
	public static final int JNI_VERSION_24 = 0x00180000;

	public static final long _libjvm;

	private static final MethodHandle JNI_GetCreatedJavaVMs;

	private static final MethodHandle JVM_GetCallerClass;

	static
	{
		// 加载JNI，本类所有涉及JVM相关的函数都必须在加载libjvm.so以后才能调用
		_libjvm = shared_object.dlopen("jvm");
		JNI_GetCreatedJavaVMs = shared_object.dlsym(_libjvm, function_signature.of("JNI_GetCreatedJavaVMs", cxx_type.jint, cxx_type.pvoid, cxx_type.jsize, cxx_type.pjsize));
		// JVM内部函数
		JVM_GetCallerClass = shared_object.dlsym(_libjvm, function_signature.of("JVM_GetCallerClass", cxx_type.jclass, cxx_type.pvoid), true);// 要保存线程上下文现场
	}

	/**
	 * JNI_GetCreatedJavaVMs()函数，获取该进程的所有JVM实例。<br>
	 * 一般的JVM实现每个进程只支持一个JVM实例，但此处仍然保留多实例的获取。<br>
	 * 
	 * @param max_num 最多获取多少个指针
	 * @return获取到的是JNIInvokeInterface_*[]，需要再次取引用得到各个JNIInvokeInterface_对象的基地址。
	 */
	public static final long[] JNI_GetCreatedJavaVMs(int max_num)
	{
		// JavaVM** -> JNIInvokeInterface_***
		try (pointer pppJNIInvokeInterface_ = memory.malloc(max_num, cxx_type.pvoid).auto(); pointer vm_num = memory.malloc(cxx_type.jsize).auto();)
		{
			_check_jni_call_ret((int) JNI_GetCreatedJavaVMs.invokeExact(pppJNIInvokeInterface_.address(), max_num, vm_num.address()));
			int final_num = Math.min(max_num, (int) vm_num.dereference());
			// 返回JNIInvokeInterface_**[]，即JavaVM*[]
			return (long[]) pppJNIInvokeInterface_.to_jarray(final_num, long.class);// 仅64位
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNI_GetCreatedJavaVMs() failed", ex);
		}
	}

	public static final long JNI_GetCreatedJavaVMs()
	{
		return JNI_GetCreatedJavaVMs(1)[0];
	}

	public static final JNIInvokeInterface_[] jni_invoke_interfaces(int max_num)
	{
		long[] jvms = JNI_GetCreatedJavaVMs(max_num);
		JNIInvokeInterface_[] interfaces = new JNIInvokeInterface_[jvms.length];
		for (int i = 0; i < jvms.length; ++i)
			interfaces[i] = new JNIInvokeInterface_(jvms[i]);
		return interfaces;
	}

	public static final JNIInvokeInterface_ jni_invoke_interfaces()
	{
		return jni_invoke_interfaces(1)[0];
	}

	public static final JNINativeInterface_ jni_native_interface(JNIInvokeInterface_ java_vm, int jni_version)
	{
		return new JNINativeInterface_(java_vm.GetEnv(jni_version));
	}

	public static final JNINativeInterface_ jni_native_interface(JNIInvokeInterface_ java_vm)
	{
		return new JNINativeInterface_(java_vm.GetEnv());
	}

	public static final JNINativeInterface_ jni_native_interface(int jni_version)
	{
		return jni_native_interface(jni_invoke_interfaces(), jni_version);
	}

	public static final JNINativeInterface_ jni_native_interface()
	{
		return jni_native_interface(jni_invoke_interfaces());
	}

	/**
	 * jdk.internal.reflect.Reflection.getCallerClass()获取调用此方法的上下文的类。<br>
	 * 调用此方法算起，返回栈帧中第一个没有caller_sensitive标记的方法所在的类。<br>
	 * 此方法本身在JVM内部是call_sensitive的。<br>
	 * 调用时的栈帧如下：<br>
	 * [0] [ @CallerSensitive public jdk.internal.reflect.Reflection.getCallerClass() ]<br>
	 * [1] [ @CallerSensitive API.method ] 直接调用Reflection.getCallerClass()的方法，即此方法。<br>
	 * [.] [ (skipped intermediate frames) ] 根据Method*->intrinsic_id()决定是否跳过，例如反射、MethodHandle、lambda就会跳过。<br>
	 * [n] [ caller ]<br>
	 * 此方法无法使用，MethodHandle的栈帧会干扰JVM_GetCallerClass()的判定导致抛出错误
	 * 
	 * @param pJNIEnv
	 * @return
	 */
	@Deprecated
	public static final long JVM_GetCallerClass(long pJNIEnv)
	{
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/prims/jvm.cpp#L724
		// https://github.com/openjdk/jdk/blob/jdk-25%2B36/src/hotspot/share/oops/method.cpp#L1435
		class __init
		{
			private static Class<?> jdk_internal_reflect_Reflection = null;

			static
			{
				try
				{
					jdk_internal_reflect_Reflection = Class.forName("jdk.internal.reflect.Reflection");
				}
				catch (ClassNotFoundException ex)
				{
					ex.printStackTrace();
				}
				// 直接调用Reflection.getCallerClass()的方法必须是call_sensitive的，否则报错
				// MethodHandle::invokeExact()方法在语法层是直接调用Reflection.getCallerClass()的方法，但为其设置caller_sensitive标记仍然报错直接调用方法不是call_sensitive的
				// 因此判断内部还有JVM生成的或内部的中间函数，故必须在字节码层面使用invokeStatic直接调用。

				java_lang_Class.as_InstanceKlass(jdk_internal_reflect_Reflection);
				jvmsp.hotspot.oops.Method _vm_getCallerClass = InstanceKlass.lookup_method(jdk_internal_reflect_Reflection, "getCallerClass", "()Ljava/lang/Class;");
				jvmsp.hotspot.oops.Method _libjvm_JVM_GetCallerClass = InstanceKlass.lookup_method(libjvm.class, "JVM_GetCallerClass", "(J)J");
				jvmsp.hotspot.oops.ConstMethodFlags flags = _libjvm_JVM_GetCallerClass.constMethod().flags();
				flags.set_caller_sensitive(true);// 为get_caller_class()方法设置caller_sensitive标志
				flags.set_intrinsic_candidate(true);// 设置内联建议标志
				_libjvm_JVM_GetCallerClass.set_intrinsic_id(_vm_getCallerClass.intrinsic_id());// 设置内部ID，遍历栈帧时查找caller时就会跳过此栈帧
			}
		}
		try
		{
			unsafe.ensure_class_initialized(__init.class);
			return (long) JVM_GetCallerClass.invokeExact(pJNIEnv);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JVM_GetCallerClass() failed", ex);
		}
	}

}
package jvmsp.libso;

import java.lang.invoke.MethodHandle;

import jvmsp.memory;
import jvmsp.shared_object;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.function_signature;
import jvmsp.type.cxx_type.pointer;
import jvmsp.unsafe;

/**
 * libjvm.so库中的函数封装
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

	private static final long _libjvm;

	private static final MethodHandle JNI_GetCreatedJavaVMs;

	static
	{
		// 加载JNI，本类所有涉及JVM相关的函数都必须在加载libjvm.so以后才能调用
		_libjvm = shared_object.dlopen("jvm");
		JNI_GetCreatedJavaVMs = shared_object.dlsym(_libjvm, function_signature.of("JNI_GetCreatedJavaVMs", cxx_type.jint, cxx_type.pvoid, cxx_type.jsize, cxx_type.pjsize));
	}

	/**
	 * JNI_GetCreatedJavaVMs()函数，获取该进程的所有JVM实例。<br>
	 * 一般的JVM实现每个进程只支持一个JVM实例，但此处仍然保留多实例的获取。<br>
	 * 
	 * @param max_num 最多获取多少个指针
	 * @return获取到的是JNIInvokeInterface_*[]，需要再次取引用得到各个JNIInvokeInterface_对象的基地址。
	 */
	public static final long[] get_created_java_vms(int max_num)
	{
		try (pointer vm_buf = memory.malloc(max_num, cxx_type.pvoid).auto(); pointer vm_num = memory.malloc(cxx_type.jsize).auto();)
		{
			int ret = (int) JNI_GetCreatedJavaVMs.invokeExact(vm_buf.address(), max_num, vm_num.address());
			_check_jni_call_ret(ret);
			int final_num = Math.min(max_num, (int) vm_num.dereference());
			return (long[]) vm_buf.to_jarray(final_num, long.class);//
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNI_GetCreatedJavaVMs() failed", ex);
		}
	}

	public static final long get_created_java_vms()
	{
		return get_created_java_vms(1)[0];
	}

	public static final jni_invoke_interface[] jni_invoke_interfaces(int max_num)
	{
		long[] jvms = get_created_java_vms(max_num);
		jni_invoke_interface[] interfaces = new jni_invoke_interface[jvms.length];
		for (int i = 0; i < jvms.length; ++i)
			interfaces[i] = new jni_invoke_interface(unsafe.get_pointed_base(jvms[i]));
		return interfaces;
	}

	public static final jni_invoke_interface jni_invoke_interfaces()
	{
		return jni_invoke_interfaces(1)[0];
	}

	public static final jni_native_interface jni_native_interface(jni_invoke_interface ii, int jni_version)
	{
		return new jni_native_interface(unsafe.get_pointed_base(ii.get_env(jni_version)));
	}

	public static final jni_native_interface jni_native_interface(jni_invoke_interface ii)
	{
		return new jni_native_interface(unsafe.get_pointed_base(ii.get_env()));
	}

	public static final jni_native_interface jni_native_interface(int jni_version)
	{
		return jni_native_interface(jni_invoke_interfaces(), jni_version);
	}

	public static final jni_native_interface jni_native_interface()
	{
		return jni_native_interface(jni_invoke_interfaces());
	}
}
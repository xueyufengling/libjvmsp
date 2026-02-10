package jvmsp.libso;

import java.lang.invoke.MethodHandle;

import jvmsp.memory;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.function_pointer_type;
import jvmsp.type.cxx_type.pointer;

/**
 * JNIInvokeInterface_的函数封装
 */
public class jni_invoke_interface
{
	public static final cxx_type JNIInvokeInterface_ = cxx_type.define("JNIInvokeInterface_")
			.decl_field("reserved0", cxx_type.pvoid)
			.decl_field("reserved1", cxx_type.pvoid)
			.decl_field("reserved2", cxx_type.pvoid)
			.decl_field("DestroyJavaVM", function_pointer_type.of(cxx_type.jint, cxx_type.pvoid))
			.decl_field("AttachCurrentThread", function_pointer_type.of(cxx_type.jint, cxx_type.pvoid, cxx_type.pvoid, cxx_type.pvoid))
			.decl_field("DetachCurrentThread", function_pointer_type.of(cxx_type.jint, cxx_type.pvoid))
			.decl_field("GetEnv", function_pointer_type.of(cxx_type.jint, cxx_type.pvoid, cxx_type.pvoid, cxx_type.jint))
			.decl_field("AttachCurrentThreadAsDaemon", function_pointer_type.of(cxx_type.jint, cxx_type.pvoid, cxx_type.pvoid, cxx_type.pvoid))
			.resolve();

	private final cxx_type.object JNIInvokeInterface_base;

	private final MethodHandle DestroyJavaVM;
	private final MethodHandle AttachCurrentThread;
	private final MethodHandle DetachCurrentThread;
	private final MethodHandle GetEnv;
	private final MethodHandle AttachCurrentThreadAsDaemon;

	/**
	 * 创建一个JNI调用接口
	 * 
	 * @param JNIInvokeInterface_addr JNIInvokeInterface_的基地址
	 */
	public jni_invoke_interface(long JNIInvokeInterface_addr)
	{
		this.JNIInvokeInterface_base = JNIInvokeInterface_.new object(JNIInvokeInterface_addr);
		this.DestroyJavaVM = JNIInvokeInterface_base.callable("DestroyJavaVM");
		this.AttachCurrentThread = JNIInvokeInterface_base.callable("AttachCurrentThread");
		this.DetachCurrentThread = JNIInvokeInterface_base.callable("DetachCurrentThread");
		this.GetEnv = JNIInvokeInterface_base.callable("GetEnv");
		this.AttachCurrentThreadAsDaemon = JNIInvokeInterface_base.callable("AttachCurrentThreadAsDaemon");
	}

	/**
	 * 摧毁该JVM
	 */
	public final void destroy_java_vm()
	{
		try
		{
			int ret = (int) DestroyJavaVM.invokeExact(JNIInvokeInterface_base.address());
			libjvm._check_jni_call_ret(ret);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNIInvokeInterface_::DestroyJavaVM() failed", ex);
		}
		System.out.println("destroyed");
	}

	public final void detach_current_thread()
	{
		try
		{
			int ret = (int) DetachCurrentThread.invokeExact(JNIInvokeInterface_base.address());
			libjvm._check_jni_call_ret(ret);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNIInvokeInterface_::DetachCurrentThread() failed", ex);
		}
	}

	/**
	 * 获取JNINativeInterface_的指针
	 * 
	 * @param jni_version 当前的JNI版本，如果不知道则可以最低传入JNI_VERSION_1_1
	 * @return
	 */
	public final long get_env(int jni_version)
	{
		try (pointer penv = memory.malloc(cxx_type.pvoid).auto();)
		{
			int ret = (int) GetEnv.invokeExact(JNIInvokeInterface_base.address(), penv.address(), jni_version);
			libjvm._check_jni_call_ret(ret);
			return (long) penv.dereference();
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNIInvokeInterface_::GetEnv() failed", ex);
		}
	}

	public final long get_env()
	{
		return get_env(libjvm.JNI_VERSION_1_1);
	}

}
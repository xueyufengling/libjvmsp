package jvmsp.libjvm;

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

	private final cxx_type.object JNIInvokeInterface_instance;

	private final MethodHandle JNIInvokeInterface_DestroyJavaVM;
	private final MethodHandle JNIInvokeInterface_AttachCurrentThread;
	private final MethodHandle JNIInvokeInterface_DetachCurrentThread;
	private final MethodHandle JNIInvokeInterface_GetEnv;
	private final MethodHandle JNIInvokeInterface_AttachCurrentThreadAsDaemon;

	/**
	 * 创建一个JNI调用接口
	 * 
	 * @param JNIInvokeInterface_addr JNIInvokeInterface_的基地址
	 */
	public jni_invoke_interface(long JNIInvokeInterface_addr)
	{
		this.JNIInvokeInterface_instance = JNIInvokeInterface_.new object(JNIInvokeInterface_addr);
		this.JNIInvokeInterface_DestroyJavaVM = JNIInvokeInterface_instance.callable("DestroyJavaVM");
		this.JNIInvokeInterface_AttachCurrentThread = JNIInvokeInterface_instance.callable("AttachCurrentThread");
		this.JNIInvokeInterface_DetachCurrentThread = JNIInvokeInterface_instance.callable("DetachCurrentThread");
		this.JNIInvokeInterface_GetEnv = JNIInvokeInterface_instance.callable("GetEnv");
		this.JNIInvokeInterface_AttachCurrentThreadAsDaemon = JNIInvokeInterface_instance.callable("AttachCurrentThreadAsDaemon");
	}

	/**
	 * 摧毁该JVM
	 */
	public final void destroy_java_vm()
	{
		try
		{
			int ret = (int) JNIInvokeInterface_DestroyJavaVM.invokeExact(JNIInvokeInterface_instance.address());
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
			int ret = (int) JNIInvokeInterface_DetachCurrentThread.invokeExact(JNIInvokeInterface_instance.address());
			libjvm._check_jni_call_ret(ret);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNIInvokeInterface_::DetachCurrentThread() failed", ex);
		}
	}

	public final long get_env(int jni_version)
	{
		try (pointer penv = memory.malloc(cxx_type.pvoid).auto();)
		{
			int ret = (int) JNIInvokeInterface_GetEnv.invokeExact(JNIInvokeInterface_instance.address(), penv.address(), jni_version);
			libjvm._check_jni_call_ret(ret);
			return (long) penv.dereference();
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNIInvokeInterface_::GetEnv() failed", ex);
		}
	}
}
package jvmsp.libso;

import java.lang.invoke.MethodHandle;

import jvmsp.memory;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.function_pointer_type;
import jvmsp.type.cxx_type.pointer;
import jvmsp.unsafe;

/**
 * JNIInvokeInterface_的函数封装
 */
public class JNIInvokeInterface_
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
	private final long ppJNIInvokeInterface;

	private final MethodHandle DestroyJavaVM;
	private final MethodHandle AttachCurrentThread;
	private final MethodHandle DetachCurrentThread;
	private final MethodHandle GetEnv;
	private final MethodHandle AttachCurrentThreadAsDaemon;

	/**
	 * 创建一个JNI调用接口
	 * 
	 * @param pJNIInvokeInterface JNIInvokeInterface_的基地址
	 */
	public JNIInvokeInterface_(long ppJNIInvokeInterface)
	{
		this.JNIInvokeInterface_base = JNIInvokeInterface_.new object(unsafe.read_ptr(ppJNIInvokeInterface));
		this.ppJNIInvokeInterface = ppJNIInvokeInterface;
		this.DestroyJavaVM = JNIInvokeInterface_base.callable("DestroyJavaVM");
		this.AttachCurrentThread = JNIInvokeInterface_base.callable("AttachCurrentThread");
		this.DetachCurrentThread = JNIInvokeInterface_base.callable("DetachCurrentThread");
		this.GetEnv = JNIInvokeInterface_base.callable("GetEnv");
		this.AttachCurrentThreadAsDaemon = JNIInvokeInterface_base.callable("AttachCurrentThreadAsDaemon");
	}

	public final long pJavaVM()
	{
		return ppJNIInvokeInterface;
	}

	/**
	 * 摧毁该JVM
	 */
	public final void DestroyJavaVM()
	{
		try
		{
			int ret = (int) DestroyJavaVM.invokeExact(pJavaVM());
			libjvm._check_jni_call_ret(ret);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNIInvokeInterface_::DestroyJavaVM() failed", ex);
		}
		System.out.println("destroyed");
	}

	public final void DetachCurrentThread()
	{
		try
		{
			int ret = (int) DetachCurrentThread.invokeExact(pJavaVM());
			libjvm._check_jni_call_ret(ret);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNIInvokeInterface_::DetachCurrentThread() failed", ex);
		}
	}

	/**
	 * 获取JNINativeInterface_的二级指针，即JNIEnv*
	 * 
	 * @param jni_version 当前的JNI版本，如果不知道则可以最低传入JNI_VERSION_1_1
	 * @return
	 */
	public final long GetEnv(int jni_version)
	{
		try (pointer penv = memory.malloc(cxx_type.pvoid).auto();)
		{

			// jint (JNICALL *GetEnv)(JavaVM *vm, void **penv, jint version);
			// 获取对应jni_version的JNINativeInterface_** penv，即JNIEnv*
			libjvm._check_jni_call_ret((int) GetEnv.invokeExact(pJavaVM(), penv.address(), jni_version));
			return (long) penv.dereference();
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNIInvokeInterface_::GetEnv() failed", ex);
		}
	}

	public final long GetEnv()
	{
		return GetEnv(libjvm.JNI_VERSION_1_1);
	}

}
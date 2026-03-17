package jvmsp.libso;

import jvmsp.unsafe;
import jvmsp.type.cxx_type;
import jvmsp.type.java_type;

/**
 * JNI层的OOP包装
 */
public class jni_handles
{
	/**
	 * 为oop创建一个local的handle，等价于JNIHandles::make_local(oop)。<br>
	 * local引用本质上是个指向oop的指针。
	 * 
	 * @param oop 要创建local引用的oop
	 * @return oop对应的handle，不需要的时候必须通过free()销毁
	 */
	public static final long make_local(long oop)
	{
		long handle = unsafe.malloc(cxx_type.pvoid.size());
		unsafe.write(handle, oop);
		return handle;
	}

	public static final long make_local(Object object)
	{
		return make_local(java_type.oop_of(object));
	}

	public static final long resolve_oop_from_local_handle(long local_handle)
	{
		return unsafe.read_long(null, local_handle);
	}

	public static final Object resolve_object_from_local_handle(long local_handle)
	{
		return java_type.object_from_oop(resolve_oop_from_local_handle(local_handle));
	}
}

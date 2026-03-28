package jvmsp.hotspot.memory;

/**
 * 引用类型
 */
public abstract class ReferenceType
{
	/**
	 * 通常的类
	 */
	public static final byte REF_NONE = 0;

	/**
	 * java.lang.ref.SoftReference的子类
	 */
	public static final byte REF_SOFT = 1;

	/**
	 * java.lang.ref.WeakReference的子类
	 */
	public static final byte REF_WEAK = 2;

	/**
	 * java.lang.ref.FinalReference的子类
	 */
	public static final byte REF_FINAL = 3;

	/**
	 * java.lang.ref.PhantomReference的子类
	 */
	public static final byte REF_PHANTOM = 4;
}

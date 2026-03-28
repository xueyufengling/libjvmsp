package jvmsp.hotspot.oops;

public abstract class ClassState
{
	/**
	 * 已分配内存，但未链接
	 */
	public static final byte allocated = 0;

	/**
	 * 已加载且插入了继承链，但未链接
	 */
	public static final byte loaded = 1;

	/**
	 * 链接、验证成功，但未初始化
	 */
	public static final byte linked = 2;

	/**
	 * 正在执行类初始化
	 */
	public static final byte being_initialized = 3;

	/**
	 * 初始化完成
	 */
	public static final byte fully_initialized = 4;

	/**
	 * 初始化出错
	 */
	public static final byte initialization_error = 5;
}

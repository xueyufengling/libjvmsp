package jvmsp;

/**
 * 镜像类，使用无法访问的类时将镜像类用做占位符。镜像类对象可以转换成目标类对象。<br>
 * 需要自己保证镜像类和目标类的内存布局相同。
 * 
 * @param <M> 镜像类
 * @param <D> 目标类
 */
public interface Mirror<M, D> extends _crtp<M> {
	/**
	 * 转换为目标对象
	 * 
	 * @return
	 */
	public default D cast() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public default D cast(M destTypeObj) {
		return (D) ObjectManipulator.cast(this, destTypeObj);
	}

	@SuppressWarnings("unchecked")
	public static <T> T cast(Object target, T destTypeObj) {
		return (T) ObjectManipulator.cast(target, destTypeObj);
	}
}

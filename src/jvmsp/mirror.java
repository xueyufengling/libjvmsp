package jvmsp;

/**
 * 镜像类，使用无法访问的类时将镜像类用做占位符。镜像类对象可以转换成目标类对象。<br>
 * 需要自己保证镜像类和目标类的内存布局相同。
 * 
 * @param <_Mirror> 镜像类
 * @param <_Refl>   目标类
 */
public interface mirror<_Mirror, _Refl> extends _crtp<_Mirror> {
	/**
	 * 转换为目标对象
	 * 
	 * @return
	 */
	public default _Refl cast() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public default _Refl cast(_Mirror destTypeObj) {
		return (_Refl) oops.cast(this, destTypeObj);
	}

	@SuppressWarnings("unchecked")
	public static <_T> _T cast(Object target, _T destTypeObj) {
		return (_T) oops.cast(target, destTypeObj);
	}
}

package jvmsp;

/**
 * 在父类中使用子类Class<?>对象
 * 
 * @param <_Derived>
 */
public interface _crtp<_Derived> {
	@SuppressWarnings("unchecked")
	public default Class<_Derived> derived_class() {
		return (Class<_Derived>) this.getClass();
	}
}

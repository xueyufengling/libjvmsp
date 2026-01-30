package jvmsp;

/**
 * 在父类中使用子类Class<?>对象
 * 
 * @param <Derived>
 */
public interface _crtp<Derived> {
	@SuppressWarnings("unchecked")
	public default Class<Derived> getDerivedClass() {
		return (Class<Derived>) this.getClass();
	}
}

package jvmsp;

public final class placeholders {

	@SuppressWarnings("unchecked")
	public static final <T> T undefined(long castTypeKlassWord) {
		return (T) ObjectManipulator.cast(new Object(), castTypeKlassWord);
	}

	/**
	 * 用于作为Object类型的static final变量初始值，防止变量字面值或null值被内联
	 * 
	 * @param <T>
	 * @param klass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T undefined(T destTypeObj) {
		return (T) ObjectManipulator.cast(new Object(), destTypeObj);
	}

	@SuppressWarnings("unchecked")
	public static final <T> T undefined(Object obj, Class<?> castType) {
		return (T) ObjectManipulator.cast(new Object(), castType);
	}

	@SuppressWarnings("unchecked")
	public static final <T> T undefined(Object obj, String castType) {
		return (T) ObjectManipulator.cast(new Object(), castType);
	}

	/**
	 * 防止Object类型的static final变量初始null字面值被内联<br>
	 * 当跨类修改目标类字段，且static final Object被初始化为null字面值时：如果不在修改之前在本类使用这个变量，那么这个值的修改就不会成功（会被内联）。
	 * 
	 * @param var
	 * @return
	 */
	public static final void not_inlined(Object var) {

	}

	/**
	 * 任何枚举类型的占位符
	 */
	public static enum any_enum {
		Null;

		/**
		 * 将该占位符转换为实际的枚举类型值
		 * 
		 * @param <T>
		 * @param targetClass
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public final <T extends Enum<T>> T as(Class<T> targetClass) {
			return (T) ObjectManipulator.cast(this, targetClass);
		}

		/**
		 * 将一个枚举类型值包装为占位符
		 * 
		 * @param <T>
		 * @param enumeration
		 * @return
		 */
		public static final <T extends Enum<T>> any_enum pack(T enumeration) {
			return (any_enum) ObjectManipulator.cast(enumeration, any_enum.class);
		}
	}

	/**
	 * 用于lambda表达式内更改表达式外的局部变量值
	 * 
	 * @param <T>
	 */
	public static class type_wrapper<T> {
		public T value;

		public type_wrapper(T value) {
			this.value = value;
		}

		public static <T> type_wrapper<T> wrap(T value) {
			return new type_wrapper<T>(value);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static type_wrapper wrap() {
			return new type_wrapper(null);
		}
	}
}

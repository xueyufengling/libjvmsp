package jvmsp;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * 多重继承解决方案<br>
 * 将所有接口所需字段定义到接口的内部类，这个内部类实例可以绑定到一个接口对象上，接口通过definition()方法获取字段内部类实例。
 * 
 * @param <Def>
 */
public interface BaseClass<Def extends BaseClass.Definition<? extends BaseClass<?>>> {
	/**
	 * 接口内部类都是隐式public static嵌套类<br>
	 * 接口字段定义父类
	 * 
	 * @param <Derived>
	 */
	abstract class Definition<Derived extends BaseClass<? extends Definition<?>>> {
		/**
		 * (derived_obj, base_type)->base_type_definition
		 */
		private static final HashMap<BaseClass<?>, HashMap<Class<?>, Object>> definitions = new HashMap<>();

		public final Derived this_;

		private static Field this_field;

		/**
		 * 初始化对象时使用
		 */
		private static final HashMap<Definition<?>, BaseClass<?>> this_preinit_refs = new HashMap<>();

		static {
			this_field = reflection.get_declared_field(Definition.class, "this_");
		}

		@SuppressWarnings("unchecked")
		protected Definition() {
			this_ = (Derived) this_preinit_refs.remove(this);// 在执行子类构造函数之前先设置好子类的this_引用
		}

		/**
		 * 将本实例绑定在一个BaseClass对象上
		 * 
		 * @param obj
		 * @return
		 */
		@SuppressWarnings({ "rawtypes" })
		public final Definition move(Derived obj) {
			Class<?> base_type = this.getClass();
			if (this_ != null) {
				HashMap<Class<?>, Object> orig_base_defs = Definition.definitions.computeIfAbsent(this_, (BaseClass) -> new HashMap<>());
				orig_base_defs.remove(base_type);// 从原绑定对象移除该实例
			}
			ObjectManipulator.setObject(this, this_field, obj);
			HashMap<Class<?>, Object> new_base_defs = Definition.definitions.computeIfAbsent(obj, (BaseClass) -> new HashMap<>());
			new_base_defs.put(base_type, this);// 将指定基类定义加入Map
			return this;
		}
	}

	default Def construct(Class<Def> base_type, Class<?>[] arg_types, Object... args) {
		Def def = unsafe.allocate(base_type);// 先分配对象内存
		Definition.this_preinit_refs.put(def, this);// 为目标对象指定this_引用
		try {
			def = jobject.placement_new(def, arg_types, args);// 调用目标构造函数
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		Definition.definitions.computeIfAbsent(this, (BaseClass) -> new HashMap<>()).put(base_type, def);
		return def;
	}

	@SuppressWarnings("unchecked")
	default Def construct(Object base_type, Class<?>[] arg_types, Object... args) {
		return construct((Class<Def>) base_type, arg_types, args);
	}

	/**
	 * 子类需要有正确的Definition对象
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default Def definition(Class<?> type) {
		return (Def) Definition.definitions.get(this).get(type);
	}

	@SuppressWarnings("unchecked")
	default Def definition() {
		HashMap<Class<?>, Object> base_defs = Definition.definitions.get(this);
		if (base_defs.size() == 1)
			return (Def) base_defs.values().iterator().next();// 只继承了一个BaseClass则直接返回其字段定义实例
		else
			throw new IllegalArgumentException("Class " + this.getClass() + " have multipe base class, specify the target base class type.");
	}
}

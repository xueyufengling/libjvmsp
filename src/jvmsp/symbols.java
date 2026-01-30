package jvmsp;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import jvmsp.internal.reflection_factory;

/**
 * 句柄操作相关，包括调用native方法<br>
 * 不依赖<br>
 * 
 * @implNote 该类未引用任何本库的类，需要最先初始化
 */
public class symbols {
	public static final int PUBLIC = Modifier.PUBLIC;
	public static final int PRIVATE = Modifier.PRIVATE;
	public static final int PROTECTED = Modifier.PROTECTED;
	public static final int PACKAGE = Modifier.STATIC;
	public static final int MODULE = PACKAGE << 1;
	public static final int UNCONDITIONAL = PACKAGE << 2;
	public static final int ORIGINAL = PACKAGE << 3;

	public static final int ALL_MODES = (PUBLIC | PRIVATE | PROTECTED | PACKAGE | MODULE | UNCONDITIONAL | ORIGINAL);
	public static final int FULL_POWER_MODES = (ALL_MODES & ~UNCONDITIONAL);

	public static final int TRUSTED = -1;

	static final MethodHandles.Lookup TRUSTED_LOOKUP;

	static {
		TRUSTED_LOOKUP = allocate_trusted_lookup();
	}

	/**
	 * Handle调用时try-catch外返回值，这些值永远不会被返回，但出于语法需要必须有一个返回值。
	 */
	public static final byte UNREACHABLE_BYTE = -1;
	public static final char UNREACHABLE_CHAR = 0;
	public static final short UNREACHABLE_SHORT = -1;
	public static final long UNREACHABLE_LONG = -1;
	public static final Object UNREACHABLE_REFERENCE = null;
	public static final boolean UNREACHABLE_BOOLEAN = false;
	public static final int UNREACHABLE_INT = -1;
	public static final double UNREACHABLE_DOUBLE = -1;
	public static final float UNREACHABLE_FLOAT = -1;

	/**
	 * 使用ReflectionFactory的反序列化调用Lookup的构造函数新构建一个Lookup对象。<br>
	 * 
	 * @param lookup_class
	 * @param prev_lookup_class
	 * @param allowed_modes
	 * @return
	 */
	public static final MethodHandles.Lookup allocate_lookup(Class<?> lookup_class, Class<?> prev_lookup_class, int allowed_modes) {
		MethodHandles.Lookup lookup = null;
		try {
			lookup = reflection_factory.construct(MethodHandles.Lookup.class, MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class), lookup_class, prev_lookup_class, allowed_modes);
		} catch (IllegalArgumentException | NoSuchMethodException | SecurityException ex) {
			ex.printStackTrace();
		}
		return lookup;
	}

	/**
	 * 构造一个TRUSTED的Lookup
	 * 
	 * @return
	 */
	public static final MethodHandles.Lookup allocate_trusted_lookup() {
		return allocate_lookup(Object.class, null, TRUSTED);
	}

	/**
	 * 使用ReflectionFactory的反序列化调用Lookup的构造函数新构建一个Lookup对象。<br>
	 * 
	 * @return
	 */
	public static final MethodHandles.Lookup allocate_lookup(Class<?> lookup_class) {
		MethodHandles.Lookup lookup = null;
		try {
			lookup = reflection_factory.construct(MethodHandles.Lookup.class, MethodHandles.Lookup.class.getDeclaredConstructor(Class.class), lookup_class);
		} catch (IllegalArgumentException | NoSuchMethodException | SecurityException ex) {
			ex.printStackTrace();
		}
		return lookup;
	}

	public static final MethodHandles.Lookup allocate_lookup() {
		return allocate_lookup(Object.class);
	}

	/**
	 * 用于查找任何字段
	 * 
	 * @param clazz
	 * @param field_name
	 * @param type
	 * @return
	 */
	public static VarHandle find_var(Class<?> clazz, String field_name, Class<?> type) {
		VarHandle v = null;
		try {
			v = MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP).findVarHandle(clazz, field_name, type);
		} catch (IllegalAccessException | NoSuchFieldException ex) {
			ex.printStackTrace();
		}
		return v;
	}

	public static VarHandle find_static_var(Class<?> clazz, String field_name, Class<?> type) {
		VarHandle v = null;
		try {
			v = MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP).findStaticVarHandle(clazz, field_name, type);
		} catch (IllegalAccessException | NoSuchFieldException ex) {
			ex.printStackTrace();
		}
		return v;
	}

	/**
	 * 查找构造函数
	 * 
	 * @param clazz
	 * @param field_name
	 * @param jtype
	 * @return
	 */
	public static MethodHandle find_constructor(Class<?> clazz, Class<?>... arg_types) {
		MethodHandle m = null;
		try {
			m = MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP).findConstructor(clazz, MethodType.methodType(void.class, arg_types));
		} catch (IllegalAccessException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
		return m;
	}

	public static MethodHandle find_initializer(Class<?> clazz) {
		MethodHandle m = null;
		try {
			m = MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP).findStatic(clazz, INITIALIZER_NAME, MethodType.methodType(void.class, new Class<?>[0]));
		} catch (IllegalAccessException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
		return m;
	}

	/**
	 * 查找任意字节码行为的方法句柄(包括native)，从search_chain_start_subclazz开始查找，如果该类不存在方法则一直向上查找方法，直到在指定的超类search_chain_end_superclazz中也找不到方法时终止并抛出错误
	 * 句柄等价于Unsafe查找到的offset与base的组合，明确指定了一个内存中的方法地址
	 * 
	 * @param search_chain_start_subclazz 查找链起始类，也是要查找的对象，必须是search_chain_end_superclazz的子类
	 * @param search_chain_end_superclazz 查找链终止类
	 * @param type                        方法类型，包含返回值和参数类型
	 * @return 查找到的方法句柄
	 */
	public static MethodHandle find_special_method(Class<?> search_chain_start_subclazz, Class<?> search_chain_end_superclazz, String method_name, MethodType type) {
		MethodHandle m = null;
		try {
			m = MethodHandles.privateLookupIn(search_chain_start_subclazz, TRUSTED_LOOKUP).findSpecial(search_chain_end_superclazz, method_name, type, search_chain_start_subclazz);
		} catch (IllegalAccessException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
		return m;
	}

	public static MethodHandle find_special_method(Class<?> search_clazz, String method_name, MethodType type) {
		return find_special_method(search_clazz, search_clazz, method_name, type);
	}

	public static MethodHandle find_special_method(Class<?> search_chain_start_subclazz, Class<?> search_chain_end_superclazz, String method_name, Class<?> return_type, Class<?>... arg_types) {
		return find_special_method(search_chain_start_subclazz, search_chain_end_superclazz, method_name, MethodType.methodType(return_type, arg_types));
	}

	public static MethodHandle find_special_method(Class<?> search_clazz, String method_name, Class<?> return_type, Class<?>... arg_types) {
		return find_special_method(search_clazz, search_clazz, method_name, return_type, arg_types);
	}

	public static MethodHandle find_virtual_method(Class<?> clazz, String method_name, MethodType type) {
		MethodHandle m = null;
		try {
			m = MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP).findVirtual(clazz, method_name, type);
		} catch (IllegalAccessException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
		return m;
	}

	public static MethodHandle find_virtual_method(Class<?> clazz, String method_name, Class<?> return_type, Class<?>... arg_types) {
		return find_virtual_method(clazz, method_name, MethodType.methodType(return_type, arg_types));
	}

	/**
	 * 查找静态函数的方法句柄
	 * 
	 * @param clazz
	 * @param method_name
	 * @param type
	 * @return
	 */
	public static MethodHandle find_static_method(Class<?> clazz, String method_name, MethodType type) {
		MethodHandle m = null;
		try {
			m = MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP).findStatic(clazz, method_name, type);
		} catch (IllegalAccessException | NoSuchMethodException ex) {
			ex.printStackTrace();
		}
		return m;
	}

	public static MethodHandle find_static_method(Class<?> clazz, String method_name, Class<?> return_type, Class<?>... arg_types) {
		return find_static_method(clazz, method_name, MethodType.methodType(return_type, arg_types));
	}

	/**
	 * 使用反射获取目标信息并查找对应的方法句柄，仅支持查找本类的方法和构造函数。
	 * 
	 * @param clazz
	 * @param method_name
	 * @param return_type
	 * @param arg_types
	 * @return
	 */
	public static MethodHandle find_method(Class<?> clazz, String method_name, Class<?>... arg_types) {
		MethodHandle m = null;
		if (method_name.equals(CONSTRUCTOR_NAME))
			m = find_constructor(clazz, arg_types);
		else {
			Method rm = reflection.get_declared_method(clazz, method_name, arg_types);
			if (Modifier.isStatic(rm.getModifiers()))
				m = symbols.find_static_method(clazz, method_name, rm.getReturnType(), arg_types);
			else
				m = symbols.find_virtual_method(clazz, method_name, rm.getReturnType(), arg_types);
		}
		return m;
	}

	/**
	 * 调用method方法，自动打包对象和参数
	 * 
	 * @param method
	 * @param obj
	 * @param args
	 * @return
	 */
	public static Object invoke(MethodHandle method, Object obj, Object... args) {
		Object[] wrapped_args = new Object[args.length + 1];
		wrapped_args[0] = obj;
		System.arraycopy(args, 0, wrapped_args, 1, args.length);
		try {
			return method.invokeWithArguments(wrapped_args);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_REFERENCE;
	}

	public static Object read(Object obj, String field_name, Class<?> type) {
		return find_var(obj.getClass(), field_name, type).get(obj);
	}

	public static Object read_static(Class<?> cls, String field_name, Class<?> type) {
		return find_static_var(cls, field_name, type).get();
	}

	public static void write(Object obj, String field_name, Class<?> type, Object value) {
		find_var(obj.getClass(), field_name, type).set(obj, value);
	}

	public static void write_static(Class<?> cls, String field_name, Class<?> type, Object value) {
		find_static_var(cls, field_name, type).set(value);
	}

	/**
	 * VM底层相关信息
	 */

	static Class<?> class_java_lang_invoke_MethodHandleNatives;

	static {
		try {
			class_java_lang_invoke_MethodHandleNatives = Class.forName("java.lang.invoke.MethodHandleNatives");
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * java.lang.invoke.MethodHandleNatives.Constants定义的常数<br>
	 * 主要用于MemberName
	 */
	public static final class constants {

		static Class<?> class_java_lang_invoke_MethodHandleNatives_Constants;

		public static final int MN_IS_METHOD, // method (not constructor)
				MN_IS_CONSTRUCTOR, // constructor
				MN_IS_FIELD, // field
				MN_IS_TYPE, // nested type
				MN_CALLER_SENSITIVE, // @CallerSensitive annotation detected
				MN_TRUSTED_FINAL, // trusted final field
				MN_REFERENCE_KIND_SHIFT, // refKind
				MN_REFERENCE_KIND_MASK;

		/**
		 * Constant pool reference-kind codes, as used by CONSTANT_MethodHandle CP entries.
		 */
		public static final byte REF_NONE, // null value
				REF_getField,
				REF_getStatic,
				REF_putField,
				REF_putStatic,
				REF_invokeVirtual,
				REF_invokeStatic,
				REF_invokeSpecial,
				REF_newInvokeSpecial,
				REF_invokeInterface,
				REF_LIMIT;

		/**
		 * Flags for Lookup.ClassOptions
		 */
		public static final int NESTMATE_CLASS,
				HIDDEN_CLASS,
				STRONG_LOADER_LINK,
				ACCESS_VM_ANNOTATIONS;

		/**
		 * Lookup modes
		 */
		public static final int LM_MODULE,
				LM_UNCONDITIONAL,
				LM_TRUSTED;

		static {
			try {
				class_java_lang_invoke_MethodHandleNatives_Constants = Class.forName("java.lang.invoke.MethodHandleNatives$Constants");
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
			MN_IS_METHOD = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "MN_IS_METHOD", int.class);
			MN_IS_CONSTRUCTOR = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "MN_IS_CONSTRUCTOR", int.class);
			MN_IS_FIELD = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "MN_IS_FIELD", int.class);
			MN_IS_TYPE = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "MN_IS_TYPE", int.class);
			MN_CALLER_SENSITIVE = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "MN_CALLER_SENSITIVE", int.class);
			MN_TRUSTED_FINAL = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "MN_TRUSTED_FINAL", int.class);
			MN_REFERENCE_KIND_SHIFT = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "MN_REFERENCE_KIND_SHIFT", int.class);
			MN_REFERENCE_KIND_MASK = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "MN_REFERENCE_KIND_MASK", int.class);

			REF_NONE = (byte) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "REF_NONE", byte.class);
			REF_getField = (byte) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "REF_getField", byte.class);
			REF_getStatic = (byte) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "REF_getStatic", byte.class);
			REF_putField = (byte) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "REF_putField", byte.class);
			REF_putStatic = (byte) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "REF_putStatic", byte.class);
			REF_invokeVirtual = (byte) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "REF_invokeVirtual", byte.class);
			REF_invokeStatic = (byte) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "REF_invokeStatic", byte.class);
			REF_invokeSpecial = (byte) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "REF_invokeSpecial", byte.class);
			REF_newInvokeSpecial = (byte) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "REF_newInvokeSpecial", byte.class);
			REF_invokeInterface = (byte) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "REF_invokeInterface", byte.class);
			REF_LIMIT = (byte) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "REF_LIMIT", byte.class);

			NESTMATE_CLASS = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "NESTMATE_CLASS", int.class);
			HIDDEN_CLASS = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "HIDDEN_CLASS", int.class);
			STRONG_LOADER_LINK = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "STRONG_LOADER_LINK", int.class);
			ACCESS_VM_ANNOTATIONS = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "ACCESS_VM_ANNOTATIONS", int.class);

			LM_MODULE = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "LM_MODULE", int.class);
			LM_UNCONDITIONAL = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "LM_UNCONDITIONAL", int.class);
			LM_TRUSTED = (int) read_static(class_java_lang_invoke_MethodHandleNatives_Constants, "LM_TRUSTED", int.class);
		}
	}

	private static Class<?> class_java_lang_invoke_MemberName;
	private static MethodHandle getMemberVMInfo;

	static {
		try {
			class_java_lang_invoke_MemberName = Class.forName("java.lang.invoke.MemberName");
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		getMemberVMInfo = symbols.find_static_method(class_java_lang_invoke_MethodHandleNatives, "getMemberVMInfo", Object.class, class_java_lang_invoke_MemberName);
	}

	/**
	 * 目标成员字段或方法的信息。<br>
	 */
	public static final class vm_info {
		/**
		 * 成员的偏移量，相对于class或interface起始。<br>
		 * {@link https://github.com/openjdk/jdk/blob/3ff83ec49e561c44dd99508364b8ba068274b63a/src/hotspot/share/classfile/javaClasses.hpp#L1310}
		 */
		public final long offset;

		/**
		 * 目标成员字段或方法的信息。<br>
		 * 若是字段，则target_ptr为字段所属类型Class<?> oop。<br>
		 * 若是方法，则target_ptr为其在C++层的Method*指针。根据Method*可以进一步获取constMethod*<br>
		 */
		public final Object target_ptr;

		private vm_info(long vmindex, Object vmtarget) {
			this.offset = vmindex;
			this.target_ptr = vmtarget;
		}

		/**
		 * 获取InstanceMirrorKlass。<br>
		 * 这个对象实际就是staticFieldBase().<br>
		 * 
		 * @return
		 */
		public final pointer instance_mirror_klass() {
			return pointer.address_of(target_ptr).cast(Class.class);
		}

		public final pointer method() {
			return pointer.address_of(target_ptr).cast(byte.class);
		}
	}

	/**
	 * 获取成员的底层信息
	 * 
	 * @param memberName
	 * @return
	 */
	public static vm_info get_vm_info(Object memberName) {
		Object[] vminfo = null;
		try {
			vminfo = (Object[]) getMemberVMInfo.invoke(memberName);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return new vm_info((Long) vminfo[0], vminfo[1]);
	}

	public static vm_info get_vm_info(Class<?> target, String method_name, Class<?>... arg_types) {
		Object memberName = member_name(symbols.find_method(target, method_name, arg_types));
		return get_vm_info(memberName);
	}

	/**
	 * java.lang.invoke.MemberName缓存MethodHandle的相关metadata。<br>
	 * JVM的MethodHandle执行检查依赖于该类，修改目标MethodHandle的MemberName可以绕过检查。
	 */

	private static MethodHandle matchingFlagsSet;
	private static MethodHandle allFlagsSet;
	private static MethodHandle anyFlagSet;

	// unofficial modifier flags, used by HotSpot:
	public static final int BRIDGE;
	public static final int VARARGS;
	public static final int SYNTHETIC;
	public static final int ANNOTATION;
	public static final int ENUM;

	private static MethodHandle isBridge;
	private static MethodHandle isVarargs;
	private static MethodHandle isSynthetic;

	public static final String INITIALIZER_NAME = "<cinit>";
	public static final String CONSTRUCTOR_NAME; // the ever-popular

	// modifiers exported by the JVM:
	public static final int RECOGNIZED_MODIFIERS;

	// private flags, not part of RECOGNIZED_MODIFIERS:
	public static final int IS_METHOD, // method (not constructor)
			IS_CONSTRUCTOR, // constructor
			IS_FIELD, // field
			IS_TYPE, // nested type
			CALLER_SENSITIVE, // @CallerSensitive annotation detected
			TRUSTED_FINAL; // trusted final field

	public static final int ALL_ACCESS;
	public static final int ALL_KINDS;
	public static final int IS_INVOCABLE;

	private static MethodHandle isInvocable;
	private static MethodHandle isMethod;
	private static MethodHandle isConstructor;
	private static MethodHandle isField;
	private static MethodHandle isType;
	private static MethodHandle isPackage;
	private static MethodHandle isCallerSensitive;
	private static MethodHandle isTrustedFinalField;

	private static Class<?> class_java_lang_invoke_DirectMethodHandle;
	private static Class<?> class_java_lang_invoke_DirectMethodHandle$Constructor;

	static {
		try {
			class_java_lang_invoke_DirectMethodHandle = Class.forName("java.lang.invoke.DirectMethodHandle");
			class_java_lang_invoke_DirectMethodHandle$Constructor = Class.forName("java.lang.invoke.DirectMethodHandle$Constructor");

		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		BRIDGE = (int) ObjectManipulator.access(class_java_lang_invoke_MemberName, "BRIDGE");
		VARARGS = (int) ObjectManipulator.access(class_java_lang_invoke_MemberName, "VARARGS");
		SYNTHETIC = (int) ObjectManipulator.access(class_java_lang_invoke_MemberName, "SYNTHETIC");
		ANNOTATION = (int) ObjectManipulator.access(class_java_lang_invoke_MemberName, "ANNOTATION");
		ENUM = (int) ObjectManipulator.access(class_java_lang_invoke_MemberName, "ENUM");

		CONSTRUCTOR_NAME = (String) ObjectManipulator.access(class_java_lang_invoke_MemberName, "CONSTRUCTOR_NAME");
		RECOGNIZED_MODIFIERS = (int) ObjectManipulator.access(class_java_lang_invoke_MemberName, "RECOGNIZED_MODIFIERS");

		IS_METHOD = constants.MN_IS_METHOD; // method (not constructor)
		IS_CONSTRUCTOR = constants.MN_IS_CONSTRUCTOR; // constructor
		IS_FIELD = constants.MN_IS_FIELD; // field
		IS_TYPE = constants.MN_IS_TYPE; // nested type
		CALLER_SENSITIVE = constants.MN_CALLER_SENSITIVE; // @CallerSensitive annotation detected
		TRUSTED_FINAL = constants.MN_TRUSTED_FINAL; // trusted final field

		ALL_ACCESS = (int) ObjectManipulator.access(class_java_lang_invoke_MemberName, "ALL_ACCESS");
		ALL_KINDS = (int) ObjectManipulator.access(class_java_lang_invoke_MemberName, "ALL_KINDS");
		IS_INVOCABLE = (int) ObjectManipulator.access(class_java_lang_invoke_MemberName, "IS_INVOCABLE");

		matchingFlagsSet = find_special_method(class_java_lang_invoke_MemberName, "matchingFlagsSet", boolean.class, int.class, int.class);
		allFlagsSet = find_special_method(class_java_lang_invoke_MemberName, "allFlagsSet", boolean.class, int.class);
		anyFlagSet = find_special_method(class_java_lang_invoke_MemberName, "anyFlagSet", boolean.class, int.class);

		isBridge = find_special_method(class_java_lang_invoke_MemberName, "isBridge", boolean.class);
		isVarargs = find_special_method(class_java_lang_invoke_MemberName, "isVarargs", boolean.class);
		isSynthetic = find_special_method(class_java_lang_invoke_MemberName, "isSynthetic", boolean.class);

		isInvocable = find_special_method(class_java_lang_invoke_MemberName, "isInvocable", boolean.class);
		isMethod = find_special_method(class_java_lang_invoke_MemberName, "isMethod", boolean.class);
		isConstructor = find_special_method(class_java_lang_invoke_MemberName, "isConstructor", boolean.class);
		isField = find_special_method(class_java_lang_invoke_MemberName, "isField", boolean.class);
		isType = find_special_method(class_java_lang_invoke_MemberName, "isType", boolean.class);
		isPackage = find_special_method(class_java_lang_invoke_MemberName, "isPackage", boolean.class);
		isCallerSensitive = find_special_method(class_java_lang_invoke_MemberName, "isCallerSensitive", boolean.class);
		isTrustedFinalField = find_special_method(class_java_lang_invoke_MemberName, "isTrustedFinalField", boolean.class);
	}

	public static boolean match_flags_set(Object member_name, int mask, int flags) {
		try {
			return (boolean) matchingFlagsSet.invokeExact(member_name, mask, flags);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean all_flags_set(Object member_name, int flags) {
		try {
			return (boolean) allFlagsSet.invokeExact(member_name, flags);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean any_flag_set(Object member_name, int flags) {
		try {
			return (boolean) anyFlagSet.invokeExact(member_name, flags);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean is_bridge(Object member_name) {
		try {
			return (boolean) isBridge.invokeExact(member_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean is_varargs(Object member_name) {
		try {
			return (boolean) isVarargs.invokeExact(member_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean is_synthetic(Object member_name) {
		try {
			return (boolean) isSynthetic.invokeExact(member_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean is_invocable(Object member_name) {
		try {
			return (boolean) isInvocable.invokeExact(member_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean is_method(Object member_name) {
		try {
			return (boolean) isMethod.invokeExact(member_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean is_constructor(Object member_name) {
		try {
			return (boolean) isConstructor.invokeExact(member_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean is_field(Object member_name) {
		try {
			return (boolean) isField.invokeExact(member_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean is_type(Object member_name) {
		try {
			return (boolean) isType.invokeExact(member_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean is_package(Object member_name) {
		try {
			return (boolean) isPackage.invokeExact(member_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean is_caller_sensitive(Object member_name) {
		try {
			return (boolean) isCallerSensitive.invokeExact(member_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	public static boolean is_trusted_final_field(Object member_name) {
		try {
			return (boolean) isTrustedFinalField.invokeExact(member_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BOOLEAN;
	}

	/**
	 * DirectMethodHandle$Constructor的MemberName对象
	 */
	private static VarHandle java_lang_invoke_DirectMethodHandle$Constructor_initMethod;
	private static VarHandle java_lang_invoke_DirectMethodHandle_member;

	static {
		java_lang_invoke_DirectMethodHandle$Constructor_initMethod = find_var(class_java_lang_invoke_DirectMethodHandle$Constructor, "initMethod", class_java_lang_invoke_MemberName);
		java_lang_invoke_DirectMethodHandle_member = find_var(class_java_lang_invoke_DirectMethodHandle, "member", class_java_lang_invoke_MemberName);
	}

	/**
	 * 获取一个Callable的MemberName
	 * 
	 * @param m
	 * @return
	 */
	public static final Object member_name(MethodHandle m) {
		if (class_java_lang_invoke_DirectMethodHandle$Constructor.isInstance(m))
			return java_lang_invoke_DirectMethodHandle$Constructor_initMethod.get(m);
		else if (class_java_lang_invoke_DirectMethodHandle.isInstance(m))
			return java_lang_invoke_DirectMethodHandle_member.get(m);
		return null;
	}

	private static VarHandle java_lang_invoke_MemberName_flags;

	static {
		java_lang_invoke_MemberName_flags = find_var(class_java_lang_invoke_MemberName, "flags", int.class);
	}

	/**
	 * 获取一个MemberName的标志
	 * 
	 * @param member_name
	 * @return
	 */
	public static int member_name_flags(Object member_name) {
		return (int) java_lang_invoke_MemberName_flags.get(member_name);
	}

	/**
	 * 设置一个MemberName的标志
	 * 
	 * @param member_name
	 * @param flags
	 * @return
	 */
	public static void set_member_name_flags(Object member_name, int flags) {
		java_lang_invoke_MemberName_flags.set(member_name, flags);
	}

	/**
	 * 设置flags中的标志flag是否启用，可通过该方法为flags增加或删除flag。
	 * 
	 * @param flags
	 * @param flag
	 * @param mark
	 * @return
	 */
	public static int set_flag(int flags, int flag, boolean mark) {
		return mark ? flags | flag : flags & (~flag);
	}

	private static MethodHandle getDirectMethod;

	static {
		getDirectMethod = find_special_method(MethodHandles.Lookup.class, "getDirectMethod", MethodHandle.class, byte.class, Class.class, class_java_lang_invoke_MemberName, MethodHandles.Lookup.class);
	}

	/**
	 * 将MemberName包装为MethodHandle
	 * 
	 * @param refKind      调用类型，实际上是字节码，从MethodHandleNativesConstants中查看，例如类的非静态成员方法是invokeVirtual。
	 * @param refc         调用者的所属类，即这个方法属于哪个类
	 * @param method
	 * @param callerLookup
	 * @return
	 */
	public static MethodHandle direct_method(byte refKind, Class<?> refc, Object method, Lookup callerLookup) {
		try {
			return (MethodHandle) getDirectMethod.invoke(TRUSTED_LOOKUP, refKind, refc, method, callerLookup);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return (MethodHandle) UNREACHABLE_REFERENCE;
	}

	public static MethodHandle direct_method(byte refKind, Class<?> refc, Object method) {
		return direct_method(refKind, refc, method, TRUSTED_LOOKUP);
	}

	private static MethodHandle getReferenceKind;

	static {
		getReferenceKind = find_virtual_method(class_java_lang_invoke_MemberName, "getReferenceKind", byte.class);
	}

	/**
	 * 获取指定member_name的调用字节码
	 * 
	 * @param member_name
	 * @return
	 */
	public static byte reference_kind(Object member_name) {
		try {
			return (byte) getReferenceKind.invoke(member_name);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_BYTE;
	}

	private static MethodHandle init;

	static {
		init = find_virtual_method(class_java_lang_invoke_MemberName, "init", void.class, Class.class, String.class, Object.class, int.class);
	}

	/**
	 * 在一个对象上进行初始化
	 * 
	 * @param member_name
	 * @param defClass
	 * @param name
	 * @param type        Class<?>或MethodType
	 * @param flags
	 * @return
	 */
	public static final Object init(Object member_name, Class<?> defClass, String name, Object type, int flags) {
		try {
			return (Object) init.invoke(member_name, defClass, name, flags);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return UNREACHABLE_REFERENCE;
	}

	/**
	 * 构建一个字段或方法的名称
	 * 
	 * @param defClass
	 * @param name
	 * @param type
	 * @param flags
	 * @return
	 */
	public static final Object allocate(Class<?> defClass, String name, Object type, int flags) {
		return init(unsafe.allocate(class_java_lang_invoke_MemberName), defClass, name, type, flags);
	}

	/**
	 * 构造函数类型
	 * 
	 * @param targetClass
	 * @param arg_types
	 * @return
	 */
	public static String constructor_description(Class<?> targetClass, Class<?>[] arg_types) {
		StringBuilder result = new StringBuilder();
		result.append(targetClass.getName()).append("(");
		for (int i = 0; i < arg_types.length; ++i) {
			result.append(arg_types[i].getName());
			if (i != arg_types.length)
				result.append(", ");
		}
		result.append(")");
		return result.toString();
	}
}

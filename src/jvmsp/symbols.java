package jvmsp;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

import jvmsp.internal.reflection_factory;

/**
 * 句柄操作相关，包括调用native方法<br>
 * 不依赖<br>
 * 
 * @implNote 该类未引用任何本库的类，需要最先初始化
 */
public class symbols
{
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

	static
	{
		TRUSTED_LOOKUP = allocate_trusted_lookup();
	}

	/**
	 * 使用ReflectionFactory的反序列化调用Lookup的构造函数新构建一个Lookup对象。<br>
	 * 
	 * @param lookup_class
	 * @param prev_lookup_class
	 * @param allowed_modes
	 * @return
	 */
	public static final MethodHandles.Lookup allocate_lookup(Class<?> lookup_class, Class<?> prev_lookup_class, int allowed_modes)
	{
		try
		{
			return reflection_factory.construct(MethodHandles.Lookup.class, MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class), lookup_class, prev_lookup_class, allowed_modes);
		}
		catch (IllegalArgumentException | NoSuchMethodException | SecurityException ex)
		{
			throw new java.lang.InternalError("allocate lookup of '" + lookup_class + "' failed", ex);
		}
	}

	/**
	 * 构造一个TRUSTED的Lookup
	 * 
	 * @return
	 */
	public static final MethodHandles.Lookup allocate_trusted_lookup()
	{
		return allocate_lookup(Object.class, null, TRUSTED);
	}

	/**
	 * 使用ReflectionFactory的反序列化调用Lookup的构造函数新构建一个Lookup对象。<br>
	 * 
	 * @return
	 */
	public static final MethodHandles.Lookup allocate_lookup(Class<?> lookup_class)
	{
		try
		{
			return reflection_factory.construct(MethodHandles.Lookup.class, MethodHandles.Lookup.class.getDeclaredConstructor(Class.class), lookup_class);
		}
		catch (IllegalArgumentException | NoSuchMethodException | SecurityException ex)
		{
			throw new java.lang.InternalError("allocate lookup of '" + lookup_class + "' failed", ex);
		}
	}

	public static final MethodHandles.Lookup allocate_lookup()
	{
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
	public static final VarHandle find_var(Class<?> clazz, String field_name, Class<?> type)
	{
		try
		{
			return MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP).findVarHandle(clazz, field_name, type);
		}
		catch (IllegalAccessException | NoSuchFieldException ex)
		{
			throw new java.lang.InternalError("find var handle '" + field_name + "' in '" + clazz + "' failed", ex);
		}
	}

	public static final VarHandle find_static_var(Class<?> clazz, String field_name, Class<?> type)
	{
		try
		{
			return MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP).findStaticVarHandle(clazz, field_name, type);
		}
		catch (IllegalAccessException | NoSuchFieldException ex)
		{
			throw new java.lang.InternalError("find static var handle '" + field_name + "' in '" + clazz + "' failed", ex);
		}
	}

	/**
	 * 查找构造函数
	 * 
	 * @param clazz
	 * @param arg_types
	 * @return
	 */
	public static final MethodHandle find_constructor(Class<?> clazz, Class<?>... arg_types)
	{
		try
		{
			return MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP).findConstructor(clazz, MethodType.methodType(void.class, arg_types));
		}
		catch (IllegalAccessException | NoSuchMethodException ex)
		{
			throw new java.lang.InternalError("find constructor handle of '" + clazz + "' failed", ex);
		}
	}

	public static final MethodHandle find_initializer(Class<?> clazz)
	{
		try
		{
			return MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP).findStatic(clazz, INITIALIZER_NAME, MethodType.methodType(void.class, new Class<?>[0]));
		}
		catch (IllegalAccessException | NoSuchMethodException ex)
		{
			throw new java.lang.InternalError("find initializer handle of '" + clazz + "' failed", ex);
		}
	}

	/**
	 * 查找任意字节码行为的方法句柄(包括native)，从search_chain_start_subclazz开始查找，如果该类不存在方法则一直向上查找方法，直到在指定的超类search_chain_end_superclazz中也找不到方法时终止并抛出错误 句柄等价于Unsafe查找到的offset与base的组合，明确指定了一个内存中的方法地址
	 * 
	 * @param search_chain_start_subclazz 查找链起始类，也是要查找的对象，必须是search_chain_end_superclazz的子类
	 * @param search_chain_end_superclazz 查找链终止类
	 * @param type                        方法类型，包含返回值和参数类型
	 * @return 查找到的方法句柄
	 */
	public static final MethodHandle find_special_method(Class<?> search_chain_start_subclazz, Class<?> search_chain_end_superclazz, String method_name, MethodType type)
	{
		try
		{
			return MethodHandles.privateLookupIn(search_chain_start_subclazz, TRUSTED_LOOKUP).findSpecial(search_chain_end_superclazz, method_name, type, search_chain_start_subclazz);
		}
		catch (IllegalAccessException | NoSuchMethodException ex)
		{
			throw new java.lang.InternalError("find special method handle '" + method_name + "' in '" + search_chain_start_subclazz + "' failed", ex);
		}
	}

	public static final MethodHandle find_special_method(Class<?> search_clazz, String method_name, MethodType type)
	{
		return find_special_method(search_clazz, search_clazz, method_name, type);
	}

	public static final MethodHandle find_special_method(Class<?> search_chain_start_subclazz, Class<?> search_chain_end_superclazz, String method_name, Class<?> return_type, Class<?>... arg_types)
	{
		return find_special_method(search_chain_start_subclazz, search_chain_end_superclazz, method_name, MethodType.methodType(return_type, arg_types));
	}

	public static final MethodHandle find_special_method(Class<?> search_clazz, String method_name, Class<?> return_type, Class<?>... arg_types)
	{
		return find_special_method(search_clazz, search_clazz, method_name, return_type, arg_types);
	}

	public static final MethodHandle find_virtual_method(Class<?> clazz, String method_name, MethodType type)
	{
		try
		{
			return MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP).findVirtual(clazz, method_name, type);
		}
		catch (IllegalAccessException | NoSuchMethodException ex)
		{
			throw new java.lang.InternalError("find virtual method handle '" + method_name + "' in '" + clazz + "' failed", ex);
		}
	}

	public static final MethodHandle find_virtual_method(Class<?> clazz, String method_name, Class<?> return_type, Class<?>... arg_types)
	{
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
	public static final MethodHandle find_static_method(Class<?> clazz, String method_name, MethodType type)
	{
		try
		{
			return MethodHandles.privateLookupIn(clazz, TRUSTED_LOOKUP).findStatic(clazz, method_name, type);
		}
		catch (IllegalAccessException | NoSuchMethodException ex)
		{
			throw new java.lang.InternalError("find static method handle '" + method_name + "' in '" + clazz + "' failed", ex);
		}
	}

	public static final MethodHandle find_static_method(Class<?> clazz, String method_name, Class<?> return_type, Class<?>... arg_types)
	{
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
	public static final MethodHandle find_method(Class<?> clazz, String method_name, Class<?>... arg_types)
	{
		MethodHandle m = null;
		if (method_name.equals(CONSTRUCTOR_NAME))
			m = find_constructor(clazz, arg_types);
		else
		{
			Method rm = reflection.find_declared_method(clazz, method_name, arg_types);
			if (Modifier.isStatic(rm.getModifiers()))
				m = symbols.find_static_method(clazz, method_name, rm.getReturnType(), arg_types);
			else
				m = symbols.find_virtual_method(clazz, method_name, rm.getReturnType(), arg_types);
		}
		return m;
	}

	private static VarHandle Class_classData;

	private static Class<?> java_lang_invoke_MethodHandles$Lookup$ClassFile;

	private static MethodHandle ClassFile_readClassFile;

	static
	{
		Class_classData = find_var(Class.class, "classData", Object.class);
		try
		{
			java_lang_invoke_MethodHandles$Lookup$ClassFile = Class.forName("java.lang.invoke.MethodHandles$Lookup$ClassFile");
			ClassFile_readClassFile = find_static_method(java_lang_invoke_MethodHandles$Lookup$ClassFile, "readClassFile", java_lang_invoke_MethodHandles$Lookup$ClassFile, byte[].class);
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 获取所属类的classData。<br>
	 * 该对象可以为用户自定义的任何对象，相当于clazz类的静态字段对象
	 * 
	 * @param clazz
	 * @return
	 */
	public static final Object get_class_data(Class<?> clazz)
	{
		return Class_classData.get(clazz);
	}

	/**
	 * 读取ClassFile，隐藏类定义时使用
	 * 
	 * @param bytes
	 * @return
	 */
	public static final Object read_class_file(byte[] bytes)
	{
		try
		{
			return ClassFile_readClassFile.invoke(bytes);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("read class file of '" + bytes + "' failed", ex);
		}
	}

	public static final Class<?> define_hidden_class(Class<?> context_clazz, String name, byte[] bytes, ProtectionDomain pd, boolean initialize)
	{
		return class_loader.define(context_clazz, name, bytes, 0, bytes.length, pd, initialize, constants.HIDDEN_CLASS, null);// 隐藏类的class_data为null
	}

	/**
	 * 用于查找定义于clazz的上下文中的类，本质上是通过clazz的ClassLoader定义的。<br>
	 * 
	 * 无法查找隐藏类，虽然隐藏类也是由clazz的ClassLoader加载的，且具有名称，但只有包名（context_clazz的包名）没有类名，因此无法通过Class.forName()或Lookup.findClass()以名称查找
	 * 
	 * @param clazz
	 * @param name
	 * @return
	 */
	public static final Class<?> find_class(Class<?> context_clazz, String name)
	{
		try
		{
			return MethodHandles.privateLookupIn(context_clazz, TRUSTED_LOOKUP).findClass(name);
		}
		catch (IllegalAccessException | ClassNotFoundException ex)
		{
			throw new java.lang.InternalError("find class '" + name + "' in context '" + context_clazz + "' failed", ex);
		}
	}

	public static final Object read(Object obj, String field_name, Class<?> type)
	{
		return find_var(obj.getClass(), field_name, type).get(obj);
	}

	public static final Object read_static(Class<?> clazz, String field_name, Class<?> type)
	{
		return find_static_var(clazz, field_name, type).get();
	}

	public static final void write(Object obj, String field_name, Class<?> type, Object value)
	{
		find_var(obj.getClass(), field_name, type).set(obj, value);
	}

	public static final void write_static(Class<?> clazz, String field_name, Class<?> type, Object value)
	{
		find_static_var(clazz, field_name, type).set(value);
	}

	/**
	 * VM底层相关信息
	 */

	static Class<?> java_lang_invoke_MethodHandleNatives;

	static
	{
		try
		{
			java_lang_invoke_MethodHandleNatives = Class.forName("java.lang.invoke.MethodHandleNatives");
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * java.lang.invoke.MethodHandleNatives.Constants定义的常数<br>
	 * 主要用于MemberName
	 */
	public static final class constants
	{

		static Class<?> java_lang_invoke_MethodHandleNatives_Constants;

		public static final int MN_IS_METHOD, // method (not constructor)
				MN_IS_CONSTRUCTOR, // constructor
				MN_IS_FIELD, // field
				MN_IS_TYPE, // nested type
				MN_CALLER_SENSITIVE, // @CallerSensitive annotation detected
				MN_TRUSTED_FINAL, // trusted final field
				MN_REFERENCE_KIND_SHIFT, // ref_kind
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

		static
		{
			try
			{
				java_lang_invoke_MethodHandleNatives_Constants = Class.forName("java.lang.invoke.MethodHandleNatives$Constants");
			}
			catch (ClassNotFoundException ex)
			{
				ex.printStackTrace();
			}
			MN_IS_METHOD = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "MN_IS_METHOD", int.class);
			MN_IS_CONSTRUCTOR = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "MN_IS_CONSTRUCTOR", int.class);
			MN_IS_FIELD = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "MN_IS_FIELD", int.class);
			MN_IS_TYPE = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "MN_IS_TYPE", int.class);
			MN_CALLER_SENSITIVE = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "MN_CALLER_SENSITIVE", int.class);
			MN_TRUSTED_FINAL = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "MN_TRUSTED_FINAL", int.class);
			MN_REFERENCE_KIND_SHIFT = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "MN_REFERENCE_KIND_SHIFT", int.class);
			MN_REFERENCE_KIND_MASK = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "MN_REFERENCE_KIND_MASK", int.class);

			REF_NONE = (byte) read_static(java_lang_invoke_MethodHandleNatives_Constants, "REF_NONE", byte.class);
			REF_getField = (byte) read_static(java_lang_invoke_MethodHandleNatives_Constants, "REF_getField", byte.class);
			REF_getStatic = (byte) read_static(java_lang_invoke_MethodHandleNatives_Constants, "REF_getStatic", byte.class);
			REF_putField = (byte) read_static(java_lang_invoke_MethodHandleNatives_Constants, "REF_putField", byte.class);
			REF_putStatic = (byte) read_static(java_lang_invoke_MethodHandleNatives_Constants, "REF_putStatic", byte.class);
			REF_invokeVirtual = (byte) read_static(java_lang_invoke_MethodHandleNatives_Constants, "REF_invokeVirtual", byte.class);
			REF_invokeStatic = (byte) read_static(java_lang_invoke_MethodHandleNatives_Constants, "REF_invokeStatic", byte.class);
			REF_invokeSpecial = (byte) read_static(java_lang_invoke_MethodHandleNatives_Constants, "REF_invokeSpecial", byte.class);
			REF_newInvokeSpecial = (byte) read_static(java_lang_invoke_MethodHandleNatives_Constants, "REF_newInvokeSpecial", byte.class);
			REF_invokeInterface = (byte) read_static(java_lang_invoke_MethodHandleNatives_Constants, "REF_invokeInterface", byte.class);
			REF_LIMIT = (byte) read_static(java_lang_invoke_MethodHandleNatives_Constants, "REF_LIMIT", byte.class);

			NESTMATE_CLASS = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "NESTMATE_CLASS", int.class);
			HIDDEN_CLASS = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "HIDDEN_CLASS", int.class);
			STRONG_LOADER_LINK = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "STRONG_LOADER_LINK", int.class);
			ACCESS_VM_ANNOTATIONS = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "ACCESS_VM_ANNOTATIONS", int.class);

			LM_MODULE = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "LM_MODULE", int.class);
			LM_UNCONDITIONAL = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "LM_UNCONDITIONAL", int.class);
			LM_TRUSTED = (int) read_static(java_lang_invoke_MethodHandleNatives_Constants, "LM_TRUSTED", int.class);
		}
	}

	/**
	 * 
	 */

	/**
	 * 目标成员字段或方法的信息。<br>
	 */
	public static final class vm_info
	{
		/**
		 * 成员的偏移量，相对于class或interface起始。<br>
		 * {@link https://github.com/openjdk/jdk/blob/3ff83ec49e561c44dd99508364b8ba068274b63a/src/hotspot/share/classfile/javaClasses.hpp#L1310}
		 */
		public final long vm_index;

		/**
		 * 目标成员字段或方法的信息。<br>
		 */
		public final Object vm_target;

		private vm_info(long vm_index, Object vm_target)
		{
			this.vm_index = vm_index;
			this.vm_target = vm_target;
		}
	}

	// JVM底层的符号信息
	private static Class<?> java_lang_invoke_MemberName;

	private static MethodHandle objectFieldOffset;
	private static MethodHandle staticFieldOffset;
	private static MethodHandle staticFieldBase;
	private static MethodHandle getMemberVMInfo;

	static
	{
		try
		{
			java_lang_invoke_MemberName = Class.forName("java.lang.invoke.MemberName");
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		getMemberVMInfo = symbols.find_static_method(java_lang_invoke_MethodHandleNatives, "objectFieldOffset", long.class, java_lang_invoke_MemberName);
		getMemberVMInfo = symbols.find_static_method(java_lang_invoke_MethodHandleNatives, "staticFieldOffset", long.class, java_lang_invoke_MemberName);
		getMemberVMInfo = symbols.find_static_method(java_lang_invoke_MethodHandleNatives, "staticFieldBase", Object.class, java_lang_invoke_MemberName);
		getMemberVMInfo = symbols.find_static_method(java_lang_invoke_MethodHandleNatives, "getMemberVMInfo", Object.class, java_lang_invoke_MemberName);
	}

	public static final long object_field_offset(Object member_name)
	{
		try
		{
			return (long) objectFieldOffset.invoke(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get object field offset of '" + member_name.toString() + "' failed", ex);
		}
	}

	public static final long static_field_offset(Object member_name)
	{
		try
		{
			return (long) staticFieldOffset.invoke(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get static field offset of '" + member_name.toString() + "' failed", ex);
		}
	}

	public static final Object static_field_base(Object member_name)
	{
		try
		{
			return (Object) staticFieldBase.invoke(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get static field base of '" + member_name.toString() + "' failed", ex);
		}
	}

	/**
	 * 获取成员的底层信息
	 * 
	 * @param member_name
	 * @return
	 */
	public static final vm_info get_vm_info(Object member_name)
	{
		try
		{
			Object[] vminfo = (Object[]) getMemberVMInfo.invoke(member_name);
			return new vm_info((Long) vminfo[0], vminfo[1]);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get vm info of '" + member_name.toString() + "' failed", ex);
		}
	}

	public static final vm_info get_vm_info(Class<?> target, String method_name, Class<?>... arg_types)
	{
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

	private static Class<?> java_lang_invoke_DirectMethodHandle;
	private static Class<?> java_lang_invoke_DirectMethodHandle$Constructor;

	private static MethodHandle isInvocable;
	private static MethodHandle isMethod;
	private static MethodHandle isConstructor;
	private static MethodHandle isField;
	private static MethodHandle isType;
	private static MethodHandle isPackage;
	private static MethodHandle isCallerSensitive;
	private static MethodHandle isTrustedFinalField;

	static
	{
		try
		{
			java_lang_invoke_DirectMethodHandle = Class.forName("java.lang.invoke.DirectMethodHandle");
			java_lang_invoke_DirectMethodHandle$Constructor = Class.forName("java.lang.invoke.DirectMethodHandle$Constructor");
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		BRIDGE = (int) read_static(java_lang_invoke_MemberName, "BRIDGE", int.class);
		VARARGS = (int) read_static(java_lang_invoke_MemberName, "VARARGS", int.class);
		SYNTHETIC = (int) read_static(java_lang_invoke_MemberName, "SYNTHETIC", int.class);
		ANNOTATION = (int) read_static(java_lang_invoke_MemberName, "ANNOTATION", int.class);
		ENUM = (int) read_static(java_lang_invoke_MemberName, "ENUM", int.class);

		CONSTRUCTOR_NAME = (String) read_static(java_lang_invoke_MemberName, "CONSTRUCTOR_NAME", String.class);
		RECOGNIZED_MODIFIERS = (int) read_static(java_lang_invoke_MemberName, "RECOGNIZED_MODIFIERS", int.class);

		IS_METHOD = constants.MN_IS_METHOD; // method (not constructor)
		IS_CONSTRUCTOR = constants.MN_IS_CONSTRUCTOR; // constructor
		IS_FIELD = constants.MN_IS_FIELD; // field
		IS_TYPE = constants.MN_IS_TYPE; // nested type
		CALLER_SENSITIVE = constants.MN_CALLER_SENSITIVE; // @CallerSensitive annotation detected
		TRUSTED_FINAL = constants.MN_TRUSTED_FINAL; // trusted final field

		ALL_ACCESS = (int) read_static(java_lang_invoke_MemberName, "ALL_ACCESS", int.class);
		ALL_KINDS = (int) read_static(java_lang_invoke_MemberName, "ALL_KINDS", int.class);
		IS_INVOCABLE = (int) read_static(java_lang_invoke_MemberName, "IS_INVOCABLE", int.class);

		matchingFlagsSet = find_special_method(java_lang_invoke_MemberName, "matchingFlagsSet", boolean.class, int.class, int.class);
		allFlagsSet = find_special_method(java_lang_invoke_MemberName, "allFlagsSet", boolean.class, int.class);
		anyFlagSet = find_special_method(java_lang_invoke_MemberName, "anyFlagSet", boolean.class, int.class);

		isBridge = find_special_method(java_lang_invoke_MemberName, "isBridge", boolean.class);
		isVarargs = find_special_method(java_lang_invoke_MemberName, "isVarargs", boolean.class);
		isSynthetic = find_special_method(java_lang_invoke_MemberName, "isSynthetic", boolean.class);

		isInvocable = find_special_method(java_lang_invoke_MemberName, "isInvocable", boolean.class);
		isMethod = find_special_method(java_lang_invoke_MemberName, "isMethod", boolean.class);
		isConstructor = find_special_method(java_lang_invoke_MemberName, "isConstructor", boolean.class);
		isField = find_special_method(java_lang_invoke_MemberName, "isField", boolean.class);
		isType = find_special_method(java_lang_invoke_MemberName, "isType", boolean.class);
		isPackage = find_special_method(java_lang_invoke_MemberName, "isPackage", boolean.class);
		isCallerSensitive = find_special_method(java_lang_invoke_MemberName, "isCallerSensitive", boolean.class);
		isTrustedFinalField = find_special_method(java_lang_invoke_MemberName, "isTrustedFinalField", boolean.class);
	}

	public static final boolean match_flags_set(Object member_name, int mask, int flags)
	{
		try
		{
			return (boolean) matchingFlagsSet.invokeExact(member_name, mask, flags);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' matching flags set failed", ex);
		}
	}

	public static final boolean all_flags_set(Object member_name, int flags)
	{
		try
		{
			return (boolean) allFlagsSet.invokeExact(member_name, flags);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check all flags set failed", ex);
		}
	}

	public static final boolean any_flag_set(Object member_name, int flags)
	{
		try
		{
			return (boolean) anyFlagSet.invokeExact(member_name, flags);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check any flags set failed", ex);
		}
	}

	public static final boolean is_bridge(Object member_name)
	{
		try
		{
			return (boolean) isBridge.invokeExact(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check is bridge failed", ex);
		}
	}

	public static final boolean is_varargs(Object member_name)
	{
		try
		{
			return (boolean) isVarargs.invokeExact(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check is varargs failed", ex);
		}
	}

	public static final boolean is_synthetic(Object member_name)
	{
		try
		{
			return (boolean) isSynthetic.invokeExact(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check is synthetic failed", ex);
		}
	}

	public static final boolean is_invocable(Object member_name)
	{
		try
		{
			return (boolean) isInvocable.invokeExact(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check is invocable failed", ex);
		}
	}

	public static final boolean is_method(Object member_name)
	{
		try
		{
			return (boolean) isMethod.invokeExact(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check is method failed", ex);
		}
	}

	public static final boolean is_constructor(Object member_name)
	{
		try
		{
			return (boolean) isConstructor.invokeExact(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check is constructor failed", ex);
		}
	}

	public static final boolean is_field(Object member_name)
	{
		try
		{
			return (boolean) isField.invokeExact(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check is field failed", ex);
		}
	}

	public static final boolean is_type(Object member_name)
	{
		try
		{
			return (boolean) isType.invokeExact(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check is type failed", ex);
		}
	}

	public static final boolean is_package(Object member_name)
	{
		try
		{
			return (boolean) isPackage.invokeExact(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check is package failed", ex);
		}
	}

	public static final boolean is_caller_sensitive(Object member_name)
	{
		try
		{
			return (boolean) isCallerSensitive.invokeExact(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check is caller sensitive failed", ex);
		}
	}

	public static final boolean is_trusted_final_field(Object member_name)
	{
		try
		{
			return (boolean) isTrustedFinalField.invokeExact(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' check is trusted final field failed", ex);
		}
	}

	/**
	 * DirectMethodHandle$Constructor的MemberName对象
	 */
	private static VarHandle java_lang_invoke_DirectMethodHandle$Constructor_initMethod;
	private static VarHandle java_lang_invoke_DirectMethodHandle_member;

	static
	{
		java_lang_invoke_DirectMethodHandle$Constructor_initMethod = find_var(java_lang_invoke_DirectMethodHandle$Constructor, "initMethod", java_lang_invoke_MemberName);
		java_lang_invoke_DirectMethodHandle_member = find_var(java_lang_invoke_DirectMethodHandle, "member", java_lang_invoke_MemberName);
	}

	/**
	 * 获取一个Callable的MemberName
	 * 
	 * @param m
	 * @return
	 */
	public static final Object member_name(MethodHandle m)
	{
		if (java_lang_invoke_DirectMethodHandle$Constructor.isInstance(m))
			return java_lang_invoke_DirectMethodHandle$Constructor_initMethod.get(m);
		else if (java_lang_invoke_DirectMethodHandle.isInstance(m))
			return java_lang_invoke_DirectMethodHandle_member.get(m);
		return null;
	}

	private static VarHandle java_lang_invoke_MemberName_flags;

	static
	{
		java_lang_invoke_MemberName_flags = find_var(java_lang_invoke_MemberName, "flags", int.class);
	}

	/**
	 * 获取一个MemberName的标志
	 * 
	 * @param member_name
	 * @return
	 */
	public static final int member_name_flags(Object member_name)
	{
		return (int) java_lang_invoke_MemberName_flags.get(member_name);
	}

	/**
	 * 设置一个MemberName的标志
	 * 
	 * @param member_name
	 * @param flags
	 * @return
	 */
	public static final void set_member_name_flags(Object member_name, int flags)
	{
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
	public static final int set_flag(int flags, int flag, boolean mark)
	{
		return mark ? flags | flag : flags & (~flag);
	}

	private static MethodHandle getDirectMethodCommon;

	static
	{
		getDirectMethodCommon = find_special_method(MethodHandles.Lookup.class, "getDirectMethodCommon", MethodHandle.class, byte.class, Class.class, java_lang_invoke_MemberName, boolean.class, boolean.class, MethodHandles.Lookup.class);
	}

	public static final MethodHandle direct_method(byte ref_kind, Class<?> refc, Object member_name, boolean check_security, boolean do_restrict, MethodHandles.Lookup bound_caller)
	{
		try
		{
			return (MethodHandle) getDirectMethodCommon.invoke(TRUSTED_LOOKUP, ref_kind, refc, member_name, check_security, do_restrict, bound_caller);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' wrap direct method handle failed", ex);
		}
	}

	/**
	 * 将MemberName包装为MethodHandle，无安全检查
	 * 
	 * @param ref_kind 调用类型，实际上是字节码，从MethodHandleNativesConstants中查看，例如类的非静态成员方法是invokeVirtual。
	 * @param refc     调用者的所属类，即这个方法属于哪个类
	 * @param method
	 * @return
	 */
	public static final MethodHandle direct_method(byte ref_kind, Class<?> refc, Object method)
	{
		return direct_method(ref_kind, refc, method, false, true, TRUSTED_LOOKUP);
	}

	public static final MethodHandle direct_method(Class<?> refc, Object method)
	{
		return direct_method(constants.REF_invokeSpecial, refc, method, false, false, TRUSTED_LOOKUP);
	}

	private static MethodHandle getReferenceKind;

	static
	{
		getReferenceKind = find_virtual_method(java_lang_invoke_MemberName, "getReferenceKind", byte.class);
	}

	/**
	 * 获取指定member_name的调用字节码
	 * 
	 * @param member_name
	 * @return
	 */
	public static final byte reference_kind(Object member_name)
	{
		try
		{
			return (byte) getReferenceKind.invoke(member_name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' get reference kind failed", ex);
		}
	}

	private static MethodHandle MemberName_init;

	static
	{
		MemberName_init = find_virtual_method(java_lang_invoke_MemberName, "init", void.class, Class.class, String.class, Object.class, int.class);
	}

	/**
	 * 在一个对象上进行初始化
	 * 
	 * @param member_name
	 * @param def_class
	 * @param name
	 * @param type        Class<?>或MethodType
	 * @param flags
	 * @return
	 */
	public static final Object __init(Object member_name, Class<?> def_class, String name, Object type, int flags)
	{
		try
		{
			return (Object) MemberName_init.invoke(member_name, def_class, name, flags);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("member name '" + member_name.toString() + "' init failed", ex);
		}
	}

	/**
	 * 构建一个字段或方法的名称
	 * 
	 * @param def_class
	 * @param name
	 * @param type
	 * @param flags
	 * @return
	 */
	public static final Object allocate(Class<?> def_class, String name, Object type, int flags)
	{
		return __init(unsafe.allocate(java_lang_invoke_MemberName), def_class, name, type, flags);
	}

	/**
	 * 构造函数类型
	 * 
	 * @param target_class
	 * @param arg_types
	 * @return
	 */
	public static final String constructor_description(Class<?> target_class, Class<?>[] arg_types)
	{
		StringBuilder result = new StringBuilder();
		result.append(target_class.getName()).append("(");
		for (int i = 0; i < arg_types.length; ++i)
		{
			result.append(arg_types[i].getName());
			if (i != arg_types.length)
				result.append(", ");
		}
		result.append(")");
		return result.toString();
	}

	/**
	 * 获取一个可以被当作实例方法调用的构造函数。
	 * 
	 * @param target_class
	 * @param arg_types
	 * @return
	 */
	public static final MethodHandle constructor_method(Class<?> target_class, Class<?>... arg_types)
	{
		Object member_name = symbols.member_name(symbols.find_constructor(target_class, arg_types));
		int flags = symbols.member_name_flags(member_name);
		flags = symbols.set_flag(flags, symbols.IS_CONSTRUCTOR, false);// 取消构造函数标志
		flags = symbols.set_flag(flags, symbols.IS_METHOD, true);// 添加普通方法标志
		symbols.set_member_name_flags(member_name, flags);
		return symbols.direct_method(symbols.constants.REF_invokeVirtual, target_class, member_name);
	}

	private static Class<?> java_lang_invoke_BoundMethodHandle;

	private static MethodHandle MethodHandle_rebind;
	private static MethodHandle BoundMethodHandle_bindArgumentL;
	private static MethodHandle BoundMethodHandle_bindArgumentI;
	private static MethodHandle BoundMethodHandle_bindArgumentJ;
	private static MethodHandle BoundMethodHandle_bindArgumentF;
	private static MethodHandle BoundMethodHandle_bindArgumentD;

	static
	{
		try
		{
			java_lang_invoke_BoundMethodHandle = Class.forName("java.lang.invoke.BoundMethodHandle");
			MethodHandle_rebind = find_virtual_method(MethodHandle.class, "rebind", java_lang_invoke_BoundMethodHandle);
			BoundMethodHandle_bindArgumentL = find_virtual_method(java_lang_invoke_BoundMethodHandle, "bindArgumentL", java_lang_invoke_BoundMethodHandle, int.class, Object.class);
			BoundMethodHandle_bindArgumentI = find_virtual_method(java_lang_invoke_BoundMethodHandle, "bindArgumentI", java_lang_invoke_BoundMethodHandle, int.class, int.class);
			BoundMethodHandle_bindArgumentJ = find_virtual_method(java_lang_invoke_BoundMethodHandle, "bindArgumentJ", java_lang_invoke_BoundMethodHandle, int.class, long.class);
			BoundMethodHandle_bindArgumentF = find_virtual_method(java_lang_invoke_BoundMethodHandle, "bindArgumentF", java_lang_invoke_BoundMethodHandle, int.class, float.class);
			BoundMethodHandle_bindArgumentD = find_virtual_method(java_lang_invoke_BoundMethodHandle, "bindArgumentD", java_lang_invoke_BoundMethodHandle, int.class, double.class);
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 重新绑定参数，返回BoundMethodHandle对象
	 * 
	 * @param target
	 * @return
	 */
	public static final MethodHandle __rebind(MethodHandle target)
	{
		try
		{
			return (MethodHandle) MethodHandle_rebind.invoke(target);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("rebind '" + target + "' failed", ex);
		}
	}

	public static final MethodHandle bind(MethodHandle target, int pos, Object arg)
	{
		try
		{
			return (MethodHandle) BoundMethodHandle_bindArgumentL.invoke(__rebind(target), pos, arg);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("bind '" + arg + "' to '" + target + "' at '" + pos + "'  failed", ex);
		}
	}

	public static final MethodHandle bind(MethodHandle target, int pos, int arg)
	{
		try
		{
			return (MethodHandle) BoundMethodHandle_bindArgumentI.invoke(__rebind(target), pos, arg);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("bind '" + arg + "' to '" + target + "' at '" + pos + "'  failed", ex);
		}
	}

	public static final MethodHandle bind(MethodHandle target, int pos, long arg)
	{
		try
		{
			return (MethodHandle) BoundMethodHandle_bindArgumentJ.invoke(__rebind(target), pos, arg);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("bind '" + arg + "' to '" + target + "' at '" + pos + "'  failed", ex);
		}
	}

	public static final MethodHandle bind(MethodHandle target, int pos, float arg)
	{
		try
		{
			return (MethodHandle) BoundMethodHandle_bindArgumentF.invoke(__rebind(target), pos, arg);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("bind '" + arg + "' to '" + target + "' at '" + pos + "'  failed", ex);
		}
	}

	public static final MethodHandle bind(MethodHandle target, int pos, double arg)
	{
		try
		{
			return (MethodHandle) BoundMethodHandle_bindArgumentD.invoke(__rebind(target), pos, arg);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("bind '" + arg + "' to '" + target + "' at '" + pos + "'  failed", ex);
		}
	}

}

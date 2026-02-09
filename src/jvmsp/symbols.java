package jvmsp;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;

import jvmsp.cxx_type.pointer;
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
	 * @param field_name
	 * @param jtype
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

	/**
	 * 调用method方法，自动打包对象和参数
	 * 
	 * @param method
	 * @param obj
	 * @param args
	 * @return
	 */
	public static final Object call(MethodHandle method, Object obj, Object... args)
	{
		Object[] wrapped_args = new Object[args.length + 1];
		wrapped_args[0] = obj;
		System.arraycopy(args, 0, wrapped_args, 1, args.length);
		try
		{
			return method.invokeWithArguments(wrapped_args);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call '" + method.toString() + "' failed");
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
		public final long offset;

		/**
		 * 目标成员字段或方法的信息。<br>
		 * 若是字段，则target_ptr为字段所属类型Class<?> oop。<br>
		 * 若是方法，则target_ptr为其在C++层的Method*指针。根据Method*可以进一步获取constMethod*<br>
		 */
		public final Object target_ptr;

		private vm_info(long vmindex, Object vmtarget)
		{
			this.offset = vmindex;
			this.target_ptr = vmtarget;
		}

		/**
		 * 获取InstanceMirrorKlass。<br>
		 * 这个对象实际就是staticFieldBase().<br>
		 * 
		 * @return
		 */
		public final pointer instance_mirror_klass()
		{
			return pointer.address_of(target_ptr).cast(Class.class);
		}

		public final pointer method()
		{
			return pointer.address_of(target_ptr).cast(byte.class);
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
	 * native调用相关
	 */

	private static Class<?> jdk_internal_foreign_abi_NativeEntryPoint;
	private static Class<?> java_lang_invoke_NativeMethodHandle;

	private static Class<?> jdk_internal_foreign_abi_LinkerOptions;

	private static Class<?> jdk_internal_foreign_abi_ABIDescriptor;
	private static Class<?> jdk_internal_foreign_abi_VMStorage;

	private static MethodHandle NativeEntryPoint_makeDowncallStub;
	private static MethodHandle NativeEntryPoint_freeDowncallStub0;
	private static MethodHandle NativeEntryPoint_make;
	private static MethodHandle NativeMethodHandle_make;

	static
	{
		try
		{
			jdk_internal_foreign_abi_NativeEntryPoint = Class.forName("jdk.internal.foreign.abi.NativeEntryPoint");
			java_lang_invoke_NativeMethodHandle = Class.forName("java.lang.invoke.NativeMethodHandle");
			jdk_internal_foreign_abi_ABIDescriptor = Class.forName("jdk.internal.foreign.abi.ABIDescriptor");
			jdk_internal_foreign_abi_VMStorage = Class.forName("jdk.internal.foreign.abi.VMStorage");
			jdk_internal_foreign_abi_LinkerOptions = Class.forName("jdk.internal.foreign.abi.LinkerOptions");
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
		NativeEntryPoint_makeDowncallStub = find_static_method(jdk_internal_foreign_abi_NativeEntryPoint, "makeDowncallStub", long.class,
				MethodType.class, jdk_internal_foreign_abi_ABIDescriptor, jdk_internal_foreign_abi_VMStorage.arrayType(), jdk_internal_foreign_abi_VMStorage.arrayType(), boolean.class, int.class, boolean.class);
		NativeEntryPoint_freeDowncallStub0 = find_static_method(jdk_internal_foreign_abi_NativeEntryPoint, "freeDowncallStub0", boolean.class, long.class);
		NativeEntryPoint_make = find_static_method(jdk_internal_foreign_abi_NativeEntryPoint, "make", jdk_internal_foreign_abi_NativeEntryPoint,
				jdk_internal_foreign_abi_ABIDescriptor, jdk_internal_foreign_abi_VMStorage.arrayType(), jdk_internal_foreign_abi_VMStorage.arrayType(), MethodType.class, boolean.class, int.class, boolean.class);
		NativeMethodHandle_make = find_static_method(java_lang_invoke_NativeMethodHandle, "make", MethodHandle.class, jdk_internal_foreign_abi_NativeEntryPoint);
	}

	/**
	 * 为指定类型的函数创建stub函数并返回其指针。<br>
	 * JVM调用so库中的函数时，首先为目标函数类型创建一个能调用目标函数类型的C函数，即stub函数。stub函数比目标函数多了一个首参数，即要调用的目标函数指针的地址，类型为long。<br>
	 * stub函数中全部的C/C++指针类型均为long，Java类型则为对应的Class<?>。<br>
	 * stub函数创建完成以后，可以使用NativeEntryPoint.make()和NativeMethodHandle.make()创建stub函数的MethodHandle。<br>
	 * 但在FFM实现中这个Handle是隐藏类中定义的，不能直接访问，因为JVM的FFM API实现中要将指针地址绑定到MemorySegment，
	 * 故内部实现为jdk.internal.foreign.abi.BindingSpecializer。specializeHelper()在运行时生成字节码定义一个隐藏类，
	 * 而该隐藏类调用了NativeMethodHandle.make()生成的stub函数的句柄。<br>
	 * 
	 * @param method_type             JVM层stub方法的类型。如果是C++指针类型则传入long。第一个参数必须是long，代表函数指针地址，后面按顺序传入参数
	 * @param abi_descriptor          ABI描述符
	 * @param vm_storage_arg_moves    传入参数的保存位置
	 * @param vm_storage_return_moves 接收返回值的保存位置
	 * @param needs_return_buffer     返回值是否需要缓冲区
	 * @param captured_state_mask
	 * @param needs_transition
	 * @return
	 */
	// @formatter:off
	/* 例如，对于JNI_GetCreatedJavaVMs()函数生成的隐藏类字节码为   
	public final class jdk.internal.foreign.abi.DowncallStub
	  minor version: 0
	  major version: 65
	  flags: (0x0031) ACC_PUBLIC, ACC_FINAL, ACC_SUPER
	  this_class: #2                          // jdk/internal/foreign/abi/DowncallStub
	  super_class: #4                         // java/lang/Object
	  interfaces: 0, fields: 0, methods: 1, attributes: 1
	Constant pool:
	   #1 = Utf8               jdk/internal/foreign/abi/DowncallStub
	   #2 = Class              #1             // jdk/internal/foreign/abi/DowncallStub
	   #3 = Utf8               java/lang/Object
	   #4 = Class              #3             // java/lang/Object
	   #5 = Utf8               invoke
	   #6 = Utf8               (Ljava/lang/foreign/SegmentAllocator;Ljava/lang/foreign/MemorySegment;Ljava/lang/foreign/MemorySegment;ILjava/lang/foreign/MemorySegment;)I
	   #7 = Utf8               jdk/internal/foreign/abi/SharedUtils
	   #8 = Class              #7             // jdk/internal/foreign/abi/SharedUtils
	   #9 = Utf8               DUMMY_ARENA
	  #10 = Utf8               Ljava/lang/foreign/Arena;
	  #11 = NameAndType        #9:#10         // DUMMY_ARENA:Ljava/lang/foreign/Arena;
	  #12 = Fieldref           #8.#11         // jdk/internal/foreign/abi/SharedUtils.DUMMY_ARENA:Ljava/lang/foreign/Arena;
	  #13 = Utf8               jdk/internal/foreign/AbstractMemorySegmentImpl
	  #14 = Class              #13            // jdk/internal/foreign/AbstractMemorySegmentImpl
	  #15 = Utf8               sessionImpl
	  #16 = Utf8               ()Ljdk/internal/foreign/MemorySessionImpl;
	  #17 = NameAndType        #15:#16        // sessionImpl:()Ljdk/internal/foreign/MemorySessionImpl;
	  #18 = Methodref          #14.#17        // jdk/internal/foreign/AbstractMemorySegmentImpl.sessionImpl:()Ljdk/internal/foreign/MemorySessionImpl;
	  #19 = Utf8               jdk/internal/foreign/MemorySessionImpl
	  #20 = Class              #19            // jdk/internal/foreign/MemorySessionImpl
	  #21 = Utf8               acquire0
	  #22 = Utf8               ()V
	  #23 = NameAndType        #21:#22        // acquire0:()V
	  #24 = Methodref          #20.#23        // jdk/internal/foreign/MemorySessionImpl.acquire0:()V
	  #25 = Utf8               unboxSegment
	  #26 = Utf8               (Ljava/lang/foreign/MemorySegment;)J
	  #27 = NameAndType        #25:#26        // unboxSegment:(Ljava/lang/foreign/MemorySegment;)J
	  #28 = Methodref          #8.#27         // jdk/internal/foreign/abi/SharedUtils.unboxSegment:(Ljava/lang/foreign/MemorySegment;)J
	  #29 = Utf8               java/lang/invoke/MethodHandles
	  #30 = Class              #29            // java/lang/invoke/MethodHandles
	  #31 = Utf8               classData
	  #32 = Utf8               (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
	  #33 = NameAndType        #31:#32        // classData:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
	  #34 = Methodref          #30.#33        // java/lang/invoke/MethodHandles.classData:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
	  #35 = MethodHandle       6:#34          // REF_invokeStatic java/lang/invoke/MethodHandles.classData:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
	  #36 = Utf8               _
	  #37 = Utf8               Ljava/lang/Object;
	  #38 = NameAndType        #36:#37        // _:Ljava/lang/Object;
	  #39 = Dynamic            #0:#38         // #0:_:Ljava/lang/Object; 动态常量，通过BootstrapMethods表中索引为0的引导方法计算，名称为"_"，类型为java.lang.Object
	  #40 = Utf8               java/lang/invoke/MethodHandle
	  #41 = Class              #40            // java/lang/invoke/MethodHandle
	  #42 = Utf8               invokeExact
	  #43 = Utf8               (JJIJ)I
	  #44 = NameAndType        #42:#43        // invokeExact:(JJIJ)I
	  #45 = Methodref          #41.#44        // java/lang/invoke/MethodHandle.invokeExact:(JJIJ)I
	  #46 = Utf8               java/lang/foreign/Arena
	  #47 = Class              #46            // java/lang/foreign/Arena
	  #48 = Utf8               close
	  #49 = NameAndType        #48:#22        // close:()V
	  #50 = InterfaceMethodref #47.#49        // java/lang/foreign/Arena.close:()V
	  #51 = Utf8               release0
	  #52 = NameAndType        #51:#22        // release0:()V
	  #53 = Methodref          #20.#52        // jdk/internal/foreign/MemorySessionImpl.release0:()V
	  #54 = Utf8               Code
	  #55 = Utf8               StackMapTable
	  #56 = Utf8               java/lang/foreign/SegmentAllocator
	  #57 = Class              #56            // java/lang/foreign/SegmentAllocator
	  #58 = Utf8               java/lang/foreign/MemorySegment
	  #59 = Class              #58            // java/lang/foreign/MemorySegment
	  #60 = Utf8               java/lang/Throwable
	  #61 = Class              #60            // java/lang/Throwable
	  #62 = Utf8               BootstrapMethods
	{
	  // Java层用户调用的native函数的MethodHandle，实际上是调用了此invoke()方法，此方法内又调用了stub函数。
	  // 原函数签名为jint JNI_GetCreatedJavaVMs(JavaVM **vm_buf, jsize bufLen, jsize *numVMs)
	  // 可以看到，invoke()方法中的C/C++地址从stub方法的long变为了MemorySegment，这是Java层对native地址做的包装。
	  public static int invoke(java.lang.foreign.SegmentAllocator, java.lang.foreign.MemorySegment, java.lang.foreign.MemorySegment, int, java.lang.foreign.MemorySegment);
	    descriptor: (Ljava/lang/foreign/SegmentAllocator;Ljava/lang/foreign/MemorySegment;Ljava/lang/foreign/MemorySegment;ILjava/lang/foreign/MemorySegment;)I
	    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
	    Code:
	      stack=8, locals=17, args_size=5
	         0: aconst_null
	         1: astore        12
	         3: aconst_null
	         4: astore        13
	         6: aconst_null
	         7: astore        14
	         9: getstatic     #12                 // Field jdk/internal/foreign/abi/SharedUtils.DUMMY_ARENA:Ljava/lang/foreign/Arena;
	        12: astore        15
	        14: aload_1
	        15: dup
	        16: checkcast     #14                 // class jdk/internal/foreign/AbstractMemorySegmentImpl
	        19: invokevirtual #18                 // Method jdk/internal/foreign/AbstractMemorySegmentImpl.sessionImpl:()Ljdk/internal/foreign/MemorySessionImpl;
	        22: dup
	        23: invokevirtual #24                 // Method jdk/internal/foreign/MemorySessionImpl.acquire0:()V
	        26: astore        12
	        28: invokestatic  #28                 // Method jdk/internal/foreign/abi/SharedUtils.unboxSegment:(Ljava/lang/foreign/MemorySegment;)J
	        31: lstore        5
	        33: aload_2
	        34: dup
	        35: checkcast     #14                 // class jdk/internal/foreign/AbstractMemorySegmentImpl
	        38: invokevirtual #18                 // Method jdk/internal/foreign/AbstractMemorySegmentImpl.sessionImpl:()Ljdk/internal/foreign/MemorySessionImpl;
	        41: dup
	        42: aload         12
	        44: if_acmpeq     56
	        47: dup
	        48: invokevirtual #24                 // Method jdk/internal/foreign/MemorySessionImpl.acquire0:()V
	        51: astore        13
	        53: goto          57
	        56: pop
	        57: invokestatic  #28                 // Method jdk/internal/foreign/abi/SharedUtils.unboxSegment:(Ljava/lang/foreign/MemorySegment;)J
	        60: lstore        7
	        62: iload_3
	        63: istore        9
	        65: aload         4
	        67: dup
	        68: checkcast     #14                 // class jdk/internal/foreign/AbstractMemorySegmentImpl
	        71: invokevirtual #18                 // Method jdk/internal/foreign/AbstractMemorySegmentImpl.sessionImpl:()Ljdk/internal/foreign/MemorySessionImpl;
	        74: dup
	        75: aload         12
	        77: if_acmpeq     95
	        80: dup
	        81: aload         13
	        83: if_acmpeq     95
	        86: dup
	        87: invokevirtual #24                 // Method jdk/internal/foreign/MemorySessionImpl.acquire0:()V
	        90: astore        14
	        92: goto          96
	        95: pop
	        96: invokestatic  #28                 // Method jdk/internal/foreign/abi/SharedUtils.unboxSegment:(Ljava/lang/foreign/MemorySegment;)J
	        99: lstore        10
	       101: ldc           #39                 // Dynamic #0:_:Ljava/lang/Object; 注意！这里是运行时加载了常量池中MethodHandles.classData()获取的动态的#39对象入栈，该对象实际是stub函数的MethodHandle
	       103: checkcast     #41                 // class java/lang/invoke/MethodHandle 检查压入栈的#39是否是#41类型
	       106: lload         5                   // 目标函数指针地址
	       108: lload         7                   // vm_buf指针地址
	       110: iload         9                   // bufLen
	       112: lload         10                  // numVMs指针地址
	       114: invokevirtual #45                 // Method java/lang/invoke/MethodHandle.invokeExact:(JJIJ)I
	       117: istore        16
	       119: iload         16
	       121: aload         15
	       123: checkcast     #47                 // class java/lang/foreign/Arena
	       126: invokeinterface #50,  1           // InterfaceMethod java/lang/foreign/Arena.close:()V
	       131: aload         12
	       133: ifnull        141
	       136: aload         12
	       138: invokevirtual #53                 // Method jdk/internal/foreign/MemorySessionImpl.release0:()V
	       141: aload         13
	       143: ifnull        151
	       146: aload         13
	       148: invokevirtual #53                 // Method jdk/internal/foreign/MemorySessionImpl.release0:()V
	       151: aload         14
	       153: ifnull        161
	       156: aload         14
	       158: invokevirtual #53                 // Method jdk/internal/foreign/MemorySessionImpl.release0:()V
	       161: ireturn
	       162: aload         15
	       164: checkcast     #47                 // class java/lang/foreign/Arena
	       167: invokeinterface #50,  1           // InterfaceMethod java/lang/foreign/Arena.close:()V
	       172: aload         12
	       174: ifnull        182
	       177: aload         12
	       179: invokevirtual #53                 // Method jdk/internal/foreign/MemorySessionImpl.release0:()V
	       182: aload         13
	       184: ifnull        192
	       187: aload         13
	       189: invokevirtual #53                 // Method jdk/internal/foreign/MemorySessionImpl.release0:()V
	       192: aload         14
	       194: ifnull        202
	       197: aload         14
	       199: invokevirtual #53                 // Method jdk/internal/foreign/MemorySessionImpl.release0:()V
	       202: athrow
	      Exception table:
	         from    to  target type
	            14   121   162   any
	      StackMapTable: number_of_entries = 11
	        frame_type = 255 // full_frame
	          offset_delta = 56
	          locals = [ class java/lang/foreign/SegmentAllocator, class java/lang/foreign/MemorySegment, class java/lang/foreign/MemorySegment, int, class java/lang/foreign/MemorySegment, long, top, top, top, top, top, class jdk/internal/foreign/MemorySessionImpl, null, null, class java/lang/foreign/Arena ]
	          stack = [ class java/lang/foreign/MemorySegment, class jdk/internal/foreign/MemorySessionImpl ]
	        frame_type = 255 // full_frame
	          offset_delta = 0
	          locals = [ class java/lang/foreign/SegmentAllocator, class java/lang/foreign/MemorySegment, class java/lang/foreign/MemorySegment, int, class java/lang/foreign/MemorySegment, long, top, top, top, top, top, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, null, class java/lang/foreign/Arena ]
	          stack = [ class java/lang/foreign/MemorySegment ]
	        frame_type = 255 // full_frame
	          offset_delta = 37
	          locals = [ class java/lang/foreign/SegmentAllocator, class java/lang/foreign/MemorySegment, class java/lang/foreign/MemorySegment, int, class java/lang/foreign/MemorySegment, long, long, int, top, top, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, null, class java/lang/foreign/Arena ]
	          stack = [ class java/lang/foreign/MemorySegment, class jdk/internal/foreign/MemorySessionImpl ]
	        frame_type = 255 // full_frame
	          offset_delta = 0
	          locals = [ class java/lang/foreign/SegmentAllocator, class java/lang/foreign/MemorySegment, class java/lang/foreign/MemorySegment, int, class java/lang/foreign/MemorySegment, long, long, int, top, top, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, class java/lang/foreign/Arena ]
	          stack = [ class java/lang/foreign/MemorySegment ]
	        frame_type = 255 // full_frame
	          offset_delta = 44
	          locals = [ class java/lang/foreign/SegmentAllocator, class java/lang/foreign/MemorySegment, class java/lang/foreign/MemorySegment, int, class java/lang/foreign/MemorySegment, long, long, int, long, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, class java/lang/foreign/Arena, int ]
	          stack = [ int ]
	        frame_type = 73 // same_locals_1_stack_item
	          stack = [ int ]
	        frame_type = 73 // same_locals_1_stack_item
	          stack = [ int ]
	        frame_type = 255 // full_frame
	          offset_delta = 0
	          locals = [ class java/lang/foreign/SegmentAllocator, class java/lang/foreign/MemorySegment, class java/lang/foreign/MemorySegment, int, class java/lang/foreign/MemorySegment, top, top, top, top, top, top, top, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, class jdk/internal/foreign/MemorySessionImpl, class java/lang/foreign/Arena ]
	          stack = [ class java/lang/Throwable ]
	        frame_type = 83 // same_locals_1_stack_item
	          stack = [ class java/lang/Throwable ]
	        frame_type = 73 // same_locals_1_stack_item
	          stack = [ class java/lang/Throwable ]
	        frame_type = 73 // same_locals_1_stack_item
	          stack = [ class java/lang/Throwable ]
	}
	BootstrapMethods: //0号方法，计算stub函数的MethodHandle
	  0: #35 REF_invokeStatic java/lang/invoke/MethodHandles.classData:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;
	    Method arguments:
	    
	// 反编译后的源代码为
	
	package jdk.internal.foreign.abi;
	
	import java.lang.foreign.Arena;
	import java.lang.foreign.MemorySegment;
	import java.lang.foreign.SegmentAllocator;
	import java.lang.invoke.MethodHandle;
	import jdk.internal.foreign.AbstractMemorySegmentImpl;
	import jdk.internal.foreign.MemorySessionImpl;
	
	public final class DowncallStub {
	
	    public static int invoke(SegmentAllocator segmentallocator, MemorySegment func_ptr, MemorySegment vm_buf, int bufLen, MemorySegment numVMs) {
	        MemorySessionImpl memorysessionimpl = null;
	        MemorySessionImpl memorysessionimpl1 = null;
	        MemorySessionImpl memorysessionimpl2 = null;
	        Arena arena = SharedUtils.DUMMY_ARENA;
	        int invoke_return_value;
	        try {
	            MemorySessionImpl memorysessionimpl3 = ((AbstractMemorySegmentImpl) func_ptr).sessionImpl();
	            memorysessionimpl3.acquire0();
	            memorysessionimpl = memorysessionimpl3;
	            long func_ptr_address = SharedUtils.unboxSegment(func_ptr);
	            memorysessionimpl3 = ((AbstractMemorySegmentImpl) vm_buf).sessionImpl();
	            if (memorysessionimpl3 != memorysessionimpl) {
	                memorysessionimpl3.acquire0();
	                memorysessionimpl1 = memorysessionimpl3;
	            }
	            long vm_buf_address = SharedUtils.unboxSegment(vm_buf);
	            memorysessionimpl3 = ((AbstractMemorySegmentImpl) numVMs).sessionImpl();
	            if (memorysessionimpl3 != memorysessionimpl && memorysessionimpl3 != memorysessionimpl1) {
	                memorysessionimpl3.acquire0();
	                memorysessionimpl2 = memorysessionimpl3;
	            }
	            long numVMs_address = SharedUtils.unboxSegment(numVMs); // 获取numVMs数组的内存地址
	            int stub_return_value = ((MethodHandle) classData("_")).invokeExact(func_ptr_address, vm_buf_address, bufLen, numVMs_address); // 调用stub函数
	            invoke_return_value = stub_return_value;
	        } catch (Throwable throwable) {
	            ((Arena) arena).close();
	            if (memorysessionimpl != null) {
	                memorysessionimpl.release0();
	            }
	            if (memorysessionimpl1 != null) {
	                memorysessionimpl1.release0();
	            }
	            if (memorysessionimpl2 != null) {
	                memorysessionimpl2.release0();
	            }
	            throw throwable;
	        }
	        ((Arena) arena).close();
	        if (memorysessionimpl != null) {
	            memorysessionimpl.release0();
	        }
	        if (memorysessionimpl1 != null) {
	            memorysessionimpl1.release0();
	        }
	        if (memorysessionimpl2 != null) {
	            memorysessionimpl2.release0();
	        }
	        return invoke_return_value;
	    }
	}
	*/
	// @formatter:on
	public static final long downcall_stub(MethodType method_type, Object abi_descriptor, Object[] vm_storage_arg_moves_enc_arg_moves, Object[] vm_storage_enc_return_moves, boolean needs_return_buffer, int captured_state_mask, boolean needs_transition)
	{
		try
		{
			return (long) NativeEntryPoint_makeDowncallStub.invoke(method_type, abi_descriptor, vm_storage_arg_moves_enc_arg_moves, vm_storage_enc_return_moves, needs_return_buffer, captured_state_mask, needs_transition);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("get downcall stub of type '" + method_type.toString() + "' failed", ex);
		}
	}

	public static final boolean free_downcall_stub(long downcall_stub)
	{
		try
		{
			return (boolean) NativeEntryPoint_freeDowncallStub0.invokeExact(downcall_stub);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("free downcall stub '" + downcall_stub + "' failed", ex);
		}
	}

	/**
	 * 为指定类型的函数创建入口点stub并缓存。<br>
	 * 
	 * @param method_type             JVM层stub方法的类型。如果是C++指针类型则传入long。第一个参数必须是long，代表函数指针地址，后面按顺序传入参数
	 * @param abi_descriptor          ABI描述符
	 * @param vm_storage_arg_moves    传入参数的保存位置
	 * @param vm_storage_return_moves 接收返回值的保存位置
	 * @param needs_return_buffer     返回值是否需要缓冲区
	 * @param captured_state_mask
	 * @param needs_transition
	 * @return
	 */
	public static final Object native_entry(MethodType method_type, Object abi_descriptor, Object[] vm_storage_arg_moves, Object[] vm_storage_return_moves, boolean needs_return_buffer, int captured_state_mask, boolean needs_transition)
	{
		try
		{
			return NativeEntryPoint_make.invoke(abi_descriptor, vm_storage_arg_moves, vm_storage_return_moves, method_type, needs_return_buffer, captured_state_mask, needs_transition);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("wrap native entry point of type '" + method_type.toString() + "' failed", ex);
		}
	}

	/**
	 * JVM内部使用的ABIDescriptor对象，不同架构和平台ABI描述对象不同。 https://github.com/openjdk/jdk/blob/a69409b0b7bcb4eb9a66327e1c6c53b3361ea1e9/src/hotspot/cpu/x86/foreignGlobals_x86_64.cpp#L46
	 */
	public static enum abi
	{
		x86_64_CSysV("x64.sysv", "CallArranger", "CSysV"),
		x86_64_CWindows("x64.windows", "CallArranger", "CWindows"),
		aarch64_CLinux("aarch64", "CallArranger", "C", true, "LINUX", false),
		aarch64_CMacOS("aarch64", "CallArranger", "C", true, "MACOS", false),
		aarch64_CWindows("aarch64.windows", "WindowsAArch64CallArranger", "WindowsAArch64AbiDescriptor", true, "aarch64", "CallArranger", "WINDOWS", false),
		power_pc_64_C("ppc64", "CallArranger", "C", false, "ABIv2", false),
		riscv64_CLinux("riscv64.linux", "LinuxRISCV64CallArranger", "CLinux"),
		s390_CLinux("s390.linux", "LinuxS390CallArranger", "CLinux");

		public static final abi host;

		private static Class<?> jdk_internal_foreign_CABI;

		private static MethodHandle CABI_computeCurrent;

		static
		{
			try
			{
				jdk_internal_foreign_CABI = Class.forName("jdk.internal.foreign.CABI");
			}
			catch (ClassNotFoundException ex)
			{
				ex.printStackTrace();
			}
			CABI_computeCurrent = find_static_method(jdk_internal_foreign_CABI, "computeCurrent", jdk_internal_foreign_CABI);
		}

		/**
		 * 获取宿主机的CABI枚举值
		 * 
		 * @return
		 */
		@SuppressWarnings("rawtypes")
		public static final String host_cabi()
		{
			try
			{
				return ((Enum) (Object) CABI_computeCurrent.invoke()).name();
			}
			catch (Throwable ex)
			{
				throw new java.lang.InternalError("get host abi descriptor failed", ex);
			}
		}

		/**
		 * 获取宿主机的架构ABI
		 * 
		 * @return
		 */
		private static final abi get_host_abi_descriptor()
		{
			switch (host_cabi())
			{
			case "SYS_V":
				return x86_64_CSysV;
			case "WIN_64":
				return x86_64_CWindows;
			case "LINUX_AARCH_64":
				return aarch64_CLinux;
			case "MAC_OS_AARCH_64":
				return aarch64_CMacOS;
			case "WIN_AARCH_64":
				return aarch64_CWindows;
			case "LINUX_PPC_64_LE":
				return power_pc_64_C;
			case "LINUX_RISCV_64":
				return riscv64_CLinux;
			case "LINUX_S390":
				return s390_CLinux;
			case "FALLBACK":
			case "UNSUPPORTED":
			default:
				return null;
			}
		}

		static
		{
			host = get_host_abi_descriptor();
		}

		private Object _internal_descriptor = null;

		Class<?> _internal_abi_context_class;
		Class<?> _internal_call_arranger_class;
		Class<?> _internal_bindings_class;

		_get_bindings _get_bindings;

		private static abstract class _get_bindings
		{
			protected Object _internal_call_arranger_instance = null;
			protected MethodHandle CallArranger_getBindings;

			protected _get_bindings(Object _internal_call_arranger_instance, MethodHandle CallArranger_getBindings)
			{
				this._internal_call_arranger_instance = _internal_call_arranger_instance;
				this.CallArranger_getBindings = CallArranger_getBindings;
			}

			abstract Object call(FunctionDescriptor fd, boolean for_upcall, Object options);
		}

		private static class _static_get_bindings extends _get_bindings
		{
			protected _static_get_bindings(MethodHandle CallArranger_getBindings)
			{
				super(null, CallArranger_getBindings);
			}

			@Override
			Object call(FunctionDescriptor fd, boolean for_upcall, Object options)
			{
				try
				{
					return CallArranger_getBindings.invoke(fd.toMethodType(), fd, for_upcall, options);
				}
				catch (Throwable ex)
				{
					throw new java.lang.InternalError("static get bindings of '" + fd + "' failed", ex);
				}
			}
		}

		private static class _member_get_bindings extends _get_bindings
		{
			protected _member_get_bindings(Object _internal_call_arranger_instance, MethodHandle CallArranger_getBindings)
			{
				super(_internal_call_arranger_instance, CallArranger_getBindings);
			}

			@Override
			Object call(FunctionDescriptor fd, boolean for_upcall, Object options)
			{
				try
				{
					return CallArranger_getBindings.invoke(_internal_call_arranger_instance, fd.toMethodType(), fd, for_upcall, options);
				}
				catch (Throwable ex)
				{
					throw new java.lang.InternalError("member get bindings of '" + fd + "' failed", ex);
				}
			}
		}

		private abi(String abi_pkg_name, String abi_context_name, String abi_instance_name, boolean is_abi_static, String call_arranger_pkg_name, String call_arranger_name, String call_arranger_instance_name, boolean is_get_bindings_static)
		{
			try
			{
				// ABIDescriptor需要与CallArranger一起使用，CallArranger负责计算参数位置和重排以符合ABI
				_internal_abi_context_class = Class.forName("jdk.internal.foreign.abi." + abi_pkg_name + "." + abi_context_name);
				_internal_call_arranger_class = Class.forName("jdk.internal.foreign.abi." + call_arranger_pkg_name + "." + call_arranger_name);
				_internal_bindings_class = Class.forName(_internal_call_arranger_class.getName() + "$Bindings");
				Object _internal_call_arranger_instance = null;
				if (!is_abi_static || !is_get_bindings_static)
				{
					if (call_arranger_instance_name == null)
					{
						throw new java.lang.InternalError("call arranger instance name of '" + this.name() + "' cannot be null");
					}
					else
					{
						// CallArranger的实例一定是静态单例
						_internal_call_arranger_instance = unsafe.read_reference(_internal_call_arranger_class, call_arranger_instance_name);
					}
				}
				if (is_abi_static)
				{
					_internal_descriptor = unsafe.read_reference(_internal_abi_context_class, abi_instance_name);
				}
				else
				{
					Field _internal_descriptor_field = reflection.find_declared_field(_internal_abi_context_class, abi_instance_name);
					_internal_descriptor = unsafe.read_reference(_internal_call_arranger_instance, _internal_descriptor_field);
				}
				if (is_get_bindings_static)
				{
					// getBindings()为静态方法
					_get_bindings = new _static_get_bindings(find_static_method(_internal_call_arranger_class, "getBindings", _internal_bindings_class, MethodType.class, FunctionDescriptor.class, boolean.class, jdk_internal_foreign_abi_LinkerOptions));
				}
				else
				{
					// getBindings()为实例方法
					_get_bindings = new _member_get_bindings(_internal_call_arranger_instance, find_special_method(_internal_call_arranger_class, "getBindings", _internal_bindings_class, MethodType.class, FunctionDescriptor.class, boolean.class, jdk_internal_foreign_abi_LinkerOptions));
				}
			}
			catch (ClassNotFoundException ex)
			{
				throw new java.lang.InternalError("get internal abi descriptor of '" + this.name() + "' failed", ex);
			}
		}

		private abi(String abi_pkg_name, String abi_context_name, String abi_instance_name, String call_arranger_pkg_name, String call_arranger_name, String call_arranger_instance_name)
		{
			this(abi_pkg_name, abi_context_name, abi_instance_name, true, call_arranger_pkg_name, call_arranger_name, call_arranger_instance_name, true);
		}

		private abi(String abi_call_arranger_pkg_name, String abi_context_call_arranger_name, String abi_instance_name, boolean is_abi_static, String call_arranger_instance_name, boolean is_get_bindings_static)
		{
			this(abi_call_arranger_pkg_name, abi_context_call_arranger_name, abi_instance_name, is_abi_static, abi_call_arranger_pkg_name, abi_context_call_arranger_name, call_arranger_instance_name, is_get_bindings_static);
		}

		private abi(String abi_call_arranger_pkg_name, String abi_context_call_arranger_name, String static_abi_instance_name, String static_call_arranger_instance_name)
		{
			this(abi_call_arranger_pkg_name, abi_context_call_arranger_name, static_abi_instance_name, true, static_call_arranger_instance_name, true);
		}

		private abi(String abi_call_arranger_pkg_name, String abi_context_call_arranger_name, String static_abi_instance_name)
		{
			this(abi_call_arranger_pkg_name, abi_context_call_arranger_name, static_abi_instance_name, null);
		}

		public final Object descriptor()
		{
			return _internal_descriptor;
		}

		private static MethodHandle LinkerOptions_constructor;

		static
		{
			LinkerOptions_constructor = find_constructor(jdk_internal_foreign_abi_LinkerOptions, Map.class);
		}

		/**
		 * 创建jdk.internal.foreign.abi.LinkerOptions对象
		 * 
		 * @param options
		 * @return
		 */
		public static Object link_options(Map<Class<?>, Linker.Option> options)
		{
			try
			{
				return LinkerOptions_constructor.invoke(options);
			}
			catch (Throwable ex)
			{
				throw new java.lang.InternalError("create link options '" + options + "' failed", ex);
			}
		}

		/**
		 * 无特殊的Linker设置
		 */
		public static final Object none_link_options;

		static
		{
			none_link_options = link_options(Map.of());
		}

		public static class constraint
		{
			private static final long __klass_word;

			private static Class<?> jdk_internal_foreign_abi_CallingSequence;
			private static Class<?> jdk_internal_foreign_abi_Binding$Move;

			private static VarHandle CallingSequence_calleeMethodType;
			private static VarHandle CallingSequence_needsReturnBuffer;
			private static VarHandle CallingSequence_returnBufferSize;
			private static VarHandle CallingSequence_allocationSize;

			private static MethodHandle CallingSequence_needsTransition;
			private static MethodHandle CallingSequence_capturedStateMask;

			private static VarHandle CallingSequence_returnBindings;
			private static VarHandle CallingSequence_argumentBindings;

			private static MethodHandle Binding$Move_storage;

			static
			{
				try
				{
					jdk_internal_foreign_abi_CallingSequence = Class.forName("jdk.internal.foreign.abi.CallingSequence");
					jdk_internal_foreign_abi_Binding$Move = Class.forName("jdk.internal.foreign.abi.Binding$Move");
				}
				catch (ClassNotFoundException ex)
				{
					ex.printStackTrace();
				}
				__klass_word = java_type.get_klass_word(constraint.class);

				CallingSequence_calleeMethodType = find_var(jdk_internal_foreign_abi_CallingSequence, "calleeMethodType", MethodType.class);
				CallingSequence_needsReturnBuffer = find_var(jdk_internal_foreign_abi_CallingSequence, "needsReturnBuffer", boolean.class);
				CallingSequence_returnBufferSize = find_var(jdk_internal_foreign_abi_CallingSequence, "returnBufferSize", long.class);
				CallingSequence_allocationSize = find_var(jdk_internal_foreign_abi_CallingSequence, "allocationSize", long.class);
				CallingSequence_needsTransition = find_special_method(jdk_internal_foreign_abi_CallingSequence, "needsTransition", boolean.class);
				CallingSequence_capturedStateMask = find_special_method(jdk_internal_foreign_abi_CallingSequence, "capturedStateMask", int.class);

				CallingSequence_returnBindings = find_var(jdk_internal_foreign_abi_CallingSequence, "returnBindings", List.class);
				CallingSequence_argumentBindings = find_var(jdk_internal_foreign_abi_CallingSequence, "argumentBindings", List.class);

				Binding$Move_storage = find_virtual_method(jdk_internal_foreign_abi_Binding$Move, "storage", jdk_internal_foreign_abi_VMStorage);
			}

			/**
			 * 获取jdk.internal.foreign.abi.Binding.Move的storage()参数
			 * 
			 * @param move
			 * @return
			 */
			public static final Object get_binding_move_storage(Object move)
			{
				try
				{
					return Binding$Move_storage.invoke(move);
				}
				catch (Throwable ex)
				{
					throw new java.lang.InternalError("get storage of '" + move + "' failed", ex);
				}
			}

			private Object calling_sequence;
			private boolean is_in_memory_return;

			private constraint()
			{
				throw new java.lang.InstantiationError("constraint should not be instantiated");
			}

			public String toString()
			{
				return calling_sequence.toString();
			}

			/**
			 * 从CallArranger.Bindings转换为constraint
			 * 
			 * @param bindings
			 * @return
			 */
			public static constraint __from(Object bindings)
			{
				return (constraint) java_type.cast(bindings, __klass_word);
			}

			public final Object calling_sequence()
			{
				return calling_sequence;
			}

			public final boolean is_in_memory_return()
			{
				return is_in_memory_return;
			}

			public final MethodType stub_method_type()
			{
				return (MethodType) CallingSequence_calleeMethodType.get(calling_sequence);
			}

			public final boolean needs_return_buffer()
			{
				return (boolean) CallingSequence_needsReturnBuffer.get(calling_sequence);
			}

			public final long return_buffer_size()
			{
				return (long) CallingSequence_returnBufferSize.get(calling_sequence);
			}

			public final long allocation_size()
			{
				return (long) CallingSequence_allocationSize.get(calling_sequence);
			}

			public final boolean needs_transition()
			{
				try
				{
					return (boolean) CallingSequence_needsTransition.invoke(calling_sequence);
				}
				catch (Throwable ex)
				{
					throw new java.lang.InternalError("check needs transition of '" + calling_sequence + "' failed", ex);
				}
			}

			public final int captured_state_mask()
			{
				try
				{
					return (int) CallingSequence_capturedStateMask.invoke(calling_sequence);
				}
				catch (Throwable ex)
				{
					throw new java.lang.InternalError("get captured state mask of '" + calling_sequence + "' failed", ex);
				}
			}

			/**
			 * 获取返回值的VMStorage[]
			 * 
			 * @return
			 */
			public final Object[] ret_vm_storage()
			{
				List<Object> ret_bindings = (List<Object>) CallingSequence_returnBindings.get(calling_sequence);
				return ret_bindings.stream()
						.filter(jdk_internal_foreign_abi_Binding$Move::isInstance)
						.map(constraint::get_binding_move_storage)
						.toArray(virtual_machine.storage._new);
			}

			/**
			 * 获取参数的VMStorage[]
			 * 
			 * @return
			 */
			public final Object[] args_vm_storage()
			{
				List<List<Object>> args_bindings = (List<List<Object>>) CallingSequence_argumentBindings.get(calling_sequence);
				return args_bindings.stream().flatMap(List::stream)
						.filter(jdk_internal_foreign_abi_Binding$Move::isInstance)
						.map(constraint::get_binding_move_storage)
						.toArray(virtual_machine.storage._new);
			}
		}

		/**
		 * 为函数的参数进行C->Java的类型替换处理和重排以符合ABI标准
		 * 
		 * @param fd
		 * @param for_upcall 对于调用C/C++的downcall为false，否则为true
		 * @param options
		 * @return
		 */
		public final constraint resolve_constraints(FunctionDescriptor fd, boolean for_upcall, Object options)
		{
			return constraint.__from(_get_bindings.call(fd, for_upcall, options));

		}

		public final constraint resolve_constraints(FunctionDescriptor fd)
		{
			return resolve_constraints(fd, false, none_link_options);
		}

		public final Object native_entry(constraint resolved_constraint)
		{
			return symbols.native_entry(resolved_constraint.stub_method_type(),
					this.descriptor(),
					resolved_constraint.args_vm_storage(),
					resolved_constraint.ret_vm_storage(),
					resolved_constraint.needs_return_buffer(),
					resolved_constraint.captured_state_mask(),
					resolved_constraint.needs_transition());
		}

	}

	public static final MethodHandle native_function(Object downcall_entry)
	{
		try
		{
			return (MethodHandle) NativeMethodHandle_make.invoke(downcall_entry);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("wrap native method handle of '" + downcall_entry.toString() + "' failed", ex);
		}
	}

	public static final MethodHandle native_function(Object downcall_entry, long func_ptr)
	{
		return null;
		// return native_function(downcall_entry).(func_ptr);
	}

	/**
	 * 方法包装对象，可以修改方法调用相关参数和标志。
	 */
	public static final class callable
	{
		private int flags;
		private Class<?> target_class;
		private MethodHandle warpped_method;
		private Object member_name;
		private byte invoke_bytecode;

		private callable(Class<?> target_class, String target_method_name, Class<?>... arg_types)
		{
			this.target_class = target_class;
			MethodHandle m = null;
			if (target_method_name.equals(symbols.CONSTRUCTOR_NAME))
				m = constructor(target_class, arg_types);// 目标函数是构造函数则将构造函数转换为可当作方法调用的构造函数
			else
				m = symbols.find_method(target_class, target_method_name, arg_types);
			this.member_name = symbols.member_name(m);
			this.flags = symbols.member_name_flags(member_name);
			this.invoke_bytecode = symbols.reference_kind(member_name);
		}

		/**
		 * 获取一个可以被当作实例方法调用的构造函数。
		 * 
		 * @param target_class
		 * @param arg_types
		 * @return
		 */
		public static final MethodHandle constructor(Class<?> target_class, Class<?>... arg_types)
		{
			Object member_name = symbols.member_name(symbols.find_constructor(target_class, arg_types));
			int flags = symbols.member_name_flags(member_name);
			flags = symbols.set_flag(flags, symbols.IS_CONSTRUCTOR, false);// 取消构造函数标志
			flags = symbols.set_flag(flags, symbols.IS_METHOD, true);// 添加普通方法标志
			symbols.set_member_name_flags(member_name, flags);
			return symbols.direct_method(symbols.constants.REF_invokeVirtual, target_class, member_name);
		}

		/**
		 * 从一个可调用对象绑定字节码。
		 * 
		 * @param clazz
		 * @param target_method_name
		 * @param arg_types
		 * @return
		 */
		public static final callable bind(Class<?> clazz, String target_method_name, Class<?>... arg_types)
		{
			return new callable(clazz, target_method_name, arg_types);
		}

		/**
		 * 设置标志
		 * 
		 * @param flag
		 * @param mark
		 * @return
		 */
		public callable set_flag(int flag, boolean mark)
		{
			this.flags = symbols.set_flag(this.flags, flag, mark);
			return this;
		}

		/**
		 * 目标方法是否是CallerSensitive的，由于检查是在C++层的constMethod，因此为MethodHandle设置该标志无效。
		 * 
		 * @param mark
		 * @return
		 */
		public boolean is_caller_sensitive(boolean mark)
		{
			return symbols.is_caller_sensitive(member_name);
		}

		/**
		 * 设置标志并包装symbols为MethodHandle
		 */
		public callable warp()
		{
			symbols.set_member_name_flags(member_name, flags);
			warpped_method = symbols.direct_method(invoke_bytecode, target_class, member_name);
			return this;
		}

		/**
		 * 返回包装好的可调用对象的MethodHandle
		 * 
		 * @return
		 */
		public MethodHandle unwarp()
		{
			return warpped_method;
		}

		/**
		 * 调用该可调用对象
		 * 
		 * @param args
		 * @return
		 */
		public Object call(Object... args)
		{
			try
			{
				return warpped_method.invokeWithArguments(args);
			}
			catch (Throwable ex)
			{
				throw new java.lang.InternalError("call callable '" + member_name.toString() + "' failed", ex);
			}
		}
	}
}

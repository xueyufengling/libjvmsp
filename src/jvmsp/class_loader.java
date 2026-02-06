package jvmsp;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import jvmsp.internal.iterate_on_write_list;

public class class_loader
{
	private static MethodHandle ClassLoader_defineClass1;
	private static MethodHandle ClassLoader_defineClass;
	private static MethodHandle ClassLoader_findClass;

	public static final String DEFAULT_PARENT_CLASS_LOADER_NAME = "parent";

	static
	{
		ClassLoader_defineClass1 = symbols.find_static_method(ClassLoader.class, "defineClass1", Class.class, ClassLoader.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class, String.class);
		ClassLoader_defineClass = symbols.find_special_method(ClassLoader.class, "defineClass", Class.class, String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
		ClassLoader_findClass = symbols.find_special_method(ClassLoader.class, "findClass", Class.class, String.class);
	}

	@FunctionalInterface
	public static interface bytecode_source
	{
		/**
		 * 根据类名查找字节码
		 * 
		 * @param class_name
		 * @return
		 */
		public byte[] gen_bytecode(String class_name);

		public static final class map implements bytecode_source
		{
			public final HashMap<String, byte[]> klassDefs;

			public map(HashMap<String, byte[]> klassDefs)
			{
				this.klassDefs = klassDefs;
			}

			@Override
			public byte[] gen_bytecode(String class_name)
			{
				return klassDefs.get(class_name);
			}

			public static final map from(HashMap<String, byte[]> klassDefs)
			{
				return new map(klassDefs);
			}
		}

		public static map as_map(bytecode_source source)
		{
			return (map) source;
		}
	}

	public static final class proxy extends ClassLoader
	{
		private ClassLoader son;
		private bytecode_source def_bytecode_source;
		private boolean reverse_loading = false;// 记录是否已经开始向下查找，防止在整个加载链中都查找不到无限循环loadClass()

		public proxy(ClassLoader dest, String dest_parent_field_name, bytecode_source source)
		{
			super(parent(dest, dest_parent_field_name));
			class_loader.set_parent(dest, dest_parent_field_name, this);
			this.son = dest;
			this.def_bytecode_source = source;
		}

		/**
		 * Proxy将插入双亲委托加载链的dest的上方
		 * 
		 * @param dest            目标ClassLoader
		 * @param undefined_class 要加载的字节码
		 */
		public proxy(ClassLoader dest, String dest_parent_field_name, HashMap<String, byte[]> undefined_class)
		{
			this(dest, dest_parent_field_name, bytecode_source.map.from(undefined_class));
		}

		public proxy(ClassLoader dest, bytecode_source source)
		{
			this(dest, DEFAULT_PARENT_CLASS_LOADER_NAME, source);
		}

		public proxy(ClassLoader dest, HashMap<String, byte[]> undefined_class)
		{
			this(dest, DEFAULT_PARENT_CLASS_LOADER_NAME, undefined_class);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException
		{
			byte[] byte_code = def_bytecode_source.gen_bytecode(name);
			// 代理及之上的类加载器找不到类定义时，则让子类加载器son去加载目标类，这样动态加载的类就可以引用son加载的类
			if (byte_code == null)
			{
				if (reverse_loading)
				{
					reverse_loading = false;
					return null;
				}
				else
				{
					reverse_loading = true;
					return son.loadClass(name);
				}
			}
			return defineClass(name, byte_code, 0, byte_code.length);
		}

		public final Class<?> load(String className)
		{
			try
			{
				return son.loadClass(className);
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			return null;
		}

		public final bytecode_source bytecode_source()
		{
			return def_bytecode_source;
		}

		/**
		 * @param dest
		 * @param undefined_class
		 * @return
		 */
		public static final proxy attach(ClassLoader dest, HashMap<String, byte[]> undefined_class)
		{
			return new proxy(dest, undefined_class);
		}

		public static final proxy attach(ClassLoader dest, String dest_parent_field_name, HashMap<String, byte[]> undefined_class)
		{
			return new proxy(dest, dest_parent_field_name, undefined_class);
		}

		public static final proxy attach(ClassLoader dest, bytecode_source source)
		{
			return new proxy(dest, source);
		}

		public static final proxy attach(ClassLoader dest, String dest_parent_field_name, bytecode_source source)
		{
			return new proxy(dest, dest_parent_field_name, source);
		}
	}

	public static final class class_definition
	{
		public final ClassLoader loader;
		public final String name;
		public final byte[] b;
		public final int off;
		public final int len;
		public final ProtectionDomain pd;
		public final String source;

		private class_definition(ClassLoader loader, String name, byte[] b, int off, int len, ProtectionDomain pd, String source)
		{
			this.loader = loader;
			this.name = name;
			this.b = b;
			this.off = off;
			this.len = len;
			this.pd = pd;
			this.source = source;
		}

		public static final class_definition of(ClassLoader loader, String name, byte[] b, int off, int len, ProtectionDomain pd, String source)
		{
			return new class_definition(loader, name, b, off, len, pd, source);
		}
	}

	private static Field Class_classLoader;

	static
	{
		Class_classLoader = reflection.no_field_filter_find(Class.class, "classLoader");
	}

	/**
	 * 设置Class的classLoader变量
	 * 
	 * @param clazz
	 * @param loader
	 * @return
	 */
	public static final Class<?> set_class_loader(Class<?> clazz, ClassLoader loader)
	{
		unsafe.write(clazz, Class_classLoader, loader);
		return clazz;
	}

	/**
	 * 不经过安全检查直接获取classLoader
	 * 
	 * @param target
	 * @param parent
	 * @return
	 */
	public static final ClassLoader get_class_loader(Class<?> clazz)
	{
		return (ClassLoader) reflection.read(clazz, Class_classLoader);
	}

	/**
	 * 将类设置为系统类
	 * 
	 * @param clazz
	 */
	public static final void as_bootstrap(Class<?> clazz)
	{
		set_class_loader(clazz, null);// 将clazz的类加载器设置为BootstrapClassLoader
	}

	public static final void as_bootstrap(Field f)
	{
		as_bootstrap(f.getDeclaringClass());
	}

	public static final void as_bootstrap(Executable e)
	{
		as_bootstrap(e.getDeclaringClass());
	}

	private static final iterate_on_write_list<class_definition> class_defs = new iterate_on_write_list<>();

	public static final void define(Collection<class_definition> defs) throws Throwable
	{
		class_defs.add(defs);
		class_defs.foreach((class_definition def) ->
		{
			class_loader.define(def);
		});
		class_defs.clear();
	}

	/**
	 * 将undefined_class委托给父类为loader的新自定义ClassLoader加载。<br>
	 * 注意，类加载的起点始终是手动调用的Class.forName(name,init,classLoader)、classLoader.loadClass(name)或直接使用该类型，例如直接在代码中使用{@code A a=new A();}的上下文的ClassLoader。<br>
	 * 类搜寻只会从起点开始，一直向上查找直到BootstrapClassLoader，而不会去查找起点ClassLoader的子代ClassLoader。<br>
	 * 因此，调用该方法返回的ClassLoader需要用户手动加载相关类，并且用反射使用加载的类，不能直接在代码中使用这些类型。<br>
	 * 
	 * @param loader
	 * @param undefined_class
	 * @return
	 */
	public static final ClassLoader new_class_loader(ClassLoader loader, HashMap<String, byte[]> undefined_class)
	{
		return new ClassLoader(loader)
		{
			private HashMap<String, byte[]> class_defs = undefined_class;

			@Override
			protected Class<?> findClass(String name) throws ClassNotFoundException
			{
				byte[] byte_code = class_defs.get(name);
				if (byte_code == null)
					throw new ClassNotFoundException(name);
				return defineClass(name, byte_code, 0, byte_code.length);
			}
		};
	}

	public static final ClassLoader new_class_loader(HashMap<String, byte[]> undefined_class)
	{
		Class<?> caller = reflection.caller_class();
		return new_class_loader(caller.getClassLoader(), undefined_class);
	}

	/**
	 * 获取指定类加载器的指定字段名称的父加载器. 一般父加载器都是final字段，而final字段只能使用Unsafe更改，不可使用VarHandle。
	 * 
	 * @param loader
	 * @param search_class           要搜寻字段的类，此类必须是loader本类及其父类
	 * @param dest_parent_field_name 要搜寻的父加载器字段名称
	 * @return
	 */
	public static final ClassLoader parent(ClassLoader loader, Class<? extends ClassLoader> search_class, String dest_parent_field_name)
	{
		return (ClassLoader) reflection.read(loader, reflection.no_field_filter_find(search_class, dest_parent_field_name));
	}

	public static final ClassLoader parent(ClassLoader loader, String dest_parent_field_name)
	{
		return parent(loader, loader.getClass(), dest_parent_field_name);
	}

	/**
	 * 获取Java标准的父加载器，即字段parent。 一般自定义类加载器其父加载器有可能不是parent，而是用户自定义的字段，此时需要在此传入实际使用的父加载器字段名称
	 * 
	 * @param loader
	 * @return
	 */
	public static final ClassLoader parent(ClassLoader loader)
	{
		return parent(loader, ClassLoader.class, DEFAULT_PARENT_CLASS_LOADER_NAME);
	}

	/**
	 * 为ClassLoader设置父加载器
	 * 
	 * @param target                 目标ClassLoader
	 * @param dest_parent_field_name 目标ClassLoader使用的父类加载器的字段名，当没有使用ClassLoader.parent成员构建加载委托链时需要指定实际的字段名称
	 * @param parent
	 * @return
	 */
	public static final ClassLoader set_parent(ClassLoader target, Class<? extends ClassLoader> search_class, String dest_parent_field_name, ClassLoader parent)
	{
		reflection.write(target, reflection.no_field_filter_find(target.getClass(), dest_parent_field_name), parent);
		return target;
	}

	public static final ClassLoader set_parent(ClassLoader target, String dest_parent_field_name, ClassLoader parent)
	{
		return set_parent(target, target.getClass(), dest_parent_field_name, parent);
	}

	/**
	 * 设置Java标准的父加载器
	 * 
	 * @param target
	 * @param parent
	 * @return
	 */
	public static final ClassLoader set_parent(ClassLoader target, ClassLoader parent)
	{
		return set_parent(target, ClassLoader.class, DEFAULT_PARENT_CLASS_LOADER_NAME, parent);
	}

	public static final Class<?> define(ClassLoader loader, String name, byte[] b, int off, int len, ProtectionDomain protection_domain, String source) throws ClassFormatError
	{
		try
		{
			return (Class<?>) ClassLoader_defineClass1.invokeExact(loader, name, b, off, len, protection_domain, source);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("define class '" + name + "' failed", ex);
		}
	}

	public static final Class<?> define(class_definition def) throws ClassFormatError
	{
		return define(def.loader, def.name, def.b, def.off, def.len, def.pd, def.source);
	}

	/**
	 * 从指定的dClassLoader加载class
	 * 
	 * @param loader
	 * @param name
	 * @param b
	 * @param off
	 * @param len
	 * @param protection_domain
	 * @return
	 * @throws ClassFormatError
	 */
	public static final Class<?> define(ClassLoader loader, String name, byte[] b, int off, int len, ProtectionDomain protection_domain) throws ClassFormatError
	{
		try
		{
			return (Class<?>) ClassLoader_defineClass.invokeExact(loader, name, b, off, len, protection_domain);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("define class '" + name + "' failed", ex);
		}
	}

	/**
	 * 从指定stack_skip对应的类的ClassLoader加载class
	 * 
	 * @param stack_skip
	 * @param name
	 * @param b
	 * @param off
	 * @param len
	 * @param protection_domain
	 * @return
	 * @throws ClassFormatError
	 */
	public static final Class<?> define(int stack_skip, String name, byte[] b, int off, int len, ProtectionDomain protection_domain) throws ClassFormatError
	{
		return define(reflection.unwind_class(stack_skip).getClassLoader(), name, b, off, len, protection_domain);
	}

	/**
	 * 从指定的dClassLoader加载class
	 * 
	 * @param loader
	 * @param name
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws ClassFormatError
	 */
	public static final Class<?> define(ClassLoader loader, String name, byte[] b, int off, int len) throws ClassFormatError
	{
		return define(loader, name, b, off, len, null);
	}

	public static final Class<?> define(ClassLoader loader, String name, byte[] b) throws ClassFormatError
	{
		return define(loader, name, b, 0, b.length);
	}

	/**
	 * 查找类
	 * 
	 * @param loader
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static final Class<?> find(ClassLoader loader, String name) throws ClassNotFoundException
	{
		try
		{
			return (Class<?>) ClassLoader_findClass.invokeExact(loader, name);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("define class '" + name + "' failed", ex);
		}
	}

	public static final ClassLoader caller_class_loader()
	{
		return reflection.unwind_class(4).getClassLoader();
	}

	/**
	 * 加载类（包括未使用的类）并决定是否初始化
	 * 
	 * @param loader
	 * @param init
	 * @param class_names
	 */
	public static final void load(ClassLoader loader, boolean init, String... class_names)
	{
		for (String clazz : class_names)
		{
			reflection.find_class(clazz, init, loader);
		}
	}

	public static final void load(boolean init, String... class_names)
	{
		ClassLoader loader = caller_class_loader();
		load(loader, init, class_names);
	}

	/**
	 * 加载并初始化类
	 * 
	 * @param class_names
	 */
	public static final void load(String... class_names)
	{
		ClassLoader loader = caller_class_loader();
		load(loader, true, class_names);
	}

	public static final void load(Class<?> any_class_in_package, file_system.uri.resolver resolver, boolean init, String start_path, boolean include_subpackage)
	{
		List<String> class_names = reflection.class_names_in_package(any_class_in_package, resolver, start_path, include_subpackage);
		ClassLoader loader = any_class_in_package.getClassLoader();
		for (String clazz : class_names)
		{
			reflection.find_class(clazz, init, loader);
		}
	}

	public static final void load(Class<?> any_class_in_package, boolean init, String start_path, boolean include_subpackage)
	{
		load(any_class_in_package, file_system.uri.resolver.DEFAULT, init, start_path, include_subpackage);
	}

	public static final void load(boolean init, String start_path, boolean include_subpackage)
	{
		Class<?> caller = reflection.caller_class();
		load(caller, init, start_path, include_subpackage);
	}

	/**
	 * 加载并初始化类
	 * 
	 * @param class_names
	 */
	public static final void load(String start_path, boolean include_subpackage)
	{
		Class<?> caller = reflection.caller_class();
		load(caller, true, start_path, include_subpackage);
	}

	/**
	 * 获取一个类加载器加载的类，只包含它本身的类，不包含其委托给父类加载的类。<br>
	 * 注意，该数组原则上只有JVM内部使用，它不是线程安全的，该方法得到的引用不可直接访问或操作，否则会抛出并行修改错误。<br>
	 * 需要使用可以访问的数组，请使用{@code loadedClassesCopy()}
	 * 
	 * @param loader
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final ArrayList<Class<?>> loaded_classes(ClassLoader loader)
	{
		if (loader != null)
			return (ArrayList<Class<?>>) reflection.read(loader, "classes");
		return null;
	}

	@SuppressWarnings("unchecked")
	public static final ArrayList<Class<?>> loaded_classes_copy(ClassLoader loader)
	{
		return (ArrayList<Class<?>>) loaded_classes(loader).clone();
	}

	/**
	 * 获取已经加载的包列表
	 * 
	 * @return
	 */
	public static final String[] loaded_packages(ClassLoader loader)
	{
		Package[] packages = loader.getDefinedPackages();// 获取调用该方法的类
		if (packages == null)
			return null;
		String[] package_names = new String[packages.length];
		for (int i = 0; i < packages.length; ++i)
		{
			package_names[i] = packages[i].getName();
		}
		return package_names;
	}

	public static final String[] loaded_packages()
	{
		Class<?> caller = reflection.caller_class();
		return loaded_packages(caller.getClassLoader());
	}

	/**
	 * 加载JAR文件中的类
	 */

	/**
	 * 加载指定jar中的所有类
	 * 
	 * @param loader
	 * @param jar
	 */
	public static final ClassLoader load_jar(ClassLoader loader, InputStream... jars)
	{
		return class_loader.proxy.attach(loader, DEFAULT_PARENT_CLASS_LOADER_NAME, file_system.collect_class(jars));
	}

	public static final ClassLoader load_jar(ClassLoader loader, byte[]... multi_jar_bytes)
	{
		return load_jar(loader, file_system.jar_streams(multi_jar_bytes));
	}

	/**
	 * 加载jar子包中的类
	 * 
	 * @param loader
	 * @param jar
	 * @param package_name
	 * @param include_subpackage
	 */
	public static final ClassLoader load_jar(ClassLoader loader, String package_name, boolean include_subpackage, InputStream... jars)
	{
		return class_loader.proxy.attach(loader, DEFAULT_PARENT_CLASS_LOADER_NAME, file_system.collect_class(package_name, include_subpackage, jars));
	}

	public static final ClassLoader load_jar(ClassLoader loader, String package_name, boolean include_subpackage, byte[]... multi_jar_bytes)
	{
		return load_jar(loader, file_system.jar_streams(multi_jar_bytes));
	}

	/**
	 * 从调用该方法的类所属的ClassLoader加载目标jar中的全部类
	 * 
	 * @param jar
	 */
	public static final ClassLoader load_jar(InputStream... jars)
	{
		Class<?> caller = reflection.caller_class();
		return load_jar(caller.getClassLoader(), jars);
	}

	public static final ClassLoader load_jar(byte[]... multi_jar_bytes)
	{
		Class<?> caller = reflection.caller_class();
		return load_jar(caller.getClassLoader(), file_system.jar_streams(multi_jar_bytes));
	}

	public static final ClassLoader load_jar(String... jar_paths)
	{
		Class<?> caller = reflection.caller_class();
		return load_jar(caller.getClassLoader(), file_system.jar_streams(caller, jar_paths));
	}

	/**
	 * 从调用该方法的类所属的ClassLoader加载目标jar中指定路径下的类
	 * 
	 * @param jar
	 * @param package_name
	 * @param include_subpackage
	 */
	public static final ClassLoader load_jar(String package_name, boolean include_subpackage, InputStream... jars)
	{
		Class<?> caller = reflection.caller_class();
		return load_jar(caller.getClassLoader(), package_name, include_subpackage, jars);
	}

	public static final ClassLoader load_jar(String package_name, boolean include_subpackage, byte[]... multi_jar_bytes)
	{
		Class<?> caller = reflection.caller_class();
		return load_jar(caller.getClassLoader(), package_name, include_subpackage, file_system.jar_streams(multi_jar_bytes));
	}

	public static final ClassLoader load_jar(String package_name, boolean include_subpackage, String... entry_jar_paths)
	{
		Class<?> caller = reflection.caller_class();
		return load_jar(caller.getClassLoader(), package_name, include_subpackage, file_system.jar_streams(caller, entry_jar_paths));
	}

	private static Field ClassLoader_libraries;// 原字段为final字段，故使用反射+unsafe
	private static Class<?> jdk_internal_loader_BootLoader;

	private static Field BootLoader_getNativeLibraries;

	static
	{
		ClassLoader_libraries = reflection.find_declared_field(ClassLoader.class, "libraries");
		jdk_internal_loader_BootLoader = reflection.find_class("jdk.internal.loader.BootLoader");
		BootLoader_getNativeLibraries = reflection.find_declared_field(jdk_internal_loader_BootLoader, "NATIVE_LIBS");
	}

	public static final Object get_bootstrap_libraries()
	{
		return unsafe.read_reference(null, BootLoader_getNativeLibraries);
	}

	public static final void set_bootstrap_libraries(Object libraries)
	{
		unsafe.write(null, BootLoader_getNativeLibraries, libraries);
	}

	public static final void set_libraries(ClassLoader loader, Object libraries)
	{
		if (loader == null)
		{
			set_bootstrap_libraries(libraries);
		}
		else
		{
			unsafe.write(loader, ClassLoader_libraries, libraries);
		}
	}

	public static final Object get_libraries(ClassLoader loader)
	{
		if (loader == null)
		{
			return get_bootstrap_libraries();
		}
		else
		{
			return unsafe.read_reference(loader, ClassLoader_libraries);
		}
	}
}

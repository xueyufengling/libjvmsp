package jvmsp;

import java.io.InputStream;

public class JarKlassLoader {
	public static String parentClassLoaderField = class_loader.default_parent_field_name;

	/**
	 * 加载指定jar中的所有类
	 * 
	 * @param loader
	 * @param jar
	 */
	public static ClassLoader loadKlass(ClassLoader loader, InputStream... jars) {
		return class_loader.proxy.attach(loader, parentClassLoaderField, file_system.collect_class(jars));
	}

	public static ClassLoader loadKlass(ClassLoader loader, byte[]... multi_jar_bytes) {
		return loadKlass(loader, file_system.jar_streams(multi_jar_bytes));
	}

	public static final void resetParentClassLoaderField() {
		parentClassLoaderField = class_loader.default_parent_field_name;
	}

	/**
	 * 加载jar子包中的类
	 * 
	 * @param loader
	 * @param jar
	 * @param package_name
	 * @param include_subpackage
	 */
	public static ClassLoader loadKlass(ClassLoader loader, String package_name, boolean include_subpackage, InputStream... jars) {
		return class_loader.proxy.attach(loader, parentClassLoaderField, file_system.collect_class(package_name, include_subpackage, jars));
	}

	public static ClassLoader loadKlass(ClassLoader loader, String package_name, boolean include_subpackage, byte[]... multi_jar_bytes) {
		return loadKlass(loader, file_system.jar_streams(multi_jar_bytes));
	}

	/**
	 * 从调用该方法的类所属的ClassLoader加载目标jar中的全部类
	 * 
	 * @param jar
	 */
	public static ClassLoader loadKlass(InputStream... jars) {
		Class<?> caller = JavaLang.caller_class();
		return loadKlass(caller.getClassLoader(), jars);
	}

	public static ClassLoader loadKlass(byte[]... multi_jar_bytes) {
		Class<?> caller = JavaLang.caller_class();
		return loadKlass(caller.getClassLoader(), file_system.jar_streams(multi_jar_bytes));
	}

	public static ClassLoader loadKlass(String... jar_paths) {
		Class<?> caller = JavaLang.caller_class();
		return loadKlass(caller.getClassLoader(), file_system.jar_streams(caller, jar_paths));
	}

	/**
	 * 从调用该方法的类所属的ClassLoader加载目标jar中指定路径下的类
	 * 
	 * @param jar
	 * @param package_name
	 * @param include_subpackage
	 */
	public static ClassLoader loadKlass(String package_name, boolean include_subpackage, InputStream... jars) {
		Class<?> caller = JavaLang.caller_class();
		return loadKlass(caller.getClassLoader(), package_name, include_subpackage, jars);
	}

	public static ClassLoader loadKlass(String package_name, boolean include_subpackage, byte[]... multi_jar_bytes) {
		Class<?> caller = JavaLang.caller_class();
		return loadKlass(caller.getClassLoader(), package_name, include_subpackage, file_system.jar_streams(multi_jar_bytes));
	}

	public static ClassLoader loadKlass(String package_name, boolean include_subpackage, String... entry_jar_paths) {
		Class<?> caller = JavaLang.caller_class();
		return loadKlass(caller.getClassLoader(), package_name, include_subpackage, file_system.jar_streams(caller, entry_jar_paths));
	}
}

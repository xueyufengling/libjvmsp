package jvmsp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;

public class file_system
{

	public static final int DEFAULT_BUFFER_SIZE = 1024;

	public static final boolean is_root(String dir)
	{
		return dir == null || dir.equals("") || dir.equals("/");
	}

	public static final String CLASS_EXTENSION_NAME = ".class";

	/**
	 * 如果any_class打包在jar内，则获取jar文件的路径。<br>
	 * 如果它是未打包的class文件，则获取其所在的文件夹路径。<br>
	 * 
	 * @param clazz 任意一个类
	 * @return class的本地文件路径；当class在jar内时返回jar的绝对路径
	 */
	public static final String classpath(Class<?> clazz)
	{
		ProtectionDomain protection_domain = clazz.getProtectionDomain();
		if (protection_domain == null) // 通过运行时defineClass()定义的类可能为null
		{
			return null;
		}
		CodeSource code_source = protection_domain.getCodeSource();
		if (code_source == null)// Bootstrap ClassLoader加载类的CodeSource为null
		{
			return null;
		}
		URL location = code_source.getLocation();
		if (location != null)
		{
			try
			{
				return URLDecoder.decode(location.getPath(), StandardCharsets.UTF_8.name());
			}
			catch (UnsupportedEncodingException ex)
			{
				throw new AssertionError("UTF-8 not supported", ex);
			}
		}
		return null;
	}

	public static final String classpath()
	{
		Class<?> caller = reflection.caller_class();// 获取调用该方法的类
		return classpath(caller);
	}

	/**
	 * 标准化绝对路径
	 * 
	 * @param path
	 * @return
	 */
	public static final String normalized_abs_path(String path)
	{
		if (path == null || path.equals(""))
			return "/";
		char ch = path.charAt(0);// 检查开头有没有多余的路径分隔符
		boolean is_windows = virtual_machine.platform.host == virtual_machine.platform.windows;
		if (ch == File.separatorChar || ch == '/')
		{
			if (is_windows)
				path = path.substring(1);
		}
		else
		{
			if (!is_windows)
				path = "/" + path;
		}
		ch = path.charAt(path.length() - 1);// 检查路径末尾有没有多余的路径分隔符
		if (ch == File.separatorChar || ch == '/')
			path = path.substring(0, path.length() - 1);
		path = path.replace('/', File.separatorChar);
		return path;
	}

	/**
	 * 标准化相对路径
	 * 
	 * @param path
	 * @return
	 */
	public static final String normalized_rlt_path(String path)
	{
		// 若path为""，则不可charAt()，需要直接视作根路径返回。 在Debug环境下可能会出现此种情况
		if (path == null || path.equals(""))
			return "";
		char ch = path.charAt(0);// 检查开头有没有多余的路径分隔符
		int start = 0;
		int end = path.length();
		if (ch == File.separatorChar || ch == '/')
			start = 1;
		ch = path.charAt(path.length() - 1);// 检查路径末尾有没有多余的路径分隔符
		if (ch == File.separatorChar || ch == '/')
			end = end - 1;
		if (start >= end)
			return "";
		else
			return path.substring(start, end);
	}

	public static final byte[] read(Path path)
	{
		try
		{
			return Files.readAllBytes(path);
		}
		catch (IOException ex)
		{
			throw new java.lang.InternalError("read bytes of '" + path + "' failed", ex);
		}
	}

	@FunctionalInterface
	public interface file_entry_operation
	{
		/**
		 * 遍历处理JarEntry
		 * 
		 * @param entry
		 * @param bytes
		 * @return 在collect时是否收集该Entry
		 */
		public boolean operate(String start_path, Path entry);

		@FunctionalInterface
		public static interface file
		{
			/**
			 * 单个文件处理
			 * 
			 * @param start_path        开始遍历的路径
			 * @param relative_file_dir 相对于开始路径的文件所在文件夹路径
			 * @param file_name         文件名称
			 * @param entry
			 */
			public boolean operate(String start_path, String relative_file_dir, String file_name, Path entry);
		}

		@FunctionalInterface
		public static interface _class
		{
			/**
			 * 单个文件处理
			 * 
			 * @param class_full_name
			 * @param entry
			 * @param bytes
			 */
			public boolean operate(String class_full_name, Path entry);
		}
	}

	public static final List<String> class_names_local(Class<?> any_class_in_package, String package_name, boolean include_subpackage)
	{
		List<String> class_names = new ArrayList<>();
		file_system.filter_class(classpath(any_class_in_package), include_subpackage, (String class_full_name, Path entry) ->
		{
			if (package_name == "")// 没有包的默认空间
			{
				if (!class_full_name.contains("."))
					class_names.add(class_full_name);
			}
			else
			{
				if (class_full_name.startsWith(package_name))
					class_names.add(class_full_name);
			}
			return true;
		});
		return class_names;
	}

	public static final List<String> class_names_local(String package_name, boolean include_subpackage)
	{
		Class<?> caller = reflection.caller_class();
		return class_names_local(caller, package_name, include_subpackage);// 获取调用该方法的类
	}

	public static final List<String> class_names_local(Class<?> any_class_in_package, String package_name)
	{
		return class_names_local(any_class_in_package, package_name, false);
	}

	public static final List<String> class_names_local(String package_name)
	{
		Class<?> caller = reflection.caller_class();
		return class_names_local(caller, package_name);// 获取调用该方法的类
	}

	/**
	 * jar文件的相关操作，任何操作都需要传入{@code any_class_in_jar}，即jar内的任意一个类。<br>
	 * <p>
	 * 如果使用没有{@code any_class_in_jar}参数的方法，那么将获取调用者所在类作为{@code any_class_in_jar}参数
	 */

	public static final class jar_entry_data
	{
		public final String file_dir;
		public final String file_name;
		public final JarEntry entry;
		public final byte[] data;

		public jar_entry_data(String file_dir, String file_name, JarEntry entry, byte[] data)
		{
			this.file_dir = file_dir;
			this.file_name = file_name;
			this.entry = entry;
			this.data = data;
		}

		public jar_entry_data(JarEntry entry, byte[] data)
		{
			String path = entry.getName();
			int sep = path.lastIndexOf('/');
			this.file_dir = sep == -1 ? null : path.substring(0, sep);
			this.file_name = path.substring(sep + 1);
			this.entry = entry;
			this.data = data;
		}

		public static final jar_entry_data from(String file_dir, String file_name, JarEntry entry, byte[] data)
		{
			return new jar_entry_data(file_dir, file_name, entry, data);
		}

		public static final jar_entry_data from(JarEntry entry, byte[] data)
		{
			return new jar_entry_data(entry, data);
		}
	}

	@FunctionalInterface
	public interface jar_entry_operation
	{
		/**
		 * 遍历处理JarEntry
		 * 
		 * @param entry
		 * @param bytes
		 * @return 在collect时是否收集该Entry
		 */
		public boolean operate(JarEntry entry, ByteArrayOutputStream bytes);

		@FunctionalInterface
		public static interface file
		{
			/**
			 * 单个文件处理
			 * 
			 * @param file_dir  jar包内的路径
			 * @param file_name
			 * @param entry
			 * @param bytes
			 */
			public boolean operate(String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes);
		}

		@FunctionalInterface
		public static interface _class
		{
			/**
			 * 单个文件处理
			 * 
			 * @param class_full_name
			 * @param entry
			 * @param bytes
			 */
			public boolean operate(String class_full_name, JarEntry entry, ByteArrayOutputStream bytes);
		}
	}

	public static final String JAR_EXTENSION_NAME = ".jar";

	// ------------------------------------------------------------ Internal Utils ----------------------------------------------------------------------------

	/**
	 * 从InputStream中获取指定path的JarEntry
	 * 
	 * @param jar  必须是新流，指针offset在0
	 * @param path
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	@SuppressWarnings("unused")
	private static JarEntry get_jar_entry(InputStream jar, String path) throws IOException
	{
		JarEntry entry = null;
		try (JarInputStream jar_stream = new JarInputStream(jar))
		{
			while ((entry = jar_stream.getNextJarEntry()) != null)
			{
				if (entry.getName().equals(path))
					break;
			}
		}
		return entry;
	}

	/**
	 * 读取JarInputStream中的指定entry的内容
	 * 
	 * @param jar         必须是新流，指针offset在0
	 * @param buffer_size 读取缓冲区大小
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static final ByteArrayOutputStream get_jar_entry_bytes(JarInputStream jar_stream, int buffer_size) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[buffer_size];
		int read = 0;
		while ((read = jar_stream.read(buffer)) != -1)
		{
			bos.write(buffer, 0, read);
		}
		return bos;
	}

	/**
	 * 读取jar InputStream中指定path的数据
	 * 
	 * @param jar  必须是新流，指针offset在0
	 * @param path
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static final jar_entry_data read_jar_entry(InputStream jar, String path) throws IOException
	{
		jar_entry_data data = null;
		try (JarInputStream jar_stream = new JarInputStream(jar))
		{
			JarEntry entry = null;
			while ((entry = jar_stream.getNextJarEntry()) != null)
			{
				if (entry.getName().equals(path))
					data = jar_entry_data.from(entry, get_jar_entry_bytes(jar_stream, file_system.DEFAULT_BUFFER_SIZE).toByteArray());
			}
		}
		return data;
	}

	// -------------------------------------------------------- Resources ----------------------------------------------------------------------

	public static final byte[] resource_bytes(Class<?> any_class_in_jar, String path)
	{

		try (InputStream res = any_class_in_jar.getResource(path).openStream())
		{
			return res.readAllBytes();
		}
		catch (IOException ex)
		{
			throw new java.lang.InternalError("get resource bytes of class '" + any_class_in_jar + "' at '" + path + "' failed");
		}
	}

	public static final byte[] resource_bytes(String path)
	{
		Class<?> caller = reflection.caller_class();
		return resource_bytes(caller, path);// 获取调用该方法的类
	}

	public static final InputStream resource_stream(Class<?> any_class_in_jar, String path)
	{
		try
		{
			return any_class_in_jar.getResource(path).openStream();
		}
		catch (IOException ex)
		{
			throw new java.lang.InternalError("get resource bytes of class '" + any_class_in_jar + "' at '" + path + "' failed", ex);
		}
	}

	public static final InputStream resource_stream(String path)
	{
		Class<?> caller = reflection.caller_class();
		return resource_stream(caller, path);// 获取调用该方法的类
	}

	/**
	 * 从JarFile中读取资源
	 * 
	 * @param jar
	 * @param path
	 * @return
	 */
	public static final byte[] resource_bytes(JarFile jar, String path)
	{
		try (jar)
		{
			JarEntry entry = jar.getJarEntry(path);
			if (entry != null)
			{
				InputStream input_stream = jar.getInputStream(entry);
				byte[] bytes = input_stream.readAllBytes();
				input_stream.close();
				return bytes;
			}
			return null;
		}
		catch (IOException ex)
		{
			throw new java.lang.InternalError("get jar file resource bytes at '" + path + "' failed", ex);
		}
	}

	/**
	 * 从文件系统中读取jar文件并获取资源字节
	 * 
	 * @param jar_path
	 * @param path
	 * @return
	 */
	public static final byte[] resource_bytes(String jar_path, String path)
	{
		try
		{
			return resource_bytes(new JarFile(jar_path), path);
		}
		catch (IOException ex)
		{
			throw new java.lang.InternalError("get jar file '" + jar_path + "' resource bytes at '" + path + "' failed", ex);
		}
	}

	public static final byte[] resource_bytes(InputStream jar_bytes, String path)
	{
		byte[] bytes = null;
		try
		{
			bytes = read_jar_entry(jar_bytes, path).data;
		}
		catch (IOException ex)
		{
			throw new java.lang.InternalError("get resource bytes in jar at '" + path + "' failed", ex);
		}
		return bytes;
	}

	public static final byte[] resource_bytes(byte[] jar_bytes, String path)
	{
		return resource_bytes(new ByteArrayInputStream(jar_bytes), path);
	}

	/**
	 * 获取any_class_in_jar所在jar文件字节流
	 * 
	 * @param any_class_in_jar
	 * @param resolver         路径解析器，根据URI分割为合法的文件系统路径和Entry路径
	 * @return
	 * @throws FileNotFoundException
	 */
	public static final InputStream jar_stream(Class<?> any_class_in_jar) throws FileNotFoundException
	{
		return new FileInputStream(classpath(any_class_in_jar));
	}

	public static final InputStream jar_stream(byte[] bytes)
	{
		return new ByteArrayInputStream(bytes);
	}

	/**
	 * 获取多个any_class_in_jars类所在jar文件字节流
	 * 
	 * @param any_class_in_jars
	 * @param resolver          路径解析器，根据URI分割为合法的文件系统路径和Entry路径
	 * @return
	 * @throws FileNotFoundException
	 */
	public static final InputStream[] jar_streams(Class<?>... any_class_in_jars) throws FileNotFoundException
	{
		InputStream[] streams = new InputStream[any_class_in_jars.length];
		for (int idx = 0; idx < any_class_in_jars.length; ++idx)
		{
			streams[idx] = new FileInputStream(classpath(any_class_in_jars[idx]));
		}
		return streams;
	}

	public static final InputStream[] jar_streams(byte[]... multi_bytes)
	{
		InputStream[] streams = new InputStream[multi_bytes.length];
		for (int idx = 0; idx < multi_bytes.length; ++idx)
		{
			streams[idx] = new ByteArrayInputStream(multi_bytes[idx]);
		}
		return streams;
	}

	public static final InputStream[] jar_streams(Class<?> any_class_in_jar, String... entry_paths)
	{
		InputStream[] streams = new InputStream[entry_paths.length];
		for (int idx = 0; idx < entry_paths.length; ++idx)
		{
			streams[idx] = jar_stream(resource_bytes(any_class_in_jar, entry_paths[idx]));
		}
		return streams;
	}

	// ----------------------------------------------------------- Class ---------------------------------------------------------------------------
	/**
	 * 获取jar文件中指定Java包下的所有类名（含包名）
	 * 
	 * @param any_class_in_package jar包内的任意一个类，这是为了获取加载jar包内加载class文件的ClassLoader
	 * @param package_name         要获取的包名
	 * @param include_subpackage   是否获取该包及其所有递归子包的类名称
	 * @return 类名数组
	 */
	public static final List<String> class_names_in_jar(Class<?> any_class_in_package, String package_name, boolean include_subpackage)
	{
		try
		{
			List<String> class_names = new ArrayList<>();
			file_system.filter_class(jar_stream(any_class_in_package), package_name.replace('.', '/'), include_subpackage, (String class_full_name, JarEntry entry, ByteArrayOutputStream bytes) ->
			{
				class_names.add(class_full_name);
				return true;
			});
			return class_names;
		}
		catch (FileNotFoundException ex)
		{
			throw new java.lang.InternalError("get class names in jar of '" + any_class_in_package + "' with package '" + package_name + "' failed", ex);
		}
	}

	public static final List<String> class_names_in_jar(String package_name, boolean include_subpackage)
	{
		Class<?> caller = reflection.caller_class();
		return class_names_in_jar(caller, package_name, include_subpackage);// 获取调用该方法的类
	}

	public static final List<String> class_names_in_jar(Class<?> any_class_in_package, String package_name)
	{
		return class_names_in_jar(any_class_in_package, package_name, false);
	}

	public static final List<String> class_names_in_jar(String package_name)
	{
		Class<?> caller = reflection.caller_class();
		return class_names_in_jar(caller, package_name);// 获取调用该方法的类
	}

	/**
	 * 获取一个已经加载的jar文件中指定Java包下的所有类
	 * 
	 * @param any_class_in_package jar包内的任意一个类，这是为了获取加载jar包内加载class文件的ClassLoader
	 * @param package_name         要获取的包名
	 * @param include_subpackage   是否获取该包及其所有递归子包的类名称
	 * @return 包名数组
	 */
	public static final List<Class<?>> classes_in_jar(Class<?> any_class_in_package, String package_name, boolean include_subpackage)
	{
		List<Class<?>> class_list = new ArrayList<>();
		List<String> class_names = class_names_in_jar(any_class_in_package, package_name, include_subpackage);
		ClassLoader class_loader = any_class_in_package.getClassLoader();
		for (String class_name : class_names)
			try
			{
				class_list.add(class_loader.loadClass(class_name));
			}
			catch (ClassNotFoundException ex)
			{
				throw new java.lang.InternalError("get classes in jar of '" + any_class_in_package + "' with package '" + package_name + "' failed", ex);
			}
		return class_list;
	}

	public static final List<Class<?>> classes_in_jar(String package_name, boolean include_subpackage)
	{
		Class<?> caller = reflection.caller_class();
		return classes_in_jar(caller, package_name, include_subpackage);// 获取调用该方法的类
	}

	public static final List<Class<?>> classes_in_jar(Class<?> any_class_in_package, String package_name)
	{
		return classes_in_jar(any_class_in_package, package_name, false);
	}

	public static final List<Class<?>> classes_in_jar(String package_name)
	{
		Class<?> caller = reflection.caller_class();
		return classes_in_jar(caller, package_name);// 获取调用该方法的类
	}

	/**
	 * 获取jar文件中指定Java包下的所有具有指定超类的类
	 * 
	 * @param any_class_in_package jar包内的任意一个类，这是为了获取加载jar包内加载class文件的ClassLoader
	 * @param package_name         要获取的包名
	 * @param include_subpackage   是否获取该包及其所有递归子包的类名称
	 * @return 包名数组
	 */
	public static final List<Class<?>> subclasses_in_jar(Class<?> any_class_in_package, String package_name, Class<?> super_class, boolean include_subpackage)
	{
		List<Class<?>> specified_class_list = new ArrayList<>();
		List<Class<?>> class_list = classes_in_jar(any_class_in_package, package_name, include_subpackage);
		for (Class<?> clazz : class_list)
			if (reflection.has_super(clazz, super_class))
				specified_class_list.add(clazz);
		return specified_class_list;
	}

	public static final List<Class<?>> subclasses_in_jar(String package_name, Class<?> super_class, boolean include_subpackage)
	{
		Class<?> caller = reflection.caller_class();
		return subclasses_in_jar(caller, package_name, super_class, include_subpackage);// 获取调用该方法的类
	}

	public static final List<Class<?>> subclasses_in_jar(Class<?> any_class_in_package, String package_name, Class<?> super_class)
	{
		return subclasses_in_jar(any_class_in_package, package_name, super_class, false);
	}

	public static final List<Class<?>> subclasses_in_jar(String package_name, Class<?> super_class)
	{
		return subclasses_in_jar(reflection.caller_class(), package_name, super_class);// 获取调用该方法的类
	}

	// -------------------------------------------------------- foreach Operations --------------------------------------------------------------------

	// foreach函数族
	/**
	 * 遍历每个JarEntry
	 * 
	 * @param jar
	 * @param op
	 */
	public static final void foreach(InputStream jar, jar_entry_operation op)
	{
		try (JarInputStream jar_stream = new JarInputStream(jar))
		{
			JarEntry entry = null;
			while ((entry = jar_stream.getNextJarEntry()) != null)
			{
				op.operate(entry, get_jar_entry_bytes(jar_stream, DEFAULT_BUFFER_SIZE));
			}
		}
		catch (IOException ex)
		{
			throw new java.lang.InternalError("foreach operation in jar failed", ex);
		}
	}

	/**
	 * 遍历本地文件系统
	 * 
	 * @param start_path
	 * @param op
	 */
	public static final void foreach(String start_path, file_entry_operation op)
	{
		Path root = Paths.get(start_path);
		try (Stream<Path> paths = Files.walk(root))
		{
			paths.forEach(file ->
			{
				op.operate(start_path, file);
			});
		}
		catch (IOException ex)
		{
			throw new java.lang.InternalError("foreach operation in local file system failed", ex);
		}
	}

	/**
	 * 遍历每个文件
	 * 
	 * @param jar
	 * @param op
	 */
	public static final void foreach(InputStream jar, jar_entry_operation.file op)
	{
		foreach(jar, new jar_entry_operation()
		{
			@Override
			public boolean operate(JarEntry entry, ByteArrayOutputStream bytes)
			{
				if (!entry.isDirectory())
				{
					String path = entry.getName();
					int sep = path.lastIndexOf('/');
					op.operate(sep == -1 ? "/" : path.substring(0, sep), path.substring(sep + 1), entry, bytes);
				}
				return true;
			}
		});
	}

	public static final void foreach(String start_path, file_entry_operation.file op)
	{
		foreach(start_path, true, op);
	}

	/**
	 * 从指定目录开始遍历每个文件
	 * 
	 * @param jar
	 * @param start_path         开始遍历的目录
	 * @param include_subpackage 是否遍历子目录
	 * @param op
	 */
	public static final void foreach(InputStream jar, String start_path, boolean include_subpackage, jar_entry_operation.file op)
	{
		foreach(jar, new jar_entry_operation.file()
		{
			@Override
			public boolean operate(String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes)
			{
				if (is_root(start_path))
				{
					if (is_root(file_dir))
						op.operate(file_dir, file_name, entry, bytes);
				}
				else
				{
					if (!is_root(file_dir) && file_dir.startsWith(start_path))
						op.operate(file_dir, file_name, entry, bytes);
				}
				return true;
			}
		});
	}

	public static final void foreach(String start_path, boolean include_subpackage, file_entry_operation.file op)
	{
		String std_start_path = normalized_abs_path(start_path);
		Path root = Paths.get(std_start_path);
		try (Stream<Path> paths = Files.walk(root, include_subpackage ? Integer.MAX_VALUE : 1))
		{
			paths.filter(Files::isRegularFile)
					.forEach(file ->
					{
						op.operate(std_start_path, normalized_rlt_path(normalized_abs_path(file.getParent().toString()).replace(std_start_path, "")), file.getFileName().toString(), file);
					});
		}
		catch (IOException ex)
		{
			throw new java.lang.InternalError("foreach operation in jar failed", ex);
		}
	}

	// collect函数族

	/**
	 * 按照条件收集Entry
	 * 
	 * @param jar
	 * @param condition 条件，返回true则代表收集，false代表不收集
	 * @return
	 */
	public static final List<jar_entry_data> collect(InputStream jar, jar_entry_operation.file condition)
	{
		List<jar_entry_data> entries = new ArrayList<>();
		foreach(jar, new jar_entry_operation.file()
		{
			@Override
			public boolean operate(String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes)
			{
				boolean reserved = condition.operate(file_dir, file_name, entry, bytes);
				if (reserved)
					entries.add(jar_entry_data.from(file_dir, file_name, entry, bytes.toByteArray()));
				return reserved;
			}
		});
		return entries;
	}

	public static final List<Path> collect(String start_path, file_entry_operation.file condition)
	{
		return collect(start_path, true, condition);
	}

	/**
	 * 遍历子包收集文件
	 * 
	 * @param jar
	 * @param start_path
	 * @param include_subpackage
	 * @param condition
	 * @return
	 */
	public static final List<jar_entry_data> collect(InputStream jar, String start_path, boolean include_subpackage, jar_entry_operation.file condition)
	{
		List<jar_entry_data> entries = new ArrayList<>();
		foreach(jar, start_path, include_subpackage, new jar_entry_operation.file()
		{
			@Override
			public boolean operate(String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes)
			{
				boolean reserved = condition.operate(file_dir, file_name, entry, bytes);
				if (reserved)
					entries.add(jar_entry_data.from(file_dir, file_name, entry, bytes.toByteArray()));
				return reserved;
			}
		});
		return entries;
	}

	public static final List<Path> collect(String start_path, boolean include_subpackage, file_entry_operation.file condition)
	{
		List<Path> entries = new ArrayList<>();
		foreach(start_path, include_subpackage, new file_entry_operation.file()
		{
			@Override
			public boolean operate(String start_root_path, String relative_file_dir, String file_name, Path entry)
			{
				boolean reserved = condition.operate(start_root_path, relative_file_dir, file_name, entry);
				if (reserved)
					entries.add(entry);
				return reserved;
			}
		});
		return entries;
	}

	// collect函数的具体实现
	/**
	 * 收集jar中的文件名匹配正则表达式的文件
	 * 
	 * @param jar
	 * @param regex
	 * @return
	 */
	public static final List<jar_entry_data> collect(InputStream jar, String regex)
	{
		return collect(jar, (String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes) ->
		{
			return file_name.matches(regex);
		});
	}

	public static final List<Path> collect(String start_path, String regex)
	{
		return collect(start_path, (String start_root_path, String relative_file_dir, String file_name, Path entry) ->
		{
			return file_name.matches(regex);
		});
	}

	public static final List<jar_entry_data> collect(InputStream jar, String start_path, boolean include_subpackage, String regex)
	{
		return collect(jar, (String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes) ->
		{
			return file_name.matches(regex);
		});
	}

	public static final List<Path> collect(String start_path, boolean include_subpackage, String regex)
	{
		return collect(start_path, include_subpackage, (String start_root_path, String relative_file_dir, String file_name, Path entry) ->
		{
			return file_name.matches(regex);
		});
	}

	public static final List<jar_entry_data> collect_type(InputStream jar, String type)
	{
		return collect(jar, (String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes) ->
		{
			return file_name.endsWith(type);
		});
	}

	public static final List<Path> collect_type(String start_path, String type)
	{
		return collect(start_path, (String start_root_path, String relative_file_dir, String file_name, Path entry) ->
		{
			return file_name.endsWith(type);
		});
	}

	public static final List<jar_entry_data> collect_type(InputStream jar, String start_path, boolean include_subpackage, String file_type)
	{
		return collect(jar, (String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes) ->
		{
			return file_name.endsWith(file_type);
		});
	}

	public static final List<Path> collect_type(String start_path, boolean include_subpackage, String file_type)
	{
		return collect(start_path, include_subpackage, (String start_root_path, String relative_file_dir, String file_name, Path entry) ->
		{
			return file_name.endsWith(file_type);
		});
	}

	// filter函数族
	/**
	 * 遍历指定条件的文件并执行操作
	 * 
	 * @param jar
	 * @param condition
	 * @param op
	 */
	public static final void filter(InputStream jar, jar_entry_operation.file condition, jar_entry_operation.file op)
	{
		foreach(jar, new jar_entry_operation.file()
		{
			@Override
			public boolean operate(String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes)
			{
				if (condition.operate(file_dir, file_name, entry, bytes))
					op.operate(file_dir, file_name, entry, bytes);
				return true;
			}
		});
	}

	public static final void filter(String start_path, file_entry_operation.file condition, file_entry_operation.file op)
	{
		filter(start_path, true, condition, op);
	}

	// ---------
	public static final void filter(InputStream jar, String start_path, boolean include_subpackage, jar_entry_operation.file condition, jar_entry_operation.file op)
	{
		foreach(jar, start_path, include_subpackage, new jar_entry_operation.file()
		{
			@Override
			public boolean operate(String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes)
			{
				if (condition.operate(file_dir, file_name, entry, bytes))
					op.operate(file_dir, file_name, entry, bytes);
				return true;
			}
		});
	}

	public static final void filter(String start_path, boolean include_subpackage, file_entry_operation.file condition, file_entry_operation.file op)
	{
		foreach(start_path, include_subpackage, new file_entry_operation.file()
		{
			@Override
			public boolean operate(String start_root_path, String relative_file_dir, String file_name, Path entry)
			{
				if (condition.operate(start_root_path, relative_file_dir, file_name, entry))
					op.operate(start_root_path, relative_file_dir, file_name, entry);
				return true;
			}
		});
	}

	// -----------
	public static final void filter(InputStream jar, String regex, jar_entry_operation.file op)
	{
		filter(jar, (String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes) ->
		{
			return file_name.matches(regex);
		}, op);
	}

	public static final void filter(String start_path, String regex, file_entry_operation.file op)
	{
		filter(start_path, true, regex, op);
	}

	// ------------
	public static final void filter(InputStream jar, String start_path, boolean include_subpackage, String regex, jar_entry_operation.file op)
	{
		filter(jar, start_path, include_subpackage, (String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes) ->
		{
			return file_name.matches(regex);
		}, op);
	}

	public static final void filter(String start_path, boolean include_subpackage, String regex, file_entry_operation.file op)
	{
		filter(start_path, include_subpackage, (String start_root_path, String relative_file_dir, String file_name, Path entry) ->
		{
			return file_name.matches(regex);
		}, op);
	}

	// ------------
	public static final void filter_type(InputStream jar, String file_type, jar_entry_operation.file op)
	{
		filter(jar, (String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes) ->
		{
			return file_name.endsWith(file_type);
		}, op);
	}

	public static final void filter_type(String start_path, String file_type, file_entry_operation.file op)
	{
		filter_type(start_path, true, file_type, op);
	}

	// ------------
	public static final void filter_type(InputStream jar, String start_path, boolean include_subpackage, String file_type, jar_entry_operation.file op)
	{
		filter(jar, start_path, include_subpackage, (String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes) ->
		{
			return file_name.endsWith(file_type);
		}, op);
	}

	public static final void filter_type(String start_path, boolean include_subpackage, String file_type, file_entry_operation.file op)
	{
		filter(start_path, include_subpackage, (String start_root_path, String relative_file_dir, String file_name, Path entry) ->
		{
			return file_name.endsWith(file_type);
		}, op);
	}

	// ----------------------------------------------------------------- Class --------------------------------------------------------------------------
	public static final void filter_class(InputStream jar, jar_entry_operation._class op)
	{
		filter_type(jar, CLASS_EXTENSION_NAME, (String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes) ->
		{
			String full_path = entry.getName();
			op.operate(full_path.substring(0, full_path.length() - CLASS_EXTENSION_NAME.length()).replace('/', '.'), entry, bytes);
			return true;
		});
	}

	public static final void filter_class(String start_path, file_entry_operation._class op)
	{
		filter_class(start_path, true, op);
	}

	// ------------
	public static final void filter_class(InputStream jar, String start_path, boolean include_subpackage, jar_entry_operation._class op)
	{
		filter_type(jar, start_path, include_subpackage, CLASS_EXTENSION_NAME, (String file_dir, String file_name, JarEntry entry, ByteArrayOutputStream bytes) ->
		{
			String full_path = entry.getName();
			op.operate(full_path.substring(0, full_path.length() - CLASS_EXTENSION_NAME.length()).replace('/', '.'), entry, bytes);
			return true;
		});
	}

	public static final void filter_class(String start_path, boolean include_subpackage, file_entry_operation._class op)
	{
		filter_type(start_path, include_subpackage, CLASS_EXTENSION_NAME, (String start_root_path, String relative_file_dir, String file_name, Path entry) ->
		{

			String simple_name = file_name.substring(0, file_name.length() - CLASS_EXTENSION_NAME.length());
			if (relative_file_dir == "")
			{
				op.operate(simple_name, entry);
			}
			else
			{
				StringBuilder class_name = new StringBuilder();
				class_name.append(relative_file_dir.replace(File.separatorChar, '.'));
				class_name.append('.');
				class_name.append(simple_name);
				op.operate(class_name.toString(), entry);
			}
			return true;
		});
	}

	// ------------
	public static final HashMap<String, byte[]> collect_class(InputStream... jars)
	{
		HashMap<String, byte[]> class_defs = new HashMap<>();
		for (InputStream jar : jars)
			filter_class(jar, (String class_full_name, JarEntry entry, ByteArrayOutputStream bytes) ->
			{
				class_defs.put(class_full_name, bytes.toByteArray());
				return true;
			});
		return class_defs;
	}

	public static final HashMap<String, byte[]> collect_class(String... start_paths)
	{
		HashMap<String, byte[]> class_defs = new HashMap<>();
		for (String start_path : start_paths)
			filter_class(start_path, (String class_full_name, Path entry) ->
			{
				class_defs.put(class_full_name, read(entry));
				return true;
			});
		return class_defs;
	}

	// ------------
	public static final HashMap<String, byte[]> collect_class(String start_path, boolean include_subpackage, InputStream... jars)
	{
		HashMap<String, byte[]> class_defs = new HashMap<>();
		for (InputStream jar : jars)
			filter_class(jar, start_path, include_subpackage, (String class_full_name, JarEntry entry, ByteArrayOutputStream bytes) ->
			{
				class_defs.put(class_full_name, bytes.toByteArray());
				return true;
			});
		return class_defs;
	}

	public static final HashMap<String, byte[]> collect_class(boolean include_subpackage, String... start_paths)
	{
		HashMap<String, byte[]> class_defs = new HashMap<>();
		for (String start_path : start_paths)
			filter_class(start_path, include_subpackage, (String class_full_name, Path entry) ->
			{
				class_defs.put(class_full_name, read(entry));
				return true;
			});
		return class_defs;
	}
}

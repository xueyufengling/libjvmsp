package jvmsp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 模块系统相关
 */
public class module
{
	public static Method implAddOpens;

	static
	{
		try
		{
			implAddOpens = Module.class.getDeclaredMethod("implAddOpens", String.class);
			// implAddOpens.setAccessible(true);
		}
		catch (NoSuchMethodException | SecurityException ex)
		{
			ex.printStackTrace();
		}
	}

	public static void add_opens(Module module, String pkg)
	{
		try
		{
			implAddOpens.invoke(module, pkg);
		}
		catch (IllegalAccessException | InvocationTargetException ex)
		{
			throw new java.lang.InternalError("add opens module '" + module + "' to package '" + pkg + "' failed", ex);
		}
	}
}

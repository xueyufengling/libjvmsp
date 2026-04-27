package jvmsp;

import java.util.function.Supplier;

/**
 * 不同版本的代码行为控制
 */
public class versions
{
	private final int[] version_tags;
	// 当前版本tag
	private final int current_version;
	// 当前版本号的索引，从0开始计数
	private final int current_version_idx;

	/**
	 * 
	 * @param calc_current_version 当前版本号的计算函数，必须介于version_tags中的最大值和最小值之间。<br>
	 * @param version_tags
	 */
	protected versions(Supplier<Integer> calc_current_version, int... version_tags)
	{
		this.version_tags = version_tags;
		this.current_version = calc_current_version.get();
		this.current_version_idx = algorithms.floor_idx(current_version, version_tags);
	}

	public static versions define(Supplier<Integer> calc_current_version, int... version_tags)
	{
		return new versions(calc_current_version, version_tags);
	}

	/**
	 * 所有可能的版本号。<br>
	 * 
	 * @return
	 */
	public int[] version_tags()
	{
		return version_tags;
	}

	public final int current_version()
	{
		return current_version;
	}

	public final int current_version_idx()
	{
		return current_version_idx;
	}

	/**
	 * 当前兼容的版本号
	 * 
	 * @return
	 */
	public final int current_compatible_version()
	{
		return version_tags[current_version_idx];
	}

	/**
	 * 从传入的值中根据当前版本号返回兼容的值
	 * 
	 * @param <_T>
	 * @param values
	 * @return
	 */
	@SafeVarargs
	public final <_T> _T _switch(_T... values)
	{
		return values[current_version_idx];
	}

	/**
	 * 如果当前版本号的索引超出了待选择的值，则直接返回default_value
	 * 
	 * @param <_T>
	 * @param default_value
	 * @param values
	 * @return
	 */
	@SafeVarargs
	public final <_T> _T switch_exist_default(_T default_value, _T... values)
	{
		return current_version_idx >= values.length ? default_value : values[current_version_idx];
	}

	@SafeVarargs
	public final <_T> _T switch_exist(_T... values)
	{
		return switch_exist_default(null, values);
	}

	@SafeVarargs
	public final <_T> _T switch_execute(Supplier<_T>... execs)
	{
		Supplier<_T> exec = _switch(execs);
		if (exec != null)
			return exec.get();
		else
			return null;
	}

	@SafeVarargs
	public final <_T> _T switch_execute_exist(Supplier<_T>... values)
	{
		return current_version_idx >= values.length ? null : switch_execute(values);
	}

	/**
	 * 根据版本long类型的值，如果无效则使用默认值。
	 * 
	 * @param default_value
	 * @param values
	 * @return
	 */
	@SafeVarargs
	public final long switch_execute_existj(long default_value, Supplier<Long>... values)
	{
		if (current_version_idx < values.length)
		{
			Long v = switch_execute(values);
			if (v == null)
			{
				return default_value;
			}
			else
			{
				return v;
			}
		}
		else
		{
			return default_value;
		}
	}

	@SafeVarargs
	public final long switch_execute_existj(Supplier<Long>... values)
	{
		return switch_execute_existj(0, values);
	}

	public final byte _switch(byte... values)
	{
		return values[current_version_idx];
	}

	public final boolean _switch(boolean... values)
	{
		return values[current_version_idx];
	}

	public final short _switch(short... values)
	{
		return values[current_version_idx];
	}

	public final char _switch(char... values)
	{
		return values[current_version_idx];
	}

	public final int _switch(int... values)
	{
		return values[current_version_idx];
	}

	public final float _switch(float... values)
	{
		return values[current_version_idx];
	}

	public final long _switch(long... values)
	{
		return values[current_version_idx];
	}

	public final double _switch(double... values)
	{
		return values[current_version_idx];
	}

	/**
	 * 选择非null值，如果目标值为null，则继续向老版本查找值，直到有一个老版本有非null值。<br>
	 * 
	 * @param <_T>
	 * @param values
	 * @return
	 */
	@SafeVarargs
	public final <_T> _T switch_nonnull(_T... values)
	{
		for (int idx = current_version_idx; idx >= 0; --idx)
		{
			_T v = values[idx];
			if (v != null)
				return v;
		}
		return null;
	}

	@SafeVarargs
	public final <_T> _T switch_execute_nonnull(Supplier<_T>... execs)
	{
		Supplier<_T> exec = switch_nonnull(execs);
		if (exec != null)
			return exec.get();
		else
			return null;
	}

	@SafeVarargs
	public final void switch_execute_nonnull(Runnable... execs)
	{
		Runnable exec = switch_nonnull(execs);
		if (exec != null)
			exec.run();
	}

	public final <_T> _T match_execute(int min, int max, Supplier<_T> exec)
	{
		if (min <= current_version && current_version < max)
		{
			return exec.get();
		}
		else
		{
			return null;
		}
	}

	public final <_T> _T higher_execute(int min, Supplier<_T> exec)
	{
		if (min <= current_version)
		{
			return exec.get();
		}
		else
		{
			return null;
		}
	}

	public final <_T> _T lower_execute(int max, Supplier<_T> exec)
	{
		if (current_version < max)
		{
			return exec.get();
		}
		else
		{
			return null;
		}
	}

	public static final int JDK_21 = 21;
	public static final int JDK_25 = 25;

	@SuppressWarnings("deprecation")
	public static final versions jdk_versions = versions.define(() -> Runtime.version().major(),
			JDK_21,
			JDK_25);
}

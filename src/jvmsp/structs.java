package jvmsp;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class structs
{
	/**
	 * long类型的变长数组
	 */
	public static class long_array
	{
		public static final int default_capacity = 16;

		private long[] data;
		private int extends_cap;

		private int size = 0;

		public long_array(int capacity, int extends_cap)
		{
			this.data = new long[capacity];
			this.extends_cap = extends_cap;
		}

		public long_array(int capacity)
		{
			this(capacity, capacity);
		}

		public long_array()
		{
			this(default_capacity);
		}

		public void add(long value)
		{
			if (size >= data.length)
			{
				grow();
			}
			data[size++] = value;
		}

		public long get(int idx)
		{
			return data[idx];
		}

		public void set(int idx, long value)
		{
			data[idx] = value;
		}

		public int size()
		{
			return size;
		}

		public long[] data()
		{
			return data;
		}

		public void clear()
		{
			size = 0;
		}

		private void grow()
		{
			long[] new_data = new long[data.length + extends_cap];
			System.arraycopy(data, 0, new_data, 0, size);
			data = new_data;
		}
	}

	/**
	 * 当遍历时如果写入，则也会遍历新写入的元素。<br>
	 * 用途：当本list内的元素操作依赖于遍历的顺序时，可以catch第一次遍历失败并且重新遍历尚未遍历的元素。<br>
	 * 例如，元素a的操作需要元素b和c先执行操作，否则会抛出异常，而b和c在list中的顺序又位于a之后，因此遍历时对a的操作必然失败。<br>
	 * 而本类则会忽略a的异常，继续处理后续的元素b、c，待后续元素全部处理完成后，再重新遍历操作失败的元素，即a，此时对a的操作就能成功.<br>
	 * 典型的应用场景是定义一系列具有互相依赖关系的class。<br>
	 * 
	 * @param <_T>
	 */
	public static class instant_iterate_list<_T>
	{
		/**
		 * 未处理
		 */
		private ConcurrentLinkedDeque<_T> unprocessed = new ConcurrentLinkedDeque<>();
		/**
		 * 已处理列表
		 */
		private ConcurrentLinkedDeque<_T> processed = new ConcurrentLinkedDeque<>();

		public void foreach(Consumer<_T> op, BiConsumer<_T, Throwable> ex_op) throws Throwable
		{
			int last_unprocessed_count = unprocessed.size();
			Throwable last_ex = null;
			while (!unprocessed.isEmpty())
			{
				/**
				 * 如果抛出错误的那次迭代新加入了元素，则忽略上次错误继续迭代新的元素列表。 如果没有新元素加入，则将抛出上次迭代记录的错误。
				 */
				if (last_unprocessed_count == unprocessed.size())
					if (last_ex != null)// 第一次进入不会抛出错误
						throw last_ex;
				for (_T e : unprocessed)
				{
					try
					{
						op.accept(e);
						processed.add(e);
					}
					catch (Throwable ex)
					{
						last_unprocessed_count = unprocessed.size();
						last_ex = ex;
						ex_op.accept(e, ex);// 执行抛出错误就放弃当前操作继续执行下一个元素的操作
					}
				} // 重新迭代时新加入的元素已经同步到unprocessed
				unprocessed.removeAll(processed);
			}
			ConcurrentLinkedDeque<_T> tmp = processed;
			processed = unprocessed;
			unprocessed = tmp;
		}

		public static final BiConsumer<Object, Throwable> ignore_runtime_exception = (Object e, Throwable ex) ->
		{
		};

		public static final BiConsumer<Object, Throwable> print_runtime_exception = (Object e, Throwable ex) ->
		{
			System.err.println("iterating element " + e);
			ex.printStackTrace();
		};

		@SuppressWarnings("unchecked")
		public void foreach(Consumer<_T> op) throws Throwable
		{
			foreach(op, (BiConsumer<_T, Throwable>) ignore_runtime_exception);
		}

		public void add(_T e)
		{
			unprocessed.add(e);
		}

		public void add(Collection<? extends _T> c)
		{
			unprocessed.addAll(c);
		}

		public void clear()
		{
			unprocessed.clear();
		}

		public int size()
		{
			return unprocessed.size() + processed.size();
		}
	}

}

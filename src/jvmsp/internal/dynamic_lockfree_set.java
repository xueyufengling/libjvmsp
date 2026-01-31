package jvmsp.internal;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class dynamic_lockfree_set<T> {
	/**
	 * 未处理
	 */
	private CopyOnWriteArrayList<T> unprocessed = new CopyOnWriteArrayList<>();
	/**
	 * 已处理列表
	 */
	private CopyOnWriteArrayList<T> processed = new CopyOnWriteArrayList<>();

	public void foreach(Consumer<T> op, BiConsumer<T, Throwable> ex_op) throws Throwable {
		int last_unprocessed_count = unprocessed.size();
		Throwable last_ex = null;
		while (!unprocessed.isEmpty()) {
			/**
			 * 如果抛出错误的那次迭代新加入了元素，则忽略上次错误继续迭代新的元素列表。
			 * 如果没有新元素加入，则将抛出上次迭代记录的错误。
			 */
			if (last_unprocessed_count == unprocessed.size())
				if (last_ex != null)// 第一次进入不会抛出错误
					throw last_ex;
			for (T e : unprocessed) {
				try {
					op.accept(e);
					processed.add(e);
				} catch (Throwable ex) {
					last_unprocessed_count = unprocessed.size();
					last_ex = ex;
					ex_op.accept(e, ex);// 执行抛出错误就放弃当前操作继续执行下一个元素的操作
				}
			} // 重新迭代时新加入的元素已经同步到unprocessed
			unprocessed.removeAll(processed);
		}
		CopyOnWriteArrayList<T> tmp = processed;
		processed = unprocessed;
		unprocessed = tmp;
	}

	public static final BiConsumer<Object, Throwable> IGNORE_RUNTIME_EXCEPTION = (Object e, Throwable ex) -> {
	};

	public static final BiConsumer<Object, Throwable> PRINT_RUNTIME_EXCEPTION = (Object e, Throwable ex) -> {
		System.err.println("Iterating element " + e + " throws RuntimeException");
		ex.printStackTrace();
	};

	@SuppressWarnings("unchecked")
	public void foreach(Consumer<T> op) throws Throwable {
		foreach(op, (BiConsumer<T, Throwable>) IGNORE_RUNTIME_EXCEPTION);
	}

	public void add(T e) {
		unprocessed.add(e);
	}

	public void add(Collection<? extends T> c) {
		unprocessed.addAll(c);
	}

	public void clear() {
		unprocessed.clear();
	}

	public int size() {
		return unprocessed.size() + processed.size();
	}
}

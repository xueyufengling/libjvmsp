package jvmsp;

public class algorithms
{
	/**
	 * 二分查找一维边界判断。<br>
	 * 如果一个数value<bounds[0]则返回-1，bounds[0]<value<bounds[1]则返回0，以此类推<br>
	 * 
	 * @param value  要判定的数值
	 * @param bounds 从小到大排列的数，即边界值
	 * @return
	 */
	public final static int floor_idx(double value, double... bounds)
	{
		if (bounds == null || bounds.length == 0 || value < bounds[0])
		{
			return -1;
		}
		int last = bounds.length - 1;
		if (value >= bounds[last])
		{
			return last;
		}
		int left = 0;
		int right = last;
		// 二分查找
		while (left < right)
		{
			int mid = (left + right) >>> 1;
			if (mid == left)// 迭代终止
				return mid;
			if (value >= bounds[mid])
			{
				left = mid;
			}
			else
			{
				right = mid;
			}
		}
		return left;
	}

	public final static int floor_idx(int value, int... bounds)
	{
		if (bounds == null || bounds.length == 0 || value < bounds[0])
		{
			return -1;
		}
		int last = bounds.length - 1;
		if (value >= bounds[last])
		{
			return last;
		}
		int left = 0;
		int right = last;
		// 二分查找
		while (left < right)
		{
			int mid = (left + right) >>> 1;
			if (mid == left)// 迭代终止
				return mid;
			if (value >= bounds[mid])
			{
				left = mid;
			}
			else
			{
				right = mid;
			}
		}
		return left;
	}

	/**
	 * 求2为底的对数，用于2的整数次幂的快速算法
	 * 
	 * @param num
	 * @return -1为无效结果
	 */
	public static final int uint64_log2(long uint64)
	{
		if (uint64 == 0)// 非法值
			return -1;
		int power = 0;
		long i = 0x01;
		while (i != uint64)
		{
			++power;
			if (i == 0)// 溢出
				return -1;
			i <<= 1;
		}
		return power;
	}
}

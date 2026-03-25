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
}

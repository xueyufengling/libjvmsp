package jvmsp.hotspot.memory;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.gc.shared.CollectedHeap;
import jvmsp.memory.memory_object;

/**
 * JVM的堆
 */
public class Universe
{
	private static final long _collectedHeap = vm_struct.entry.find("Universe", "_collectedHeap").address;// 静态，堆的基地址

	public static final long _collectedHeap()
	{
		return unsafe.read_pointer(_collectedHeap);
	}

	public static final CollectedHeap heap()
	{
		return memory_object.as_memory_object(CollectedHeap.class, _collectedHeap);
	}
}

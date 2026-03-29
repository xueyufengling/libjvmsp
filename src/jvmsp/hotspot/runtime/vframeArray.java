package jvmsp.hotspot.runtime;

import jvmsp.hotspot.vm_struct;
import jvmsp.hotspot.memory.CHeapObj;

public class vframeArray extends CHeapObj
{
	public static final String type_name = "vframeArray";
	public static final long size = sizeof(type_name);

	private static final long _original = vm_struct.entry.find("vframeArray", "_original").offset;// 8
	private static final long _caller = vm_struct.entry.find("vframeArray", "_caller").offset;// 64
	private static final long _frames = vm_struct.entry.find("vframeArray", "_frames").offset;// 188

	public vframeArray(long address)
	{
		super(type_name, address);
	}
}
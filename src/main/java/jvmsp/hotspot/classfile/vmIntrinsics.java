package jvmsp.hotspot.classfile;

import jvmsp.hotspot.vm_constant;
import jvmsp.hotspot.memory.AllStatic;

public class vmIntrinsics extends AllStatic
{
	public static final String type_name = "vmIntrinsics";

	public static final int _invokeBasic = vm_constant.find_int("vmIntrinsics::_invokeBasic");
	public static final int _linkToVirtual = vm_constant.find_int("vmIntrinsics::_linkToVirtual");
	public static final int _linkToStatic = vm_constant.find_int("vmIntrinsics::_linkToStatic");
	public static final int _linkToSpecial = vm_constant.find_int("vmIntrinsics::_linkToSpecial");
	public static final int _linkToInterface = vm_constant.find_int("vmIntrinsics::_linkToInterface");
	public static final int _linkToNative = vm_constant.find_int("vmIntrinsics::_linkToNative");

	private vmIntrinsics()
	{
		super(type_name);
	}
}

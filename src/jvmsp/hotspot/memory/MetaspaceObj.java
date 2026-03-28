package jvmsp.hotspot.memory;

import jvmsp.unsafe;
import jvmsp.hotspot.vm_struct;

public abstract class MetaspaceObj extends vm_struct
{
	public static final String type_name = "MetaspaceObj";
	public static final long size = sizeof(type_name);

	// JDK26+改为_aot_metaspace_base
	public static final long _shared_metaspace_base = vm_struct.entry.find(type_name, "_shared_metaspace_base").address;
	public static final long _shared_metaspace_top = vm_struct.entry.find(type_name, "_shared_metaspace_top").address;

	public MetaspaceObj(String name, long address)
	{
		super(name, address);
	}

	public MetaspaceObj(long address)
	{
		this(type_name, address);
	}

	public static final long shared_metaspace_base()
	{
		return unsafe.read_pointer(_shared_metaspace_base);
	}

	public static final void set_shared_metaspace_base(long base)
	{
		unsafe.write_pointer(_shared_metaspace_base, base);
	}

	public static final long shared_metaspace_top()
	{
		return unsafe.read_pointer(_shared_metaspace_top);
	}

	public static final void set_shared_metaspace_top(long top)
	{
		unsafe.write_pointer(_shared_metaspace_top, top);
	}

	public static final void set_shared_metaspace_range(long base, long top)
	{
		set_shared_metaspace_base(base);
		set_shared_metaspace_top(top);
	}

	/**
	 * 必须返回MetaspaceObj.Type中定义的值
	 * 
	 * @return
	 */
	public abstract int type();

	/**
	 * 元空间对象的类型
	 */
	public static final class Type
	{
		public static final int ClassType = 0;
		public static final int SymbolType = 1;
		public static final int TypeArrayU1Type = 2;
		public static final int TypeArrayU2Type = 3;
		public static final int TypeArrayU4Type = 4;
		public static final int TypeArrayU8Type = 5;
		public static final int TypeArrayOtherType = 6;
		public static final int MethodType = 7;
		public static final int ConstMethodType = 8;
		public static final int MethodDataType = 9;
		public static final int ConstantPoolType = 10;
		public static final int ConstantPoolCacheType = 11;
		public static final int AnnotationsType = 12;
		public static final int MethodCountersType = 13;
		public static final int RecordComponentType = 14;
		public static final int KlassTrainingDataType = 15;
		public static final int MethodTrainingDataType = 16;
		public static final int CompileTrainingDataType = 17;
		public static final int AdapterHandlerEntryType = 18;
		public static final int AdapterFingerPrintType = 19;

		public static final int array_type(int elem_size)
		{
			switch (elem_size)
			{
			case 1:
				return TypeArrayU1Type;
			case 2:
				return TypeArrayU2Type;
			case 4:
				return TypeArrayU4Type;
			case 8:
				return TypeArrayU8Type;
			default:
				return TypeArrayOtherType;
			}
		}

		public static final String type_name(int type)
		{
			switch (type)
			{
			case ClassType:
				return "Class";
			case SymbolType:
				return "Symbol";
			case TypeArrayU1Type:
				return "TypeArrayU1";
			case TypeArrayU2Type:
				return "TypeArrayU2";
			case TypeArrayU4Type:
				return "TypeArrayU4";
			case TypeArrayU8Type:
				return "TypeArrayU8";
			case TypeArrayOtherType:
				return "TypeArrayOther";
			case MethodType:
				return "Method";
			case ConstMethodType:
				return "ConstMethod";
			case MethodDataType:
				return "MethodData";
			case ConstantPoolType:
				return "ConstantPool";
			case ConstantPoolCacheType:
				return "ConstantPoolCache";
			case AnnotationsType:
				return "Annotations";
			case MethodCountersType:
				return "MethodCounters";
			case RecordComponentType:
				return "RecordComponent";
			case KlassTrainingDataType:
				return "KlassTrainingData";
			case MethodTrainingDataType:
				return "MethodTrainingData";
			case CompileTrainingDataType:
				return "CompileTrainingData";
			case AdapterHandlerEntryType:
				return "AdapterHandlerEntry";
			case AdapterFingerPrintType:
				return "AdapterFingerPrint";
			default:
				return null;
			}
		}
	}
}

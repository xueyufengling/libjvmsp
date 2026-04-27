package jvmsp.hotspot.utilities;

import jvmsp.memory;
import jvmsp.type.cxx_type;
import jvmsp.hotspot.vm_constant;
import jvmsp.hotspot.vm_struct;

/**
 * FakeRtti实际继承自FakeRttiSupport<T, TagType>。<br>
 * typedef FakeRttiSupport<BarrierSet, Name> FakeRtti;<br>
 */
public class FakeRtti extends vm_struct
{
	public static final String type_name = "FakeRtti";

	public static final cxx_type FakeRtti = cxx_type.define(type_name)
			// typedef intptr_t intx;
			// typedef uintptr_t uintx;
			// uintx _tag_set;
			.decl_field("_tag_set", globalDefinitions.uintx)
			// Name _concrete_tag;
			.decl_field("_concrete_tag", cxx_type._int)
			.resolve();

	public static final long size = FakeRtti.size();

	private static final long _tag_set = FakeRtti.field("_tag_set").offset();
	private static final long _concrete_tag = FakeRtti.field("_concrete_tag").offset();

	public static abstract class Name
	{
		public static final int ModRef = vm_constant.find_int_or("BarrierSet::ModRef", -1);
		public static final int CardTableBarrierSet = vm_constant.find_int_or("BarrierSet::CardTableBarrierSet", 0);// 枚举在源文件中定义为0，但运行时获取值为1
		public static final int EpsilonBarrierSet = vm_constant.find_int_or("BarrierSet::CardTableBarrierSet", 1);
		public static final int G1BarrierSet = vm_constant.find_int_or("BarrierSet::G1BarrierSet", 2);
		public static final int ShenandoahBarrierSet = vm_constant.find_int_or("BarrierSet::CardTableBarrierSet", 3);
		public static final int ZBarrierSet = vm_constant.find_int_or("BarrierSet::CardTableBarrierSet", 4);
	}

	public FakeRtti(long address)
	{
		super(type_name, address);
	}

	public long tag_set()
	{
		return super.read_byte(_tag_set);
	}

	public void set_tag_set(long tag_set)
	{
		super.write(_tag_set, tag_set);
	}

	/**
	 * 必须是Name中定义的值。<br>
	 * 
	 * @return
	 */
	public int concrete_tag()
	{
		return super.read_cint(_concrete_tag);
	}

	public void set_concrete_tag(int concrete_tag)
	{
		super.write_cint(_concrete_tag, concrete_tag);
	}

	public boolean has_tag(int tag)
	{
		return memory.flag_bit(tag_set(), tag);
	}

	public void set_tag(int tag, boolean value)
	{
		set_tag_set(memory.set_flag_bit(tag_set(), tag_bit(tag), value));
	}

	public void add_tag(int tag)
	{
		set_tag(tag, true);
	}

	public void clear_tag(int tag)
	{
		set_tag(tag, false);
	}

	public static final long tag_bit(int tag)
	{
		return 1 << validate_tag(tag);
	}

	public static final int validate_tag(int tag)
	{
		assert 0 <= tag : "Tag '" + tag + "' is negative ";
		assert tag < globalDefinitions.BitsPerWord : "Tag '" + tag + "' is too large";
		return tag;
	}
}

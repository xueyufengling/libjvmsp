package jvmsp.hotspot._native.include;

import jvmsp.type.cxx_type;
import jvmsp.hotspot.vm_constant;

/**
 * JVM标准常量头文件
 */
public class classfile_constants
{
	// 类、字段、方法通用标志

	/**
	 * 是否是public修饰的
	 */
	public static final short JVM_ACC_PUBLIC = 0x0001;

	/**
	 * 是否是private修饰的
	 */
	public static final short JVM_ACC_PRIVATE = 0x0002;

	/**
	 * 是否是protected修饰的
	 */
	public static final short JVM_ACC_PROTECTED = 0x0004;

	/**
	 * 是否是final修饰的
	 */
	public static final short JVM_ACC_FINAL = 0x0010;

	/**
	 * 是否是编译器自动生成的
	 */
	public static final short JVM_ACC_SYNTHETIC = 0x1000;

	// 类、字段通用标志

	/**
	 * 是否是枚举
	 */
	public static final short JVM_ACC_ENUM = 0x4000;

	// 类、方法通用标志

	/**
	 * 是否是abstract修饰的
	 */
	public static final short JVM_ACC_ABSTRACT = 0x0400;

	// 字段、方法通用标志

	/**
	 * 是否是static修饰的
	 */
	public static final short JVM_ACC_STATIC = 0x0008;

	// 类专用标志

	/**
	 * 类专用，invokespecial使用
	 */
	public static final short JVM_ACC_SUPER = 0x0020;

	/**
	 * 类专用，是否是接口
	 */
	public static final short JVM_ACC_INTERFACE = 0x0200;

	/**
	 * 类专用，是否是注解
	 */
	public static final short JVM_ACC_ANNOTATION = 0x2000;

	// 字段专用标志

	/**
	 * 字段专用，是否是transient修饰的，序列化与反序列化使用
	 */
	public static final short JVM_ACC_TRANSIENT = 0x0080;

	/**
	 * 字段专用，是否是volatile修饰的
	 */
	public static final short JVM_ACC_VOLATILE = 0x0040;

	// 方法专用标志

	/**
	 * 方法专用，是否加锁
	 */
	public static final short JVM_ACC_SYNCHRONIZED = 0x0020;

	/**
	 * 方法专用，是否是泛型编译生成的桥接方法
	 */
	public static final short JVM_ACC_BRIDGE = 0x0040;

	/**
	 * 方法专用，是否是变长参数
	 */
	public static final short JVM_ACC_VARARGS = 0x0080;

	/**
	 * 方法专用，是否是native修饰的
	 */
	public static final short JVM_ACC_NATIVE = 0x0100;

	/**
	 * 方法专用，strictfp即严格浮点运算
	 */
	public static final short JVM_ACC_STRICT = 0x0800;

	/**
	 * JVM识别类的标志掩码
	 */
	public static final short JVM_RECOGNIZED_CLASS_MODIFIERS = (JVM_ACC_PUBLIC |
			JVM_ACC_FINAL |
			JVM_ACC_SUPER |
			JVM_ACC_INTERFACE |
			JVM_ACC_ABSTRACT |
			JVM_ACC_ANNOTATION |
			JVM_ACC_ENUM |
			JVM_ACC_SYNTHETIC);

	/**
	 * JVM识别字段的标志掩码
	 */
	public static final short JVM_RECOGNIZED_FIELD_MODIFIERS = (JVM_ACC_PUBLIC |
			JVM_ACC_PRIVATE |
			JVM_ACC_PROTECTED |
			JVM_ACC_STATIC |
			JVM_ACC_FINAL |
			JVM_ACC_VOLATILE |
			JVM_ACC_TRANSIENT |
			JVM_ACC_ENUM |
			JVM_ACC_SYNTHETIC);

	/**
	 * JVM识别方法的标志掩码
	 */
	public static final short JVM_RECOGNIZED_METHOD_MODIFIERS = (JVM_ACC_PUBLIC |
			JVM_ACC_PRIVATE |
			JVM_ACC_PROTECTED |
			JVM_ACC_STATIC |
			JVM_ACC_FINAL |
			JVM_ACC_SYNCHRONIZED |
			JVM_ACC_BRIDGE |
			JVM_ACC_VARARGS |
			JVM_ACC_NATIVE |
			JVM_ACC_ABSTRACT |
			JVM_ACC_STRICT |
			JVM_ACC_SYNTHETIC);

	public static final int JVM_ACC_PUBLIC_BIT = 0;
	public static final int JVM_ACC_PRIVATE_BIT = 1;
	public static final int JVM_ACC_PROTECTED_BIT = 2;
	public static final int JVM_ACC_STATIC_BIT = 3;
	public static final int JVM_ACC_FINAL_BIT = 4;
	public static final int JVM_ACC_SYNCHRONIZED_BIT = 5;
	public static final int JVM_ACC_SUPER_BIT = 5;
	public static final int JVM_ACC_VOLATILE_BIT = 6;
	public static final int JVM_ACC_BRIDGE_BIT = 6;
	public static final int JVM_ACC_TRANSIENT_BIT = 7;
	public static final int JVM_ACC_VARARGS_BIT = 7;
	public static final int JVM_ACC_NATIVE_BIT = 8;
	public static final int JVM_ACC_INTERFACE_BIT = 9;
	public static final int JVM_ACC_ABSTRACT_BIT = 10;
	public static final int JVM_ACC_STRICT_BIT = 11;
	public static final int JVM_ACC_SYNTHETIC_BIT = 12;
	public static final int JVM_ACC_ANNOTATION_BIT = 13;
	public static final int JVM_ACC_ENUM_BIT = 14;

	public static final int JVM_T_BOOLEAN = 4;
	public static final int JVM_T_CHAR = 5;
	public static final int JVM_T_FLOAT = 6;
	public static final int JVM_T_DOUBLE = 7;
	public static final int JVM_T_BYTE = 8;
	public static final int JVM_T_SHORT = 9;
	public static final int JVM_T_INT = 10;
	public static final int JVM_T_LONG = 11;

	public static final byte JVM_CONSTANT_Utf8 = (byte) vm_constant.find_int("JVM_CONSTANT_Utf8");// 1
	public static final byte JVM_CONSTANT_Unicode = (byte) vm_constant.find_int("JVM_CONSTANT_Unicode");// 2
	public static final byte JVM_CONSTANT_Integer = (byte) vm_constant.find_int("JVM_CONSTANT_Integer");// 3
	public static final byte JVM_CONSTANT_Float = (byte) vm_constant.find_int("JVM_CONSTANT_Float");// 4
	public static final byte JVM_CONSTANT_Long = (byte) vm_constant.find_int("JVM_CONSTANT_Long");// 5
	public static final byte JVM_CONSTANT_Double = (byte) vm_constant.find_int("JVM_CONSTANT_Double");// 6
	public static final byte JVM_CONSTANT_Class = (byte) vm_constant.find_int("JVM_CONSTANT_Class");// 7
	public static final byte JVM_CONSTANT_String = (byte) vm_constant.find_int("JVM_CONSTANT_String");// 8
	public static final byte JVM_CONSTANT_Fieldref = (byte) vm_constant.find_int("JVM_CONSTANT_Fieldref");// 9
	public static final byte JVM_CONSTANT_Methodref = (byte) vm_constant.find_int("JVM_CONSTANT_Methodref");// 10
	public static final byte JVM_CONSTANT_InterfaceMethodref = (byte) vm_constant.find_int("JVM_CONSTANT_InterfaceMethodref");// 11
	public static final byte JVM_CONSTANT_NameAndType = (byte) vm_constant.find_int("JVM_CONSTANT_NameAndType");// 12
	public static final byte JVM_CONSTANT_MethodHandle = (byte) vm_constant.find_int("JVM_CONSTANT_MethodHandle");// 15
	public static final byte JVM_CONSTANT_MethodType = (byte) vm_constant.find_int("JVM_CONSTANT_MethodType");// 16
	public static final byte JVM_CONSTANT_Dynamic = (byte) vm_constant.find_int("JVM_CONSTANT_Dynamic");// 17
	public static final byte JVM_CONSTANT_InvokeDynamic = (byte) vm_constant.find_int("JVM_CONSTANT_InvokeDynamic");// 18
	public static final byte JVM_CONSTANT_Module = (byte) vm_constant.find_int("JVM_CONSTANT_Module");// 19
	public static final byte JVM_CONSTANT_Package = (byte) vm_constant.find_int("JVM_CONSTANT_Package");// 20
	public static final byte JVM_CONSTANT_ExternalMax = (byte) vm_constant.find_int("JVM_CONSTANT_ExternalMax");// 20
	public static final byte JVM_CONSTANT_Invalid = (byte) vm_constant.find_int("JVM_CONSTANT_Invalid");
	public static final byte JVM_CONSTANT_InternalMin = (byte) vm_constant.find_int("JVM_CONSTANT_InternalMin");
	public static final byte JVM_CONSTANT_UnresolvedClass = (byte) vm_constant.find_int("JVM_CONSTANT_UnresolvedClass");
	public static final byte JVM_CONSTANT_ClassIndex = (byte) vm_constant.find_int("JVM_CONSTANT_ClassIndex");
	public static final byte JVM_CONSTANT_StringIndex = (byte) vm_constant.find_int("JVM_CONSTANT_StringIndex");
	public static final byte JVM_CONSTANT_UnresolvedClassInError = (byte) vm_constant.find_int("JVM_CONSTANT_UnresolvedClassInError");
	public static final byte JVM_CONSTANT_MethodHandleInError = (byte) vm_constant.find_int("JVM_CONSTANT_MethodHandleInError");
	public static final byte JVM_CONSTANT_MethodTypeInError = (byte) vm_constant.find_int("JVM_CONSTANT_MethodTypeInError");
	public static final byte JVM_CONSTANT_DynamicInError = (byte) vm_constant.find_int("JVM_CONSTANT_DynamicInError");
	public static final byte JVM_CONSTANT_InternalMax = (byte) vm_constant.find_int("JVM_CONSTANT_InternalMax");

	// JVM引用类型
	public static final int JVM_REF_getField = 1;
	public static final int JVM_REF_getStatic = 2;
	public static final int JVM_REF_putField = 3;
	public static final int JVM_REF_putStatic = 4;
	public static final int JVM_REF_invokeVirtual = 5;
	public static final int JVM_REF_invokeStatic = 6;
	public static final int JVM_REF_invokeSpecial = 7;
	public static final int JVM_REF_newInvokeSpecial = 8;
	public static final int JVM_REF_invokeInterface = 9;

	/* StackMapTable type item numbers */
	public static final int JVM_ITEM_Top = 0;
	public static final int JVM_ITEM_Integer = 1;
	public static final int JVM_ITEM_Float = 2;
	public static final int JVM_ITEM_Double = 3;
	public static final int JVM_ITEM_Long = 4;
	public static final int JVM_ITEM_Null = 5;
	public static final int JVM_ITEM_UninitializedThis = 6;
	public static final int JVM_ITEM_Object = 7;
	public static final int JVM_ITEM_Uninitialized = 8;

	/* Type signatures */
	public static final int JVM_SIGNATURE_SLASH = '/';
	public static final int JVM_SIGNATURE_DOT = '.';
	public static final int JVM_SIGNATURE_SPECIAL = '<';
	public static final int JVM_SIGNATURE_ENDSPECIAL = '>';
	public static final int JVM_SIGNATURE_ARRAY = '[';
	public static final int JVM_SIGNATURE_BYTE = 'B';
	public static final int JVM_SIGNATURE_CHAR = 'C';
	public static final int JVM_SIGNATURE_CLASS = 'L';
	public static final int JVM_SIGNATURE_ENDCLASS = ';';
	public static final int JVM_SIGNATURE_ENUM = 'E';
	public static final int JVM_SIGNATURE_FLOAT = 'F';
	public static final int JVM_SIGNATURE_DOUBLE = 'D';
	public static final int JVM_SIGNATURE_FUNC = '(';
	public static final int JVM_SIGNATURE_ENDFUNC = ')';
	public static final int JVM_SIGNATURE_INT = 'I';
	public static final int JVM_SIGNATURE_LONG = 'J';
	public static final int JVM_SIGNATURE_SHORT = 'S';
	public static final int JVM_SIGNATURE_VOID = 'V';
	public static final int JVM_SIGNATURE_BOOLEAN = 'Z';

	/* Opcodes */
	public static final byte JVM_OPC_nop = 0x00;
	public static final byte JVM_OPC_aconst_null = 0x01;
	public static final byte JVM_OPC_iconst_m1 = 0x02;
	public static final byte JVM_OPC_iconst_0 = 0x03;
	public static final byte JVM_OPC_iconst_1 = 0x04;
	public static final byte JVM_OPC_iconst_2 = 0x05;
	public static final byte JVM_OPC_iconst_3 = 0x06;
	public static final byte JVM_OPC_iconst_4 = 0x07;
	public static final byte JVM_OPC_iconst_5 = 0x08;
	public static final byte JVM_OPC_lconst_0 = 0x09;
	public static final byte JVM_OPC_lconst_1 = 0x0A;
	public static final byte JVM_OPC_fconst_0 = 0x0B;
	public static final byte JVM_OPC_fconst_1 = 0x0C;
	public static final byte JVM_OPC_fconst_2 = 0x0D;
	public static final byte JVM_OPC_dconst_0 = 0x0E;
	public static final byte JVM_OPC_dconst_1 = 0x0F;
	public static final byte JVM_OPC_bipush = 0x10;
	public static final byte JVM_OPC_sipush = 0x11;
	public static final byte JVM_OPC_ldc = 0x12;
	public static final byte JVM_OPC_ldc_w = 0x13;
	public static final byte JVM_OPC_ldc2_w = 0x14;
	public static final byte JVM_OPC_iload = 0x15;
	public static final byte JVM_OPC_lload = 0x16;
	public static final byte JVM_OPC_fload = 0x17;
	public static final byte JVM_OPC_dload = 0x18;
	public static final byte JVM_OPC_aload = 0x19;
	public static final byte JVM_OPC_iload_0 = 0x1A;
	public static final byte JVM_OPC_iload_1 = 0x1B;
	public static final byte JVM_OPC_iload_2 = 0x1C;
	public static final byte JVM_OPC_iload_3 = 0x1D;
	public static final byte JVM_OPC_lload_0 = 0x1E;
	public static final byte JVM_OPC_lload_1 = 0x1F;
	public static final byte JVM_OPC_lload_2 = 0x20;
	public static final byte JVM_OPC_lload_3 = 0x21;
	public static final byte JVM_OPC_fload_0 = 0x22;
	public static final byte JVM_OPC_fload_1 = 0x23;
	public static final byte JVM_OPC_fload_2 = 0x24;
	public static final byte JVM_OPC_fload_3 = 0x25;
	public static final byte JVM_OPC_dload_0 = 0x26;
	public static final byte JVM_OPC_dload_1 = 0x27;
	public static final byte JVM_OPC_dload_2 = 0x28;
	public static final byte JVM_OPC_dload_3 = 0x29;
	public static final byte JVM_OPC_aload_0 = 0x2A;
	public static final byte JVM_OPC_aload_1 = 0x2B;
	public static final byte JVM_OPC_aload_2 = 0x2C;
	public static final byte JVM_OPC_aload_3 = 0x2D;
	public static final byte JVM_OPC_iaload = 0x2E;
	public static final byte JVM_OPC_laload = 0x2F;
	public static final byte JVM_OPC_faload = 0x30;
	public static final byte JVM_OPC_daload = 0x31;
	public static final byte JVM_OPC_aaload = 0x32;
	public static final byte JVM_OPC_baload = 0x33;
	public static final byte JVM_OPC_caload = 0x34;
	public static final byte JVM_OPC_saload = 0x35;
	public static final byte JVM_OPC_istore = 0x36;
	public static final byte JVM_OPC_lstore = 0x37;
	public static final byte JVM_OPC_fstore = 0x38;
	public static final byte JVM_OPC_dstore = 0x39;
	public static final byte JVM_OPC_astore = 0x3A;
	public static final byte JVM_OPC_istore_0 = 0x3B;
	public static final byte JVM_OPC_istore_1 = 0x3C;
	public static final byte JVM_OPC_istore_2 = 0x3D;
	public static final byte JVM_OPC_istore_3 = 0x3E;
	public static final byte JVM_OPC_lstore_0 = 0x3F;
	public static final byte JVM_OPC_lstore_1 = 0x40;
	public static final byte JVM_OPC_lstore_2 = 0x41;
	public static final byte JVM_OPC_lstore_3 = 0x42;
	public static final byte JVM_OPC_fstore_0 = 0x43;
	public static final byte JVM_OPC_fstore_1 = 0x44;
	public static final byte JVM_OPC_fstore_2 = 0x45;
	public static final byte JVM_OPC_fstore_3 = 0x46;
	public static final byte JVM_OPC_dstore_0 = 0x47;
	public static final byte JVM_OPC_dstore_1 = 0x48;
	public static final byte JVM_OPC_dstore_2 = 0x49;
	public static final byte JVM_OPC_dstore_3 = 0x4A;
	public static final byte JVM_OPC_astore_0 = 0x4B;
	public static final byte JVM_OPC_astore_1 = 0x4C;
	public static final byte JVM_OPC_astore_2 = 0x4D;
	public static final byte JVM_OPC_astore_3 = 0x4E;
	public static final byte JVM_OPC_iastore = 0x4F;
	public static final byte JVM_OPC_lastore = 0x50;
	public static final byte JVM_OPC_fastore = 0x51;
	public static final byte JVM_OPC_dastore = 0x52;
	public static final byte JVM_OPC_aastore = 0x53;
	public static final byte JVM_OPC_bastore = 0x54;
	public static final byte JVM_OPC_castore = 0x55;
	public static final byte JVM_OPC_sastore = 0x56;
	public static final byte JVM_OPC_pop = 0x57;
	public static final byte JVM_OPC_pop2 = 0x58;
	public static final byte JVM_OPC_dup = 0x59;
	public static final byte JVM_OPC_dup_x1 = 0x5A;
	public static final byte JVM_OPC_dup_x2 = 0x5B;
	public static final byte JVM_OPC_dup2 = 0x5C;
	public static final byte JVM_OPC_dup2_x1 = 0x5D;
	public static final byte JVM_OPC_dup2_x2 = 0x5E;
	public static final byte JVM_OPC_swap = 0x5F;
	public static final byte JVM_OPC_iadd = 0x60;
	public static final byte JVM_OPC_ladd = 0x61;
	public static final byte JVM_OPC_fadd = 0x62;
	public static final byte JVM_OPC_dadd = 0x63;
	public static final byte JVM_OPC_isub = 0x64;
	public static final byte JVM_OPC_lsub = 0x65;
	public static final byte JVM_OPC_fsub = 0x66;
	public static final byte JVM_OPC_dsub = 0x67;
	public static final byte JVM_OPC_imul = 0x68;
	public static final byte JVM_OPC_lmul = 0x69;
	public static final byte JVM_OPC_fmul = 0x6A;
	public static final byte JVM_OPC_dmul = 0x6B;
	public static final byte JVM_OPC_idiv = 0x6C;
	public static final byte JVM_OPC_ldiv = 0x6D;
	public static final byte JVM_OPC_fdiv = 0x6E;
	public static final byte JVM_OPC_ddiv = 0x6F;
	public static final byte JVM_OPC_irem = 0x70;
	public static final byte JVM_OPC_lrem = 0x71;
	public static final byte JVM_OPC_frem = 0x72;
	public static final byte JVM_OPC_drem = 0x73;
	public static final byte JVM_OPC_ineg = 0x74;
	public static final byte JVM_OPC_lneg = 0x75;
	public static final byte JVM_OPC_fneg = 0x76;
	public static final byte JVM_OPC_dneg = 0x77;
	public static final byte JVM_OPC_ishl = 0x78;
	public static final byte JVM_OPC_lshl = 0x79;
	public static final byte JVM_OPC_ishr = 0x7A;
	public static final byte JVM_OPC_lshr = 0x7B;
	public static final byte JVM_OPC_iushr = 0x7C;
	public static final byte JVM_OPC_lushr = 0x7D;
	public static final byte JVM_OPC_iand = 0x7E;
	public static final byte JVM_OPC_land = 0x7F;
	public static final byte JVM_OPC_ior = (byte) 0x80;
	public static final byte JVM_OPC_lor = (byte) 0x81;
	public static final byte JVM_OPC_ixor = (byte) 0x82;
	public static final byte JVM_OPC_lxor = (byte) 0x83;
	public static final byte JVM_OPC_iinc = (byte) 0x84;
	public static final byte JVM_OPC_i2l = (byte) 0x85;
	public static final byte JVM_OPC_i2f = (byte) 0x86;
	public static final byte JVM_OPC_i2d = (byte) 0x87;
	public static final byte JVM_OPC_l2i = (byte) 0x88;
	public static final byte JVM_OPC_l2f = (byte) 0x89;
	public static final byte JVM_OPC_l2d = (byte) 0x8A;
	public static final byte JVM_OPC_f2i = (byte) 0x8B;
	public static final byte JVM_OPC_f2l = (byte) 0x8C;
	public static final byte JVM_OPC_f2d = (byte) 0x8D;
	public static final byte JVM_OPC_d2i = (byte) 0x8E;
	public static final byte JVM_OPC_d2l = (byte) 0x8F;
	public static final byte JVM_OPC_d2f = (byte) 0x90;
	public static final byte JVM_OPC_i2b = (byte) 0x91;
	public static final byte JVM_OPC_i2c = (byte) 0x92;
	public static final byte JVM_OPC_i2s = (byte) 0x93;
	public static final byte JVM_OPC_lcmp = (byte) 0x94;
	public static final byte JVM_OPC_fcmpl = (byte) 0x95;
	public static final byte JVM_OPC_fcmpg = (byte) 0x96;
	public static final byte JVM_OPC_dcmpl = (byte) 0x97;
	public static final byte JVM_OPC_dcmpg = (byte) 0x98;
	public static final byte JVM_OPC_ifeq = (byte) 0x99;
	public static final byte JVM_OPC_ifne = (byte) 0x9A;
	public static final byte JVM_OPC_iflt = (byte) 0x9B;
	public static final byte JVM_OPC_ifge = (byte) 0x9C;
	public static final byte JVM_OPC_ifgt = (byte) 0x9D;
	public static final byte JVM_OPC_ifle = (byte) 0x9E;
	public static final byte JVM_OPC_if_icmpeq = (byte) 0x9F;
	public static final byte JVM_OPC_if_icmpne = (byte) 0xA0;
	public static final byte JVM_OPC_if_icmplt = (byte) 0xA1;
	public static final byte JVM_OPC_if_icmpge = (byte) 0xA2;
	public static final byte JVM_OPC_if_icmpgt = (byte) 0xA3;
	public static final byte JVM_OPC_if_icmple = (byte) 0xA4;
	public static final byte JVM_OPC_if_acmpeq = (byte) 0xA5;
	public static final byte JVM_OPC_if_acmpne = (byte) 0xA6;
	public static final byte JVM_OPC_goto = (byte) 0xA7;
	public static final byte JVM_OPC_jsr = (byte) 0xA8;
	public static final byte JVM_OPC_ret = (byte) 0xA9;
	public static final byte JVM_OPC_tableswitch = (byte) 0xAA;
	public static final byte JVM_OPC_lookupswitch = (byte) 0xAB;
	public static final byte JVM_OPC_ireturn = (byte) 0xAC;
	public static final byte JVM_OPC_lreturn = (byte) 0xAD;
	public static final byte JVM_OPC_freturn = (byte) 0xAE;
	public static final byte JVM_OPC_dreturn = (byte) 0xAF;
	public static final byte JVM_OPC_areturn = (byte) 0xB0;
	public static final byte JVM_OPC_return = (byte) 0xB1;
	public static final byte JVM_OPC_getstatic = (byte) 0xB2;
	public static final byte JVM_OPC_putstatic = (byte) 0xB3;
	public static final byte JVM_OPC_getfield = (byte) 0xB4;
	public static final byte JVM_OPC_putfield = (byte) 0xB5;
	public static final byte JVM_OPC_invokevirtual = (byte) 0xB6;
	public static final byte JVM_OPC_invokespecial = (byte) 0xB7;
	public static final byte JVM_OPC_invokestatic = (byte) 0xB8;
	public static final byte JVM_OPC_invokeinterface = (byte) 0xB9;
	public static final byte JVM_OPC_invokedynamic = (byte) 0xBA;
	public static final byte JVM_OPC_new = (byte) 0xBB;
	public static final byte JVM_OPC_newarray = (byte) 0xBC;
	public static final byte JVM_OPC_anewarray = (byte) 0xBD;
	public static final byte JVM_OPC_arraylength = (byte) 0xBE;
	public static final byte JVM_OPC_athrow = (byte) 0xBF;
	public static final byte JVM_OPC_checkcast = (byte) 0xC0;
	public static final byte JVM_OPC_instanceof = (byte) 0xC1;
	public static final byte JVM_OPC_monitorenter = (byte) 0xC2;
	public static final byte JVM_OPC_monitorexit = (byte) 0xC3;
	public static final byte JVM_OPC_wide = (byte) 0xC4;
	public static final byte JVM_OPC_multianewarray = (byte) 0xC5;
	public static final byte JVM_OPC_ifnull = (byte) 0xC6;
	public static final byte JVM_OPC_ifnonnull = (byte) 0xC7;
	public static final byte JVM_OPC_goto_w = (byte) 0xC8;
	public static final byte JVM_OPC_jsr_w = (byte) 0xC9;

	// 标准字节码总个数
	public static final int JVM_OPC_MAX = 0xC9;

	/*
	 * Opcode length initializer, use with something like:
	 * byte[] opcodeLength = JVM_OPCODE_LENGTH_INITIALIZER;
	 */
	public static final byte[] JVM_OPCODE_LENGTH_INITIALIZER =
	{
			1, // nop
			1, // aconst_null
			1, // iconst_m1
			1, // iconst_0
			1, // iconst_1
			1, // iconst_2
			1, // iconst_3
			1, // iconst_4
			1, // iconst_5
			1, // lconst_0
			1, // lconst_1
			1, // fconst_0
			1, // fconst_1
			1, // fconst_2
			1, // dconst_0
			1, // dconst_1
			2, // bipush
			3, // sipush
			2, // ldc
			3, // ldc_w
			3, // ldc2_w
			2, // iload
			2, // lload
			2, // fload
			2, // dload
			2, // aload
			1, // iload_0
			1, // iload_1
			1, // iload_2
			1, // iload_3
			1, // lload_0
			1, // lload_1
			1, // lload_2
			1, // lload_3
			1, // fload_0
			1, // fload_1
			1, // fload_2
			1, // fload_3
			1, // dload_0
			1, // dload_1
			1, // dload_2
			1, // dload_3
			1, // aload_0
			1, // aload_1
			1, // aload_2
			1, // aload_3
			1, // iaload
			1, // laload
			1, // faload
			1, // daload
			1, // aaload
			1, // baload
			1, // caload
			1, // saload
			2, // istore
			2, // lstore
			2, // fstore
			2, // dstore
			2, // astore
			1, // istore_0
			1, // istore_1
			1, // istore_2
			1, // istore_3
			1, // lstore_0
			1, // lstore_1
			1, // lstore_2
			1, // lstore_3
			1, // fstore_0
			1, // fstore_1
			1, // fstore_2
			1, // fstore_3
			1, // dstore_0
			1, // dstore_1
			1, // dstore_2
			1, // dstore_3
			1, // astore_0
			1, // astore_1
			1, // astore_2
			1, // astore_3
			1, // iastore
			1, // lastore
			1, // fastore
			1, // dastore
			1, // aastore
			1, // bastore
			1, // castore
			1, // sastore
			1, // pop
			1, // pop2
			1, // dup
			1, // dup_x1
			1, // dup_x2
			1, // dup2
			1, // dup2_x1
			1, // dup2_x2
			1, // swap
			1, // iadd
			1, // ladd
			1, // fadd
			1, // dadd
			1, // isub
			1, // lsub
			1, // fsub
			1, // dsub
			1, // imul
			1, // lmul
			1, // fmul
			1, // dmul
			1, // idiv
			1, // ldiv
			1, // fdiv
			1, // ddiv
			1, // irem
			1, // lrem
			1, // frem
			1, // drem
			1, // ineg
			1, // lneg
			1, // fneg
			1, // dneg
			1, // ishl
			1, // lshl
			1, // ishr
			1, // lshr
			1, // iushr
			1, // lushr
			1, // iand
			1, // land
			1, // ior
			1, // lor
			1, // ixor
			1, // lxor
			3, // iinc
			1, // i2l
			1, // i2f
			1, // i2d
			1, // l2i
			1, // l2f
			1, // l2d
			1, // f2i
			1, // f2l
			1, // f2d
			1, // d2i
			1, // d2l
			1, // d2f
			1, // i2b
			1, // i2c
			1, // i2s
			1, // lcmp
			1, // fcmpl
			1, // fcmpg
			1, // dcmpl
			1, // dcmpg
			3, // ifeq
			3, // ifne
			3, // iflt
			3, // ifge
			3, // ifgt
			3, // ifle
			3, // if_icmpeq
			3, // if_icmpne
			3, // if_icmplt
			3, // if_icmpge
			3, // if_icmpgt
			3, // if_icmple
			3, // if_acmpeq
			3, // if_acmpne
			3, // goto
			3, // jsr
			2, // ret
			99, // tableswitch
			99, // lookupswitch
			1, // ireturn
			1, // lreturn
			1, // freturn
			1, // dreturn
			1, // areturn
			1, // return
			3, // getstatic
			3, // putstatic
			3, // getfield
			3, // putfield
			3, // invokevirtual
			3, // invokespecial
			3, // invokestatic
			5, // invokeinterface
			5, // invokedynamic
			3, // new
			2, // newarray
			3, // anewarray
			1, // arraylength
			1, // athrow
			3, // checkcast
			3, // instanceof
			1, // monitorenter
			1, // monitorexit
			0, // wide
			4, // multianewarray
			3, // ifnull
			3, // ifnonnull
			5, // goto_w
			5 // jsr_w
	};

	public static final int instruction_length(byte bc)
	{
		return JVM_OPCODE_LENGTH_INITIALIZER[cxx_type.as_uint8_t(bc)];
	}
}

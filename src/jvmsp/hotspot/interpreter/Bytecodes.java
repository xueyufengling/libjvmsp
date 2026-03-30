package jvmsp.hotspot.interpreter;

import jvmsp.type.cxx_type;
import jvmsp.unsafe;
import jvmsp.hotspot._native.include.classfile_constants;
import jvmsp.hotspot.memory.AllStatic;

public class Bytecodes extends AllStatic
{
	public static final String type_name = "Bytecodes";

	private Bytecodes()
	{
		super(type_name);
	}

	/**
	 * 字节码值
	 */
	public static abstract class Code
	{
		public static final byte _illegal = -1; // 0xFF

		// Java标准字节码
		public static final byte _nop = classfile_constants.JVM_OPC_nop; // 0x00
		public static final byte _aconst_null = classfile_constants.JVM_OPC_aconst_null; // 0x01
		public static final byte _iconst_m1 = classfile_constants.JVM_OPC_iconst_m1; // 0x02
		public static final byte _iconst_0 = classfile_constants.JVM_OPC_iconst_0; // 0x03
		public static final byte _iconst_1 = classfile_constants.JVM_OPC_iconst_1; // 0x04
		public static final byte _iconst_2 = classfile_constants.JVM_OPC_iconst_2; // 0x05
		public static final byte _iconst_3 = classfile_constants.JVM_OPC_iconst_3; // 0x06
		public static final byte _iconst_4 = classfile_constants.JVM_OPC_iconst_4; // 0x07
		public static final byte _iconst_5 = classfile_constants.JVM_OPC_iconst_5; // 0x08
		public static final byte _lconst_0 = classfile_constants.JVM_OPC_lconst_0; // 0x09
		public static final byte _lconst_1 = classfile_constants.JVM_OPC_lconst_1; // 0x0A
		public static final byte _fconst_0 = classfile_constants.JVM_OPC_fconst_0; // 0x0B
		public static final byte _fconst_1 = classfile_constants.JVM_OPC_fconst_1; // 0x0C
		public static final byte _fconst_2 = classfile_constants.JVM_OPC_fconst_2; // 0x0D
		public static final byte _dconst_0 = classfile_constants.JVM_OPC_dconst_0; // 0x0E
		public static final byte _dconst_1 = classfile_constants.JVM_OPC_dconst_1; // 0x0F
		public static final byte _bipush = classfile_constants.JVM_OPC_bipush; // 0x10
		public static final byte _sipush = classfile_constants.JVM_OPC_sipush; // 0x11
		public static final byte _ldc = classfile_constants.JVM_OPC_ldc; // 0x12
		public static final byte _ldc_w = classfile_constants.JVM_OPC_ldc_w; // 0x13
		public static final byte _ldc2_w = classfile_constants.JVM_OPC_ldc2_w; // 0x14
		public static final byte _iload = classfile_constants.JVM_OPC_iload; // 0x15
		public static final byte _lload = classfile_constants.JVM_OPC_lload; // 0x16
		public static final byte _fload = classfile_constants.JVM_OPC_fload; // 0x17
		public static final byte _dload = classfile_constants.JVM_OPC_dload; // 0x18
		public static final byte _aload = classfile_constants.JVM_OPC_aload; // 0x19
		public static final byte _iload_0 = classfile_constants.JVM_OPC_iload_0; // 0x1A
		public static final byte _iload_1 = classfile_constants.JVM_OPC_iload_1; // 0x1B
		public static final byte _iload_2 = classfile_constants.JVM_OPC_iload_2; // 0x1C
		public static final byte _iload_3 = classfile_constants.JVM_OPC_iload_3; // 0x1D
		public static final byte _lload_0 = classfile_constants.JVM_OPC_lload_0; // 0x1E
		public static final byte _lload_1 = classfile_constants.JVM_OPC_lload_1; // 0x1F
		public static final byte _lload_2 = classfile_constants.JVM_OPC_lload_2; // 0x20
		public static final byte _lload_3 = classfile_constants.JVM_OPC_lload_3; // 0x21
		public static final byte _fload_0 = classfile_constants.JVM_OPC_fload_0; // 0x22
		public static final byte _fload_1 = classfile_constants.JVM_OPC_fload_1; // 0x23
		public static final byte _fload_2 = classfile_constants.JVM_OPC_fload_2; // 0x24
		public static final byte _fload_3 = classfile_constants.JVM_OPC_fload_3; // 0x25
		public static final byte _dload_0 = classfile_constants.JVM_OPC_dload_0; // 0x26
		public static final byte _dload_1 = classfile_constants.JVM_OPC_dload_1; // 0x27
		public static final byte _dload_2 = classfile_constants.JVM_OPC_dload_2; // 0x28
		public static final byte _dload_3 = classfile_constants.JVM_OPC_dload_3; // 0x29
		public static final byte _aload_0 = classfile_constants.JVM_OPC_aload_0; // 0x2A
		public static final byte _aload_1 = classfile_constants.JVM_OPC_aload_1; // 0x2B
		public static final byte _aload_2 = classfile_constants.JVM_OPC_aload_2; // 0x2C
		public static final byte _aload_3 = classfile_constants.JVM_OPC_aload_3; // 0x2D
		public static final byte _iaload = classfile_constants.JVM_OPC_iaload; // 0x2E
		public static final byte _laload = classfile_constants.JVM_OPC_laload; // 0x2F
		public static final byte _faload = classfile_constants.JVM_OPC_faload; // 0x30
		public static final byte _daload = classfile_constants.JVM_OPC_daload; // 0x31
		public static final byte _aaload = classfile_constants.JVM_OPC_aaload; // 0x32
		public static final byte _baload = classfile_constants.JVM_OPC_baload; // 0x33
		public static final byte _caload = classfile_constants.JVM_OPC_caload; // 0x34
		public static final byte _saload = classfile_constants.JVM_OPC_saload; // 0x35
		public static final byte _istore = classfile_constants.JVM_OPC_istore; // 0x36
		public static final byte _lstore = classfile_constants.JVM_OPC_lstore; // 0x37
		public static final byte _fstore = classfile_constants.JVM_OPC_fstore; // 0x38
		public static final byte _dstore = classfile_constants.JVM_OPC_dstore; // 0x39
		public static final byte _astore = classfile_constants.JVM_OPC_astore; // 0x3A
		public static final byte _istore_0 = classfile_constants.JVM_OPC_istore_0; // 0x3B
		public static final byte _istore_1 = classfile_constants.JVM_OPC_istore_1; // 0x3C
		public static final byte _istore_2 = classfile_constants.JVM_OPC_istore_2; // 0x3D
		public static final byte _istore_3 = classfile_constants.JVM_OPC_istore_3; // 0x3E
		public static final byte _lstore_0 = classfile_constants.JVM_OPC_lstore_0; // 0x3F
		public static final byte _lstore_1 = classfile_constants.JVM_OPC_lstore_1; // 0x40
		public static final byte _lstore_2 = classfile_constants.JVM_OPC_lstore_2; // 0x41
		public static final byte _lstore_3 = classfile_constants.JVM_OPC_lstore_3; // 0x42
		public static final byte _fstore_0 = classfile_constants.JVM_OPC_fstore_0; // 0x43
		public static final byte _fstore_1 = classfile_constants.JVM_OPC_fstore_1; // 0x44
		public static final byte _fstore_2 = classfile_constants.JVM_OPC_fstore_2; // 0x45
		public static final byte _fstore_3 = classfile_constants.JVM_OPC_fstore_3; // 0x46
		public static final byte _dstore_0 = classfile_constants.JVM_OPC_dstore_0; // 0x47
		public static final byte _dstore_1 = classfile_constants.JVM_OPC_dstore_1; // 0x48
		public static final byte _dstore_2 = classfile_constants.JVM_OPC_dstore_2; // 0x49
		public static final byte _dstore_3 = classfile_constants.JVM_OPC_dstore_3; // 0x4A
		public static final byte _astore_0 = classfile_constants.JVM_OPC_astore_0; // 0x4B
		public static final byte _astore_1 = classfile_constants.JVM_OPC_astore_1; // 0x4C
		public static final byte _astore_2 = classfile_constants.JVM_OPC_astore_2; // 0x4D
		public static final byte _astore_3 = classfile_constants.JVM_OPC_astore_3; // 0x4E
		public static final byte _iastore = classfile_constants.JVM_OPC_iastore; // 0x4F
		public static final byte _lastore = classfile_constants.JVM_OPC_lastore; // 0x50
		public static final byte _fastore = classfile_constants.JVM_OPC_fastore; // 0x51
		public static final byte _dastore = classfile_constants.JVM_OPC_dastore; // 0x52
		public static final byte _aastore = classfile_constants.JVM_OPC_aastore; // 0x53
		public static final byte _bastore = classfile_constants.JVM_OPC_bastore; // 0x54
		public static final byte _castore = classfile_constants.JVM_OPC_castore; // 0x55
		public static final byte _sastore = classfile_constants.JVM_OPC_sastore; // 0x56
		public static final byte _pop = classfile_constants.JVM_OPC_pop; // 0x57
		public static final byte _pop2 = classfile_constants.JVM_OPC_pop2; // 0x58
		public static final byte _dup = classfile_constants.JVM_OPC_dup; // 0x59
		public static final byte _dup_x1 = classfile_constants.JVM_OPC_dup_x1; // 0x5A
		public static final byte _dup_x2 = classfile_constants.JVM_OPC_dup_x2; // 0x5B
		public static final byte _dup2 = classfile_constants.JVM_OPC_dup2; // 0x5C
		public static final byte _dup2_x1 = classfile_constants.JVM_OPC_dup2_x1; // 0x5D
		public static final byte _dup2_x2 = classfile_constants.JVM_OPC_dup2_x2; // 0x5E
		public static final byte _swap = classfile_constants.JVM_OPC_swap; // 0x5F
		public static final byte _iadd = classfile_constants.JVM_OPC_iadd; // 0x60
		public static final byte _ladd = classfile_constants.JVM_OPC_ladd; // 0x61
		public static final byte _fadd = classfile_constants.JVM_OPC_fadd; // 0x62
		public static final byte _dadd = classfile_constants.JVM_OPC_dadd; // 0x63
		public static final byte _isub = classfile_constants.JVM_OPC_isub; // 0x64
		public static final byte _lsub = classfile_constants.JVM_OPC_lsub; // 0x65
		public static final byte _fsub = classfile_constants.JVM_OPC_fsub; // 0x66
		public static final byte _dsub = classfile_constants.JVM_OPC_dsub; // 0x67
		public static final byte _imul = classfile_constants.JVM_OPC_imul; // 0x68
		public static final byte _lmul = classfile_constants.JVM_OPC_lmul; // 0x69
		public static final byte _fmul = classfile_constants.JVM_OPC_fmul; // 0x6A
		public static final byte _dmul = classfile_constants.JVM_OPC_dmul; // 0x6B
		public static final byte _idiv = classfile_constants.JVM_OPC_idiv; // 0x6C
		public static final byte _ldiv = classfile_constants.JVM_OPC_ldiv; // 0x6D
		public static final byte _fdiv = classfile_constants.JVM_OPC_fdiv; // 0x6E
		public static final byte _ddiv = classfile_constants.JVM_OPC_ddiv; // 0x6F
		public static final byte _irem = classfile_constants.JVM_OPC_irem; // 0x70
		public static final byte _lrem = classfile_constants.JVM_OPC_lrem; // 0x71
		public static final byte _frem = classfile_constants.JVM_OPC_frem; // 0x72
		public static final byte _drem = classfile_constants.JVM_OPC_drem; // 0x73
		public static final byte _ineg = classfile_constants.JVM_OPC_ineg; // 0x74
		public static final byte _lneg = classfile_constants.JVM_OPC_lneg; // 0x75
		public static final byte _fneg = classfile_constants.JVM_OPC_fneg; // 0x76
		public static final byte _dneg = classfile_constants.JVM_OPC_dneg; // 0x77
		public static final byte _ishl = classfile_constants.JVM_OPC_ishl; // 0x78
		public static final byte _lshl = classfile_constants.JVM_OPC_lshl; // 0x79
		public static final byte _ishr = classfile_constants.JVM_OPC_ishr; // 0x7A
		public static final byte _lshr = classfile_constants.JVM_OPC_lshr; // 0x7B
		public static final byte _iushr = classfile_constants.JVM_OPC_iushr; // 0x7C
		public static final byte _lushr = classfile_constants.JVM_OPC_lushr; // 0x7D
		public static final byte _iand = classfile_constants.JVM_OPC_iand; // 0x7E
		public static final byte _land = classfile_constants.JVM_OPC_land; // 0x7F
		public static final byte _ior = classfile_constants.JVM_OPC_ior; // 0x80
		public static final byte _lor = classfile_constants.JVM_OPC_lor; // 0x81
		public static final byte _ixor = classfile_constants.JVM_OPC_ixor; // 0x82
		public static final byte _lxor = classfile_constants.JVM_OPC_lxor; // 0x83
		public static final byte _iinc = classfile_constants.JVM_OPC_iinc; // 0x84
		public static final byte _i2l = classfile_constants.JVM_OPC_i2l; // 0x85
		public static final byte _i2f = classfile_constants.JVM_OPC_i2f; // 0x86
		public static final byte _i2d = classfile_constants.JVM_OPC_i2d; // 0x87
		public static final byte _l2i = classfile_constants.JVM_OPC_l2i; // 0x88
		public static final byte _l2f = classfile_constants.JVM_OPC_l2f; // 0x89
		public static final byte _l2d = classfile_constants.JVM_OPC_l2d; // 0x8A
		public static final byte _f2i = classfile_constants.JVM_OPC_f2i; // 0x8B
		public static final byte _f2l = classfile_constants.JVM_OPC_f2l; // 0x8C
		public static final byte _f2d = classfile_constants.JVM_OPC_f2d; // 0x8D
		public static final byte _d2i = classfile_constants.JVM_OPC_d2i; // 0x8E
		public static final byte _d2l = classfile_constants.JVM_OPC_d2l; // 0x8F
		public static final byte _d2f = classfile_constants.JVM_OPC_d2f; // 0x90
		public static final byte _i2b = classfile_constants.JVM_OPC_i2b; // 0x91
		public static final byte _i2c = classfile_constants.JVM_OPC_i2c; // 0x92
		public static final byte _i2s = classfile_constants.JVM_OPC_i2s; // 0x93
		public static final byte _lcmp = classfile_constants.JVM_OPC_lcmp; // 0x94
		public static final byte _fcmpl = classfile_constants.JVM_OPC_fcmpl; // 0x95
		public static final byte _fcmpg = classfile_constants.JVM_OPC_fcmpg; // 0x96
		public static final byte _dcmpl = classfile_constants.JVM_OPC_dcmpl; // 0x97
		public static final byte _dcmpg = classfile_constants.JVM_OPC_dcmpg; // 0x98
		public static final byte _ifeq = classfile_constants.JVM_OPC_ifeq; // 0x99
		public static final byte _ifne = classfile_constants.JVM_OPC_ifne; // 0x9A
		public static final byte _iflt = classfile_constants.JVM_OPC_iflt; // 0x9B
		public static final byte _ifge = classfile_constants.JVM_OPC_ifge; // 0x9C
		public static final byte _ifgt = classfile_constants.JVM_OPC_ifgt; // 0x9D
		public static final byte _ifle = classfile_constants.JVM_OPC_ifle; // 0x9E
		public static final byte _if_icmpeq = classfile_constants.JVM_OPC_if_icmpeq; // 0x9F
		public static final byte _if_icmpne = classfile_constants.JVM_OPC_if_icmpne; // 0xA0
		public static final byte _if_icmplt = classfile_constants.JVM_OPC_if_icmplt; // 0xA1
		public static final byte _if_icmpge = classfile_constants.JVM_OPC_if_icmpge; // 0xA2
		public static final byte _if_icmpgt = classfile_constants.JVM_OPC_if_icmpgt; // 0xA3
		public static final byte _if_icmple = classfile_constants.JVM_OPC_if_icmple; // 0xA4
		public static final byte _if_acmpeq = classfile_constants.JVM_OPC_if_acmpeq; // 0xA5
		public static final byte _if_acmpne = classfile_constants.JVM_OPC_if_acmpne; // 0xA6
		public static final byte _goto = classfile_constants.JVM_OPC_goto; // 0xA7
		public static final byte _jsr = classfile_constants.JVM_OPC_jsr; // 0xA8
		public static final byte _ret = classfile_constants.JVM_OPC_ret; // 0xA9
		public static final byte _tableswitch = classfile_constants.JVM_OPC_tableswitch; // 0xAA
		public static final byte _lookupswitch = classfile_constants.JVM_OPC_lookupswitch; // 0xAB
		public static final byte _ireturn = classfile_constants.JVM_OPC_ireturn; // 0xAC
		public static final byte _lreturn = classfile_constants.JVM_OPC_lreturn; // 0xAD
		public static final byte _freturn = classfile_constants.JVM_OPC_freturn; // 0xAE
		public static final byte _dreturn = classfile_constants.JVM_OPC_dreturn; // 0xAF
		public static final byte _areturn = classfile_constants.JVM_OPC_areturn; // 0xB0
		public static final byte _return = classfile_constants.JVM_OPC_return; // 0xB1
		public static final byte _getstatic = classfile_constants.JVM_OPC_getstatic; // 0xB2
		public static final byte _putstatic = classfile_constants.JVM_OPC_putstatic; // 0xB3
		public static final byte _getfield = classfile_constants.JVM_OPC_getfield; // 0xB4
		public static final byte _putfield = classfile_constants.JVM_OPC_putfield; // 0xB5
		public static final byte _invokevirtual = classfile_constants.JVM_OPC_invokevirtual; // 0xB6
		public static final byte _invokespecial = classfile_constants.JVM_OPC_invokespecial; // 0xB7
		public static final byte _invokestatic = classfile_constants.JVM_OPC_invokestatic; // 0xB8
		public static final byte _invokeinterface = classfile_constants.JVM_OPC_invokeinterface; // 0xB9
		public static final byte _invokedynamic = classfile_constants.JVM_OPC_invokedynamic; // 0xBA
		public static final byte _new = classfile_constants.JVM_OPC_new; // 0xBB
		public static final byte _newarray = classfile_constants.JVM_OPC_newarray; // 0xBC
		public static final byte _anewarray = classfile_constants.JVM_OPC_anewarray; // 0xBD
		public static final byte _arraylength = classfile_constants.JVM_OPC_arraylength; // 0xBE
		public static final byte _athrow = classfile_constants.JVM_OPC_athrow; // 0xBF
		public static final byte _checkcast = classfile_constants.JVM_OPC_checkcast; // 0xC0
		public static final byte _instanceof = classfile_constants.JVM_OPC_instanceof; // 0xC1
		public static final byte _monitorenter = classfile_constants.JVM_OPC_monitorenter; // 0xC2
		public static final byte _monitorexit = classfile_constants.JVM_OPC_monitorexit; // 0xC3
		public static final byte _wide = classfile_constants.JVM_OPC_wide; // 0xC4
		public static final byte _multianewarray = classfile_constants.JVM_OPC_multianewarray; // 0xC5
		public static final byte _ifnull = classfile_constants.JVM_OPC_ifnull; // 0xC6
		public static final byte _ifnonnull = classfile_constants.JVM_OPC_ifnonnull; // 0xC7
		public static final byte _goto_w = classfile_constants.JVM_OPC_goto_w; // 0xC8
		public static final byte _jsr_w = classfile_constants.JVM_OPC_jsr_w; // 0xC9

		// 调试断点
		public static final byte _breakpoint = (byte) 202; // 0xCA

		// Java总共的字节码数目
		public static final int number_of_java_codes = ((_breakpoint + 1) & cxx_type.uint8_t_mask); // 0xCB

		// JVM内部字节码
		public static final byte _fast_agetfield = (byte) number_of_java_codes; // 0xCB
		public static final byte _fast_bgetfield = (byte) (_fast_agetfield + 1); // 0xCC
		public static final byte _fast_cgetfield = (byte) (_fast_bgetfield + 1); // 0xCD
		public static final byte _fast_dgetfield = (byte) (_fast_cgetfield + 1); // 0xCE
		public static final byte _fast_fgetfield = (byte) (_fast_dgetfield + 1); // 0xCF
		public static final byte _fast_igetfield = (byte) (_fast_fgetfield + 1); // 0xD0
		public static final byte _fast_lgetfield = (byte) (_fast_igetfield + 1); // 0xD1
		public static final byte _fast_sgetfield = (byte) (_fast_lgetfield + 1); // 0xD2

		public static final byte _fast_aputfield = (byte) (_fast_sgetfield + 1); // 0xD3
		public static final byte _fast_bputfield = (byte) (_fast_aputfield + 1); // 0xD4
		public static final byte _fast_zputfield = (byte) (_fast_bputfield + 1); // 0xD5
		public static final byte _fast_cputfield = (byte) (_fast_zputfield + 1); // 0xD6
		public static final byte _fast_dputfield = (byte) (_fast_cputfield + 1); // 0xD7
		public static final byte _fast_fputfield = (byte) (_fast_dputfield + 1); // 0xD8
		public static final byte _fast_iputfield = (byte) (_fast_fputfield + 1); // 0xD9
		public static final byte _fast_lputfield = (byte) (_fast_iputfield + 1); // 0xDA
		public static final byte _fast_sputfield = (byte) (_fast_lputfield + 1); // 0xDB

		public static final byte _fast_aload_0 = (byte) (_fast_sputfield + 1); // 0xDC
		public static final byte _fast_iaccess_0 = (byte) (_fast_aload_0 + 1); // 0xDD
		public static final byte _fast_aaccess_0 = (byte) (_fast_iaccess_0 + 1); // 0xDE
		public static final byte _fast_faccess_0 = (byte) (_fast_aaccess_0 + 1); // 0xDF

		public static final byte _fast_iload = (byte) (_fast_faccess_0 + 1); // 0xE0
		public static final byte _fast_iload2 = (byte) (_fast_iload + 1); // 0xE1
		public static final byte _fast_icaload = (byte) (_fast_iload2 + 1); // 0xE2

		public static final byte _fast_invokevfinal = (byte) (_fast_icaload + 1); // 0xE3
		public static final byte _fast_linearswitch = (byte) (_fast_invokevfinal + 1); // 0xE4
		public static final byte _fast_binaryswitch = (byte) (_fast_linearswitch + 1); // 0xE5

		public static final byte _fast_aldc = (byte) (_fast_binaryswitch + 1); // 0xE6
		public static final byte _fast_aldc_w = (byte) (_fast_aldc + 1); // 0xE7

		public static final byte _return_register_finalizer = (byte) (_fast_aldc_w + 1); // 0xE8

		public static final byte _invokehandle = (byte) (_return_register_finalizer + 1); // 0xE9

		public static final byte _nofast_getfield = (byte) (_invokehandle + 1); // 0xEA
		public static final byte _nofast_putfield = (byte) (_nofast_getfield + 1); // 0xEB
		public static final byte _nofast_aload_0 = (byte) (_nofast_putfield + 1); // 0xEC
		public static final byte _nofast_iload = (byte) (_nofast_aload_0 + 1); // 0xED

		public static final byte _shouldnotreachhere = (byte) (_nofast_iload + 1); // 0xEE

		public static final int number_of_codes = ((_shouldnotreachhere + 1) & cxx_type.uint8_t_mask); // 0xEF

		public static final String[] bc_names = new String[Code.number_of_codes];

		static
		{
			// 标准 JVM 字节码
			bc_names[cxx_type.as_uint8_t(_nop)] = "nop";
			bc_names[cxx_type.as_uint8_t(_aconst_null)] = "aconst_null";
			bc_names[cxx_type.as_uint8_t(_iconst_m1)] = "iconst_m1";
			bc_names[cxx_type.as_uint8_t(_iconst_0)] = "iconst_0";
			bc_names[cxx_type.as_uint8_t(_iconst_1)] = "iconst_1";
			bc_names[cxx_type.as_uint8_t(_iconst_2)] = "iconst_2";
			bc_names[cxx_type.as_uint8_t(_iconst_3)] = "iconst_3";
			bc_names[cxx_type.as_uint8_t(_iconst_4)] = "iconst_4";
			bc_names[cxx_type.as_uint8_t(_iconst_5)] = "iconst_5";
			bc_names[cxx_type.as_uint8_t(_lconst_0)] = "lconst_0";
			bc_names[cxx_type.as_uint8_t(_lconst_1)] = "lconst_1";
			bc_names[cxx_type.as_uint8_t(_fconst_0)] = "fconst_0";
			bc_names[cxx_type.as_uint8_t(_fconst_1)] = "fconst_1";
			bc_names[cxx_type.as_uint8_t(_fconst_2)] = "fconst_2";
			bc_names[cxx_type.as_uint8_t(_dconst_0)] = "dconst_0";
			bc_names[cxx_type.as_uint8_t(_dconst_1)] = "dconst_1";
			bc_names[cxx_type.as_uint8_t(_bipush)] = "bipush";
			bc_names[cxx_type.as_uint8_t(_sipush)] = "sipush";
			bc_names[cxx_type.as_uint8_t(_ldc)] = "ldc";
			bc_names[cxx_type.as_uint8_t(_ldc_w)] = "ldc_w";
			bc_names[cxx_type.as_uint8_t(_ldc2_w)] = "ldc2_w";
			bc_names[cxx_type.as_uint8_t(_iload)] = "iload";
			bc_names[cxx_type.as_uint8_t(_lload)] = "lload";
			bc_names[cxx_type.as_uint8_t(_fload)] = "fload";
			bc_names[cxx_type.as_uint8_t(_dload)] = "dload";
			bc_names[cxx_type.as_uint8_t(_aload)] = "aload";
			bc_names[cxx_type.as_uint8_t(_iload_0)] = "iload_0";
			bc_names[cxx_type.as_uint8_t(_iload_1)] = "iload_1";
			bc_names[cxx_type.as_uint8_t(_iload_2)] = "iload_2";
			bc_names[cxx_type.as_uint8_t(_iload_3)] = "iload_3";
			bc_names[cxx_type.as_uint8_t(_lload_0)] = "lload_0";
			bc_names[cxx_type.as_uint8_t(_lload_1)] = "lload_1";
			bc_names[cxx_type.as_uint8_t(_lload_2)] = "lload_2";
			bc_names[cxx_type.as_uint8_t(_lload_3)] = "lload_3";
			bc_names[cxx_type.as_uint8_t(_fload_0)] = "fload_0";
			bc_names[cxx_type.as_uint8_t(_fload_1)] = "fload_1";
			bc_names[cxx_type.as_uint8_t(_fload_2)] = "fload_2";
			bc_names[cxx_type.as_uint8_t(_fload_3)] = "fload_3";
			bc_names[cxx_type.as_uint8_t(_dload_0)] = "dload_0";
			bc_names[cxx_type.as_uint8_t(_dload_1)] = "dload_1";
			bc_names[cxx_type.as_uint8_t(_dload_2)] = "dload_2";
			bc_names[cxx_type.as_uint8_t(_dload_3)] = "dload_3";
			bc_names[cxx_type.as_uint8_t(_aload_0)] = "aload_0";
			bc_names[cxx_type.as_uint8_t(_aload_1)] = "aload_1";
			bc_names[cxx_type.as_uint8_t(_aload_2)] = "aload_2";
			bc_names[cxx_type.as_uint8_t(_aload_3)] = "aload_3";
			bc_names[cxx_type.as_uint8_t(_iaload)] = "iaload";
			bc_names[cxx_type.as_uint8_t(_laload)] = "laload";
			bc_names[cxx_type.as_uint8_t(_faload)] = "faload";
			bc_names[cxx_type.as_uint8_t(_daload)] = "daload";
			bc_names[cxx_type.as_uint8_t(_aaload)] = "aaload";
			bc_names[cxx_type.as_uint8_t(_baload)] = "baload";
			bc_names[cxx_type.as_uint8_t(_caload)] = "caload";
			bc_names[cxx_type.as_uint8_t(_saload)] = "saload";
			bc_names[cxx_type.as_uint8_t(_istore)] = "istore";
			bc_names[cxx_type.as_uint8_t(_lstore)] = "lstore";
			bc_names[cxx_type.as_uint8_t(_fstore)] = "fstore";
			bc_names[cxx_type.as_uint8_t(_dstore)] = "dstore";
			bc_names[cxx_type.as_uint8_t(_astore)] = "astore";
			bc_names[cxx_type.as_uint8_t(_istore_0)] = "istore_0";
			bc_names[cxx_type.as_uint8_t(_istore_1)] = "istore_1";
			bc_names[cxx_type.as_uint8_t(_istore_2)] = "istore_2";
			bc_names[cxx_type.as_uint8_t(_istore_3)] = "istore_3";
			bc_names[cxx_type.as_uint8_t(_lstore_0)] = "lstore_0";
			bc_names[cxx_type.as_uint8_t(_lstore_1)] = "lstore_1";
			bc_names[cxx_type.as_uint8_t(_lstore_2)] = "lstore_2";
			bc_names[cxx_type.as_uint8_t(_lstore_3)] = "lstore_3";
			bc_names[cxx_type.as_uint8_t(_fstore_0)] = "fstore_0";
			bc_names[cxx_type.as_uint8_t(_fstore_1)] = "fstore_1";
			bc_names[cxx_type.as_uint8_t(_fstore_2)] = "fstore_2";
			bc_names[cxx_type.as_uint8_t(_fstore_3)] = "fstore_3";
			bc_names[cxx_type.as_uint8_t(_dstore_0)] = "dstore_0";
			bc_names[cxx_type.as_uint8_t(_dstore_1)] = "dstore_1";
			bc_names[cxx_type.as_uint8_t(_dstore_2)] = "dstore_2";
			bc_names[cxx_type.as_uint8_t(_dstore_3)] = "dstore_3";
			bc_names[cxx_type.as_uint8_t(_astore_0)] = "astore_0";
			bc_names[cxx_type.as_uint8_t(_astore_1)] = "astore_1";
			bc_names[cxx_type.as_uint8_t(_astore_2)] = "astore_2";
			bc_names[cxx_type.as_uint8_t(_astore_3)] = "astore_3";
			bc_names[cxx_type.as_uint8_t(_iastore)] = "iastore";
			bc_names[cxx_type.as_uint8_t(_lastore)] = "lastore";
			bc_names[cxx_type.as_uint8_t(_fastore)] = "fastore";
			bc_names[cxx_type.as_uint8_t(_dastore)] = "dastore";
			bc_names[cxx_type.as_uint8_t(_aastore)] = "aastore";
			bc_names[cxx_type.as_uint8_t(_bastore)] = "bastore";
			bc_names[cxx_type.as_uint8_t(_castore)] = "castore";
			bc_names[cxx_type.as_uint8_t(_sastore)] = "sastore";
			bc_names[cxx_type.as_uint8_t(_pop)] = "pop";
			bc_names[cxx_type.as_uint8_t(_pop2)] = "pop2";
			bc_names[cxx_type.as_uint8_t(_dup)] = "dup";
			bc_names[cxx_type.as_uint8_t(_dup_x1)] = "dup_x1";
			bc_names[cxx_type.as_uint8_t(_dup_x2)] = "dup_x2";
			bc_names[cxx_type.as_uint8_t(_dup2)] = "dup2";
			bc_names[cxx_type.as_uint8_t(_dup2_x1)] = "dup2_x1";
			bc_names[cxx_type.as_uint8_t(_dup2_x2)] = "dup2_x2";
			bc_names[cxx_type.as_uint8_t(_swap)] = "swap";
			bc_names[cxx_type.as_uint8_t(_iadd)] = "iadd";
			bc_names[cxx_type.as_uint8_t(_ladd)] = "ladd";
			bc_names[cxx_type.as_uint8_t(_fadd)] = "fadd";
			bc_names[cxx_type.as_uint8_t(_dadd)] = "dadd";
			bc_names[cxx_type.as_uint8_t(_isub)] = "isub";
			bc_names[cxx_type.as_uint8_t(_lsub)] = "lsub";
			bc_names[cxx_type.as_uint8_t(_fsub)] = "fsub";
			bc_names[cxx_type.as_uint8_t(_dsub)] = "dsub";
			bc_names[cxx_type.as_uint8_t(_imul)] = "imul";
			bc_names[cxx_type.as_uint8_t(_lmul)] = "lmul";
			bc_names[cxx_type.as_uint8_t(_fmul)] = "fmul";
			bc_names[cxx_type.as_uint8_t(_dmul)] = "dmul";
			bc_names[cxx_type.as_uint8_t(_idiv)] = "idiv";
			bc_names[cxx_type.as_uint8_t(_ldiv)] = "ldiv";
			bc_names[cxx_type.as_uint8_t(_fdiv)] = "fdiv";
			bc_names[cxx_type.as_uint8_t(_ddiv)] = "ddiv";
			bc_names[cxx_type.as_uint8_t(_irem)] = "irem";
			bc_names[cxx_type.as_uint8_t(_lrem)] = "lrem";
			bc_names[cxx_type.as_uint8_t(_frem)] = "frem";
			bc_names[cxx_type.as_uint8_t(_drem)] = "drem";
			bc_names[cxx_type.as_uint8_t(_ineg)] = "ineg";
			bc_names[cxx_type.as_uint8_t(_lneg)] = "lneg";
			bc_names[cxx_type.as_uint8_t(_fneg)] = "fneg";
			bc_names[cxx_type.as_uint8_t(_dneg)] = "dneg";
			bc_names[cxx_type.as_uint8_t(_ishl)] = "ishl";
			bc_names[cxx_type.as_uint8_t(_lshl)] = "lshl";
			bc_names[cxx_type.as_uint8_t(_ishr)] = "ishr";
			bc_names[cxx_type.as_uint8_t(_lshr)] = "lshr";
			bc_names[cxx_type.as_uint8_t(_iushr)] = "iushr";
			bc_names[cxx_type.as_uint8_t(_lushr)] = "lushr";
			bc_names[cxx_type.as_uint8_t(_iand)] = "iand";
			bc_names[cxx_type.as_uint8_t(_land)] = "land";
			bc_names[cxx_type.as_uint8_t(_ior)] = "ior";
			bc_names[cxx_type.as_uint8_t(_lor)] = "lor";
			bc_names[cxx_type.as_uint8_t(_ixor)] = "ixor";
			bc_names[cxx_type.as_uint8_t(_lxor)] = "lxor";
			bc_names[cxx_type.as_uint8_t(_iinc)] = "iinc";
			bc_names[cxx_type.as_uint8_t(_i2l)] = "i2l";
			bc_names[cxx_type.as_uint8_t(_i2f)] = "i2f";
			bc_names[cxx_type.as_uint8_t(_i2d)] = "i2d";
			bc_names[cxx_type.as_uint8_t(_l2i)] = "l2i";
			bc_names[cxx_type.as_uint8_t(_l2f)] = "l2f";
			bc_names[cxx_type.as_uint8_t(_l2d)] = "l2d";
			bc_names[cxx_type.as_uint8_t(_f2i)] = "f2i";
			bc_names[cxx_type.as_uint8_t(_f2l)] = "f2l";
			bc_names[cxx_type.as_uint8_t(_f2d)] = "f2d";
			bc_names[cxx_type.as_uint8_t(_d2i)] = "d2i";
			bc_names[cxx_type.as_uint8_t(_d2l)] = "d2l";
			bc_names[cxx_type.as_uint8_t(_d2f)] = "d2f";
			bc_names[cxx_type.as_uint8_t(_i2b)] = "i2b";
			bc_names[cxx_type.as_uint8_t(_i2c)] = "i2c";
			bc_names[cxx_type.as_uint8_t(_i2s)] = "i2s";
			bc_names[cxx_type.as_uint8_t(_lcmp)] = "lcmp";
			bc_names[cxx_type.as_uint8_t(_fcmpl)] = "fcmpl";
			bc_names[cxx_type.as_uint8_t(_fcmpg)] = "fcmpg";
			bc_names[cxx_type.as_uint8_t(_dcmpl)] = "dcmpl";
			bc_names[cxx_type.as_uint8_t(_dcmpg)] = "dcmpg";
			bc_names[cxx_type.as_uint8_t(_ifeq)] = "ifeq";
			bc_names[cxx_type.as_uint8_t(_ifne)] = "ifne";
			bc_names[cxx_type.as_uint8_t(_iflt)] = "iflt";
			bc_names[cxx_type.as_uint8_t(_ifge)] = "ifge";
			bc_names[cxx_type.as_uint8_t(_ifgt)] = "ifgt";
			bc_names[cxx_type.as_uint8_t(_ifle)] = "ifle";
			bc_names[cxx_type.as_uint8_t(_if_icmpeq)] = "if_icmpeq";
			bc_names[cxx_type.as_uint8_t(_if_icmpne)] = "if_icmpne";
			bc_names[cxx_type.as_uint8_t(_if_icmplt)] = "if_icmplt";
			bc_names[cxx_type.as_uint8_t(_if_icmpge)] = "if_icmpge";
			bc_names[cxx_type.as_uint8_t(_if_icmpgt)] = "if_icmpgt";
			bc_names[cxx_type.as_uint8_t(_if_icmple)] = "if_icmple";
			bc_names[cxx_type.as_uint8_t(_if_acmpeq)] = "if_acmpeq";
			bc_names[cxx_type.as_uint8_t(_if_acmpne)] = "if_acmpne";
			bc_names[cxx_type.as_uint8_t(_goto)] = "goto";
			bc_names[cxx_type.as_uint8_t(_jsr)] = "jsr";
			bc_names[cxx_type.as_uint8_t(_ret)] = "ret";
			bc_names[cxx_type.as_uint8_t(_tableswitch)] = "tableswitch";
			bc_names[cxx_type.as_uint8_t(_lookupswitch)] = "lookupswitch";
			bc_names[cxx_type.as_uint8_t(_ireturn)] = "ireturn";
			bc_names[cxx_type.as_uint8_t(_lreturn)] = "lreturn";
			bc_names[cxx_type.as_uint8_t(_freturn)] = "freturn";
			bc_names[cxx_type.as_uint8_t(_dreturn)] = "dreturn";
			bc_names[cxx_type.as_uint8_t(_areturn)] = "areturn";
			bc_names[cxx_type.as_uint8_t(_return)] = "return";
			bc_names[cxx_type.as_uint8_t(_getstatic)] = "getstatic";
			bc_names[cxx_type.as_uint8_t(_putstatic)] = "putstatic";
			bc_names[cxx_type.as_uint8_t(_getfield)] = "getfield";
			bc_names[cxx_type.as_uint8_t(_putfield)] = "putfield";
			bc_names[cxx_type.as_uint8_t(_invokevirtual)] = "invokevirtual";
			bc_names[cxx_type.as_uint8_t(_invokespecial)] = "invokespecial";
			bc_names[cxx_type.as_uint8_t(_invokestatic)] = "invokestatic";
			bc_names[cxx_type.as_uint8_t(_invokeinterface)] = "invokeinterface";
			bc_names[cxx_type.as_uint8_t(_invokedynamic)] = "invokedynamic";
			bc_names[cxx_type.as_uint8_t(_new)] = "new";
			bc_names[cxx_type.as_uint8_t(_newarray)] = "newarray";
			bc_names[cxx_type.as_uint8_t(_anewarray)] = "anewarray";
			bc_names[cxx_type.as_uint8_t(_arraylength)] = "arraylength";
			bc_names[cxx_type.as_uint8_t(_athrow)] = "athrow";
			bc_names[cxx_type.as_uint8_t(_checkcast)] = "checkcast";
			bc_names[cxx_type.as_uint8_t(_instanceof)] = "instanceof";
			bc_names[cxx_type.as_uint8_t(_monitorenter)] = "monitorenter";
			bc_names[cxx_type.as_uint8_t(_monitorexit)] = "monitorexit";
			bc_names[cxx_type.as_uint8_t(_wide)] = "wide";
			bc_names[cxx_type.as_uint8_t(_multianewarray)] = "multianewarray";
			bc_names[cxx_type.as_uint8_t(_ifnull)] = "ifnull";
			bc_names[cxx_type.as_uint8_t(_ifnonnull)] = "ifnonnull";
			bc_names[cxx_type.as_uint8_t(_goto_w)] = "goto_w";
			bc_names[cxx_type.as_uint8_t(_jsr_w)] = "jsr_w";

			// JVM 内部字节码
			bc_names[cxx_type.as_uint8_t(_breakpoint)] = "breakpoint";
			bc_names[cxx_type.as_uint8_t(_fast_agetfield)] = "fast_agetfield";
			bc_names[cxx_type.as_uint8_t(_fast_bgetfield)] = "fast_bgetfield";
			bc_names[cxx_type.as_uint8_t(_fast_cgetfield)] = "fast_cgetfield";
			bc_names[cxx_type.as_uint8_t(_fast_dgetfield)] = "fast_dgetfield";
			bc_names[cxx_type.as_uint8_t(_fast_fgetfield)] = "fast_fgetfield";
			bc_names[cxx_type.as_uint8_t(_fast_igetfield)] = "fast_igetfield";
			bc_names[cxx_type.as_uint8_t(_fast_lgetfield)] = "fast_lgetfield";
			bc_names[cxx_type.as_uint8_t(_fast_sgetfield)] = "fast_sgetfield";
			bc_names[cxx_type.as_uint8_t(_fast_aputfield)] = "fast_aputfield";
			bc_names[cxx_type.as_uint8_t(_fast_bputfield)] = "fast_bputfield";
			bc_names[cxx_type.as_uint8_t(_fast_zputfield)] = "fast_zputfield";
			bc_names[cxx_type.as_uint8_t(_fast_cputfield)] = "fast_cputfield";
			bc_names[cxx_type.as_uint8_t(_fast_dputfield)] = "fast_dputfield";
			bc_names[cxx_type.as_uint8_t(_fast_fputfield)] = "fast_fputfield";
			bc_names[cxx_type.as_uint8_t(_fast_iputfield)] = "fast_iputfield";
			bc_names[cxx_type.as_uint8_t(_fast_lputfield)] = "fast_lputfield";
			bc_names[cxx_type.as_uint8_t(_fast_sputfield)] = "fast_sputfield";
			bc_names[cxx_type.as_uint8_t(_fast_aload_0)] = "fast_aload_0";
			bc_names[cxx_type.as_uint8_t(_fast_iaccess_0)] = "fast_iaccess_0";
			bc_names[cxx_type.as_uint8_t(_fast_aaccess_0)] = "fast_aaccess_0";
			bc_names[cxx_type.as_uint8_t(_fast_faccess_0)] = "fast_faccess_0";
			bc_names[cxx_type.as_uint8_t(_fast_iload)] = "fast_iload";
			bc_names[cxx_type.as_uint8_t(_fast_iload2)] = "fast_iload2";
			bc_names[cxx_type.as_uint8_t(_fast_icaload)] = "fast_icaload";
			bc_names[cxx_type.as_uint8_t(_fast_invokevfinal)] = "fast_invokevfinal";
			bc_names[cxx_type.as_uint8_t(_fast_linearswitch)] = "fast_linearswitch";
			bc_names[cxx_type.as_uint8_t(_fast_binaryswitch)] = "fast_binaryswitch";
			bc_names[cxx_type.as_uint8_t(_fast_aldc)] = "fast_aldc";
			bc_names[cxx_type.as_uint8_t(_fast_aldc_w)] = "fast_aldc_w";
			bc_names[cxx_type.as_uint8_t(_return_register_finalizer)] = "return_register_finalizer";
			bc_names[cxx_type.as_uint8_t(_invokehandle)] = "invokehandle";
			bc_names[cxx_type.as_uint8_t(_nofast_getfield)] = "nofast_getfield";
			bc_names[cxx_type.as_uint8_t(_nofast_putfield)] = "nofast_putfield";
			bc_names[cxx_type.as_uint8_t(_nofast_aload_0)] = "nofast_aload_0";
			bc_names[cxx_type.as_uint8_t(_nofast_iload)] = "nofast_iload";
			bc_names[cxx_type.as_uint8_t(_shouldnotreachhere)] = "shouldnotreachhere";
		}

		public static final String to_string(byte bc)
		{
			return bc_names[cxx_type.as_uint8_t(bc)];
		}

		/**
		 * 从字节码中获取常量池索引，即大端索引值。<br>
		 * 
		 * @param op1
		 * @param op2
		 * @return
		 */
		public static final int cp_idx(byte op1, byte op2)
		{
			return (op1 << 8) | (op2 & 0xFF);
		}

		public static final int fetch_cp_idx(byte[] bc, int ins_bci)
		{
			return cp_idx(bc[ins_bci + 1], bc[ins_bci + 2]);
		}
	}

	public static abstract class Flags
	{
		// 语义标记
		public static final short _bc_can_trap = 1 << 0; // bytecode execution can trap or block
		public static final short _bc_can_rewrite = 1 << 1; // bytecode execution has an alternate form

		// 格式化标记
		public static final short _fmt_has_c = 1 << 2; // constant, such as sipush "bcc"
		public static final short _fmt_has_j = 1 << 3; // constant pool cache index, such as getfield "bjj"
		public static final short _fmt_has_k = 1 << 4; // constant pool index, such as ldc "bk"
		public static final short _fmt_has_i = 1 << 5; // local index, such as iload
		public static final short _fmt_has_o = 1 << 6; // offset, such as ifeq
		public static final short _fmt_has_nbo = 1 << 7; // contains native-order field(s)
		public static final short _fmt_has_u2 = 1 << 8; // contains double-byte field(s)
		public static final short _fmt_has_u4 = 1 << 9; // contains quad-byte field
		public static final short _fmt_not_variable = 1 << 10; // not of variable length (simple or wide)
		public static final short _fmt_not_simple = 1 << 11; // either wide or variable length
		public static final short _all_fmt_bits = (short) (_fmt_not_simple * 2 - _fmt_has_c);

		// 多格式示例
		public static final short _fmt_b = (short) _fmt_not_variable;
		public static final short _fmt_bc = (short) (_fmt_b | _fmt_has_c);
		public static final short _fmt_bi = (short) (_fmt_b | _fmt_has_i);
		public static final short _fmt_bkk = (short) (_fmt_b | _fmt_has_k | _fmt_has_u2);
		public static final short _fmt_bJJ = (short) (_fmt_b | _fmt_has_j | _fmt_has_u2 | _fmt_has_nbo);
		public static final short _fmt_bo2 = (short) (_fmt_b | _fmt_has_o | _fmt_has_u2);
		public static final short _fmt_bo4 = (short) (_fmt_b | _fmt_has_o | _fmt_has_u4);
	}

	/**
	 * 从目标地址读取字节码或断点
	 * 
	 * @param bcp
	 * @return
	 */
	public static final byte code_or_bp_at(long bcp)
	{
		return unsafe.read_byte(bcp);
	}

	public static final String to_string(byte[] bytecode)
	{
		StringBuilder sb = new StringBuilder();
		for (int bci = 0; bci < bytecode.length; ++bci)
		{
			byte bc = bytecode[bci];
			sb.append(Code.to_string(bc));
			int ins_len = classfile_constants.instruction_length(bc);
			switch (ins_len)
			{
			case 0:
				sb.append(' ');
				break;
			case 1:
				sb.append('\n');
				break;
			default:
				sb.append(' ');
				for (int op = 1; op < ins_len; ++op)
				{
					bci += op;
					sb.append(String.format("0x%02x", bytecode[bci]));
					if (op == ins_len - 1)
					{
						sb.append('\n');
						break;
					}
					sb.append(", ");// 不是最后一个参数则用逗号分隔
				}
				break;
			}
		}
		int last = sb.length() - 1;
		if (sb.charAt(last) == '\n')
			sb.deleteCharAt(last);// 删除结尾多余的'\n'
		return sb.toString();
	}
}

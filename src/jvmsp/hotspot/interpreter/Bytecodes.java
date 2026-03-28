package jvmsp.hotspot.interpreter;

import jvmsp.unsafe;

public abstract class Bytecodes
{
	/**
	 * 字节码值
	 */
	public static abstract class Code
	{
		public static final byte _illegal = -1; // 0xFF

		// Java标准字节码
		public static final byte _nop = 0; // 0x00
		public static final byte _aconst_null = 1; // 0x01
		public static final byte _iconst_m1 = 2; // 0x02
		public static final byte _iconst_0 = 3; // 0x03
		public static final byte _iconst_1 = 4; // 0x04
		public static final byte _iconst_2 = 5; // 0x05
		public static final byte _iconst_3 = 6; // 0x06
		public static final byte _iconst_4 = 7; // 0x07
		public static final byte _iconst_5 = 8; // 0x08
		public static final byte _lconst_0 = 9; // 0x09
		public static final byte _lconst_1 = 10; // 0x0A
		public static final byte _fconst_0 = 11; // 0x0B
		public static final byte _fconst_1 = 12; // 0x0C
		public static final byte _fconst_2 = 13; // 0x0D
		public static final byte _dconst_0 = 14; // 0x0E
		public static final byte _dconst_1 = 15; // 0x0F
		public static final byte _bipush = 16; // 0x10
		public static final byte _sipush = 17; // 0x11
		public static final byte _ldc = 18; // 0x12
		public static final byte _ldc_w = 19; // 0x13
		public static final byte _ldc2_w = 20; // 0x14
		public static final byte _iload = 21; // 0x15
		public static final byte _lload = 22; // 0x16
		public static final byte _fload = 23; // 0x17
		public static final byte _dload = 24; // 0x18
		public static final byte _aload = 25; // 0x19
		public static final byte _iload_0 = 26; // 0x1A
		public static final byte _iload_1 = 27; // 0x1B
		public static final byte _iload_2 = 28; // 0x1C
		public static final byte _iload_3 = 29; // 0x1D
		public static final byte _lload_0 = 30; // 0x1E
		public static final byte _lload_1 = 31; // 0x1F
		public static final byte _lload_2 = 32; // 0x20
		public static final byte _lload_3 = 33; // 0x21
		public static final byte _fload_0 = 34; // 0x22
		public static final byte _fload_1 = 35; // 0x23
		public static final byte _fload_2 = 36; // 0x24
		public static final byte _fload_3 = 37; // 0x25
		public static final byte _dload_0 = 38; // 0x26
		public static final byte _dload_1 = 39; // 0x27
		public static final byte _dload_2 = 40; // 0x28
		public static final byte _dload_3 = 41; // 0x29
		public static final byte _aload_0 = 42; // 0x2A
		public static final byte _aload_1 = 43; // 0x2B
		public static final byte _aload_2 = 44; // 0x2C
		public static final byte _aload_3 = 45; // 0x2D
		public static final byte _iaload = 46; // 0x2E
		public static final byte _laload = 47; // 0x2F
		public static final byte _faload = 48; // 0x30
		public static final byte _daload = 49; // 0x31
		public static final byte _aaload = 50; // 0x32
		public static final byte _baload = 51; // 0x33
		public static final byte _caload = 52; // 0x34
		public static final byte _saload = 53; // 0x35
		public static final byte _istore = 54; // 0x36
		public static final byte _lstore = 55; // 0x37
		public static final byte _fstore = 56; // 0x38
		public static final byte _dstore = 57; // 0x39
		public static final byte _astore = 58; // 0x3A
		public static final byte _istore_0 = 59; // 0x3B
		public static final byte _istore_1 = 60; // 0x3C
		public static final byte _istore_2 = 61; // 0x3D
		public static final byte _istore_3 = 62; // 0x3E
		public static final byte _lstore_0 = 63; // 0x3F
		public static final byte _lstore_1 = 64; // 0x40
		public static final byte _lstore_2 = 65; // 0x41
		public static final byte _lstore_3 = 66; // 0x42
		public static final byte _fstore_0 = 67; // 0x43
		public static final byte _fstore_1 = 68; // 0x44
		public static final byte _fstore_2 = 69; // 0x45
		public static final byte _fstore_3 = 70; // 0x46
		public static final byte _dstore_0 = 71; // 0x47
		public static final byte _dstore_1 = 72; // 0x48
		public static final byte _dstore_2 = 73; // 0x49
		public static final byte _dstore_3 = 74; // 0x4A
		public static final byte _astore_0 = 75; // 0x4B
		public static final byte _astore_1 = 76; // 0x4C
		public static final byte _astore_2 = 77; // 0x4D
		public static final byte _astore_3 = 78; // 0x4E
		public static final byte _iastore = 79; // 0x4F
		public static final byte _lastore = 80; // 0x50
		public static final byte _fastore = 81; // 0x51
		public static final byte _dastore = 82; // 0x52
		public static final byte _aastore = 83; // 0x53
		public static final byte _bastore = 84; // 0x54
		public static final byte _castore = 85; // 0x55
		public static final byte _sastore = 86; // 0x56
		public static final byte _pop = 87; // 0x57
		public static final byte _pop2 = 88; // 0x58
		public static final byte _dup = 89; // 0x59
		public static final byte _dup_x1 = 90; // 0x5A
		public static final byte _dup_x2 = 91; // 0x5B
		public static final byte _dup2 = 92; // 0x5C
		public static final byte _dup2_x1 = 93; // 0x5D
		public static final byte _dup2_x2 = 94; // 0x5E
		public static final byte _swap = 95; // 0x5F
		public static final byte _iadd = 96; // 0x60
		public static final byte _ladd = 97; // 0x61
		public static final byte _fadd = 98; // 0x62
		public static final byte _dadd = 99; // 0x63
		public static final byte _isub = 100; // 0x64
		public static final byte _lsub = 101; // 0x65
		public static final byte _fsub = 102; // 0x66
		public static final byte _dsub = 103; // 0x67
		public static final byte _imul = 104; // 0x68
		public static final byte _lmul = 105; // 0x69
		public static final byte _fmul = 106; // 0x6A
		public static final byte _dmul = 107; // 0x6B
		public static final byte _idiv = 108; // 0x6C
		public static final byte _ldiv = 109; // 0x6D
		public static final byte _fdiv = 110; // 0x6E
		public static final byte _ddiv = 111; // 0x6F
		public static final byte _irem = 112; // 0x70
		public static final byte _lrem = 113; // 0x71
		public static final byte _frem = 114; // 0x72
		public static final byte _drem = 115; // 0x73
		public static final byte _ineg = 116; // 0x74
		public static final byte _lneg = 117; // 0x75
		public static final byte _fneg = 118; // 0x76
		public static final byte _dneg = 119; // 0x77
		public static final byte _ishl = 120; // 0x78
		public static final byte _lshl = 121; // 0x79
		public static final byte _ishr = 122; // 0x7A
		public static final byte _lshr = 123; // 0x7B
		public static final byte _iushr = 124; // 0x7C
		public static final byte _lushr = 125; // 0x7D
		public static final byte _iand = 126; // 0x7E
		public static final byte _land = 127; // 0x7F
		public static final byte _ior = (byte) 128; // 0x80
		public static final byte _lor = (byte) 129; // 0x81
		public static final byte _ixor = (byte) 130; // 0x82
		public static final byte _lxor = (byte) 131; // 0x83
		public static final byte _iinc = (byte) 132; // 0x84
		public static final byte _i2l = (byte) 133; // 0x85
		public static final byte _i2f = (byte) 134; // 0x86
		public static final byte _i2d = (byte) 135; // 0x87
		public static final byte _l2i = (byte) 136; // 0x88
		public static final byte _l2f = (byte) 137; // 0x89
		public static final byte _l2d = (byte) 138; // 0x8A
		public static final byte _f2i = (byte) 139; // 0x8B
		public static final byte _f2l = (byte) 140; // 0x8C
		public static final byte _f2d = (byte) 141; // 0x8D
		public static final byte _d2i = (byte) 142; // 0x8E
		public static final byte _d2l = (byte) 143; // 0x8F
		public static final byte _d2f = (byte) 144; // 0x90
		public static final byte _i2b = (byte) 145; // 0x91
		public static final byte _i2c = (byte) 146; // 0x92
		public static final byte _i2s = (byte) 147; // 0x93
		public static final byte _lcmp = (byte) 148; // 0x94
		public static final byte _fcmpl = (byte) 149; // 0x95
		public static final byte _fcmpg = (byte) 150; // 0x96
		public static final byte _dcmpl = (byte) 151; // 0x97
		public static final byte _dcmpg = (byte) 152; // 0x98
		public static final byte _ifeq = (byte) 153; // 0x99
		public static final byte _ifne = (byte) 154; // 0x9A
		public static final byte _iflt = (byte) 155; // 0x9B
		public static final byte _ifge = (byte) 156; // 0x9C
		public static final byte _ifgt = (byte) 157; // 0x9D
		public static final byte _ifle = (byte) 158; // 0x9E
		public static final byte _if_icmpeq = (byte) 159; // 0x9F
		public static final byte _if_icmpne = (byte) 160; // 0xA0
		public static final byte _if_icmplt = (byte) 161; // 0xA1
		public static final byte _if_icmpge = (byte) 162; // 0xA2
		public static final byte _if_icmpgt = (byte) 163; // 0xA3
		public static final byte _if_icmple = (byte) 164; // 0xA4
		public static final byte _if_acmpeq = (byte) 165; // 0xA5
		public static final byte _if_acmpne = (byte) 166; // 0xA6
		public static final byte _goto = (byte) 167; // 0xA7
		public static final byte _jsr = (byte) 168; // 0xA8
		public static final byte _ret = (byte) 169; // 0xA9
		public static final byte _tableswitch = (byte) 170; // 0xAA
		public static final byte _lookupswitch = (byte) 171; // 0xAB
		public static final byte _ireturn = (byte) 172; // 0xAC
		public static final byte _lreturn = (byte) 173; // 0xAD
		public static final byte _freturn = (byte) 174; // 0xAE
		public static final byte _dreturn = (byte) 175; // 0xAF
		public static final byte _areturn = (byte) 176; // 0xB0
		public static final byte _return = (byte) 177; // 0xB1
		public static final byte _getstatic = (byte) 178; // 0xB2
		public static final byte _putstatic = (byte) 179; // 0xB3
		public static final byte _getfield = (byte) 180; // 0xB4
		public static final byte _putfield = (byte) 181; // 0xB5
		public static final byte _invokevirtual = (byte) 182; // 0xB6
		public static final byte _invokespecial = (byte) 183; // 0xB7
		public static final byte _invokestatic = (byte) 184; // 0xB8
		public static final byte _invokeinterface = (byte) 185; // 0xB9
		public static final byte _invokedynamic = (byte) 186; // 0xBA
		public static final byte _new = (byte) 187; // 0xBB
		public static final byte _newarray = (byte) 188; // 0xBC
		public static final byte _anewarray = (byte) 189; // 0xBD
		public static final byte _arraylength = (byte) 190; // 0xBE
		public static final byte _athrow = (byte) 191; // 0xBF
		public static final byte _checkcast = (byte) 192; // 0xC0
		public static final byte _instanceof = (byte) 193; // 0xC1
		public static final byte _monitorenter = (byte) 194; // 0xC2
		public static final byte _monitorexit = (byte) 195; // 0xC3
		public static final byte _wide = (byte) 196; // 0xC4
		public static final byte _multianewarray = (byte) 197; // 0xC5
		public static final byte _ifnull = (byte) 198; // 0xC6
		public static final byte _ifnonnull = (byte) 199; // 0xC7
		public static final byte _goto_w = (byte) 200; // 0xC8
		public static final byte _jsr_w = (byte) 201; // 0xC9
		public static final byte _breakpoint = (byte) 202; // 0xCA

		public static final int number_of_java_codes = 203; // 0xCB

		// JVM内部字节码
		public static final byte _fast_agetfield = (byte) number_of_java_codes; // 0xCB
		public static final byte _fast_bgetfield = (byte) (number_of_java_codes + 1); // 0xCC
		public static final byte _fast_cgetfield = (byte) (number_of_java_codes + 2); // 0xCD
		public static final byte _fast_dgetfield = (byte) (number_of_java_codes + 3); // 0xCE
		public static final byte _fast_fgetfield = (byte) (number_of_java_codes + 4); // 0xCF
		public static final byte _fast_igetfield = (byte) (number_of_java_codes + 5); // 0xD0
		public static final byte _fast_lgetfield = (byte) (number_of_java_codes + 6); // 0xD1
		public static final byte _fast_sgetfield = (byte) (number_of_java_codes + 7); // 0xD2

		public static final byte _fast_aputfield = (byte) (number_of_java_codes + 8); // 0xD3
		public static final byte _fast_bputfield = (byte) (number_of_java_codes + 9); // 0xD4
		public static final byte _fast_zputfield = (byte) (number_of_java_codes + 10); // 0xD5
		public static final byte _fast_cputfield = (byte) (number_of_java_codes + 11); // 0xD6
		public static final byte _fast_dputfield = (byte) (number_of_java_codes + 12); // 0xD7
		public static final byte _fast_fputfield = (byte) (number_of_java_codes + 13); // 0xD8
		public static final byte _fast_iputfield = (byte) (number_of_java_codes + 14); // 0xD9
		public static final byte _fast_lputfield = (byte) (number_of_java_codes + 15); // 0xDA
		public static final byte _fast_sputfield = (byte) (number_of_java_codes + 16); // 0xDB

		public static final byte _fast_aload_0 = (byte) (number_of_java_codes + 17); // 0xDC
		public static final byte _fast_iaccess_0 = (byte) (number_of_java_codes + 18); // 0xDD
		public static final byte _fast_aaccess_0 = (byte) (number_of_java_codes + 19); // 0xDE
		public static final byte _fast_faccess_0 = (byte) (number_of_java_codes + 20); // 0xDF

		public static final byte _fast_iload = (byte) (number_of_java_codes + 21); // 0xE0
		public static final byte _fast_iload2 = (byte) (number_of_java_codes + 22); // 0xE1
		public static final byte _fast_icaload = (byte) (number_of_java_codes + 23); // 0xE2

		public static final byte _fast_invokevfinal = (byte) (number_of_java_codes + 24); // 0xE3
		public static final byte _fast_linearswitch = (byte) (number_of_java_codes + 25); // 0xE4
		public static final byte _fast_binaryswitch = (byte) (number_of_java_codes + 26); // 0xE5

		public static final byte _fast_aldc = (byte) (number_of_java_codes + 27); // 0xE6
		public static final byte _fast_aldc_w = (byte) (number_of_java_codes + 28); // 0xE7

		public static final byte _return_register_finalizer = (byte) (number_of_java_codes + 29); // 0xE8

		public static final byte _invokehandle = (byte) (number_of_java_codes + 30); // 0xE9

		public static final byte _nofast_getfield = (byte) (number_of_java_codes + 31); // 0xEA
		public static final byte _nofast_putfield = (byte) (number_of_java_codes + 32); // 0xEB
		public static final byte _nofast_aload_0 = (byte) (number_of_java_codes + 33); // 0xEC
		public static final byte _nofast_iload = (byte) (number_of_java_codes + 34); // 0xED

		public static final byte _shouldnotreachhere = (byte) (number_of_java_codes + 35); // 0xEE

		public static final int number_of_codes = 239; // 0xEF
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
}

package jvmsp.hotspot.code;

import jvmsp.hotspot.vm_struct;

public class nmethod extends CodeBlob
{
	public static final String type_name = "nmethod";
	public static final long size = sizeof(type_name);

	private static final long _method = switch_offset(
			null, // JDK21
			() -> vm_struct.entry.find(type_name, "_method").offset // JDK25
	);
	private static final long _immutable_data = switch_offset(
			null, // JDK21
			() -> vm_struct.entry.find(type_name, "_immutable_data").offset // JDK25
	);

	private static final long _osr_link = vm_struct.entry.find(type_name, "_osr_link").offset; // 208
	private static final long _entry_point = vm_struct.entry.find(type_name, "_entry_point").offset; // 224
	private static final long _verified_entry_point = vm_struct.entry.find(type_name, "_verified_entry_point").offset; // 232
	private static final long _osr_entry_point = vm_struct.entry.find(type_name, "_osr_entry_point").offset; // 240
	private static final long _entry_bci = vm_struct.entry.find(type_name, "_entry_bci").offset; // 252
	private static final long _exception_offset = vm_struct.entry.find(type_name, "_exception_offset").offset; // 256
	private static final long _consts_offset = vm_struct.entry.find(type_name, "_consts_offset").offset; // 264
	private static final long _stub_offset = vm_struct.entry.find(type_name, "_stub_offset").offset; // 268
	private static final long _oops_offset = vm_struct.entry.find(type_name, "_oops_offset").offset; // 272
	private static final long _metadata_offset = vm_struct.entry.find(type_name, "_metadata_offset").offset; // 276
	private static final long _scopes_pcs_offset = vm_struct.entry.find(type_name, "_scopes_pcs_offset").offset; // 284
	private static final long _dependencies_offset = vm_struct.entry.find(type_name, "_dependencies_offset").offset; // 288
	private static final long _handler_table_offset = vm_struct.entry.find(type_name, "_handler_table_offset").offset; // 292
	private static final long _nul_chk_table_offset = vm_struct.entry.find(type_name, "_nul_chk_table_offset").offset; // 296
	private static final long _nmethod_end_offset = vm_struct.entry.find(type_name, "_nmethod_end_offset").offset; // 308
	private static final long _orig_pc_offset = vm_struct.entry.find(type_name, "_orig_pc_offset").offset; // 312
	private static final long _compile_id = vm_struct.entry.find(type_name, "_compile_id").offset; // 316
	private static final long _comp_level = vm_struct.entry.find(type_name, "_comp_level").offset; // 332
	private static final long _state = vm_struct.entry.find(type_name, "_state").offset; // 336

	public nmethod(long address)
	{
		super(type_name, address);
	}

}
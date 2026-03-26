package jvmsp.hotspot.code;

import jvmsp.hotspot.vm_struct;

public class nmethod extends CodeBlob
{
	private static final long _method = vm_struct.entry.find("nmethod", "_method").offset;// JDK25

	private static final long _entry_bci = vm_struct.entry.find("nmethod", "_entry_bci").offset;

	private static final long _osr_link = vm_struct.entry.find("nmethod", "_osr_link").offset;
	private static final long _immutable_data = vm_struct.entry.find("nmethod", "_immutable_data").offset;

	private static final long _state = vm_struct.entry.find("nmethod", "_state").offset;
	private static final long _exception_offset = vm_struct.entry.find("nmethod", "_exception_offset").offset;
	private static final long _orig_pc_offset = vm_struct.entry.find("nmethod", "_orig_pc_offset").offset;
	private static final long _stub_offset = vm_struct.entry.find("nmethod", "_stub_offset").offset;
	// private static final long _consts_offset = vm_struct.entry.find("nmethod", "_consts_offset").offset;
	// private static final long _oops_offset = vm_struct.entry.find("nmethod", "_oops_offset").offset;
//	private static final long _metadata_offset = vm_struct.entry.find("nmethod", "_metadata_offset").offset;
	private static final long _scopes_pcs_offset = vm_struct.entry.find("nmethod", "_scopes_pcs_offset").offset;
//	private static final long _dependencies_offset = vm_struct.entry.find("nmethod", "_dependencies_offset").offset;
	private static final long _handler_table_offset = vm_struct.entry.find("nmethod", "_handler_table_offset").offset;
	private static final long _nul_chk_table_offset = vm_struct.entry.find("nmethod", "_nul_chk_table_offset").offset;
//	private static final long _nmethod_end_offset = vm_struct.entry.find("nmethod", "_nmethod_end_offset").offset;
//	private static final long _entry_point = vm_struct.entry.find("nmethod", "_entry_point").offset;
//	private static final long _verified_entry_point = vm_struct.entry.find("nmethod", "_verified_entry_point").offset;
	private static final long _osr_entry_point = vm_struct.entry.find("nmethod", "_osr_entry_point").offset;
	private static final long _compile_id = vm_struct.entry.find("nmethod", "_compile_id").offset;
	private static final long _comp_level = vm_struct.entry.find("nmethod", "_comp_level").offset;

	public nmethod(long address)
	{
		super("nmethod", address);
	}

}
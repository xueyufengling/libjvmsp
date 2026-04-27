package jvmsp.libso.win;

public class winnt
{
	// 节/内存区相关访问掩码
	public static final long SECTION_QUERY = 0x0001L;
	public static final long SECTION_MAP_WRITE = 0x0002L;
	public static final long SECTION_MAP_READ = 0x0004L;
	public static final long SECTION_MAP_EXECUTE = 0x0008L;
	public static final long SECTION_EXTEND_SIZE = 0x0010L;
	public static final long SECTION_MAP_EXECUTE_EXPLICIT = 0x0020L;

	public static final long SECTION_ALL_ACCESS = (0x000F0000L | SECTION_QUERY | SECTION_MAP_WRITE | SECTION_MAP_READ | SECTION_MAP_EXECUTE | SECTION_EXTEND_SIZE);

	// 会话访问权限
	public static final long SESSION_QUERY_ACCESS = 0x1L;
	public static final long SESSION_MODIFY_ACCESS = 0x2L;

	public static final long SESSION_ALL_ACCESS = (0x000F0000L | SESSION_QUERY_ACCESS | SESSION_MODIFY_ACCESS);

	// 内存页保护属性
	public static final long PAGE_NOACCESS = 0x01L;
	public static final long PAGE_READONLY = 0x02L;
	public static final long PAGE_READWRITE = 0x04L;
	public static final long PAGE_WRITECOPY = 0x08L;
	public static final long PAGE_EXECUTE = 0x10L;
	public static final long PAGE_EXECUTE_READ = 0x20L;
	public static final long PAGE_EXECUTE_READWRITE = 0x40L;
	public static final long PAGE_EXECUTE_WRITECOPY = 0x80L;
	public static final long PAGE_GUARD = 0x100L;
	public static final long PAGE_NOCACHE = 0x200L;
	public static final long PAGE_WRITECOMBINE = 0x400L;
	public static final long PAGE_GRAPHICS_NOACCESS = 0x0800L;
	public static final long PAGE_GRAPHICS_READONLY = 0x1000L;
	public static final long PAGE_GRAPHICS_READWRITE = 0x2000L;
	public static final long PAGE_GRAPHICS_EXECUTE = 0x4000L;
	public static final long PAGE_GRAPHICS_EXECUTE_READ = 0x8000L;
	public static final long PAGE_GRAPHICS_EXECUTE_READWRITE = 0x10000L;
	public static final long PAGE_GRAPHICS_COHERENT = 0x20000L;
	public static final long PAGE_GRAPHICS_NOCACHE = 0x40000L;
	public static final long PAGE_ENCLAVE_THREAD_CONTROL = 0x80000000L;
	public static final long PAGE_REVERT_TO_FILE_MAP = 0x80000000L;
	public static final long PAGE_TARGETS_NO_UPDATE = 0x40000000L;
	public static final long PAGE_TARGETS_INVALID = 0x40000000L;
	public static final long PAGE_ENCLAVE_UNVALIDATED = 0x20000000L;
	public static final long PAGE_ENCLAVE_MASK = 0x10000000L;
	public static final long PAGE_ENCLAVE_DECOMMIT = (PAGE_ENCLAVE_MASK | 0L);
	public static final long PAGE_ENCLAVE_SS_FIRST = (PAGE_ENCLAVE_MASK | 1L);
	public static final long PAGE_ENCLAVE_SS_REST = (PAGE_ENCLAVE_MASK | 2L);

	// 内存分配与释放标志
	public static final long MEM_COMMIT = 0x1000L;
	public static final long MEM_RESERVE = 0x2000L;
	public static final long MEM_REPLACE_PLACEHOLDER = 0x4000L;
	public static final long MEM_DECOMMIT = 0x4000L;
	public static final long MEM_RELEASE = 0x8000L;
	public static final long MEM_FREE = 0x10000L;
	public static final long MEM_PRIVATE = 0x20000L;
	public static final long MEM_RESERVE_PLACEHOLDER = 0x40000L;
	public static final long MEM_MAPPED = 0x40000L;
	public static final long MEM_RESET = 0x80000L;
	public static final long MEM_TOP_DOWN = 0x100000L;
	public static final long MEM_WRITE_WATCH = 0x200000L;
	public static final long MEM_PHYSICAL = 0x400000L;
	public static final long MEM_ROTATE = 0x800000L;
	public static final long MEM_DIFFERENT_IMAGE_BASE_OK = 0x800000L;
	public static final long MEM_RESET_UNDO = 0x1000000L;
	public static final long MEM_LARGE_PAGES = 0x20000000L;
	public static final long MEM_4MB_PAGES = 0x80000000L;
	public static final long MEM_64K_PAGES = (MEM_LARGE_PAGES | MEM_PHYSICAL);
	public static final long MEM_UNMAP_WITH_TRANSIENT_BOOST = 0x00000001L;
	public static final long MEM_COALESCE_PLACEHOLDERS = 0x00000001L;
	public static final long MEM_PRESERVE_PLACEHOLDER = 0x00000002L;
}

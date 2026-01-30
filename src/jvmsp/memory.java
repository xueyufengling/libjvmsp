package jvmsp;

import java.nio.ByteOrder;

public abstract class memory {

	/**
	 * 字节序
	 */
	public static enum endian {
		LITTLE, BIG;
	}

	public static final endian LOCAL_ENDIAN;

	static {
		LOCAL_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ? endian.BIG : endian.LITTLE;
	}

	public static final pointer malloc(long size) {
		return pointer.at(unsafe.allocate(size));
	}

	public static final pointer malloc(long size, Class<?> type_cls) {
		return pointer.at(unsafe.allocate(size * jtype.sizeof(type_cls)), type_cls);
	}

	public static final void free(pointer ptr) {
		unsafe.free(ptr.address());
	}

	public static final void memset(pointer ptr, int value, long bytes) {
		unsafe.memset(null, ptr.address(), bytes, (byte) value);
	}

	public static void memcpy(pointer ptrDest, pointer ptrSrc, long bytes) {
		unsafe.memcpy(null, ptrSrc.address(), null, ptrDest.address(), bytes);
	}

	static void memcpy(long addrDest, long addrSrc, long bytes) {
		unsafe.memcpy(null, addrSrc, null, addrDest, bytes);
	}
}

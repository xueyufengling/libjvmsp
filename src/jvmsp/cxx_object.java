package jvmsp;

/**
 * C++对象操作
 */
public class cxx_object {
	final pointer ptr;
	private cxx_type type;

	cxx_object(pointer ptr, cxx_type type) {
		this.ptr = ptr.copy().cast(type);
		this.type = type;
	}

	cxx_object(long addr, cxx_type type) {
		this.ptr = pointer.at(addr, getClass());
		this.type = type;
	}

	public final Object access(String field_name) {
		return type.field(field_name).access(ptr.addr);
	}

	public final Object access(cxx_field f) {
		return f.access(ptr.addr);
	}
}

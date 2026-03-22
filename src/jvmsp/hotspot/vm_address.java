package jvmsp.hotspot;

import java.util.HashMap;
import java.util.Map;

import jvmsp.memory;
import jvmsp.shared_object;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.object;
import jvmsp.unsafe;
import jvmsp.libso.libjvm;

public class vm_address
{
	public static final cxx_type VMAddressEntry = cxx_type.define("VMAddressEntry")
			.decl_field("name", cxx_type.pchar)
			.decl_field("value", cxx_type.pvoid)
			.resolve();

	public static class entry
	{
		// VMTypes信息的起始地址
		private static final long jvmciHotSpotVMAddresses;

		static
		{
			jvmciHotSpotVMAddresses = unsafe.read_long(shared_object.dlsym(libjvm._libjvm, "jvmciHotSpotVMAddresses"));
		}

		public final String name;// 地址的名称
		public final long value;// 地址的值

		private entry(long entry_addr)
		{
			object addr = VMAddressEntry.new object(entry_addr);
			this.name = memory.string((long) addr.read("name"));
			this.value = (long) addr.read("value");
		}

		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder().append("VMAddressEntry [")
					.append("name = ").append(name)
					.append(", value = ").append(value)
					.append(']');
			return sb.toString();
		}

		private static final Map<String, entry> address_entries = new HashMap<>();

		static
		{
			for (int idx = 0;; ++idx)
			{
				entry entry = new entry(jvmciHotSpotVMAddresses + idx * VMAddressEntry.size());
				address_entries.put(entry.name, entry);
				System.out.println(entry);
				if (entry.name == null)
				{
					break;
				}
			}
		}

		public static final entry get(String addr_name)
		{
			return address_entries.get(addr_name);
		}
	}

	public static final long get(String addr_name)
	{
		return entry.get(addr_name).value;
	}
}

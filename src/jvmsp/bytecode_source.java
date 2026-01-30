package jvmsp;

import java.util.HashMap;

@FunctionalInterface
public interface bytecode_source {
	/**
	 * 根据类名查找字节码
	 * 
	 * @param class_name
	 * @return
	 */
	public byte[] genByteCode(String class_name);

	public static class Map implements bytecode_source {
		public final HashMap<String, byte[]> klassDefs;

		public Map(HashMap<String, byte[]> klassDefs) {
			this.klassDefs = klassDefs;
		}

		@Override
		public byte[] genByteCode(String class_name) {
			return klassDefs.get(class_name);
		}

		public static Map from(HashMap<String, byte[]> klassDefs) {
			return new Map(klassDefs);
		}
	}

	public static Map asMap(bytecode_source source) {
		return (Map) source;
	}
}

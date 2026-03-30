package jvmsp.hotspot.utilities;

public class sizes
{
	public static final int in_words(long byte_size)
	{
		return (int) (byte_size / globalDefinitions.wordSize);
	}

	public static final int in_bytes(long word_size)
	{
		return (int) (word_size * globalDefinitions.wordSize);
	}
}

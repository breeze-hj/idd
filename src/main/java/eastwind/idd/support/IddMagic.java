package eastwind.idd.support;

public class IddMagic {

	public static final byte[] MAGIC = new byte[6];

	static {
		MAGIC[0] = 0x00;
		System.arraycopy("idd".getBytes(), 0, MAGIC, 1, 3);
		MAGIC[4] = (byte) 0xff;
	}

}

package eastwind.idd.model;

public class TcpObject {

	public Long id;
	public Long respondTo;
	public byte type;
	public byte args;
	public transient Object data;
	
	@Override
	public String toString() {
		return "TcpObject [id=" + id + ", respondTo=" + respondTo + ", type=" + type + ", data=" + data + "]";
	}
}

package eastwind.idd.idd;

public class Sequence {

	private String name;
	private long version;
	private long logId;
	private long nextVal;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public long getNextVal() {
		return nextVal;
	}

	public void setNextVal(long nextVal) {
		this.nextVal = nextVal;
	}

	public long getLogId() {
		return logId;
	}

	public void setLogId(long logId) {
		this.logId = logId;
	}
}

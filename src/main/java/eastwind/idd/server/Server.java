package eastwind.idd.server;

import java.net.InetSocketAddress;
import java.util.Date;

import eastwind.idd.support.StateFul;

public abstract class Server extends StateFul<ServerState> {

	protected String uuid;
	protected String addressStr;
	protected Date startTime;
	protected InetSocketAddress address;
	protected int role;

	public String getUuid() {
		return uuid;
	}

	public String getAddressStr() {
		return addressStr;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public int getRole() {
		return role;
	}

	public boolean isOnline() {
		return getState() == ServerState.ONLINE;
	}

	public boolean isOffline2() {
		return getState() == ServerState.OFFLINE2;
	}

	public boolean isMyself() {
		return false;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public abstract long getCurrentTerm();

	public abstract long getLogId();
}

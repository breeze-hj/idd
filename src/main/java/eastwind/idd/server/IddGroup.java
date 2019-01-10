package eastwind.idd.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eastwind.idd.support.StateFul;

public class IddGroup extends StateFul<IddGroupState> {

	private BootstrapServer myself;
	private Map<String, IddServer> address2Servers = new HashMap<>();

	public IddGroup(BootstrapServer myself) {
		this.myself = myself;
	}

	public GroupExchangeConsumer exchange(Object message, int timeout) {
		List<ExchangeContext> exchanges = new ArrayList<>();
		for (IddServer server : getAll()) {
			exchanges.add(server.exchange(message, timeout));
		}
		return new GroupExchangeConsumer(exchanges);
	}

	public Server findLeader() {
		if (myself.getRole() == Role.LEADER) {
			return myself;
		}
		for (IddServer server : address2Servers.values()) {
			if (server.getUuid() != null) {
				if (server.getRole() == Role.LEADER) {
					return server;
				}
			}
		}
		return null;
	}

	public boolean isEqAll(int n) {
		return n == getSize();
	}

	public boolean isGtThenHalf(int n) {
		return n > getSize() - n;
	}

	public boolean isHalfOnline() {
		int all = getSize();
		int onlines = 1;
		for (IddServer server : address2Servers.values()) {
			if (server.isOnline()) {
				onlines++;
			}
		}
		return onlines > all - onlines;
	}

	public boolean isAllOnOffLine() {
		for (IddServer server : address2Servers.values()) {
			if (server.isOnline() || server.isOffline2()) {
				continue;
			}
			return false;
		}
		return true;
	}

	public Server get(String addressStr) {
		if (myself.getAddressStr().equals(addressStr)) {
			return myself;
		}
		return address2Servers.get(addressStr);
	}

	public void stub(IddServer server) {
		address2Servers.put(server.getAddressStr(), server);
		server.whenOffline2(v -> {
			if (!isHalfOnline()) {
				changeState(IddGroupState.UNSERVICEABLE, null);
			}
		});
	}

	public int getSize() {
		return address2Servers.size() + 1;
	}

	public BootstrapServer getMyself() {
		return myself;
	}

	public void service() {
		changeState(IddGroupState.SERVICEABLE, null);
	}
	
	public boolean isServiceable() {
		return getState() == IddGroupState.SERVICEABLE;
	}
	
	public Iterable<IddServer> getAll() {
		return address2Servers.values();
	}
}

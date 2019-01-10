package eastwind.idd.server;

public class IddServerFactory extends BootstrapServiceable {

	public IddServerFactory(BootstrapServer bootstrapServer) {
		super(bootstrapServer);
	}

	public IddServer newServer(String addressStr) {
		IddGroup iddGroup = bootstrapServer.getIddGroup();
		IddServer server = new IddServer(addressStr);
		iddGroup.stub(server);
		server.whenOnline(v -> checkElect(server));
		server.whenOffline2(v -> checkElect(server));
		return server;
	}

	private void checkElect(IddServer newServer) {
		IddGroup iddGroup = bootstrapServer.getIddGroup();
		if (iddGroup.isAllOnOffLine() && iddGroup.isHalfOnline()) {
			Server leader = iddGroup.findLeader();
			if (leader == null || leader.isOffline2()) {
				bootstrapServer.getElectEngine().tryElect(newServer);
			}
		}
	}
}

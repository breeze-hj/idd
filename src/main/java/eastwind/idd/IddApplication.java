package eastwind.idd;

import eastwind.idd.server.BootstrapServer;
import eastwind.idd.server.IddClient;

public class IddApplication {

	private IddClient iddClient;

	public static IddApplication create(String addressStr, String allAddressesStr) {
		IddApplication iddApplication = new IddApplication();
		BootstrapServer bootstrapServer = new BootstrapServer(addressStr, allAddressesStr);
		bootstrapServer.start();
		iddApplication.iddClient = new IddClient(bootstrapServer);
		return iddApplication;
	}

	public IddClient getIddClient() {
		return iddClient;
	}
}

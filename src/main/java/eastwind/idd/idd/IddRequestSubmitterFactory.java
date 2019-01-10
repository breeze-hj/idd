package eastwind.idd.idd;

import eastwind.idd.server.BootstrapServer;
import eastwind.idd.server.BootstrapServiceable;
import eastwind.idd.server.Role;

public class IddRequestSubmitterFactory extends BootstrapServiceable {

	public IddRequestSubmitterFactory(BootstrapServer bootstrapServer) {
		super(bootstrapServer);
	}

	public IddRequestSubmitter newSubmitter() {
		if (bootstrapServer.getRole() == Role.LEADER) {
			DataServiceAdapter dataServiceAdapter = new DataServiceAdapter(bootstrapServer.getDataService());
			return new LeaderSubmitter(bootstrapServer.getIddGroup(), dataServiceAdapter);
		} else {
			return new FollowerSubmitter(bootstrapServer.getIddGroup());
		}
	}
	
}

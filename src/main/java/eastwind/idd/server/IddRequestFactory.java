package eastwind.idd.server;

import eastwind.idd.idd.Sequence;
import eastwind.idd.model.IddRequest;

public class IddRequestFactory extends BootstrapServiceable {

	public IddRequestFactory(BootstrapServer bootstrapServer) {
		super(bootstrapServer);
	}

	public IddRequest newIddRequest(int type, String name) {
		IddRequest iddRequest = new IddRequest();
		iddRequest.term = bootstrapServer.getCurrentTerm();
		iddRequest.requestType = type;
		iddRequest.sequence = new Sequence();
		iddRequest.sequence.setName(name);
		return iddRequest;
	}
	
}

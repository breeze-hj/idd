package eastwind.idd.handler;

import java.util.concurrent.CompletableFuture;

import eastwind.idd.channel.ExchangePair;
import eastwind.idd.channel.InboundChannel;
import eastwind.idd.channel.OutboundChannel;
import eastwind.idd.idd.DataServiceAdapter;
import eastwind.idd.idd.IddRequestSubmitterFactory;
import eastwind.idd.model.IddRequest;
import eastwind.idd.model.ObjectWrapper;
import eastwind.idd.server.BootstrapServer;
import eastwind.idd.server.BootstrapServiceable;
import eastwind.idd.server.Role;
import eastwind.idd.support.Result;

public class IddRequestHandler extends BootstrapServiceable implements Handler<IddRequest> {

//	private static Logger LOGGER = LoggerFactory.getLogger(IddRequestHandler.class);
	
	private DataServiceAdapter dataServiceAdapter;
	private IddRequestSubmitterFactory iddRequestSubmitterFactory;
	
	public IddRequestHandler(BootstrapServer bootstrapServer) {
		super(bootstrapServer);
		this.dataServiceAdapter = new DataServiceAdapter(bootstrapServer.getDataService());
		this.iddRequestSubmitterFactory = new IddRequestSubmitterFactory(bootstrapServer);
	}
	
	@Override
	public Object handleFromInboundChannel(InboundChannel channel, IddRequest t, ExchangePair pair) {
		if (t.term > bootstrapServer.getCurrentTerm()) {
			return Result.fail(3);
		}
		if (bootstrapServer.getRole() == Role.LEADER) {
			CompletableFuture<Result> cf = iddRequestSubmitterFactory.newSubmitter().submit(t);
			cf.thenAccept(r -> {
				ObjectWrapper wrapper = ObjectWrapper.wrap(pair.id, r);
				channel.send(wrapper, null, null);
			});
		} else {
			return dataServiceAdapter.execute(t);
		}
		return null;
	}

	@Override
	public Object handleFromOutboundChannel(OutboundChannel channel, IddRequest t, ExchangePair pair) {
		return null;
	}

}

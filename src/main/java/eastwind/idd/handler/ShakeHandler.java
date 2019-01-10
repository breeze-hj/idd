package eastwind.idd.handler;

import eastwind.idd.channel.ExchangePair;
import eastwind.idd.channel.InboundChannel;
import eastwind.idd.channel.OutboundChannel;
import eastwind.idd.model.Shake;
import eastwind.idd.server.BootstrapServer;
import eastwind.idd.server.BootstrapServiceable;
import eastwind.idd.server.IddGroup;
import eastwind.idd.server.IddServer;

public class ShakeHandler extends BootstrapServiceable implements Handler<Shake> {

	public ShakeHandler(BootstrapServer bootstrapServer) {
		super(bootstrapServer);
	}

	@Override
	public Object handleFromInboundChannel(InboundChannel channel, Shake t, ExchangePair pair) {
		IddGroup iddGroup = bootstrapServer.getIddGroup();
		IddServer iddServer = (IddServer) iddGroup.get(t.addressStr);
		channel.setServer(iddServer);
		bootstrapServer.getServerOpener().open(iddServer);
		return bootstrapServer.shakeBuilder().build();
	}

	@Override
	public Object handleFromOutboundChannel(OutboundChannel channel, Shake t, ExchangePair pair) {
		IddServer iddServer = channel.getServer();
		iddServer.setUuid(t.uuid);
		iddServer.setStartTime(t.startTime);
		iddServer.setRole(t.role);
		iddServer.setTerm(t.term);
		channel.shaked();
		return null;
	}

}

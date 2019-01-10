package eastwind.idd.handler;

import eastwind.idd.server.IddServer;
import eastwind.idd.channel.ExchangePair;
import eastwind.idd.channel.InboundChannel;
import eastwind.idd.channel.OutboundChannel;
import eastwind.idd.server.ExchangeContext;

public interface Handler<T> {

	Object handleFromInboundChannel(InboundChannel channel, T t, ExchangePair pair);

	Object handleFromOutboundChannel(OutboundChannel channel, T t, ExchangePair pair);

	default void completeExchange(OutboundChannel channel, T t, Long respondId) {
		IddServer server = channel.getServer();
		ExchangeContext context = server.removeExchange(respondId);
		context.success(t);
	}

}

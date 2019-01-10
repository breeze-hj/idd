package eastwind.idd.handler;

import eastwind.idd.channel.ExchangePair;
import eastwind.idd.channel.InboundChannel;
import eastwind.idd.channel.OutboundChannel;
import eastwind.idd.support.Result;

public class ResultHandler implements Handler<Result> {

	@Override
	public Object handleFromInboundChannel(InboundChannel channel, Result t, ExchangePair pair) {
		return null;
	}

	@Override
	public Object handleFromOutboundChannel(OutboundChannel channel, Result t, ExchangePair pair) {
		completeExchange(channel, t, pair.respondTo);
		return null;
	}

}

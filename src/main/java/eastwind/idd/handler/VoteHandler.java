package eastwind.idd.handler;

import eastwind.idd.channel.ExchangePair;
import eastwind.idd.channel.InboundChannel;
import eastwind.idd.channel.OutboundChannel;
import eastwind.idd.model.Vote;
import eastwind.idd.server.ElectEngine;

public class VoteHandler implements Handler<Vote> {

	private ElectEngine electEngine;

	public VoteHandler(ElectEngine electEngine) {
		this.electEngine = electEngine;
	}

	@Override
	public Object handleFromInboundChannel(InboundChannel channel, Vote t, ExchangePair pair) {
		return electEngine.recvVote(channel.getServer(), t);
	}

	@Override
	public Object handleFromOutboundChannel(OutboundChannel channel, Vote t, ExchangePair pair) {
		completeExchange(channel, t, pair.respondTo);
		return null;
	}

}

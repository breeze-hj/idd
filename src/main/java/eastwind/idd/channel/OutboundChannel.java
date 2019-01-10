package eastwind.idd.channel;

import java.net.InetSocketAddress;

import eastwind.idd.handler.Handler;

public class OutboundChannel extends TcpChannel implements AsyncOpenChannel {

	private InetSocketAddress remoteAddress;
	private boolean opening;

	public OutboundChannel(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	public boolean isOpening() {
		return opening;
	}

	public void setOpening(boolean opening) {
		this.opening = opening;
	}

	@Override
	protected Object handle(Handler<Object> handler, Object data, ExchangePair exchangePair) {
		return handler.handleFromOutboundChannel(this, data, exchangePair);
	}

}

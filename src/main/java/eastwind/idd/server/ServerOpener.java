package eastwind.idd.server;

import java.net.InetSocketAddress;
import java.util.List;

import eastwind.idd.channel.ChannelFactory;
import eastwind.idd.channel.ChannelOpener;
import eastwind.idd.channel.OutboundChannel;
import eastwind.idd.support.IddUtils;

public class ServerOpener {

	private ChannelFactory channelFactory;
	private ChannelOpener channelOpener;

	public ServerOpener(ChannelFactory channelFactory, ChannelOpener channelOpener) {
		this.channelFactory = channelFactory;
		this.channelOpener = channelOpener;
	}

	public void open(IddServer server) {
		List<OutboundChannel> outboundChannels = server.getOutboundChannels();
		if (outboundChannels.size() == 0) {
			InetSocketAddress remoteAddress = IddUtils.parseAddress(server.getAddressStr());
			OutboundChannel channel = channelFactory.newOutboundChannel(remoteAddress);
			server.addChannel(channel);
			channel.setServer(server);
			channelOpener.open(channel);
		} else {
			for (OutboundChannel channel : outboundChannels) {
				if (channel.isClosed() && !channel.isOpening()) {
					channelOpener.open(channel);
				}
			}
		}
	}
}

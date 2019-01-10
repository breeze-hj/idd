package eastwind.idd.channel;

import java.net.InetSocketAddress;

public class MasterChannel extends AbstractChannel implements AsyncOpenChannel {

	private InetSocketAddress address;
	private ChannelFactory channelFactory;

	public MasterChannel(InetSocketAddress address, ChannelFactory channelFactory) {
		this.address = address;
		this.channelFactory = channelFactory;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public ChannelFactory getChannelFactory() {
		return channelFactory;
	}

}

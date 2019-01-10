package eastwind.idd.channel;

import java.util.ArrayList;
import java.util.List;

public class ChannelGroup {

	private List<InboundChannel> inboundChannels = new ArrayList<>();
	private List<OutboundChannel> outboundChannels = new ArrayList<>();

	public void addChannel(TcpChannel tcpChannel) {
		if (tcpChannel instanceof InboundChannel) {
			inboundChannels.add((InboundChannel) tcpChannel);
		} else if (tcpChannel instanceof OutboundChannel) {
			outboundChannels.add((OutboundChannel) tcpChannel);
		}
	}

	public void removeChannel(TcpChannel tcpChannel) {
		if (tcpChannel instanceof InboundChannel) {
			inboundChannels.remove((InboundChannel) tcpChannel);
		} else {
			outboundChannels.remove((OutboundChannel) tcpChannel);
		}
	}

	public OutboundChannel getOne() {
		for (OutboundChannel channel : outboundChannels) {
			if (channel.isShaked()) {
				return channel;
			}
		}
		return null;
	}
	
	public List<OutboundChannel> getOutboundChannels() {
		return outboundChannels;
	}

}

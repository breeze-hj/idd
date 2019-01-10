package eastwind.idd.channel;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import eastwind.idd.handler.IddRequestHandler;
import eastwind.idd.handler.ResultHandler;
import eastwind.idd.handler.ShakeHandler;
import eastwind.idd.handler.VoteHandler;
import eastwind.idd.http.HttpRequestDispatcher;
import eastwind.idd.http.HttpRequestDispatcherFactory;
import eastwind.idd.model.Shake;
import eastwind.idd.server.BootstrapServer;
import eastwind.idd.server.BootstrapServiceable;
import io.netty.channel.Channel;

public class ChannelFactory extends BootstrapServiceable {

	private HttpRequestDispatcher httpRequestDispatcher;
	private ObjectMapper objectMapper;

	public ChannelFactory(BootstrapServer bootstrapServer) {
		super(bootstrapServer);
		HttpRequestDispatcherFactory factory = new HttpRequestDispatcherFactory(bootstrapServer);
		this.httpRequestDispatcher = factory.newHttpRequestDispather();
		
		objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	}

	public MasterChannel newMasterChannel(InetSocketAddress address) {
		MasterChannel channel = new MasterChannel(address, this);
		return channel;
	}

	public InboundChannel newInboundChannel(Channel nettyChannel) {
		InboundChannel channel = new InboundChannel(nettyChannel);
		channel.setHandlerProvider(defaultApplyProvider());
		channel.active();
		return channel;
	}

	public OutboundChannel newOutboundChannel(InetSocketAddress remoteAddress) {
		OutboundChannel channel = new OutboundChannel(remoteAddress);
		channel.setHandlerProvider(defaultApplyProvider());
		channel.onActive(v -> {
			Shake shake = bootstrapServer.shakeBuilder().build();
			channel.send(shake, null, null);
		});
		channel.onShaked(v -> {
			channel.getServer().online();
		});
		channel.onClosed(v -> channel.setOpening(false));
		return channel;
	}

	private HandlerProvider defaultApplyProvider() {
		HandlerProvider handlerProvider = new HandlerProvider();
		handlerProvider.register(new ShakeHandler(bootstrapServer));
		handlerProvider.register(new VoteHandler(bootstrapServer.getElectEngine()));
		handlerProvider.register(new IddRequestHandler(bootstrapServer));
		handlerProvider.register(new ResultHandler());
		return handlerProvider;
	}

	public HttpChannel newHttpChannel(Channel nettyChannel) {
		return new HttpChannel(nettyChannel, httpRequestDispatcher, objectMapper);
	}
	
}

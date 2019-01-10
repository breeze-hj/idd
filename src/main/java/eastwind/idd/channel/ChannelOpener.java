package eastwind.idd.channel;

import eastwind.idd.codec.ChannelClassifyHandler;
import eastwind.idd.codec.IOExceptionHandler;
import eastwind.idd.codec.RecvHandler;
import eastwind.idd.codec.TcpObjectCodec;
import eastwind.idd.support.NamedThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ChannelOpener {

	private Bootstrap bootstrap = new Bootstrap();
	private ServerBootstrap serverBootstrap = new ServerBootstrap();
	private NioEventLoopGroup parentGroup;
	private NioEventLoopGroup childGroup;

	public ChannelOpener(String threadPrefix) {
		parentGroup = new NioEventLoopGroup(1, new NamedThreadFactory(threadPrefix + "-io0"));
		childGroup = new NioEventLoopGroup(0, new NamedThreadFactory(threadPrefix + "-io1"));

		IOExceptionHandler ioExceptionHandler = new IOExceptionHandler();
		RecvHandler recvHandler = new RecvHandler();
		ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast(ioExceptionHandler);
				pipeline.addLast(new TcpObjectCodec());
				pipeline.addLast(recvHandler);
			}
		};
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.group(childGroup);
		bootstrap.handler(channelInitializer);
	}

	public void open(MasterChannel channel) {
		ChannelClassifyHandler cch = new ChannelClassifyHandler(channel.getChannelFactory());
		ChannelInitializer<Channel> sci = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(cch);
			}
		};

		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.group(parentGroup, childGroup);
		serverBootstrap.childHandler(sci);
		serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);

		channel.bind(serverBootstrap.bind(channel.getAddress()));
	}

	public void open(OutboundChannel channel) {
		ChannelFuture channelFuture = bootstrap.connect(channel.getRemoteAddress());
		channelFuture.addListener((f) -> NettyChannelBinder.bind(channelFuture.channel(), channel));
		channel.bind(channelFuture);
	}

	public void shutdown() {
		parentGroup.shutdownGracefully();
		childGroup.shutdownGracefully();
	}
}

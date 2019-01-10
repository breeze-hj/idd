package eastwind.idd.codec;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eastwind.idd.channel.ChannelFactory;
import eastwind.idd.channel.HttpChannel;
import eastwind.idd.channel.InboundChannel;
import eastwind.idd.channel.NettyChannelBinder;
import eastwind.idd.support.IddMagic;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

@Sharable
public class ChannelClassifyHandler extends ChannelInboundHandlerAdapter {

	private static Logger LOGGER = LoggerFactory.getLogger(ChannelClassifyHandler.class);
	
	private static List<byte[]> HTTP_MAGICS = Lists
			.newArrayList("GET ", "POST ", "DELETE ", "PUT ", "HEAD ", "OPTIONS ").stream().map(m -> m.getBytes())
			.collect(Collectors.toList());

	private ChannelFactory channelFactory;
	private IOExceptionHandler ioExceptionHandler = new IOExceptionHandler();
	private RecvHandler recvHandler = new RecvHandler();
	
	public ChannelClassifyHandler(ChannelFactory channelFactory) {
		this.channelFactory = channelFactory;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ChannelPipeline pipeline = ctx.pipeline();
		ByteBuf in = (ByteBuf) msg;
		if (in.readableBytes() >= IddMagic.MAGIC.length) {
			if (startWith(in, IddMagic.MAGIC)) {
				Channel channel = ctx.channel();
				InboundChannel inboundChannel = channelFactory.newInboundChannel(channel);
				NettyChannelBinder.bind(channel, inboundChannel);
				pipeline.addLast(ioExceptionHandler);
				pipeline.addLast(new TcpObjectCodec());
				pipeline.addLast(recvHandler);
				pipeline.remove(this);
				ctx.fireChannelRead(msg);
				return;
			}
		}
		for (byte[] httpMagic : HTTP_MAGICS) {
			if (startWith(in, httpMagic)) {
				Channel channel = ctx.channel();
				HttpChannel httpChannel = channelFactory.newHttpChannel(channel);
				NettyChannelBinder.bind(channel, httpChannel);
				pipeline.addLast(ioExceptionHandler);
				pipeline.addLast(new HttpServerCodec());
				pipeline.addLast(new HttpObjectAggregator(1024));
				pipeline.addLast(recvHandler);
				pipeline.remove(this);
				ctx.fireChannelRead(msg);
				return;
			}
		}
		if (in.readableBytes() > 8) {
			LOGGER.error("Unsupported protocal from {}", ctx.channel());
		}
	}


	private boolean startWith(ByteBuf in, byte[] magic) {
		if (in.readableBytes() < magic.length) {
			return false;
		}
		for (int i = 0; i < magic.length; i++) {
			if (in.getByte(i) != magic[i]) {
				return false;
			}
		}
		return true;
	}
}

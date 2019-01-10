package eastwind.idd.codec;

import eastwind.idd.channel.InetChannel;
import eastwind.idd.channel.NettyChannelBinder;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
public class RecvHandler extends SimpleChannelInboundHandler<Object> {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		InetChannel channel = NettyChannelBinder.getBinder(ctx.channel());
		channel.recv(msg);
	}

}

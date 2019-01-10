package eastwind.idd.channel;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * Created by jan.huang on 2018/3/5.
 */
public class NettyChannelBinder {

	private static final AttributeKey<InetChannel<?>> INET_CHANNEL = AttributeKey.valueOf("INET_CHANNEL");

	public static void bind(Channel channel, InetChannel<?> inetChannel) {
		channel.attr(INET_CHANNEL).set(inetChannel);
	}

	public static InetChannel<?> getBinder(Channel channel) {
		return channel.attr(INET_CHANNEL).get();
	}
}

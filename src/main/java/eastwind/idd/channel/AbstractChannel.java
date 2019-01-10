package eastwind.idd.channel;

import java.util.function.Consumer;

import eastwind.idd.support.Result;
import eastwind.idd.support.StateFul;
import io.netty.channel.Channel;

/**
 * Created by jan.huang on 2018/4/17.
 */
public class AbstractChannel extends StateFul<ChannelState> {

	protected Channel channel;

	public AbstractChannel() {
		super.state = ChannelState.INITIAL;
	}

	public void setNettyChannel(Channel channel) {
		this.channel = channel;
		this.channel.closeFuture().addListener(cf -> close());
	}

	public void active() {
		super.changeState(ChannelState.ACTIVE, null);
	}

	public void onActive(Consumer<Void> consumer) {
		super.onState(ChannelState.ACTIVE, consumer);
	}

	public boolean isActive() {
		return getState() == ChannelState.ACTIVE;
	}

	public void inactive(Throwable th) {
		super.changeState(ChannelState.INACTIVE, Result.fail(th));
	}

	public void onInactive(Consumer<Throwable> consumer) {
		super.onState(ChannelState.INACTIVE, consumer);
	}

	public void shaked() {
		super.changeState(ChannelState.SHAKED, null);
	}

	public void onShaked(Consumer<Void> consumer) {
		super.onState(ChannelState.SHAKED, consumer);
	}

	public boolean isShaked() {
		return getState() == ChannelState.SHAKED;
	}

	public void shakeFailed(int code) {
		super.changeState(ChannelState.SHAK_FAILED, code);
	}

	public void onShakeFailed(Consumer<Void> consumer) {
		super.onState(ChannelState.SHAK_FAILED, consumer);
	}

	public void close() {
		super.changeState(ChannelState.CLOSED, null);
	}

	public void onClosed(Consumer<Void> consumer) {
		super.onState(ChannelState.CLOSED, consumer);
	}

	public boolean isClosed() {
		return getState() == ChannelState.CLOSED;
	}

	public Channel getNettyChannel() {
		return channel;
	}
}

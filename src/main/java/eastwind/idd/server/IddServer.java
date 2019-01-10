package eastwind.idd.server;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.idd.channel.ChannelGroup;
import eastwind.idd.channel.TcpChannel;
import eastwind.idd.channel.OutboundChannel;
import eastwind.idd.model.TcpObject;
import eastwind.idd.support.IddUtils;

public class IddServer extends Server {

	private static Logger LOGGER = LoggerFactory.getLogger(IddServer.class);

	private long currentTerm;
	private long logId;
	private long lastSentTime;
	private long lastRecvTime;

	private Map<Long, ExchangeContext> exchangePool = new ConcurrentHashMap<>();
	private ChannelGroup channelGroup = new ChannelGroup();

	public IddServer(String addressStr) {
		super.addressStr = addressStr;
		super.address = IddUtils.parseAddress(addressStr);
	}

	public long getLastSentTime() {
		return lastSentTime;
	}

	public void resetLastSentTime() {
		this.lastSentTime = System.currentTimeMillis();
	}

	public long getLastRecvTime() {
		return lastRecvTime;
	}

	public void resetLastRecvTime() {
		this.lastRecvTime = System.currentTimeMillis();
	}

	public void addChannel(TcpChannel tcpChannel) {
		channelGroup.addChannel(tcpChannel);
	}

	public void send(Object message) {
		OutboundChannel channel = channelGroup.getOne();
		if (channel != null) {
			channel.send(message, null, null);
		}
	}

	public ExchangeContext exchange(Object message, int timeout) {
		ExchangeContext context = new ExchangeContext();
		OutboundChannel channel = channelGroup.getOne();
		if (channel == null) {
			context.fail(1);
		} else {
			CompletableFuture<TcpObject> cf = new CompletableFuture<TcpObject>();
			final AtomicLong id = new AtomicLong();
			channel.send(message, o -> {
				id.set(o.id);
				addExchange(o.id, context);
			}, cf);
			cf.exceptionally(th -> {
				removeExchange(id.get());
				return null;
			});
		}
		return context;
	}

	private void addExchange(Long id, ExchangeContext context) {
		synchronized (exchangePool) {
			exchangePool.put(id, context);
		}
	}

	public ExchangeContext removeExchange(Long id) {
		synchronized (exchangePool) {
			return exchangePool.remove(id);
		}
	}

	public List<OutboundChannel> getOutboundChannels() {
		return channelGroup.getOutboundChannels();
	}

	public void online() {
		LOGGER.info("{} is online.", this);
		super.changeState(ServerState.ONLINE, null);
	}

	public void whenOnline(Consumer<Object> consumer) {
		super.onState(ServerState.ONLINE, consumer);
	}

	public void offline1() {
		super.changeState(ServerState.OFFLINE1, null);
	}

	public boolean isOffline1() {
		return getState() == ServerState.OFFLINE1;
	}

	public void whenOffline1(Consumer<Object> consumer) {
		super.onState(ServerState.OFFLINE1, consumer);
	}

	public void offline2() {
		LOGGER.info("{} is offline2.", this);
		super.changeState(ServerState.OFFLINE2, null);
	}

	public void whenOffline2(Consumer<Object> consumer) {
		super.onState(ServerState.OFFLINE2, consumer);
	}

	@Override
	public long getCurrentTerm() {
		return currentTerm;
	}

	@Override
	public long getLogId() {
		return logId;
	}

	public void setUuid(String uuid) {
		super.uuid = uuid;
	}

	public void setAddressStr(String addressStr) {
		super.addressStr = addressStr;
	}

	public void setStartTime(Date startTime) {
		super.startTime = startTime;
	}

	public void setRole(int role) {
		super.role = role;
	}

	public void setTerm(long term) {
		this.currentTerm = term;
	}

	public void setLogId(long logId) {
		this.logId = logId;
	}

	@Override
	public String toString() {
		return String.format("remote[idd@%s]", addressStr);
	}

}

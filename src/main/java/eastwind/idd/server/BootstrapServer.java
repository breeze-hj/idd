package eastwind.idd.server;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import eastwind.idd.channel.ChannelFactory;
import eastwind.idd.channel.ChannelOpener;
import eastwind.idd.channel.MasterChannel;
import eastwind.idd.data.DataService;
import eastwind.idd.data.MemoryDataService;
import eastwind.idd.support.DelayedExecutor;
import eastwind.idd.support.HashedWheelTimerExecutor;
import eastwind.idd.support.IddUtils;
import eastwind.idd.support.QueuedDelayedExecutor;

public class BootstrapServer extends Server {

	private static Logger LOGGER = LoggerFactory.getLogger(BootstrapServer.class);

	private String allAddressesStr;
	private MasterChannel masterChannel;
	private ChannelFactory channelFactory;
	private ChannelOpener channelOpener;
	private ServerOpener serverOpener;
	private IddGroup iddGroup = new IddGroup(this);
	private ElectEngine electEngine;
	private DataService dataService = new MemoryDataService();
	private IddServerFactory iddServerFactory = new IddServerFactory(this);

	private DelayedExecutor hashedWheelTimerExecutor;
	private DelayedExecutor queuedDelayedExecutor;
	private CompletableFuture<Void> shutdownFuture = new CompletableFuture<Void>();

	public BootstrapServer(String addressStr, String allAddressesStr) {
		super.uuid = UUID.randomUUID().toString();
		super.addressStr = addressStr.trim();
		super.address = IddUtils.parseAddress(addressStr);
		super.startTime = new Date();
		this.allAddressesStr = allAddressesStr;
		this.channelFactory = new ChannelFactory(this);
		this.masterChannel = channelFactory.newMasterChannel(address);

		String threadPrefix = "idd@" + address.getPort();
		this.channelOpener = new ChannelOpener(threadPrefix);
		this.serverOpener = new ServerOpener(channelFactory, channelOpener);

		this.hashedWheelTimerExecutor = new HashedWheelTimerExecutor(threadPrefix);
		this.queuedDelayedExecutor = new QueuedDelayedExecutor(threadPrefix);
		super.role = Role.UNDEFINED;
	}

	public void start() {
		LOGGER.info("starting {}...", this);
		electEngine = new ElectEngine(iddGroup, queuedDelayedExecutor, dataService);
		electEngine.onElected(t -> iddGroup.service());
		channelOpener.open(masterChannel);
		CompletableFuture<Object> cf = new CompletableFuture<Object>();
		prepare();
		masterChannel.onActive(v -> {
			cf.complete(null);
			startCluster();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				this.shutdown();
			}));
			LOGGER.info("{} start OK!", this);
		});
		masterChannel.onInactive(th -> {
			cf.completeExceptionally(th);
			LOGGER.error("{} start fail!", this);
			LOGGER.error("", th);
			this.shutdown();
		});
		cf.join();
	}

	private void prepare() {
		List<String> l = Splitter.onPattern("[,;]").trimResults().omitEmptyStrings().splitToList(allAddressesStr);
		for (String str : l) {
			if (!str.equals(addressStr)) {
				iddServerFactory.newServer(str);
			}
		}
	}

	private void startCluster() {
		for (IddServer server : iddGroup.getAll()) {
			serverOpener.open(server);
		}
	}

	public String getAllAddressesStr() {
		return allAddressesStr;
	}

	public void shutdown() {
		shutdownFuture.complete(null);
	}

	public ShakeBuilder shakeBuilder() {
		return new ShakeBuilder(this);
	}

	public CompletableFuture<Void> getShutdownFuture() {
		return shutdownFuture;
	}

	public IddGroup getIddGroup() {
		return iddGroup;
	}

	public ElectEngine getElectEngine() {
		return electEngine;
	}

	public ServerOpener getServerOpener() {
		return serverOpener;
	}

	public DelayedExecutor getHashedWheelTimerExecutor() {
		return hashedWheelTimerExecutor;
	}

	public DelayedExecutor getQueuedDelayedExecutor() {
		return queuedDelayedExecutor;
	}

	public boolean isMyself() {
		return true;
	}

	@Override
	public long getCurrentTerm() {
		return dataService.getCurrentTerm();
	}

	public DataService getDataService() {
		return dataService;
	}

	@Override
	public long getLogId() {
		return dataService.getLogId();
	}

	@Override
	public String toString() {
		return String.format("master[idd@%s (%s)]", addressStr, uuid);
	}
}

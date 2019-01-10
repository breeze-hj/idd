package eastwind.idd.server;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eastwind.idd.data.DataService;
import eastwind.idd.model.Vote;
import eastwind.idd.support.DelayedExecutor;
import eastwind.idd.support.StateFul;

public class ElectEngine extends StateFul<ElectState> {

	private static Logger LOGGER = LoggerFactory.getLogger(ElectEngine.class);

	private static final int TIMEOUT = 50;
	private static final int TASK_DELAY = 100;

	private IddGroup iddGroup;
	private DataService dataService;
	private Server votedFor;
	// initial pre-vote pre-candidate candidate leader
	private int step;
	private int modCount;
	private Map<IddServer, Server> votedForOfOthers = new HashMap<>();
	private Set<IddServer> notifyed = new HashSet<>();
	private DelayedExecutor delayedExecutor;

	public ElectEngine(IddGroup iddGroup, DelayedExecutor delayedExecutor, DataService dataService) {
		super.state = ElectState.INITIAL;
		this.iddGroup = iddGroup;
		this.delayedExecutor = delayedExecutor;
		this.dataService = dataService;
	}

	public void tryElect(IddServer newServer) {
		// electing
		if (step > 0) {
			if (newServer.isOnline() && votedFor != null && !notifyed.contains(newServer)) {
				if (votedFor == getMyself()) {
					Vote forVote = Vote.forVote(dataService.getCurrentTerm(), getMyself().getLogId());
					doForVoteOne(forVote, newServer);
				} else {
					Vote notify = Vote.vote(votedFor.addressStr);
					doNotifyOne(notify, newServer);
				}
			}
		} else if (iddGroup.isAllOnOffLine()) {
			// TODO check term and multi leaders
			Server leader = iddGroup.findLeader();
			if (leader == null || leader.isOffline2()) {
				doPreVote();
			}
		}
	}

	public void onElected(Consumer<Void> consumer) {
		super.onState(ElectState.ELECTED, consumer);
	}

	private void doPreVote() {
		if (votedFor != null) {
			return;
		}
		LOGGER.info("do pre-vote..., try {} times.", modCount);
		this.step = 1;
		long currentTerm = dataService.getCurrentTerm();
		long logId = dataService.getLogId();
		Vote preVote = Vote.preVote(currentTerm, logId);
		GroupExchangeConsumer gec = iddGroup.exchange(preVote, 50);
		gec.allOf(t -> {
			if (step == 1) {
				modCount++;
				delayedExecutor.delayExecute(TASK_DELAY, de -> doPreVote());
			}
		});
		gec.anyOf(t -> {
			if (gec.isHalfCompleted()) {
				Stream<ExchangeContext> stream = gec.getCompleted().stream();
				int n = (int) stream.filter(ec -> ((Vote) ec.getResult()).type == Vote.AGREE).count();
				if (iddGroup.isGtThenHalf(n + 1)) {
					doForVote();
					gec.allOf(null).anyOf(null);
				} else {
					stream = gec.getCompleted().stream();
					n = (int) stream.filter(ec -> ((Vote) ec.getResult()).type == Vote.OPPOSE).count();
					if (iddGroup.isGtThenHalf(n)) {
						// TODO
					}
				}
			}
		});
	}

	private void doForVote() {
		this.step = 2;
		delayedExecutor.delayExecute(defaultVoteDelay(), de -> {
			if (votedFor == null) {
				votedFor = getMyself();
				LOGGER.info("vote to {}.", getMyself());
				this.step = 3;
				long term = dataService.incrementTerm();
				long logId = dataService.getLogId();
				Vote forVote = Vote.forVote(term, logId);
				for (IddServer server : iddGroup.getAll()) {
					if (server.isOnline()) {
						doForVoteOne(forVote, server);
					}
				}
				delayedExecutor.delayExecute(TASK_DELAY >> 1, new CandidateTask());
			}
		});
	}

	private void doForVoteOne(Vote forVote, IddServer server) {
		ExchangeContext ec = server.exchange(forVote, TIMEOUT);
		ec.onSuccess(t -> {
			notifyed.add(server);
			Vote back = (Vote) t;
			if (back.type == Vote.AGREE) {
				// do nothing
			}
		});
	}

	private void doNotify() {
		Vote vote = Vote.vote(votedFor.addressStr);
		for (IddServer server : iddGroup.getAll()) {
			if (server.isOnline()) {
				doNotifyOne(vote, server);
			}
		}
	}

	private void doNotifyOne(Vote vote, IddServer server) {
		server.exchange(vote, TIMEOUT).onSuccess(t -> {
			notifyed.add(server);
		});
	}

	public Vote recvVote(IddServer from, Vote inbound) {
		if (inbound.type == Vote.PRE_VOTE) {
			return recvPreVote(inbound);
		} else if (inbound.type == Vote.FOR_VOTE) {
			return recvForVote(from, inbound);
		} else if (inbound.type == Vote.VOTE) {
			Server to = iddGroup.get(inbound.target);
			votedForOfOthers.put(from, to);
			return Vote.agree();
		} else if (inbound.type == Vote.LEADER) {
			getMyself().setRole(Role.FOLLOWER);

			from.setRole(Role.LEADER);
			from.setTerm(inbound.term);
			from.setLogId(inbound.logId);

			LOGGER.info("new leader: {}.", from);
			changeState(ElectState.ELECTED, null);
		}
		return null;
	}

	private Vote recvForVote(IddServer from, Vote inbound) {
		votedForOfOthers.put(from, from);
		if (votedFor == null && getMyself().getCurrentTerm() < inbound.term
				&& getMyself().getLogId() <= inbound.logId) {
			this.votedFor = from;
			dataService.setCurrentTerm(inbound.term);
			LOGGER.info("vote to {}.", from);
			step = 2;
			doNotify();
			return Vote.agree();
		} else {
			return Vote.oppose();
		}
	}

	private Vote recvPreVote(Vote inbound) {
		Server server = iddGroup.findLeader();
		if (server == null) {
			if (iddGroup.isAllOnOffLine()) {
				if (inbound.term >= getMyself().getCurrentTerm() && inbound.logId >= getMyself().getLogId()) {
					return Vote.agree();
				} else {
					return Vote.oppose();
				}
			} else {
				return Vote.pending();
			}
		} else {
			if (server.isOffline2()) {
				if (inbound.term >= getMyself().getCurrentTerm() && inbound.logId >= getMyself().getLogId()) {
					return Vote.agree();
				} else {
					return Vote.oppose();
				}
			} else {
				return Vote.oppose();
			}
		}
	}

	private Map<Server, Integer> countVotes() {
		Map<Server, Integer> countOfVotes = new HashMap<>();
		for (Server server : votedForOfOthers.values()) {
			countOfVotes.compute(server, (k, v) -> v == null ? 1 : v + 1);
		}
		if (votedFor != null) {
			countOfVotes.compute(votedFor, (k, v) -> v == null ? 1 : v + 1);
		}
		return countOfVotes;
	}

	private Server getMyself() {
		return iddGroup.getMyself();
	}

	private long defaultVoteDelay() {
		return RandomUtils.nextInt(50, 250);
	}

	private void reset() {
		step = 0;
		notifyed.clear();
		votedForOfOthers.clear();
	}

	private class CandidateTask implements Consumer<DelayedExecutor> {

		private int uselessRun;
		private int _modCount;

		@Override
		public void accept(DelayedExecutor de) {
			if (step == 0) {
				return;
			}
			if (iddGroup.isEqAll(votedForOfOthers.size() + 1) || uselessRun > 2) {
				if (iddGroup.isGtThenHalf(votedForOfOthers.size() + 1)) {
					Map<Server, Integer> voteCounts = countVotes();
					int mine = voteCounts.get(getMyself());
					if (iddGroup.isGtThenHalf(mine)) {
						LOGGER.info("new leader: {}, votes:{}/{}", getMyself(), mine, iddGroup.getSize());
						reset();
						getMyself().setRole(Role.LEADER);
						Vote vote = Vote.leader(dataService.getCurrentTerm(), dataService.getLogId());
						for (IddServer server : iddGroup.getAll()) {
							server.send(vote);
							changeState(ElectState.ELECTED, null);
						}
					} else {
						int max = voteCounts.entrySet().stream().max(Comparator.comparingInt(en -> en.getValue())).get()
								.getValue();
						if (mine < max) {
							// withdraw
							return;
						} else {
							// re-vote
							modCount = 0;
							notifyed.clear();
							votedForOfOthers.clear();
							de.delayExecute(defaultVoteDelay(), t -> doForVote());
						}
					}
				}
			} else {
				if (_modCount == modCount) {
					uselessRun++;
				} else {
					uselessRun = 0;
					_modCount = modCount;
				}
				de.delayExecute(TASK_DELAY, this);
			}
		}

	}
}

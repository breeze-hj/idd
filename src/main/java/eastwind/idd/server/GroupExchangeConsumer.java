package eastwind.idd.server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GroupExchangeConsumer {

	private List<ExchangeContext> uncompleted;
	private List<ExchangeContext> completed;
	private Consumer<Void> anyOf;
	private Consumer<Void> allOf;

	public GroupExchangeConsumer(List<ExchangeContext> all) {
		this.uncompleted = new ArrayList<>(all);
		this.completed = new ArrayList<>(all.size());
		for (ExchangeContext ec : all) {
			ec.onStateOnce(t -> {
				synchronized (GroupExchangeConsumer.this) {
					uncompleted.remove(ec);
					completed.add(ec);
					if (anyOf != null) {
						anyOf.accept(null);
					}
					if (allOf != null && uncompleted.size() == 0) {
						allOf.accept(null);
					}
				}
			}, ExchangeState.ALL);
		}
	}

	public GroupExchangeConsumer anyOf(Consumer<Void> anyOf) {
		this.anyOf = anyOf;
		if (anyOf != null) {
			if (completed.size() > 0) {
				anyOf.accept(null);
			}
		}
		return this;
	}

	public GroupExchangeConsumer allOf(Consumer<Void> allOf) {
		this.allOf = allOf;
		if (allOf != null) {
			if (uncompleted.size() == 0) {
				allOf.accept(null);
			}
		}
		return this;
	}

	public boolean isHalfCompleted() {
		return completed.size() >= uncompleted.size();
	}

	public List<ExchangeContext> getCompleted() {
		return completed;
	}

	public List<ExchangeContext> getUncompleted() {
		return uncompleted;
	}

	public int getSize() {
		return completed.size() + uncompleted.size();
	}
}

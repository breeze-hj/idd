package eastwind.idd.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by jan.huang on 2018/4/11.
 */
public class StateFul<T extends State> {

	protected T state;
	@SuppressWarnings("rawtypes")
	private Map<T, LinkedList<Consumer>> stateConsumers = new HashMap<>(4);
	private Object result;

	private List<OnceConsumer<T>> onceConsumers = new LinkedList<>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void changeState(T newState, Object result) {
		this.result = result;
		
		if (this.state != newState) {
			this.state = newState;
			LinkedList<Consumer> consumers = stateConsumers.get(newState);
			if (consumers != null) {
				for (Consumer consumer : consumers) {
					consumer.accept(result);
				}
			}
		}

		if (onceConsumers.size() > 0) {
			List<OnceConsumer<T>> remove = new ArrayList<>(onceConsumers.size());
			for (OnceConsumer<T> oc : onceConsumers) {
				if (oc.states.contains(state)) {
					oc.consumer.accept(result);
					remove.add(oc);
				} else {
					for (T s : oc.states) {
						if (s.level > state.level) {
							break;
						}
						remove.add(oc);
					}
				}
			}
			onceConsumers.removeAll(remove);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void onState(T state, Consumer consumer) {
		if (state == this.state) {
			consumer.accept(result);
		} else {
			LinkedList<Consumer> consumers = stateConsumers.get(state);
			if (consumers == null) {
				consumers = new LinkedList<>();
				stateConsumers.put(state, consumers);
			}
			consumers.add(consumer);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onStateOnce(Consumer consumer, T... states) {
		List<T> l = Arrays.asList(states);
		if (l.contains(state)) {
			consumer.accept(result);
		} else {
			onceConsumers.add(new OnceConsumer<>(l, consumer));
		}
	}

	public T getState() {
		return state;
	}

	public Object getResult() {
		return result;
	}

	static class OnceConsumer<T extends State> {
		List<T> states;
		@SuppressWarnings("rawtypes")
		Consumer consumer;

		@SuppressWarnings("rawtypes")
		public OnceConsumer(List<T> states, Consumer consumer) {
			this.states = states;
			this.consumer = consumer;
		}

	}
}

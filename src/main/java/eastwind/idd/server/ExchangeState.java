package eastwind.idd.server;

import eastwind.idd.support.State;

public class ExchangeState extends State {

	public static final ExchangeState INITIAL = new ExchangeState("INITIAL", 0);

	public static final ExchangeState SUCCESS = new ExchangeState("SUCCESS", 1);

	public static final ExchangeState FAILED = new ExchangeState("FAILED", 1);

	public static final ExchangeState TIMEOUT = new ExchangeState("TIMEOUT", 1);

	public static final ExchangeState CANCEL = new ExchangeState("CANCEL", 1);

	public static final ExchangeState[] ALL = { SUCCESS, FAILED, TIMEOUT, CANCEL };

	public ExchangeState(String state, int level) {
		super(state, level);
	}

}

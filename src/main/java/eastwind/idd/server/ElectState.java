package eastwind.idd.server;

import eastwind.idd.support.State;

public class ElectState extends State {

	public static final ElectState INITIAL = new ElectState("INITIAL", 0);

	public static final ElectState ELECTED = new ElectState("ELECTED", 1);
	
	public static final ElectState ABORT = new ElectState("ABORT", 1);

	public ElectState(String state, int level) {
		super(state, level);
	}

}

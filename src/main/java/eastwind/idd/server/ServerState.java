package eastwind.idd.server;

import eastwind.idd.support.State;

/**
 * Created by jan.huang on 2018/5/3.
 */
public class ServerState extends State {

	public static final ServerState INITIAL = new ServerState("INITIAL", 0);

	public static final ServerState ONLINE = new ServerState("ONLINE", 1);

	// subjective offline
	public static final ServerState OFFLINE1 = new ServerState("OFFLINE1", 2);

	// objective offline
	public static final ServerState OFFLINE2 = new ServerState("OFFLINE2", 3);

	public ServerState(String state, int level) {
		super(state, level);
	}

}

package eastwind.idd.channel;

import eastwind.idd.support.State;

public class ChannelState extends State {

	public static final ChannelState INITIAL = new ChannelState("INITIAL", 0);

	public static final ChannelState ACTIVE = new ChannelState("ACTIVE", 1);

	public static final ChannelState INACTIVE = new ChannelState("INACTIVE", 3);

	public static final ChannelState SHAKED = new ChannelState("SHAKED", 2);

	public static final ChannelState SHAK_FAILED = new ChannelState("SHAK_FAILED", 2);

	public static final ChannelState CLOSED = new ChannelState("CLOSED", 4);

	public ChannelState(String state, int level) {
		super(state, level);
	}

}

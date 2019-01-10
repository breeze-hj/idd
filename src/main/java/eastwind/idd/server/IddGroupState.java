package eastwind.idd.server;

import eastwind.idd.support.State;

public class IddGroupState extends State {

	public static final IddGroupState INITIAL = new IddGroupState("INITIAL", 0);
	
    public static final IddGroupState UNSERVICEABLE = new IddGroupState("UNSERVICEABLE", 1);
    
    public static final IddGroupState SERVICEABLE = new IddGroupState("SERVICEABLE", 1);
	
	public IddGroupState(String state, int level) {
		super(state, level);
	}

}

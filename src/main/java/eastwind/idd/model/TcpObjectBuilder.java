package eastwind.idd.model;

import eastwind.idd.support.MillisX10Sequence;
import eastwind.idd.support.LongSequence;

public class TcpObjectBuilder {

	private static LongSequence SEQUENCER = new MillisX10Sequence();
	
	public static TcpObject newTcpObject() {
		TcpObject tcpObject = new TcpObject();
		tcpObject.id = SEQUENCER.get();
		return tcpObject;
	}
	
}

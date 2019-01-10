package eastwind.idd.model;

import eastwind.idd.idd.Sequence;

public class IddRequest {

	public long term;
	public int requestType;
	// submit, sync
	public int executeType;
	public Sequence sequence;

	public IddRequest toSyncRequest(boolean cloneSequence) {
		IddRequest iddRequest = new IddRequest();
		iddRequest.term = this.term;
		iddRequest.requestType = this.requestType;
		iddRequest.executeType = 2;
		if (cloneSequence) {
			iddRequest.sequence = new Sequence();
			iddRequest.sequence.setLogId(sequence.getLogId());
			iddRequest.sequence.setNextVal(sequence.getNextVal());
			iddRequest.sequence.setVersion(sequence.getVersion());
			iddRequest.sequence.setName(sequence.getName());
		}
		return iddRequest;
	}

}

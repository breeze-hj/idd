package eastwind.idd.idd;

import eastwind.idd.data.DataService;
import eastwind.idd.model.IddRequest;
import eastwind.idd.support.Result;

public class DataServiceAdapter {

	private DataService dataService;

	public DataServiceAdapter(DataService dataService) {
		this.dataService = dataService;
	}

	public synchronized Result preExecute(IddRequest iddRequest) {
		int type = iddRequest.requestType;
		Sequence current = iddRequest.sequence;
		long logId = dataService.incrementLogId();
		current.setLogId(logId);
		Sequence exist = dataService.getSequence(current.getName());
		if (type == 1) {
			if (exist != null) {
				return Result.fail("duplicate sequence: " + current.getName() + ".");
			}
			current.setVersion(logId);
			current.setNextVal(1);
			dataService.createSequence(current);
			IddRequest syncRequest = iddRequest.toSyncRequest(true);
			return Result.success(syncRequest);
		} else if (type == 2) {
			if (exist == null) {
				return Result.fail("sequence not found: " + current.getName() + ".");
			}
			current.setNextVal(exist.getNextVal());
			current.setVersion(exist.getVersion());
			exist.setNextVal(exist.getNextVal() + 1);
			exist.setLogId(logId);
			dataService.updateSequence(exist);
			IddRequest syncRequest = iddRequest.toSyncRequest(false);
			syncRequest.sequence = exist;
			return Result.success(syncRequest);
		}
		return null;
	}
	
	public Result execute(IddRequest iddRequest) {
		int type = iddRequest.requestType;
		Sequence sequence = iddRequest.sequence;
		// create
		if (type == 1) {
			dataService.setLogId(sequence.getLogId());
			dataService.createSequence(sequence);
			return Result.success(sequence);
			// new sequence
		} else if (type == 2) {
			dataService.setLogId(sequence.getLogId());
			dataService.updateSequence(sequence);
			return Result.success(sequence);
		}
		return Result.success(1);
	}

}

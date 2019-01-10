package eastwind.idd.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import eastwind.idd.idd.Sequence;

public class MemoryDataService implements DataService {

	private long term;
	private long logId;
	private Map<String, Sequence> sequences = new HashMap<>();
	
	@Override
	public long getCurrentTerm() {
		return term;
	}

	@Override
	public void setCurrentTerm(long term) {
		this.term = term;
	}

	@Override
	public long getLogId() {
		return logId;
	}

	@Override
	public long setLogId(long logId) {
		this.logId = logId;
		return logId;
	}

	@Override
	public Sequence getSequence(String name) {
		return sequences.get(name);
	}

	@Override
	public void createSequence(Sequence sequence) {
		sequences.put(sequence.getName(), sequence);
	}

	@Override
	public void updateSequence(Sequence sequence) {
		Sequence exist = getSequence(sequence.getName());
		exist.setLogId(sequence.getLogId());
		exist.setNextVal(sequence.getNextVal());
	}

	@Override
	public Iterator<Sequence> getAll() {
		return sequences.values().iterator();
	}

}

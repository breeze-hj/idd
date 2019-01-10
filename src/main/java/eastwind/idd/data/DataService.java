package eastwind.idd.data;

import java.util.Iterator;

import eastwind.idd.idd.Sequence;

public interface DataService {

	long getCurrentTerm();
	
	void setCurrentTerm(long term);
	
	default long incrementTerm() {
		long term = getCurrentTerm() + 1;
		setCurrentTerm(term);
		return term;
	}
	
	long getLogId();
	
	long setLogId(long logId);
	
	default long incrementLogId() {
		long logId = getLogId() + 1;
		setLogId(logId);
		return logId;
	}
	
	Sequence getSequence(String name);
	
	void createSequence(Sequence sequence);
	
	void updateSequence(Sequence sequence);
	
	Iterator<Sequence> getAll();
}

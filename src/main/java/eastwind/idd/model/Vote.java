package eastwind.idd.model;

public class Vote {

	public static final int PRE_VOTE = 1;
	public static final int FOR_VOTE = 2;
	public static final int VOTE = 3;
	public static final int LEADER = 4;
	
	public static final int AGREE = 8;
	public static final int OPPOSE = 9;
	public static final int PENDING = 0;

	public int type;
	public long term;
	public long logId;
	public String target;

	public static Vote preVote(long term, long logId) {
		Vote vote = new Vote();
		vote.type = PRE_VOTE;
		vote.term = term;
		vote.logId = logId;
		return vote;
	}
	
	public static Vote forVote(long term, long logId) {
		Vote vote = new Vote();
		vote.type = FOR_VOTE;
		vote.term = term;
		vote.logId = logId;
		return vote;
	}
	
	public static Vote vote(String target) {
		Vote vote = new Vote();
		vote.type = VOTE;
		vote.target = target;
		return vote;
	}
	
	public static Vote leader(long term, long logId) {
		Vote vote = new Vote();
		vote.type = LEADER;
		vote.term = term;
		vote.logId = logId;
		return vote;
	}
	
	public static Vote agree() {
		Vote vote = new Vote();
		vote.type = AGREE;
		return vote;
	}
	
	public static Vote oppose() {
		Vote vote = new Vote();
		vote.type = OPPOSE;
		return vote;
	}
	
	public static Vote pending() {
		Vote vote = new Vote();
		vote.type = PENDING;
		return vote;
	}
}

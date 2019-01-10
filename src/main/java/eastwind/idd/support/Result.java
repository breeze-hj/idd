package eastwind.idd.support;

public class Result {

	private static final byte SUCCESS = 1;
	private static final byte FAILED = 2;
	private static final byte CANCELED = 3;

	public byte state;
	public Object value;

	public static Result success(Object value) {
		Result result = new Result();
		result.state = SUCCESS;
		result.value = value;
		return result;
	}

	public static Result cancel(Object value) {
		Result result = new Result();
		result.state = CANCELED;
		result.value = value;
		return result;
	}
	
	public static Result fail(Object th) {
		Result result = new Result();
		result.state = FAILED;
		result.value = th;
		return result;
	}

	public boolean isCanceled() {
		return state == CANCELED;
	}

	public boolean isSuccess() {
		return state == SUCCESS;
	}

	public boolean isFailed() {
		return state == FAILED;
	}

	public Object getValue() {
		return value;
	}

}

package eastwind.idd.server;

import java.util.function.Consumer;

import eastwind.idd.support.StateFul;

public class ExchangeContext extends StateFul<ExchangeState> {

	public void success(Object result) {
		super.changeState(ExchangeState.SUCCESS, result);
	}

	public boolean isSuccess() {
		return getState() == ExchangeState.SUCCESS;
	}

	public void onSuccess(Consumer<Object> consumer) {
		super.onState(ExchangeState.SUCCESS, consumer);
	}

	public void fail(Object cause) {
		super.changeState(ExchangeState.FAILED, cause);
	}

	public boolean isFailed() {
		return getState() == ExchangeState.FAILED;
	}

	public void onFailed(Consumer<Object> consumer) {
		super.onState(ExchangeState.FAILED, consumer);
	}
}

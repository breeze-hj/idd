package eastwind.idd.idd;

import java.util.concurrent.CompletableFuture;

import eastwind.idd.model.IddRequest;
import eastwind.idd.server.ExchangeContext;
import eastwind.idd.server.IddGroup;
import eastwind.idd.server.IddServer;
import eastwind.idd.support.Result;

public class FollowerSubmitter implements IddRequestSubmitter {

	private IddGroup iddGroup;

	public FollowerSubmitter(IddGroup iddGroup) {
		this.iddGroup = iddGroup;
	}

	public CompletableFuture<Result> submit(IddRequest iddRequest) {
		CompletableFuture<Result> future = new CompletableFuture<>();
		IddServer leader = (IddServer) iddGroup.findLeader();
		ExchangeContext ec = leader.exchange(iddRequest, 1000);
		ec.onSuccess(o -> future.complete((Result) o));
		return future;
	}
}

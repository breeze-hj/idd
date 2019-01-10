package eastwind.idd.idd;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import eastwind.idd.model.IddRequest;
import eastwind.idd.server.ExchangeContext;
import eastwind.idd.server.GroupExchangeConsumer;
import eastwind.idd.server.IddGroup;
import eastwind.idd.support.Result;

public class LeaderSubmitter implements IddRequestSubmitter {

	private IddGroup iddGroup;
	private DataServiceAdapter dataServiceAdapter;

	public LeaderSubmitter(IddGroup iddGroup, DataServiceAdapter dataServiceAdapter) {
		this.iddGroup = iddGroup;
		this.dataServiceAdapter = dataServiceAdapter;
	}

	@Override
	public CompletableFuture<Result> submit(IddRequest iddRequest) {
		CompletableFuture<Result> future = new CompletableFuture<>();
		Result r = dataServiceAdapter.preExecute(iddRequest);
		if (r.isSuccess()) {
			IddRequest syncRequest = (IddRequest) r.value;
			GroupExchangeConsumer gec = iddGroup.exchange(syncRequest, 1000);
			gec.anyOf(t -> {
				if (gec.isHalfCompleted()) {
					List<ExchangeContext> l = gec.getCompleted();
					int count = (int) l.stream().filter(ec -> ((Result) ec.getResult()).isSuccess()).count();
					if (iddGroup.isGtThenHalf(count)) {
//						Result result = dataServiceAdapter.execute(iddRequest);
						future.complete(Result.success(iddRequest.sequence));
					}
				}
			});
		} else {
			future.complete(r);
		}
		return future;
	}
}

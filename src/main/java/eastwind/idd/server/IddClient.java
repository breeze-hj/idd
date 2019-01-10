package eastwind.idd.server;

import java.util.concurrent.CompletableFuture;

import eastwind.idd.idd.IddRequestSubmitterFactory;
import eastwind.idd.idd.Sequence;
import eastwind.idd.model.IddRequest;
import eastwind.idd.support.Result;
import eastwind.idd.th.IddException;

public class IddClient {

	private IddRequestFactory iddRequestFactory;
	private IddGroup iddGroup;
	private IddRequestSubmitterFactory iddRequestSubmitterFactory;

	public IddClient(BootstrapServer bootstrapServer) {
		this.iddRequestFactory = new IddRequestFactory(bootstrapServer);
		this.iddGroup = bootstrapServer.getIddGroup();
		this.iddRequestSubmitterFactory = new IddRequestSubmitterFactory(bootstrapServer);
	}

	public CompletableFuture<Sequence> create(String name) {
		CompletableFuture<Sequence> finalCf = new CompletableFuture<Sequence>();
		if (iddGroup.getState() == IddGroupState.UNSERVICEABLE) {
			finalCf.completeExceptionally(new IddException("idd service is unavailable"));
			return finalCf;
		}
		IddRequest iddRequest = iddRequestFactory.newIddRequest(1, name);
		submit(finalCf, iddRequest);
		return finalCf;
	}
	
	public CompletableFuture<Sequence> next(String name) {
		CompletableFuture<Sequence> finalCf = new CompletableFuture<Sequence>();
		if (iddGroup.getState() == IddGroupState.UNSERVICEABLE) {
			finalCf.completeExceptionally(new IddException("idd service is unavailable"));
			return finalCf;
		}
		IddRequest iddRequest = iddRequestFactory.newIddRequest(2, name);
		submit(finalCf, iddRequest);
		return finalCf;
	}

	private void submit(CompletableFuture<Sequence> finalCf, IddRequest iddRequest) {
		if (iddGroup.isServiceable()) {
			doSubmit(finalCf, iddRequest);
		} else {
			iddGroup.onStateOnce(v -> doSubmit(finalCf, iddRequest), IddGroupState.SERVICEABLE);
			iddGroup.onStateOnce(v -> finalCf.completeExceptionally(new IddException("idd service is unavailable")),
					IddGroupState.UNSERVICEABLE);
		}
	}

	private void doSubmit(CompletableFuture<Sequence> finalCf, IddRequest iddRequest) {
		CompletableFuture<Result> submitCF = iddRequestSubmitterFactory.newSubmitter().submit(iddRequest);
		submitCF.thenAccept(r -> {
			if (r.isSuccess()) {
				finalCf.complete((Sequence) r.value);
			} else if (r.isFailed()) {
				finalCf.completeExceptionally(new IddException((String) r.value));
			} else if (r.isCanceled()) {
				// TODO
			}
		});
	}

}

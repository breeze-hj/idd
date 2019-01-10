package eastwind.idd.idd;

import java.util.concurrent.CompletableFuture;

import eastwind.idd.model.IddRequest;
import eastwind.idd.support.Result;

public interface IddRequestSubmitter {

	CompletableFuture<Result> submit(IddRequest iddRequest);
	
}

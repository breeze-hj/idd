package eastwind.idd.test;

import java.util.concurrent.CompletableFuture;

import eastwind.idd.IddApplication;
import eastwind.idd.idd.Sequence;
import eastwind.idd.server.IddClient;

public class TestIdd {

	public static void main(String[] args) {
		String[] addresses = { ":18727", ":18728", ":18729" };
		String allAddressesStr = String.join(",", addresses);

		IddClient[] iddClients = new IddClient[addresses.length];
		for (int i = 0; i < addresses.length; i++) {
			iddClients[i] = IddApplication.create(addresses[i], allAddressesStr).getIddClient();
		}

		// create
		CompletableFuture<Sequence> cf = iddClients[0].create("user");
		cf.thenAccept(s -> System.out.println("create sequence: " + s.getName() + "."));
		cf.join();

		// create if exist
		cf = iddClients[1].create("user");
		cf.exceptionally(th -> {
			System.err.println(th.getMessage());
			return null;
		});
		
		// get id
		for (int i = 0; i < 10; i++) {
			iddClients[i % iddClients.length].next("user")
					.thenAccept(s -> System.out.println("new id: " + s.getNextVal())).join();
		}
	}

}

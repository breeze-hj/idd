package eastwind.idd.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eastwind.idd.data.DataService;
import eastwind.idd.idd.Sequence;
import eastwind.idd.server.BootstrapServer;
import eastwind.idd.server.BootstrapServiceable;
import eastwind.idd.server.IddClient;
import eastwind.idd.server.Role;
import eastwind.idd.server.Server;
import eastwind.idd.support.IddUtils;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpRequestDispatcherFactory extends BootstrapServiceable {

	public HttpRequestDispatcherFactory(BootstrapServer bootstrapServer) {
		super(bootstrapServer);
	}

	public HttpRequestDispatcher newHttpRequestDispather() {
		HttpRequestDispatcher dispatcher = new HttpRequestDispatcher();
		dispatcher.onPath("/", this::handelIndex);
		dispatcher.onPath("/favicon.ico", this::handelFavicon);
		dispatcher.onPath("/*", this::handleSequenceRequest);
		return dispatcher;
	}

	private Object handelFavicon(FullHttpRequest request) {
		return HttpResponseStatus.NOT_FOUND;
	}

	private Object handelIndex(FullHttpRequest request) {
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("address", bootstrapServer.getAddressStr());
		int role = bootstrapServer.getRole();
		if (role == Role.LEADER) {
			data.put("role", "LEADER");
		} else if (role == Role.FOLLOWER) {
			data.put("role", "FOLLOWER");
			Server leader = bootstrapServer.getIddGroup().findLeader();
			data.put("leader", leader.getAddressStr());
		} else {
			data.put("role", "UNDEFINED");
		}
		data.put("servers", bootstrapServer.getAllAddressesStr());
		data.put("startAt", bootstrapServer.getStartTime());

		data.put("currentTerm", bootstrapServer.getCurrentTerm());
		data.put("logId", bootstrapServer.getLogId());

		data.put("sequences", bootstrapServer.getDataService().getAll());
		return data;
	}
	
	public Object handleSequenceRequest(FullHttpRequest request) {
		try {
			URI uri = new URI(request.uri());
			String path = uri.getPath();
			List<String> paths = IddUtils.splitPath(path);
			String name = paths.get(0);
			
			HttpMethod httpMethod = request.method();
			if (httpMethod == HttpMethod.GET) {
				DataService dataService = bootstrapServer.getDataService();
				Sequence sequence = dataService.getSequence(name);
				if (sequence == null) {
					return HttpResponseStatus.NOT_FOUND;
				}
				return sequence;
			}
			
			if (httpMethod == HttpMethod.POST) {
				IddClient iddClient = new IddClient(bootstrapServer);
				return iddClient.next(name);
			}
			
			if (httpMethod == HttpMethod.PUT) {
				IddClient iddClient = new IddClient(bootstrapServer);
				return iddClient.create(name);
			}
			
		} catch (URISyntaxException e) {
			return HttpResponseStatus.BAD_REQUEST;
		}
		return HttpResponseStatus.NOT_FOUND;
	}
}

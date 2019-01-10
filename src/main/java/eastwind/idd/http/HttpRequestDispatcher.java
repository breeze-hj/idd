package eastwind.idd.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

import eastwind.idd.support.IddUtils;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

public class HttpRequestDispatcher {

	private PatternNode root = new PatternNode(null);

	public void onPath(String path, Function<FullHttpRequest, Object> function, HttpMethod... httpMethods) {
		PatternNode node = findPatternNode(path, true);
		node.consumerNodes.add(new ConsumerNode(httpMethods, function));
	}

	public Function<FullHttpRequest, Object> map(FullHttpRequest httpRequest) throws URISyntaxException {
		URI uri = new URI(httpRequest.uri());
		String path = uri.getPath();
		PatternNode patternNode = findPatternNode(path, false);
		if (patternNode == null) {
			return null;
		}
		Function<FullHttpRequest, Object> function = null;
		for (ConsumerNode consumerNode : patternNode.consumerNodes) {
			if (consumerNode.httpMethods.size() == 0 || consumerNode.httpMethods.contains(httpRequest.method())) {
				function = consumerNode.function;
				break;
			}
		}
		return function;
	}

	private PatternNode findPatternNode(String path, boolean autoCreate) {
		List<String> l = IddUtils.splitPath(path);
		PatternNode node = root;
		for (String pattern : l) {
			PatternNode next = null;
			for (PatternNode n : node.childrens) {
				if (n.pattern.equals("*") || n.pattern.equals(pattern)) {
					next = n;
					break;
				}
			}
			if (next == null) {
				if (autoCreate) {
					next = new PatternNode(pattern);
					node.childrens.add(next);
				} else {
					return null;
				}
			}
			node = next;
		}
		return node;
	}

	static class PatternNode {
		String pattern;
		List<ConsumerNode> consumerNodes = new ArrayList<>();
		List<PatternNode> childrens = new ArrayList<>();

		PatternNode(String pattern) {
			this.pattern = pattern;
		}
	}

	static class ConsumerNode {
		List<HttpMethod> httpMethods;
		Function<FullHttpRequest, Object> function;

		public ConsumerNode(HttpMethod[] httpMethods, Function<FullHttpRequest, Object> function) {
			super();
			this.httpMethods = Lists.newArrayList(httpMethods);
			this.function = function;
		}
	}
}

package eastwind.idd.channel;

import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eastwind.idd.http.HttpRequestDispatcher;
import eastwind.idd.support.Result;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

public class HttpChannel extends AbstractChannel implements InetChannel<FullHttpRequest> {

	private HttpRequestDispatcher httpRequestDispatcher;
	private ObjectMapper objectMapper;

	public HttpChannel(Channel channel, HttpRequestDispatcher httpRequestDispatcher, ObjectMapper objectMapper) {
		super.channel = channel;
		this.httpRequestDispatcher = httpRequestDispatcher;
		this.objectMapper = objectMapper;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void recv(FullHttpRequest fullHttpRequest) {
		try {
			Function<FullHttpRequest, Object> function = httpRequestDispatcher.map(fullHttpRequest);
			if (function != null) {
				try {
					Object r = function.apply(fullHttpRequest);
					if (r instanceof CompletableFuture) {
						CompletableFuture cf = (CompletableFuture) r;
						cf.thenAccept(t -> send(t));
						cf.exceptionally(th -> {
							send(th);
							return null;
						});
					} else {
						send(r);
					}
				} catch (Exception e) {
					send(e);
				}
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void send(Object response) {
		try {
			send0(response);
		} catch (Exception e) {
			try {
				send0(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			} catch (Exception e1) {
			}
		}
	}

	private void send0(Object response) throws Exception {
		FullHttpResponse httpResponse;
		if (response instanceof HttpResponseStatus) {
			HttpResponseStatus status = (HttpResponseStatus) response;
			ByteBuf buf = toBuf(status.toString());
			httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf);
			HttpUtil.setContentLength(httpResponse, buf.readableBytes());
		} else if (response instanceof Result) {
			Result r = (Result) response;
			HttpResponseStatus status = null;
			if (r.isSuccess()) {
				status = HttpResponseStatus.OK;
			} else if (r.isFailed()) {
				status = HttpResponseStatus.BAD_REQUEST;
			}
			if (r.value == null) {
				httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
				HttpUtil.setContentLength(httpResponse, 0);
			} else {
				ByteBuf buf = toBuf(r.value);
				httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
				HttpUtil.setContentLength(httpResponse, buf.readableBytes());
			}
		} else if (response instanceof Throwable) {
			Throwable th = (Throwable) response;
			ByteBuf buf = toBuf(th.getMessage());
			httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, buf);
			HttpUtil.setContentLength(httpResponse, buf.readableBytes());
		} else {
			ByteBuf buf = toBuf(response);
			httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
			HttpUtil.setContentLength(httpResponse, buf.readableBytes());
		}
		getNettyChannel().writeAndFlush(httpResponse);
	}

	private ByteBuf toBuf(Object data) throws JsonProcessingException {
		if (data instanceof String) {
			return Unpooled.copiedBuffer((String) data, Charset.forName("utf-8"));
		} else {
			byte[] b = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(data);
			ByteBuf buf = Unpooled.copiedBuffer(b);
			return buf;
		}
	}
}

package eastwind.idd.channel;

import java.util.HashMap;
import java.util.Map;

import eastwind.idd.handler.Handler;
import eastwind.idd.support.IddUtils;


public class HandlerProvider {

	private Map<Class<?>, Handler<?>> handlers = new HashMap<>();

	public Map<Class<?>, Handler<?>> getHandlers() {
		return handlers;
	}

	public synchronized <T> void register(Handler<T> handler) {
		Class<?> cls = IddUtils.getInterfaceTypeArgument0(handler.getClass(), Handler.class);
		handlers.put(cls, handler);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> Handler<T> getHandler(Class<T> cls) {
		return (Handler<T>) handlers.get(cls);
	}
}

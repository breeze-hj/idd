package eastwind.idd.support;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class IddUtils {

	public static Class<?> getInterfaceTypeArgument0(Class<?> cls, Class<?> inter) {
		Type[] types = cls.getGenericInterfaces();
		for (Type t : types) {
			if (t instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) t;
				if (pt.getRawType().equals(inter)) {
					return (Class<?>) pt.getActualTypeArguments()[0];
				}
			}
		}
		return null;
	}

	public static InetSocketAddress parseAddress(String addressStr) {
		List<String> l = Splitter.on(":").trimResults().omitEmptyStrings().splitToList(addressStr);
		if (l.size() == 0) {
			return null;
		} else if (l.size() == 1) {
			return new InetSocketAddress(Integer.parseInt(l.get(0)));
		} else {
			return new InetSocketAddress(l.get(0), Integer.parseInt(l.get(1)));
		}
	}

	public static List<String> splitPath(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return Lists.newArrayList(Splitter.on("/").trimResults().split(path));
	}
}

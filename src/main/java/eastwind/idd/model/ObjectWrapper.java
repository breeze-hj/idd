package eastwind.idd.model;

public class ObjectWrapper {

	public byte args;
	public Long respondTo;
	public Object data;
	
	public static ObjectWrapper wrap(Long respondTo, Object data) {
		ObjectWrapper wrapper = new ObjectWrapper();
		wrapper.respondTo = respondTo;
		wrapper.args = 1;
		wrapper.data = data;
		return wrapper;
	}
	
}

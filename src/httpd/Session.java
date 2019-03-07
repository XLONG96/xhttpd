package httpd;

import java.util.HashMap;

public class Session {
	private HashMap<String, Object> attributes;
	
	public Session() {
		attributes = new HashMap<>();
	}
	
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}
	
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
}

package httpd;

import java.util.HashMap;

public class HttpRequest {
	private HashMap<String, String> headers;
	private String uri;
	private HashMap<String, String> parameters;
	private String contentType;
	private Session session;
	private String sessionId;
	private boolean firstVisit = false;
	
	public boolean isFirstVisit() {
		return firstVisit;
	}
	
	public void setFirstVisit(boolean firstVisit) {
		this.firstVisit = firstVisit;
	}
	
	public void appendParameters(HashMap<String, String> parameters) {
		if (this.parameters == null) {
			this.parameters = parameters;
		} else {
			parameters.putAll(parameters);
		}
	}
	
	public String getParamter(String name) {
		return parameters.get(name);
	}
	
	public HashMap<String, String> getHeaders() {
		return headers;
	}
	
	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}
	
	public String getUri() {
		return uri;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public HashMap<String, String> getParameters() {
		return parameters;
	}
	
	public void setParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
}
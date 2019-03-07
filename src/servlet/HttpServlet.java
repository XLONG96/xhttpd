package servlet;

import httpd.HttpRequest;
import httpd.HttpResponse;

public abstract class HttpServlet {
	private String uri;
	
	public HttpServlet(String uri) {
		this.uri = uri;
	}
	
	public void doGet(HttpRequest request, HttpResponse response) {
		doService(request, response);
	}
	
	public void doPost(HttpRequest request, HttpResponse response) {
		doService(request, response);
	}
	
	public abstract void doService(HttpRequest request, HttpResponse response);

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	
}

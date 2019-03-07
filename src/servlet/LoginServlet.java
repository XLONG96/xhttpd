package servlet;

import httpd.HttpRequest;
import httpd.HttpResponse;
import httpd.Session;

public class LoginServlet extends HttpServlet {
	final static String DEFAULT_USERNAME = "admin";
	final static String DEFAULT_PASSWORD = "123";
	final static String DEFAULT_LOGIN_PAGE = "/login.html";
	
	public LoginServlet(String uri) {
		super(uri);
	}

	@Override
	public void doGet(HttpRequest request, HttpResponse response) {
		doFilter(request, response);
	}
	
	@Override
	public void doPost(HttpRequest request, HttpResponse response) {
		doService(request, response);
	}
	
	public void doFilter(HttpRequest request, HttpResponse response) {
		Session session = request.getSession();
		String username = (String)session.getAttribute("username");
		String passwd = (String)session.getAttribute("passwd");
		
		if (!request.getUri().equals(DEFAULT_LOGIN_PAGE)) {
			if (username != null) {
				if (username.equals(DEFAULT_USERNAME) && passwd != null 
						&& passwd.equals(DEFAULT_PASSWORD)) {
					return ;
				} 
			} else {
				// ��ת����¼ҳ
				response.sendRedirect("login.html");
			}
		}
	}
	
	@Override
	public void doService(HttpRequest request, HttpResponse response) {
		Session session = request.getSession();
		String username = (String)request.getParamter("username");
		String passwd = (String)request.getParamter("password");
		
		if (username != null) {
			if (username.equals(DEFAULT_USERNAME) && passwd != null 
					&& passwd.equals(DEFAULT_PASSWORD)) {
				session.setAttribute("username", username);
				session.setAttribute("password", passwd);
				// ��ת����ҳ
				response.sendRedirect("index.html");
			}  else {
				// ��ת����¼����ҳ
				response.sendRedirect("error.html");
			}
		} else {
			// ��ת����¼����ҳ
			response.sendRedirect("error.html");
		}
	}
}

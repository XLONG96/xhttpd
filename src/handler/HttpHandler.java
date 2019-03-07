package handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import httpd.HttpRequest;
import httpd.HttpResponse;
import httpd.HttpStatusCoder;
import httpd.Response;
import servlet.LoginServlet;

public class HttpHandler implements Handler {
	private static final Log logger = LogFactory.getLog(HttpHandler.class);
	
	static final String SESSION_COOKIE_NAME = "XSESSID"; 
	static final int MAX_CONTENT_LENGTH = 1024 * 1024;
	
	static final String DEFAULT_WEB_ROOT = HttpHandler.class.getResource("/").getPath();
	
	private String webRoot = DEFAULT_WEB_ROOT;
	
	private SessionManager sessionManager;
	
	private LoginServlet servlet;
	
	public HttpHandler(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
		this.servlet = new LoginServlet("/login");
	}
	
	public Response execute(ByteBuffer in) {
		String request = new String(in.array());
		logger.info("\n" + request);
		return requestHandler(request);
	}
	
	public HttpResponse requestHandler(String request) {
		
		try {
			StringReader reader = new StringReader(request);
			BufferedReader breader = new BufferedReader(reader);
			HashMap<String, String> headerList = new HashMap<>(); 
			HashMap<String, String> paramList = new HashMap<>(); 
			String req, method, uri, header, value, param, paramVal;
			String[] firstLine, postParamLine, paramLine;
			HttpRequest httpRequest = new HttpRequest();
			
			req = breader.readLine();
			
			if (req != null) {
				// 获取方法和请求路径
				firstLine = req.split(" ");
				if (firstLine == null || firstLine.length != 3) {
					return errorHandler(HttpStatusCoder.BAD_REQUEST);
				}
				method = firstLine[0];
				uri = firstLine[1];
				
				// 请求解码
				uri = URLDecoder.decode(uri, "UTF-8");
				
				// 处理头部
				req = breader.readLine();
				while (req != null && !req.equals("")) {
					
					int index = req.indexOf(':');
					if (index != -1) {
						header = req.substring(0, index);
						value = req.substring(index + 1, req.length());
						headerList.put(header, value);
					} else {
						return errorHandler(HttpStatusCoder.BAD_REQUEST); 
					}
					
					req = breader.readLine();
				}
				
				// 获取POST的请求参数
				// application/x-www-form-urlencoded
				// title=value&title=value...
				req = breader.readLine();
				postParamLine = req.split("&");
				if (postParamLine != null)
				for (String pair : postParamLine) {
					paramLine = pair.split("=");
					
					if (paramLine == null || paramLine.length != 2) {
						break;
					} else {
						param = paramLine[0].trim();
						paramVal = paramLine[1].trim();
						paramList.put(param, paramVal);
					}
					
					req = breader.readLine();
				}
				
				// 获取cookie
				String cookie = headerList.get("Cookie");
				String sessionId;
				if (cookie != null) {
					sessionId = cookie.split("=")[1];
				} else {
					sessionId = sessionManager.createNewSession();
					httpRequest.setFirstVisit(true);
				}
				
				// 获取请求资源类型
				String contentType = getMIMEType(uri);
				
				// 封装 HTTP request 对象
				httpRequest.setContentType(contentType);
				httpRequest.setSessionId(sessionId);
				httpRequest.setSession(sessionManager.getSessionById(sessionId));
				httpRequest.setUri(uri);
				httpRequest.setHeaders(headerList);
				httpRequest.setParameters(paramList);

			} else {
				return errorHandler(HttpStatusCoder.BAD_REQUEST);
			}

			//headerList.forEach((key, val) -> System.out.println(key + " " + val));
			paramList.forEach((key, val) -> logger.info("Post key-val:" + key + ":" + val));
			
			HttpResponse httpResponse = new HttpResponse();
			// 根据方法进行相应处理
			switch (method) {
				case "GET" : return doGet(httpRequest, httpResponse);
				case "POST" : return doPost(httpRequest, httpResponse);
				case "HEAD" : return doHead(httpResponse);
				default : return errorHandler(HttpStatusCoder.BAD_REQUEST);
			}	
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return errorHandler(HttpStatusCoder.BAD_REQUEST);
		
	}

	public HttpResponse errorHandler(HttpStatusCoder statusCode) {
		// 自定义错误页面
		HttpResponse response = new HttpResponse();
		response.setStatusCode(statusCode);
		if (statusCode == HttpStatusCoder.NOT_FOUND) {
			response.setData(getServerSource("404.html"));
		}
		return response;
	}

	public HttpResponse doGet(HttpRequest request, HttpResponse response) {
		HashMap<String, String> paramList = new HashMap<>(); 
		String param, paramVal;
		String[] UriLine, reqParams, params;
		String uri = request.getUri();
		
		// 解析url中的参数
		UriLine = uri.split("\\?");
		if (UriLine != null && UriLine.length == 2) {
			uri = UriLine[0];
			request.setUri(uri);
			reqParams = UriLine[1].split("&");
			for (int i = 0; i < reqParams.length; i++) {
				params = reqParams[i].split("=");
				if (params == null || params.length != 2) {
					return errorHandler(HttpStatusCoder.BAD_REQUEST);
				} else {
					param = params[0].trim();
					paramVal = params[1].trim();
					paramList.put(param, paramVal);
				}
			}
			
			paramList.forEach((key, val) -> logger.info("Get key-val:" + key + ":" + val));
			request.appendParameters(paramList);
		}
		
		if (request.getContentType().equals("text/plain")
				|| request.getContentType().equals("text/html")) {
			servlet.doGet(request, response);
		}
		
		return response(request, response);
	}
	
	public HttpResponse doPost(HttpRequest request, HttpResponse response) {
		servlet.doPost(request, response);
		return response(request, response);
	}
	
	public HttpResponse doHead(HttpResponse response) {
		response.setStatusCode(HttpStatusCoder.SUCCESS);
		return response;
	}
	
	public HttpResponse response(HttpRequest request, HttpResponse response) {
		byte[] data;
		String contentType = "text/html";
		String uri = request.getUri();
		
		if (response.getStatusCode() == HttpStatusCoder.FOUND) {
			return response;
		}
		
		if (uri.equals("/")) {
			data = getServerSource("index.html");
			if (data != null) {
				response.addHeader("Content-Type", contentType);
				//headers.put("Content-length", data.length+"");
				response.setStatusCode(HttpStatusCoder.SUCCESS);
				response.setData(data);
			} else {
				return errorHandler(HttpStatusCoder.NOT_FOUND);
			}
			
		} else {
			data = getServerSource(uri);
			
			if (data != null) {
				contentType = request.getContentType();
				response.addHeader("Content-Type", contentType);
				//headers.put("Content-length", data.length+"");
				response.setStatusCode(HttpStatusCoder.SUCCESS);
				response.setData(data);
			} else {
				return errorHandler(HttpStatusCoder.NOT_FOUND);
			}
		}
		
		// 如果是第一次访问则设置本地cookie
		if (request.isFirstVisit()) {
			response.addHeader("Set-Cookie", SESSION_COOKIE_NAME + "=" 
					+ request.getSessionId());
		}
		
		return response;
	}
	
	public String getMIMEType(String uri) {
		String contentType = "text/plain";
		
		if (uri.contains(".js")) {
			contentType = "application/x-javascript";
		} else if (uri.contains(".css")) {
			contentType = "text/css";
		} else if (uri.contains(".jpg")) {
			contentType = "image/jpeg";
		} else if (uri.contains(".png")) {
			contentType = "image/png";
		} else if (uri.contains("html") || uri.contains("htm")) {
			contentType = "text/html";
		}
		
		return contentType;
	}

	
	public byte[] getServerSource(String uri) {
		logger.info(webRoot + uri);
		File file = new File(webRoot + uri);
		byte[] content;
		
		if (file.exists()) {
			try {
				FileInputStream input = new FileInputStream(file);
				content = new byte[(int)file.length()];
				input.read(content);
				input.close();
				return content;
				
			} catch (FileNotFoundException e) {
				logger.error("File can not found");
				e.printStackTrace();
			} catch (IOException e) {
				logger.error("File can not read");
				e.printStackTrace();
			} 
		}
		
		return null;
	}
	
	public void setWebRoot(String webRoot) {
		this.webRoot = webRoot;
	}
	
}

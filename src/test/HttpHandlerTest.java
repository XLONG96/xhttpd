package test;

import handler.HttpHandler;
import handler.SessionManager;

public class HttpHandlerTest {

	public static void main(String[] args) throws InterruptedException {
		SessionManager sessionManager = new SessionManager();
		HttpHandler handler = new HttpHandler(sessionManager);
		String request = 
		 "GET /index.html?name=get HTTP/1.1\n"
		+"Host: 127.0.0.1:8888\n"
		+"Connection: keep-alive\n"
		+"Cache-Control: max-age=0\n"
		+"Upgrade-Insecure-Requests: 1\n"
		+"User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36\n"
		+"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\n"
		+"Accept-Encoding: gzip, deflate, br\n"
		+"Accept-Language: zh-CN,zh;q=0.9\n"
		+"Referer: http://127.0.0.1:8888/JGO.html\n"
		+"\n"
		+"username=admin&password=xl107350\n";
		//+"name=lang\r\n"
		//+"password=123\r\n";
		System.out.println("data:");
		byte[] data = handler.requestHandler(request).getResponse();
		System.out.println(new String(data, 0, data.length));
	}
}

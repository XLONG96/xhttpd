package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import handler.HttpHandler;
import handler.SessionManager;

public class Bootstrap {
	private static final Log logger = LogFactory.getLog(Bootstrap.class);
	
	static final int PORT = 8080;
	
	public static void main(String[] args) throws IOException {
		SessionManager sessionManager = new SessionManager();
		HttpHandler handler = new HttpHandler(sessionManager);
		EventLoop server = new EventLoop(handler, PORT);
		server.init();
		server.start();
	}

}

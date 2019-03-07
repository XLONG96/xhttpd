package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import handler.Handler;
import handler.HttpHandler;
import httpd.Response;

public class EventLoop {
	private static final Log logger = LogFactory.getLog(EventLoop.class);
	
	final static int MAX_WORKING_THREAD = 8;
	
	// in bytes
	final static int BUFFER_SIZE = 1024;
	
	private int port = 8080;
	
	// for accept connecting
	private Selector mainSelector;
	
	// for read event and other
	private Selector subSelector;
	
	private Handler handler;
	
	private HashSet<SocketChannel> workingChannel;
	
	private ExecutorService threadPool;
	
	public EventLoop(Handler handler, int port) {
		this.handler = handler;
		this.port = port;
	}
	
	public void init() throws IOException {
		mainSelector = Selector.open();
		subSelector = Selector.open();
		ServerSocketChannel  ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		InetSocketAddress address = new InetSocketAddress(port);
		ssc.socket().bind(address);
		// 注册accept事件
		ssc.register(mainSelector, SelectionKey.OP_ACCEPT);
		workingChannel = new HashSet<>();
		threadPool = Executors.newFixedThreadPool(MAX_WORKING_THREAD);
	}
	
	private class InboundHandler implements Runnable {
		private SocketChannel sc;
		
		public InboundHandler(SocketChannel sc) {
			this.sc = sc;
		}
		
		@Override
		public void run() {
			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			
			try {
				int len = sc.read(buffer);
				
				if(len < 0){
				    close(sc);
				    return ;
				}
				
				// 回应请求
				Response response = handler.execute(buffer);
				ByteBuffer outBuffer = ByteBuffer.wrap(response.getResponse());
				sc.write(outBuffer);
				
				workingChannel.remove(sc);
				// 关闭连接
				close(sc);
				
			} catch (Exception e) {
				logger.error("Cilent closed the channel");
				e.printStackTrace();
				workingChannel.remove(sc);
			    close(sc);
			}
		}
	}
	
	public void accept(SelectionKey key) throws IOException {
		ServerSocketChannel nssc = (ServerSocketChannel)key.channel();
		
		SocketChannel nsc = nssc.accept();
		
		nsc.configureBlocking(false);
		nsc.register(subSelector, SelectionKey.OP_READ);
	}
	
	public void acceptRequest(SelectionKey key) {
		SocketChannel sc = (SocketChannel)key.channel();
		
		if (!workingChannel.add(sc)) {
			// 该通道正在处理连接请求，则直接返回
			return ;
		}
		
		threadPool.submit(new InboundHandler(sc));
	}
	
	public void close(SocketChannel sc) {
		try {
	    	sc.socket().close();
			sc.close();
		} catch (IOException e) {
			logger.error("Can't close cilent's channel");
			e.printStackTrace();
		}
	}
	
	private class SubSelectorListener implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					subSelector.selectNow();
				} catch (IOException e) {
					logger.error("Subselector select error");
					e.printStackTrace();
				}
				
				Set<SelectionKey> set = subSelector.selectedKeys();
				Iterator<SelectionKey> it = set.iterator();
				
				while (it.hasNext()) {
					
					SelectionKey sk = (SelectionKey)it.next();
					
					it.remove();
					
				    if (sk.isValid() && sk.isReadable()) {
						acceptRequest(sk);
					}
				}
			}
		}
	}
	
	public void start() throws IOException {
		logger.info(String.format("Server listening in port: %s", port));
		
		threadPool.submit(new SubSelectorListener());
		
		while (true) {
			mainSelector.selectNow();
			
			Set<SelectionKey> set = mainSelector.selectedKeys();
			Iterator<SelectionKey> it = set.iterator();
			
			while (it.hasNext()) {
				
				SelectionKey sk = (SelectionKey)it.next();
				
				it.remove();
				
			    if (sk.isValid() && sk.isAcceptable()) {
					accept(sk);
				} else if (sk.isValid() && sk.isReadable()) {
					acceptRequest(sk);
				}
			}
		}
	}
	
	public int getPort() {
		return port;
	}

	public void removeWorkingChannel(SocketChannel sc) {
		if (sc != null) {
			workingChannel.remove(sc);
		}
	}

}

package handler;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import httpd.Response;

public interface Handler {
	Response execute(ByteBuffer in);
}

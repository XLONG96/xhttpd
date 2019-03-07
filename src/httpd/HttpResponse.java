package httpd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import handler.HttpHandler;

public class HttpResponse implements Response {
	private static final Log logger = LogFactory.getLog(HttpResponse.class);
	
	
	static final int MAX_HEADER_LENGTH = 200;
	
	private HashMap<String, String> headers;
	
	private HttpStatusCoder statusCode;
	
	private byte[] data;
	
	public HashMap<String, String> getHeaders() {
		return headers;
	}
	
	public void addHeader(String name, String value) {
		if (headers == null) {
			headers = new HashMap<>();
		}
		headers.put(name, value);
	}
	
	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}
	
	public HttpStatusCoder getStatusCode() {
		return statusCode;
	}
	
	public void setStatusCode(HttpStatusCoder statusCode) {
		this.statusCode = statusCode;
	}
	
	public byte[] getData() {
		return data;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public byte[] getResponse() {
		if (headers == null) {
			//System.err.println("Error: Headers is Null"); 
			headers = new HashMap<>();
		}
		
		if (data != null) {
			// GZIP
			data = gzip(data);
			headers.put("Content-length", String.valueOf(data.length));
			headers.put("Content-Encoding", "gzip");
			headers.put("Set-Cookie", "XSESSID=123456789");
		}
		
		headers.put("Server", "XLang");
		
		StringBuffer buffer = new StringBuffer(MAX_HEADER_LENGTH);
		buffer.append("HTTP/1.0 " + this.statusCode.toString() + "\r\n");
		
		headers.forEach((param, value) -> {
			buffer.append(param + ":" + value + "\r\n");
		});
		buffer.append("\r\n");
		logger.info("Response header:\n" + buffer);
		
		if (data == null) {
			buffer.append("\r\n");
			return buffer.toString().getBytes();
		} else {
			return merge(buffer.toString().getBytes(), data);
		}
	}
	
	public byte[] merge(byte[] header, byte[] data) {
		byte[] merged = new byte[header.length + data.length];
		
		int off = 0;
		for (; off < header.length; off++) {
			merged[off] = header[off];
		}
		
		for (int i = 0; i < data.length; i++, off++) {
			merged[off] = data[i];
		}
		
		return merged;
	}
	
	public byte[] gzip(byte[] data) {
		
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(bos);
			
			gzip.write(data);
			gzip.finish();
			gzip.close();
			
			return bos.toByteArray();
		} catch (IOException e) {
			System.out.println("Error: GZIP Error");
			e.printStackTrace();
		}
		
		return data;
	}
	
	public void sendRedirect(String newUri) {
		// 302 found
		statusCode = HttpStatusCoder.FOUND;
		addHeader("Location", newUri);
	}
	
}

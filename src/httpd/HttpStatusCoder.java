package httpd;

public enum HttpStatusCoder implements StatusCoder{
	
	SUCCESS("200 OK"),
	FOUND("302 Found"),
	BAD_REQUEST("400 Bad Request"),
	NOT_FOUND("404 File Not Found"),
	SERVER_ERROR("500 Internal Server Error");
	
	public String code;
	
	private HttpStatusCoder(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return code;
	}
	
}

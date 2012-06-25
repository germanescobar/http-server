package net.gescobar.httpserver;

public enum HttpStatus {
	
	OK(200, "OK"),
	
	NO_CONTENT(204, "No Content");

	private int code;
	
	private String reason;
	
	private HttpStatus(int code, String reason) {
		this.code = code;
		this.reason = reason;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getReason() {
		return reason;
	}
	
}

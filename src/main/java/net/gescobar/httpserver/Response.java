package net.gescobar.httpserver;

/**
 * Exposes convenient methods to write the response back to the client. 
 * 
 * @author German Escobar
 */
public interface Response {
	
	public enum HttpStatus {
		
		OK(200, "OK"),
		CREATED(201, "Created"),
		ACCEPTED(202, "Accepted"),
		PARTIAL_INFO(203, "Partial Info"),
		NO_CONTENT(204, "No Content"),
		MOVED(301, "Moved Permanently"),
		FOUND(302, "Found"),
		SEE_OTHER(303, "See Other"),
		NOT_MODIFIED(304, "Not Modified"),
		BAD_REQUEST(400, "Bad Request"),
		UNAUTHORIZED(401, "Unauthorized"),
		FORBIDDEN(403, "Forbidden"),
		NOT_FOUND(404, "Not Found"),
		CONFLICT(409, "Conflict"),
		INTERNAL_ERROR(500, "Internal Error"),
		NOT_IMPLEMENTED(501, "Not Implemented"),
		OVERLOADED(502, "Overloaded"),
		GATEWAY_TIMEOUT(503, "Gateway Timeout");

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

	void status(HttpStatus status);
	
	void contentType(String contentType);
	
}

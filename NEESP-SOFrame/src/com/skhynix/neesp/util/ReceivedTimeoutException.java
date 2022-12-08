package com.skhynix.neesp.util;

public class ReceivedTimeoutException extends Throwable {
	
	static final long serialVersionUID = -3387516993124229948L;
			
	public ReceivedTimeoutException () {
		super();
	}

	public ReceivedTimeoutException (String message) {
		super(message);
	}

	public ReceivedTimeoutException (String message, Throwable cause) {
		super(message, cause);
	}
	
	public ReceivedTimeoutException (Throwable cause) {
		super(cause);
	}

	public ReceivedTimeoutException (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause);
	}	
}

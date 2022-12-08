package com.skhynix.neesp.util;

public class SOFrameException extends Throwable {
	
	static final long serialVersionUID = -3387516993124229949L;
			
	public SOFrameException () {
		super();
	}

	public SOFrameException (String message) {
		super(message);
	}

	public SOFrameException (String message, Throwable cause) {
		super(message, cause);
	}
	
	public SOFrameException (Throwable cause) {
		super(cause);
	}

	public SOFrameException (String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause);
	}	
}

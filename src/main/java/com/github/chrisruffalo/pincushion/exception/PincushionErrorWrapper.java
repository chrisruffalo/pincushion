package com.github.chrisruffalo.pincushion.exception;

/**
 * Simple class for response message wrapping as
 * a JSON type.
 * 
 * @author cruffalo
 *
 */
public class PincushionErrorWrapper {

	private String message;

	public PincushionErrorWrapper() {
		
	}
	
	public PincushionErrorWrapper(String message) {
		this();
		
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}

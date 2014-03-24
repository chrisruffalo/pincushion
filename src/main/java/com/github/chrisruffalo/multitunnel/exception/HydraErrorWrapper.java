package com.github.chrisruffalo.multitunnel.exception;

/**
 * Simple class for response message wrapping as
 * a JSON type.
 * 
 * @author cruffalo
 *
 */
public class HydraErrorWrapper {

	private String message;

	public HydraErrorWrapper() {
		
	}
	
	public HydraErrorWrapper(String message) {
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

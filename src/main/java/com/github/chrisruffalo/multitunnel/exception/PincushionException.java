package com.github.chrisruffalo.multitunnel.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.spi.NoLogWebApplicationException;

/**
 * Hydra exceptions extend the no log web application exception BECAUSE 
 * they should buble out correctly to the web layer
 * 
 * @author cruffalo
 *
 */
public class PincushionException extends NoLogWebApplicationException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public PincushionException(Throwable cause, String message) {
		super(cause, Response.status(Status.INTERNAL_SERVER_ERROR).entity(new PincushionErrorWrapper(message)).build());
	}
	
	public PincushionException(String message) {
		this(new RuntimeException(message), message);
	}
	
	
	
}

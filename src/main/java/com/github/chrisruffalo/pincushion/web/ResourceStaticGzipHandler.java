package com.github.chrisruffalo.pincushion.web;

import java.net.MalformedURLException;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceStaticGzipHandler extends ResourceHandler {

	private final Logger logger;
	
	public ResourceStaticGzipHandler() {
		super();
		
		this.logger = LoggerFactory.getLogger("gzip");
	}
	
    public Resource getResource(String path) throws MalformedURLException {
    	if(!path.toLowerCase().endsWith(".gz")) {
	    	final Resource resource = super.getResource(path + ".gz");
   	
	    	if(resource.exists()) {
				Resource original = super.getResource(path);
				// only return gzipped resource if the size
				// is smaller than the original resource
				if(original.length() > resource.length()) {	    		
	    			return resource;
				} else {
					return original;
				}
	    	}
    	}
    	return super.getResource(path);
    }
    
    @Override
    protected void doResponseHeaders(HttpServletResponse response, Resource resource, String mimeType) {
    	super.doResponseHeaders(response, resource, mimeType);

    	// un-bend response
    	if(response != null && "application/gzip".equalsIgnoreCase(response.getContentType()) || "application/gzip".equalsIgnoreCase(mimeType)) {
    		// un-bend
    		this.logger.trace("serving static pre-gzipped content");
    		
    		// find and guess the mime type from the path minus ".gz"
    		String path = resource.toString();
    		if(path.toLowerCase().endsWith(".gz")) {
    			path = path.substring(0, path.length() - 3);
    		}
    		String newMimeType = this.getMimeTypes().getMimeByExtension(path);
    		
    		// if the new mime type is valid, use it, otherwise it will null
    		// this value out and the browser will have to figure it out
    		// so it is a win-win either way
    		response.setContentType(newMimeType);
    		
    		// set content encoding as gzip
    		response.setHeader(HttpHeader.CONTENT_ENCODING.asString(), "gzip");    		
    	}
    }
    
	
}

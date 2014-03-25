package com.github.chrisruffalo.pincushion.web.rest.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.chrisruffalo.pincushion.util.ApplicationProperties;

@Path("/info")
public class InfoService {

	@Path("/version")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String version() {
		return ApplicationProperties.INSTANCE.version();
	}
	
}

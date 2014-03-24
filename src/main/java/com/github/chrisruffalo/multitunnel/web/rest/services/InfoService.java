package com.github.chrisruffalo.multitunnel.web.rest.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.chrisruffalo.multitunnel.util.ApplicationProperties;

@Path("/info")
public class InfoService {

	@Path("/version")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String version() {
		return ApplicationProperties.INSTANCE.version();
	}
	
}

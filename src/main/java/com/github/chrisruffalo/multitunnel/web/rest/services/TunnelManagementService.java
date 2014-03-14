package com.github.chrisruffalo.multitunnel.web.rest.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.github.chrisruffalo.multitunnel.model.TunnelConfiguration;
import com.github.chrisruffalo.multitunnel.tunnel.TunnelManager;

@Path("/tunnel")
public class TunnelManagementService {

	private final TunnelManager manager;
	
	public TunnelManagementService(@Context TunnelManager manager) {
		this.manager = manager;
	}
	
	@Path("/info")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<TunnelConfiguration> info() {
		return this.manager.info();
	}
	
	@Path("/{port}/remove")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public boolean removeTunnelByPort(@PathParam("port") int port) {
		this.manager.stop(port);
		return true;
	}
}

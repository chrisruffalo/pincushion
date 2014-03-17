package com.github.chrisruffalo.multitunnel.web.rest.services;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelReference;
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
	public List<TunnelReference> info() {
		return this.manager.info();
	}
	
	@Path("/{id}/remove")
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	public boolean removeTunnelById(@PathParam("id") String id) {
		this.manager.stop(id);
		return true;
	}
	
	@Path("/{id}/pause")
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public TunnelReference pauseTunnelById(@PathParam("id") String id) {
		return this.manager.pause(id);
	}
	
	@Path("/{id}/resume")
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public TunnelReference resumeTunnelById(@PathParam("id") String id) {
		return this.manager.resume(id);
	}
	
	@Path("/add")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public TunnelReference addTunnel(TunnelConfiguration configuration) {
		TunnelReference ref = this.manager.create(configuration);
		return ref;
	}
}

package com.github.chrisruffalo.pincushion.web.rest.services;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.github.chrisruffalo.pincushion.model.tunnel.TunnelBootstrap;
import com.github.chrisruffalo.pincushion.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.pincushion.model.tunnel.TunnelReference;
import com.github.chrisruffalo.pincushion.tunnel.TunnelManager;

@Path("/tunnel")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TunnelManagementService {

	private final TunnelManager manager;
	
	public TunnelManagementService(@Context TunnelManager manager) {
		this.manager = manager;
	}
	
	@Path("/info")
	@GET
	public List<TunnelReference> info() {
		return this.manager.info();
	}
	
	@Path("/bootstrap")
	@GET
	public TunnelBootstrap getTunnelBootstrap() {
		return this.manager.bootstrap();
	}
	
	@Path("/{id}")
	@GET
	public TunnelReference getTunnelById(@PathParam("id") String id) {
		return this.manager.get(id);
	}
	
	@Path("/{id}/bootstrap")
	@GET
	public TunnelBootstrap getTunnelBootstrapById(@PathParam("id") String id) {
		return this.manager.bootstrap(id);
	}
	
	@Path("/{id}/config")
	@GET
	public TunnelConfiguration getTunnelConfigById(@PathParam("id") String id) {
		return this.manager.configuration(id);
	}
	
	@Path("/{id}/remove")
	@DELETE
	public List<TunnelReference> removeTunnelById(@PathParam("id") String id) {
		this.manager.delete(id);
		return this.info();
	}
	
	@Path("/{id}/pause")
	@POST
	public TunnelReference pauseTunnelById(@PathParam("id") String id) {
		return this.manager.pause(id);
	}
	
	@Path("/{id}/resume")
	@POST
	public TunnelReference resumeTunnelById(@PathParam("id") String id) {
		return this.manager.resume(id);
	}
	
	@Path("/start")
	@PUT
	public TunnelReference start(TunnelConfiguration configuration) {
		final TunnelReference ref = this.manager.create(configuration, true);
		return ref;
	}
	
	@Path("/{id}/update")
	@PUT
	public TunnelReference start(@PathParam("id") String id, TunnelConfiguration configuration) {
		final TunnelReference ref = this.manager.update(id, configuration);
		return ref;
	}
}

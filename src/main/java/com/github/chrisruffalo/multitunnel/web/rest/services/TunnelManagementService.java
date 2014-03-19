package com.github.chrisruffalo.multitunnel.web.rest.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelBootstrap;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelReference;
import com.github.chrisruffalo.multitunnel.tunnel.TunnelManager;
import com.github.chrisruffalo.multitunnel.util.InterfaceHelper;

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
	
	@Path("/blocked") 
	@GET
	public Set<Integer> getBlockedPorts() {
		return this.manager.blocked();
	}
	
	@Path("/interfaces")
	@GET
	public Map<String, Set<String>> getValidInterfaces() {
		return InterfaceHelper.INSTANCE.validInterfaces();
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
	@Produces(MediaType.TEXT_PLAIN)
	public boolean removeTunnelById(@PathParam("id") String id) {
		this.manager.stop(id);
		return true;
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
		TunnelReference ref = this.manager.create(configuration);
		return ref;
	}
	
	@Path("/{id}/update")
	@PUT
	public TunnelReference start(@PathParam("id") String id, TunnelConfiguration configuration) {
		TunnelReference ref = this.manager.update(id, configuration);
		return ref;
	}
}

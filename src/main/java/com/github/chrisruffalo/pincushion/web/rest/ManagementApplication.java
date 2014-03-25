package com.github.chrisruffalo.pincushion.web.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.github.chrisruffalo.pincushion.web.rest.services.InfoService;
import com.github.chrisruffalo.pincushion.web.rest.services.TunnelManagementService;

public class ManagementApplication extends Application {

	private final Set<Class<?>> types;
	
	private final Set<Object> singletons;
	
	public ManagementApplication() {
		// add types
		this.types = new HashSet<>();
		this.types.add(InfoService.class);
		this.types.add(TunnelManagementService.class);

		// create singletons
		this.singletons = new HashSet<>();
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		return this.types;
	}
	
	@Override
	public Set<Object> getSingletons() {
		return this.singletons;
	}

}

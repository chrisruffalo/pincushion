package com.github.chrisruffalo.pincushion.web.support;

import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;

import javax.servlet.ServletContextEvent;

import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;

import com.github.chrisruffalo.pincushion.tunnel.TunnelManager;

public class ResteasyBootstrapInstanceFactory implements InstanceFactory<ResteasyBootstrap> {

	private final TunnelManager manager;
	
	public ResteasyBootstrapInstanceFactory(TunnelManager manager) {
		this.manager = manager;
	}
	
	@Override
	public InstanceHandle<ResteasyBootstrap> createInstance() throws InstantiationException {
		
		// create bootstrap
		final ResteasyBootstrap bootstrap = new ResteasyBootstrap(){
            @Override
            public void contextInitialized(ServletContextEvent event) {
                super.contextInitialized(event);
                deployment.getDispatcher().getDefaultContextObjects().put(TunnelManager.class, manager);
            }
	    };

	    // create new instance handle for bootstrap
		InstanceHandle<ResteasyBootstrap> handle = new InstanceHandle<ResteasyBootstrap>() {
			@Override
			public ResteasyBootstrap getInstance() {
				return bootstrap;
			}

			@Override
			public void release() {
				// nothing to do
			}
			
		};
		
		return handle;
	}

}

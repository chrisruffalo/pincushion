package com.github.chrisruffalo.pincushion.web;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.DeflateEncodingProvider;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;

import javax.servlet.ServletException;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chrisruffalo.pincushion.model.configuration.PincushionConfiguration;
import com.github.chrisruffalo.pincushion.tunnel.TunnelManager;
import com.github.chrisruffalo.pincushion.web.rest.ManagementApplication;
import com.github.chrisruffalo.pincushion.web.support.ResteasyBootstrapInstanceFactory;

/**
 * Starts (and manages) the Pincushion web interface using Undertow.
 *
 */
public class ManagementInterface {

	private final String managementInterface;
	
	private final int managementPort;
	
	private final Logger logger;
	
	private final TunnelManager manager;
	
	private Undertow server;

    /**
     * Create a new instance of the web management interface.  Requires the
     * manager for the tunnels and derives some values from the root
     * Pincushion configuration.
     *
     * @param manager tunnel manager to manage tunnels with
     * @param config the pincushion configuration
     */
	public ManagementInterface(TunnelManager manager, PincushionConfiguration config) {
		this.managementInterface = config.getManagementInterface();
		this.managementPort = config.getManagementPort();
		
		this.manager = manager;
		
		this.logger = LoggerFactory.getLogger("management [" + this.managementInterface + ":" + this.managementPort + "]");
	}

    /**
     * Start the management interface.
     *
     */
	public void start() {
		
		Builder builder = Undertow.builder();
		
		// add listener
		builder.addHttpListener(this.managementPort, this.managementInterface);
		
		// set up resource handler
		ClassPathResourceManager classPathManager = new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), "web");
		ResourceHandler resources = Handlers.resource(classPathManager);
		resources.setWelcomeFiles("index.html");
		resources.setDirectoryListingEnabled(false);
		
		// set up servlet handler
		DeploymentInfo servletBuilder = Servlets.deployment()
				.setClassLoader(Thread.currentThread().getContextClassLoader())
				.setContextPath("/services")
				.setDeploymentName("pincushion-services.war")
				;
		
		// add context parameters
		//servletBuilder.addInitParameter(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX, "/");
		
		// add context listener for bootstrap
		ListenerInfo restBootstrapListener = Servlets.listener(ResteasyBootstrap.class, new ResteasyBootstrapInstanceFactory(this.manager));
		servletBuilder.addListener(restBootstrapListener);
		
		// create and add servlet to servlet handler
		ServletInfo restServletInfo = Servlets.servlet("restEasyHandler", HttpServletDispatcher.class);
		restServletInfo.addInitParam("javax.ws.rs.Application", ManagementApplication.class.getName());
		restServletInfo.addMapping("/*");
		restServletInfo.setLoadOnStartup(1);
		servletBuilder.addServlet(restServletInfo);
		
		// deploy servlets
		DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(servletBuilder);
		
		deploymentManager.deploy();
		HttpHandler services = null;
		try {
			services = deploymentManager.start();
		} catch (ServletException e) {
			this.logger.error("Could not start services deployment: {}", e.getLocalizedMessage());
		}

		// create paths
		PathHandler path = Handlers.path(resources);
		if(services != null) {
			path.addPrefixPath("/services", services);
		}
		
		// create encoding repository
		ContentEncodingRepository repo = new ContentEncodingRepository();

        // permit gzip and deflate
		repo.addEncodingHandler("gzip", new GzipEncodingProvider(), 1000);
		repo.addEncodingHandler("deflate", new DeflateEncodingProvider(), 10);
		
		// ensure that everything can be encoded with appropriate compression
		EncodingHandler encoder = new EncodingHandler(path, repo);
		
		// add path handler to server
		builder.setHandler(encoder);
		
		// start undertow server
		this.server = builder.build();
		this.server.start();
		
		this.logger.info("Started undertow management interface at {} on port {}", this.managementInterface, this.managementPort);
	}

    /**
     * Stop the web management interface.
     *
     */
    public void stop() {
        if(this.server != null) {
            this.server.stop();
            this.logger.info("Stopped undertow management interface");
        }
    }
}

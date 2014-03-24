package com.github.chrisruffalo.multitunnel.tunnel;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chrisruffalo.multitunnel.exception.HydraException;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelBootstrap;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelReference;
import com.github.chrisruffalo.multitunnel.tunnel.impl.Tunnel;
import com.github.chrisruffalo.multitunnel.util.InterfaceHelper;
import com.github.chrisruffalo.multitunnel.util.PortHelper;

public class TunnelManager {
	
	private final Map<String, Tunnel> tunnels;

	private final EventLoopGroup workerGroup;
	
	private final Logger logger;
	
	public TunnelManager(EventLoopGroup workerGroup, Integer... blockedPorts) {
		this.tunnels = new HashMap<>();
		this.workerGroup = workerGroup;
		
		this.logger = LoggerFactory.getLogger("tunnel manager");
	}
	
	public TunnelReference create(final TunnelConfiguration config) {
		// make sure a configuration has been provided
		if(config == null || config.getName() == null || config.getName().isEmpty()) {
			String message = String.format("An invalid configuration was provided.  A configuration must be non-null, contain a non-null name, and a non-null port.");
			
			this.logger.error(message);
			
			// throw error?
			throw new HydraException(message);
		}
		
		// make sure we have a source port
		if(config.getSourcePort() == null) {
			// create message
			String message = String.format("No source port was provided with configuration named '%s'", config.getName());
			
			this.logger.error(message);
			
			// throw error
			throw new HydraException(message);
		}
		
		// make sure the interface is something sane
		String iface = config.getSourceInterface();
		if(iface == null || iface.isEmpty()) {
			iface = "0.0.0.0";
			this.logger.warn("Empty or null interface is converted to default '0.0.0.0'");
		}
		String goodIface =  InterfaceHelper.INSTANCE.sanitize(iface);
		if(!iface.equalsIgnoreCase(goodIface)) {
			this.logger.warn("The interface '{}' has been automatically (safely) converted to '{}'. (If this is not desired, choose a correct interface)", iface, goodIface);
		}
				
		// check for blocked ports
		Integer port = config.getSourcePort();
		if(port == null || port < 0) {
			// create message
			String message = String.format("configuration with name '%s' can must have a non-null port between 1 and 65536", config.getName());

			// log error
			this.logger.error(message);
			
			// throw error
			throw new HydraException(message);
		}
		boolean available = PortHelper.INSTANCE.available(goodIface, port);
		if(!available) {
			// create message
			String message = String.format("configuration with name '%s' is attempting to use port '%d' on interface '%s' which is already in use", config.getName(), port, goodIface);

			// log error
			this.logger.error(message);
			
			// throw error
			throw new HydraException(message);
		}
			
		// destination host
		if(config.getDestHost() == null || config.getDestHost().isEmpty()) {
			// create message
			String message = String.format("configuration with name '%s' is and source port '%d' should have a valid destination host", config.getName(), config.getSourcePort());

			// log error
			this.logger.error(message);
			
			// throw error
			throw new HydraException(message);
		}
		
		// destination port
		if(config.getDestPort() == null || config.getDestPort() < 1) {
			// create message
			String message = String.format("configuration with name '%s' is and source port '%d' should have a valid destination port", config.getName(), config.getSourcePort());

			// log error
			this.logger.error(message);
			
			// throw error
			throw new HydraException(message);
		}
		
		// create tunnel
		Tunnel tunnel = new Tunnel(new NioEventLoopGroup(2), this.workerGroup, config);
		
		// start tunnel
		boolean result = tunnel.start();
		
		// save result in list and return reference
		if(result) {
			// save
			this.tunnels.put(tunnel.id(), tunnel);
		
			TunnelReference reference = new TunnelReference();
			
			// set up reference
			reference.setConfigruation(tunnel.configuration());
			reference.setStats(tunnel.stats());
			
			return reference;
		}
				
		// no reference because nothing was saved
		return null;
	}
	
	public void stop(String id) {
		Tunnel tunnel = this.tunnels.get(id);
		
		if(tunnel != null) {
			tunnel.stop();
			
			this.tunnels.remove(tunnel.id());
		} else {
			this.logger.info("could not stop tunnel id:{}", id);
		}
	}
	
	public TunnelReference pause(String id) {
		Tunnel tunnel = this.tunnels.get(id);
		
		if(tunnel != null) {
			tunnel.pause();
			return tunnel.ref();
		}
		
		return null;
	}
	

	public TunnelReference resume(String id) {
		Tunnel tunnel = this.tunnels.get(id);
		
		if(tunnel != null) {
			tunnel.resume();
			return tunnel.ref();
		}
		return null;
	}
	
	public List<TunnelReference> info() {
		
		if(this.tunnels.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<TunnelReference> configurations = new ArrayList<>(this.tunnels.size());
		
		for(Tunnel t : this.tunnels.values()) {
			configurations.add(t.ref());
		}
		
		return configurations;
	}

	public TunnelReference get(String id) {
		Tunnel tunnel = this.tunnels.get(id);
		
		if(tunnel != null) {
			return tunnel.ref();
		}		
		
		return null;
	}

	public TunnelReference update(String id, TunnelConfiguration configuration) {
		this.stop(id);
		return this.create(configuration);
	}

	public TunnelConfiguration configuration(String id) {
		Tunnel tunnel = this.tunnels.get(id);
		
		if(tunnel != null) {
			return tunnel.configuration();
		}
		
		return new TunnelConfiguration();
	}
	
	public TunnelBootstrap bootstrap() {
		return this.createBootstrap(null);
	}
	
	public TunnelBootstrap bootstrap(String id) {
		return this.createBootstrap(this.configuration(id));
	}
	
	private TunnelBootstrap createBootstrap(TunnelConfiguration configuration) {
		TunnelBootstrap bootstrap = new TunnelBootstrap();
	
		// only set configuration if configuration is not null		
		if(configuration != null) {
			bootstrap.setConfiguration(configuration);
		}
		
		bootstrap.setValidInterfaces(InterfaceHelper.INSTANCE.validInterfaces());
		
		return bootstrap;
	}
	
}

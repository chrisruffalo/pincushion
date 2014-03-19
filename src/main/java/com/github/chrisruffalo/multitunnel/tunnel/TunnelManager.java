package com.github.chrisruffalo.multitunnel.tunnel;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chrisruffalo.multitunnel.exception.HydraException;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelBootstrap;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelReference;
import com.github.chrisruffalo.multitunnel.tunnel.impl.Tunnel;
import com.github.chrisruffalo.multitunnel.util.InterfaceHelper;

public class TunnelManager {
	
	private final Map<String, Tunnel> tunnels;

	private final Set<Integer> blocked;
	
	private final EventLoopGroup workerGroup;
	
	private final Logger logger;
	
	public TunnelManager(EventLoopGroup workerGroup, Integer... blockedPorts) {
		this.tunnels = new HashMap<>();
		this.workerGroup = workerGroup;
		
		this.blocked = new HashSet<>();
		this.blocked.addAll(Arrays.asList(blockedPorts));
		
		this.logger = LoggerFactory.getLogger("tunnel manager");
	}
	
	public TunnelReference create(TunnelConfiguration config) {
		if(config == null || config.getName() == null || config.getName().isEmpty()) {
			String message = String.format("An invalid configuration was provided.  A configuration must be non-null, contain a non-null name, and a non-null port.");
			
			this.logger.error(message);
			
			// throw error?
			throw new HydraException(message);
		}
		
		if(config.getSourcePort() == null) {
			// create message
			String message = String.format("No source port was provided with configuration named '{}'", config.getName());
			
			this.logger.error(message);
			
			// throw error?
			throw new HydraException(message);
		}
		
		int port = config.getSourcePort();
		if(blocked.contains(port) || tunnels.containsKey(port)) {
			// create message
			String message = String.format("configuration with name '%s' is attempting to use port '%d' which is already in use", config.getName(), config.getSourcePort());
			
			// log error
			this.logger.error(message);
			
			// throw error?
			throw new HydraException(message);
		}	
		
		TunnelReference reference = new TunnelReference();
		
		// create tunnel
		Tunnel tunnel = new Tunnel(new NioEventLoopGroup(2), this.workerGroup, config);
		
		// save
		this.tunnels.put(tunnel.id(), tunnel);
		this.blocked.add(config.getSourcePort());
		
		// start tunnel
		tunnel.start();
		
		// set up reference
		reference.setConfigruation(tunnel.configuration());
		reference.setStats(tunnel.stats());
		
		return reference;
	}
	
	public void stop(String id) {
		Tunnel tunnel = this.tunnels.get(id);
		
		if(tunnel != null) {
			tunnel.stop();
			
			this.tunnels.remove(tunnel.id());
			
			this.blocked.remove(tunnel.configuration().getSourcePort());
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
		
		bootstrap.setBlockedPorts(this.blocked());
		bootstrap.setValidInterfaces(InterfaceHelper.INSTANCE.validInterfaces());
		
		return bootstrap;
	}

	public Set<Integer> blocked() {
		return Collections.unmodifiableSet(this.blocked);
	}
	
	
	
}

package com.github.chrisruffalo.multitunnel.tunnel;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.HashMap;
import java.util.Map;

import com.github.chrisruffalo.multitunnel.model.TunnelConfiguration;
import com.github.chrisruffalo.multitunnel.tunnel.impl.Tunnel;

public class TunnelManager {
	
	private final Map<Integer, Tunnel> tunnels;

	private EventLoopGroup workerGroup;
	
	public TunnelManager(EventLoopGroup workerGroup) {
		this.tunnels = new HashMap<>();
		this.workerGroup = workerGroup;
	}
	
	public void create(TunnelConfiguration config) {
		// create tunnel
		Tunnel tunnel = new Tunnel(new NioEventLoopGroup(1), this.workerGroup, config);
		
		// save
		this.tunnels.put(config.getSourcePort(), tunnel);
		
		// start tunnel
		tunnel.start();
	}
	
	public void stop(int port) {
		Tunnel tunnel = this.tunnels.get(port);
		
		if(tunnel != null) {
			tunnel.stop();
			
			this.tunnels.remove(port);
		}
	}
	
}

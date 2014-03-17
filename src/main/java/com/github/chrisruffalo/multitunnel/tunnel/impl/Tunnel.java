package com.github.chrisruffalo.multitunnel.tunnel.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chrisruffalo.multitunnel.client.ClientFactory;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelStatistics;

public class Tunnel {

	private final EventLoopGroup bossGroup;
	
	private final EventLoopGroup workerGroup;
	
	private final int port;

	private final String destinationHost;
	
	private final int destinationPort;
	
	private ChannelFuture channelFuture;
	
	private final Logger logger;
	
	private final TunnelConfiguration configuration;
	
	private StatisticsCollector collector;
	
	public Tunnel(EventLoopGroup bossGroup, EventLoopGroup workerGroup, TunnelConfiguration configuration) {
		this.bossGroup = bossGroup;
		this.workerGroup = workerGroup;
		this.port = configuration.getSourcePort();
		this.destinationHost = configuration.getDestHost();
		this.destinationPort = configuration.getDestPort();
		
		this.configuration = configuration;
		
		this.logger = LoggerFactory.getLogger("tunnel [" + port + "] => [" + destinationHost + ":" + destinationPort + "]");
	}
	
	public void start() {
		// create new client factory
		final ClientFactory factory = new ClientFactory(this.workerGroup);
		
		// create
		this.collector = new StatisticsCollector();
		
		// create server bootstrap
        ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup)
         .channel(NioServerSocketChannel.class)
         .childHandler(new ChannelInitializer<SocketChannel>() { 
			@Override
            public void initChannel(SocketChannel ch) throws Exception {
				// add stats collector to head of pipeline
				ch.pipeline().addFirst("stats", collector);
				
				// create forwarding client bootstrapper 
				Bootstrap bootstrap = factory.bootstrap(destinationHost, destinationPort);
					
				// log
				//ch.pipeline().addLast(new LoggingHandler("forward-log", LogLevel.INFO));
				
				// add a forwarder from this server connection to the client
                ch.pipeline().addLast("request-forwarder", new RequestForwarder(bootstrap));
            }
         })
         .option(ChannelOption.SO_BACKLOG, 256) 
         .option(ChannelOption.SO_KEEPALIVE, true)
         .option(ChannelOption.SO_REUSEADDR, true)
         .childOption(ChannelOption.TCP_NODELAY, true)
         .childOption(ChannelOption.AUTO_READ, false)
         ;

		try {
			this.channelFuture = b.bind(this.port).sync();

			this.logger.info("started");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		if(this.channelFuture == null) {
			return;
		}
		this.channelFuture.channel().close();
		this.channelFuture = null;
		
		this.logger.info("stopped");
	}

	public TunnelConfiguration configuration() {
		return this.configuration;
	}

	public TunnelStatistics stats() {
		return this.collector.collect();
	}
	
}

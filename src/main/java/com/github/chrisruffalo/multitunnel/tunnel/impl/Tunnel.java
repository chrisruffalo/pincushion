package com.github.chrisruffalo.multitunnel.tunnel.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.chrisruffalo.multitunnel.client.ClientFactory;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelReference;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelStatistics;
import com.github.chrisruffalo.multitunnel.tunnel.impl.control.PauseController;
import com.github.chrisruffalo.multitunnel.tunnel.impl.control.StatisticsCollector;
import com.github.chrisruffalo.multitunnel.tunnel.impl.forward.RequestForwarder;

public class Tunnel {

	private final String id;
	
	private final EventLoopGroup bossGroup;
	
	private final EventLoopGroup workerGroup;
	
	private final String sourceInterface;
	
	private final int sourcePort;

	private final String destinationHost;
	
	private final int destinationPort;
	
	private ChannelFuture channelFuture;
	
	private final Logger logger;
	
	private final TunnelConfiguration configuration;
	
	private StatisticsCollector collector;
	
	private PauseController pauseController;
	
	private TunnelStatus status;
	
	public Tunnel(EventLoopGroup bossGroup, EventLoopGroup workerGroup, TunnelConfiguration configuration) {
		this.id = UUID.randomUUID().toString();
		
		this.bossGroup = bossGroup;
		this.workerGroup = workerGroup;
		this.sourceInterface = configuration.getSourceInterface();
		this.sourcePort = configuration.getSourcePort();
		this.destinationHost = configuration.getDestHost();
		this.destinationPort = configuration.getDestPort();
		
		this.configuration = configuration;
		
		this.logger = LoggerFactory.getLogger("tunnel [" + this.sourceInterface + ":" + this.sourcePort + "] => [" + this.destinationHost + ":" + this.destinationPort + "]");
	}
	
	public void start() {
		// create new client factory
		final ClientFactory factory = new ClientFactory(this.workerGroup);
		
		// create pipeline components
		this.collector = new StatisticsCollector();
		this.pauseController = new PauseController();
		
		// create server bootstrap
        ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup)
         .channel(NioServerSocketChannel.class)
         .childHandler(new ChannelInitializer<SocketChannel>() { 
			@Override
            public void initChannel(SocketChannel ch) throws Exception {
				// add stats collector to head of pipeline
				ch.pipeline().addFirst("stats", collector);
				
				// add pause controller even before that...
				ch.pipeline().addFirst("pause", pauseController);
				
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
			this.channelFuture = b.bind(this.sourceInterface, this.sourcePort).sync();

			this.logger.info("started");
			
			// set status
			this.status = TunnelStatus.RUNNING;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void pause() {
		this.pauseController.pause(true);
		this.logger.info("paused");
		this.status = TunnelStatus.PAUSED;
	}
	
	public void resume() {
		this.pauseController.pause(false);
		this.logger.info("resumed");
		this.status = TunnelStatus.RUNNING;
	}
	
	public void stop() {
		if(this.channelFuture == null) {
			return;
		}
		this.channelFuture.channel().close();
		this.channelFuture = null;
		
		this.logger.info("stopped");
		this.status = TunnelStatus.STOPPED;
	}

	public TunnelConfiguration configuration() {
		return this.configuration;
	}

	public TunnelStatistics stats() {
		return this.collector.collect();
	}
	
	public String id() {
		return this.id;
	}

	public TunnelStatus status() {
		return this.status;
	}

	public TunnelReference ref() {
		TunnelReference ref = new TunnelReference();
		
		ref.setId(this.id);
		ref.setStats(this.collector.collect());
		ref.setStatus(this.status);
		ref.setConfigruation(this.configuration);
		
		return ref;
	}
}

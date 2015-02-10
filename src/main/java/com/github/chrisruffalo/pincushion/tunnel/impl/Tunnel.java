package com.github.chrisruffalo.pincushion.tunnel.impl;

import com.github.chrisruffalo.pincushion.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.pincushion.model.tunnel.TunnelReference;
import com.github.chrisruffalo.pincushion.model.tunnel.TunnelStatistics;
import com.github.chrisruffalo.pincushion.tunnel.impl.control.PauseController;
import com.github.chrisruffalo.pincushion.tunnel.impl.control.StatisticsCollector;
import com.github.chrisruffalo.pincushion.tunnel.impl.initializer.RequestForwardInitializer;
import com.github.chrisruffalo.pincushion.util.InterfaceHelper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class Tunnel {

	private final String id;
	
	private final EventLoopGroup bossGroup;
	
	private final EventLoopGroup workerGroup;
	
	private final String sourceInterface;
	
	private final String bindInterface;
	
	private final String prettyBind;
	
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
		this.bindInterface = InterfaceHelper.INSTANCE.sanitize(this.sourceInterface);
		this.sourcePort = configuration.getSourcePort();
		this.destinationHost = configuration.getDestHost();
		this.destinationPort = configuration.getDestPort();

		this.configuration = configuration;
		
		// create visual representation of binding
		String source = this.sourceInterface;
		if(!source.equals(this.bindInterface)) {
			source += " (" + this.bindInterface + ")";
			this.prettyBind = source;
		} else {
			this.prettyBind = null;
		}
		
		this.logger = LoggerFactory.getLogger("tunnel [" + source + ":" + this.sourcePort + "] => [" + this.destinationHost + ":" + this.destinationPort + "]");
	}
	
	public boolean start() {
		// create pipeline components
		this.collector = new StatisticsCollector();
		this.pauseController = new PauseController();
		
		// pre-resolve destination host
		String localAddress = destinationHost;
		try {
            InetAddress address = InetAddress.getByName(localAddress);
            localAddress = address.getHostAddress();
        } catch (UnknownHostException uhe) {
            // do nothing / pass on
        }
		final String localDestination = localAddress;
		this.logger.trace("resolution: {} => {}", destinationHost, localDestination);

        // create request forward initializer
        final RequestForwardInitializer requestForwardInitializer = new RequestForwardInitializer(this.collector, this.pauseController, this.workerGroup, localAddress, this.destinationPort);

		// create server bootstrap
        final ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup)
         .channel(NioServerSocketChannel.class)
         .childHandler(requestForwardInitializer)
         // server options
         .option(ChannelOption.TCP_NODELAY, true)
         .option(ChannelOption.SO_BACKLOG, 256) 
         .option(ChannelOption.SO_KEEPALIVE, true)
         .option(ChannelOption.SO_REUSEADDR, true)
         //.option(ChannelOption.AUTO_READ, false)
         // allocator
         .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
         // magic numbers
         .option(ChannelOption.SO_SNDBUF, 1048576)
         .option(ChannelOption.SO_RCVBUF, 1048576)
         .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 655360)         
         // child options
         .childOption(ChannelOption.TCP_NODELAY, true)
         .childOption(ChannelOption.AUTO_READ, false)
         .childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 655360)
         // allocator
         .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
         ;

		try {
			// bind and read if successful
			this.channelFuture = b.bind(this.bindInterface, this.sourcePort).sync();

			this.logger.info("started");
			
			// set status
			this.status = TunnelStatus.RUNNING;
			
			// add to manager
			return true;
		} catch (InterruptedException e) {
			this.logger.error("Error while starting tunnel: {}", e.getMessage(), e);
			this.status = TunnelStatus.ERROR;
		}
		
		// could not start
		return false;
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

	public String bind() {
        return this.bindInterface;
	}
	
	public String prettyBind() {
		if(this.prettyBind != null && !this.prettyBind.isEmpty()) {
			return this.prettyBind;
		}
		return this.bindInterface;
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
		final TunnelReference ref = new TunnelReference();
		
		ref.setId(this.id);
		ref.setBind(this.prettyBind());
		ref.setStats(this.collector.collect());
		ref.setStatus(this.status);
		ref.setConfigruation(this.configuration);
		
		return ref;
	}
}

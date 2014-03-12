package com.github.chrisruffalo.multitunnel.tunnel;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TunnelServer {

	private final EventLoopGroup bossGroup;
	
	private final EventLoopGroup workerGroup;
	
	private final int port;

	private final String destinationHost;
	
	private final int destinationPort;
	
	private ChannelFuture channelFuture;
	
	private final Logger logger;
	
	public TunnelServer(int port, String destinationHost, int destinationPort) {
		this(new NioEventLoopGroup(), new NioEventLoopGroup(), port, destinationHost, destinationPort);
	}
	
	public TunnelServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup, int port, String destinationHost, int destinationPort) {
		this.bossGroup = bossGroup;
		this.workerGroup = workerGroup;
		this.port = port;
		this.destinationHost = destinationHost;
		this.destinationPort = destinationPort;
		
		this.logger = LoggerFactory.getLogger("tunnel [" + port + "] => [" + destinationHost + ":" + destinationPort + "]");
	}
	
	public void start() {
        ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup)
         .channel(NioServerSocketChannel.class)
         .childHandler(new ChannelInitializer<SocketChannel>() { 
			@Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new OutboundTunnel(workerGroup, port, destinationHost, destinationPort));
            }
         })
         .option(ChannelOption.SO_BACKLOG, 256) 
         .option(ChannelOption.SO_KEEPALIVE, true)
         .option(ChannelOption.SO_REUSEADDR, true)
         .option(ChannelOption.TCP_NODELAY, true);

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

}

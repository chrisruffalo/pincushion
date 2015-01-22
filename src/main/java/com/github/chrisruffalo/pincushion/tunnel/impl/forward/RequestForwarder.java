package com.github.chrisruffalo.pincushion.tunnel.impl.forward;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;

public class RequestForwarder extends ChannelForwarder {

	private final Bootstrap bootstrap;
	
	private Channel remote;
	
	public RequestForwarder(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}
	
	@Override
	protected Channel target() {
		return this.remote;
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
	
		final Channel target = this.target();
		if(target != null) {
			return;
		}
		
		// save origin channel
		final Channel origin = ctx.channel();
		
		// update bootstrap with pipeline
		this.bootstrap
		.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				// log
				//ch.pipeline().addLast(new LoggingHandler("response-log", LogLevel.INFO));
				
				// add returner that will return values back
				// to origin
				ch.pipeline().addLast("response-forwarder", new ResponseForwarder(origin));
			}
		})
		.option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.SO_KEEPALIVE, true)
        .option(ChannelOption.SO_REUSEADDR, true)
        //.option(ChannelOption.AUTO_READ, false)
        // magic numbers
        .option(ChannelOption.SO_SNDBUF, 1048576)
        .option(ChannelOption.SO_RCVBUF, 1048576)
        .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 655360)
        // allocator
        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        ;
		
		// bootstrap connection
		final ChannelFuture future = this.bootstrap.connect();
		
		// save target
		this.remote = future.channel();
				
		// if channel opens ok, start read
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if(future.isSuccess()) {
					// kick read because remote is open
					origin.read();
				} else {
					origin.close();
				}
			}
		});
		
		// start read
		this.logger().trace("request: starting read");
		
		// forward event
		super.channelActive(ctx);
	}
}

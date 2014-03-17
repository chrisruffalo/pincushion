package com.github.chrisruffalo.multitunnel.tunnel.impl.forward;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;

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
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	
		Channel target = this.target();
		if(target != null) {
			return;
		}

		// save origin channel
		final Channel origin = ctx.channel();
		
		// update bootstrap with pipeline
		this.bootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				// log
				//ch.pipeline().addLast(new LoggingHandler("return-log", LogLevel.INFO));
				
				// add returner that will return values back
				// to origin
				ch.pipeline().addLast("request-forwarder", new ResponseForwarder(origin));
			}
		});
		
		// bootstrap connection
		ChannelFuture future = this.bootstrap.connect();
		
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
	
		// forward event
		super.channelActive(ctx);
	}
	
	
}

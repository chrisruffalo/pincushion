package com.github.chrisruffalo.multitunnel.tunnel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ChannelForwarder extends ChannelHandlerAdapter {

	private final ChannelFuture channelFuture;
	
	private final Logger logger;
	
	public ChannelForwarder(final ChannelFuture channelFuture) {
		this.channelFuture = channelFuture;
		this.logger = LoggerFactory.getLogger("channel forwarder");
	}
	
	protected Channel target() {
		return this.channelFuture.channel();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = this.target();
		if(channel == null || !channel.isWritable()) {
			this.logger.error("target connection is not available during activation, closing");
			ctx.close();
		} else {
			this.logger.trace("route {} -> {} established", ctx.channel().id(), channel.id());
		}
	
		// forward event
		super.channelActive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Channel channel = this.target();
		if(channel == null || !channel.isWritable()) {
			this.logger.trace("target channel is not available for writing, closing");
			ctx.close();
		}		
		
		// retain the reference so it can be forwarded
		ReferenceCountUtil.retain(msg);
		// forward the msg
		channel.writeAndFlush(msg);
		
		// forward event
		super.channelRead(ctx, msg);
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		// disconnect forwarding channel
		Channel channel = this.target();
		if(channel != null && channel.isOpen()) {
			this.logger.trace("disconnecting target channel");
			ctx.disconnect();
		}
		
		// forward event
		super.disconnect(ctx, promise);
	}

	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		// close forwarding channel
		Channel channel = this.target();
		if(channel != null && channel.isOpen()) {
			this.logger.trace("closing target channel");
			ctx.close();
		}
		
		super.close(ctx, promise);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)	throws Exception {
		// got an error
		this.logger.error("error: {}", cause.getMessage(), cause);
		
		// close target
		Channel channel = this.target();
		if(channel != null && channel.isOpen()) {
			this.logger.info("closing target channel");
			ctx.close();
		}
				
		super.exceptionCaught(ctx, cause);
	}	
		
}

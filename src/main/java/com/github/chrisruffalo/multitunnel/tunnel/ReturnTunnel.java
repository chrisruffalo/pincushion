package com.github.chrisruffalo.multitunnel.tunnel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReturnTunnel extends ChannelInboundHandlerAdapter {

	private final ChannelHandlerContext returnContext;
	
	private final Logger logger;
	
	public ReturnTunnel(ChannelHandlerContext ctx) {
		this.returnContext = ctx;
		
		this.logger = LoggerFactory.getLogger("return-" + UUID.randomUUID().toString());
	}
	
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
		this.logger.trace("returning to origin");
		
		// write
		this.returnContext.write(msg);
		this.returnContext.flush();
	}
	
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        this.logger.error("Error: {}", cause.getMessage(), cause);
        ctx.close();
    }
	
}

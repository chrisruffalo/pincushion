package com.github.chrisruffalo.multitunnel.tunnel.impl;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class ResponseForwarder extends ChannelForwarder {

	private final Channel channel;
	
	public ResponseForwarder(final Channel channel) {
		this.channel = channel;
	}
	
	protected Channel target() {
		return this.channel;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	    ctx.read();
	    ctx.write(Unpooled.EMPTY_BUFFER);
	}
}

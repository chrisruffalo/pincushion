package com.github.chrisruffalo.multitunnel.tunnel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class ResponseForwarder extends ChannelForwarder {

	public ResponseForwarder(ChannelFuture channelFuture) {
		super(channelFuture);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = this.target();
		if(channel == null) {
			return;
		}
		
		// close response channel
		if(channel.isActive() || channel.isOpen()) {
			channel.close();
		}
		
		// forward event
		super.channelInactive(ctx);
	}

	
}

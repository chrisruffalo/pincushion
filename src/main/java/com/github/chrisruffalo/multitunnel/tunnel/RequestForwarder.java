package com.github.chrisruffalo.multitunnel.tunnel;

import io.netty.channel.ChannelFuture;

public class RequestForwarder extends ChannelForwarder {

	public RequestForwarder(ChannelFuture channelFuture) {
		super(channelFuture);
	}

	
	
}

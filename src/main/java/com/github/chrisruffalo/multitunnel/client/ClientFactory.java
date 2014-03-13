package com.github.chrisruffalo.multitunnel.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientFactory {

	private final Bootstrap bootstrap;
	
	public ClientFactory(EventLoopGroup group) {
		// create remote connection details
		this.bootstrap = new Bootstrap()
        	.channel(NioSocketChannel.class)
        	.group(group)
        	.option(ChannelOption.TCP_NODELAY, true)
        	.option(ChannelOption.AUTO_READ, false)
	        ;
	}
	
	public Bootstrap bootstrap(String destinationHost, int destinationPort) {
		
		// create
		Bootstrap b = this.bootstrap.clone().remoteAddress(destinationHost, destinationPort)
		;
        
		// return
        return b.clone();
	}
	
}

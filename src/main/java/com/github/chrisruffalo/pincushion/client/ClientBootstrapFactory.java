package com.github.chrisruffalo.pincushion.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public enum ClientBootstrapFactory {

    INSTANCE;

	private final Bootstrap bootstrap;
	
	private ClientBootstrapFactory() {
		// create remote connection details
		this.bootstrap = new Bootstrap()
        	.channel(NioSocketChannel.class)
        	.option(ChannelOption.TCP_NODELAY, true)
        	.option(ChannelOption.AUTO_READ, false)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.SO_REUSEADDR, true)
            // magic numbers for "better" connections
            .option(ChannelOption.SO_SNDBUF, 1048576)
            .option(ChannelOption.SO_RCVBUF, 1048576)
            .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 655360)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
	        ;
	}
	
	public Bootstrap bootstrap(EventLoopGroup group, String destinationHost, int destinationPort) {
		// create based on previous bootstrap
		final Bootstrap b = this.bootstrap
            .clone()
            .group(group)
            .remoteAddress(destinationHost, destinationPort)
		    ;

		// return bootstrap with destination host and address set
        return b;
	}
	
}

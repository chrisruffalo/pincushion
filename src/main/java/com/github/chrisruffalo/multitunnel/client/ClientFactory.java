package com.github.chrisruffalo.multitunnel.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.github.chrisruffalo.multitunnel.tunnel.ResponseForwarder;

public class ClientFactory {

	private final Bootstrap bootstrap;
	
	public ClientFactory(final EventLoopGroup workerGroup) {
		// create remote connection details
		this.bootstrap = new Bootstrap()
        	.group(workerGroup)
        	.channel(NioSocketChannel.class)
	        .option(ChannelOption.TCP_NODELAY, true)
	        .option(ChannelOption.SO_KEEPALIVE, true);
	}
	
	public ChannelFuture client(String destinationHost, int destinationPort, final ChannelFuture target) {
		
		// create
		Bootstrap b = this.bootstrap.clone()
		.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
            	ch.pipeline().addLast("return-channel", new ResponseForwarder(target));
            }
        });
		
		// connect to remote
		ChannelFuture future = b.connect(destinationHost, destinationPort).syncUninterruptibly();
        
		// return
        return future;
	}
	
}

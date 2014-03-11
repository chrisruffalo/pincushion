package com.github.chrisruffalo.multitunnel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tunnel extends ChannelInboundHandlerAdapter {
	
	private final Logger logger;
	
	private final int destinationPort;
	
	private final String destinationHost;
	
	private final ChannelFuture channelFuture;
	
	public Tunnel(EventLoopGroup workerGroup, int sourcePort, String destinationHost, int destinationPort) {
		// save values
		this.destinationPort = destinationPort;
		this.destinationHost = destinationHost;
		
		// create descriptive logger
		this.logger = LoggerFactory.getLogger("outbound [" + sourcePort + "] => [" + destinationHost + ":" + destinationPort + "]");
		
		// connect
		Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
            }
        });

        // start the client and keep the channel for writing to
        try {
			this.channelFuture = b.connect(this.destinationHost, this.destinationPort).sync();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}       
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		
		// add write back to pipeline
		Channel local = this.channelFuture.channel();
		TunnelReturn returner = new TunnelReturn(ctx);
		local.pipeline().addLast(returner);
	}

	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
		this.logger.trace("forwarding to client");
		
		// forward input to client pipeline
		Channel local = this.channelFuture.channel();
		local.write(msg);
		local.flush();
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        this.logger.error("Error: {}", cause.getMessage(), cause);
        ctx.close();
    }

}
